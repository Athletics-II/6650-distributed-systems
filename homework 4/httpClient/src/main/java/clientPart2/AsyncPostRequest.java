package clientPart2;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

public class AsyncPostRequest {
    private static final String BASE_URL = "http://44.232.105.145:8080/albumServlet-0.0.1-SNAPSHOT/review";
    private static final int MAX_RETRIES = 5;
    private static final long RETRY_WAIT_TIME = 500;

    public static int sendReviews(HttpClient client) throws InterruptedException {
        // Send two likes
        int res1 = sendSingleReview(client, "like");
        int res2 = sendSingleReview(client, "like");
        // Send one dislike
        int res3 = sendSingleReview(client, "dislike");

        if (res1 == 200 && res2 == 200 && res3 == 200) {
            return 200;
        } else {
            return -1;
        }
    }

    private static int sendSingleReview(HttpClient client, String likeOrNot) throws InterruptedException {
        String endpoint = String.format("%s/%s/%d", BASE_URL, likeOrNot, 1);
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(endpoint))
                .POST(HttpRequest.BodyPublishers.noBody())
                .header("Content-Type", "application/json")
                .build();

        HttpResponse<String> response;
        int retryCount = 0;

        while (retryCount < MAX_RETRIES) {
            try {
                response = client.send(request, HttpResponse.BodyHandlers.ofString());
                // Check if the response status code is 4XX or 5XX
                if (response.statusCode() >= 400 && response.statusCode() <= 599) {
                    System.out.println("POST Review Request failed with status code: " + response.statusCode() + ". Retrying...");
                    retryCount++;
                    Thread.sleep(RETRY_WAIT_TIME); // Wait before retrying
                } else {
                    return response.statusCode();
                }
            } catch (IOException | InterruptedException e) {
                if (retryCount < MAX_RETRIES) {
                    System.out.println("POST Review Request failed due to an exception. Retrying... Attempt " + (retryCount + 1));
                    retryCount++;
                    Thread.sleep(RETRY_WAIT_TIME);
                } else {
                    System.out.println("POST Review Request failed after " + MAX_RETRIES + " attempts due to an exception: " + e.getMessage());
                }

            }
        }
        return 0;
    }
}
