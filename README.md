Some examples of HttpAsyncClient behavior:
- with default configuration (will wait endlessly if no timeout is defined in the request)
- with custom connection manager and default ioReactor (same)
- with custom connection manager and custom ioReactor (will always exit after timeout expires)

run com.example.demo.DemoClient.main()
