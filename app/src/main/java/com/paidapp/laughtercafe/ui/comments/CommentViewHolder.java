package com.paidapp.laughtercafe.ui.comments;

import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.paidapp.laughtercafe.Model.CommentModel;

import java.util.List;

public class CommentViewHolder extends ViewModel {
    private MutableLiveData<List<CommentModel>> mutableLiveDataFoodList;

    public CommentViewHolder() {
        mutableLiveDataFoodList = new MutableLiveData<>();
    }

    public MutableLiveData<List<CommentModel>> getMutableLiveDataFoodList() {
        return mutableLiveDataFoodList;
    }

    public void setCommentList(List<CommentModel> commentList)
    {
        mutableLiveDataFoodList.setValue(commentList);
    }
}
