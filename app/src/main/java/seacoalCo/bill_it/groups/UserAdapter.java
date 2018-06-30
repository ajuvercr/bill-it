package seacoalCo.bill_it.groups;

import android.app.Activity;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import seacoalCo.bill_it.R;
import seacoalCo.bill_it.logics.Store;
import seacoalCo.bill_it.logics.group.Group;
import seacoalCo.bill_it.logics.user.User;
import seacoalCo.bill_it.mail.Mail;

public class UserAdapter extends RecyclerView.Adapter{
    public final static String addMemberString = "ADDMEMEBERS";
    private Runnable addFriends;
    protected ArrayList<String> users;
    protected RecyclerView.ViewHolder holder;

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.overview_user, parent, false);
        return new UserViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {

        this.holder = holder;
        UserViewHolder itemHolder = (UserViewHolder) holder;

        if(users.get(position).equals(addMemberString)) {
            itemHolder.nameField.setText("Add Members");
            itemHolder.emailField.setText("");
            itemHolder.emailField.setHeight(0);
            itemHolder.creditField.setText("");
            itemHolder.creditField.setHeight(0);
            itemHolder.actionButton.setText("Add");
            itemHolder.imageView.setImageResource(R.drawable.add_icon_64);
            itemHolder.container.setOnClickListener((e) -> action(position, itemHolder));
        }else {
            User u = Store.getInStateUser(users.get(position));

            HashMap<String, Integer> credits = Group.getCurrentGroup().getCredits();

            // fill fields
            if (u == null) {
                itemHolder.nameField.setText("Loading");
                itemHolder.emailField.setText("");
                itemHolder.creditField.setText("");
            } else {
                itemHolder.emailField.setText(u.getEmail());
                itemHolder.nameField.setText(u.getName());
                itemHolder.creditField.setText(String.valueOf(credits.getOrDefault(u.getId(), 0)/100.0f));
                itemHolder.actionButton.setVisibility(View.INVISIBLE);
            }

        }
        itemHolder.actionButton.setOnClickListener((v) -> action(position, itemHolder));
    }

    protected void action(int pos, UserViewHolder holder) {
        if(users.get(pos).equals(addMemberString)) {
            addFriends.run();
        }else {
            HashMap<String, HashMap<String, Integer>> out = Group.getCurrentGroup().getOptimalCredits();

            String debts = "Debtor\t\t->\tCreditor\tAmount";
            for(Map.Entry<String, HashMap<String, Integer>> ds: out.entrySet()){
                for(Map.Entry<String, Integer> c: ds.getValue().entrySet()){
                    debts+= "\n"+Store.getInStateUser(ds.getKey()).getName()+"\t->\t"+Store.getInStateUser(c.getKey()).getName()+" "+c.getValue()/100.0f;
                }
            }
            Group group = Group.getCurrentGroup();
            User user = Store.getInStateUser(users.get(pos));
            Mail mail = new Mail(new String[]{user.getEmail()},
                    "Bill repartition from Bill-it", "Hello " + user.getName() + "," +
                    "\nAccording to Bill-it:\n"
                    + generateContent(group,user) + "\nBest Regards.");
            ((Activity) holder.imageView.getContext()).startActivityForResult(mail.getIntentForSending(), 2);
        }
    }

    private String generateContent(Group group,User user){
        HashMap<String, Integer> credits = group.getCredits();
        String content = "";
        for(String userid: group.getUserIds()){
            String line = "- ";
            String name = Store.getInStateUser(userid).getName();
            if(name.equals(user.getName())){
                line += "You have ";
            }else{
                line += name + " has ";
            }
            line += "to ";
            double credit = credits.getOrDefault(userid,0) / 100.0;
            if(credit < 0){
                line += "pay €" + String.valueOf(-credit) + "\n";
            }
            else if(credit > 0){
                line += "receive €" + String.valueOf(credit) +"\n";
            }else {
                line += "do nothing\n";
            }
            content += line;
        }
        return content;
    }


    public void setAddFriends(Runnable addFriends) {
        this.addFriends = addFriends;
    }

    public void setContent(ArrayList<String> users) {
        this.users = users;
        this.users.add(addMemberString);
    }

    @Override
    public int getItemCount() {
        return users.size();
    }

    public void deleteUserFromGroup(int pos) {
        Group.getCurrentGroup().deleteMember(users.get(pos));
        deleteItem(pos);
    }

    public static class UserViewHolder extends RecyclerView.ViewHolder {
        TextView nameField, emailField, creditField;
        Button actionButton;
        ImageView imageView;
        LinearLayout container;

        UserViewHolder(View v) {
            super(v);
            nameField = itemView.findViewById(R.id.name);
            emailField = itemView.findViewById(R.id.email);
            creditField = itemView.findViewById(R.id.credit);
            actionButton = itemView.findViewById(R.id.actionButton);
            imageView = itemView.findViewById(R.id.imageView3);
            container = itemView.findViewById(R.id.overview_user_wrapper);
        }
    }

    public UserAdapter(ArrayList<String> dataset) {
        users = dataset;

        users.forEach((u) -> {
            Store.getUser(u, (f, us) -> {
                if(f) {
                    notifyDataSetChanged();
                }
            });
        });

        users.add(addMemberString);
    }

    void deleteItem(int position) {
        users.remove(position);
        notifyItemRemoved(position);
        notifyItemRangeChanged(position, users.size());
        //holder.itemView.setVisibility(View.GONE);
        notifyDataSetChanged();
    }

    public String getItem(int position) {
        return users.get(position);
    }
}
