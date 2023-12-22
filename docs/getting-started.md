# Getting started

The easiest way to get started with the IMX orchestration engine is creating a new Java project. An example project can
be found here: https://github.com/imx-org/imx-orchestrate-imxgeo

Create a new Maven project, using [Spring Initializr](https://start.spring.io).

Add the following dependency to your POM file (`pom.xml`):

```xml
<dependency>
    <groupId>nl.geostandaarden.imx.orchestrate</groupId>
    <artifactId>orchestrate-gateway</artifactId>
    <version>0.1.0</version>
</dependency>
```

Create a `/config` folder, containing the model and model mapping YAML documents. Models and model mappings can be provided as YAML documents, following
the following schemas:
- [Model schema](/parser-yaml/src/main/resources/model.yaml)
- [Model mapping schema](/parser-yaml/src/main/resources/model-mapping.yaml)

The model mapping language is described here: https://geonovum.github.io/IMX-ModelMapping/

In this example, we'll be using the `file` source adapter to get up & running quickly. However, there are other source adapters available:
- [GraphQL](https://github.com/imx-org/imx-orchestrate/tree/main/source-graphql)
- [OGC API Features](https://github.com/imx-org/imx-orchestrate-ogcapi)

Create a `/data` folder with a subfolder for every source. Each subfolder should contain JSON data files for
every source object type. The name of the file must be identical to the name of the object type,
e.g: `/data/adr/Address.json` (where `adr` is the name of the source, and `Address` is the name of the object type).

Add the following section to `/src/main/resources/application.yml`:

```yaml
orchestrate:
  mapping: config/mapping.yaml
  sources:
    adr:
      type: file
      options:
        dataPath: data/adr
```

Run the application using Maven:

```bash
mvn spring-boot:run
```

Open the GraphQL UI: http://localhost:8080/graphiql?path=/graphql
