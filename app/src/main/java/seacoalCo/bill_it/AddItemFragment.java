package seacoalCo.bill_it;

import android.app.Dialog;
import android.support.v4.app.DialogFragment;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;

import seacoalCo.bill_it.logics.group.Group;
import seacoalCo.bill_it.logics.item.Item;

/**
 * Created by silvius_seacoal on 12.05.18.
 */

public class AddItemFragment extends DialogFragment {
    private Runnable onEnded = () -> {};
    public void setOnEnded(Runnable onEnded) {
        this.onEnded = onEnded;
    }

    public void onDismiss(DialogInterface di) {
        onEnded.run();
    }

    public Dialog onCreateDialog(Bundle savedInstanceState) {
        // Use the Builder class for convenient dialog construction
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        LayoutInflater inflater = getActivity().getLayoutInflater();

        View layout = inflater.inflate(R.layout.add_item_layout, null);

        builder.setView(layout)
                .setPositiveButton("Add", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        int price = (int) (Float.parseFloat(((EditText)layout.findViewById(R.id.price)).getText().toString())*100);
                        Group g = Group.getCurrentGroup();
                        for(int i = 0; i < Integer.parseInt(((EditText) layout.findViewById(R.id.quantity)).getText().toString()); i++) {
                            g.addItem(
                                    new Item(
                                            ((EditText)layout.findViewById(R.id.item_name)).getText().toString(),
                                            price
                                    )
                            );
                        }
                        getDialog().dismiss();
                    }
                })
                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        getDialog().dismiss();
                    }
                })
                .setTitle("Add Items");
        // Create the AlertDialog object and return it
        return builder.create();
    }
}
