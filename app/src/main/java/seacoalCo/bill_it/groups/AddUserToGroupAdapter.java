package seacoalCo.bill_it.groups;


import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.stream.Collectors;

import seacoalCo.bill_it.R;
import seacoalCo.bill_it.logics.Store;
import seacoalCo.bill_it.logics.group.Group;

public class AddUserToGroupAdapter extends UserAdapter{

    private Context c;

    public AddUserToGroupAdapter(ArrayList<String> dataset, Context c) {
        super(dataset.stream()
                .filter((u) -> !Group.getCurrentGroup().getUserIds().contains(u))
                .collect(Collectors.toCollection(ArrayList::new)));

        this.c = c;

        this.users.remove(this.users.size()-1); // delete add user
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        super.onBindViewHolder(holder, position);
        UserViewHolder itemHolder = (UserViewHolder) holder;

        itemHolder.creditField.setText("0");
        itemHolder.creditField.setHeight(0);

        itemHolder.actionButton.setText("Add");
        itemHolder.actionButton.setVisibility(View.VISIBLE);
    }

    @Override
    public void setContent(ArrayList<String> content) {
        this.users = content;
    }

    @Override
    protected void action(int pos, UserViewHolder holder) {
        Group g = Group.getCurrentGroup();
        g.addUser(users.get(pos));
        Store.getUser(users.get(pos), (t, u) -> {
            if(t)
                u.addGroup(g.getId());
        });
        deleteItem(pos);
        Toast toast = Toast.makeText(c, R.string.friend_added, Toast.LENGTH_SHORT);
        toast.show();
    }
}
