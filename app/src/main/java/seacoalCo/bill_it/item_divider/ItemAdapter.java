package seacoalCo.bill_it.item_divider;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.ArrayList;

import seacoalCo.bill_it.R;
import seacoalCo.bill_it.logics.item.Item;

public class ItemAdapter extends RecyclerView.Adapter{
    private  ArrayList<Item> dataset_;
    private static ClickListener clickListener;

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.itemdivider_item, parent, false);
        return new ItemViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        ItemViewHolder itemHolder = (ItemViewHolder) holder;
        Item item = dataset_.get(position);

        Log.d("Items", "Item: "+item.toString());

        itemHolder.titleField.setText(item.getName());

        String price = String.valueOf(item.getPrice()/100.0f);

        // TODO add not cents, put point so it is euro
        itemHolder.priceField.setText(price);
    }

    @Override
    public int getItemCount() {
        return dataset_.size();
    }

    public static class ItemViewHolder extends RecyclerView.ViewHolder implements View.OnLongClickListener {
        TextView titleField;
        TextView priceField;

        ItemViewHolder(View v) {
            super(v);
            v.setOnLongClickListener(this);
            titleField = itemView.findViewById(R.id.title);
            priceField = itemView.findViewById(R.id.price);
        }

        @Override
        public boolean onLongClick(View v) {
            clickListener.onItemLongClick(getAdapterPosition(), v);
            return false;
        }
    }

    public void setOnItemClickListener(ClickListener listener) {
        ItemAdapter.clickListener = listener;
    }

    public ItemAdapter(ArrayList<Item> dataset) {
        dataset_ = dataset;
        Log.d("Items", "Items:");
        for(Item i: dataset) {
            Log.d("Items", "Item:\t"+i.toString());
        }
    }

    public interface ClickListener {
        void onItemLongClick(int position, View v);
    }
}
