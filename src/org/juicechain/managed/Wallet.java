package org.juicechain.managed;

import org.bitcoinj.core.DumpedPrivateKey;
import org.bitcoinj.core.ECKey;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.juicechain.JuicEchain;
import org.juicechain.exceptions.NotAuthorizedException;
import org.juicechain.exceptions.NotFoundException;
import org.juicechain.exceptions.TransferException;
import org.juicechain.helpers.Response;
import org.juicechain.models.Balance;

import java.io.IOException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

public class Wallet{

    public String privateKey;
    public String publicKey;
    public String address;
    public String provider;
    public String title;
    public String node;

    private Node _node;

    public Wallet(Node node){
        _node = node;
    }

    /**
     * Fetch balance of the wallet
     *
     * @return List with balance of each asset
     * @throws NotAuthorizedException
     * @throws IOException
     */
    public List<Balance> getBalance() throws NotAuthorizedException, IOException {
        return getBalance(0);
    }

    /**
     * Fetch balance of the wallet
     *
     * @param minconf minimal amount of confirmations required to appear in list
     *
     * @return List with balance of each asset
     * @throws NotAuthorizedException
     * @throws IOException
     */
    public List<Balance> getBalance(int minconf) throws NotAuthorizedException, IOException {

        ArrayList<Balance> balances = null;

        Response<JSONArray> response = (Response<JSONArray>) _node.request().get("/node/wallet/" + address + "/" + minconf + "/ACV");

        if (response.success){
            balances = new ArrayList<Balance>(response.payload.size());

            for (Object  _balance : response.payload){
                Balance balance = new Balance();
                balance.quantity = Integer.parseInt(((JSONObject)_balance).get("quantity").toString());
                balance.asset = ((JSONObject)_balance).get("name").toString();
                balance.updated = new Date();
                balances.add(balance);
            }
        }

        return balances;
    }

    /**
     * Transfer asset to receiver
     *
     * @param receiverAddress Address of receiver (Can be any wallet/address in JuicEchain)
     * @param asset Name of the asset
     * @param quantity Amount of assets to transfer
     * @param payload Additional comments stored with the transfer
     * @return
     * @throws NotAuthorizedException
     * @throws TransferException
     * @throws IOException
     */
    public boolean transfer(String receiverAddress, String asset, int quantity, String payload)
            throws NotAuthorizedException, TransferException, IOException {

        JSONObject body = new JSONObject();
        body.put("asset", asset);
        body.put("amount", quantity);
        body.put("payload", payload);

        String authentication = this.getAuthentication("");

        Response<JSONObject> response = (Response<JSONObject>) _node.request()
                .post("wallet/transfer/" + receiverAddress, body.toJSONString(), authentication);

        if (response.success)
            return true;
        else {
            throw new TransferException(response.error);
        }
    }

    /**
     * Load Asset
     *
     * @param name Name of asset
     * @return Asset
     *
     * @throws NotAuthorizedException
     * @throws IOException
     * @throws NotFoundException
     * @throws ParseException
     */
    public Asset getAsset(String name) throws NotAuthorizedException, IOException, NotFoundException, ParseException {

        JSONObject body = new JSONObject();
        body.put("name", name);

        Response<JSONObject> response = (Response<JSONObject>) _node.request().post("asset/details", body.toJSONString(), "");

        if (response.success) {
            Asset asset = new Asset(_node);
            asset.parse(response.payload);
            return asset;
        } else {
            throw new NotFoundException("Invalid Asset");
        }
    }

    /**
     * Generate BitCoin signed Message as wallet authentication
     *
     * @param asset Name of Asset
     *
     * @return Base64 Encoded JSON with BitCoin Signature
     */
    public String getAuthentication(String asset){
        String deviceId = "";
        DumpedPrivateKey dpk = DumpedPrivateKey.fromBase58(null, privateKey);
        ECKey key = dpk.getKey();

        DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ");
        dateFormat.setTimeZone(TimeZone.getDefault());

        String message = "{" +
                "\"address\":\"" + address + "\"" +
                ",\"device\":\"" + deviceId + "\"" +
                ",\"asset\":\"" + asset + "\"" +
                ",\"timestamp\":\"" + dateFormat.format(new Date()) + "\"}";

        String signatureBase64 = key.signMessage(message);
        message = message.substring(0, message.length() - 1);
        message += ", \"signature\" : \" " + signatureBase64 + " \" } ";

        return org.bouncycastle.util.encoders.Base64.toBase64String(message.getBytes());
    }

    public boolean parse(JSONObject obj){

        // system
        this.privateKey = obj.get("privateKey").toString();
        this.publicKey = obj.get("publicKey").toString();
        this.address = obj.get("address").toString();
        this.node = obj.get("node").toString();
        this.provider = obj.get("provider").toString();
        this.title = obj.get("title").toString();

        return true;
    }

}
