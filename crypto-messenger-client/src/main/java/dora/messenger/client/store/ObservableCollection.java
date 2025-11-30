package dora.messenger.client.store;

import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import static java.util.Objects.requireNonNull;

public abstract class ObservableCollection<T> extends Observable<Collection<T>> {

    private final List<CollectionObserver<T>> observers = new CopyOnWriteArrayList<>();

    /**
     * Registers a collection observer.
     */
    public void observe(@NotNull CollectionObserver<T> observer) {
        requireNonNull(observer, "observer");
        observers.add(observer);

        // Register regular observer.
        super.observe(observer);
    }

    /**
     * Notifies collection observers that an item was added.
     */
    protected void notifyAdded(T item) {
        observers.forEach((observer) -> observer.itemAdded(item));
    }

    /**
     * Notifies collection observers that an item was removed.
     */
    protected void notifyRemoved(T item) {
        observers.forEach((observer) -> observer.itemRemoved(item));
    }

    public interface CollectionObserver<T> extends Observer<Collection<T>> {

        /**
         * Called when a collection item is added.
         */
        void itemAdded(T item);

        /**
         * Called when a collection item is removed.
         */
        void itemRemoved(T item);
    }
}
