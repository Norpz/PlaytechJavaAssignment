package com.playtech.assignment;

public class Transaction {
    String transactionId;
    String userId;
    String type;
    double amount;
    String method;
    String accountNumber;

    public Transaction(String transactionId, String userId, String type, double amount, String method, String accountNumber) {
        this.transactionId = transactionId;
        this.userId = userId;
        this.type = type;
        this.amount = amount;
        this.method = method;
        this.accountNumber = accountNumber;

    }

    @Override
    public String toString() { //For debugging purposes
        return "Transaction{" +
                "transactionId='" + transactionId + '\'' +
                ", userId='" + userId + '\'' +
                ", type='" + type + '\'' +
                ", amount=" + amount +
                ", method='" + method + '\'' +
                ", accountNumber='" + accountNumber + '\'' +
                '}';
    }

}
