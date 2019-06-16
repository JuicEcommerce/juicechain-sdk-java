package org.juicechain.helpers;

import okhttp3.*;
import org.json.simple.JSONAware;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.juicechain.exceptions.NotAuthorizedException;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.util.concurrent.TimeUnit;

public class RequestHelper {

    private String node;
    private String username;
    private String apiKey;

    public RequestHelper(String node, String username, String apiKey){
        this.node = node;
        this.username = username;
        this.apiKey = apiKey;
    }

    public <T extends JSONAware> Response get(String path) throws NotAuthorizedException, IOException {
        String token = this.requestToken();

        if (token == null)
            throw new NotAuthorizedException();

        String url = "https://" + this.node + ".juicechain.org/" + path;

        return new Response<T> (this.executeRequest(createGetRequest(url, token, "")));
    }

    public <T extends JSONAware> Response post(String path, String body, String signature) throws NotAuthorizedException, IOException {
        String token = this.requestToken();

        if (token == null)
            throw new NotAuthorizedException();

        String url = "https://" + this.node + ".juicechain.org/node/" + path;

        MediaType JSON = MediaType.parse("application/json; charset=utf-8");
        RequestBody JSONBody = RequestBody.create(JSON, body);

        return new Response<T> (this.executeRequest(createPostRequest(url, JSONBody, token, signature)));
    }

    public <T extends JSONAware> Response put(String path, String body, String signature) throws NotAuthorizedException, IOException {
        String token = this.requestToken();

        if (token == null)
            throw new NotAuthorizedException();

        String url = "https://" + this.node + ".juicechain.org/node/" + path;

        MediaType JSON = MediaType.parse("application/json; charset=utf-8");
        RequestBody JSONBody = RequestBody.create(JSON, body);

        return new Response<T> (this.executeRequest(createPutRequest(url, JSONBody, token, signature)));
    }

    public <T extends JSONAware> Response upload(String path, String asset, File file) throws NotAuthorizedException, IOException {
        String token = this.requestToken();

        if (token == null)
            throw new NotAuthorizedException();

        String url = "https://" + this.node + ".juicechain.org/node/" + path;

        RequestBody body = new MultipartBody.Builder().setType(MultipartBody.FORM)
                .addFormDataPart("name", asset)
                .addFormDataPart("buffer", file.getName(), okhttp3.RequestBody.create( okhttp3.MediaType.parse("image/png"), file) )
                .build();

        return new Response<T> (this.executeRequest(createPutRequest(url, body, token, "")));
    }


    private static String handleResponse(HttpURLConnection conn) throws IOException, NotAuthorizedException {
        int response = conn.getResponseCode();
        if (response == 404) {
            conn.disconnect();
            return null;
        } else if (response == 400) {
            conn.disconnect();
            throw new NotAuthorizedException();
        } else {
            return extractBody(conn);
        }
    }

    private static String extractBody(HttpURLConnection conn) throws IOException {
        BufferedReader br = new BufferedReader(new InputStreamReader((conn.getInputStream())));
        String output;
        String result = "";
        while ((output = br.readLine()) != null) {
            result += output;
        }
        conn.disconnect();
        return result;
    }

    private static OkHttpClient getClient(){
        return new OkHttpClient.Builder()
                .connectTimeout(10, TimeUnit.SECONDS)
                .writeTimeout(180, TimeUnit.SECONDS)
                .readTimeout(180, TimeUnit.SECONDS)
                .build();
    }

    private static Request createPutRequest(String url, RequestBody body, String authorization, String signature){
        return new Request.Builder()
                .url(url)
                .addHeader("authorization", authorization)
                .addHeader("signature", signature)
                .put(body)
                .build();
    }

    private static Request createPostRequest(String url, RequestBody body, String authorization, String signature){
        return new Request.Builder()
                .url(url)
                .addHeader("authorization", authorization)
                .addHeader("signature", signature)
                .post(body)
                .build();
    }

    private static Request createGetRequest(String url, String authorization, String signature){
        return new Request.Builder()
                .url(url)
                .addHeader("authorization", authorization)
                .addHeader("signature", signature)
                .get()
                .build();
    }

    private static String executeRequest(Request request) throws IOException, NotAuthorizedException {
        OkHttpClient client = getClient();
        okhttp3.Response response = client.newCall(request).execute();

        if (response.code() == 404) {
            return null;
        } else if (response.code()  == 400) {
            throw new NotAuthorizedException();
        } else {
            return response.body().string();
        }
    }


    private String requestToken() throws NotAuthorizedException, IOException {

        JSONObject auth = new JSONObject();
        auth.put("username", this.username);
        auth.put("key", this.apiKey);

        String url = "https://" + this.node + ".juicechain.org/node/auth";

        MediaType JSON = MediaType.parse("application/json; charset=utf-8");
        RequestBody JSONBody = RequestBody.create(JSON, auth.toJSONString());

        String response =  this.executeRequest(createPostRequest(url, JSONBody, "", ""));

        if (response == null)
            throw new NotAuthorizedException("Invalid credentials");

        JSONParser jsonParser = new JSONParser();
        JSONObject result = null;
        try {
            result = (JSONObject) jsonParser.parse(response);
        } catch (ParseException e) {
            throw new IOException("Failed parsing JSON");
        }

        if (result.get("success").equals(true)){
            return result.get("token").toString();
        } else {
            return null;
        }

    }

}
