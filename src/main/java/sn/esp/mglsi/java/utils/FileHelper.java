package sn.esp.mglsi.java.utils;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.regex.Pattern;

public class FileHelper {

    public static byte[] readFileBytes(File file) throws IOException {
        Path path = Paths.get(file.getAbsolutePath());
        return Files.readAllBytes(path);
    }

    private static boolean isDotFile(String fileName) {
        return Pattern.matches("^\\..*", fileName);
    }

    /**
     * @param file a File instance
     * @return true if file is a directory and begins with a dot
     */
    public static boolean isHiddenFolder(File file) {
        return file.isDirectory() && isDotFile(file.getName());
    }
}