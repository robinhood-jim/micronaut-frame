package com.robin.core.base.manager;

import com.robin.core.base.dao.JdbcRepository;
import com.robin.core.base.dao.util.AnnotationRetriever;
import com.robin.core.base.dao.util.PropertyFunction;
import com.robin.core.base.exception.DAOException;
import com.robin.core.base.exception.ServiceException;
import com.robin.core.base.model.BaseObject;
import com.robin.core.base.service.IBaseAnnotationJdbcService;
import com.robin.core.base.util.Const;
import com.robin.core.query.util.PageQuery;
import com.robin.core.sql.util.FilterCondition;
import com.robin.core.sql.util.FilterConditionBuilder;
import io.micronaut.context.ApplicationContext;
import io.micronaut.inject.qualifiers.Qualifiers;
import io.micronaut.transaction.annotation.ReadOnly;
import jakarta.inject.Inject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.PostConstruct;
import javax.transaction.Transactional;
import java.io.Serializable;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.List;
import java.util.Map;


public abstract class  JdbcManager<V extends BaseObject,P extends Serializable> implements IBaseAnnotationJdbcService<V,P> {
    protected JdbcRepository jdbcRepository;
    protected Class<V> type;
    protected Class<P> pkType;
    protected Logger logger= LoggerFactory.getLogger(getClass());
    protected AnnotationRetriever.EntityContent entityContent;

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
        pkType=(Class) parametrizedType.getActualTypeArguments()[1];
        if(type!=null){
            entityContent= AnnotationRetriever.getMappingTableByCache(type);
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
    @Transactional
    public P saveEntity(V v) throws ServiceException {
        try {
            return jdbcRepository.createVO(v,pkType);
        }catch (DAOException ex){
            throw new ServiceException(ex);
        }
    }

    @Override
    @Transactional
    public int updateEntity(V v) throws ServiceException {
        try {
            return jdbcRepository.updateVO(v,null);
        }catch (DAOException ex){
            throw new ServiceException(ex);
        }
    }

    @Override
    @Transactional
    public int deleteEntity(P[] ps) throws ServiceException {
        try {
            return jdbcRepository.deleteVO(type,ps);
        }catch (DAOException ex){
            throw new ServiceException(ex);
        }
    }

    @Override
    @Transactional
    public int deleteByField(String s, Object o) throws ServiceException {
        try {
            return jdbcRepository.deleteByField(type,s,o);
        }catch (DAOException ex){
            throw new ServiceException(ex);
        }
    }

    @Override
    @Transactional
    public int deleteByField(PropertyFunction<V, ?> propertyFunction, Object o) throws ServiceException {
        try {
            return jdbcRepository.deleteByField(type,propertyFunction,o);
        }catch (DAOException ex){
            throw new ServiceException(ex);
        }
    }

    @Override
    @ReadOnly
    public V getEntity(P p) throws ServiceException {
        try {
            return (V)jdbcRepository.getEntity(type,p);
        }catch (DAOException ex){
            throw new ServiceException(ex);
        }
    }

    @Override
    @ReadOnly
    public void queryBySelectId(PageQuery pageQuery) throws ServiceException {
        try{
            jdbcRepository.queryBySelectId(pageQuery);
        }catch(DAOException ex){
            throw new ServiceException(ex);
        }
    }

    @Override
    @ReadOnly
    public List<Map<String, Object>> queryByPageSql(String sql, PageQuery pageQuery) throws ServiceException {
        try{
            return jdbcRepository.queryByPageSql(sql, pageQuery);
        }catch(DAOException ex){
            throw new ServiceException(ex);
        }
    }

    @Override
    @Transactional
    public void executeBySelectId(PageQuery pageQuery) throws ServiceException {
        try{
            jdbcRepository.executeBySelectId(pageQuery);
        }catch(DAOException ex){
            throw new ServiceException(ex);
        }
    }

    @Override
    @ReadOnly
    public List<Map<String, Object>> queryBySql(String sqlstr,Object... objs) throws ServiceException {
        try{
            return jdbcRepository.queryBySql(sqlstr,objs);
        }catch(DAOException ex){
            throw new ServiceException(ex);
        }
    }

    @Override
    @ReadOnly
    public PageQuery queryBySql(String querySQL, String countSql, String[] displayname, PageQuery pageQuery) throws ServiceException {
        try{
            return jdbcRepository.queryBySql(querySQL, countSql, displayname, pageQuery);
        }catch(DAOException ex){
            throw new ServiceException(ex);
        }
    }



    @Override
    @ReadOnly
    public int queryByInt(String querySQL,Object... objects) throws ServiceException {
        try{
            return jdbcRepository.queryByInt(querySQL,objects);
        }catch(DAOException ex){
            throw new ServiceException(ex);
        }
    }

    @Override
    @ReadOnly
    public List<V> queryByField(String fieldName, Const.OPERATOR oper, Object... fieldValues) throws ServiceException {
        List<V> retlist;
        try{
            retlist=jdbcRepository.queryByField(type, fieldName, oper, fieldValues);
        }
        catch(DAOException ex){
            throw new ServiceException(ex);
        }catch(Exception e){
            throw new ServiceException(e);
        }
        return retlist;
    }

    @Override
    @ReadOnly
    public List<V> queryByField(PropertyFunction<V, ?> propertyFunction, Const.OPERATOR operator, Object... fieldValues) throws ServiceException {
        List<V> retlist;
        try{
            retlist=jdbcRepository.queryByField(type, propertyFunction, operator, fieldValues);
        }
        catch(DAOException ex){
            throw new ServiceException(ex);
        }catch(Exception e){
            throw new ServiceException(e);
        }
        return retlist;
    }

    @Override
    @ReadOnly
    public List<V> queryByFieldOrderBy(String orderByStr, String fieldName, Const.OPERATOR oper, Object... fieldValues) throws ServiceException {
        List<V> retlist;
        try{
            retlist= jdbcRepository.queryByFieldOrderBy(type,fieldName, oper, orderByStr, fieldValues);
        }
        catch(DAOException ex){
            throw new ServiceException(ex);
        }catch(Exception e){
            throw new ServiceException(e);
        }
        return retlist;
    }

    @Override
    public List<V> queryByFieldOrderBy(String s, PropertyFunction<V, ?> propertyFunction, Const.OPERATOR operator, Object... fieldValues) throws ServiceException {
        List<V> retlist;
        try{
            retlist= jdbcRepository.queryByFieldOrderBy(type,propertyFunction, operator,s, fieldValues);
        }
        catch(DAOException ex){
            throw new ServiceException(ex);
        }catch(Exception e){
            throw new ServiceException(e);
        }
        return retlist;
    }

    @Override
    @ReadOnly
    public List<V> queryAll() throws ServiceException {
        List<V> retlist;
        try{
            retlist=jdbcRepository.queryAll(type);
        }catch (Exception e) {
            throw new ServiceException(e);
        }
        return retlist;
    }

    @Override
    @ReadOnly
    public List<V> queryByVO(V vo, Map<String, Object> additonMap, String orderByStr) throws ServiceException {
        List<V> retlist;
        try {
            retlist= jdbcRepository.queryByVO(type, vo, additonMap, orderByStr);
        } catch (DAOException ex) {
            throw new ServiceException(ex);
        }
        return retlist;
    }

    @Override
    @ReadOnly
    public List<V> queryByCondition(List<FilterCondition> list, PageQuery s) {
        return jdbcRepository.queryByCondition(type,list,s);
    }

    @Override
    public List<V> queryByCondition(FilterConditionBuilder filterConditionBuilder, PageQuery pageQuery) {
        List<V> retlist;
        try {
            retlist= jdbcRepository.queryByCondition(type, filterConditionBuilder.getConditions(),pageQuery);
        } catch (DAOException ex) {
            throw new ServiceException(ex);
        }
        return retlist;
    }


    @Override
    @ReadOnly
    public List<V> queryByCondition(List<FilterCondition> list){
        PageQuery pageQuery=new PageQuery();
        pageQuery.setPageSize(0);
        return jdbcRepository.queryByCondition(type,list,pageQuery);
    }

    @Override
    @ReadOnly
    public V getByField(String s, Const.OPERATOR operator, Object... objects) throws ServiceException {
        try{
            return jdbcRepository.getByField(type,s,operator,objects);
        }catch (DAOException ex){
            throw new ServiceException(ex);
        }
    }

    @Override
    @ReadOnly
    public V getByField(PropertyFunction<V, ?> propertyFunction, Const.OPERATOR operator, Object... objects) throws ServiceException {
        try{
            return jdbcRepository.getByField(type,propertyFunction,operator,objects);
        }catch (DAOException ex){
            throw new ServiceException(ex);
        }
    }

    public JdbcRepository getJdbcRepository() {
        return jdbcRepository;
    }
}
