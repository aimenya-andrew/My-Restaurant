package com.paidapp.laughtercafe.Callback;

import com.paidapp.laughtercafe.Model.CategoryModel;

import java.util.List;

public interface ICategoryCallbackListener {
    void onCategoryLoadSuccess(List<CategoryModel> categoryModelList);
    void onCategoryLoadFailed(String message);
}
