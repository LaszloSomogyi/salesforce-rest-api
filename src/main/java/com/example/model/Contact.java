package com.example.model;

import java.io.Serializable;

public class Contact implements Serializable {

    private String lastName;
    private String firstName;
    private Account account;

    public Contact() {
    }

    public Contact(String lastName, String firstName, Account account) {
        this.lastName = lastName;
        this.firstName = firstName;
        this.account = account;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public Account getAccount() {
        return account;
    }

    public void setAccount(Account account) {
        this.account = account;
    }

    @Override
    public String toString() {
        return "Contact{" +
                "lastName='" + lastName + '\'' +
                ", firstName='" + firstName + '\'' +
                ", account=" + account +
                '}';
    }
}
