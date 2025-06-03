package com.carolinarollergirls.scoreboard.discovery;

import com.carolinarollergirls.scoreboard.utils.Logger;

import javax.jmdns.JmDNS;
import javax.jmdns.JmmDNS;
import javax.jmdns.ServiceInfo;
import javax.jmdns.impl.JmDNSImpl;
import javax.jmdns.impl.JmmDNSImpl;
import java.io.IOException;
import java.net.InetAddress;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CompletableFuture;

public class Discovery {
    private static final String fallbackMdnsName = "scoreboard";

    private final JmmDNS jmmdns;
    private final int port;
    private final String name;

    public Discovery(int port) {
        this(port, fallbackMdnsName);
    }

    public Discovery(int port, String mdnsIncomingName) {
        String mdnsStrippedName = mdnsIncomingName.replaceAll("^[0-9a-zA-Z\\-]", "");
        this.name = mdnsStrippedName.isEmpty() ? fallbackMdnsName : mdnsStrippedName;
        this.port = port;

        JmmDNS.Factory.setClassDelegate(() -> new NamedJmmDNSImpl(this.name));
        jmmdns = JmmDNS.Factory.getInstance();
    }

    public void start() {
        CompletableFuture.runAsync(this::internalStart);
    }

    public void internalStart() {
        List<ServiceInfo> services = Arrays.asList(
                ServiceInfo.create("_http._tcp.local.", "Scoreboard Index", "_scoreboard", port, "path=/"),
                ServiceInfo.create("_http._tcp.local.", "Scoreboard Main", "_scoreboard", port, "path=/views/standard/"),
                ServiceInfo.create("_http._tcp.local.", "scoreboard Operator Panel", "_scoreboard", port, "path=/nso/sbo/"),
                ServiceInfo.create("_http._tcp.local.", "scoreboard Broadcast Overlay", "_scoreboard", port, "path=/views/overlay/")
        );

        boolean success = false;
        for (ServiceInfo service : services) {
            try {
                jmmdns.registerService(service);
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
        try {
            jmmdns.unregisterAllServices();
            jmmdns.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private static class NamedJmmDNSImpl extends JmmDNSImpl {
        private final String name;

        private NamedJmmDNSImpl(String name) {
            this.name = name;
        }

        @Override
        protected JmDNS createJmDnsInstance(InetAddress address) throws IOException {
            return new JmDNSImpl(address, name);
        }
    }
}
