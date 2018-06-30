package seacoalCo.bill_it;

import android.app.AlertDialog;
import android.content.Context;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v4.app.NavUtils;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.text.InputType;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.yarolegovich.discretescrollview.DiscreteScrollView;

import java.util.ArrayList;

import seacoalCo.bill_it.item_divider.ItemAdapter;
import seacoalCo.bill_it.item_divider.UserAdapter;
import seacoalCo.bill_it.logics.Store;
import seacoalCo.bill_it.logics.group.Group;
import seacoalCo.bill_it.logics.item.Item;
import seacoalCo.bill_it.logics.user.User;
import seacoalCo.bill_it.utility_classes.TutorialBuilder;

/**
 * Created by honneur on 20/04/18.
 */

public class SplitActivity extends AppCompatActivity {
    private ArrayList<Item> state;
    private ArrayList<String> bs, cs;
    private Item prevItem;
    private int prefPos;


    private RecyclerView buyerRV, consumerRV, itemRV;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_split);

        Toolbar toolbar = findViewById(R.id.done_toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        state = new ArrayList<>(Group.getCurrentGroup().getItems());

        DiscreteScrollView buyerRV = findViewById(R.id.buyer_rv);
        buyerRV.setSlideOnFling(true);

        DiscreteScrollView consumerRV = findViewById(R.id.consumer_rv);
        consumerRV.setSlideOnFling(true);

        DiscreteScrollView itemRV = findViewById(R.id.item_rv);
        itemRV.setSlideOnFling(true);

        // recyclerview inladen

        Group g = Group.getCurrentGroup();
        bs = new ArrayList<>();
        cs = new ArrayList<>();

        if(g != null) {
            bs.addAll(g.getUserIds());
            bs.add(User.SPLITALL.getId());
            cs.addAll(g.getUserIds());
            cs.add(User.SPLITALL.getId());
        }else{
            Log.d("Items", "No current Group");
        }

        final ItemAdapter itemA = new ItemAdapter(state);
        itemA.setOnItemClickListener((position, v) -> {
            Item item = state.get(position);
            buildDialog(item, itemA, position);
        });
        final RecyclerView.Adapter buyerA = new UserAdapter(bs);
        final RecyclerView.Adapter consumerA = new UserAdapter(cs);
        
        ItemTouchHelper swipeDeleteHelper = new ItemTouchHelper(new ItemTouchHelper.SimpleCallback(0,
                ItemTouchHelper.UP|ItemTouchHelper.DOWN) {

            @Override
            public int getSwipeDirs(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder) {
                return itemRV.getCurrentItem()==viewHolder.getAdapterPosition() ?
                        super.getSwipeDirs(recyclerView, viewHolder) : ItemTouchHelper.DOWN;
            }

            @Override
            public boolean onMove(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, RecyclerView.ViewHolder target) {
                return false;
            }

            // Behaviour definiëren.
            @Override
            public void onSwiped(RecyclerView.ViewHolder viewHolder, int direction) {
                Group g = Group.getCurrentGroup();
                if(g != null) {
                    if (direction == ItemTouchHelper.UP) {
                        int iPos = itemRV.getCurrentItem();
                        g.handle(
                                bs.get(buyerRV.getCurrentItem()),
                                cs.get(consumerRV.getCurrentItem()),
                                state.get(iPos)
                        );

                        prevItem = state.remove(iPos);
                        prefPos = iPos;
                        itemRV.removeViewAt(iPos);
                        itemA.notifyItemRemoved(iPos);
                        itemA.notifyItemRangeChanged(iPos, state.size());
                        itemA.notifyDataSetChanged();

                        consumerA.notifyDataSetChanged();
                        buyerA.notifyDataSetChanged();

                        if (state.isEmpty()) {
                            next();
                        }
                        Snackbar.make(findViewById(R.id.snackContainer), "Item split", Snackbar.LENGTH_INDEFINITE)
                                .setAction("UNDO", v -> {
                                    g.unHandle(prevItem, prefPos);
                                    state.add(prevItem);
                                    prevItem = null;
                                    itemA.notifyItemInserted(prefPos);
                                    consumerA.notifyDataSetChanged();
                                    buyerA.notifyDataSetChanged();
                                }).show();
                    } else if (direction == ItemTouchHelper.DOWN) {
                        int iPos = viewHolder.getAdapterPosition();
                        g.removeItem(state.get(iPos));
                        prevItem = state.remove(iPos);
                        itemRV.removeViewAt(iPos);
                        itemA.notifyItemRemoved(iPos);
                        itemA.notifyItemRangeChanged(iPos, state.size());
                        itemA.notifyDataSetChanged();
                        Snackbar.make(findViewById(R.id.snackContainer), "Item removed", Snackbar.LENGTH_INDEFINITE)
                                .setAction("UNDO", v -> {
                                    state.add(prevItem);
                                    prevItem = null;
                                    itemA.notifyItemInserted(prefPos);
                                }).show();
                    }
                }
            }
        });
        swipeDeleteHelper.attachToRecyclerView(itemRV);

        itemRV.setAdapter(itemA);
        buyerRV.setAdapter(buyerA);
        consumerRV.setAdapter(consumerA);

        Toast toast = Toast.makeText(this, R.string.swipe_instructions, Toast.LENGTH_LONG);
        toast.show();

        if (PreferenceManager.getDefaultSharedPreferences(getApplicationContext())
                .getBoolean(getString(R.string.split_tutorial), true) && savedInstanceState == null) {
            AlertDialog alertDialog = TutorialBuilder.buildTutorial(R.string.split_tutorial, getString(R.string.split_tutorial_message), this);
            alertDialog.show();
        }
    }

    public void onSaveInstanceState(Bundle b) {
        super.onSaveInstanceState(b);

        b.putSerializable("items", state);
        b.putSerializable("buyers", bs);
        b.putSerializable("consumers", cs);
    }

    public void onRestoreInstanceState(@Nullable  Bundle bundle) {
        super.onRestoreInstanceState(bundle);

        if(bundle == null) {
            return;
        }

        state = new ArrayList<>(Group.getCurrentGroup().getItems());
        bs = (ArrayList<String>) bundle.getSerializable("buyers");
        cs = (ArrayList<String>) bundle.getSerializable("consumers");
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.actionbar_done, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_menu_done:
                next();
                return true;
            case R.id.home:
                NavUtils.navigateUpFromSameTask(this);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void next() {
        Store.save(Group.getCurrentGroup());
        finish();
    }

    private void buildDialog(Item item, ItemAdapter itemA, int position) {
        Context c = this;

        AlertDialog.Builder builder = new AlertDialog.Builder(c);
        builder.setTitle(R.string.edit_item);

        LinearLayout container = new LinearLayout(c);
        container.setOrientation(LinearLayout.VERTICAL);

        TextView nameLable = new TextView(c);
        nameLable.setText(getString(R.string.description));
        container.addView(nameLable);

        EditText nameField = new EditText(c);
        nameField.setText(item.getName());
        container.addView(nameField);
        nameField.setHint("Secret message");

        TextView priceLabel = new TextView(c);
        priceLabel.setText(getString(R.string.unit_price));
        container.addView(priceLabel);

        EditText priceField = new EditText(c);
        priceField.setText(String.valueOf(item.getPrice()));
        priceField.setInputType(InputType.TYPE_NUMBER_FLAG_DECIMAL);
        priceField.setHint("May contain traces of panda");
        container.addView(priceField);

        container.setPadding(50, 0, 0, 0);

        builder.setView(container);
        builder.setPositiveButton("OK", (dialog, which) -> {
            if (priceField.getText().toString().matches("[0-9]+.?[0-9]*")) {
                item.setName(nameField.getText().toString());
                item.setPrice(Integer.parseInt(priceField.getText().toString()));
                itemA.notifyItemChanged(position);
                itemA.notifyDataSetChanged();
                dialog.dismiss();
            }
            else {
                priceField.setError(getString(R.string.price_invalid));
            }
        });
        builder.setNegativeButton(getString(R.string.cancel), (dialog, which) -> dialog.dismiss());
        builder.create().show();
    }
}
