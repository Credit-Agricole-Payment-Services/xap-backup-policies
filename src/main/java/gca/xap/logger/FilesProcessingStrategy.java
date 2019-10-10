package gca.xap.logger;

import java.io.File;
import java.util.List;

public interface FilesProcessingStrategy {
	void processFilesList(List<File> files, RetentionConfiguration retentionConfiguration, FileCallback fileCallback);
}
