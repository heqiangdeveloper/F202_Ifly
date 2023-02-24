package com.iflytek.adapter;

public class PlatformHelp {
    private static PlatformHelp platformHelp;
    private PlatformClientListener platformClient;

    public PlatformHelp() {
    }

    public static PlatformHelp getInstance() {
        if (platformHelp == null) {
            platformHelp = new PlatformHelp();
        }

        return platformHelp;
    }

    public PlatformClientListener getPlatformClient() {
        return this.platformClient;
    }

    public void setPlatformClient(PlatformClientListener platformClient) {
        this.platformClient = platformClient;
    }
}