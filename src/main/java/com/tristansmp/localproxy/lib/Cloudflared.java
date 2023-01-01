package com.tristansmp.localproxy.lib;

import com.tristansmp.localproxy.LocalProxy;

import java.io.File;

public class Cloudflared {

    private final Runtime rt = Runtime.getRuntime();
    private boolean usingGlobalCloudflared = false;
    private String cloudflaredPath = "cloudflared";
    private Process process = null;

    public Cloudflared() {
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
            throw new RuntimeException("Unsupported OS");
        }

        LocalProxy.LOGGER.info("Downloading cloudflared from " + downloadUrl);

        try {
            rt.exec("curl -L " + downloadUrl + " -o cloudflared");
        } catch (Exception e) {
            LocalProxy.LOGGER.error("Failed to download cloudflared");
        }
    }

    public void downloadBundledCloudflared(String url) {
        try {
            LocalProxy.LOGGER.info("Downloading bundled cloudflared");
            rt.exec("curl -L " + url + " -o cloudflared");
            rt.exec("chmod +x cloudflared");
        } catch (Exception e) {
            LocalProxy.LOGGER.error("Failed to download bundled cloudflared");
        }
    }

    public void connect(String url, String localPort) {
        if (this.process != null) {
            this.process.destroy();
        }

        try {
            if (usingGlobalCloudflared) {
                rt.exec("cloudflared access tcp --hostname " + url + " --url localhost:" + localPort);
            } else {
                process = rt.exec("./cloudflared access tcp --hostname " + url + " --url localhost:" + localPort);
            }
        } catch (Exception e) {
            LocalProxy.LOGGER.error("Failed to connect to cloudflared");
        }
    }

    public void disconnect() {
        if (this.process != null) {
            this.process.destroy();
        }
    }
}
