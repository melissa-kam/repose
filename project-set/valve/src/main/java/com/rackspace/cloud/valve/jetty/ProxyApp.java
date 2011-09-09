package com.rackspace.cloud.valve.jetty;

import com.rackspace.cloud.valve.jetty.servlet.ProxyServlet;
import com.rackspace.papi.components.clientauth.ClientAuthenticationFilter;
import com.rackspace.papi.servlet.InitParameter;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.Map;
import javax.servlet.DispatcherType;
import org.apache.commons.httpclient.URI;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;

import org.springframework.web.context.ContextLoaderListener;

/**
 *
 * @author zinic
 */
public class ProxyApp {

    private final Server jettyServerReference;
    private final ServletContextHandler rootContext;

    public ProxyApp(int port, String targetHost) throws Exception {
        jettyServerReference = new Server(port);
        jettyServerReference.setSendServerVersion(false);

        rootContext = buildRootContext(jettyServerReference);

        final EnumSet<DispatcherType> dispatchers = EnumSet.allOf(DispatcherType.class);

        rootContext.addFilter(ClientAuthenticationFilter.class, "/*", dispatchers);
        rootContext.addServlet(new ServletHolder(new ProxyServlet(new URI(targetHost))), "/*");
    }

    private ServletContextHandler buildRootContext(Server serverReference) {
        final Map<String, String> initParams = new HashMap<String, String>();
        initParams.put(InitParameter.POWER_API_CONFIG_DIR.getParameterName(), "/etc/powerapi");
        

        final ServletContextHandler servletContext = new ServletContextHandler(serverReference, "/");

        servletContext.getInitParams().putAll(initParams);

        servletContext.addEventListener(new ContextLoaderListener());

        return servletContext;
    }

    public void go() throws Exception {
        jettyServerReference.start();
    }

    public static void main(String[] args) throws Exception {
        if (args.length > 0) {
            new ProxyApp(8088, args[0]).go();
        } else {
            System.out.println("Expecting single argument representing the host URL to forward requests too");
            new ProxyApp(8088, "http://173.203.238.19:8080").go();
        }
    }
}