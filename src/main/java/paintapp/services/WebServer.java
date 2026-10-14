package paintapp.services;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import com.sun.net.httpserver.HttpServer;
import java.io.ByteArrayOutputStream;
import java.util.Base64;
import java.util.List;
import javax.imageio.ImageIO;
import javafx.embed.swing.SwingFXUtils;
import javafx.scene.image.Image;

public class WebServer {

    private HttpServer server;

    private volatile String pageHtml = """
        <!DOCTYPE html>
        <html>
        <head>
            <meta charset="UTF-8">
            <meta http-equiv="refresh" content="3">
            <title>PaintApp Gallery</title>
        </head>
        <body>
            <h1>PaintApp Gallery</h1>
            <p>No images selected.</p>
        </body>
        </html>
        """;

    /**
     * Starts a new server that runs locally at http://localhost:8080
     * @throws IOException
     */
    public void start() throws IOException {
        server = HttpServer.create(
            new InetSocketAddress("localhost", 8080),
            0
        );

        server.createContext("/", exchange -> {
            byte[] response = pageHtml.getBytes(
                StandardCharsets.UTF_8
            );

            exchange.getResponseHeaders().set(
                "Content-Type",
                "text/html; charset=UTF-8"
            );

            exchange.getResponseHeaders().set(
                "Cache-Control",
                "no-store"
            );

            try {
                exchange.sendResponseHeaders(200, response.length);

                try (var output = exchange.getResponseBody()) {
                    output.write(response);
                }
            } finally {
                exchange.close();
            }
        });

        server.start();
    }

    /**
     * Shut down the server
     */
    public void stop() {
        if (server != null) {
            server.stop(0);
            server = null;
        }
    }

    /**
     * Embeds every passed image as png data
     * @param images
     * @throws IOException
     */
    public void updateImages(List<Image> images) throws IOException {
        StringBuilder html = new StringBuilder("""
            <!DOCTYPE html>
            <html>
            <head>
                <meta charset="UTF-8">
                <meta http-equiv="refresh" content="3">
                <title>PaintApp Gallery</title>
            </head>
            <body>
                <h1>PaintApp Gallery</h1>
            """);

        if (images.isEmpty()) {
            html.append("<p>No images selected.</p>");
        } else {
            int imageNumber = 1;

            for (Image image : images) {
                try (ByteArrayOutputStream output =
                        new ByteArrayOutputStream()) {

                    boolean written = ImageIO.write(
                        SwingFXUtils.fromFXImage(image, null),
                        "png",
                        output
                    );

                    if (!written) {
                        throw new IOException("Could not encode gallery image.");
                    }

                    String encoded = Base64.getEncoder().encodeToString(
                        output.toByteArray()
                    );

                    html.append("<h2>Image ")
                        .append(imageNumber++)
                        .append("</h2>")
                        .append("<img style=\"max-width:100%;height:auto\" ")
                        .append("alt=\"Shared drawing\" ")
                        .append("src=\"data:image/png;base64,")
                        .append(encoded)
                        .append("\">");
                }
            }
        }

        html.append("</body></html>");
        pageHtml = html.toString();
    }
}