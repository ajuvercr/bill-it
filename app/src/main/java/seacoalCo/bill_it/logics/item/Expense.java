package seacoalCo.bill_it.logics.item;

import java.io.Serializable;
import java.util.HashSet;

import seacoalCo.bill_it.logics.Store;
import seacoalCo.bill_it.logics.user.User;

public class Expense implements Serializable {
    private static final long serialVersionUID = 8287325598247931107L;
    protected Item item;
    protected String buyer;
    protected String consumer;
    private boolean nullBuyer, nullConsumer;

    public static Expense getExpense(Item i, String b, String c, int size) {
        return b.hashCode() == User.SPLITALL.getId().hashCode() || c.hashCode() == User.SPLITALL.getId().hashCode() ?
                new DividedExpense(i, b, c, size) : new Expense(i, b, c);
    }

    private Expense(Item i, String b, String c) {
        item = i;
        buyer = b;
        consumer = c;
        nullBuyer = false;
        nullConsumer = false;
    }

    public Expense() {

    }

    public void setItem(Item item) {
        this.item = item;
    }

    public void setBuyer(String buyer) {
        this.buyer = buyer;
    }

    public void setConsumer(String consumer) {
        this.consumer = consumer;
    }

    public boolean isNullBuyer() {
        return nullBuyer;
    }

    public void setNullBuyer(boolean nullBuyer) {
        this.nullBuyer = nullBuyer;
    }

    public boolean isNullConsumer() {
        return nullConsumer;
    }

    public void setNullConsumer(boolean nullConsumer) {
        this.nullConsumer = nullConsumer;
    }

    public int getContribution(String ui) {
        int out = 0;

        if(ui.equals(buyer) && !nullBuyer) {
            out += item.getPrice();
        }

        if(ui.equals(consumer) && !nullConsumer) {
            out -= item.getPrice();
        }

        return out;
    }

    public boolean handlePayed(String ui) {
        boolean changed = false;
        if(buyer.equals(ui) && !nullBuyer){
            changed = true;
            nullBuyer = true;
        }

        if(consumer.equals(ui) && !nullConsumer) {
            changed = true;
            nullConsumer = true;
        }

        return changed;
    }

    public String toString() {
        User b = Store.getInStateUser(buyer);
        User c = Store.getInStateUser(consumer);
        String buyerName = b == null ? "Buyer" : b.getName();
        String consumerName = c == null ? "Consumer" : c.getName();
        return buyerName + " "+item.toString()+" -> "+consumerName;
    }

    public Item getItem() {
        return item;
    }

    public String getBuyer() {
        return buyer;
    }

    public String getConsumer() {
        return consumer;
    }

    private static class DividedExpense extends Expense{
        private static final long serialVersionUID = -2430204493378987888L;
        private HashSet<String> payed, retrieved;
        private int size;

        private DividedExpense(Item i, String buyer, String cons, int size) {
            super(i, buyer, cons);
            payed = new HashSet<>();
            retrieved = new HashSet<>();
            this.size = size;
        }

        public int getContribution(String ui) {
            int out = super.getContribution(ui);
            int partialPrice = item.getPrice() / size;
            if (buyer.hashCode() == User.SPLITALL.getId().hashCode() && !retrieved.contains(ui)) {
                out += partialPrice;
            }

            if(consumer.hashCode() == User.SPLITALL.getId().hashCode() && !payed.contains(ui)) {
                out -= partialPrice;
            }

            return out;
        }

        public boolean handlePayed(String ui) {
            super.handlePayed(ui);
            boolean out = false;
            if (buyer.equals(User.SPLITALL.getId())) {
                out |= retrieved.add(ui);
            }
            if (consumer.hashCode() == User.SPLITALL.getId().hashCode()) {
                out |= payed.add(ui);
            }

            return out;
        }
    }
}
