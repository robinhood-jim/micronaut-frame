package com.robin.core.base.controller;


import com.robin.core.base.exception.ServiceException;
import com.robin.core.base.model.BaseObject;
import com.robin.core.base.reflect.ReflectUtils;
import com.robin.core.base.service.IBaseAnnotationJdbcService;
import com.robin.core.base.spring.SpringContextHolder;
import com.robin.core.base.util.Const;
import com.robin.core.convert.util.ConvertUtil;
import com.robin.core.query.util.PageQuery;
import com.robin.core.query.util.QueryFactory;
import com.robin.core.query.util.QueryString;
import com.robin.core.web.codeset.Code;
import com.robin.core.web.codeset.CodeSetService;
import com.robin.core.web.international.Translator;
import io.micronaut.context.ApplicationContext;
import io.micronaut.http.HttpParameters;
import io.micronaut.http.HttpRequest;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.beanutils.PropertyUtils;

import javax.annotation.PostConstruct;
import javax.inject.Named;
import java.io.Serializable;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.*;

@Slf4j
public class BaseCrudController<O extends BaseObject, P extends Serializable, S extends IBaseAnnotationJdbcService<O,P>> {
    private Class<O> objectType;
    private Class<P> pkType;
    private Class<S> serviceType;
    protected S service;
    protected Method valueOfMethod;
    protected static final String COL_MESSAGE="message";
    protected static final String COL_SUCCESS="success";


    public BaseCrudController() {
        Type genericSuperClass = getClass().getGenericSuperclass();
        ParameterizedType parametrizedType;
        if ((genericSuperClass instanceof ParameterizedType)) {
            parametrizedType = (ParameterizedType) genericSuperClass;
        } else {
            if ((genericSuperClass instanceof Class)) {
                parametrizedType = (ParameterizedType) ((Class) genericSuperClass).getGenericSuperclass();
            } else {
                throw new IllegalStateException("class " + getClass() + " is not subtype of ParametrizedType.");
            }
        }
        this.objectType = ((Class) parametrizedType.getActualTypeArguments()[0]);
        this.pkType = ((Class) parametrizedType.getActualTypeArguments()[1]);
        this.serviceType = ((Class) parametrizedType.getActualTypeArguments()[2]);
        try {
            valueOfMethod = this.pkType.getMethod("valueOf", String.class);
        } catch (Exception ex) {
            log.error("{}", ex);
        }
    }
    @PostConstruct
    public void init(@Named  ApplicationContext context){
        this.service=context.getBean(serviceType);
    }
    protected Map<String,Object> doSave(O object){
        Map<String, Object> retMap = new HashMap();
        try {
            P pk=this.service.saveEntity(object);
            wrapSuccess(retMap);
            doAfterAdd(object,pk, retMap);
        } catch (Exception ex) {
            this.log.error("{}", ex);
            wrapResponse(retMap, ex);
        }
        return retMap;
    }
    protected Map<String,Object> doUpdate(O object,P id){
        Map<String,Object> retMap = new HashMap();
        try{
            O updateObj = this.service.getEntity(id);
            ConvertUtil.convertToModelForUpdate(object,object);
            this.service.updateEntity(updateObj);
            this.doAfterUpdate(updateObj, retMap);
            this.wrapSuccess(retMap);
        }catch (Exception ex){
            this.log.error("{}", ex);
            wrapResponse(retMap, ex);
        }
        return retMap;
    }
    protected Map<String,Object> doView(P id){
        Map<String,Object> retMap=new HashMap<>();
        try{
            O object = service.getEntity(id);
            retMap = wrapSuccess("success");
            doAfterView(object, retMap);
            wrapSuccess(retMap);
        }catch (ServiceException ex){
            this.log.error("{}", ex);
            wrapResponse(retMap, ex);
        }
        return retMap;
    }
    protected Map<String,Object> doEdit(P id){
        Map<String, Object> retMap = new HashMap<String, Object>();
        try {
            BaseObject object = service.getEntity(id);
            doAfterEdit(object, retMap);
            wrapSuccess(retMap);
        } catch (Exception e) {
            log.error("{}", e);
            wrapFailed(retMap, e);
        }
        return retMap;
    }
    protected Map<String,Object> doDelete(P[] ids){
        Map<String, Object> retMap = new HashMap();
        try {
            this.service.deleteEntity(ids);
            doAfterDelete(ids, retMap);
            wrapSuccess(retMap);
        } catch (Exception ex) {
            wrapFailed(retMap, ex);
        }
        return retMap;
    }
    protected Map<String, Object> doQuery(HttpRequest request, PageQuery query) {
        Map<String, Object> retMap = new HashMap<>();
        try {
            Map<String, String> valueMap = wrapRequest(request);

            if (query.getParameters().isEmpty()) {
                query.setParameters(valueMap);
            }
            this.service.queryBySelectId(query);
            doAfterQuery(query, retMap);
            wrapSuccess(retMap);
        } catch (Exception ex) {
            wrapFailed(retMap, ex);
        }
        return retMap;
    }

