package seacoalCo.bill_it.item_divider;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.HashMap;

import seacoalCo.bill_it.R;
import seacoalCo.bill_it.custom_views.CircularTextView;
import seacoalCo.bill_it.logics.Store;
import seacoalCo.bill_it.logics.group.Group;
import seacoalCo.bill_it.logics.user.User;

public class UserAdapter extends RecyclerView.Adapter {
    private ArrayList<String> dataset_;

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.itemdivider_user, parent, false);
        return new UserViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        UserViewHolder itemHolder = (UserViewHolder) holder;

        User user = Store.getInStateUser(dataset_.get(position));

        if(user != null) {
            HashMap<String, Integer> credits = Group.getCurrentGroup().getCredits();
            itemHolder.userNameField.setText(user.getName());

            int credit = credits.getOrDefault(dataset_.get(position), 0);
            if (credit < 0) {
                itemHolder.creditField.setStrokeColor("red");
            } else if (credit > 0) {
                itemHolder.creditField.setStrokeColor("green");
            } else {
                itemHolder.creditField.setStrokeColor("grey");
            }

            itemHolder.creditField.setStrokeWidth(5);
            itemHolder.creditField.setSolidColor("White");
            itemHolder.creditField.setText(String.valueOf(credit/100.0f));
        }else{
            itemHolder.creditField.setStrokeWidth(0);
            itemHolder.creditField.setSolidColor("white");
            itemHolder.creditField.setText("");
        }
    }

    @Override
    public int getItemCount() {
        return dataset_.size();
    }

    public static class UserViewHolder extends RecyclerView.ViewHolder {
        CircularTextView creditField;
        TextView userNameField;

        UserViewHolder(View v) {
            super(v);
            creditField = itemView.findViewById(R.id.credit);
            userNameField = itemView.findViewById(R.id.user);
        }
    }

    public UserAdapter(ArrayList<String> dataset) {
        dataset_ = dataset;
    }
}
