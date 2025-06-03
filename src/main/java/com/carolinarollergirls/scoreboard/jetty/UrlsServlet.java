package com.carolinarollergirls.scoreboard.jetty;

import java.io.IOException;
import java.net.*;
import java.util.Collections;
import java.util.Set;
import java.util.TreeSet;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.common.base.Strings;
import com.google.common.collect.ImmutableList;
import org.eclipse.jetty.server.Connector;
import org.eclipse.jetty.server.NetworkConnector;
import org.eclipse.jetty.server.Server;

public class UrlsServlet extends HttpServlet {
    public UrlsServlet(Server s, String discoveryName) { server = s;
        this.discoveryName = discoveryName;
    }

    protected Iterable<String> getUrls(boolean skipLoopback) throws MalformedURLException, SocketException {
        Set<String> urls = new TreeSet<>();
        for (Connector c : server.getConnectors()) {
            if (c instanceof NetworkConnector) {
                addURLs(urls, ((NetworkConnector) c).getHost(), ((NetworkConnector) c).getLocalPort(), skipLoopback);
            }
        }
        return ImmutableList.sortedCopyOf(urls);
    }

    private int addressSort(String left, String right) {
        //noinspection StringEquality
        if (left == right) {
            return 0;
        }
        if (left != null && left.equals(right)) {
            return 0;
        }
        if (Strings.isNullOrEmpty(left)) {
            return 1;
        }
        if (Strings.isNullOrEmpty(right)) {
            return -1;
        }
        if (left.startsWith("http://127.0.0.1")) {
            return 1;
        }
        if (left.startsWith("http://[fe80")) {
            return 1;
        }
        if (left.matches("http://[^1-9]")) {
            return -1;
        }
        return left.compareTo(right);
    }

    protected void addURLs(Set<String> urls, String host, int port, boolean skipLoopback) throws MalformedURLException, SocketException {
        if (discoveryName != null) {
            urls.add(new URL("http", this.discoveryName + ".local", port, "/").toString());
        }
        if (null == host) {
            for (NetworkInterface iface : Collections.list(NetworkInterface.getNetworkInterfaces())) {
                for (InetAddress addr : Collections.list(iface.getInetAddresses())) {
                    if (addr.isMulticastAddress()) continue;
                    if (addr.isLinkLocalAddress()) continue;
                    if (skipLoopback && isLoopback(addr)) continue;

                    String hostAddress = addrToString(addr);
                    urls.add(new URL("http", hostAddress, port, "/").toString());
                }
            }
        } else {
            urls.add(new URL("http", host, port, "/").toString());
            try {
                // Get the IP address of the given host.
                urls.add(new URL("http", InetAddress.getByName(host).getHostAddress(), port, "/").toString());
            } catch (UnknownHostException uhE) {}
        }
    }

    private static String addrToString(InetAddress addr) {
        if (addr instanceof Inet6Address addr6) {
            return addr6.getHostAddress().replaceAll("%[^\\]]+", "");
        }
        return addr.getHostAddress();
    }

    private static boolean isLoopback(InetAddress addr) throws SocketException {
        if (addr.isLoopbackAddress()) {
            return true;
        }
        if (!(addr instanceof Inet6Address addr6)) {
            return false;
        }
        // skip link local address (fe80::) on loopback interface (e.g. fe80::1%lo0)
        NetworkInterface scopedInterface = addr6.getScopedInterface();
        if (scopedInterface != null) {
            return scopedInterface.isLoopback();
        }

        int scope = addr6.getScopeId();
        NetworkInterface idScopedInterface = NetworkInterface.getByIndex(scope);
        return idScopedInterface.isLoopback();
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        response.setHeader("Cache-Control", "no-cache");
        response.setHeader("Expires", "-1");
        response.setCharacterEncoding("UTF-8");

        try {
            response.setContentType("text/plain");
            for (String u : getUrls(true)) {
                response.getWriter().println(u);
            }
            response.setStatus(HttpServletResponse.SC_OK);
        } catch (MalformedURLException muE) {
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
                               "Could not parse internal URL : " + muE.getMessage());
        } catch (SocketException sE) {
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Socket Exception : " + sE.getMessage());
        }
    }

    protected Server server;
    private final String discoveryName;
}
