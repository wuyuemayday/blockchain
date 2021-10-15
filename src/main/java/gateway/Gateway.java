package gateway;

import blockchain.Blockchain;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Gateway {
    private static final Logger logger = LoggerFactory.getLogger(Gateway.class);

    private final OkHttpClient client;
    private final Gson gson;

    public Gateway(final OkHttpClient client,
                   final Gson gson) {
        this.client = client;
        this.gson = gson;
    }

    public Blockchain getChain(final String node) throws Exception {
        final String address = "http://" + node + "/chain";
        logger.info("Sending request to {}", address);
        final Request req = new Request.Builder().url(address).build();
        final Response resp = this.client.newCall(req).execute();
        if (!resp.isSuccessful()) {
            throw new Exception(String.format("Fail to call %s", address));
        }

        final String body = resp.body().string();

        final JsonObject responseObject = gson.fromJson(body, JsonObject.class);
        return gson.fromJson(responseObject.get("chain"), Blockchain.class);
    }
}
