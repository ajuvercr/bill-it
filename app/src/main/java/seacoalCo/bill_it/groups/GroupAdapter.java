package seacoalCo.bill_it.groups;

import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.List;

import seacoalCo.bill_it.OverViewActivity;
import seacoalCo.bill_it.R;
import seacoalCo.bill_it.logics.Store;
import seacoalCo.bill_it.logics.group.Group;

public class GroupAdapter extends RecyclerView.Adapter<GroupViewHolder>  {
    private List<String> data;
    private Context context;

    public GroupAdapter(List<String> data, Context context) {
        this.context = context;
        modifyData(data);
    }

    public void modifyData(List<String> data) {
        this.data = data;

        for(String s: data) {
            Store.getGroup(s, (f, g) -> {
                if(f)
                    notifyDataSetChanged();
            });
        }
    }


    @NonNull
    @Override
    public GroupViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater layoutInflater = LayoutInflater.from(parent.getContext());
        View view = layoutInflater.inflate(R.layout.group_list_item, parent, false);
        GroupViewHolder holder = new GroupViewHolder(view);
        return holder;
    }

    @Override
    public void onBindViewHolder(@NonNull GroupViewHolder holder, final int position) {
        final String group = data.get(position);
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Group g = Store.getInStateGroup(group);
                if(g != null) {
                    g.setCurrent();
                    Intent intent = new Intent(context, OverViewActivity.class);
                    context.startActivity(intent);
                }
            }
        });
        holder.setContent(group);
    }

    @Override
    public int getItemCount() {
        return data.size();
    }
}