    protected void doAfterAdd(BaseObject obj,P pk, Map<String, Object> retMap) {
        retMap.put("primaryKey",pk);
    }

    protected void doAfterView(BaseObject obj, Map<String, Object> retMap) {
        retMap.put("model", obj);
    }

    protected void doAfterEdit(BaseObject obj, Map<String, Object> retMap) {
        retMap.put("model", obj);
    }

    protected void doAfterUpdate( BaseObject obj, Map<String, Object> retMap) {
    }

    protected void doAfterQuery(PageQuery query, Map<String, Object> retMap) {
        retMap.put("query", query);
    }

    protected void doAfterDelete( P[] ids, Map<String, Object> retMap) {

    }
    protected PageQuery wrapPageQuery(HttpRequest<?> request) {
        PageQuery query = new PageQuery();
        HttpParameters parameters = request.getParameters();
        Iterator<Map.Entry<String, List<String>>> iter = parameters.asMap().entrySet().iterator();
        Map<String, Object> tmpmap = new HashMap();
        Map<String, Method> methodMap = ReflectUtils.getAllSetMethod(query);
        try {
            while (iter.hasNext()) {
                Map.Entry<String, List<String>> entry = iter.next();
                String key = entry.getKey();
                if (key.startsWith("query.")) {
                    String compareKey = key.substring(6);
                    if (methodMap.containsKey(compareKey)) {
                        methodMap.get(compareKey).invoke(query,
                                ConvertUtil.parseParameter(methodMap.get(compareKey).getParameterTypes()[0], entry.getValue().get(0)));
                    }
                }
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return query;
    }
    public static Map<String, String> wrapRequest(HttpRequest<?> request)
    {
        Map<String, String> map = new HashMap();
        Iterator<Map.Entry<String,List<String>>> iter = request.getParameters().asMap().entrySet().iterator();
        while (iter.hasNext())
        {
            Map.Entry<String,List<String>> entry=iter.next();
            map.put(entry.getKey(), entry.getValue().get(0));
        }
        return map;
    }
    protected  P[] parseId(String[] ids) throws Exception {
        if (ids == null || ids.length == 0) {
            throw new Exception("ID not exists!");
        }
        P[] array=(P[])java.lang.reflect.Array.newInstance(pkType,ids.length);
        try {
            for (int i = 0; i < ids.length; i++) {
                P p = pkType.newInstance();
                valueOfMethod.invoke(p, ids[i]);
                array[i]=p;
            }
        } catch (Exception ex) {
            log.error("{}", ex);
        }
        return array;
    }
    protected void wrapResponse(Map<String,Object> retmap,Exception ex){
        if(ex!=null){
            wrapFailed(retmap,ex);
        }else
        {
            wrapSuccess(retmap,"success");
        }
    }


    protected void wrapSuccess(Map<String, Object> retMap)
    {
        retMap.put(COL_SUCCESS, true);
    }

    protected void wrapFailed( Map<String, Object> retMap, Exception ex)
    {
        retMap.put(COL_SUCCESS, false);
        retMap.put(COL_MESSAGE, ex.getMessage());
    }
    protected void wrapFailed( Map<String, Object> retMap, String message)
    {
        retMap.put(COL_SUCCESS, false);
        retMap.put(COL_MESSAGE, message);
    }


    protected Map<String, Object> wrapSuccess(String displayMsg)
    {
        Map<String, Object> retmap = new HashMap();
        retmap.put(COL_SUCCESS, true);
        retmap.put(COL_MESSAGE, displayMsg);
        return retmap;
    }

    protected void wrapSuccess(Map<String, Object> retmap, String displayMsg)
    {
        retmap.put(COL_SUCCESS, true);
        retmap.put(COL_MESSAGE, displayMsg);
    }
    protected void  wrapError(Map<String, Object> retmap,String message)
    {
        retmap.put(COL_SUCCESS, false);
        retmap.put(COL_MESSAGE, message);
    }
    protected Map<String, Object> wrapObject(Object object)
    {
        Map<String, Object> retmap = new HashMap();
        retmap.put(COL_SUCCESS, true);
        retmap.put("data", object);
        return retmap;
    }

    protected Map<String, Object> wrapError(Exception ex)
    {
        Map<String, Object> retmap = new HashMap();
        retmap.put(COL_SUCCESS, false);
        retmap.put(COL_MESSAGE, ex.getMessage());
        return retmap;
    }
    protected Map<String, Object> wrapDhtmlxGridOutput(PageQuery query) {
        List<Map<String, Object>> list = query.getRecordSet();
        Map<String, Object> retMap = new HashMap<String, Object>();
        PageQuery tquery = query;
        tquery.setRecordSet(null);
        tquery.getParameters().clear();
        try {
            QueryFactory factory = (QueryFactory) SpringContextHolder.getBean("queryFactory");
            QueryString queryObj = factory.getQuery(query.getSelectParamId());
            String field = queryObj.getField();
            String[] arr = field.split(",");
            List<String> fieldName = new ArrayList<String>();
            String idColumn = null;
            for (int i = 0; i < arr.length; i++) {
                String colname = null;
                if (arr[i].contains(" as")) {
                    String[] arr1 = arr[i].split(" as");
                    colname = arr1[1].trim();
                } else if (arr[i].contains(" AS")) {
                    String[] arr1 = arr[i].split(" AS");
                    colname = arr1[1].trim();
                } else {
                    colname = arr[i].trim();
                }
                if (i == 0) {
                    idColumn = colname;
                }
                fieldName.add(colname);
            }
            List<Map<String, Object>> retList = new ArrayList<Map<String, Object>>();
            for (Map<String, Object> map : list) {
                Map<String, Object> tmap = new HashMap<String, Object>();
                List<String> tmpList = new ArrayList<String>();
                for (String key : fieldName) {
                    if (key.equals(idColumn)) {
                        tmpList.add("0");
                    } else if (map.containsKey(key)) {
                        tmpList.add(map.get(key).toString());
                    }
                }
                tmap.put("data", tmpList);
                tmap.put("id", map.get(idColumn));
                retList.add(tmap);
            }
            retMap.put("rows", retList);
            retMap.put("query", tquery);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return retMap;
    }
    protected List<Map<String, String>> convertObjToMapList(List<?> orglist)
            throws Exception
    {
        List<Map<String, String>> list = new ArrayList();
        if ((orglist != null) && (!orglist.isEmpty())) {
            for (Object object : orglist)
            {
                Map<String, String> map = new HashMap();
                ConvertUtil.objectToMap(map, object);
                list.add(map);
            }
        }
        return list;
    }


    protected void setCode(String codeSetNos)
    {
        if (codeSetNos == null) {
            return;
        }
        String[] codes;
        if (codeSetNos.indexOf(";") !=-1)
        {
            codes = codeSetNos.split(";");
        }
        else
        {
            if (codeSetNos.indexOf(",") !=-1)
            {
                codes = codeSetNos.split(",");
            }
            else
            {
                codes = new String[1];
                codes[0] = codeSetNos;
            }
        }
        CodeSetService util= SpringContextHolder.getBean(CodeSetService.class);
        for (int i = 0; i < codes.length; i++)
        {
            util.getCacheCode(codes[i]);
        }
    }
    protected void setCode(String codeSetNo, List<?> codes, String label, String value) {
        CodeSetService util= SpringContextHolder.getBean(CodeSetService.class);
        util.setCode(codeSetNo,codes,label,value);
    }


    protected void filterListByCodeSet(PageQuery query, String columnName, String codeNo,String defaultValue)
    {
        if (!query.getRecordSet().isEmpty())
        {
            for (Map<String, Object> map : query.getRecordSet())
            {
                if(map.get(columnName)!=null) {
                    String name = findCodeName(codeNo, map.get(columnName).toString());
                    if ((name != null) && (!"".equals(name))) {
                        map.put(columnName, name);
                    }
                }else{
                    if(defaultValue==null) {
                        map.put(columnName, "");
                    }else{
                        map.put(columnName,defaultValue);
                    }
                }
            }
        }
    }
    protected void fillMissingValue(PageQuery query,String columnName,String defaultValue){
        if (!query.getRecordSet().isEmpty())
        {
            for(Map<String,Object> map:query.getRecordSet()){
                if(map.get(columnName)==null || map.get(columnName).toString().isEmpty()){
                    map.put(columnName,defaultValue);
                }
            }
        }
    }

    protected void filterListByCodeSet(List<?> list, String columnName, String codeNo)
            throws Exception
    {
        if ((list != null) && (!list.isEmpty())) {
            for (Object obj : list)
            {
                Object transval = PropertyUtils.getProperty(obj, columnName);
                String name = findCodeName(codeNo, transval.toString());
                if ((name != null) && (!"".equals(name))) {
                    PropertyUtils.setProperty(obj, columnName, name);
                }
            }
        }
    }

    protected String findCodeName(String codeNo, String value)
    {
        CodeSetService util= SpringContextHolder.getBean(CodeSetService.class);
        Map<String,String> codeMap = util.getCacheCode(codeNo);
        if (value == null) {
            return "";
        }
        if (codeMap == null || codeMap.isEmpty()) {
            return "";
        }

        if (codeMap.containsKey(value)) {
            return codeMap.get(value);
        }
        return "";
    }

    protected List<Code> findCodeSetArr(String codeSetNo){
        CodeSetService util= SpringContextHolder.getBean(CodeSetService.class);
        return getCodeList(util.getCacheCode(codeSetNo));
    }
    protected void insertNullSelect(List<Map<String, Object>> list)
    {
        Map<String, Object> tmap = new HashMap();
        tmap.put("value", "");
        String message= Translator.toLocale("combo.NullDisplay");
        tmap.put("text", message);
        list.add(tmap);
    }
    protected void insertMapToSelect(List<Map<String, Object>> list,Map<String,String> codeMap)
    {
        Iterator<Map.Entry<String,String>> iterator=codeMap.entrySet().iterator();
        while(iterator.hasNext()){
            Map.Entry<String,String> entry=iterator.next();
            Map<String, Object> tmap = new HashMap();
            tmap.put("value", entry.getKey());
            tmap.put("text", entry.getValue());
            list.add(tmap);
        }
    }
    protected List<Map<String,Object>> wrapYesNoCombo(boolean insertNullVal){
        List<Map<String,Object>> list=new ArrayList<>();
        if (insertNullVal) {
            insertNullSelect(list);
        }
        Map<String, Object> tmap = new HashMap();
        tmap.put("value", Const.VALID);
        tmap.put("text", Translator.toLocale("combo.yesDisplay"));
        list.add(tmap);
        Map<String, Object> tmap1 = new HashMap();
        tmap1.put("value", "0");
        tmap1.put("text", Translator.toLocale("combo.noDisplay"));
        list.add(tmap1);
        return list;
    }
    protected List<Map<String,Object>> wrapCodeSet(String codeSetNo){
        List<Map<String,Object>> list=new ArrayList<>();
        CodeSetService util= SpringContextHolder.getBean(CodeSetService.class);
        List<Code> codeList=getCodeList(util.getCacheCode(codeSetNo));
        for(Code code:codeList){
            Map<String, Object> tmap = new HashMap();
            tmap.put("value", code.getValue());
            tmap.put("text", code.getCodeName());
            list.add(tmap);
        }
        return list;
    }
    protected List<Code> getCodeList(Map<String,String> codeMap){
        List<Code> retlist=new ArrayList<>();
        Iterator<Map.Entry<String,String>> iter= codeMap.entrySet().iterator();
        while(iter.hasNext()){
            Map.Entry<String,String> entry=iter.next();
            retlist.add(new Code(entry.getValue(),entry.getKey()));
        }
        return retlist;
    }
}
