package seacoalCo.bill_it.utility_classes;

import android.content.Context;
import android.util.AttributeSet;

import seacoalCo.bill_it.logics.Store;
import seacoalCo.bill_it.logics.user.User;

public  class ValidatingNamePreference extends ValidatingEditTextPreference {

    public ValidatingNamePreference(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    public ValidatingNamePreference(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public ValidatingNamePreference(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public ValidatingNamePreference(Context context) {
        super(context);
    }

    @Override
    protected String onValidate(String text) {
        return FieldValidator.validateName(text, getContext());
    }

    @Override
    protected void onConfirm(String value) {
        User user = User.getLoggedInUser();
        user.setName(value);
        Store.save(user);
    }
}