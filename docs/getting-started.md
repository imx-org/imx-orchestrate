# Getting started

The easiest way to get started with the IMX orchestration engine is creating a new Java project. An example project can
be found here: https://github.com/imx-org/imx-orchestrate-imxgeo

Add the following dependency to your POM file:

```xml
<dependency>
    <groupId>nl.geostandaarden.imx.orchestrate</groupId>
    <artifactId>orchestrate-gateway</artifactId>
    <version>0.1.0</version>
</dependency>
```

Create a folder, containing the model and model mapping documents. Models and model mappings  can be provided as YAML documents, following
the following schemas:
- [Model schema](/parser-yaml/src/main/resources/model.yaml)
- [Model mapping schema](/parser-yaml/src/main/resources/model-mapping.yaml)
