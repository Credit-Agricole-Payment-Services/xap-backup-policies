package gca.xap.logger;

import java.io.File;
import java.io.IOException;

public interface FileCallback {
	void process(File input) throws IOException;
}
