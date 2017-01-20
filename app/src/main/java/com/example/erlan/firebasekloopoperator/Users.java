package com.example.erlan.firebasekloopoperator;

/**
 * Created by erlan on 20.01.2017.
 */

public class Users {
    private String email;
    private int status;

    public Users() {

    }

    public Users(String email, int status) {
        this.email = email;
        this.status = status;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }
}
