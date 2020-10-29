package com.paidapp.laughtercafe.Callback;

import com.paidapp.laughtercafe.Model.BestDealModel;
import com.paidapp.laughtercafe.Model.RestaurantModel;

import java.util.List;

public interface IRestaurantCallbackListener {
    void onRestaurantLoadSuccess(List<RestaurantModel> restaurantModelList);
    void onRestaurantLoadFailed(String message);
}
