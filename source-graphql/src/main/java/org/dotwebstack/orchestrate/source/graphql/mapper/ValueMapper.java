package org.dotwebstack.orchestrate.source.graphql.mapper;

import graphql.language.ArrayValue;
import graphql.language.BooleanValue;
import graphql.language.IntValue;
import graphql.language.StringValue;
import graphql.language.Value;
import java.util.List;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.dotwebstack.orchestrate.source.SourceException;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
class ValueMapper {

  static Value<?> mapToValue(Object value) {
    if (value instanceof String s) {
      return StringValue.of(s);
    }
    if (value instanceof Integer i) {
      return IntValue.of(i);
    }
    if (value instanceof Boolean b) {
      return BooleanValue.of(b);
    }
    if (value instanceof List<?> l) {
      var array = ArrayValue.newArrayValue();
      l.forEach(item -> array.value(mapToValue(item)));
      return array.build();
    }

    throw new SourceException(String.format("Value type '%s' is unsupported.", value.getClass()));
  }
}
