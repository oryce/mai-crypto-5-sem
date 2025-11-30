package dora.messenger.client.store;

import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.function.Predicate;

public class CollectionRef<T> extends ObservableCollection<T> {

    private volatile Collection<T> collection;

    private CollectionRef(Collection<T> collection) {
        this.collection = collection;
    }

    public static <T> CollectionRef<T> of(Collection<T> collection) {
        return new CollectionRef<>(collection);
    }

    public static <T> CollectionRef<T> ofNull() {
        return new CollectionRef<>(null);
    }

    public Collection<T> get() {
        if (collection == null) return null;
        return Collections.unmodifiableCollection(collection);
    }

    public void set(Collection<T> newValue) {
        collection = newValue;
        notifyChanged(newValue);
    }

    public boolean add(T item) {
        boolean added = collection.add(item);

        if (added) {
            notifyAdded(item);
            notifyChanged(collection);
        }

        return added;
    }

    public boolean remove(T item) {
        boolean removed = collection.remove(item);

        if (removed) {
            notifyRemoved(item);
            notifyChanged(collection);
        }

        return removed;
    }

    public boolean removeIf(Predicate<? super T> filter) {
        Iterator<T> iterator = collection.iterator();
        boolean removedAny = false;

        while (iterator.hasNext()) {
            T item = iterator.next();

            if (filter.test(item)) {
                iterator.remove();
                notifyRemoved(item);
                removedAny = true;
            }
        }

        if (removedAny) {
            notifyChanged(collection);
        }

        return removedAny;
    }
}
