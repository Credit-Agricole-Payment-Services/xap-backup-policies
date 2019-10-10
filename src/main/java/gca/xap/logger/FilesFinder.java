package gca.xap.logger;

import java.io.File;
import java.util.List;

public interface FilesFinder {
	List<File> findFiles(File directory, String filenamePattern);
}
