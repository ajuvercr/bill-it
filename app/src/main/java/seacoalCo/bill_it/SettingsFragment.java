package seacoalCo.bill_it;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;

import seacoalCo.bill_it.utility_classes.ValidatingEditTextPreference;
import seacoalCo.bill_it.utility_classes.ValidatingMailPreference;
import seacoalCo.bill_it.utility_classes.ValidatingNamePreference;

public class SettingsFragment extends PreferenceFragment {

    SharedPreferences preferences;
    ValidatingEditTextPreference nameField;
    ValidatingEditTextPreference mailField;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        addPreferencesFromResource(R.xml.preferences);

        preferences = PreferenceManager.getDefaultSharedPreferences(getActivity());

        nameField = (ValidatingNamePreference) findPreference(getString(R.string.user_name));
        mailField = (ValidatingMailPreference) findPreference(getString(R.string.email));
    }
}
