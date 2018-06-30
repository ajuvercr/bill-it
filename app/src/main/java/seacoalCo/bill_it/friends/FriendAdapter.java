package seacoalCo.bill_it.friends;

import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.List;

import seacoalCo.bill_it.R;
import seacoalCo.bill_it.logics.Store;
import seacoalCo.bill_it.logics.user.User;

public class FriendAdapter extends RecyclerView.Adapter<FriendViewHolder> {
    private List<String> data;
    private Context context;

    public FriendAdapter(List<String> data, Context context) {
        this.data = data;
        this.context = context;
        modifyData(data);
    }

    public void modifyData(List<String> data) {
        this.data = data;
        for(String s: data) {
            Store.getUser(s, (f, g) -> {
                if(f)
                    notifyDataSetChanged();
            });
        }
    }

    @Override
    public FriendViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater layoutInflater = LayoutInflater.from(parent.getContext());
        View view = layoutInflater.inflate(R.layout.friend_list_item, parent, false);
        return new FriendViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull final FriendViewHolder holder, final int position) {
        final String user = data.get(position);
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                User u = Store.getInStateUser(user);
                if(u != null) {
                    u.setCurrent();
                    Intent intent = new Intent(context, EditFriend.class);
                    context.startActivity(intent);
                }
            }
        });
        holder.setContent(user);
    }

    @Override
    public int getItemCount() {
        return data.size();
    }
}
