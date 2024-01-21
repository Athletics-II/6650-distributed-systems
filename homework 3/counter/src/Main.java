import java.time.Duration;
import java.time.Instant;

class Resource {
  private static int counter = 0;

  public synchronized void increment() {
    while (counter < 10) {
      counter++;
    }
  }

  public int getCounter() {
    return counter;
  }
}

class Task implements Runnable{
  private Resource res;

  public Task(Resource res) {
    this.res = res;
  }

  @Override
  public void run() {
    res.increment();
  }
}

public class Main {
  public static void main(String[] args) {
    int threadNumber = 1000;
    if (args.length > 0) {
      try {
        threadNumber = Integer.parseInt(args[0]);
      } catch (NumberFormatException e) {
        System.out.println("Argument is not a valid integer. Using default value: " + threadNumber);
      }
    }
    Resource res = new Resource();
    Instant startTime = Instant.now();
    for (int i = 0; i < threadNumber; i++) {
      Thread t = new Thread(new Task(res));
      t.start();
      try {
        t.join();
      } catch (InterruptedException e) {
        e.printStackTrace();
      }
    }

    Instant endTime = Instant.now();
    Duration duration = Duration.between(startTime, endTime);
    long millis = duration.toMillis();
    System.out.println("Counter: " + res.getCounter() + " Execution time: " + millis + "ms\n");
  }

}
