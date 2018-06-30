package seacoalCo.bill_it.groups;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import seacoalCo.bill_it.MainDrawerActivity;
import seacoalCo.bill_it.R;
import seacoalCo.bill_it.logics.group.Group;
import seacoalCo.bill_it.logics.user.User;
import seacoalCo.bill_it.utility_classes.IdGenerator;

public class AddGroup extends AppCompatActivity {
    private TextView txtName;
    private Button confirmButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_group);

        txtName = findViewById(R.id.addGroupName);
        confirmButton = findViewById(R.id.addGroupConfirm);

        confirmButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Group g = new Group(txtName.getText().toString(), User.getLoggedInUser().getId(), IdGenerator.getNewId());
                User.getLoggedInUser().addGroup(
                        g.getId()
                );
                MainDrawerActivity.currentMainActivity.addGroup(g);
                finish();
            }
        });
    }
}
