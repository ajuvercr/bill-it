package seacoalCo.bill_it.camera.custom_views;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;

import java.util.ArrayList;

import seacoalCo.bill_it.R;
import seacoalCo.bill_it.camera.ItemDraft;

public class ItemAdapter extends RecyclerView.Adapter {

    private ArrayList<ItemDraft> dataset_;

    public static class ItemViewHolder extends RecyclerView.ViewHolder {

        EditText quantityField;
        EditText descriptionField;
        EditText unitPriceField;

        ItemViewHolder(View v) {
            super(v);
            quantityField = itemView.findViewById(R.id.quantity);
            descriptionField = itemView.findViewById(R.id.description);
            unitPriceField = itemView.findViewById(R.id.unit_price);
        }

    }

    public ItemAdapter(ArrayList<ItemDraft> dataset) {
        dataset_ = dataset;
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        ItemViewHolder itemHolder = (ItemViewHolder) holder;
        ItemDraft item = dataset_.get(position);

        itemHolder.quantityField.setText(String.valueOf(item.getQuantity()));
        itemHolder.quantityField.addTextChangedListener(new MyTextWatcher(item, 'q'));

        itemHolder.descriptionField.setText(item.getDescription());
        itemHolder.descriptionField.addTextChangedListener(new MyTextWatcher(item, 'd'));

        String unitPriceString = String.valueOf(item.getUnitPrice());
        if (unitPriceString.matches("[0-9]*.[0-9]")) {
            unitPriceString += 0;
        }
        itemHolder.unitPriceField.setText(unitPriceString);
        itemHolder.unitPriceField.addTextChangedListener(new MyTextWatcher(item, 'u'));

        String totalPriceString = String.valueOf(item.getTotalPrice());
        if (totalPriceString.matches("[0-9]*.[0-9]")) {
            totalPriceString += 0;
        }
    }

    @Override
    public int getItemCount() {
        return dataset_.size();
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_draft_layout, parent, false);
        return new ItemViewHolder(v);
    }

    private class MyTextWatcher implements TextWatcher {

        private ItemDraft item;
        private char field;

        private MyTextWatcher(ItemDraft item, char field) {
            this.item = item;
            this.field = field;
        }

        @Override
        public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

        }

        @Override
        public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

        }

        @Override
        public void afterTextChanged(Editable editable) {
            if (!editable.toString().equals("")) {
                if (field == 'q') {
                    item.setQuantity(Integer.parseInt(editable.toString()));
                } else if (field == 'd') {
                    item.setDescription(editable.toString());
                } else if (field == 'u') {
                    item.setUnitPrice(Double.parseDouble(editable.toString()));
                }
            }
        }
    }
}
