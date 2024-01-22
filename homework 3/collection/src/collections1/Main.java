package collections1;

import java.util.ArrayList;
import java.util.Vector;
import java.time.Instant;
import java.time.Duration;

public class Main {
  public static void main(String[] args) {
    Vector<Integer> vector = new Vector<>();
    ArrayList<Integer> arrayList = new ArrayList<>();
    Instant startTime1 = Instant.now();
    for (int i=0; i<100000; i++) {
      // Since java is inherently single-threaded, we don't need to explicitly create a new thread here
      vector.add(i);
    }
    Instant endTime1 = Instant.now();
    Duration duration1 = Duration.between(startTime1, endTime1);
    System.out.println("Add to vector execution time: " + duration1.toMillis() + "ms");

    Instant startTime2 = Instant.now();
    for (int j=0; j<100000; j++) {
      arrayList.add(j);
    }
    Instant endTime2 = Instant.now();
    Duration duration2 = Duration.between(startTime2, endTime2);
    System.out.println("Add to arrayList execution time: " + duration2.toMillis() + "ms");
  }
}