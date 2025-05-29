package com.syu.itzy_mayo.Goal;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.viewpager2.adapter.FragmentStateAdapter;

public class GoalPagerAdapter extends FragmentStateAdapter {

    public GoalPagerAdapter(@NonNull Fragment fragment) {
        super(fragment);
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
        return GoalTabFragment.newInstance(position == 0 ? "today" : "all");
    }

    @Override
    public int getItemCount() {return 2;}

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public boolean containsItem(long itemId) {
        return itemId == 0 || itemId == 1;
    }
}