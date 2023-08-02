package cn.katoumegumi.java.common.model;

import java.util.List;

public class GenericsTypeModel {

    private String className;

    private List<GenericsTypeModel> genericsTypeModelList;


    public String getClassName() {
        return className;
    }

    public GenericsTypeModel setClassName(String className) {
        this.className = className;
        return this;
    }

    public List<GenericsTypeModel> getGenericsTypeModelList() {
        return genericsTypeModelList;
    }

    public GenericsTypeModel setGenericsTypeModelList(List<GenericsTypeModel> genericsTypeModelList) {
        this.genericsTypeModelList = genericsTypeModelList;
        return this;
    }
}
