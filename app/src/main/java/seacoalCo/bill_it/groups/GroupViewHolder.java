package seacoalCo.bill_it.groups;

import android.content.Intent;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;

import java.util.stream.Collectors;

import seacoalCo.bill_it.R;
import seacoalCo.bill_it.camera.google.OcrCaptureActivity;
import seacoalCo.bill_it.logics.Store;
import seacoalCo.bill_it.logics.group.Group;
import seacoalCo.bill_it.logics.user.User;

public class GroupViewHolder extends RecyclerView.ViewHolder {
    private TextView txtName;
    private TextView txtMembers;
    private ImageButton cameraButton;

    public GroupViewHolder(View itemView) {
        super(itemView);
        txtName = itemView.findViewById(R.id.groupName);
        txtMembers = itemView.findViewById(R.id.groupMembers);
        cameraButton = itemView.findViewById(R.id.cameraButton);
    }

    public void setContent(final String g) {
        Group group = Store.getInStateGroup(g);
        if(group != null) {
            txtName.setText(group.getName());
            int maxLength = 25;
            String membersString = group.getUserIds().stream().map((ui) -> {
                User u = Store.getInStateUser(ui);
                if(u == null) {
                    return "";
                }else {
                    return u.getName();
                }
            }).collect(Collectors.joining(", "));
            if (membersString.length() > maxLength) {
                membersString = membersString.substring(0, maxLength) + "...";
            }
            txtMembers.setText(membersString);
        }else{
            txtName.setText("Loading");
            txtMembers.setText("");
        }

        cameraButton.setOnClickListener(v -> {
            if (group != null) {
                group.setCurrent();
                Intent intent = new Intent(itemView.getContext(), OcrCaptureActivity.class);
                itemView.getContext().startActivity(intent);
            }
        });
    }
}
