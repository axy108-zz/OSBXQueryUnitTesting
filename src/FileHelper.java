import java.io.FileInputStream;
import java.io.InputStreamReader;


public class FileHelper {
	/**
	 * Reads a file into a string using the character set specified.
	 * 
	 * @param filename
	 *            Name of file to read in.
	 * @param charset
	 *            Character set the file is using.
	 * @return String contents of file.
	 * @throws java.io.IOException
	 */
	public static String readFile(String filename, String charset)
			throws java.io.IOException {
		StringBuffer fileData = new StringBuffer();
		FileInputStream inputStream = new FileInputStream(filename);
		InputStreamReader reader = new InputStreamReader(inputStream, charset);
		// read the file contents in 1k buffer segments
		char[] buf = new char[1024];
		int numRead = 0;
		while ((numRead = reader.read(buf)) != -1) {
			String readData = String.valueOf(buf, 0, numRead);
			fileData.append(readData);
			buf = new char[1024];
		}
		reader.close();
		return fileData.toString();
	}
}
