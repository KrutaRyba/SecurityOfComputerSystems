package socs.keygen;

import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Paths;

/**
 * Class that provides functionality for saving content to given directory.
*/
public class FileSaver {
    /**
     * Function that saves array of bytes under given name and to given directory. If file with the same name exists, it is overwritten.
     * @param directory Directory to save file in
     * @param fileName Name of file
     * @param content Content of file in bytes
     * @throws IOException Directory does not exist
     */
    public void save(String directory, String fileName, byte[] content) throws IOException {
        try (FileOutputStream outputStream = new FileOutputStream(Paths.get(directory, fileName).toString())) {
            outputStream.write(content);
            outputStream.flush();
        }
    }
}
