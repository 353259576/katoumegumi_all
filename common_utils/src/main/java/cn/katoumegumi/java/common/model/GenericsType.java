package cn.katoumegumi.java.common.model;

import java.util.ArrayList;
import java.util.List;

public class GenericsType {

    private String className;

    private List<GenericsType> genericsTypeList;

    public GenericsType() {
        this.genericsTypeList = new ArrayList<>();
    }

    public String getClassName() {
        return className;
    }

    public GenericsType setClassName(String className) {
        this.className = className;
        return this;
    }

    public GenericsType setGenericsTypeList(List<GenericsType> genericsTypeList) {
        this.genericsTypeList = genericsTypeList;
        return this;
    }

    public List<GenericsType> getGenericsTypeList() {
        return genericsTypeList;
    }
}
