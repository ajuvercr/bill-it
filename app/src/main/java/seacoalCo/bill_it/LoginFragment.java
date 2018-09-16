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

import seacoalCo.bill_it.logics.Store;

public class LoginFragment extends Fragment {

    EditText mailField;
    EditText passField;
    Button logButton;

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

        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getContext());
        SharedPreferences.Editor editor = preferences.edit();
        editor.putString(getString(R.string.user_name), mail);
        editor.putString(getString(R.string.email), mailField.getText().toString());
        editor.putString(getContext().getString(R.string.user_id), Store.randomAlphaNumeric(5));
        editor.putString(getString(R.string.password), pass);
        editor.apply();

        loginActivity.loggedIn();
    }
}
