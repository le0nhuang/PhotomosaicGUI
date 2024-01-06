import javax.imageio.ImageIO;
import javax.swing.filechooser.FileFilter;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.io.*;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.*;

/**
 * Class that unzips a given file.
 * Requires: the given file is a zip file.
 *
 * Reference: https://www.baeldung.com/java-compress-and-uncompress
 */

public class FileUnzip{
    private final File file;
    private static final FileFilter imFilter = new FileNameExtensionFilter("Images Only",
            ImageIO.getReaderFileSuffixes());

    public FileUnzip(File file){
        FileFilter zipFilter = new FileNameExtensionFilter("Zip Files", "zip");
        assert zipFilter.accept(file);
        this.file = file;
    }

    /**
     * Unzips the given file.
     */

    public List<String> unzip() throws IOException{
        List<String> a = new ArrayList<>();
        String fileZip = file.getAbsolutePath();
        int x = fileZip.lastIndexOf('.');
        String dir = fileZip.substring(0,x);
        File destDir = new File(dir);
        byte[] buffer = new byte[1024];
        ZipInputStream zis = new ZipInputStream(new FileInputStream(fileZip));
        ZipEntry zipEntry = zis.getNextEntry();
        while (zipEntry != null) {
            File newFile = newFile(destDir, zipEntry);
            if(newFile == null){
                zipEntry = zis.getNextEntry();
                continue;
            }
            if (zipEntry.isDirectory()) {
                if (!newFile.isDirectory() && !newFile.mkdirs()) {
                    throw new IOException("Failed to create directory " + newFile);
                }
            } else {
                // fix for Windows-created archives
                File parent = newFile.getParentFile();
                if (!parent.isDirectory() && !parent.mkdirs()) {
                    throw new IOException("Failed to create directory " + parent);
                }

                // write file content
                FileOutputStream fos = new FileOutputStream(newFile);
                int len;
                while ((len = zis.read(buffer)) > 0) {
                    fos.write(buffer, 0, len);
                }
                a.add(newFile.getAbsolutePath());
                fos.close();
            }
            zipEntry = zis.getNextEntry();
        }
        zis.closeEntry();
        zis.close();
        return a;
    }

    /**
     * Helper method to create a new File given a destination directory and a zip entry
     *
     * Ensures that all files within the zip are image files, as well as within the target
     * directory.
     */
    private static File newFile(File destinationDir, ZipEntry zipEntry) throws IOException {
        File destFile = new File(destinationDir, zipEntry.getName());

        String destDirPath = destinationDir.getCanonicalPath();
        String destFilePath = destFile.getCanonicalPath();

        if (!destFilePath.startsWith(destDirPath + File.separator)) {
            throw new IOException("Entry is outside of the target dir: " + zipEntry.getName());
        }

        if(!imFilter.accept(destFile)){
            return null;
        }

        if(destFile.getAbsolutePath().contains("__MACOSX")){
            return null;
        }

        return destFile;
    }

}