run com.example.demo.DemoClient.main()

The example runs 2 http post's (one without timeout, the other with a timeout) on each of the following clients:

- (sync) client with default configuration (HttpClients.createDefault()). This client will wait endlessly if no timeout is defined in the request;
- (sync) client with custom RequestConfig. This client will always exit after its timeout expires even if the request has no timeout defined.
- asyncClient with default configuration (HttpAsyncClients.createDefault()). This client will wait endlessly if no timeout is defined in the request;
- asyncClient with custom connection manager and custom ioReactor. This client will always exit after its timeout expires even if the request has no timeout defined.

