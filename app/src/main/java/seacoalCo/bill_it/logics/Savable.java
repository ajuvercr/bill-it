package seacoalCo.bill_it.logics;

import java.io.Serializable;

public interface Savable extends Serializable {
    String collection();

    String getId();
}
