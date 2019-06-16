package org.juicechain.helpers;

import org.json.simple.JSONAware;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

public class Response<T extends JSONAware> {

    public boolean success;
    public T payload;
    public String error;

    public Response(String responseString){
        JSONParser jsonParser = new JSONParser();
        try {
            JSONObject result = (JSONObject) jsonParser.parse(responseString);

            this.success = Boolean.parseBoolean(result.get("success").toString());

            if (this.success){
                this.payload = (T) result.get("payload");
            }

            if (result.get("error") != null)
                this.error = result.get("error").toString();

        } catch (ParseException ex){
            this.success = false;
            this.error = "Failed parsing response body";
        }
    }

}
