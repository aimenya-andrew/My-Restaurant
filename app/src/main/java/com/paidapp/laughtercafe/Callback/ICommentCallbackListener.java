package com.paidapp.laughtercafe.Callback;

import com.paidapp.laughtercafe.Model.CommentModel;

import java.util.List;

public interface ICommentCallbackListener {
    void onCommentLoadSuccess(List<CommentModel> commentModels);
    void onCommentLoadFailed(String message);
}
