package seacoalCo.bill_it;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

public class SettingsActivity extends AppCompatActivity {

    SettingsFragment fragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        fragment = new SettingsFragment();

        getFragmentManager().beginTransaction()
                .replace(R.id.root, fragment)
                .commit();
    }
}
