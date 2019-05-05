package sn.esp.mglsi.java.model;

import org.apache.commons.io.FilenameUtils;

import java.io.File;
import java.sql.Date;
import java.sql.Timestamp;
import java.text.DateFormat;
import java.text.SimpleDateFormat;

public class FolderContent {
    private String name;

    public enum Category {
        DIRECTORY,
        FILE
    }

    private Category category;
    private double size;
    private String lastModifiedDate;
    private String extension;

    public FolderContent(File f) {
        this.name = f.getName();
        this.size = (double) f.length() / 1024;
        this.category = f.isDirectory() ? Category.DIRECTORY : Category.FILE;
        this.extension = FilenameUtils.getExtension(f.getName());

        Timestamp timestamp = new Timestamp(f.lastModified());
        Date date = new Date(timestamp.getTime());
        DateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy H:mm");
        //DateFormat dateFormat = DateFormat.getDateInstance(DateFormat.FULL, Locale.FRANCE);
        this.lastModifiedDate = dateFormat.format(date);
    }

    public String getExtension() {
        return extension;
    }

    public String getName() {
        return name;
    }

    public Category getCategory() {
        return category;
    }

    public double getSize() {
        return size;
    }

    public String getLastModifiedDate() {
        return lastModifiedDate;
    }
}
