package seacoalCo.bill_it.friends;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.TextView;

import seacoalCo.bill_it.R;
import seacoalCo.bill_it.logics.Store;
import seacoalCo.bill_it.logics.user.User;

public class FriendViewHolder extends RecyclerView.ViewHolder {
    private TextView txtName;
    private TextView txtEmail;

    public FriendViewHolder(View itemView) {
        super(itemView);
        txtName = itemView.findViewById(R.id.txtName);
        txtEmail = itemView.findViewById(R.id.txtEmail);
    }

    public void setContent(String g) {
        User u = Store.getInStateUser(g);
        if (u == null) {
            txtName.setText("Loading");
            txtEmail.setText("");
        } else {
            txtName.setText(u.getName());
            txtEmail.setText(u.getEmail());
        }
    }
}
