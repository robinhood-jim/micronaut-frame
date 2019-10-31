package com.robin.core.base.manager;

import com.robin.core.base.dao.JdbcRepository;
import com.robin.core.base.dao.util.AnnotationRetrevior;
import com.robin.core.base.exception.DAOException;
import com.robin.core.base.exception.ServiceException;
import com.robin.core.base.model.BaseObject;
import com.robin.core.base.service.IBaseAnnotationJdbcService;
import com.robin.core.query.util.PageQuery;
import io.micronaut.context.ApplicationContext;
import io.micronaut.inject.qualifiers.Qualifiers;
import io.micronaut.spring.tx.annotation.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.transaction.annotation.Propagation;

import javax.annotation.PostConstruct;
import javax.inject.Inject;
import java.io.Serializable;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.List;
import java.util.Map;


public class  JdbcManager<V extends BaseObject,P extends Serializable> implements IBaseAnnotationJdbcService<V,P> {
    protected JdbcRepository jdbcRepository;
    protected Class<V> type;
    protected Logger logger= LoggerFactory.getLogger(getClass());
    protected AnnotationRetrevior.EntityContent entityContent;

    @Inject
    private ApplicationContext applicationContext;
    public JdbcManager(){
        Type genericSuperClass = getClass().getGenericSuperclass();
        ParameterizedType parametrizedType;
        if (genericSuperClass instanceof ParameterizedType) {
            parametrizedType = (ParameterizedType) genericSuperClass;
        } else if (genericSuperClass instanceof Class) {
            parametrizedType = (ParameterizedType) ((Class<?>) genericSuperClass).getGenericSuperclass();
        } else {
            throw new IllegalStateException("class " + getClass() + " is not subtype of ParametrizedType.");
        }
        type = (Class) parametrizedType.getActualTypeArguments()[0];
        if(type!=null){
            entityContent= AnnotationRetrevior.getMappingTableByCache(type);
        }
    }
    @PostConstruct
    public void init(){
        if(entityContent!=null && entityContent.getJdbcDao()!=null && !entityContent.getJdbcDao().isEmpty()){
            jdbcRepository= applicationContext.getBean(JdbcRepository.class, Qualifiers.byName(entityContent.getJdbcDao()));
        }else{
            jdbcRepository= applicationContext.getBean(JdbcRepository.class);
        }
    }



    @Override
    @Transactional(propagation = Propagation.REQUIRED,rollbackFor = ServiceException.class)
    public P saveEntity(V v) throws ServiceException {
        try {
            return (P)jdbcRepository.createVO(v);
        }catch (DAOException ex){
            throw new ServiceException(ex);
        }
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRED,rollbackFor = ServiceException.class)
    public int updateEntity(V v) throws ServiceException {
        try {
            return jdbcRepository.updateVO(type,v);
        }catch (DAOException ex){
            throw new ServiceException(ex);
        }
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRED,rollbackFor = ServiceException.class)
    public int deleteEntity(P[] ps) throws ServiceException {
        try {
            return jdbcRepository.deleteVO(type,ps);
        }catch (DAOException ex){
            throw new ServiceException(ex);
        }
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRED,rollbackFor = ServiceException.class)
    public int deleteByField(String s, Object o) throws ServiceException {
        try {
            return jdbcRepository.deleteByField(type,s,o);
        }catch (DAOException ex){
            throw new ServiceException(ex);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public V getEntity(P p) throws ServiceException {
        try {
            return (V)jdbcRepository.getEntity(type,p);
        }catch (DAOException ex){
            throw new ServiceException(ex);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public void queryBySelectId(PageQuery pageQuery) throws ServiceException {
        try{
            jdbcRepository.queryBySelectId(pageQuery);
        }catch(DAOException ex){
            throw new ServiceException(ex);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public List<Map<String, Object>> queryByPageSql(String sql, PageQuery pageQuery) throws ServiceException {
        try{
            return jdbcRepository.queryByPageSql(sql, pageQuery);
        }catch(DAOException ex){
            throw new ServiceException(ex);
        }
    }

    @Override
    @Transactional(propagation=Propagation.REQUIRED,rollbackFor=RuntimeException.class)
    public void executeBySelectId(PageQuery pageQuery) throws ServiceException {
        try{
            jdbcRepository.executeBySelectId(pageQuery);
        }catch(DAOException ex){
            throw new ServiceException(ex);
        }
    }

    @Override
    @Transactional(readOnly=true)
    public List<Map<String, Object>> queryBySql(String sqlstr) throws ServiceException {
        try{
            return jdbcRepository.queryBySql(sqlstr);
        }catch(DAOException ex){
            throw new ServiceException(ex);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public PageQuery queryBySql(String querySQL, String countSql, String[] displayname, PageQuery pageQuery) throws ServiceException {
        try{
            return jdbcRepository.queryBySql(querySQL, countSql, displayname, pageQuery);
        }catch(DAOException ex){
            throw new ServiceException(ex);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public List<Map<String, Object>> queryBySql(String sqlstr, Object[] objects) throws ServiceException {
        try{
            return jdbcRepository.queryBySql(sqlstr, objects);
        }catch(DAOException ex){
            throw new ServiceException(ex);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public int queryByInt(String querySQL) throws ServiceException {
        try{
            return jdbcRepository.queryByInt(querySQL);
        }catch(DAOException ex){
            throw new ServiceException(ex);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public List<V> queryByField(String fieldName, String oper, Object... fieldValues) throws ServiceException {
        List<V> retlist;
        try{
            retlist=(List<V>) jdbcRepository.queryByField(type, fieldName, oper, fieldValues);
        }
        catch(DAOException ex){
            throw new ServiceException(ex);
        }catch(Exception e){
            throw new ServiceException(e);
        }
        return retlist;
    }

    @Override
    @Transactional(readOnly = true)
    public List<V> queryByFieldOrderBy(String orderByStr, String fieldName, String oper, Object... fieldValues) throws ServiceException {
        List<V> retlist;
        try{
            retlist=(List<V>) jdbcRepository.queryByFieldOrderBy(type, orderByStr, fieldName, oper, fieldValues);
        }
        catch(DAOException ex){
            throw new ServiceException(ex);
        }catch(Exception e){
            throw new ServiceException(e);
        }
        return retlist;
    }

    @Override
    @Transactional(readOnly = true)
    public List<V> queryAll() throws ServiceException {
        List<V> retlist;
        try{
            retlist=(List<V>)jdbcRepository.queryAll(type);
        }catch (Exception e) {
            throw new ServiceException(e);
        }
        return retlist;
    }

    @Override
    @Transactional(readOnly = true)
    public List<V> queryByVO(V vo, Map<String, Object> additonMap, String orderByStr) throws ServiceException {
        List<V> retlist;
        try {
            retlist=(List<V>) jdbcRepository.queryByVO(type, vo, additonMap, orderByStr);
        } catch (DAOException ex) {
            throw new ServiceException(ex);
        }
        return retlist;
    }

    public JdbcRepository getJdbcRepository() {
        return jdbcRepository;
    }
}
