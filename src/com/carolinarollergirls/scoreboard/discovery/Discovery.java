package com.carolinarollergirls.scoreboard.discovery;

import com.carolinarollergirls.scoreboard.utils.Logger;

import javax.jmdns.JmDNS;
import javax.jmdns.ServiceInfo;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CompletableFuture;

public class Discovery {
    private static final String fallbackMdnsName = "scoreboard";

    private JmDNS jmdns;
    private final int port;
    private final String name;

    public Discovery(int port) {
        this(port, fallbackMdnsName);
    }

    public Discovery(int port, String mdnsIncomingName) {
        String mdnsStrippedName = mdnsIncomingName.replaceAll("[^0-9a-zA-Z\\-]", "");
        this.name = mdnsStrippedName.isEmpty() ? fallbackMdnsName : mdnsStrippedName;
        this.port = port;
    }

    public void start() {
        CompletableFuture.runAsync(this::internalStart);
    }

    public void internalStart() {
        try {
            jmdns = JmDNS.create(this.name);
        } catch (IOException e) {
            Logger.printStackTrace("mDNS discovery", e);
            Logger.printMessage("Couldn't register any service for advertising via mDNS.");
        }

        List<ServiceInfo> services = Arrays.asList(
                ServiceInfo.create("_http._tcp.local.", "Scoreboard Index", "_scoreboard", port, "path=/"),
                ServiceInfo.create("_http._tcp.local.", "Scoreboard Main", "_main._scoreboard", port, "path=/views/standard/"),
                ServiceInfo.create("_http._tcp.local.", "Scoreboard Operator Panel", "_operator._scoreboard", port, "path=/nso/sbo/"),
                ServiceInfo.create("_http._tcp.local.", "scoreboard Broadcast Overlay", "_broadcast._scoreboard", port, "path=/views/overlay/")
        );

        boolean success = false;
        for (ServiceInfo service : services) {
            try {
                jmdns.registerService(service);
                success = true;
            } catch (IOException e) {
                Logger.printMessage("Can't register '" + service.getDomain() + "' for advertisement via mDNS");
            }
        }

        if (success) {
            Logger.printMessage("Advertising via mDNS: http://scoreboard.local:" + port);
        } else {
            Logger.printMessage("Couldn't register any service for advertising via mDNS.");
        }
    }

    public void stop() {
        if (this.jmdns == null) {
            return;
        }

        try {
            jmdns.unregisterAllServices();
            jmdns.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
