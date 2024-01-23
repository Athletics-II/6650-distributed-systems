import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.atomic.AtomicInteger;

public class ProducerConsumerFileWriter {

    // Shared queue
    private static final BlockingQueue<String> queue = new LinkedBlockingQueue<>();
    private static final int NUMBER_OF_PRODUCERS = 500;
    private static final AtomicInteger completedProducers = new AtomicInteger(0);

    public static void main(String[] args) throws InterruptedException {
        long startTime = System.currentTimeMillis();
        BufferedWriter writer = null;
        try {
            writer = new BufferedWriter(new FileWriter("output3.txt"));
        } catch (IOException e) {
            System.err.println("Error working with the file: " + e.getMessage());
        }
        // Start consumer thread
        Thread consumerThread = new Thread(new FileWritingConsumer(writer));
        consumerThread.start();

        // Start producer threads
        for (int i = 0; i < 500; i++) {
            new Thread(new StringProducer()).start();
        }

        long endTime = System.currentTimeMillis();
        System.out.println("Execution time: " + (endTime - startTime) + "ms");
    }

    static class StringProducer implements Runnable {
        @Override
        public void run() {
            for (int i = 0; i < 1000; i++) {
                String data = System.currentTimeMillis() + ", " + Thread.currentThread().getId() + ", " + i + "\n";; // Generate string data
                try {
                    queue.put(data); // Put data in the queue
                    if (completedProducers.incrementAndGet() == NUMBER_OF_PRODUCERS) {
                        queue.put("EOF"); // Put end-of-data signal in the queue
                    }
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            }
        }
    }

    static class FileWritingConsumer implements Runnable {
        private final BufferedWriter writer;

        public FileWritingConsumer(BufferedWriter writer) {
            this.writer = writer;
        }
        @Override
        public void run() {
            while (true) {
                String data = null; // Take data from the queue
                try {
                    data = queue.take();
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
                if (data.equals("EOF")) { // Check for end-of-data signal
                    break;
                }
                // Write data to file
                try {
                    writer.write(data);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
        }
    }
}