package org.juicechain.models;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

/**
 * Node Telemetry infos
 */
public class Telemetry {

    public double cpuUsage;
    public double memoryUsed;
    public double memoryAvailable;

    public long requestTime;
    public long requestsPerMinute;
    public long requestsPerSecond;

    public long lastBlock;

    public boolean parse(String json){
        JSONParser jsonParser = new JSONParser();
        JSONObject result = null;
        try {
            result = (JSONObject) jsonParser.parse(json);
        } catch (ParseException e) {
            return false;
        }

        // system
        this.cpuUsage = Double.parseDouble(((JSONObject) result.get("cpu")).get("average").toString());
        this.memoryAvailable = Double.parseDouble (((JSONObject) result.get("memory")).get("total").toString());
        this.memoryUsed = Double.parseDouble(((JSONObject) result.get("memory")).get("uses").toString());

        // net
        this.requestTime = Long.parseLong(result.get("requestTime").toString());
        this.requestsPerMinute = Long.parseLong(result.get("requestsPerMinute").toString());
        this.requestsPerSecond = Long.parseLong(result.get("requestsPerSecond").toString());

        // Transactions
        JSONArray objs = (JSONArray) result.get("blocks");
        this.lastBlock = Long.parseLong(((JSONObject) objs.get(4)).get("height").toString());

        return true;
    }

    public void prettyPrint(){
        System.out.println("CPU Average: " + this.cpuUsage);
        System.out.println("Memory Used: " + this.memoryUsed);
        System.out.println("Memory Available: " + this.memoryAvailable);
        System.out.println("Request Time: " + this.requestTime);
        System.out.println("Requests Per Minute: " + this.requestsPerMinute);
        System.out.println("Requests Per Second: " + this.requestsPerSecond);
        System.out.println("Last Block: " + this.lastBlock);
    }

}
