package wondough;

import java.util.*;

public class Transactions {
    private float accountBalance = 0.0f;
    private ArrayList<Transaction> transactions;

    public Transactions() {
        this.transactions = new ArrayList<Transaction>();
    }

    public float getAccountBalance() {
        return this.accountBalance;
    }

    public void setAccountBalance(float balance) {
        this.accountBalance = balance;
    }

    public void addTransaction(Transaction t) {
        this.transactions.add(t);
    }

    public ArrayList<Transaction> getTransactions() {
        return this.transactions;
    }
}
