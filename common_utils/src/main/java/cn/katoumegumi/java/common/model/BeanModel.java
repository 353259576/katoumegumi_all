package cn.katoumegumi.java.common.model;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

public class BeanModel {

    private final Class<?> beanClass;

    private final Map<String,BeanPropertyModel> propertyModelMap;

    public BeanModel(Class<?> beanClass) {
        this.beanClass = beanClass;
        this.propertyModelMap = new LinkedHashMap<>();
    }

    public Class<?> getBeanClass() {
        return beanClass;
    }


    public Map<String, BeanPropertyModel> getPropertyModelMap() {
        return propertyModelMap;
    }
}
