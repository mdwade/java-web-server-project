package sn.esp.mglsi.java.model;

import java.io.File;
import java.sql.Date;
import java.sql.Timestamp;
import java.text.DateFormat;
import java.text.SimpleDateFormat;

public class FolderContent {
    private String name;

    public enum Type {
        DIRECTORY,
        FILE
    }

    private Type type;
    private double size;
    private String lastModifiedDate;

    public FolderContent(File f) {
        this.name = f.getName();
        this.type = f.isDirectory() ? Type.DIRECTORY : Type.FILE;
        this.size = (double) f.length() / 1024;

        Timestamp timestamp = new Timestamp(f.lastModified());
        Date date = new Date(timestamp.getTime());
        DateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy H:m");
        //DateFormat dateFormat = DateFormat.getDateInstance(DateFormat.FULL, Locale.FRANCE);
        this.lastModifiedDate = dateFormat.format(date);
    }

    public String getName() {
        return name;
    }

    public Type getType() {
        return type;
    }

    public double getSize() {
        return size;
    }

    public String getLastModifiedDate() {
        return lastModifiedDate;
    }
}
