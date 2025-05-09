package nl.geostandaarden.imx.orchestrate.source.graphql.scalar;

import graphql.GraphQLContext;
import graphql.execution.CoercedVariables;
import graphql.language.StringValue;
import graphql.language.Value;
import graphql.schema.Coercing;
import graphql.schema.CoercingSerializeException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeParseException;
import java.util.Date;
import java.util.Locale;
import java.util.Objects;
import lombok.NonNull;
import org.springframework.stereotype.Component;

@Component
public class DateCoercing implements Coercing<DateSupplier, LocalDate> {

    @Override
    @SuppressWarnings("deprecation")
    public LocalDate serialize(@NonNull Object value) {
        throw new UnsupportedOperationException();
    }

    @Override
    public LocalDate serialize(@NonNull Object value, @NonNull GraphQLContext context, @NonNull Locale locale) {
        if (value instanceof LocalDate localDate) {
            return localDate;
        }

        if (value instanceof Date date) {
            return date.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
        }

        if (!(value instanceof String)) {
            throw new CoercingSerializeException(String.format(
                    "Unable to parse date string from '%s' type.",
                    value.getClass().getName()));
        }

        try {
            // to be able to also convert dates that also contain times
            if (((String) value).contains("T")) {
                return getLocalDateFromDateTimeString((String) value);
            }

            return LocalDate.parse((String) value);
        } catch (DateTimeParseException e) {
            throw new CoercingSerializeException("Parsing date string failed.", e);
        }
    }

    private LocalDate getLocalDateFromDateTimeString(@NonNull String value) {
        try {
            var zonedDateTime = ZonedDateTime.parse(value);
            return zonedDateTime.toLocalDate();
        } catch (DateTimeParseException e) {
            var localDateTime = LocalDateTime.parse(value);
            return localDateTime.toLocalDate();
        }
    }

    @Override
    @SuppressWarnings("deprecation")
    public DateSupplier parseValue(@NonNull Object value) {
        throw new UnsupportedOperationException();
    }

    @Override
    public DateSupplier parseValue(@NonNull Object value, @NonNull GraphQLContext context, @NonNull Locale locale) {
        return new DateSupplier(false, serialize(value, context, locale));
    }

    @Override
    @SuppressWarnings("deprecation")
    public DateSupplier parseLiteral(@NonNull Object value) {
        throw new UnsupportedOperationException();
    }

    @Override
    public DateSupplier parseLiteral(
            @NonNull Value<?> value,
            @NonNull CoercedVariables variables,
            @NonNull GraphQLContext context,
            @NonNull Locale locale) {
        if (value instanceof StringValue stringValue) {
            if (Objects.equals("NOW", stringValue.getValue())) {
                return new DateSupplier(true);
            }

            return new DateSupplier(false, LocalDate.parse(stringValue.getValue()));
        }

        throw new UnsupportedOperationException();
    }
}
