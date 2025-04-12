package socs.keygen;

import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Paths;

public class FileSaver {
    public void save(String directory, String fileName, byte[] content) throws IOException {
        try (FileOutputStream outputStream = new FileOutputStream(Paths.get(directory, fileName).toString())) {
            outputStream.write(content);
            outputStream.flush();
        }
    }
}
