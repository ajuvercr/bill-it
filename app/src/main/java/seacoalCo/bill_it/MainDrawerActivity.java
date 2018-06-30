package seacoalCo.bill_it;

import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.design.widget.NavigationView;
import android.support.v4.app.Fragment;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;

import com.google.firebase.auth.FirebaseAuth;

import java.util.HashSet;
import java.util.Set;

import seacoalCo.bill_it.friends.AddFriend;
import seacoalCo.bill_it.friends.Friends;
import seacoalCo.bill_it.groups.AddGroup;
import seacoalCo.bill_it.groups.Groups;
import seacoalCo.bill_it.logics.Store;
import seacoalCo.bill_it.logics.group.Group;
import seacoalCo.bill_it.logics.user.User;

public class MainDrawerActivity extends AppCompatActivity implements Friends.OnFragmentInteractionListener, Groups.OnFragmentInteractionListener, OverViewActivity.OnFragmentInteractionListener {
    public static MainDrawerActivity currentMainActivity;

    private SharedPreferences sharedPreferences;
    private DrawerLayout mDrawerLayout;
    private Set<String> groupIds = new HashSet<>();
    private Menu menu;
    private Fragment f;

    private void swapFragment(Fragment f) {
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.content_frame, f).commitAllowingStateLoss();
        this.f = f;
    }

    @Override
    public void onFragmentInteraction(Uri uri) {

    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        outState.putString("WORKAROUND_FOR_BUG_19917_KEY", "WORKAROUND_FOR_BUG_19917_VALUE");
        super.onSaveInstanceState(outState);
    }

    @Override
    public void onStop() {
        super.onStop();
        Store.saveAll();
    }

    @Override
    public void onResume() {
        super.onResume();
        NavigationView navView = findViewById(R.id.nav_view);
        navView.getMenu().performIdentifierAction(R.id.nav_groups, 0);
    }

    public void updateGroups() {
        User.getLoggedInUser().getGroups().forEach((gs) -> {
            Store.getGroup(gs, (t, g) -> {
                if(t && !groupIds.contains(g.getId())) {
                    menu.add(R.id.groupss, gs.hashCode(), 15, g.getName());
                    groupIds.add(g.getId());
                }
            });
        });
    }

    public void addGroup(Group g) {
        clearChecked();
        menu.add(R.id.groupss, g.getId().hashCode(), 15, g.getName()).setChecked(true);
        setGroup(g);
    }

    public void update() {
        if(f != null) {

        }
    }

    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                mDrawerLayout.openDrawer(GravityCompat.START);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void setGroup(Group g) {
        g.setCurrent();
        OverViewActivity ova = OverViewActivity.newInstance();
        swapFragment(ova);
        if(ova != null)
            ova.update();
    }

    private void clearChecked() {
        int size = menu.size();
        for(int i = 0; i < size; i++){
            menu.getItem(i).setChecked(false);
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        currentMainActivity = this;
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);

        setContentView(R.layout.drawer_layout);

        mDrawerLayout = findViewById(R.id.drawer_layout);

        NavigationView navView = findViewById(R.id.nav_view);
        navView.setNavigationItemSelectedListener((mi) -> {
            clearChecked();
            mi.setChecked(true);
            mDrawerLayout.closeDrawers();
            Intent intent;
            switch (mi.getItemId()) {
                case R.id.settings:
                    Intent settingsIntent = new Intent(this, SettingsActivity.class);
                    this.startActivity(settingsIntent);
                    break;
                case R.id.logout:
                    FirebaseAuth.getInstance().signOut();
                    SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
                    SharedPreferences.Editor editor = preferences.edit();
                    editor.putString(getString(R.string.user_name), " ");
                    editor.putString(getString(R.string.email), " ");
                    editor.putString(getString(R.string.user_id), " ");
                    editor.apply();
                    Intent loginIntent = new Intent(this, LoginActivity.class);
                    this.startActivity(loginIntent);
                    this.finish();
                    break;
                case R.id.add_friend:
                    intent = new Intent(this, AddFriend.class);
                    startActivity(intent);
                    break;
                case R.id.add_group:
                    intent = new Intent(this, AddGroup.class);
                    startActivity(intent);
                    break;
                case R.id.nav_friends:
                    swapFragment(Friends.newInstance());
                    break;
                default:
                    Group g = Store.getGroupByName(mi.getTitle().toString());
                    if(g != null) { // should always work
                        setGroup(g);
                    }
            }
            return true;
        });

        menu = navView.getMenu();

        updateGroups();

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        ActionBar actionbar = getSupportActionBar();
        actionbar.setDisplayHomeAsUpEnabled(true);
        actionbar.setHomeAsUpIndicator(R.drawable.ic_menu_black_24dp);

        // Set selected drawer item on first start
        if (savedInstanceState == null) {
            navView.getMenu().performIdentifierAction(R.id.nav_friends, 0);
        }
    }

}
