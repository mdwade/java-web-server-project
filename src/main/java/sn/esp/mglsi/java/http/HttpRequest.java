package sn.esp.mglsi.java.http;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;
import java.util.StringTokenizer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class HttpRequest {
    private InputStream is;
    private String uri;
    private String method;
    private Map<String, String> headers = new HashMap<>();
    private final static String HTTP_HEADER_PATTERN = "^([^:]+):(.*)$";

    public HttpRequest(InputStream inputStream) throws IOException {
        process(inputStream);
        is = inputStream;
    }

    public String getMethod() {
        return this.method;
    }

    public String getUri() {
        return this.uri;
    }

    private void process(InputStream inputStream) throws IOException {
        // we read characters from the client via input stream on the socket
        //InputStreamReader -> An InputStreamReader is a bridge from byte streams to character streams:
        // It reads bytes and decodes them into characters using a specified charset.
        BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream));

        String currentLine = bufferedReader.readLine();

        //We retrieve the method and the URI from the first line
        if (currentLine != null && !currentLine.equals("")) {
            StringTokenizer parse = new StringTokenizer(currentLine);
            this.method = parse.nextToken().toUpperCase(); // we get the HTTP method of the client
            this.uri = parse.nextToken().toLowerCase();
        }

        //We retrieve the headers from the remaining lines
        while (currentLine != null && !currentLine.equals("")) {
            currentLine = bufferedReader.readLine();

            Pattern pattern = Pattern.compile(HTTP_HEADER_PATTERN);
            Matcher m = pattern.matcher(currentLine);

            if (m.find()) {
                addHeader(m.group(1), m.group(2));
            }
        }
    }

    private void addHeader(String name, String value) {
        headers.put(name, value);
    }

    public Map<String, String> getHeaders() {
        return headers;
    }

    public void close() throws IOException {
        is.close();
    }
}
