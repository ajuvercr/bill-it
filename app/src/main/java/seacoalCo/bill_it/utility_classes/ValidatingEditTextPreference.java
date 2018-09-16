package seacoalCo.bill_it.utility_classes;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.preference.EditTextPreference;
import android.util.AttributeSet;
import android.view.View;

public abstract class ValidatingEditTextPreference extends EditTextPreference {

    ValidatingEditTextPreference(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    ValidatingEditTextPreference(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    ValidatingEditTextPreference(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    ValidatingEditTextPreference(Context context) {
        super(context);
    }

    @Override
    protected void showDialog(Bundle state) {
        super.showDialog(state);
        AlertDialog dlg = (AlertDialog)getDialog();
        View positiveButton = dlg.getButton(DialogInterface.BUTTON_POSITIVE);
        getEditText().setError(null);
        positiveButton.setOnClickListener(this::onPositiveButtonClicked);
    }

    private void onPositiveButtonClicked(View v) {
        String value = getEditText().getText().toString();
        String errorMessage = onValidate(value);
        if (errorMessage == null) {
            getEditText().setError(null);
            onClick(getDialog(),DialogInterface.BUTTON_POSITIVE);
            onConfirm(value);
            getDialog().dismiss();
        }
        else {
            getEditText().setError(errorMessage);
        }
    }
    protected abstract String onValidate(String text);
    protected abstract void onConfirm(String value);
}