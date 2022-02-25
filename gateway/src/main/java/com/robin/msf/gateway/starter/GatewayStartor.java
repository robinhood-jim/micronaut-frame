package com.robin.msf.gateway.starter;

import com.netflix.zuul.http.ZuulServlet;
import io.micronaut.aop.Around;
import io.micronaut.runtime.Micronaut;

import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;
import java.util.Enumeration;

/**
 * <p>Created at: 2019-10-17 17:38:10</p>
 *
 * @author robinjim
 * @version 1.0
 */
@Around
public class GatewayStartor {
    public static void main(String[] args) {
        Micronaut.run(GatewayStartor.class);
    }
}
