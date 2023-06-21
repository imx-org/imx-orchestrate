package org.dotwebstack.orchestrate.source.graphql.mapper;

import graphql.language.ArrayValue;
import graphql.language.BooleanValue;
import graphql.language.IntValue;
import graphql.language.StringValue;
import org.dotwebstack.orchestrate.source.SourceException;
import org.junit.jupiter.api.Test;

import java.math.BigInteger;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ValueMapperTest {

    @Test
    void mapToValue_returnsValue_forString() {
        var result = ValueMapper.mapToValue("Test");

        assertThat(result).isInstanceOf(StringValue.class);
        assertThat(((StringValue)result).getValue()).isEqualTo("Test");
    }

    @Test
    void mapToValue_returnsValue_forInteger() {
        var result = ValueMapper.mapToValue(123);

        assertThat(result).isInstanceOf(IntValue.class);
        assertThat(((IntValue)result).getValue()).isEqualTo(BigInteger.valueOf(123));
    }

    @Test
    void mapToValue_returnsValue_forBoolean() {
        var result = ValueMapper.mapToValue(true);

        assertThat(result).isInstanceOf(BooleanValue.class);
        assertThat(((BooleanValue)result).isValue()).isTrue();
    }

    @Test
    void mapToValue_returnsValue_forList() {
        var result = ValueMapper.mapToValue(List.of("Test1", "Test2"));

        assertThat(result).isInstanceOf(ArrayValue.class);
        assertThat(((ArrayValue)result).getValues()).satisfiesExactly(
                item1 -> assertThat(((StringValue)item1).getValue()).isEqualTo("Test1"),
                item2 -> assertThat(((StringValue)item2).getValue()).isEqualTo("Test2"));
    }

    @Test
    void mapToValue_throwsException_forUnsupportedType() {
        var value = BigInteger.valueOf(123);
        assertThatThrownBy(() -> ValueMapper.mapToValue(value)).isInstanceOf(SourceException.class)
                .hasMessageContaining("Value type 'class java.math.BigInteger' is unsupported.");
    }
}
