package dora.messenger.client.store;

public class Ref<T> extends Observable<T> {

    private volatile T value;

    private Ref(T value) {
        this.value = value;
    }

    public static <T> Ref<T> of(T value) {
        return new Ref<>(value);
    }

    public static <T> Ref<T> ofNull() {
        return new Ref<>(null);
    }

    public T get() {
        return value;
    }

    public void set(T newValue) {
        value = newValue;
        notifyChanged(newValue);
    }
}
