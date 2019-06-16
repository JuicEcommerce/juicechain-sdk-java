package org.juicechain;

import org.juicechain.managed.Node;

public class JuicEchain {

    public static Node getNode(String node, String username, String apiKey){
        return new Node(node, username, apiKey);
    }

}
