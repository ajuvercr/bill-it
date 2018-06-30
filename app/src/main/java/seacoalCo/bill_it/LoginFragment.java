package seacoalCo.bill_it;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class LoginFragment extends Fragment {

    EditText mailField;
    EditText passField;
    Button logButton;

    FirebaseAuth auth = FirebaseAuth.getInstance();

    public LoginFragment() {
        // Required empty public constructor
    }

    public static LoginFragment newInstance() {
        return new LoginFragment();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        //Message shows up as long as the user hasn't confirmed his e-mail address
        if(auth.getCurrentUser() != null && !auth.getCurrentUser().isEmailVerified()){
            Toast.makeText(getContext(),"Check your e-mail address.\n" +
                    "You will have received an email to confirm it",Toast.LENGTH_LONG).show();
        }
        View v = inflater.inflate(R.layout.fragment_login, container, false);
        mailField = v.findViewById(R.id.log_mail_field);
        passField = v.findViewById(R.id.log_pass_field);
        logButton = v.findViewById(R.id.log_in_button);

        logButton.setOnClickListener(v1 -> logIn());

        // Inflate the layout for this fragment
        return v;
    }

    private void logIn() {
        LoginActivity loginActivity = (LoginActivity) getActivity();
        loginActivity.setWorking(true);

        String mail = mailField.getText().toString();
        String pass = passField.getText().toString();

        auth.signInWithEmailAndPassword(mail, pass).addOnSuccessListener(authResult -> {
            FirebaseUser user = authResult.getUser();

            SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getContext());
            SharedPreferences.Editor editor = preferences.edit();
            editor.putString(getString(R.string.user_name), user.getDisplayName());
            editor.putString(getString(R.string.email), mailField.getText().toString());
            editor.putString(getContext().getString(R.string.user_id), user.getUid());
            editor.putString(getString(R.string.password), pass);
            editor.apply();

            loginActivity.loggedIn();
        })
        .addOnFailureListener(e -> {
            Toast toast = Toast.makeText(getContext(), "E-mail or password incorrect", Toast.LENGTH_SHORT);
            toast.show();
            loginActivity.setWorking(false);
        });
    }
}
