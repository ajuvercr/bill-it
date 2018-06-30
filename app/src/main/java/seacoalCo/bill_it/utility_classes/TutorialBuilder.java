package seacoalCo.bill_it.utility_classes;

import android.app.AlertDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.widget.CheckBox;
import android.widget.LinearLayout;
import android.widget.TextView;

import seacoalCo.bill_it.R;

public abstract class TutorialBuilder {

    public static AlertDialog buildTutorial(final int prefId, String info, final Context context) {

        final SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);

        AlertDialog.Builder builder = new AlertDialog.Builder(context);

        builder.setTitle("Tutorial");

        LinearLayout container = new LinearLayout(context);
        container.setOrientation(LinearLayout.VERTICAL);

        TextView message = new TextView(context);
        message.setText(info);
        message.setPadding(25, 10, 0, 0);

        container.addView(message);

        LinearLayout checkContainer = new LinearLayout(context);
        checkContainer.setOrientation(LinearLayout.HORIZONTAL);

        final CheckBox checkBox = new CheckBox(context);
        checkBox.setText(context.getString(R.string.no_reminder));
        checkBox.setChecked(true);

        checkContainer.addView(checkBox);
        checkContainer.setPadding(10, 40, 0, 0);

        container.addView(checkContainer);

        container.setPadding(25, 0, 0, 0);

        builder.setView(container);

        builder.setNeutralButton("OK", (dialog, which) -> {
            if (checkBox.isChecked()) {
                SharedPreferences.Editor editor = sharedPreferences.edit();
                editor.putBoolean(context.getString(prefId), false);
                editor.apply();
            }
            dialog.dismiss();
        });

        return builder.create();
    }
}
