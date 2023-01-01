package com.tristansmp.localproxy.lib;

import com.tristansmp.localproxy.LocalProxy;

import java.io.File;

public class Cloudflared {
    private final Runtime rt = Runtime.getRuntime();
    public ConnectionState state = ConnectionState.DISCONNECTED;
    private boolean usingGlobalCloudflared = false;
    private String cloudflaredPath = "cloudflared";
    private Process process = null;

    public Cloudflared() {
        this.setup();

        rt.addShutdownHook(new Thread(() -> {
            this.disconnect();
        }));
    }

    private void setup() {
        this.state = ConnectionState.CONNECTING;
        try {
            try {
                LocalProxy.LOGGER.info("attempting to use cloudflared from /opt/homebrew/bin/cloudflared");

                rt.exec("/opt/homebrew/bin/cloudflared --version");
                usingGlobalCloudflared = true;
                cloudflaredPath = "/opt/homebrew/bin/cloudflared";
            } catch (Exception e) {
                LocalProxy.LOGGER.info("attempting to use cloudflared from global path");

                rt.exec("cloudflared --version");
                usingGlobalCloudflared = true;
            }

            LocalProxy.LOGGER.info("Using global cloudflared");
        } catch (Exception e) {
            LocalProxy.LOGGER.info("Using bundled cloudflared");

            File cloudflared = new File("cloudflared");

            if (!cloudflared.exists()) {
                LocalProxy.LOGGER.info("Downloading cloudflared");
                this.initBundledCloudflared();
            }

            cloudflared.setExecutable(true);

            LocalProxy.LOGGER.info("Cloudflared is ready");

            usingGlobalCloudflared = false;
        }
    }

    public void initBundledCloudflared() {
        String os = System.getProperty("os.name").toLowerCase();
        String arch = System.getProperty("os.arch").toLowerCase();
        String downloadUrl = "";

        if (os.contains("win")) {
            if (arch.contains("64")) {
                downloadUrl = Constants.CLOUDFLARED_WIN_64;
            } else {
                downloadUrl = Constants.CLOUDFLARED_WIN_32;
            }
        } else if (os.contains("linux")) {
            if (arch.contains("64")) {
                downloadUrl = Constants.CLOUDFLARED_LINUX_64;
            } else {
                downloadUrl = Constants.CLOUDFLARED_LINUX_32;
            }
        } else if (os.contains("mac")) {
            downloadUrl = Constants.CLOUDFLARED_MAC;
        } else {
            this.state = ConnectionState.DISCONNECTED;
            throw new RuntimeException("Unsupported OS");
        }

        LocalProxy.LOGGER.info("Downloading cloudflared from " + downloadUrl);

        download(downloadUrl);
    }

    private void download(String downloadUrl) {
        try {
            Process p = rt.exec("curl -L " + downloadUrl + " -o cloudflared");
            p.waitFor();
        } catch (Exception e) {
            this.state = ConnectionState.DISCONNECTED;

            LocalProxy.LOGGER.error("Failed to download cloudflared");
            LocalProxy.LOGGER.error(e.getMessage());
        }
    }

    public void connect(String url, String localPort) {
        destroyProcIfExists();

        try {
            if (usingGlobalCloudflared) {
                process = rt.exec(cloudflaredPath + " access tcp --hostname " + url + " --url localhost:" + localPort);
            } else {
                process = rt.exec("./cloudflared access tcp --hostname " + url + " --url localhost:" + localPort);
            }

            this.state = ConnectionState.CONNECTED;

        } catch (Exception e) {
            this.state = ConnectionState.DISCONNECTED;

            LocalProxy.LOGGER.error("Failed to connect to cloudflared");
            LocalProxy.LOGGER.error(e.getMessage());
        }
    }

    public void disconnect() {
        destroyProcIfExists();
        this.state = ConnectionState.DISCONNECTED;
    }

    private void destroyProcIfExists() {
        if (this.process != null) {
            this.process.destroy();
        }
    }

    public boolean isConnected() {
        return this.state == ConnectionState.CONNECTED;
    }
}
