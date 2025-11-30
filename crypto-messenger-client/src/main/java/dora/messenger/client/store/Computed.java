package dora.messenger.client.store;

import org.jetbrains.annotations.NotNull;

import java.util.function.Consumer;
import java.util.function.Supplier;

import static java.util.Objects.requireNonNull;

public class Computed<T> extends Observable<T> {

    private final Supplier<T> getter;
    private final Consumer<T> setter;

    private Computed(@NotNull Supplier<T> getter, @NotNull Consumer<T> setter) {
        this.getter = requireNonNull(getter, "getter");
        this.setter = requireNonNull(setter, "setter");
    }

    public static <T> Computed<T> of(@NotNull Supplier<T> getter, @NotNull Consumer<T> setter) {
        return new Computed<>(getter, setter);
    }

    public T get() {
        return getter.get();
    }

    public void set(T newValue) {
        setter.accept(newValue);
        notifyChanged(newValue);
    }
}
