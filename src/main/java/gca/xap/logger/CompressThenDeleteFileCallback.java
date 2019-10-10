package gca.xap.logger;

import lombok.AllArgsConstructor;

import java.io.File;
import java.io.IOException;
import java.util.logging.Logger;

@AllArgsConstructor
public abstract class CompressThenDeleteFileCallback implements FileCallback {

	private final static Logger LOGGER = Logger.getLogger(CompressThenDeleteFileCallback.class.getName());

	private final File archiveDirectory;

	@Override
	public void process(File input) throws IOException {
		File parentDirectory = (archiveDirectory != null) ? archiveDirectory : input.getParentFile();
		parentDirectory.mkdirs();
		File gzipedLogFile = new File(parentDirectory, input.getName() + "." + getFileExtension());
		LOGGER.info("Compressing file: " + input.getAbsolutePath());
		compress(input, gzipedLogFile);
		// we want to keep the original file timestamp
		gzipedLogFile.setLastModified(input.lastModified());
		// only delete the input file when it has been successfully compressed
		input.delete();
	}

	protected abstract void compress(File input, File output) throws IOException;

	protected abstract String getFileExtension();

}
