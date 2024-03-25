package com.playtech.assignment;

class User {
    String userId;
    String username;
    double balance;
    String country;
    int frozen;
    double depositMin;
    double depositMax;
    double withdrawMin;
    double withdrawMax;

    public User(String userId, String username, double balance, String country, int frozen, double depositMin, double depositMax, double withdrawMin, double withdrawMax) {
        this.userId = userId;
        this.username = username;
        this.balance = balance;
        this.country = country;
        this.frozen = frozen;
        this.depositMin = depositMin;
        this.depositMax = depositMax;
        this.withdrawMin = withdrawMin;
        this.withdrawMax = withdrawMax;
    }

    @Override
    public String toString() { //For debugging purposes
        return "User{" +
                "userId='" + userId + '\'' +
                ", username='" + username + '\'' +
                ", balance=" + balance +
                ", country='" + country + '\'' +
                ", frozen=" + frozen +
                ", depositMin=" + depositMin +
                ", depositMax=" + depositMax +
                ", withdrawMin=" + withdrawMin +
                ", WithdraMax=" + withdrawMax +
                '}';
    }
}