package com.robin.msf.gateway.factory;

import com.robin.msf.gateway.config.GatewayProperties;
import io.micronaut.context.annotation.Factory;
import io.micronaut.http.client.LoadBalancer;
import io.micronaut.http.client.loadbalance.DiscoveryClientLoadBalancerFactory;

import javax.inject.Singleton;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;


@Factory
public class GatewayLoadBalancersFactory {
    @Singleton
    public Map<String, LoadBalancer> serviceLoadBalancer(GatewayProperties properties, DiscoveryClientLoadBalancerFactory factory){
        Set<String> services=properties.getServices();
        Map<String,LoadBalancer> loadBalancerMap=new HashMap<>();
        services.forEach(serviceName->loadBalancerMap.put(serviceName,factory.create(serviceName)));
        return Collections.unmodifiableMap(loadBalancerMap);
    }
}
