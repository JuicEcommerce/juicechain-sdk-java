package org.juicechain.managed;

import org.json.simple.JSONObject;
import org.juicechain.exceptions.NotAuthorizedException;
import org.juicechain.helpers.Response;

import java.io.File;
import java.io.IOException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class Asset {

    public String name;
    public String publisher;
    public String title;
    public int amount;
    public String type;
    public String description;
    public boolean valid;
    public boolean master;
    public String mediaUrl;
    public String mediaType;
    public String card;
    public String issuer;
    public Date inception;
    public Date expiration;

    private Node _node;

    public Asset(Node node){
        this._node = node;
    }

    /**
     * Update asset parameters
     *
     * @param inception
     * @param expiration
     * @return
     * @throws NotAuthorizedException
     * @throws IOException
     */
    public boolean setParameters(Date inception, Date expiration) throws NotAuthorizedException, IOException {

        JSONObject params = new JSONObject();

        if (inception != null){
            DateFormat dateFormat=new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ");
            params.put("inception", dateFormat.format(inception));
        }

        if (expiration != null){
            DateFormat dateFormat=new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ");
            params.put("expiration", dateFormat.format(expiration));
        }

        Response<JSONObject> response = (Response<JSONObject>) _node.request().put("asset/parameters", params.toJSONString(), "");

        return response.success;
    }

    /**
     * Update assets (media) card
     *
     * @param asset
     * @param filePath
     * @return
     * @throws NotAuthorizedException
     * @throws IOException
     */
    public boolean setCard(String asset, String filePath) throws NotAuthorizedException, IOException {

        File file = new File(filePath);

        Response<JSONObject> response = (Response<JSONObject>) _node.request().upload("asset/card", asset, file);

        return response.success;
    }

    /**
     * Update assets (media) Media (cover)
     * @param asset
     * @param filePath
     * @return
     * @throws NotAuthorizedException
     * @throws IOException
     */
    public boolean setMedia(String asset, String filePath) throws NotAuthorizedException, IOException {

        File file = new File(filePath);

        Response<JSONObject> response = (Response<JSONObject>) _node.request().upload("asset/media", asset, file);

        return response.success;
    }


    public void parse(JSONObject obj) throws ParseException {

        this.name = obj.get("name").toString();
        this.publisher = obj.get("publisher").toString();

        if (obj.get("amount") != null)
            this.amount = Integer.parseInt(obj.get("amount").toString());

        if (obj.get("type") != null)
            this.type = obj.get("type").toString();

        this.description = obj.get("description") != null ? obj.get("description").toString() : "";

        this.mediaUrl = ((JSONObject)obj.get("media")).get("url").toString();
        this.card = ((JSONObject)obj.get("media")).get("card").toString();
        this.mediaType = ((JSONObject)obj.get("media")).get("type").toString();

        this.master = Boolean.parseBoolean(obj.get("master").toString());

        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSXXX");

        if (obj.get("inception") != null){
            this.inception = sdf.parse(obj.get("inception").toString());
        }

        if (obj.get("expiration") != null) {
            this.expiration = sdf.parse(obj.get("expiration").toString());
        }

        this.issuer = obj.get("issuer").toString();;
    }
}