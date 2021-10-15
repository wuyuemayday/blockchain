package blockchain;

import com.google.gson.Gson;
import org.apache.commons.codec.digest.DigestUtils;

import java.util.*;

public class Block {
    private static final Gson gson = new Gson();
    private final int index;
    private final long timestamp;
    private final long proof;
    private final String previousHash;
    private final Set<Transaction> transactions;

    public Block(final int index,
                 final long timestamp,
                 final long proof,
                 final String previousHash,
                 final Set<Transaction> transactions) {
        this.index = index;
        this.timestamp = timestamp;
        this.proof = proof;
        this.previousHash = previousHash;
        this.transactions = new HashSet<>(transactions);
    }

    public String hash() {
        final String j = gson.toJson(this);
        final TreeMap<String, Object> sortedByKeys = gson.fromJson(j, TreeMap.class);
        final String encodedBlock = gson.toJson(sortedByKeys);

        return DigestUtils.sha256Hex(encodedBlock);
    }

    public int getIndex() {
        return index;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public long getProof() {
        return proof;
    }

    public String getPreviousHash() {
        return previousHash;
    }
}
