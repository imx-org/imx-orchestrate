package nl.geostandaarden.imx.orchestrate.source.graphql.scalar;

import java.time.OffsetDateTime;
import java.util.function.Supplier;

public class DateTimeSupplier implements Supplier<OffsetDateTime> {

    private final boolean isNow;

    private final OffsetDateTime dateTime;

    public DateTimeSupplier(boolean isNow, OffsetDateTime dateTime) {
        this.isNow = isNow;
        this.dateTime = dateTime;
    }

    public DateTimeSupplier(boolean isNow) {
        this.isNow = isNow;
        this.dateTime = null;
    }

    @Override
    public OffsetDateTime get() {
        if (isNow) {
            return OffsetDateTime.now();
        }
        return dateTime;
    }
}
