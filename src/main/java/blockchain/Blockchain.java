package blockchain;

import org.apache.commons.codec.digest.DigestUtils;

import java.time.Instant;
import java.util.*;

public class Blockchain {
    private static final int MAX_TRANSACTIONS_PER_BLOCK = 10;
    private final Deque<Block> chain;
    private final Mempool mempool;

    private double coinbaseReward = 12.5;
    private int difficult; // leading 0s

    public Blockchain(final int difficult) {
        this.chain = new ArrayDeque<>();
        this.difficult = difficult;
        this.mempool = new Mempool();

        final Block genesis = new Block(0, Instant.now().toEpochMilli(), 0,
                null, new HashSet<>());
        chain.offerLast(genesis);
    }

    public Block createBlock(final String minerAddress,
                             final long proof,
                             final String previousHash,
                             final List<Transaction> transactions) {
        final Instant now = Instant.now();

        final Transaction coinbase = new Transaction(UUID.randomUUID(), "BLOCKCHAIN", minerAddress, coinbaseReward);
        transactions.add(0, coinbase);

        final Block block = new Block(
                this.chain.size(),
                now.toEpochMilli(),
                proof,
                previousHash,
                new HashSet<>(transactions));

        this.chain.offerLast(block);
        return block;
    }

    public Optional<Block> getLastBlock() {
        return Optional.ofNullable(chain.peekLast());
    }

    public long proofOfWork(final long previousProof) {
        long newProof = 0;
        boolean done = false;
        String hash = null;
        while (!done) {
            newProof++;
            hash = hashFromProof(previousProof, newProof);

            int i = 0;
            while (i < difficult) {
                if (hash.charAt(i) != '0') break;
                else i++;
            }

            done = i == difficult;
        }

        return newProof;
    }

    private String hashFromProof(final long previousProof, final long currentProof) {
        final String data = String.valueOf((long) currentProof * currentProof - (long) previousProof * previousProof);
        return DigestUtils.sha256Hex(data);
    }

    public boolean isChainValid() {
        if (this.chain.isEmpty()) return false;

        Block prev = null;
        for (final Block block : chain) {
            if (prev == null) {
                prev = block;
                continue;
            }

            // check if block.previousHash is correct
            if (!block.getPreviousHash().equals(prev.hash())) {
                return false;
            }

            // check if current block's proof is correct by regenerating the hash from
            // previous proof and current proof to see the result has 'difficult' leading 0s
            final String hash = this.hashFromProof(prev.getProof(), block.getProof());
            int i = 0;
            while (i < difficult) {
                if (hash.charAt(i) != '0') break;

                i++;
            }

            if (i != difficult) {
                return false;
            }

            prev = block;
        }

        return true;
    }

    public int getLength() {
        return this.chain.size();
    }

    public int getMaxTransactionsPerBlock() {
        return MAX_TRANSACTIONS_PER_BLOCK;
    }

    public void setDifficult(final int difficult) {
        this.difficult = difficult;
    }

    public void setCoinbaseReward(final double coinbaseReward) {
        this.coinbaseReward = coinbaseReward;
    }

    public Mempool getMempool() {
        return this.mempool;
    }
}