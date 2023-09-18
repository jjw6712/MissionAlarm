package com.example.ar1.ui.rank;

import static androidx.fragment.app.FragmentManager.TAG;

import android.annotation.SuppressLint;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.lifecycle.Lifecycle;
import androidx.viewpager2.adapter.FragmentStateAdapter;

public class RankingPagerAdapter extends FragmentStateAdapter {

    public RankingPagerAdapter(@NonNull FragmentManager fragmentManager, @NonNull Lifecycle lifecycle) {
        super(fragmentManager, lifecycle);
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
        switch (position) {
            case 0:
                return new Ranking();  // 새 인스턴스 생성
            case 1:
                return new SquatRanking();  // 새 인스턴스 생성
            case 2:
                return new PushupRanking();
            default:
                throw new IllegalArgumentException("Invalid position");
        }
    }

    @Override
    public int getItemCount() {
        return 3;
    }
}