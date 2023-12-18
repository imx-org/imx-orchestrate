# Development

## Run tests

To run all tests locally, the following Maven commands can be used:

```bash
mvn test              # for unit tests
mvn integration-test  # for integration tests
```

# Coding conventions

This project uses Surefire & Failsafe Maven plugins to execute tests. Therefore, the following conventions must be followed:

* Class names for unit testing must end with `Test`.
* Class names for integration testing must end with `IT`.
