Some examples of HttpAsyncClient behavior:
- with default configuration (will wait endlessly)
- with custom connection manager and default ioReactor (same)
- with custom connection manager and custom ioReactor (will exit after timeout expires)

run com.example.demo.DemoClient.main()
