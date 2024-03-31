package clientPart2;

import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

public class ConcurrentClientCustomExecutor {
    private final ConcurrentLinkedQueue<Long> postLatencies = new ConcurrentLinkedQueue<>();
    private final ConcurrentLinkedQueue<Long> getLatencies = new ConcurrentLinkedQueue<>();
    private final ConcurrentLinkedQueue<Long> startTimes = new ConcurrentLinkedQueue<>();


    public Map<Long, Long> startExecution(
            int threadGroupSize,
            int numThreadGroups,
            long delay,
            HttpClient client,
            HttpRequest getRequest,
            HttpRequest postRequest) throws InterruptedException {

        List<ExecutorService> executors = new ArrayList<>();

        for (int i = 0; i < numThreadGroups; i++) {
            ExecutorService executor = Executors.newFixedThreadPool(threadGroupSize);
            executors.add(executor);

            for (int j = 0; j < threadGroupSize; j++) {
                executor.submit(new RequestSender(client, getRequest, postRequest, postLatencies, getLatencies, startTimes));
            }

            if (i < numThreadGroups - 1) {
                Thread.sleep(delay); // Introduce delay before starting next group
            }
        }

        // Shutdown all executors after starting all thread groups
        executors.forEach(ExecutorService::shutdown);

        // Wait for all executors to finish
        for (ExecutorService executor : executors) {
            // Wait for all tasks to complete
            boolean tasksEnded = executor.awaitTermination(Long.MAX_VALUE, TimeUnit.NANOSECONDS);
            if (!tasksEnded) {
                System.out.println("Not all tasks completed for one of the executors.\n");
            }
        }

        System.out.println("All tasks completed.\n");

        // Calculate statistics for POST requests
        System.out.println("POST Album Request Statistics:");
        calculateAndPrintStatistics(postLatencies);

        // Calculate statistics for GET requests
        System.out.println("POST Review Request Statistics:");
        calculateAndPrintStatistics(getLatencies);

        Map<Long, Long> startsPerSecond = startTimes.stream().collect(Collectors.groupingByConcurrent(e -> e, Collectors.counting()));

        return startsPerSecond;
    }

    private void calculateAndPrintStatistics(ConcurrentLinkedQueue<Long> latencies) {
        List<Long> sortedLatencies = new ArrayList<>(latencies);
        sortedLatencies.sort(Long::compare);

        long sum = 0;
        for (Long latency : sortedLatencies) {
            sum += latency;
        }
        double mean = sum / (double) sortedLatencies.size();
        long min = sortedLatencies.get(0);
        long max = sortedLatencies.get(sortedLatencies.size() - 1);
        long median = sortedLatencies.get(sortedLatencies.size() / 2);
        long p99 = sortedLatencies.get((int) (sortedLatencies.size() * 0.99) - 1);

        System.out.printf("Mean: %.2f ms, Median: %d ms, Min: %d ms, Max: %d ms, 99th Percentile: %d ms\n",
                mean, median, min, max, p99);
    }

    private static class RequestSender implements Runnable {
        private HttpClient client;
        private HttpRequest getRequest;
        private HttpRequest postRequest;
        private ConcurrentLinkedQueue<Long> postLatencies;
        private ConcurrentLinkedQueue<Long> getLatencies;
        private ConcurrentLinkedQueue<Long> startTimes;


        public RequestSender(HttpClient client, HttpRequest getRequest, HttpRequest postRequest,
                             ConcurrentLinkedQueue<Long> postLatencies,
                             ConcurrentLinkedQueue<Long> getLatencies,
                             ConcurrentLinkedQueue<Long> startTimes) {
            this.client = client;
            this.getRequest = getRequest;
            this.postRequest = postRequest;
            this.postLatencies = postLatencies;
            this.getLatencies = getLatencies;
            this.startTimes = startTimes;
        }

        @Override
        public void run() {
            for (int i = 0; i < 100; i++) { // Each thread sends 100 GET/POST request pairs
                try {
                    Instant startPost = Instant.now();
                    startTimes.add(startPost.getEpochSecond());
                    int postStatus = MultiPartBodyPublisher.sendRequestWithRetries(this.client, this.postRequest);
                    if (postStatus == 200 || postStatus == 201) {
                        Instant endPost = Instant.now();
                        postLatencies.add(Duration.between(startPost, endPost).toMillis());
                    }

                    Instant startGet = Instant.now();
                    startTimes.add(startGet.getEpochSecond());
                    int getStatus = AsyncPostRequest.sendReviews(this.client);;
                    if (getStatus == 200 || getStatus == 201) {
                        Instant endGet = Instant.now();
                        getLatencies.add(Duration.between(startGet, endGet).toMillis());
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
