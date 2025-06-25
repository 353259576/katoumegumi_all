package cn.katoumegumi.java.common.model;

import cn.katoumegumi.java.common.WsReflectUtils;

import java.lang.annotation.Annotation;
import java.lang.invoke.MethodHandle;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class BeanPropertyModel {

    private final String propertyName;

    private final Field field;

    private final Method getMethod;

    private final MethodHandle getMethodHandle;

    private final Method setMethod;

    private final MethodHandle setMethodHandle;

    private final Class<?> propertyClass;

    private final Class<?> genericClass;


    public BeanPropertyModel(String propertyName, Field field, Method getMethod, MethodHandle getMethodHandle, Method setMethod, MethodHandle setMethodHandle) {
        this.propertyName = propertyName;
        this.field = field;
        this.getMethod = getMethod;
        this.getMethodHandle = getMethodHandle.asType(getMethodHandle.type().generic());
        this.setMethod = setMethod;
        this.setMethodHandle = setMethodHandle == null?null:setMethodHandle.asType(setMethodHandle.type().generic());
        Type propertyType;
        if (this.field != null){
            this.propertyClass = this.field.getType();
            propertyType = this.field.getGenericType();
        } else if (setMethod != null){
            this.propertyClass = this.setMethod.getParameterTypes()[0];
            propertyType = this.setMethod.getGenericParameterTypes()[0];
        } else  {
            this.propertyClass = this.getMethod.getReturnType();
            propertyType = this.getMethod.getGenericReturnType();
        }
        this.genericClass = WsReflectUtils.getGenericsType(propertyType);
    }

    public String getPropertyName() {
        return propertyName;
    }

    public Field getField() {
        return field;
    }

    public Method getGetMethod() {
        return getMethod;
    }

    public MethodHandle getGetMethodHandle() {
        return getMethodHandle;
    }

    public Method getSetMethod() {
        return setMethod;
    }

    public MethodHandle getSetMethodHandle() {
        return setMethodHandle;
    }

    public <A extends Annotation> A getAnnotation(Class<A> annotationClass) {
        A annotation;
        if (this.field != null) {
            annotation = this.field.getAnnotation(annotationClass);
            if (annotation != null) {
                return annotation;
            }
        }
        annotation = this.getMethod.getAnnotation(annotationClass);
        if (annotation == null && this.setMethod != null) {
            return this.setMethod.getAnnotation(annotationClass);
        }
        return null;
    }

    public List<Annotation> getAnnotations(){
        List<Annotation> list = new ArrayList<>();
        if (this.field != null){
            list.addAll(Arrays.asList(this.field.getAnnotations()));
        }
        if (this.getMethod != null){
            list.addAll(Arrays.asList(this.getMethod.getAnnotations()));
        }
        if (this.setMethod != null){
            list.addAll(Arrays.asList(this.setMethod.getAnnotations()));
        }
        return list;
    }

    public boolean setValue(Object o,Object value){
        if (this.setMethod != null){
            try {
                Object temp = setMethodHandle.invokeExact(o,value);
                return true;
            } catch (Throwable e) {
                e.printStackTrace();
                return false;
            }
        }else if (this.field != null){
            if (this.propertyClass.isPrimitive()){
                if (this.propertyClass == int.class) {
                    return WsReflectUtils.setValue(o,((Integer)value).intValue(),field);
                } else if (this.propertyClass == long.class) {
                    return WsReflectUtils.setValue(o,((Long)value).longValue(),field);
                } else if (this.propertyClass == double.class) {
                    return WsReflectUtils.setValue(o,((Double)value).doubleValue(),field);
                } else if (this.propertyClass == float.class) {
                    return WsReflectUtils.setValue(o,((Float)value).floatValue(),field);
                } else if (this.propertyClass == boolean.class) {
                    return WsReflectUtils.setValue(o,((Boolean)value).booleanValue(),field);
                } else if (this.propertyClass == byte.class) {
                    return WsReflectUtils.setValue(o,((Byte)value).byteValue(),field);
                } else if (this.propertyClass == short.class) {
                    return WsReflectUtils.setValue(o,((Short)value).shortValue(),field);
                } else if (this.propertyClass == char.class) {
                    return WsReflectUtils.setValue(o,((Character)value).charValue(),field);
                }
            }else {
                return WsReflectUtils.setValue(o,value,this.field);
            }
        }
        return false;
    }

    public <T> T getValue(Object o){
        if (this.getMethodHandle != null){
            try {
                return (T)this.getMethodHandle.invokeExact(o);
            } catch (Throwable e) {
                throw new RuntimeException(e);
            }
        }else if (this.field != null){
            return (T) WsReflectUtils.getValue(o,this.field);
        }
        return null;
    }

    public Class<?> getPropertyClass() {
        return propertyClass;
    }

    public Class<?> getGenericClass() {
        return genericClass;
    }
}
