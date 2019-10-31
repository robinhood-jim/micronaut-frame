package com.robin.msf.config;


import com.robin.core.base.dao.JdbcRepository;
import com.robin.core.query.util.QueryFactory;
import com.robin.core.sql.util.BaseSqlGen;
import com.robin.core.sql.util.MysqlSqlGen;
import io.micronaut.context.ApplicationContext;
import io.micronaut.context.annotation.Bean;
import io.micronaut.context.annotation.Factory;
import io.micronaut.context.annotation.Requires;
import io.micronaut.context.annotation.Value;
import io.micronaut.inject.qualifiers.Qualifiers;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.jdbc.support.lob.DefaultLobHandler;
import org.springframework.jdbc.support.lob.LobHandler;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;
import javax.sql.DataSource;

@Factory

public class BaseConfig {

    @Value("${project.queryconfigpath}")
    private String queryConfigPath;

    @Inject
    private ApplicationContext applicationContext;
    @Singleton
    @Bean
    @Named("queryFactory")
    public QueryFactory getFactory() {
        QueryFactory factory=new QueryFactory();
        factory.setXmlConfigPath(queryConfigPath);
        factory.afterPropertiesSet();
        return factory;
    }
    @Singleton
    @Bean
    @Named("lobHandler")
    public LobHandler getLobHandler(){
        return new DefaultLobHandler();
    }
    @Singleton
    @Bean
    @Named("sqlGen")
    public BaseSqlGen getSqlGen(){
        return new MysqlSqlGen();
    }
    @Singleton
    @Bean
    @Named("jdbcDao")
    @Requires(beans ={QueryFactory.class,DataSource.class,LobHandler.class,BaseSqlGen.class})
    public JdbcRepository getJdbc(@Named("sqlGen") BaseSqlGen sqlGen,@Named("queryFactory") QueryFactory queryFactory,@Named("lobHandler") LobHandler lobHandler){
        return new JdbcRepository(applicationContext,null,sqlGen,queryFactory,lobHandler);
    }
    @Singleton
    @Bean
    @Requires(beans ={QueryFactory.class,DataSource.class,LobHandler.class,BaseSqlGen.class})
    @Named("jdbcDao1")
    public JdbcRepository getJdbc1(@Named("sqlGen") BaseSqlGen sqlGen,@Named("queryFactory") QueryFactory queryFactory,@Named("lobHandler") LobHandler lobHandler){
        return new JdbcRepository(applicationContext,"source1",sqlGen,queryFactory,lobHandler);
    }

    @Singleton
    @Bean
    @Named("trans1")
    public DataSourceTransactionManager getTranscationTrans1(){
        return new DataSourceTransactionManager(applicationContext.getBean(DataSource.class, Qualifiers.byName("source1")));
    }



}
