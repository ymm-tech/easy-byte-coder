package io.manbang.easyByteCoder.plugin.acceleratepigeon.mock.runtime;

import com.dianping.pigeon.domain.HostInfo;
import com.dianping.pigeon.registry.RegistryManager;
import com.dianping.pigeon.registry.domain.ServiceAddressInfo;
import com.dianping.pigeon.registry.listener.RegistryEventListener;
import com.dianping.pigeon.remoting.ServiceFactory;
import com.dianping.pigeon.remoting.common.codec.SerializerFactory;
import com.dianping.pigeon.remoting.common.exception.RpcException;
import com.dianping.pigeon.remoting.common.util.Constants;
import com.dianping.pigeon.remoting.invoker.ClientManager;
import com.dianping.pigeon.remoting.invoker.InvokerBootStrap;
import com.dianping.pigeon.remoting.invoker.config.InvokerConfig;
import com.dianping.pigeon.remoting.invoker.exception.ServiceUnavailableException;
import com.dianping.pigeon.remoting.invoker.route.balance.LoadBalanceManager;
import com.dianping.pigeon.threadpool.DefaultThreadPool;
import com.dianping.pigeon.threadpool.ThreadPool;
import org.apache.commons.lang.StringUtils;

import java.util.Collections;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;

public class PigeonMockHandle {

    private static ThreadPool registerThreadPool = new DefaultThreadPool("Pigeon-Client-Register-Pool",
            100, 100, new LinkedBlockingQueue<Runnable>(10000),
            new ThreadPoolExecutor.CallerRunsPolicy());


    public static <T> T getProxy(InvokerConfig<T> invokerConfig, Map<InvokerConfig<?>, Object> services) {

        long start = System.currentTimeMillis();

        if (invokerConfig.getServiceInterface() == null) {
            throw new IllegalArgumentException("service interface is required");
        }
        if (StringUtils.isBlank(invokerConfig.getUrl())) {
            invokerConfig.setUrl(ServiceFactory.getServiceUrl(invokerConfig));
        }
        if (!StringUtils.isBlank(invokerConfig.getProtocol())
                && !invokerConfig.getProtocol().equalsIgnoreCase(Constants.PROTOCOL_DEFAULT)) {
            String protocolPrefix = "@" + invokerConfig.getProtocol().toUpperCase() + "@";
            if (!invokerConfig.getUrl().startsWith(protocolPrefix)) {
                invokerConfig.setUrl(protocolPrefix + invokerConfig.getUrl());
            }
        }
        Object service = null;
        service = services.get(invokerConfig);
        if (service == null) {
            try {
                InvokerBootStrap.startup();
                service = SerializerFactory.getSerializer(invokerConfig.getSerialize()).proxyRequest(invokerConfig);
                if (StringUtils.isNotBlank(invokerConfig.getLoadbalance())) {
                    LoadBalanceManager.register(invokerConfig.getUrl(), invokerConfig.getGroup(),
                            invokerConfig.getLoadbalance());
                }
            } catch (Throwable t) {
                throw new RpcException("error while trying to get service:" + invokerConfig, t);
            }
            services.put(invokerConfig, service);
        }

        long end = System.currentTimeMillis();
        System.out.println("time:" + (end - start));
        return (T) service;
    }


    public static Set<HostInfo> registerClients(ClientManager clientManager,String serviceName, String group, String vip) {

        String localHost = null;
        if (vip != null && vip.startsWith("console:")) {
            localHost = "192.168.31.145" + vip.substring(vip.indexOf(":"));
        }
        ServiceAddressInfo serviceAddressInfo = clientManager.getServiceAddress(serviceName, group, vip);
        String serviceAddress = serviceAddressInfo.getAddress();
        String[] addressArray = serviceAddress.split(",");
        Set<HostInfo> addresses = Collections.newSetFromMap(new ConcurrentHashMap<HostInfo, Boolean>());
        for (int i = 0; i < addressArray.length; i++) {
            if (StringUtils.isNotBlank(addressArray[i])) {
                String address = addressArray[i];
                int idx = address.lastIndexOf(":");
                if (idx != -1) {
                    String host = null;
                    int port = -1;
                    try {
                        host = address.substring(0, idx);
                        port = Integer.parseInt(address.substring(idx + 1));
                    } catch (RuntimeException e) {

                    }
                    if (host != null && port > 0) {
                        if (localHost != null && !localHost.equals(host + ":" + port)) {
                            continue;
                        }
                        try {
                            int weight = RegistryManager.getInstance().getServiceWeight(address, false);
                            addresses.add(new HostInfo(host, port, weight));
                        } catch (Throwable e) {

                            throw new ServiceUnavailableException("error while registering service invoker:"
                                    + serviceName + ", address:" + address + e);
                        }
                    }
                } else {

                }
            }
        }
        final String url = serviceName;
        final String currentGroup = serviceAddressInfo.getGroup();


        for (final HostInfo hostInfo : addresses) {
            Runnable r = new Runnable() {

                @Override
                public void run() {

                    RegistryEventListener.providerAdded(url, hostInfo.getHost(), hostInfo.getPort(),
                            hostInfo.getWeight(), currentGroup);
                    RegistryEventListener.serverInfoChanged(url, hostInfo.getConnect());
                }

            };
            registerThreadPool.submit(r);
        }

        return addresses;
    }
}
