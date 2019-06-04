package de.foellix.aql.noah.sourcesandsinks;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import de.foellix.aql.Log;
import de.foellix.aql.noah.Triple;

public class SourceAndSinkParser {
	Collection<Triple> triples;

	public SourceAndSinkParser(String file) {
		this.triples = new ArrayList<>();

		final Path susiFile = new File(file).toPath();
		try {
			final List<String> lines = Files.readAllLines(susiFile, Charset.forName("UTF-8"));
			for (final String line : lines) {
				if (!line.startsWith("%") && (line.contains("_SOURCE_") || line.contains("_SINK_"))) {
					String stmStr = "";
					try {
						final String jimpleStr = readLine(line);
						stmStr = jimpleStr.substring(0, jimpleStr.lastIndexOf(")")) + ")";
						final Triple triple = new Triple(stmStr.substring(0, stmStr.indexOf(": ")),
								stmStr.substring(stmStr.lastIndexOf(" ") + 1),
								stmStr.substring(stmStr.indexOf(": ") + 2, stmStr.lastIndexOf(" ")));
						if (!(line.contains("_SOURCE_") && line.contains("_SINK_"))) {
							if (line.contains("_SOURCE_")) {
								triple.setSource(true);
							} else if (line.contains("_SINK_")) {
								triple.setSource(false);
							}
						}
						triple.setJimpleStr("<" + jimpleStr + ">");
						this.triples.add(triple);
					} catch (final Exception e) {
						Log.error("Error while reading the following line: " + stmStr + " ("
								+ e.getClass().getSimpleName() + ": " + e.getMessage() + ")");
					}
				}
			}
		} catch (final IOException e) {
			Log.error("Could not load SuSi File:" + susiFile.toString() + " (" + e.getClass().getSimpleName() + ": "
					+ e.getMessage() + ")");
		}
	}

	private String readLine(String input) {
		input = input.substring(0, (input.lastIndexOf(">") > 0 ? input.lastIndexOf(">") : input.length()));
		return input.substring(input.indexOf("<") + 1, input.lastIndexOf(">"));
	}

	public Collection<Triple> getTriples() {
		return this.triples;
	}
}