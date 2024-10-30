package com.robin.msf.gateway.config;

import io.micronaut.context.annotation.ConfigurationProperties;
import lombok.Data;

import java.util.Set;

@Data
@ConfigurationProperties("gateway")
public class GatewayProperties {
    private Set<String> services;
}
