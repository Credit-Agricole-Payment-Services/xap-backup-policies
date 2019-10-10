package gca.xap.logger;

import lombok.AccessLevel;
import lombok.Setter;

import java.io.*;
import java.util.zip.GZIPOutputStream;

public class GzipThenDeleteFileCallback extends CompressThenDeleteFileCallback {

	@Setter(AccessLevel.PROTECTED)
	private int readBufferSize = 4 * 1024;

	@Setter(AccessLevel.PROTECTED)
	private int writeBufferSize = 8 * 1024;

	public GzipThenDeleteFileCallback(File archiveDirectory) {
		super(archiveDirectory);
	}

	@Override
	protected void compress(File input, File output) throws IOException {
		try (OutputStream out = new GZIPOutputStream(
				new FileOutputStream(output), writeBufferSize
		)) {
			try (InputStream in = new FileInputStream(input)) {
				byte[] buffer = new byte[readBufferSize];
				int len;
				while ((len = in.read(buffer)) != -1) {
					out.write(buffer, 0, len);
				}
			}
		}
	}

	@Override
	protected String getFileExtension() {
		return "gz";
	}

}
