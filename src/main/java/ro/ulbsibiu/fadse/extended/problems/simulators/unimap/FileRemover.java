package ro.ulbsibiu.fadse.extended.problems.simulators.unimap;

import java.io.File;
import java.util.LinkedHashSet;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * File container. All files are kept by their file names. The files can be
 * deleted after they are no longer needed.
 * 
 * @author Ciprian Radu
 * 
 */
public class FileRemover {

	/**
	 * Logger for this class
	 */
	private static final Logger logger = LoggerFactory.getLogger(FileRemover.class);

	/**
	 * the files
	 */
	private Set<String> files;

	/**
	 * Default constructor
	 */
	public FileRemover() {
		files = new LinkedHashSet<String>();
	}

	/**
	 * Registers a file for deletion
	 * 
	 * @param xmlFilePath
	 *            the file path
	 */
	public void addFileToBeCleaned(String filePath) {
		files.add(filePath);
	}

	/**
	 * Deletes all registered XML files
	 */
	public void removeFiles() {
		logger.info("Removing no longer required files");
		for (String filePath : files) {
			File file = new File(filePath);
			if (!file.exists()) {
				logger.error("File " + filePath + " does not exist!");
			} else {
				if (file.isDirectory()) {
					logger.error(filePath
							+ " is not a file. It is a directory!");
				} else {
					boolean deleted = file.delete();
					if (!deleted) {
						logger.error("Could not delete file " + filePath);
					} else {
						logger.info("File " + filePath + " deleted.");
					}
				}
			}
		}
	}
}
