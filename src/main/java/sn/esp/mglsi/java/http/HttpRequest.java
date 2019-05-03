package sn.esp.mglsi.java.http;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;
import java.util.StringTokenizer;

public class HttpRequest {
    private InputStream is;
    private String uri;
    private String method;
    private Map<String, String> headers = new HashMap<>();

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

        boolean isFirstLine = true;

        String currentLine;

        do {
            currentLine = bufferedReader.readLine();

            //We check whether the first line has been read or not
            //If not, we retrieve the status line details
            if (isFirstLine) {
                StringTokenizer parse = new StringTokenizer(currentLine);
                this.method = parse.nextToken().toUpperCase(); // we get the HTTP method of the client
                this.uri = parse.nextToken().toLowerCase();
                isFirstLine = false;

            } else {
                //addHeader(currentLine);
            }

        } while (!currentLine.equals(""));
    }

    /*private void addHeader(String headerLine) {
        //if(Pattern.matches(""))
        String headerName = headerLine.split(":")[0];
        String headerValue = headerLine.split(":")[1];
        headers.put(headerName, headerValue);
    }*/

    public Map<String, String> getHeaders() {
        return headers;
    }

    public void close() throws IOException {
        is.close();
    }
}
