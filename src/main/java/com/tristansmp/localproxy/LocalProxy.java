package com.tristansmp.localproxy;

import com.tristansmp.localproxy.lib.APIClient;
import com.tristansmp.localproxy.lib.Cloudflared;
import com.tristansmp.localproxy.lib.Meta;
import net.fabricmc.api.ModInitializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LocalProxy implements ModInitializer {
    public static final Logger LOGGER = LoggerFactory.getLogger("local-proxy");
    public static final Cloudflared cloudflared = new Cloudflared();
    public static final APIClient api = new APIClient();
    public static final int LocalPort = 9997;

    @Override
    public void onInitialize() {
        Meta meta = LocalProxy.api.getMeta();

        LocalProxy.cloudflared.connect(meta.getUri(), String.valueOf(LocalPort));
    }
}