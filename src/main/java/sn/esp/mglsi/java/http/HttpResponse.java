package sn.esp.mglsi.java.http;

import org.apache.http.HttpHeaders;
import org.apache.http.entity.ContentType;
import org.python.util.PythonInterpreter;
import sn.esp.mglsi.java.WebServer;
import sn.esp.mglsi.java.model.Template;
import sn.esp.mglsi.java.utils.FileHelper;

import java.io.*;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class HttpResponse {
    private OutputStream mOutputStream;
    private String mStatusLine = getStatusLine(HttpStatusCode.OK);
    private Map<String, String> mHeaders;
    private byte[] mContent = null;

    public HttpResponse(OutputStream outputStream) {
        this.mOutputStream = outputStream;
        mHeaders = new HashMap<>();
        addDefaultHeaders();
    }

    public HttpResponse addHeader(String header, Object value) {
        mHeaders.put(header, value.toString());
        return this;
    }

    public HttpResponse setStatusCode(HttpStatusCode httpStatusCode) {
        mStatusLine = getStatusLine(httpStatusCode);
        return this;
    }

    public void redirect(String uri) throws IOException {
        setStatusCode(HttpStatusCode.PERMANENT_REDIRECT);
        addHeader(HttpHeaders.LOCATION, uri);
        send();
    }

    private String getStatusLine(HttpStatusCode httpStatusCode) {
        return String.format("HTTP/1.1 %s %s", httpStatusCode.getCode(), httpStatusCode.getDesc());
    }

    public HttpResponse setContent(File file) throws IOException {
        byte[] bytes = FileHelper.readFileBytes(file);
        this.addHeader(HttpHeaders.CONTENT_LENGTH, bytes.length);
        this.mContent = bytes;
        return this;
    }

    public void send() throws IOException {
        this.putHeaders();

        //Adding content
        if (mContent != null) {
            mOutputStream.write(mContent, 0, mContent.length);
        }

        mOutputStream.flush();
    }

    public void close() throws IOException {
        mOutputStream.close();
    }

    private void addDefaultHeaders() {
        addHeader(HttpHeaders.SERVER, WebServer.SERVER_NAME);
        addHeader(HttpHeaders.DATE, new Date());
    }

    private void putHeaders() {
        PrintWriter out = new PrintWriter(mOutputStream);

        //Adding status line
        out.println(mStatusLine);

        //Adding headers
        for (Map.Entry<String, String> entry : mHeaders.entrySet()) {
            out.println(String.format("%s: %s", entry.getKey(), entry.getValue()));
        }

        out.println();
        out.flush();
    }

    public void renderTemplate(Template template) throws IOException {
        addHeader(HttpHeaders.CONTENT_TYPE, ContentType.TEXT_HTML);
        this.putHeaders();

        Template.render(template, mOutputStream);

        mOutputStream.flush();
    }

    public void renderPythonScript(File pythonFile) throws IOException {
        this.addHeader(HttpHeaders.CONTENT_TYPE, ContentType.TEXT_PLAIN);
        this.putHeaders();

        PythonInterpreter interpreter = new PythonInterpreter();
        interpreter.setOut(mOutputStream);
        interpreter.execfile(new FileInputStream(pythonFile));

        mOutputStream.flush();
    }
}