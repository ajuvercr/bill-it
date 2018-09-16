package seacoalCo.bill_it.utility_classes;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.AttributeSet;

import seacoalCo.bill_it.LoginActivity;
import seacoalCo.bill_it.R;
import seacoalCo.bill_it.logics.Store;
import seacoalCo.bill_it.logics.user.User;

public class ValidatingMailPreference extends ValidatingEditTextPreference{

    public ValidatingMailPreference(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    public ValidatingMailPreference(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public ValidatingMailPreference(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public ValidatingMailPreference(Context context) {
        super(context);
    }

    @Override
    protected String onValidate(String text) {
        return FieldValidator.validateMail(text, getContext());
    }

    @Override
    protected void onConfirm(String value) {

        User user = User.getLoggedInUser();
        user.setEmail(value);
        Store.save(user);

        Context c = getContext();
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(c);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putString(c.getString(R.string.user_name), " ");
        editor.putString(c.getString(R.string.email), " ");
        editor.putString(c.getString(R.string.user_id), " ");
        editor.apply();
        Intent loginIntent = new Intent(c, LoginActivity.class);
        c.startActivity(loginIntent);
    }
}