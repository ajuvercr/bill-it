package seacoalCo.bill_it.camera;

import android.os.Parcel;
import android.os.Parcelable;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;

import seacoalCo.bill_it.logics.item.Item;

public class ItemDraft implements Parcelable {

    private int quantity = 0;
    private String description = "";
    private double unitPrice = 0;
    private double totalPrice = 0;

    public ItemDraft() {
    }

    // Line 22 to 55 needed to be able to store in bundle
    private ItemDraft(Parcel in) {
        quantity = in.readInt();
        description = in.readString();
        unitPrice = in.readDouble();
        totalPrice = in.readDouble();
    }

    public int describeContents() {
        return 0;
    }

    @Override
    public String toString() {
        return quantity + " x " + description + ": " + unitPrice + "€ / unit, equalling a total of " + totalPrice + "€";
    }

    public void writeToParcel(Parcel out, int flags) {
        out.writeInt(quantity);
        out.writeString(description);
        out.writeDouble(unitPrice);
        out.writeDouble(totalPrice);
    }

    public static double sum(List<ItemDraft> items) {
        return items.stream().mapToDouble(ItemDraft::getTotalPrice).sum();
    }

    public static final Parcelable.Creator<ItemDraft> CREATOR = new Parcelable.Creator<ItemDraft>() {
        public ItemDraft createFromParcel(Parcel in) {
            return new ItemDraft(in);
        }

        public ItemDraft[] newArray(int size) {
            return new ItemDraft[size];
        }
    };

    public int getQuantity() {
        return quantity;
    }

    public String getDescription() {
        return description;
    }

    public double getUnitPrice() {
        return unitPrice;
    }

    public double getTotalPrice() {
        return totalPrice;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setUnitPrice(double unitPrice) {
        this.unitPrice = unitPrice;
    }

    public void setTotalPrice(double totalPrice) {
        this.totalPrice = totalPrice;
    }

    public void splitAndAdd(String line) {
        Log.d("props", line);
        String cleaned = line.replaceAll(" {2,}", " ").replace("\t", " ")
                .replaceAll(" [xX] ", " ");
        String[] propList = cleaned.split("(?<=[0-9])\\s(?=[0-9]*[a-zA-Z])|(?<=[a-zA-Z])\\s(?=[0-9]+[,.]?[0-9]*)");
        for (String prop : propList) {
            addProp(prop);
            Log.d("props", prop);
        }
    }

    private void addProp(String prop) {
        if (prop.matches("^ *[0-9]+([,.][0-9])?[0-9]* *$")) {
            if (prop.matches(".*[.,].*")) {
                prop = prop.replace(',', '.');
                double price = Double.parseDouble(prop);
                if (totalPrice == 0 || totalPrice < price) {
                    if (totalPrice < price) {
                        unitPrice = totalPrice;
                    }
                    totalPrice = price;
                }
                else {
                    unitPrice = price;
                }
            }
            else {
                quantity = Integer.parseInt(prop);
            }
        }
        else if (description.equals("")){
            description = prop;
        }
    }

    public void finish() {
        if (unitPrice != 0 && quantity == 0) {
            quantity = (int) (totalPrice / unitPrice);
        }
        else {
            if (quantity == 0) {
                quantity = 1;
            }
            if (totalPrice != 0) {
                unitPrice = totalPrice / quantity;
            }
        }
    }

    public List<Item> buildItem() {
        List<Item> items = new ArrayList<>();
        for (int i = 0; i < quantity; i++) {
            items.add(new Item(description, (int) (unitPrice * 100)));
        }
        return items;
    }
}
