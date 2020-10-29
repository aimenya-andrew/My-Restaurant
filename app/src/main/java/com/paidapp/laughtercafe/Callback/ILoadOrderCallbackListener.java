package com.paidapp.laughtercafe.Callback;

import com.paidapp.laughtercafe.Model.OrderModel;

import java.util.List;

public interface ILoadOrderCallbackListener {
    void onLoadOrderSuccess(List<OrderModel> orderList);
    void onLoadOrderFailed(String message);
}
