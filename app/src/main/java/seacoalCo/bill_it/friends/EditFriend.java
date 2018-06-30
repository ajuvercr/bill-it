package seacoalCo.bill_it.friends;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import seacoalCo.bill_it.R;
import seacoalCo.bill_it.logics.Store;
import seacoalCo.bill_it.logics.user.User;

public class EditFriend extends AppCompatActivity {
    private TextView txtName;
    private TextView txtEmail;
    private Button confirmButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        User u = User.getCurrentUser();
        setContentView(R.layout.activity_edit_friend);
        //getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        txtName = findViewById(R.id.editName);
        txtEmail = findViewById(R.id.editEmail);
        confirmButton = findViewById(R.id.confirmEdit);

        txtName.setText(u.getName());
        txtEmail.setText(u.getEmail());

        confirmButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // TODO logics support for user edit
                close();
            }
        });

    }

    private void close() {
        User u = User.getCurrentUser();
        u.setEmail(txtEmail.getText().toString());
        u.setName(txtName.getText().toString());
        Store.save(u);
        finish();
    }

    public void onBackPressed() {
        close();
    }
}
