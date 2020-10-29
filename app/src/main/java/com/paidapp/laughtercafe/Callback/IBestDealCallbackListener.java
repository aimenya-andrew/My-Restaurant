package com.paidapp.laughtercafe.Callback;

import com.paidapp.laughtercafe.Model.BestDealModel;

import java.util.List;

public interface IBestDealCallbackListener {
    void onBestDealLoadSuccess(List<BestDealModel> bestDealModels);
    void onBestDealLoadFailed(String message);
}
