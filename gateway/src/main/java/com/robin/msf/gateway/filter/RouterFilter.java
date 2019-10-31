package com.robin.msf.gateway.filter;

import io.micronaut.http.HttpRequest;
import io.micronaut.http.HttpResponse;
import io.micronaut.http.MutableHttpResponse;
import io.micronaut.http.annotation.Filter;
import io.micronaut.http.filter.FilterChain;
import io.micronaut.http.filter.HttpServerFilter;
import io.micronaut.http.filter.ServerFilterChain;
import org.reactivestreams.Publisher;

import javax.annotation.PostConstruct;

@Filter("/**")
public class RouterFilter implements HttpServerFilter {
    @PostConstruct
    public void init(){

    }

    @Override
    public Publisher<MutableHttpResponse<?>> doFilter(HttpRequest<?> request, ServerFilterChain chain) {
        String path=request.getPath();

        return chain.proceed(request);
    }

    @Override
    public Publisher<? extends HttpResponse<?>> doFilter(HttpRequest<?> request, FilterChain chain) {
        return null;
    }

    @Override
    public int getOrder() {
        return 0;
    }
}
