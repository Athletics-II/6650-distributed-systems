# Client Design

Author: Siyi Ling

## Packages:

ClientPart1

ClientPart2

As per the assignment request, the two parts of performance testing are put into two different packages. There is some duplicated code in the two packages because the logic is mostly the same. This can of course be tackled by abstracting away the classes with similar logic to a virtual class and having child classes override the methods. But due to time limit, it is not implemented in this version.

## Major classes:

AlbumClientAPI

- This is where the main resides. The main calls a function `sendRequest` which takes in parameterized `threadGroupSize`, `numThreadGroups`, `delay`, `getURI`, `postURI`. The	`sendRequest` first uses a `CountDownLatch` to handle the concurrency of the first 10 threads for initialization. The 10 threads run instances of a private class `InitializationWorker` that implements the POST/GET requests and is also defined in the `AlbumClientAPI` class. Then it calls `createBodyPublisher` that returns a `MultiPartBodyPublisher` object, which serves as the payload of the POST request, and constructs two httpRequest instances `getRequest` and `postRequest`. After these set-ups, the `sendRequest` calls the constructor and `startExecution` of the `ConcurrentClientCustomExecutor` class, which handles concurrent request sending, records the timestamps, and prints the load test results.
- The main in `ClientPart2` exports the map that has epoch seconds and throughput as key-value pairs, which is returned from `startExecution` to a csv file on local machine, for throughput plotting.


SyncGetRequest

- This is a simple class that implements sending http request logic with up to 5 times of retries.
- This class remains the same for both `ClientPart1` and `ClientPart2`


MultiPartBodyPublisher

- This is a class built specifically for the multi-part request body of the POST api that is used in this assignment. It has a `addPart` method for album image and a `addJsonPart` method for album profile, and converts the payload to proper Json format.


ConcurrentClientCustomExecutor

- This class has a private class `RequestSender` that takes in the http client and request objects initialized in the `AlbumClientAPI` class and implements sending logic (1000 POST/GET pairs).
- This class uses `ExecutorService` in `startExecution` method to handle concurrency. It iterates `numThreadGroups` times, initializes an executor with `fixedThreadPool` of `threadGroupSize` and adds the executor to an arraylist in each iteration. In that iteration, it also submits a worker routine `RequestSender`, then sleep for `delay` amount of time. This is abiding by the assignment request, i.e. starting the next thread group immediately after the delay. After all thread groups are started, the executors in the arraylist are sequentially shut down. Then, for each executor, it awaits all threads to terminate.
- The `RequestSender` varies in `ClientPart2` by adding the logic of taking timestamps before and after each GET and POST request, adding the latencies to two `ConcurrentLinkedQueue` instances for statistical information, and adding the start times to a `ConcurrentLinkedQueue` instance for constructing the map needed for plotting.
- The `startExecution` varies in `ClientPart2` by adding the logic of converting the `ConcurrentLinkedQueue` instance mentioned above to a map of epoch seconds and throughputs as kv pairs by counting the concurrent occurences in the queue.

## How to Run

To view results of Client Part 1:

Run **AlbumClientAPI.main()** in `clientPart1`

To view results of Client Part 2:

Run **AlbumClientAPI.main()** in `clientPart2`


> Written with [StackEdit](https://stackedit.io/).
