package org.dotwebstack.orchestrate.ext.spatial;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import graphql.GraphQL;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.nio.file.Paths;
import java.util.Map;
import org.dotwebstack.orchestrate.engine.Orchestration;
import org.dotwebstack.orchestrate.engine.schema.SchemaFactory;
import org.dotwebstack.orchestrate.model.ComponentRegistry;
import org.dotwebstack.orchestrate.model.loader.ModelLoaderRegistry;
import org.dotwebstack.orchestrate.parser.yaml.YamlModelLoader;
import org.dotwebstack.orchestrate.parser.yaml.YamlModelMappingParser;
import org.dotwebstack.orchestrate.source.DataRepository;
import org.dotwebstack.orchestrate.source.ObjectRequest;
import org.dotwebstack.orchestrate.source.file.FileSource;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class SpatialIT {

  protected DataRepository prcRepository;

  @Mock
  protected DataRepository prcRepositoryStub;

  protected GraphQL graphQL;

  @BeforeEach
  void setUp() throws FileNotFoundException {
    var modelLoaderRegistry = ModelLoaderRegistry.getInstance()
        .registerModelLoader(new YamlModelLoader());
    var mappingParser = YamlModelMappingParser.getInstance(new ComponentRegistry(), modelLoaderRegistry);
    var mapping = mappingParser.parse(new FileInputStream("../data/geo/mapping.yaml"));

    prcRepository = new FileSource(mapping.getSourceModel("prc"), Paths.get("../data/prc")).getDataRepository();

    var orchestration = Orchestration.builder()
        .modelMapping(mapping)
        .source("prc", () -> prcRepositoryStub)
        .extension(new GeometryExtension())
        .build();

    graphQL = GraphQL.newGraphQL(SchemaFactory.create(orchestration))
        .build();
  }

  @Test
  void queryObject_withGeometry() {
    when(prcRepositoryStub.findOne(any(ObjectRequest.class)))
        .thenAnswer(invocation -> prcRepository.findOne(invocation.getArgument(0)));

    var result = graphQL.execute("""
          query {
            parcel(id: "P0001") {
              id
              geometry {
                asWKT
              }
            }
          }
        """);

    Map<String, Map<String, Object>> data = result.getData();

    assertThat(data).isNotNull()
        .containsKey("parcel");
  }
}
