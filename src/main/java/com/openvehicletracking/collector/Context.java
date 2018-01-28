package com.openvehicletracking.collector;

import com.openvehicletracking.core.protocol.ProtocolChain;
import com.openvehicletracking.core.protocol.impl.ProtocolChainImpl;
import com.openvehicletracking.protocols.gt100.Gt100Protocol;
import com.openvehicletracking.protocols.xtakip.XTakipProtocol;
import io.vertx.core.json.JsonObject;

public class Context {

    private static Config config;
    private static ProtocolChain protocolChain = new ProtocolChainImpl();


    public static Config getConfig() {
        return config;
    }

    public static ProtocolChain getProtocolChain() {
        return protocolChain;
    }

    public static void init(String args[], JsonObject conf) {
        config = Config.getInstance().load(conf);
        protocolChain.add(new DummyProtocol());
        protocolChain.add(new Gt100Protocol());
        protocolChain.add(new XTakipProtocol());
    }


}
