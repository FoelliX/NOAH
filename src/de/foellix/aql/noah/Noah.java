package de.foellix.aql.noah;

import static org.fusesource.jansi.Ansi.ansi;
import static org.fusesource.jansi.Ansi.Color.GREEN;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Collection;

import org.fusesource.jansi.AnsiConsole;

import de.foellix.aql.Log;
import de.foellix.aql.Properties;
import de.foellix.aql.datastructure.Answer;
import de.foellix.aql.datastructure.Reference;
import de.foellix.aql.datastructure.handler.AnswerHandler;
import de.foellix.aql.helper.Helper;
import de.foellix.aql.noah.nativecalls.NativeCallFinder;
import de.foellix.aql.noah.sourcesandsinks.SourceAndSinkFinder;
import de.foellix.aql.noah.sourcesandsinks.SourceAndSinkParser;

public class Noah {
	private String apkFile;
	private String sourceAndSinkFile;

	public static void main(String[] args) {
		new Noah(args);
	}

	public Noah(String[] args) {
		// Information
		AnsiConsole.systemInstall();
		final String authorStr1 = "Author: " + Properties.info().AUTHOR;
		final String authorStr2 = "(" + Properties.info().AUTHOR_EMAIL + ")";
		final String centerspace0 = "               "
				.substring(15 - ((int) Math.floor((28 - Properties.info().VERSION.length() - 3) / 2f)));
		final String centerspace1 = "               "
				.substring(15 - ((int) Math.floor((28 - authorStr1.length()) / 2f)));
		final String centerspace2 = "               "
				.substring(15 - Math.max(0, ((int) Math.floor((28 - authorStr2.length()) / 2f))));
		Log.msg(ansi().bold().fg(GREEN)
				.a(" _   _  ____         _    _ \r\n" + "| \\ | |/ __ \\   /\\  | |  | |\r\n"
						+ "|  \\| | |  | | /  \\ | |__| |\r\n" + "| \\ \\ | |  | |/ /\\ \\|  __  |\r\n"
						+ "| |\\  | |__| / ____ \\ |  | |\r\n" + "|_| \\_|\\____/_/    \\_\\|  |_|\r\n")
				.reset().a(centerspace0 + "v. " + Properties.info().VERSION + "\r\n\r\n" + centerspace1 + authorStr1
						+ "\r\n" + centerspace2 + authorStr2 + "\r\n"),
				Log.NORMAL);

		// Get files
		if (args.length <= 0) {
			// TODO: Log
			System.exit(0);
		} else {
			this.apkFile = args[0];
		}

		// Read parameters
		this.sourceAndSinkFile = "data/SourcesAndSinks.txt";
		for (int i = 1; i < args.length; i++) {
			if (args[i].equals("-sas") || args[i].equals("-sourcesandsinks")) {
				i++;
				this.sourceAndSinkFile = args[i];
			} else if (args[i].equals("-d") || args[i].equals("-debug")) {
				i++;
				final String debug = args[i];
				if (debug.equals("normal")) {
					Log.setLogLevel(Log.NORMAL);
				} else if (debug.equals("short")) {
					Log.setLogLevel(Log.NORMAL);
					Log.setShorten(true);
				} else if (debug.equals("warning")) {
					Log.setLogLevel(Log.WARNING);
				} else if (debug.equals("error")) {
					Log.setLogLevel(Log.ERROR);
				} else if (debug.equals("debug")) {
					Log.setLogLevel(Log.DEBUG);
				} else if (debug.equals("detailed")) {
					Log.setLogLevel(Log.DEBUG_DETAILED);
				} else if (debug.equals("special")) {
					Log.setLogLevel(Log.DEBUG_SPECIAL);
				} else {
					Log.setLogLevel(Integer.valueOf(debug).intValue());
				}
			}
		}

		// Create outputFile
		File outputFile = new File(this.apkFile.replaceAll(".apk", "_noah_answer.xml"));
		outputFile = new File("results/" + outputFile.getName());

		Log.msg("NOAH execution started!", Log.NORMAL);

		// Step 1) Find native calls
		Log.msg("Step 1/4: Finding native calls.", Log.NORMAL);
		final NativeCallFinder ncf = new NativeCallFinder(this.apkFile);
		final Collection<Reference> nativeCalls = ncf.findNativeCalls();
		if (nativeCalls != null && !nativeCalls.isEmpty()) {
			if (Log.logIt(Log.DEBUG_DETAILED)) {
				Log.msg("*** Native Calls ***", Log.DEBUG_DETAILED);
				int counter = 0;
				for (final Reference ref : nativeCalls) {
					counter++;
					Log.msg(counter + ") " + Helper.toString(ref), Log.DEBUG_DETAILED);
				}
			} else {
				Log.msg("Native Calls found: " + nativeCalls.size(), Log.NORMAL);
			}

			// Step 2) Parse Sources & Sinks file
			Log.msg("Step 2/4: Parsing sources & sinks file.", Log.NORMAL);
			final SourceAndSinkParser ssp = new SourceAndSinkParser(this.sourceAndSinkFile);
			final Collection<Triple> sourcesAndSinksParsed = ssp.getTriples();
			if (Log.logIt(Log.DEBUG_DETAILED)) {
				Log.msg("*** Sources & Sinks (parsed) ***", Log.DEBUG_DETAILED);
				int counter = 0;
				for (final Triple triple : sourcesAndSinksParsed) {
					counter++;
					Log.msg(counter + ") " + triple.toString(), Log.DEBUG_DETAILED);
				}
			} else {
				Log.msg("Sources & Sinks parsed: " + sourcesAndSinksParsed.size(), Log.NORMAL);
			}

			// Step 3) Disassemble
			Log.msg("Step 3/4: Finding sources & sinks in app.", Log.NORMAL);
			final SourceAndSinkFinder sasf = new SourceAndSinkFinder(this.apkFile);
			final Collection<Triple> sourcesAndSinksFound = sasf.getTriples();
			if (Log.logIt(Log.DEBUG_DETAILED)) {
				Log.msg("*** Sources & Sinks (found) ***", Log.DEBUG_DETAILED);
				int counter = 0;
				for (final Triple triple : sourcesAndSinksFound) {
					counter++;
					Log.msg(counter + ") " + triple.toString(), Log.DEBUG_DETAILED);
				}
			} else {
				Log.msg("Sources & Sinks found: " + sourcesAndSinksFound.size(), Log.NORMAL);
			}

			// Step 4) Over-approximate merge information
			Log.msg("Step 4/4: Computing result.", Log.NORMAL);
			final Overapproximator o = new Overapproximator(nativeCalls, sourcesAndSinksParsed, sourcesAndSinksFound);

			// Step 5) Create and store AQL-Answer
			final Answer answer = new Answer();
			answer.setFlows(o.getFlows());
			AnswerHandler.createXML(answer, outputFile);

			// Step 6) Output adapted SourcesAndSinks.txt
			File copy = new File(outputFile.getAbsolutePath());
			copy = new File(copy.getParentFile(),
					copy.getName().replaceAll("_answer", "_SourcesAndSinks").replaceAll(".xml", ".txt"));
			try {
				Files.deleteIfExists(copy.toPath());
				Files.copy(Paths.get(this.sourceAndSinkFile), copy.toPath());
			} catch (final IOException e) {
				Log.warning("Could not copy " + this.sourceAndSinkFile + " to " + copy.getAbsolutePath() + " ("
						+ e.getClass().getSimpleName() + "): " + e.getMessage());
			}
			try {
				final FileWriter fr = new FileWriter(copy, true);
				fr.write("\n% Added by NOAH\n");
				for (final String sos : o.getNewSourcesAndSinks()) {
					fr.write(sos + "\n");
				}
				fr.close();
			} catch (final IOException e) {
				Log.warning("Could not adapt copied file: " + copy.getAbsolutePath() + " ("
						+ e.getClass().getSimpleName() + ": " + e.getMessage() + ")");
			}

			Log.msg("NOAH's answer:", Log.NORMAL);
			Log.msg(Helper.toString(answer), Log.NORMAL);
		} else {
			if (nativeCalls != null) {
				Log.msg("No sensitive native calls found!", Log.NORMAL);
			} else {
				Log.msg("No native calls found!", Log.NORMAL);
			}

			// Output empty dummy answer
			AnswerHandler.createXML(new Answer(), outputFile);
		}

		Log.msg("NOAH execution finished!", Log.NORMAL);
	}
}
