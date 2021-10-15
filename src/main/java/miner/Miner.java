package miner;

import blockchain.Block;
import blockchain.Blockchain;
import blockchain.Mempool;
import blockchain.Transaction;
import gateway.Gateway;
import network.ClusterManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

public class Miner {
    private static final Logger logger = LoggerFactory.getLogger(Miner.class);

    private final String address;
    private final ClusterManager clusterManager;
    private final Gateway gateway;
    private final Mempool mempool;

    private Blockchain blockchain;

    public Miner(final String address,
                 final ClusterManager clusterManager,
                 final Blockchain chain,
                 final Gateway gateway,
                 final Mempool mempool) {
        this.address = address;
        this.clusterManager = clusterManager;
        this.blockchain = chain;
        this.gateway = gateway;
        this.mempool = mempool;
    }

    public Block mineBlock() {
        final Block last = this.blockchain.getLastBlock().orElseThrow();

        final long previousProof = last.getProof();
        final long proof = blockchain.proofOfWork(previousProof);
        final String previousHash = last.hash();

        final List<Transaction> transactions = new ArrayList<>();
        for (final Transaction t : this.mempool.getTransactions()) {
            transactions.add(t);
            if (transactions.size() == this.blockchain.getMaxTransactionsPerBlock()) {
                break;
            }
        }

        for (final Transaction t : transactions) {
            this.mempool.removeTransaction(t);
        }

        return blockchain.createBlock(this.address, proof, previousHash, transactions);
    }

    public Blockchain syncChain() throws Exception {
        Blockchain longestChain = this.blockchain;

        for (final String peerAddress : this.clusterManager.getNodes()) {
            if (!peerAddress.equals(this.address)) {
                logger.info("Checking chain from peer address {}", peerAddress);
                final Blockchain otherChain = this.gateway.getChain(peerAddress);
                if (otherChain.getLength() > longestChain.getLength()) {
                    longestChain = otherChain;
                }
            }
        }

        logger.info("longest chain {}", longestChain);
        this.blockchain = longestChain;
        return longestChain;
    }

    public Blockchain getBlockchain() {
        return this.blockchain;
    }

    public Mempool getMempool() {
        return this.mempool;
    }
}

