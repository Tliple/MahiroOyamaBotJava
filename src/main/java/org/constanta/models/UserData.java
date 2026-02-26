package org.constanta.models;

public class UserData {

    private String userId;
    private int balance;
    private String lastFantikDate;

    public UserData(String userId) {
        this.userId = userId;
        this.balance = 0;
        this.lastFantikDate = null;
    }

    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }

    public int getBalance() { return balance; }
    public void setBalance(int balance) { this.balance = balance; }
    public void addBalance(int amount) { this.balance += amount; }

    public String getLastFantikDate() { return lastFantikDate; }
    public void setLastFantikDate(String date) { this.lastFantikDate = date; }
}