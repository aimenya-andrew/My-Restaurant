package com.paidapp.laughtercafe.Model;

public class BraintreeTranscation {
    private boolean success;
    private Transaction transaction;

    public BraintreeTranscation() {
    }

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public Transaction getTransaction() {
        return transaction;
    }

    public void setTransaction(Transaction transaction) {
        this.transaction = transaction;
    }
}
