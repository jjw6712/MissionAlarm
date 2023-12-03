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

    private static final int REAL_COUNT = 6;  // 실제 페이지 수

    public RankingPagerAdapter(@NonNull FragmentManager fragmentManager, @NonNull Lifecycle lifecycle) {
        super(fragmentManager, lifecycle);
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
        int realPosition = position % REAL_COUNT;  // 실제 페이지 위치 계산
        switch (realPosition) {
            case 0:
                return new Ranking();
            case 1:
                return new SquatRanking();
            case 2:
                return new PushupRanking();
            case 3:
                return new SpeachWordsRanking();
            case 4:
                return new SpeachSentencesRanking();
            case 5:
                return new QuizRanking();
            default:
                throw new IllegalArgumentException("Invalid position");
        }
    }

    @Override
    public int getItemCount() {
        return REAL_COUNT * 2; // 실제 페이지 수의 두 배
    }
}