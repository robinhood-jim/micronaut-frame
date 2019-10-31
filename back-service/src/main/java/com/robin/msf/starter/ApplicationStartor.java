package com.robin.msf.starter;

import com.robin.msf.config.BaseConfig;
import io.micronaut.runtime.Micronaut;

/**
 * <p>Created at: 2019-10-17 17:38:10</p>
 *
 * @author robinjim
 * @version 1.0
 */
public class ApplicationStartor {
    public static void main(String[] args){
        Micronaut.run(ApplicationStartor.class);
        BaseConfig config=new BaseConfig();
    }
}
