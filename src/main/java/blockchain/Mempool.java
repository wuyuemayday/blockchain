package blockchain;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

public final class Mempool {
    private final Set<Transaction> transactions;

    public Mempool() {
        this.transactions = new HashSet<>();
    }

    public void addTransation(final Transaction txn) throws Exception {
        if (this.transactions.contains(txn)) {
            throw new IllegalArgumentException("Transation is already in the mempool");
        }

        this.transactions.add(txn);
    }

    public void removeTransaction(final Transaction txn) {
        this.transactions.remove(txn);
    }

    public Set<Transaction> getTransactions() {
        return Collections.unmodifiableSet(this.transactions);
    }
}
