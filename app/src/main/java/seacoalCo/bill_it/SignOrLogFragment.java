package seacoalCo.bill_it;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

public class SignOrLogFragment extends Fragment {

    public SignOrLogFragment() {
        // Required empty public constructor
    }

    public static SignOrLogFragment newInstance() {
        return new SignOrLogFragment();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View v = inflater.inflate(R.layout.fragment_sign_or_log, container, false);

        Button log_button = v.findViewById(R.id.log_button);
        Button sign_button = v.findViewById(R.id.sign_button);

        log_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v1) {
                LoginActivity parent = (LoginActivity) SignOrLogFragment.this.getActivity();
                if (parent.checkConnection()) {
                    parent.loadLog();
                }
            }
        });

        sign_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v12) {
                LoginActivity parent = (LoginActivity) SignOrLogFragment.this.getActivity();
                if (parent.checkConnection()) {
                    parent.loadSign();
                }
            }
        });
        return v;
    }


}
