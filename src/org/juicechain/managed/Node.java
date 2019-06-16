package org.juicechain.managed;

import org.json.simple.JSONObject;
import org.juicechain.exceptions.IssueException;
import org.juicechain.exceptions.NotAuthorizedException;
import org.juicechain.helpers.RequestHelper;
import org.juicechain.helpers.Response;
import org.juicechain.models.AssetParams;
import org.juicechain.models.AssetType;
import org.juicechain.models.Telemetry;

import java.io.IOException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;

public class Node {

    public String node;

    private RequestHelper requestHelper;

    public Node(String node, String username, String apiKey){
        this.node = node;
        this.requestHelper = new RequestHelper(node, username, apiKey);
    }

    /**
     * Create new wallet located on this Node
     *
     * @return
     * @throws NotAuthorizedException
     * @throws IOException
     */
    public Wallet createWallet() throws NotAuthorizedException, IOException {

        Response<JSONObject> response = (Response<JSONObject>) this.requestHelper.post("wallet", "", "");

        if (response.success){
            Wallet wallet = new Wallet(this);
            wallet.parse(response.payload);
            return wallet;
        }

        return null;
    }

    public Wallet getWallet(String privateKey, String address){
        Wallet wallet = new Wallet(this);
        wallet.address = address;
        wallet.privateKey = privateKey;
        wallet.node = node;

        return wallet;
    }

    /**
     * Issue new Asset
     *
     * @param name Name of the Asset
     * @param title Title (readable) & i18n
     * @param type Type of asset (admission, voucher, coupon, contract)
     * @param amount
     * @param targetAddress
     * @param publisher
     * @return
     * @throws NotAuthorizedException
     * @throws IssueException
     * @throws IOException
     * @throws ParseException
     */
    public Asset issue(String name, String title, AssetType type, int amount, String targetAddress, String publisher)
            throws NotAuthorizedException, IssueException, IOException, ParseException {

        JSONObject _title = new JSONObject();
        _title.put("de_DE", title);

        JSONObject _options = new JSONObject();
        _options.put("transferAll",  true);
        _options.put("transferNode", true);
        _options.put("returnAddress", null);

        JSONObject issueRequest = new JSONObject();
        issueRequest.put("name", name);
        issueRequest.put("title", _title);
        issueRequest.put("type", type.toString());
        issueRequest.put("amount", amount);
        issueRequest.put("target", targetAddress);
        issueRequest.put("publisher", publisher);
        issueRequest.put("options", _options);

        Response<JSONObject> response = (Response<JSONObject>) this.requestHelper.post("asset", issueRequest.toJSONString(), "");

        if (response.success){
            Asset asset = new Asset(this);
            asset.parse(response.payload);
            return asset;
        } else {
            throw new IssueException(response.error);
        }
    }

    public Asset issueNFT(String name, String receiver, String content, AssetParams params, int amount, String signature)
            throws NotAuthorizedException, IssueException, IOException, ParseException {

        JSONObject issueRequest = new JSONObject();
        issueRequest.put("name", name);
        issueRequest.put("receiver", receiver);
        issueRequest.put("content", content);
        issueRequest.put("amount", amount);

        JSONObject _params = new JSONObject();

        if (params.inception != null){
            DateFormat dateFormat=new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ");
            _params.put("inception", dateFormat.format(params.inception));
        }

        if (params.experiation != null){
            DateFormat dateFormat=new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ");
            _params.put("expiration", dateFormat.format(params.experiation));
        }

        issueRequest.put("params", _params);

        Response<JSONObject> response = (Response<JSONObject>) this.requestHelper.post("nft", issueRequest.toJSONString(), signature);

        if (response.success){
            Asset asset = new Asset(this);
            asset.parse(response.payload);
            return asset;
        } else {
            throw new IssueException(response.error);
        }
    }

    public Telemetry getTelemetry() throws NotAuthorizedException, IOException {

        Response _telemetry = this.requestHelper.get("telemetry");

        Telemetry telemetry = new Telemetry();
        telemetry.parse(_telemetry.payload.toJSONString());

        return telemetry;
    }

    public RequestHelper request() {
        return requestHelper;
    }

}

