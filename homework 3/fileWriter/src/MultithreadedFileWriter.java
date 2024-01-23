import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;

public class MultithreadedFileWriter {
    private static final int NUMBER_OF_THREADS = 500;
    private static final int STRINGS_PER_THREAD = 1000;

    public static void main(String[] args) throws InterruptedException {
        CountDownLatch completed = new CountDownLatch(NUMBER_OF_THREADS);
        long startTime = System.currentTimeMillis();
        BufferedWriter writer = null;

        try {
            writer = new BufferedWriter(new FileWriter("output1.txt"));
            for (int i = 0; i < NUMBER_OF_THREADS; i++) {
                new Thread(new WriteTaskA(writer, completed)).start();
            }
            completed.await();
        } catch (IOException | InterruptedException e) {
            System.err.println("Error working with the file: " + e.getMessage());
        } finally {
            if (writer != null) {
                try {
                    writer.close();
                } catch (IOException e) {
                    System.err.println("Error closing the file: " + e.getMessage());
                }
            }
        }
        long endTime = System.currentTimeMillis();
        System.out.println("Execution time: " + (endTime - startTime) + "ms");

        CountDownLatch completed2 = new CountDownLatch(NUMBER_OF_THREADS);
        startTime = System.currentTimeMillis();
        try {
            writer = new BufferedWriter(new FileWriter("output2.txt"));
            for (int i = 0; i < NUMBER_OF_THREADS; i++) {
                new Thread(new WriteTaskB(writer, completed2)).start();
            }
            completed2.await();
        } catch (IOException | InterruptedException e) {
            System.err.println("Error working with the file: " + e.getMessage());
        } finally {
            if (writer != null) {
                try {
                    writer.close();
                } catch (IOException e) {
                    System.err.println("Error closing the file: " + e.getMessage());
                }
            }
        }
        endTime = System.currentTimeMillis();
        System.out.println("Execution time: " + (endTime - startTime) + "ms");
    }

    private static class WriteTaskA implements Runnable {
        private final BufferedWriter writer;
        private final CountDownLatch latch;

        public WriteTaskA(BufferedWriter writer, CountDownLatch latch) {
            this.writer = writer;
            this.latch = latch;
        }

        @Override
        public void run() {
            try {
                for (int i = 0; i < STRINGS_PER_THREAD; i++) {
                    String data = System.currentTimeMillis() + ", " + Thread.currentThread().getId() + ", " + i + "\n";
                    synchronized (writer) {
                        writer.write(data);
                    }
                }
            } catch (IOException e) {
                System.err.println("Error writing to file: " + e.getMessage());
            } finally {
                latch.countDown();
            }
        }
    }


    private static class WriteTaskB implements Runnable {
        private final BufferedWriter writer;
        private final CountDownLatch latch;

        public WriteTaskB(BufferedWriter writer, CountDownLatch latch) {
            this.writer = writer;
            this.latch = latch;
        }

        @Override
        public void run() {
            try {
                List<String> dataToWrite = new ArrayList<>();
                for (int i = 0; i < STRINGS_PER_THREAD; i++) {
                    String data = System.currentTimeMillis() + ", " + Thread.currentThread().getId() + ", " + i;
                    dataToWrite.add(data);
                }
                writeData(dataToWrite);
            } finally {
                latch.countDown();
            }
        }

        private void writeData(List<String> dataToWrite) {
            synchronized (writer) {
                try {
                    for (String data : dataToWrite) {
                        writer.write(data + "\n");
                    }
                } catch (IOException e) {
                    System.err.println("Error writing to file: " + e.getMessage());
                }
            }
        }
    }
}
