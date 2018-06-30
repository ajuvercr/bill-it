package seacoalCo.bill_it.logics.group;

import android.util.Log;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

import seacoalCo.bill_it.logics.Savable;
import seacoalCo.bill_it.logics.Store;
import seacoalCo.bill_it.logics.item.Expense;
import seacoalCo.bill_it.logics.item.Item;
import seacoalCo.bill_it.logics.user.User;

public class Group implements Savable {
    private static final long serialVersionUID = 2577787792059778553L;
    private static Group currentGroup;

    public static Group getCurrentGroup() {
        return currentGroup;
    }

    private String ownerId, name, id;
    private List<String> userIds;
    private List<Item> items;
    private List<Expense> expenses;

    public Group() {
        userIds = new ArrayList<>();
        items = new ArrayList<>();
        expenses = new LinkedList<>();
    }

    public void addUser(String userId) {
        boolean good = true;
        for(String ui : userIds) {
            if (ui.equals(userId))
                good = false;
        }
        if(good){
            userIds.add(userId);
            Store.save(this);
        }
    }

    public Group(String name, String ownerId, String id) {
        this.name = name;
        this.ownerId = ownerId;
        items = new ArrayList<>();
        userIds = new ArrayList<>();
        expenses = new ArrayList<>();
        this.id = id;

        Store.save(this);
    }

    public String getOwnerId() {
        return ownerId;
    }

    public List<Item> getItems() {
        return items;
    }

    public void addItem(Item i) {
        items.add(i);
    }

    public void addAllItems(Collection<Item> is) {
        items.addAll(is);
    }

    public void setCurrent() {
        currentGroup = this;
    }

    public String getName() {
        return name;
    }

    public void unHandle(Item item, int pos) {
        items.add(pos, item);
        expenses.remove(expenses.size() - 1);
    }

    public void handle(String buyer, String consumer, Item item) {
        items.remove(item);
        expenses.add(Expense.getExpense(item, buyer, consumer, userIds.size()));
    }

    public void removeItem(Item item) {
        items.remove(item);
    }

    @Override
    public String toString() {
        return this.name;
    }

    public String toFullString() {
        String out = "Group: "+name+" "+id;
        for(String ui: userIds) {
            User u = Store.getInStateUser(ui);
            if (u != null) {
                out += "\n\t"+u.toString();
            }
        }
        for(Expense e : expenses) {
            out+= "\n\t" + e.toString();
        }
        return out + "\n";
    }

    public void deleteMember(String ui) {
        userIds.remove(ui);
    }

    public Collection<String> getUserIds() {
        return userIds;
    }

    public List<Expense> equalize(String ui) {
        List<Expense> out = new ArrayList<>();
        expenses.forEach((e) -> {
            if (e.handlePayed(ui)) out.add(e);
        });
        return out;
    }

    public List<Expense> getExpenses() {
        return expenses;
    }

    public void setOwnerId(String ownerId) {
        this.ownerId = ownerId;
    }

    public void setId(String id) {
        this.id = id;
    }

    public void setUserIds(List<String> userIds) {
        this.userIds = userIds;
    }

    public void setItems(List<Item> items) {
        this.items = items;
    }

    public void setExpenses(List<Expense> expenses) {
        this.expenses = expenses;
    }

    public void setName(String name) {
        this.name = name;
    }

    public HashMap<String, Integer> getCredits() {
        HashMap<String, Integer> credits = new HashMap<>();
        userIds.forEach((ui) -> credits.put(ui, 0));
        for(Expense e: expenses) {
            for(String ui : userIds) {
                credits.compute(ui, (u, p) -> p + e.getContribution(ui));
            }
        }
        return credits;
    }

    private class Pair<T, G> {
        T t;
        G g;
        Pair(T t, G g) {
            this.t = t;
            this.g = g;
        }
    }

    public HashMap<String, HashMap<String, Integer>> getOptimalCredits() {
        List<Pair<String, Integer>> credits = getCredits().entrySet().stream().map(s -> new Pair<String, Integer>(s.getKey(), s.getValue())).collect(Collectors.toCollection(ArrayList::new));
        ArrayList<Pair<String, Integer>> creditors = new ArrayList<Pair<String, Integer>>(credits.stream().filter(s -> s.g > 0).collect(Collectors.toCollection(ArrayList::new))) {
            public boolean add(Pair<String, Integer> mt) {
                if(mt.g == 0) {
                    return true;
                }
                int index = Collections.binarySearch(this, mt, Comparator.comparing(e -> e.g));
                if (index < 0) index = ~index;
                super.add(index, mt);
                return true;
            }
        };
        creditors.sort(Comparator.comparing(e -> e.g));
        ArrayList<Pair<String, Integer>> debtors = new ArrayList<Pair<String, Integer>>(credits.stream().filter(s -> s.g < 0).collect(Collectors.toCollection(ArrayList::new))) {
            public boolean add(Pair<String, Integer> mt) {
                if(mt.g == 0) {
                    return true;
                }
                int index = Collections.binarySearch(this, mt, Comparator.comparing(e -> -1 * e.g));
                if (index < 0) index = ~index;
                super.add(index, mt);
                return true;
            }
        };
        debtors.sort(Comparator.comparing(e -> -1*e.g));

        HashMap<String, HashMap<String, Integer>> out = new HashMap<>();
        userIds.forEach(u -> out.put(u, new HashMap<>()));

        Log.d("PING", "Creditors");
        creditors.forEach(c -> Log.d("PING", Store.getInStateUser(c.t).getName()+" -> "+c.g));
        Log.d("PING", "Debtors");
        debtors.forEach(c -> Log.d("PING", Store.getInStateUser(c.t).getName()+" -> "+c.g));

        while(!creditors.isEmpty() && !debtors.isEmpty()){
            Pair<String, Integer> c = creditors.remove(0);
            Pair<String, Integer> d = debtors.remove(0);
            out.get(d.t).putIfAbsent(c.t, 0);

            while(d.g != 0){
                int value = c.g < -1 * d.g ? c.g : -1 * d.g;
                d.g += value;
                c.g -= value;
                out.get(d.t).compute(c.t, (e, v) -> v += value);
                if(c.g == 0) {
                    if(!creditors.isEmpty()) {
                        c = creditors.remove(0);
                        out.get(d.t).putIfAbsent(c.t, 0);
                    }
                }
            }

            creditors.add(c);
            debtors.add(d);
        }

        return out;
    }

    public List<Expense> getExpensesFor(String user) {
        List<Expense> out = new ArrayList<>();
        expenses.forEach((e) -> {
            if (e.getConsumer().equals(user) || e.getBuyer().equals(user)) {
                out.add(e);
            }
        });
        return out;
    }

    @Override
    public String collection() {
        return "groups";
    }

    @Override
    public String getId() {
        return id;
    }
}
