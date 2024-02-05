package clientPart1;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Duration;
import java.time.Instant;
import java.util.concurrent.CountDownLatch;

public class AlbumClientAPI {

    private static final HttpClient httpClient = HttpClient.newBuilder()
            .version(HttpClient.Version.HTTP_2)
            .connectTimeout(Duration.ofSeconds(10))
            .build();

    public static void main(String[] args) throws Exception {
        int threadGroupSize = Integer.parseInt(args[0]);
        int numThreadGroups = Integer.parseInt(args[1]);
        int delay = Integer.parseInt(args[2]);
        String getURI = "http://54.191.166.240:8080/IGORTON/AlbumStore/1.0.0/albums/1";
        String postURI = "http://54.191.166.240:8080/IGORTON/AlbumStore/1.0.0/albums";
        CountDownLatch completed = new CountDownLatch(10);

        MultiPartBodyPublisher publisher = createBodyPublisher();

        HttpRequest getRequest = HttpRequest.newBuilder()
                .GET()
                .uri(URI.create(getURI))
                .setHeader("User-Agent", "Java 11 HttpClient Bot")
                .build();

        HttpRequest postRequest = HttpRequest.newBuilder()
                .uri(URI.create(postURI))
                .header("Content-Type", "multipart/form-data;boundary=" + publisher.getBoundary())
                .POST(publisher.build())
                .build();

        for (int i=0; i<10; i++) {
            new Thread(new InitializationWorker(completed, getRequest, postRequest)).start();
        }

        completed.await();

        Instant startTime = Instant.now();

        ConcurrentClientCustomExecutor executor = new ConcurrentClientCustomExecutor();
        executor.startExecution(10, 10, 2000, httpClient, getRequest, postRequest);

        Instant endTime = Instant.now();
        Duration duration = Duration.between(startTime, endTime);
        System.out.println("Walltime: " + duration.toSeconds() + " Throughput: " + 200000/duration.toSeconds());
    }

    private static MultiPartBodyPublisher createBodyPublisher() throws IOException {
        MultiPartBodyPublisher publisher = new MultiPartBodyPublisher();

        // Path to the image file
        Path imagePath = Paths.get("C:\\Users\\凌思漪\\Desktop\\Example.jpg");

        // JSON representation of the AlbumInfo object
        String jsonProfile = "{\"artist\":\"Artist Name\",\"title\":\"Album Title\",\"year\":\"2023\"}";

        publisher.addPart("image", imagePath)
                .addJsonPart("profile", jsonProfile);
        return publisher;
    }

    private static class InitializationWorker implements Runnable {
        private final CountDownLatch latch;
        private HttpRequest getRequest;
        private HttpRequest postRequest;

        public InitializationWorker(CountDownLatch latch, HttpRequest getRequest, HttpRequest postRequest) {
            this.latch = latch;
            this.getRequest = getRequest;
            this.postRequest = postRequest;
        }

        @Override
        public void run() {
            for (int i=0; i<100; i++) {
                try {
                    MultiPartBodyPublisher.sendRequestWithRetries(httpClient, postRequest);
                    SyncGetRequest.sendRequestWithRetries(httpClient, getRequest);
                } catch (Exception e) {
                    e.printStackTrace();
                } finally {
                    latch.countDown();
                }
            }
        }
    }
}
