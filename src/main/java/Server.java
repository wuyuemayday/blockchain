import blockchain.Block;
import blockchain.Blockchain;
import blockchain.Mempool;
import blockchain.Transaction;
import com.google.gson.Gson;
import com.squareup.okhttp.OkHttpClient;
import gateway.Gateway;
import io.javalin.Javalin;
import io.javalin.http.Context;
import io.javalin.plugin.json.JavalinJson;
import miner.Miner;
import network.ClusterManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;

public class Server {
    private static final Logger logger = LoggerFactory.getLogger(Server.class);

    private final Javalin app;
    private final Gson gson;
    private final Miner miner;
    private final ClusterManager clusterManager;
    private final String address;

    public Server(final String address,
                  final Javalin app,
                  final Gson gson,
                  final ClusterManager clusterManager,
                  final Miner miner) {
        this.address = address;
        this.app = app;
        this.gson = gson;
        this.clusterManager = clusterManager;
        this.miner = miner;
    }

    public void run() throws Exception {
        logger.info("Setting up routes ...");
        this.app.get("/", this::handleRoot);
        this.app.get("/mineblock", this::handleMineBlock);
        this.app.get("/chain", this::handleBlockchain);

        this.app.post("/transaction", this::handlePostTransaction);
        this.app.put("/sync", this::handleSyncChain);

        logger.info("Register to the cluster ...");
        this.clusterManager.registerNode(this.address);
    }

    private void handleRoot(final Context ctx) {
        ctx.result("Hello there!");
    }

    private void handleMineBlock(final Context ctx) {
        final Block newBlock = this.miner.mineBlock();
        Map<String, Object> response = new HashMap<>();
        response.put("message", "Congratulations! You just mined a block");
        response.put("block", newBlock);
        ctx.contentType("application/json");
        ctx.status(200);

        ctx.json(response);
    }

    private void handleBlockchain(final Context ctx) {
        ctx.contentType("application/json");

        final Blockchain chain = this.miner.getBlockchain();
        if (chain.isChainValid()) {
            ctx.status(200);

            Map<String, Object> response = new HashMap<>();
            response.put("chain", chain);
            response.put("length", chain.getLength());

            ctx.json(response);
        } else {
            ctx.status(500);

            ctx.html("the chain is not valid");
        }
    }

    private void handlePostTransaction(final Context ctx) {
        if (ctx.body().isEmpty()) {
            ctx.status(400);
            ctx.html("invalid request");
            return;
        }

        final Transaction txn = this.gson.fromJson(ctx.body(), Transaction.class);
        try {
            this.miner.getMempool().addTransation(txn);
            ctx.status(201);
        } catch (final Exception e) {
            ctx.status(500);
            ctx.html(e.getMessage());
        }
    }

    private void handleSyncChain(final Context ctx) {
        ctx.contentType("application/json");

        try {
            final Blockchain newChain = this.miner.syncChain();
            Map<String, Object> response = new HashMap<>();
            response.put("chain", newChain);
            response.put("length", newChain.getLength());

            ctx.json(response);
            ctx.status(200);
        } catch (final Exception e) {
           ctx.status(500);
           ctx.html(e.getMessage());
       }
    }

    public static void main(String[] args) throws Exception {
        if (args.length < 1) {
            System.exit(1);
        }

        final int port = Integer.parseInt(args[0]);
        final String address = "localhost:" + port;
        logger.info("Server {} starting ...", address);

        final ClusterManager clusterManager = new ClusterManager();

        final Gson gson = new Gson();
        JavalinJson.setFromJsonMapper(gson::fromJson);
        JavalinJson.setToJsonMapper(gson::toJson);

        final OkHttpClient client = new OkHttpClient();
        final Javalin app = Javalin.create().start(port);
        final Blockchain blockchain = new Blockchain(4);
        final Gateway gateway = new Gateway(client, gson);
        final Mempool mempool = new Mempool();

        final Miner miner = new Miner(address, clusterManager, blockchain, gateway, mempool);

        new Server(
                address, app, gson, clusterManager, miner
        ).run();
    }
}
