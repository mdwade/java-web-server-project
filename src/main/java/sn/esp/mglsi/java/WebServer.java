package sn.esp.mglsi.java;

import org.apache.http.HttpHeaders;
import org.apache.http.entity.ContentType;
import org.jtwig.JtwigModel;
import org.jtwig.JtwigTemplate;
import sn.esp.mglsi.java.http.HttpRequest;
import sn.esp.mglsi.java.http.HttpResponse;
import sn.esp.mglsi.java.http.HttpStatusCode;
import sn.esp.mglsi.java.utils.FileHelper;

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
    private static final File TEMPLATES_DIR = new File("templates");

    private static final String DEFAULT_FILE = "index.html";
    private static final String NOT_FOUND_FILE = "404.html";
    private static final String METHOD_NOT_SUPPORTED_FILE = "not_supported.html";

    // port to listen connection
    private static final int LISTENING_PORT = 3000;

    // verbose mode
    private static final boolean verbose = true;

    // Client Connection via Socket Class
    private Socket socket;

    private WebServer(Socket socket) {
        this.socket = socket;
    }

    public static void main(String[] args) {
        try {
            ServerSocket serverSocket = new ServerSocket(LISTENING_PORT);
            System.out.println("Server started.\nListening for connections on port : " + LISTENING_PORT + " ...\n");

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
        HttpRequest req = null;
        HttpResponse res = null;

        try {
            req = new HttpRequest(socket.getInputStream());
            res = new HttpResponse(socket.getOutputStream());

            final String method = req.getMethod();
            final String uri = req.getUri();

            // we support only GET and HEAD methods, we check
            if (!method.equals("GET") && !method.equals("HEAD")) {
                if (verbose) {
                    System.out.println("501 Not Implemented : " + method + " method.");
                }

                File file = new File(WEB_ROOT, METHOD_NOT_SUPPORTED_FILE);

                res.setStatusCode(HttpStatusCode.NOT_IMPLEMENTED)
                        .addHeader(HttpHeaders.CONTENT_TYPE, ContentType.TEXT_HTML)
                        .setContent(file);

            } else {

                File requestedFile = new File(WEB_ROOT, uri);

                if (requestedFile.exists()) {

                    if (requestedFile.isDirectory()) {
                        //The requested file is a directory so
                        //We check if it has an index.html file.
                        //if it does, we return it, otherwise we
                        //list the directory content
                        File defaultFile = new File(requestedFile, DEFAULT_FILE);

                        if (defaultFile.exists()) {
                            //The default file exists
                            //we return it to the client
                            res.addHeader(HttpHeaders.CONTENT_TYPE, ContentType.TEXT_HTML)
                                    .setContent(defaultFile)
                                    .send();

                        } else {
                            //The default file doesn't exist
                            //we return the directory content
                            File[] dirContent = requestedFile.listFiles();
                            List<String> items = new ArrayList<>();

                            for (File f : dirContent) {
                                items.add(f.getName());
                            }

                            JtwigTemplate template = JtwigTemplate.fileTemplate(new File(TEMPLATES_DIR, "directory_content.twig"));
                            JtwigModel model = JtwigModel.newModel().with("items", items);

                            res.render(template, model);
                        }

                    } else {
                        //The requested file is not a directory and it exits.
                        //We simply return it
                        res.addHeader(HttpHeaders.CONTENT_TYPE, getContentType(requestedFile.getName()))
                                .setContent(requestedFile)
                                .send();
                    }

                } else {

                    try {
                        fileNotFound(res);

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
                req.close();
                res.close();
                socket.close(); // we close socket connection

            } catch (Exception e) {
                System.err.println("Error closing stream : " + e.getMessage());
            }

            if (verbose) {
                System.out.println("Connection closed.\n");
            }
        }

    }

    private String getContentType(String fileRequested) {
        if (fileRequested.endsWith(".htm") || fileRequested.endsWith(".html")) {
            return ContentType.TEXT_HTML.toString();

        } else if (fileRequested.endsWith(".json")) {
            return ContentType.APPLICATION_JSON.toString();

        } else if (fileRequested.endsWith(".css")) {
            return "text/css";

        } else {
            return ContentType.TEXT_PLAIN.toString();
        }
    }

    private void fileNotFound(HttpResponse res) throws IOException {
        File notFoundFile = new File(WEB_ROOT, NOT_FOUND_FILE);

        res.setStatusCode(HttpStatusCode.NOT_FOUND)
                .addHeader(HttpHeaders.CONTENT_TYPE, ContentType.TEXT_HTML)
                .setContent(notFoundFile)
                .send();
    }
}
