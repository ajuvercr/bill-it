package seacoalCo.bill_it;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.firestore.FirebaseFirestore;

import seacoalCo.bill_it.logics.user.User;
import seacoalCo.bill_it.utility_classes.FieldValidator;

public class SignUpFragment extends Fragment {

    EditText nameField;
    EditText mailField;
    EditText passField;
    EditText confPassField;
    Button logButton;
    FirebaseFirestore db = FirebaseFirestore.getInstance();
    FirebaseAuth auth = FirebaseAuth.getInstance();

    public SignUpFragment() {
        // Required empty public constructor
    }
    public static SignUpFragment newInstance() {
        return new SignUpFragment();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View v = inflater.inflate(R.layout.fragment_sign_up, container, false);

        nameField = v.findViewById(R.id.nameField);
        mailField = v.findViewById(R.id.mailField);
        passField = v.findViewById(R.id.passField);
        confPassField = v.findViewById(R.id.conf_password);
        logButton = v.findViewById(R.id.sign_up_button);

        logButton.setOnClickListener(v1 -> signUp());

        // Inflate the layout for this fragment
        return v;
    }

    public void signUp() {
        String name = nameField.getText().toString();
        String email = mailField.getText().toString();
        String pass = passField.getText().toString();
        String passConf = confPassField.getText().toString();

        boolean noErrors = true;

        String error = FieldValidator.validateName(name, getContext());
        if (error != null) {
            nameField.setError(error);
            noErrors = false;
        }

        error = FieldValidator.validateMail(email, getContext());
        if (error != null) {
            noErrors = false;
            mailField.setError(error);
        }

        error = FieldValidator.validatePass(pass, getContext());
        if (error != null) {
            noErrors = false;
            passField.setError(error);
        }

        if (!passConf.equals(pass)) {
            noErrors = false;
            confPassField.setError(getString(R.string.pass_no_match));
        }

        if (noErrors) {

            auth.createUserWithEmailAndPassword(email, pass)
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information
                            FirebaseUser fUser = auth.getCurrentUser();

                            UserProfileChangeRequest changeRequest = new UserProfileChangeRequest.Builder()
                                    .setDisplayName(name)
                                    .build();
                            fUser.updateProfile(changeRequest);

                            SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getContext());
                            SharedPreferences.Editor editor = preferences.edit();
                            editor.putString(getString(R.string.user_name), nameField.getText().toString());
                            editor.putString(getString(R.string.email), mailField.getText().toString());
                            editor.putString(getString(R.string.password), pass);

                            User user = new User(nameField.getText().toString(), mailField.getText().toString(), fUser.getUid());
                            db.collection("users").document(fUser.getUid()).set(user)
                                    .addOnSuccessListener(aVoid -> {
                                        editor.putString(getContext().getString(R.string.user_id), fUser.getUid());


                                        editor.apply();
                                        fUser.sendEmailVerification();
                                        ((LoginActivity) getActivity()).loggedIn();
                                    });

                        }
                        else {
                            // If sign in fails, display a message to the user.
                            Toast.makeText(getContext(), "E-mail already registered",
                                    Toast.LENGTH_SHORT).show();
                        }
                    });
        }
    }
}
