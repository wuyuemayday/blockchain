package blockchain;

import java.util.UUID;

public final class Transaction {
    private final UUID id;
    private final String sender;
    private final String receiver;
    private final double amount;

    public Transaction(final UUID id,
                       final String sender,
                       final String receiver,
                       final double amount) {
        this.id = id;
        this.sender = sender;
        this.receiver = receiver;
        this.amount = amount;
    }

    public String getID() {
        return id.toString();
    }

    public String getSender() {
        return sender;
    }

    public String getReceiver() {
        return receiver;
    }

    public double getAmount() {
        return amount;
    }

    @Override
    public int hashCode() {
        return this.id.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) return false;
        if (!(obj instanceof Transaction)) return false;

        Transaction t = (Transaction) obj;
        return t.getID().equals(this.getID());
    }
}
