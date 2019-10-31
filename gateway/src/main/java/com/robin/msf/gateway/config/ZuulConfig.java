package com.robin.msf.gateway.config;

import com.netflix.zuul.ZuulFilter;
import com.netflix.zuul.context.RequestContext;
import com.netflix.zuul.filters.FilterRegistry;
import com.netflix.zuul.http.ZuulServlet;
import io.micronaut.aop.Around;
import io.micronaut.context.annotation.Bean;
import io.micronaut.context.annotation.Factory;

import javax.annotation.PostConstruct;
import javax.inject.Named;
import javax.inject.Singleton;

@Factory

public class ZuulConfig {
    FilterRegistry registry;

    @Singleton
    @Bean
    public ZuulServlet getServlet(){
        return new ZuulServlet();
    }

    @Singleton
    @Bean
    public ZuulFilter getZuulFliter(){
        return new ZuulFilter() {
            @Override
            public String filterType() {
                return "pre";
            }

            @Override
            public int filterOrder() {
                return 4000;
            }

            @Override
            public boolean shouldFilter() {
                return true;
            }

            @Override
            public Object run() {
                RequestContext.getCurrentContext().set("javaPreFilter-ran",true);
                return null;
            }
        };
    }
    @PostConstruct
    public void getFilterRegistry(@Named ZuulFilter zuulFilter,@Named ZuulServlet servlet){
        FilterRegistry registry=FilterRegistry.instance();
        registry.put("javaPreFilter",zuulFilter);

    }

}
