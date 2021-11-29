package de.foellix.aql.noah.sourcesandsinks;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;

import de.foellix.aql.Log;
import de.foellix.aql.system.ProcessWrapper;

public class Disassembler {
	public Collection<File> disassemble(Collection<File> sharedObjectFiles) {
		final Collection<File> disassembledFiles = new ArrayList<>();
		for (final File sharedObjectFile : sharedObjectFiles) {
			try {
				final File disassembledFile = new File(
						sharedObjectFile.getAbsolutePath().replaceAll(".so", "_disassembled.txt"));
				final File tool = new File("data/tools/run.sh");
				String cmd;
				if (System.getProperty("os.name").toLowerCase().contains("win")) {
					// Use Windows Subsystem for Linux
					cmd = "wsl " + getAbsoluteWSLPath(tool) + " " + getAbsoluteWSLPath(sharedObjectFile) + " "
							+ getAbsoluteWSLPath(disassembledFile);
				} else {
					cmd = tool.getAbsolutePath() + " " + sharedObjectFile.getAbsolutePath() + " "
							+ disassembledFile.getAbsolutePath();
				}
				Log.msg("Running disassembly process: " + cmd, Log.DEBUG);
				final Process p = new ProcessBuilder(cmd.split(" ")).directory(tool.getParentFile().getAbsoluteFile())
						.start();
				final ProcessWrapper pw = new ProcessWrapper(p);
				pw.waitFor();
				disassembledFiles.add(disassembledFile);

				Log.msg("Successfully disassembled " + disassembledFile.getAbsolutePath(), Log.DEBUG);
			} catch (final Exception e) {
				Log.error("Failed disassembling " + sharedObjectFile.getAbsolutePath() + " (" + e.getClass().getName()
						+ "): " + e.getMessage());
			}
		}
		return disassembledFiles;
	}

	private String getAbsoluteWSLPath(File file) {
		String path = file.getAbsolutePath();
		final char drive = path.toLowerCase().charAt(0);
		path = path.replaceAll("\\\\", "/").replaceFirst("[A-Z]:/", "/mnt/" + drive + "/");
		return path;
	}
}
