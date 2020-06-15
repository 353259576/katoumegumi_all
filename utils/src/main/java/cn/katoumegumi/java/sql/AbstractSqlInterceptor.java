package cn.katoumegumi.java.sql;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author 王松
 */
public abstract class AbstractSqlInterceptor {

    private Map<Class,FieldColumnRelation> classFieldMap = new ConcurrentHashMap<>();

    protected boolean isSelect(){
        return false;
    }

    protected boolean isInsert(){
        return false;
    }

    protected boolean isUpdate(){
        return false;
    }


    protected boolean useCondition(Class tClass){
        return true;
    }

    protected Object insertFill(){
        return null;
    }

    protected Object updateFill(){
        return null;
    }

    protected Object selectFill(){
        return null;
    }

    /**
     * 需要自动注入的属性名称
     * @return
     */
    protected abstract String fieldName();


    protected void addClassFieldName(Class tClass,FieldColumnRelation fieldColumnRelation){
        classFieldMap.put(tClass,fieldColumnRelation);
    }

    public FieldColumnRelation getFieldColumnRelation(Class tClass){
        return classFieldMap.get(tClass);
    }




}
