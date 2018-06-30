package seacoalCo.bill_it.groups;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import seacoalCo.bill_it.R;
import seacoalCo.bill_it.logics.Store;
import seacoalCo.bill_it.logics.group.Group;

public class EditGroup extends AppCompatActivity {

    private Group group;
    private int position;

    private EditText txtName;
    private Button confirmButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_group);

        group = (Group) getIntent().getSerializableExtra("group");
        position = getIntent().getIntExtra("position", 0);

        txtName = findViewById(R.id.addGroupName);


        confirmButton = findViewById(R.id.confirmGroupEdit);

        txtName.setText(group.getName());

        confirmButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String name = txtName.getText().toString();

                if(name.length() > 1) {
                    group.setName(
                        name
                    );
                }
                //hub.createGroup(txtName.getText().toString());
                Store.save(group);
                finish();
            }
        });
    }
}
