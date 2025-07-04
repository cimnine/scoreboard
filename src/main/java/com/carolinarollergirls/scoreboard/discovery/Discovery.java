package com.carolinarollergirls.scoreboard.discovery;

import com.carolinarollergirls.scoreboard.utils.Logger;
import com.google.common.base.Strings;

import javax.jmdns.JmDNS;
import javax.jmdns.ServiceInfo;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;

public class Discovery {
    private JmDNS jmdns;
    private final int port;
    private final String name;

    public Discovery(int port, String mdnsIncomingName) {
        String mdnsStrippedName = mdnsIncomingName.replaceAll("[^0-9a-zA-Z\\-]", "");
        this.name = Strings.isNullOrEmpty(mdnsStrippedName) ? "scoreboard" : mdnsStrippedName;
        this.port = port;
    }

    public boolean start() {
        try {
            jmdns = JmDNS.create(this.name);
        } catch (IOException e) {
            Logger.printStackTrace(e);
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
            return true;
        } else {
            Logger.printMessage("Couldn't register any service for advertising via mDNS.");
            return false;
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
