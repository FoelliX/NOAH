package de.foellix.aql.noah.sourcesandsinks;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import de.foellix.aql.Log;

public class Unzipper {
	public Collection<File> unzip(File apkFile) {
		final Collection<File> sharedObjectFiles = new ArrayList<>();

		final byte[] buffer = new byte[1024];
		try {
			final ZipInputStream zis = new ZipInputStream(new FileInputStream(apkFile));
			ZipEntry ze = zis.getNextEntry();
			while (ze != null) {
				final String fileName = (ze.getName().contains("/")
						? ze.getName().substring(ze.getName().lastIndexOf("/") + 1)
						: ze.getName());
				if (fileName.endsWith(".so")) {
					final File newFile = new File("data" + File.separator + "temp" + File.separator + fileName);

					final FileOutputStream fos = new FileOutputStream(newFile);
					int len;
					while ((len = zis.read(buffer)) > 0) {
						fos.write(buffer, 0, len);
					}
					fos.close();

					sharedObjectFiles.add(newFile);
					Log.msg(ze.getName() + " -> " + newFile.getAbsoluteFile().toString(), Log.DEBUG_DETAILED);
				}
				ze = zis.getNextEntry();
			}
			zis.closeEntry();
			zis.close();
		} catch (final IOException e) {
			Log.error("Encountered exception while unzipping " + apkFile + " (" + e.getClass().getSimpleName() + "): "
					+ e.getMessage());
		}

		return sharedObjectFiles;
	}
}