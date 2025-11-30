package dora.messenger.client.store;

import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import static java.util.Objects.requireNonNull;

public abstract class Observable<T> {

    private final List<Observer<T>> observers = new CopyOnWriteArrayList<>();

    /**
     * Registers an observer.
     */
    public void observe(@NotNull Observer<T> observer) {
        requireNonNull(observer, "observer");
        observers.add(observer);
    }

    /**
     * Notifies observers that the value has changed.
     */
    protected void notifyChanged(T newValue) {
        observers.forEach((observer) -> observer.valueChanged(newValue));
    }

    @FunctionalInterface
    public interface Observer<T> {

        /**
         * Called when the observed value is changed.
         */
        void valueChanged(T newValue);
    }
}
