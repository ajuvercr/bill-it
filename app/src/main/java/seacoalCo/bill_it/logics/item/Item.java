package seacoalCo.bill_it.logics.item;

import java.io.Serializable;

public class Item implements Serializable {
    private static final long serialVersionUID = -5692020650925998532L;
    private int price;
    private String name;

    public Item() {

    }

    public Item(String name, int price){
        this.price = price;
        this.name = name;
    }

    public void setPrice(int price) {
        this.price = price;
    }

    public void setName(String name) {
        this.name = name;
    }
    @Override
    public String toString() {
        return "Item: "+name+" "+price;
    }

    public int getPrice(){
        return price;
    }

    public String getName() {
        return name;
    }
}
