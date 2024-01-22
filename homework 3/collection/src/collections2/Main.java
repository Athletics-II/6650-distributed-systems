package collections2;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Hashtable;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.Map;

class Task implements Runnable{
  private final Map<Integer, Integer> map;
  private final int startRange;

  Task(Map<Integer, Integer> map, int startRange) {
    this.map = map;
    this.startRange = startRange;
  }

  @Override
  public void run() {
    for (int i = startRange; i < startRange + 10000; i++) {
      map.put(i, i);
    }
  }
}

public class Main {
  public static void main(String[] args) {
    System.out.println("Single thread 1. HashTable 2. HashMap 3. ConcurrentHashMap\n");
    singleThread(new Hashtable<>());
    singleThread(new HashMap<>());
    singleThread(new ConcurrentHashMap<>());
    System.out.println("100 threads 1. HashTable 2. HashMap 3. ConcurrentHashMap\n");
    multiThread(new Hashtable<>());
    multiThread(Collections.synchronizedMap(new HashMap<>()));
    multiThread(new ConcurrentHashMap<>());
  }

  public static void singleThread(Map<Integer, Integer> map) {
    Instant startTime = Instant.now();
    for (int i=0; i<1000000; i++) {
      map.put(i, i);
    }
    Instant endTime = Instant.now();
    Duration duration = Duration.between(startTime, endTime);
    System.out.println("Map size " + map.size() + " Execution time: " + duration.toMillis() + "ms\n");
  }

  public static void multiThread(Map<Integer, Integer> map) {
    List<Thread> threads = new ArrayList<>();
    Instant startTime = Instant.now();
    for (int i=0; i<100; i++) {
      Thread t = new Thread(new Task(map, i*10000));
      threads.add(t);
      t.start();
    }
    for (Thread t : threads) {
      try {
        t.join();
      } catch (InterruptedException e) {
        e.printStackTrace();
      }
    }
    Instant endTime = Instant.now();
    Duration duration = Duration.between(startTime, endTime);
    System.out.println("Map size " + map.size() + " Execution time: " + duration.toMillis() + "ms\n");
  }
}
