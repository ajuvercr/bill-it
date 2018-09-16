package seacoalCo.bill_it.friends;

import android.app.AlertDialog;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import seacoalCo.bill_it.R;
import seacoalCo.bill_it.logics.Store;
import seacoalCo.bill_it.logics.user.User;
import seacoalCo.bill_it.utility_classes.IdGenerator;

public class AddFriend extends AppCompatActivity {

    private TextView txtName;
    private TextView txtEmail;
    private EditText idField;

    private SharedPreferences preferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_friend);
        //getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        txtName = findViewById(R.id.addFriendName);
        txtEmail = findViewById(R.id.addFriendEmail);
        idField = findViewById(R.id.idField);
        Button confirmButton = findViewById(R.id.addFriendConfirm);

        confirmButton.setOnClickListener((v) -> {
                User.getLoggedInUser().addFriend(
                        new User(txtName.getText().toString(), txtEmail.getText().toString(), IdGenerator.getNewId()).getId()
                );
                finish();
            }
        );

        preferences = PreferenceManager.getDefaultSharedPreferences(this);

        TextView idView = findViewById(R.id.idView);
        idView.setText(preferences.getString(getString(R.string.user_id), ""));
    }

    public void onDifference(View v) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(getString(R.string.friends_explanation));
        builder.setNeutralButton("I see", (dialog, which) -> dialog.dismiss());
        builder.create().show();
    }

    public void onClip(View v) {
        ClipboardManager clip = (ClipboardManager) getSystemService(CLIPBOARD_SERVICE);
        clip.setPrimaryClip(ClipData.newPlainText("User id", preferences.getString(getString(R.string.user_id), "")));
        Toast toast = Toast.makeText(this, "Copied to clipboard", Toast.LENGTH_SHORT);
        toast.show();
    }

    public void onOnline(View v) {
        ConnectivityManager connectivityManager
                = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        if (activeNetworkInfo == null || !activeNetworkInfo.isConnected()) {
            android.support.v7.app.AlertDialog.Builder builder = new android.support.v7.app.AlertDialog.Builder(this);
            builder.setTitle("Whoops");
            builder.setMessage("You're going to need internet access to log in");
            builder.setNeutralButton("Oh  :(", (dialog, which) -> dialog.dismiss());
            builder.create().show();
        }
        else {
            String theirDoc = idField.getText().toString();

            Store.getUser(theirDoc, (s, t) -> {
                if(s) {
                    User t2 = User.getLoggedInUser();
                    t.addFriend(t2.getId());
                    t2.addFriend(t.getId());
                    Store.save(t);
                    Store.save(t2);

                    Toast toast = Toast.makeText(getApplicationContext(), "Friend added", Toast.LENGTH_SHORT);
                    toast.show();
                }else{
                    Toast toast = Toast.makeText(getApplicationContext(), "This user doesn't seem to exist", Toast.LENGTH_SHORT);
                    toast.show();
                }
            });

        }
    }
}
