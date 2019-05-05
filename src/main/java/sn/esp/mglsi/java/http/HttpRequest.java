package sn.esp.mglsi.java.http;

import java.io.*;
import java.net.URLDecoder;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.StringTokenizer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class HttpRequest {
    private InputStream is;
    private String uri;
    private String method;
    private Map<String, String> headers = new HashMap<>();
    private Map<String, String> params = new LinkedHashMap<>();

    private final static String HTTP_HEADER_PATTERN = "^([^:]+):(.*)$";
    private final static String URL_BASIC_PATTERN = "^([^?]+)(?:\\?(.*))?";
    private final static String QUERY_STRING_PATTERN = "^([^=]+)=([^=]+)$";

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

            this.method = parse.nextToken().toUpperCase();
            String rawURI = parse.nextToken().toLowerCase();

            processUri(rawURI);
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

    //We process the raw uri by separating the uri and the query string
    private void processUri(String rawURI) {
        Pattern pattern = Pattern.compile(URL_BASIC_PATTERN);
        Matcher matcher = pattern.matcher(rawURI);

        if (matcher.find()) {
            this.uri = matcher.group(1);

            String rawQueryString = matcher.group(2);

            if (rawQueryString != null) {
                processQueryString(rawQueryString);
            }

        }
    }

    private void processQueryString(String queryString) {
        String[] pairs = queryString.split("&");

        for (String pair : pairs) {

            Pattern pattern = Pattern.compile(QUERY_STRING_PATTERN);
            Matcher matcher = pattern.matcher(pair);

            if (matcher.find()) {

                String key = matcher.group(1);
                String value = matcher.group(2);

                try {
                    params.put(URLDecoder.decode(key, "UTF-8"), URLDecoder.decode(value, "UTF-8"));

                } catch (UnsupportedEncodingException e) {
                    //TODO: Log this error
                }
            }
        }
    }

    public Map<String, String> getParams() {
        return params;
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
