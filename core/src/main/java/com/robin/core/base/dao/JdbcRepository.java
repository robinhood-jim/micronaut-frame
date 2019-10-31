package com.robin.core.base.dao;


import com.robin.core.query.util.QueryFactory;
import com.robin.core.sql.util.BaseSqlGen;
import io.micronaut.context.ApplicationContext;
import io.micronaut.inject.qualifiers.Qualifiers;
import org.springframework.jdbc.support.lob.LobHandler;

import javax.sql.DataSource;

public class JdbcRepository extends JdbcDao {

    private DataSource dataSource;
    private String sourceName;

    private ApplicationContext applicationContext;

    public JdbcRepository(ApplicationContext applicationContext,String sourceName,BaseSqlGen sqlGen, QueryFactory queryFactory, LobHandler lobHandler){
        this.applicationContext=applicationContext;
        this.sourceName=sourceName;
        if(applicationContext!=null){
            if(applicationContext.getBean(DataSource.class)!=null){
                if(sourceName!=null){
                    dataSource=applicationContext.getBean(DataSource.class, Qualifiers.byName(sourceName));
                }else {
                    dataSource=applicationContext.getBean(DataSource.class);
                }
            }
        }
        setDataSource(dataSource);
        setSqlGen(sqlGen);
        setQueryFactory(queryFactory);
        setLobHandler(lobHandler);
    }



}
