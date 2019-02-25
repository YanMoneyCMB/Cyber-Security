package wondough;

import com.google.gson.Gson;
import com.google.gson.annotations.*;

/**
* Represents a transaction.
* @author The Intern
*/
public class Transaction {
    /** Stores the ID of the transaction. */
    @Expose
    private int id;
    /** Stores the ID of the account. */
    @Expose
    private int user;
    /** Stores the amount. */
    @Expose
    private float amount;
    /** Stores the description. */
    @Expose
    private String description;

    /**
    * Constructs a transaction with the specified ID.
    * @param id The unique ID of the transaction.
    */
    public Transaction(int id) {
        this.id = id;
    }

    public int getID() {
        return this.id;
    }

    public void setUserID(int user) {
        this.user = user;
    }

    public int getUserID() {
        return this.user;
    }

    public void setAmount(float amount) {
        this.amount = amount;
    }

    public float getAmount() {
        return this.amount;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getDescription() {
        return this.description;
    }
}
