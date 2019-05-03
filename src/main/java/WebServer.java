import org.apache.http.HttpHeaders;
import org.apache.http.entity.ContentType;
import org.jtwig.JtwigModel;
import org.jtwig.JtwigTemplate;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.file.NoSuchFileException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class WebServer implements Runnable {

    public static final String SERVER_NAME = "KaWaYe (1.0)";
    private static final File WEB_ROOT = new File("public_html");

    private static final File TEMPLATES_ROOT = new File("templates");

    private static final String DEFAULT_FILE = "index.html";
    private static final String FILE_NOT_FOUND = "404.html";
    private static final String METHOD_NOT_SUPPORTED = "not_supported.html";
    // port to listen connection
    private static final int PORT = 3000;

    // verbose mode
    private static final boolean verbose = true;

    // Client Connection via Socket Class
    private Socket connect;

    private WebServer(Socket socket) {
        connect = socket;
    }

    public static void main(String[] args) {
        try {
            ServerSocket serverSocket = new ServerSocket(PORT);
            System.out.println("Server started.\nListening for connections on port : " + PORT + " ...\n");

            while (true) {
                WebServer server = new WebServer(serverSocket.accept());

                System.out.println(String.format("New Connection opened. %s", new Date()));

                Thread thread = new Thread(server);
                thread.start();
            }

        } catch (IOException e) {
            System.out.println("An error occurred while opening the socket: " + e.getMessage());
        }
    }

    @Override
    public void run() {
        // we manage our particular client connection
        HttpRequest request = null;
        HttpResponse response = null;

        try {
            request = new HttpRequest(connect.getInputStream());
            response = new HttpResponse(connect.getOutputStream());

            final String method = request.getMethod();
            final String uri = request.getUri();

            // we support only GET and HEAD methods, we check
            if (!method.equals("GET") && !method.equals("HEAD")) {
                if (verbose) {
                    System.out.println("501 Not Implemented : " + method + " method.");
                }

                File file = new File(WEB_ROOT, METHOD_NOT_SUPPORTED);

                response.setStatusCode(HttpStatusCode.NOT_IMPLEMENTED)
                        .addHeader(HttpHeaders.CONTENT_TYPE, ContentType.TEXT_HTML)
                        .setContent(file);

            } else {

                File file = new File(WEB_ROOT, uri);

                if (file.exists()) {

                    if (file.isDirectory()) {
                        File defaultFile = new File(file, DEFAULT_FILE);

                        if (defaultFile.exists()) {
                            byte[] bytes = FileHelper.readFileBytes(defaultFile);

                            response.addHeader(HttpHeaders.CONTENT_TYPE, ContentType.TEXT_HTML)
                                    .addHeader(HttpHeaders.CONTENT_LENGTH, bytes.length)
                                    .setContent(bytes)
                                    .send();

                        } else {

                            File[] dirContent = file.listFiles();
                            List<String> items = new ArrayList<>();

                            for (File f : dirContent) {
                                items.add(f.getName());
                            }

                            JtwigTemplate template = JtwigTemplate.fileTemplate(new File(TEMPLATES_ROOT, "directory_content.twig"));
                            JtwigModel model = JtwigModel.newModel().with("items", items);

                            response.render(template, model);
                        }

                    } else {
                        byte[] fileBytes = FileHelper.readFileBytes(file);

                        response.setStatusCode(HttpStatusCode.OK)
                                .addHeader(HttpHeaders.CONTENT_TYPE, getContentType(file.getName()))
                                .addHeader(HttpHeaders.CONTENT_LENGTH, fileBytes.length)
                                .setContent(fileBytes)
                                .send();
                    }

                } else {
                    try {
                        fileNotFound(response);

                    } catch (IOException ioe) {
                        System.err.println("Error with file not found exception : " + ioe.getMessage());
                    }
                }

                /*if (verbose) {
                    System.out.println("File " + requestURI + " of type " + content + " returned");
                }*/
            }

        } catch (FileNotFoundException | NoSuchFileException e) {
            /*try {
                //TODO: Handle the case when response doesnt exist
                fileNotFound(response);

            } catch (IOException ioe) {
                System.err.println("Error with file not found exception : " + ioe.getMessage());
            }*/

        } catch (IOException ioe) {
            System.err.println("Server error : " + ioe);
            ioe.printStackTrace();

        } finally {
            try {
                request.close();
                response.close();
                connect.close(); // we close socket connection

            } catch (Exception e) {
                System.err.println("Error closing stream : " + e.getMessage());
            }

            if (verbose) {
                System.out.println("Connection closed.\n");
            }
        }

    }

    private ContentType getContentType(String fileRequested) {
        if (fileRequested.endsWith(".htm") || fileRequested.endsWith(".html")) {
            return ContentType.TEXT_HTML;

        } else if (fileRequested.endsWith(".json")) {
            return ContentType.APPLICATION_JSON;

        } else {
            return ContentType.TEXT_PLAIN;
        }
    }

    private void fileNotFound(HttpResponse res) throws IOException {
        File notFoundFile = new File(WEB_ROOT, FILE_NOT_FOUND);

        res.setStatusCode(HttpStatusCode.NOT_FOUND)
                .addHeader(HttpHeaders.CONTENT_TYPE, ContentType.TEXT_HTML)
                .setContent(notFoundFile)
                .send();
    }
}
