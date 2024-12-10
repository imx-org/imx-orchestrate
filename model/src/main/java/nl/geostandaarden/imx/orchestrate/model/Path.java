package nl.geostandaarden.imx.orchestrate.model;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Singular;

@Getter
@EqualsAndHashCode
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public final class Path {

    private static final String PATH_SEPARATOR = "/";

    @Singular
    private final List<String> segments;

    public String getFirstSegment() {
        return segments.get(0);
    }

    public String getLastSegment() {
        return segments.get(segments.size() - 1);
    }

    public boolean isLeaf() {
        return segments.size() == 1;
    }

    public Path withoutFirstSegment() {
        return new Path(segments.subList(1, segments.size()));
    }

    public Path append(String segment) {
        var newSegments = new ArrayList<>(segments);
        newSegments.add(segment);
        return new Path(Collections.unmodifiableList(newSegments));
    }

    public Path append(Path path) {
        var newSegments = new ArrayList<>(segments);
        newSegments.addAll(path.getSegments());
        return new Path(Collections.unmodifiableList(newSegments));
    }

    public static Path empty() {
        return new Path(List.of());
    }

    public static Path fromString(String path) {
        var segments = Arrays.asList(path.split(PATH_SEPARATOR));
        return new Path(segments);
    }

    public static Path fromProperties(Property... properties) {
        var segments = Arrays.stream(properties).map(Property::getName).toList();

        return new Path(segments);
    }

    @Override
    public String toString() {
        return String.join(PATH_SEPARATOR, segments);
    }
}
