package cn.katoumegumi.java.sql.entity;

import cn.katoumegumi.java.sql.MySearch;
import cn.katoumegumi.java.sql.MySearchList;
import cn.katoumegumi.java.sql.SqlOperator;

import java.util.ArrayList;
import java.util.List;

/**
 * 抽象的查询条件集合
 * @author ws
 */
public abstract class AbstractSearchList {

    protected final List<MySearchList> ands = new ArrayList<>();

    protected final List<MySearchList> ors = new ArrayList<>();

    protected List<MySearch> mySearches = new ArrayList<>();

    public abstract AbstractSearchList add(String fieldName, SqlOperator operator, Object value);

}
