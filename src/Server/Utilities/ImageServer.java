package Server.Utilities;

import com.sun.net.httpserver.HttpServer;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpExchange;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.nio.file.Files;

public class ImageServer {

    public static void startServer(int port) throws IOException {
        // Create an HTTP server that listens on port 8080
        HttpServer server = HttpServer.create(new InetSocketAddress(port), 0);

        // Set up a context to listen for requests at the root path
        server.createContext("/", new ImageHandler());

        // Start the server
        server.setExecutor(null); // Use the default executor
        server.start();
        System.out.println("ImageServer started on port 8080...");
    }

    // Handler for processing incoming HTTP requests
    static class ImageHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            // Get the request URI path, e.g., "/menu_item_images/restaurant1_Item0.jpg"
            String requestPath = exchange.getRequestURI().getPath();
            System.out.println("Requested path: " + requestPath);

            // Set the base directory for images
            File file = new File("." + requestPath);
            System.out.println("File path resolved to: " + file.getAbsolutePath());

            // Check if the requested file exists and is a file
            if (file.exists() && file.isFile() && isImage(file.getAbsolutePath())) {
                System.out.println("File found: " + file.getAbsolutePath());

                // Get the content type (e.g., "image/jpeg")
                String contentType = Files.probeContentType(file.toPath());

                // Set the response headers
                exchange.getResponseHeaders().set("Content-Type", contentType);
                exchange.sendResponseHeaders(200, file.length());

                // Write the file content to the response body
                try (FileInputStream fis = new FileInputStream(file);
                     OutputStream os = exchange.getResponseBody()) {
                    byte[] buffer = new byte[1024];
                    int bytesRead;
                    while ((bytesRead = fis.read(buffer)) != -1) {
                        os.write(buffer, 0, bytesRead);
                    }
                }
            } else {
                // If the file is not found, return a 404 response
                System.out.println("File not found: " + file.getAbsolutePath());
                String notFoundMessage = "404 (Not Found)\n";
                exchange.sendResponseHeaders(404, notFoundMessage.length());
                try (OutputStream os = exchange.getResponseBody()) {
                    os.write(notFoundMessage.getBytes());
                }
            }
        }
    }

    private static boolean isImage(String path) {
        String[] parts = path.split("\\.");
        if(parts.length == 0) return false;
        String extension = parts[parts.length - 1];
        System.out.println("Extension: " + extension);
        return extension.equals("jpg") || extension.equals("jpeg") || extension.equals("png");
    }
}
