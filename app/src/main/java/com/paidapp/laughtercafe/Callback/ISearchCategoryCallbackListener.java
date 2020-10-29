package com.paidapp.laughtercafe.Callback;

import com.paidapp.laughtercafe.Database.CartItem;
import com.paidapp.laughtercafe.Model.CategoryModel;
import com.paidapp.laughtercafe.Model.FoodModel;

public interface ISearchCategoryCallbackListener {
    void onSearchCategoryFound(CategoryModel categoryModel, CartItem cartItem);
    void onSearchCategoryNotFound(String message);
}
