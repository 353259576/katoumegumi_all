package cn.katoumegumi.java.common.model;

import cn.katoumegumi.java.common.WsCollectionUtils;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class GenericsTypeModel {

    private String className;

    private List<GenericsTypeModel> genericsTypeModelList;

    public GenericsTypeModel() {
        this.genericsTypeModelList = new ArrayList<>();
    }

    public String getClassName() {
        return className;
    }

    public GenericsTypeModel setClassName(String className) {
        this.className = className;
        return this;
    }

    public GenericsTypeModel setGenericsTypeModelList(List<GenericsTypeModel> genericsTypeModelList) {
        this.genericsTypeModelList = genericsTypeModelList;
        return this;
    }

    public List<GenericsTypeModel> getGenericsTypeModelList() {
        return genericsTypeModelList;
    }
}
