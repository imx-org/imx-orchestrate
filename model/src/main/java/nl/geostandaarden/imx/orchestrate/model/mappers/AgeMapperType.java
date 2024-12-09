package nl.geostandaarden.imx.orchestrate.model.mappers;

import java.time.LocalDate;
import java.time.Period;
import java.time.format.DateTimeParseException;
import java.util.Map;
import nl.geostandaarden.imx.orchestrate.model.ModelException;

public final class AgeMapperType implements ResultMapperType {

    @Override
    public String getName() {
        return "age";
    }

    @Override
    public ResultMapper create(Map<String, Object> options) {
        return (result, property) -> {
            if (result.isNull()) {
                return result;
            }

            try {
                var date = LocalDate.parse(String.valueOf(result.getValue()));
                var age = Period.between(date, LocalDate.now()).getYears();

                return result.withValue(age);
            } catch (DateTimeParseException e) {
                throw new ModelException("Could not parse date-time: " + result.getValue(), e);
            }
        };
    }
}