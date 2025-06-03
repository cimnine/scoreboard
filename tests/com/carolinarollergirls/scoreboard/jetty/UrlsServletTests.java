package com.carolinarollergirls.scoreboard.jetty;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.lang.reflect.Proxy;
import java.net.Inet6Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.util.Set;
import java.util.TreeSet;
import java.util.concurrent.atomic.AtomicInteger;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.eclipse.jetty.server.Server;
import org.junit.Test;

public class UrlsServletTests {
    @Test
    public void recognizesIPv4AndIPv6Loopback() throws Exception {
        assertTrue(UrlsServlet.isLoopback(InetAddress.getByName("127.0.0.1")));
        assertTrue(UrlsServlet.isLoopback(InetAddress.getByName("::1")));
        assertFalse(UrlsServlet.isLoopback(InetAddress.getByName("203.0.113.1")));
    }

    @Test
    public void unscopedIPv6DoesNotRequireAnInterface() throws Exception {
        assertFalse(UrlsServlet.isLoopback(InetAddress.getByName("2001:db8::1")));
        assertFalse(UrlsServlet.isLoopback(InetAddress.getByName("fe80::1")));
    }

    @Test
    public void linkLocalAddressOnLoopbackInterfaceIsLoopback() throws Exception {
        NetworkInterface loopback = NetworkInterface.getByInetAddress(InetAddress.getByName("127.0.0.1"));
        byte[] linkLocal = InetAddress.getByName("fe80::1").getAddress();
        Inet6Address scoped = Inet6Address.getByAddress(null, linkLocal, loopback.getIndex());
        assertTrue(UrlsServlet.isLoopback(scoped));
    }

    @Test
    public void localClientsKeepLoopbackUrls() throws Exception {
        assertTrue(urlResponse("127.0.0.1").contains("http://127.0.0.1:8000/"));
        assertTrue(urlResponse("[::1]").contains("http://127.0.0.1:8000/"));
    }

    @Test
    public void remoteClientsDoNotReceiveLoopbackUrls() throws Exception {
        String response = urlResponse("203.0.113.1");
        assertFalse(response.contains("127.0.0.1"));
        assertTrue(response.contains("http://203.0.113.10:8000/"));
        assertFalse(urlResponse("2001:db8::1").contains("127.0.0.1"));
    }

    @Test
    public void preservesIPv6UrlFormattingAndDiscoveryName() throws Exception {
        UrlsServlet servlet = new UrlsServlet(new Server(), "scoreboard-test");
        Set<String> urls = new TreeSet<>();
        servlet.addURLs(urls, "2001:db8::1", 8000, true);
        assertTrue(urls.contains("http://[2001:db8::1]:8000/"));
        assertTrue(urls.contains("http://scoreboard-test.local:8000/"));
    }

    private String urlResponse(String remoteAddress) throws Exception {
        UrlsServlet servlet = new UrlsServlet(new Server(), null) {
            @Override
            protected Set<String> getUrls(boolean skipLoopback) {
                Set<String> urls = new TreeSet<>();
                urls.add("http://203.0.113.10:8000/");
                if (!skipLoopback) { urls.add("http://127.0.0.1:8000/"); }
                return urls;
            }
        };
        HttpServletRequest request = (HttpServletRequest) Proxy.newProxyInstance(
            getClass().getClassLoader(), new Class<?>[] { HttpServletRequest.class },
            (proxy, method, args) -> {
                if (method.getName().equals("getRemoteAddr")) { return remoteAddress; }
                throw new UnsupportedOperationException(method.getName());
            });
        StringWriter body = new StringWriter();
        AtomicInteger status = new AtomicInteger();
        HttpServletResponse response = (HttpServletResponse) Proxy.newProxyInstance(
            getClass().getClassLoader(), new Class<?>[] { HttpServletResponse.class },
            (proxy, method, args) -> {
                if (method.getName().equals("getWriter")) { return new PrintWriter(body); }
                if (method.getName().equals("setStatus")) { status.set((Integer) args[0]); }
                return null;
            });
        servlet.doGet(request, response);
        assertEquals(HttpServletResponse.SC_OK, status.get());
        return body.toString();
    }
}
