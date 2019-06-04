package de.foellix.aql.noah.sourcesandsinks;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import de.foellix.aql.Log;
import de.foellix.aql.helper.Helper;
import de.foellix.aql.noah.Triple;

public class SourceAndSinkFinder {
	private File apkFile;

	private List<String> needles;
	private List<Triple> allTriples;

	public SourceAndSinkFinder(String apkFile) {
		this.apkFile = new File(apkFile);

		this.needles = new ArrayList<>();
		this.allTriples = new ArrayList<>();

		final Unzipper unzipper = new Unzipper();
		final Disassembler disassembler = new Disassembler();

		final Collection<File> unzippedFiles = unzipper.unzip(this.apkFile);
		final Collection<File> disassembledFiles = disassembler.disassemble(unzippedFiles);
		findSourcesAndSinks(disassembledFiles);

		// Delete temporary files
		for (final File toDelete : unzippedFiles) {
			toDelete.delete();
		}
		for (final File toDelete : disassembledFiles) {
			toDelete.delete();
		}
	}

	private void findSourcesAndSinks(Collection<File> disassembledFiles) {
		for (final File disassembledFile : disassembledFiles) {
			findSourcesAndSinks(disassembledFile.getAbsolutePath());
		}
	}

	private void findSourcesAndSinks(String disassembledFile) {
		final Collection<Triple> triples = new ArrayList<>();

		final Path filePath = Paths.get(disassembledFile);
		try {
			final List<String> lines = Files.readAllLines(filePath, StandardCharsets.UTF_8);
			final StringBuilder sb = new StringBuilder();
			boolean start = false;
			for (String line : lines) {
				if (line.equals("Contents of section .rodata:")) {
					start = true;
					continue;
				} else if (!start) {
					continue;
				}

				line = line.substring(line.lastIndexOf("  ") + 2);
				sb.append(line);
			}
			final List<String> needles = new ArrayList<>(Arrays.asList(sb.toString().split("\\.")));

			String function = null;
			String last = null;
			String fromClass = null;
			for (int i = 0; i < needles.size(); i++) {
				final String needle = needles.get(i).replaceAll(":", "");
				if (needle.matches("[a-zA-Z0-9]+")) {
					last = needle;
				} else if (needle.contains("(") && needle.contains(")") && needle.matches("[a-zA-Z0-9/;()]+")) {
					if (last != null) {
						function = last + needle;
					}
				} else if (needle.contains("/") && needle.matches("[a-zA-Z0-9/.]+")) {
					fromClass = needle;
					function = null;
				}

				if (fromClass != null && function != null && !function.startsWith("(")) {
					final String className = fromClass.replaceAll("/", ".");
					String methodName = Helper.cutFromStart(function, ")") + ")";
					if (methodName.contains("(L")) {
						methodName = methodName.replaceAll("\\(L", "(").replaceAll(";L", ";").replaceAll(";\\)", ")")
								.replaceAll(";", ",").replaceAll("/", ".");
					}
					final String returnType;
					if (function.contains(")L")) {
						returnType = Helper.cut(function, ")L").replaceAll(";", "").replaceAll("/", ".");
					} else {
						returnType = "void";
					}
					final Triple triple = new Triple(className, methodName, returnType);

					if (!triples.contains(triple)) {
						triples.add(triple);
					}
				}
			}
		} catch (final IOException e) {
			Log.error("Could not read disassembled shared object: " + filePath.toAbsolutePath() + " ("
					+ e.getClass().getSimpleName() + ": " + e.getMessage() + ")");
		}

		this.allTriples.addAll(triples);
	}

	public List<String> getStrings(boolean filter) {
		if (!filter) {
			return this.needles;
		}

		final List<String> current = new ArrayList<>();
		for (final String needle : this.needles) {
			if (needle.length() > 2 && !needle.startsWith("%") && !needle.startsWith("-")) {
				current.add(needle);
			}
		}
		return current;
	}

	public Collection<Triple> getTriples() {
		return this.allTriples;
	}
}