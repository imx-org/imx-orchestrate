package org.dotwebstack.orchestrate.ext.spatial;

import static org.assertj.core.api.Assertions.assertThat;
import static org.dotwebstack.orchestrate.ext.spatial.TestFixtures.createBgtModel;
import static org.dotwebstack.orchestrate.ext.spatial.TestFixtures.createModelMapping;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import graphql.GraphQL;
import java.nio.file.Paths;
import java.util.Map;
import org.dotwebstack.orchestrate.engine.Orchestration;
import org.dotwebstack.orchestrate.engine.schema.SchemaFactory;
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

  protected DataRepository bgtRepository;

  @Mock
  protected DataRepository bgtRepositoryStub;

  protected GraphQL graphQL;

  @BeforeEach
  void setUp() {
    bgtRepository = new FileSource(createBgtModel(), Paths.get("../data/bgt")).getDataRepository();

    var orchestration = Orchestration.builder()
        .modelMapping(createModelMapping())
        .source("bgt", () -> bgtRepositoryStub)
        .extension(new GeometryExtension())
        .build();

    graphQL = GraphQL.newGraphQL(SchemaFactory.create(orchestration))
        .build();
  }

  @Test
  void queryObject_withGeometry() {
    when(bgtRepositoryStub.findOne(any(ObjectRequest.class)))
        .thenAnswer(invocation -> bgtRepository.findOne(invocation.getArgument(0)));

    var result = graphQL.execute("""
          query {
            gebouw(identificatie: "G0200.42b3d39246840268e0530a0a28492340") {
              identificatie
              bovenaanzichtgeometrie {
                asWKT
              }
            }
          }
        """);

    Map<String, Map<String, Object>> data = result.getData();
    assertThat(data).isNotNull()
        .containsKey("gebouw");
  }
}
