package seacoalCo.bill_it.utility_classes;

import android.content.Context;
import android.util.Patterns;

import seacoalCo.bill_it.R;

public abstract class FieldValidator {

    public static String validateName(String name, Context c) {
        name = name.trim();

        if (name.length() < 2) {
            return  c.getString(R.string.name_length_error);
        }

        else if (! name.matches("([A-Za-z]+[ -']?)*")) {
            return c.getString(R.string.name_invalid);
        }
        else return null;
    }

    public static String validateMail(String email, Context c) {
        email = email.trim();

        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            return c.getString(R.string.email_invalid);
        }
        else return null;
    }

    public static String validatePass(String pass, Context c) {
        if (pass.length() < 8) {
            return c.getString(R.string.pass_length);
        }
        else return null;
    }
}
