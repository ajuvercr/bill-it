package seacoalCo.bill_it.utility_classes;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;

import seacoalCo.bill_it.friends.Friends;
import seacoalCo.bill_it.groups.Groups;

public class MyPagerAdapter extends FragmentStatePagerAdapter {


    public MyPagerAdapter(FragmentManager fm){
        super(fm);
    }

    @Override    public Fragment getItem(int position) {
        switch (position){
            case 0: return Friends.newInstance();
            case 1: return Groups.newInstance();
        }
        return null;
    }

    @Override
    public int getCount() {
        return 2;
    }

    @Override    public CharSequence getPageTitle(int position) {
        switch (position){
            case 0: return "Friends";
            case 1: return "Groups";
            default: return null;
        }
    }
}
