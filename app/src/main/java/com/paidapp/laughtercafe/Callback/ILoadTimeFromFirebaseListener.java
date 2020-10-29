package com.paidapp.laughtercafe.Callback;

import com.paidapp.laughtercafe.Model.OrderModel;

public interface ILoadTimeFromFirebaseListener {
    void onLoadTimeSuccess(OrderModel order, long estimateTimeInMs);
    void onLoadOnlyTimeSuccess(long estimateTimeInMs);
    void onLoadTimeFailed(String message);
}
