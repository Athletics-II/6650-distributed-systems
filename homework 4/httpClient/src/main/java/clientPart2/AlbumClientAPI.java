package clientPart2;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Duration;
import java.time.Instant;
import java.util.concurrent.CountDownLatch;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.util.Map;

public class AlbumClientAPI {
    private static final HttpClient httpClient = HttpClient.newBuilder()
            .version(HttpClient.Version.HTTP_2)
            .connectTimeout(Duration.ofSeconds(10))
            .build();

    public static void main(String[] args) throws Exception {
        System.out.println("Client part 2 results for Java servlet \n");
        sendRequest(10, 10, 2000,
                "http://44.232.105.145:8080/albumServlet_war/albums/1", "http://44.232.105.145:8080/albumServlet-0.0.1-SNAPSHOT/albums");
        sendRequest(10, 20, 2000,
                "http://44.232.105.145:8080/albumServlet_war/albums/1", "http://44.232.105.145:8080/albumServlet-0.0.1-SNAPSHOT/albums");
        Map<Long, Long> throughputPerSecond = sendRequest(10, 30, 2000,
                "http://44.232.105.145:8080/albumServlet_war/albums/1", "http://44.232.105.145:8080/albumServlet-0.0.1-SNAPSHOT/albums");
//        System.out.println("Client part 2 results for Go server \n");
//        sendRequest(10, 10, 2000,
//                "http://54.191.166.240:8080/IGORTON/AlbumStore/1.0.0/albums/1",
//                "http://54.191.166.240:8080/IGORTON/AlbumStore/1.0.0/albums");
//        sendRequest(10, 20, 2000,
//                "http://54.191.166.240:8080/IGORTON/AlbumStore/1.0.0/albums/1",
//                "http://54.191.166.240:8080/IGORTON/AlbumStore/1.0.0/albums");
//        Map<Long, Long> throughputPerSecond = sendRequest(10, 30, 2000,
//                "http://54.191.166.240:8080/IGORTON/AlbumStore/1.0.0/albums/1",
//                "http://54.191.166.240:8080/IGORTON/AlbumStore/1.0.0/albums");

        try (PrintWriter writer = new PrintWriter(new FileWriter("C:\\Users\\27173\\Documents\\throughput_data.csv"))) {
            writer.println("second,throughput"); // Header
            throughputPerSecond.forEach((second, count) -> writer.printf("%d,%d\n", second, count));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static Map<Long, Long> sendRequest(
            int threadGroupSize,
            int numThreadGroups,
            int delay,
            String getURI,
            String postURI) throws IOException, InterruptedException {

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
        Map<Long, Long> startsPerSecond = executor.startExecution(threadGroupSize, numThreadGroups, delay, httpClient, getRequest, postRequest);

        Instant endTime = Instant.now();
        Duration duration = Duration.between(startTime, endTime);
        System.out.println("threadGroupSize = " + threadGroupSize + ", numThreadGroups = " + numThreadGroups + ", delay = " + delay + "\n");
        System.out.println("Walltime: " + duration.toSeconds() + " Throughput: " + threadGroupSize*numThreadGroups*200/duration.toSeconds() + "\n");

        return startsPerSecond;
    }

    private static MultiPartBodyPublisher createBodyPublisher() throws IOException {
        MultiPartBodyPublisher publisher = new MultiPartBodyPublisher();

        // Path to the image file
        Path imagePath = Paths.get("C:\\Users\\27173\\Documents\\Example.jpg");

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
                    AsyncPostRequest.sendReviews(httpClient);
                } catch (Exception e) {
                    e.printStackTrace();
                } finally {
                    latch.countDown();
                }
            }
        }
    }
}
