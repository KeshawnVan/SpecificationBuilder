package demo;

import org.apache.commons.collections.CollectionUtils;

import java.util.Collection;
import java.util.Optional;

public final class Nullable {

    private Nullable() {
    }

    public static <T> Optional<T> of(T value) {
        return value instanceof Collection
                ? CollectionUtils.isEmpty((Collection) value) ? Optional.empty() : Optional.of(value)
                : Optional.ofNullable(value);
    }
}
