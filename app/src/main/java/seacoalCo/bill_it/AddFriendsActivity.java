package seacoalCo.bill_it;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;

import java.util.ArrayList;

import seacoalCo.bill_it.groups.AddUserToGroupAdapter;
import seacoalCo.bill_it.logics.Store;
import seacoalCo.bill_it.logics.group.Group;
import seacoalCo.bill_it.logics.user.User;

public class AddFriendsActivity extends AppCompatActivity {
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.add_friends_activity);

        RecyclerView usersRv = findViewById(R.id.groupsView);
        final RecyclerView.Adapter usersA = new AddUserToGroupAdapter(
                new ArrayList<>(
                        User.getLoggedInUser().getFriends()
                ), getApplicationContext()
        );

        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(this);
        usersRv.setLayoutManager(layoutManager);
        usersRv.setAdapter(usersA);

        RecyclerView.AdapterDataObserver emptyObserver = new RecyclerView.AdapterDataObserver() {
            @Override
            public void onChanged() {
                // Not called
                if (usersA.getItemCount() == 0) {
                    finish();
                }
            }
        };

        usersA.registerAdapterDataObserver(emptyObserver);
    }

    private void stop() {
        setResult(RESULT_OK);
        Store.save(Group.getCurrentGroup());
        finish();
    }

    public void onBackPressed() {
        super.onBackPressed();
        stop();
    }

    public void stop(View view) {
        stop();
    }
}
