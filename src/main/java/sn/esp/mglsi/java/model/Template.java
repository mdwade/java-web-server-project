package sn.esp.mglsi.java.model;

import org.jtwig.JtwigModel;
import org.jtwig.JtwigTemplate;

import java.io.File;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.Map;

public class Template {
    private static final File TEMPLATES_DIR = new File("web/templates");

    public enum Type {
        NOT_FOUND_TEMPLATE("404.twig"),
        INDEX_TEMPLATE("index.twig"),
        NOT_SUPPORTED_TEMPLATE("not_supported.twig"),
        FOLDER_CONTENT_TEMPLATE("folder_content.twig");

        private String fileName;

        Type(String fileName) {
            this.fileName = fileName;
        }

        String getFileName() {
            return fileName;
        }
    }

    private String fileName;
    private Map<String, Object> model;

    private Template(String fileName) {
        model = new HashMap<>();
        this.fileName = fileName;
    }

    public static Template getInstance(Type type) {
        return new Template(type.getFileName());
    }

    private String getFileName() {
        return fileName;
    }

    private Map<String, Object> getModel() {
        return model;
    }

    public void addData(String name, Object value) {
        model.put(name, value);
    }

    public static void render(Template template, OutputStream outputStream) {
        JtwigTemplate jtwigTemplate = JtwigTemplate.fileTemplate(new File(TEMPLATES_DIR, template.getFileName()));
        JtwigModel jtwigModel = JtwigModel.newModel();

        for (Map.Entry<String, Object> entry : template.getModel().entrySet()) {
            jtwigModel.with(entry.getKey(), entry.getValue());
        }

        jtwigTemplate.render(jtwigModel, outputStream);
    }
}