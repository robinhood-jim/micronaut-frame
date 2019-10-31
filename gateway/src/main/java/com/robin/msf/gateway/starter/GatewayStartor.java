package com.robin.msf.gateway.starter;

import com.netflix.zuul.http.ZuulServlet;
import com.robin.msf.gateway.config.ZuulConfig;
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
    public static void main(String[] args){
        Micronaut.run(GatewayStartor.class);
        ZuulConfig config=new ZuulConfig();

        ZuulServlet servlet=new ZuulServlet();
        try {
            servlet.init(new ServletConfig() {
                @Override
                public String getServletName() {
                    return null;
                }

                @Override
                public ServletContext getServletContext() {
                    return null;
                }

                @Override
                public String getInitParameter(String s) {
                    return "false";
                }

                @Override
                public Enumeration getInitParameterNames() {
                    return null;
                }
            });
        }catch (Exception ex){

        }
        System.out.println(servlet);
    }
}
