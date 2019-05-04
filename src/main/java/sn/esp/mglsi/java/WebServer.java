package sn.esp.mglsi.java;

import org.apache.http.HttpHeaders;
import org.apache.http.entity.ContentType;
import sn.esp.mglsi.java.http.HttpRequest;
import sn.esp.mglsi.java.http.HttpResponse;
import sn.esp.mglsi.java.http.HttpStatusCode;
import sn.esp.mglsi.java.model.FolderContent;
import sn.esp.mglsi.java.model.Template;

import java.io.File;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class WebServer implements Runnable {

    public static final String SERVER_NAME = "KaWaYe (1.0)";

    private static final File WEB_ROOT = new File("public_html");

    private static final String DEFAULT_FILE = "index.html";

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

            //We check if we received something different
            //from GET and HEAD requests.
            if (!method.equals("GET") && !method.equals("HEAD")) {

                Template template = Template.getInstance(Template.Type.NOT_SUPPORTED_TEMPLATE);
                res.setStatusCode(HttpStatusCode.NOT_IMPLEMENTED)
                        .render(template);

            } else {
                File requestedFile = new File(WEB_ROOT, uri);

                //When the request is received, we check if the requested resource exists
                if (requestedFile.exists()) {

                    //The requested resource exists so we check if it's a folder
                    if (requestedFile.isDirectory()) {

                        //If the requested resource is a directory, we check whether
                        //the uri has a trailing slash or not (because it might cause issues when we'll
                        // use relative links in the HTML document)
                        if (uri.endsWith("/")) {

                            //If the uri has a trailing slash, then everything is fine so we check
                            //if the directory has a default file (index.html)
                            File defaultFile = new File(requestedFile, DEFAULT_FILE);

                            if (defaultFile.exists()) {
                                //The default file exists
                                //we return it to the client
                                res.addHeader(HttpHeaders.CONTENT_TYPE, ContentType.TEXT_HTML)
                                        .setContent(defaultFile)
                                        .send();

                            } else {
                                //The default file doesn't exist
                                //so we list the content of the directory
                                File[] contentArray = requestedFile.listFiles();
                                List<FolderContent> folderContents = new ArrayList<>();

                                for (File f : contentArray) {
                                    folderContents.add(new FolderContent(f));
                                }

                                Template instance = Template.getInstance(Template.Type.FOLDER_CONTENT_TEMPLATE);
                                instance.addData("folderContents", folderContents);

                                res.render(instance);
                            }

                        } else {
                            //If the uri doesn't have a trailing slash, we ask the browser
                            //to reload the page by returning a 308 status code (Permanent redirect)
                            //and giving it the new uri with the trailing slash added
                            res.redirect(String.format("%s/", uri));
                        }

                    } else {
                        //The requested resource is not a directory and it exits.
                        //We simply return it
                        res.addHeader(HttpHeaders.CONTENT_TYPE, getContentType(requestedFile.getName()))
                                .setContent(requestedFile)
                                .send();
                    }

                } else {

                    Template template = Template.getInstance(Template.Type.NOT_FOUND_TEMPLATE);
                    res.setStatusCode(HttpStatusCode.NOT_FOUND)
                            .render(template);
                }
            }

        } catch (IOException ioe) {
            System.err.println("Server error : " + ioe);
            ioe.printStackTrace();

        } finally {
            try {
                if (req != null) req.close();
                if (res != null) res.close();
                if (socket != null) socket.close();

            } catch (Exception e) {
                System.err.println("Error closing stream : " + e.getMessage());
            }

            if (verbose) {
                System.out.println("Connection closed.\n");
            }
        }

    }

    //TODO: Handle the contentType properly
    private String getContentType(String fileName) {
        if (fileName.endsWith(".htm") || fileName.endsWith(".html")) {
            return ContentType.TEXT_HTML.toString();

        } else if (fileName.endsWith(".json")) {
            return ContentType.APPLICATION_JSON.toString();

        } else if (fileName.endsWith(".css")) {
            return "text/css";

        } else {
            return ContentType.TEXT_PLAIN.toString();
        }
    }
}
