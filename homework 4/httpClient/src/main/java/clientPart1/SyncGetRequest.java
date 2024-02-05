package clientPart1;

import java.io.IOException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

public class SyncGetRequest {
    private static final int MAX_RETRIES = 5;
    private static final long RETRY_WAIT_TIME = 500;

    public static void sendRequestWithRetries(HttpClient client, HttpRequest request) throws Exception {
        HttpResponse<String> response;
        int retryCount = 0;

        while (retryCount < MAX_RETRIES) {
            try {
                response = client.send(request, HttpResponse.BodyHandlers.ofString());
                // Check if the response status code is 4XX or 5XX
                if (response.statusCode() >= 400 && response.statusCode() <= 599) {
                    System.out.println("GET Request failed with status code: " + response.statusCode() + ". Retrying...");
                    retryCount++;
                    Thread.sleep(RETRY_WAIT_TIME); // Wait before retrying
                } else {
                    return;
                }
            } catch (IOException | InterruptedException e) {
                if (retryCount < MAX_RETRIES) {
                    System.out.println("GET Request failed due to an exception. Retrying... Attempt " + (retryCount + 1));
                    retryCount++;
                    Thread.sleep(RETRY_WAIT_TIME);
                } else {
                    System.out.println("GET Request failed after " + MAX_RETRIES + " attempts due to an exception: " + e.getMessage());
                }

            }
        }
    }
}
