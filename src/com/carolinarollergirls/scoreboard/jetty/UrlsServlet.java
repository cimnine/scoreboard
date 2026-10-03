package com.carolinarollergirls.scoreboard.jetty;

import java.io.IOException;
import java.net.InetAddress;
import java.net.Inet6Address;
import java.net.MalformedURLException;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.net.URISyntaxException;
import java.net.UnknownHostException;
import java.util.Collections;
import java.util.Set;
import java.util.TreeSet;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.eclipse.jetty.server.Connector;
import org.eclipse.jetty.server.NetworkConnector;
import org.eclipse.jetty.server.Server;

/** This Servlet provides the list of URLs of the server for display on the frontend */
public class UrlsServlet extends HttpServlet {
    public UrlsServlet(Server s, String discoveryName) { server = s;
        this.discoveryName = discoveryName;
    }

    public Set<String> getUrls() throws MalformedURLException, SocketException {
        return getUrls(true);
    }

    protected Set<String> getUrls(boolean skipLoopback) throws MalformedURLException, SocketException {
        Set<String> urls = new TreeSet<>();
        for (Connector c : server.getConnectors()) {
            if (c instanceof NetworkConnector) {
                addURLs(urls, ((NetworkConnector) c).getHost(), ((NetworkConnector) c).getLocalPort(), skipLoopback);
            }
        }
        return urls;
    }

    private static void addURL(Set<String> urls, String host, int port) {
        try {
            // Try to add.  If for some reason we fail, just ignore this host.
            urls.add((new java.net.URI("http", null, host, port, "/", null, null).toURL()).toString());
        } catch (URISyntaxException uriEx) {
        } catch (MalformedURLException muE) {}
    }

    protected void addURLs(Set<String> urls, String host, int port, boolean skipLoopback) throws MalformedURLException, SocketException {
        if (discoveryName != null) {
            addURL(urls, discoveryName + ".local", port);
        }
        if (null == host) {
            for (NetworkInterface iface : Collections.list(NetworkInterface.getNetworkInterfaces())) {
                for (InetAddress addr : Collections.list(iface.getInetAddresses())) {
                    if (addr.isMulticastAddress()) continue;
                    if (skipLoopback && isLoopback(addr)) continue;

                    addURL(urls, addr.getHostAddress(), port);
                }
            }
        } else {
            addURL(urls, host, port);
            try {
                // Get the IP address of the given host.
                addURL(urls, InetAddress.getByName(host).getHostAddress(), port);
            } catch (UnknownHostException uhE) {}
        }
    }

    static boolean isLoopback(InetAddress addr) throws SocketException {
        if (addr.isLoopbackAddress()) {
            return true;
        }
        if (!(addr instanceof Inet6Address)) {
            return false;
        }
        Inet6Address addr6 = (Inet6Address) addr;
        // skip link local address (fe80::) on loopback interface (e.g. fe80::1%lo0)
        NetworkInterface scopedInterface = addr6.getScopedInterface();
        if (scopedInterface != null) {
            return scopedInterface.isLoopback();
        }

        int scope = addr6.getScopeId();
        NetworkInterface idScopedInterface = NetworkInterface.getByIndex(scope);
        return idScopedInterface != null && idScopedInterface.isLoopback();
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        response.setHeader("Cache-Control", "no-cache");
        response.setHeader("Expires", "-1");
        response.setCharacterEncoding("UTF-8");

        String remoteAddr = request.getRemoteAddr();

        // ipv6 addresses come in the form of "[fe80::1%lo0]"
        if (remoteAddr.startsWith("[")) {
            remoteAddr = remoteAddr.substring(1, remoteAddr.length() - 1);
        }
        InetAddress byName = InetAddress.getByName(remoteAddr);
        boolean dontSkipLocalhostWhenConnectedViaLocalhost = isLoopback(byName);

        try {
            response.setContentType("text/plain");
            for (String u : getUrls(!dontSkipLocalhostWhenConnectedViaLocalhost)) {
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
