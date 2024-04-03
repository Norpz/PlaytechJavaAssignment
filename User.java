package com.playtech.assignment;

import java.math.BigDecimal;

class User {
    String userId;
    String username;
    BigDecimal balance;
    String country;
    int frozen;
    BigDecimal depositMin;
    BigDecimal depositMax;
    BigDecimal withdrawMin;
    BigDecimal withdrawMax;

    public User(String userId, String username, BigDecimal balance, String country, int frozen, BigDecimal depositMin, BigDecimal depositMax, BigDecimal withdrawMin, BigDecimal withdrawMax) {
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

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public BigDecimal getBalance() {
        return balance;
    }

    public void setBalance(BigDecimal balance) {
        this.balance = balance;
    }

    public String getCountry() {
        return country;
    }

    public void setCountry(String country) {
        this.country = country;
    }

    public int getFrozen() {
        return frozen;
    }

    public void setFrozen(int frozen) {
        this.frozen = frozen;
    }

    public BigDecimal getDepositMin() {
        return depositMin;
    }

    public void setDepositMin(BigDecimal depositMin) {
        this.depositMin = depositMin;
    }

    public BigDecimal getDepositMax() {
        return depositMax;
    }

    public void setDepositMax(BigDecimal depositMax) {
        this.depositMax = depositMax;
    }

    public BigDecimal getWithdrawMin() {
        return withdrawMin;
    }

    public void setWithdrawMin(BigDecimal withdrawMin) {
        this.withdrawMin = withdrawMin;
    }

    public BigDecimal getWithdrawMax() {
        return withdrawMax;
    }

    public void setWithdrawMax(BigDecimal withdrawMax) {
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