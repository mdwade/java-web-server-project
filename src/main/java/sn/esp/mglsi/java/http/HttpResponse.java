package sn.esp.mglsi.java.http;

import org.apache.http.HttpHeaders;
import org.jtwig.JtwigModel;
import org.jtwig.JtwigTemplate;
import sn.esp.mglsi.java.WebServer;
import sn.esp.mglsi.java.utils.FileHelper;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;
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

    public HttpResponse(OutputStream outputStream, Map<String, String> defaultHeaders) {
        this.mOutputStream = outputStream;
        this.mHeaders = defaultHeaders;
    }

    public HttpResponse addHeader(String header, Object value) {
        mHeaders.put(header, value.toString());
        return this;
    }

    public HttpResponse setStatusCode(HttpStatusCode httpStatusCode) {
        mStatusLine = getStatusLine(httpStatusCode);
        return this;
    }

    private String getStatusLine(HttpStatusCode httpStatusCode) {
        return String.format("HTTP/1.1 %s %s", httpStatusCode.getCode(), httpStatusCode.getDesc());
    }

    public HttpResponse setContent(byte[] bytes) {
        this.mContent = bytes;
        return this;
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

    public void render(JtwigTemplate template, JtwigModel model) throws IOException {
        this.putHeaders();

        template.render(model, mOutputStream);

        mOutputStream.flush();
    }
}