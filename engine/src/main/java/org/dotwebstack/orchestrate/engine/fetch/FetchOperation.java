package org.dotwebstack.orchestrate.engine.fetch;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.UnaryOperator;
import java.util.logging.Level;
import lombok.Builder;
import lombok.Singular;
import org.dotwebstack.orchestrate.model.types.ObjectType;
import org.dotwebstack.orchestrate.source.ObjectRequest;
import org.dotwebstack.orchestrate.source.SelectedField;
import org.dotwebstack.orchestrate.source.Source;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.publisher.SignalType;
import reactor.util.function.Tuple2;
import reactor.util.function.Tuples;

@Builder(toBuilder = true)
public final class FetchOperation {

  private final Source source;

  private final ObjectType objectType;

  private final UnaryOperator<Map<String, Object>> keyExtractor;

  @Singular
  private final List<SelectedField> selectedFields;

  @Singular
  private final Map<String, FetchOperation> nextOperations;

  public Mono<Map<String, Object>> execute(Map<String, Object> input) {
    var objectRequest = ObjectRequest.builder()
        .objectType(objectType)
        .objectKey(keyExtractor.apply(input))
        .selectedFields(selectedFields)
        .build();

    return source.getDataRepository()
        .findOne(objectRequest)
        .log(objectType.getName(), Level.INFO, SignalType.ON_NEXT)
        .flatMap(this::executeNextOperations);
  }

  private Mono<Map<String, Object>> executeNextOperations(Map<String, Object> input) {
    return Flux.fromIterable(nextOperations.entrySet())
        .flatMap(entry -> {
          @SuppressWarnings("unchecked")
          var nestedInput = (Map<String, Object>) input.get(entry.getKey());

          if (nestedInput == null) {
            return Mono.empty();
          }

          return entry.getValue()
              .execute(nestedInput)
              .map(nestedResult -> Tuples.of(entry.getKey(), nestedResult));
        })
        .collectMap(Tuple2::getT1, Tuple2::getT2, () -> new HashMap<>(input));
  }
}
