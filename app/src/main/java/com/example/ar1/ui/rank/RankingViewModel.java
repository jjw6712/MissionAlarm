package com.example.ar1.ui.rank;

import androidx.lifecycle.ViewModel;

public class RankingViewModel extends ViewModel {
    private boolean isAdapterInitialized = false;

    public boolean isAdapterInitialized() {
        return isAdapterInitialized;
    }

    public void setAdapterInitialized(boolean adapterInitialized) {
        isAdapterInitialized = adapterInitialized;
    }
}