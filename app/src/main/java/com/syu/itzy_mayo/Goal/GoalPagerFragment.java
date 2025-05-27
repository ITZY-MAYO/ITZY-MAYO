package com.syu.itzy_mayo.Goal;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.viewpager2.widget.ViewPager2;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;
import com.syu.itzy_mayo.R;

public class GoalPagerFragment extends Fragment {

    private ViewPager2 viewPager;
    private GoalPagerAdapter pagerAdapter;

    public GoalPagerFragment() {
        super(R.layout.goal_pager_fragment); // XML 레이아웃 필요
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.goal_pager_fragment, container, false);

        TabLayout tabLayout = view.findViewById(R.id.tabLayout);
        viewPager = view.findViewById(R.id.viewPager);
        FloatingActionButton fab = view.findViewById(R.id.fabAddGoal);

        pagerAdapter = new GoalPagerAdapter(this);
        viewPager.setAdapter(pagerAdapter);

        new TabLayoutMediator(tabLayout, viewPager, (tab, position) -> {
            tab.setText(position == 0 ? "오늘 목표" : "전체 목표");
        }).attach();

        viewPager.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position) {
                fab.setVisibility(position == 1 ? View.VISIBLE : View.GONE);
            }
        });

        fab.setOnClickListener(v -> {
            int pos = viewPager.getCurrentItem();
            Fragment f = getChildFragmentManager().getFragments().get(pos);
            if (f instanceof GoalTabFragment) {
                ((GoalTabFragment) f).showAddGoalDialog();
            }
        });

        return view;
    }

    public void refreshAllTabs() {
        for (Fragment f : getChildFragmentManager().getFragments()) {
            if (f instanceof GoalTabFragment) {
                ((GoalTabFragment) f).refresh();
            }
        }
    }
}
