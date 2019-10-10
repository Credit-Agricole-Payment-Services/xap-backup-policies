package gca.xap.logger;

import java.io.File;
import java.io.IOException;
import java.util.logging.Logger;

public class DeleteFileCallback implements FileCallback {

	private final static Logger LOGGER = Logger.getLogger(DeleteFileCallback.class.getName());

	@Override
	public void process(File input) throws IOException {
		LOGGER.info("Deleting file: " + input.getAbsolutePath());
		input.delete();
	}

}
