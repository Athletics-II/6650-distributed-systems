package clientPart2;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpRequest.BodyPublishers;
import java.net.http.HttpResponse;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.UUID;

public class MultiPartBodyPublisher {
    private final String boundary = UUID.randomUUID().toString();
    private final byte[] separator = ("--" + boundary + "\r\nContent-Disposition: form-data; name=")
            .getBytes();
    private final byte[] terminator = ("--" + boundary + "--").getBytes();
    private final ByteArrayOutputStream body = new ByteArrayOutputStream();
    private static final int MAX_RETRIES = 5;
    private static final long RETRY_WAIT_TIME = 500;

    public MultiPartBodyPublisher addPart(String name, Path path) throws IOException {
        body.write(separator);
        String mimeType = Files.probeContentType(path);
        body.write(("\"" + name + "\"; filename=\"" + path.getFileName() + "\"\r\nContent-Type: " + mimeType + "\r\n\r\n").getBytes());
        body.write(Files.readAllBytes(path));
        body.write("\r\n".getBytes());
        return this;
    }

    public MultiPartBodyPublisher addJsonPart(String name, String json) throws IOException {
        body.write(separator);
        body.write(("\"" + name + "\"\r\nContent-Type: application/json\r\n\r\n").getBytes());
        body.write(json.getBytes());
        body.write("\r\n".getBytes());
        return this;
    }

    public HttpRequest.BodyPublisher build() {
        try {
            body.write(terminator);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return BodyPublishers.ofByteArray(body.toByteArray());
    }

    public String getBoundary() {
        return boundary;
    }

    public static int sendRequestWithRetries(HttpClient client, HttpRequest request) throws Exception {
        HttpResponse<String> response;
        int retryCount = 0;

        while (retryCount < MAX_RETRIES) {
            try {
                response = client.send(request, HttpResponse.BodyHandlers.ofString());
                // Check if the response status code is 4XX or 5XX
                if (response.statusCode() >= 400 && response.statusCode() <= 599) {
                    System.out.println("POST Request failed with status code: " + response.statusCode() + ". Retrying...");
                    retryCount++;
                    Thread.sleep(RETRY_WAIT_TIME); // Wait before retrying
                } else {
                    return response.statusCode();
                }
            } catch (IOException | InterruptedException e) {
                if (retryCount < MAX_RETRIES) {
                    System.out.println("POST Request failed due to an exception. Retrying... Attempt " + (retryCount + 1));
                    retryCount++;
                    Thread.sleep(RETRY_WAIT_TIME);
                } else {
                    System.out.println("POST Request failed after " + MAX_RETRIES + " attempts due to an exception: " + e.getMessage());
                }

            }
        }
        return 0;
    }

}

