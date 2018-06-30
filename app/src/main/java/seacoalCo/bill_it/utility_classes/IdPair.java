package seacoalCo.bill_it.utility_classes;

/**
 * Created by silvius_seacoal on 06.05.18.
 */

public class IdPair<T> {
    public String id;
    public T item;

    public IdPair(String id, T item) {
        this.item = item;
        this.id = id;
    }
}
