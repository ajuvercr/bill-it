package seacoalCo.bill_it;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.FirebaseAuth;

import seacoalCo.bill_it.logics.Store;
import seacoalCo.bill_it.logics.user.User;
import seacoalCo.bill_it.utility_classes.TutorialBuilder;

public class LoginActivity extends AppCompatActivity {

    private FirebaseAuth auth;
    private boolean backToButtons = false;
    private boolean working = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.d("create", "created");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        FirebaseApp.initializeApp(this);
        auth = FirebaseAuth.getInstance();

        // initialize settings
        Store.init(
                getApplication()
        );

        PreferenceManager.setDefaultValues(this, R.xml.preferences, false);
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);

        if (sharedPreferences.getString(getString(R.string.user_id), " ").equals(" ")) {
            loadButtons();

            if (sharedPreferences.getBoolean(getString(R.string.WELCOMETUTORIAL), true) && savedInstanceState == null) {
                android.app.AlertDialog alertDialog = TutorialBuilder.buildTutorial(R.string.WELCOMETUTORIAL, getString(R.string.welcome_message), this);
                alertDialog.show();
            }
        }
        else {
            if(auth.getCurrentUser() == null) {
                Log.d("USSR", "Not logged in");
                auth.signInWithEmailAndPassword(sharedPreferences.getString(getString(R.string.email), " "),
                        sharedPreferences.getString(getString(R.string.password), " "))
                        .addOnSuccessListener((e) -> loggedIn()).addOnFailureListener((e) -> loadButtons())
                        .addOnCanceledListener(() -> loadButtons());
            }else{
                loggedIn();
            }
        }
    }

    public void loadButtons() {
        SignOrLogFragment fragment = new SignOrLogFragment();

        getSupportFragmentManager().beginTransaction()
                .replace(R.id.settingContainer, fragment)
                .commit();
    }

    public void loadSign() {
        backToButtons = true;
        SignUpFragment fragment = new SignUpFragment();

        getSupportFragmentManager().beginTransaction()
                .replace(R.id.settingContainer, fragment)
                .commit();
    }

    public void loadLog() {
        backToButtons = true;
        LoginFragment fragment = new LoginFragment();

        getSupportFragmentManager().beginTransaction()
                .replace(R.id.settingContainer, fragment)
                .commit();
    }

    public void loggedIn() {
        //As long as the e-mail address hasn't been confirmed
        // the user won't be able to login
        if(!auth.getCurrentUser().isEmailVerified()){
            loadLog();
        }else{
            // blocking to get current user
            SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
            String id = sharedPreferences.getString(getString(R.string.user_id), "");
            Log.d("USSR", "Log in id: "+id);
            Store.getUser(id, new Starter(
                    sharedPreferences.getString(LoginActivity.this.getString(R.string.email), ""),
                    sharedPreferences.getString(LoginActivity.this.getString(R.string.user_name), ""),
                    id
            ));
        }

    }

    private class Starter implements Store.Callback<User> {
        private String email, name, id;
        private boolean started = false;
        public Starter(String email, String name, String id) {
            this.email = email;
            this.name = name;
            this.id = id;
        }

        @Override
        public void call(boolean succes, User item) {
            if (succes) {
                User.setLoggedInUser(item);
            } else {
                if(User.getLoggedInUser() == null)
                    User.setLoggedInUser(
                            new User(
                                    name,
                                    email,
                                    id)
                    );
            }

            String mail = auth.getCurrentUser().getEmail();
            if (!mail.equals(User.getLoggedInUser().getEmail())) {
                User user = User.getLoggedInUser();
                user.setEmail(mail);
                Store.save(user);
            }

            if (!started) {
                started = true;
                Intent intent = new Intent(LoginActivity.this, MainDrawerActivity.class);
                LoginActivity.this.startActivity(intent);
                LoginActivity.this.finish();
            }
            if(MainDrawerActivity.currentMainActivity != null) {
                MainDrawerActivity.currentMainActivity.update();
            }

        }
    }

    @Override
    public void onBackPressed() {
        if (!working) {
            if (backToButtons) {
                loadButtons();
            } else finishAffinity();
        }
    }

    public boolean checkConnection() {
        ConnectivityManager connectivityManager
                = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        if (activeNetworkInfo == null || !activeNetworkInfo.isConnected()) {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle("Whoops");
            builder.setMessage("You're going to need internet access to log in");
            builder.setNeutralButton("Oh  :(", (dialog, which) -> dialog.dismiss());
            builder.create().show();
            return false;
        }
        else return true;
    }

    public void setWorking(boolean working) {
        this.working = working;
    }
}
