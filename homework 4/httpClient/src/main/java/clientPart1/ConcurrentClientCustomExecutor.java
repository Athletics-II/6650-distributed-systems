package clientPart1;

import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class ConcurrentClientCustomExecutor {
    public void startExecution(
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
                executor.submit(new RequestSender(client, getRequest, postRequest));
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
                System.out.println("Not all tasks completed for one of the executors.");
            }
        }

        System.out.println("All tasks completed.");
    }

        private static class RequestSender implements Runnable {
            private HttpClient client;
            private HttpRequest getRequest;
            private HttpRequest postRequest;

            public RequestSender(HttpClient client, HttpRequest getRequest, HttpRequest postRequest) {
                this.client = client;
                this.getRequest = getRequest;
                this.postRequest = postRequest;
            }

            @Override
            public void run() {
                for (int i = 0; i < 1000; i++) { // Each thread sends 1000 GET/POST request pairs
                    try {
                        MultiPartBodyPublisher.sendRequestWithRetries(this.client, this.postRequest);
                        SyncGetRequest.sendRequestWithRetries(this.client, this.getRequest);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        }
}
