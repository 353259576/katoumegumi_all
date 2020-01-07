package cn.katoumegumi.java.hibernate;

import cn.katoumegumi.java.common.WsFieldUtils;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import cn.katoumegumi.java.common.SupplierFunc;

import javax.persistence.criteria.JoinType;
import java.util.*;

public class MySearchList {
    private List<MySearch> mySearches = new ArrayList<>();

    private List<MySearch> orderSearches = new ArrayList<>();

    private List<MySearchList> ands = new ArrayList<>();

    private List<MySearchList> ors = new ArrayList<>();

    private Map<Class,String> tableAndNickNameMap = new HashMap<>();

    private List<TableRelation> joins = new ArrayList<>();

    private Class mainClass;

    private JoinType defaultJoinType = JoinType.INNER;

    private Page pageVO;

    public MySearchList() {

    }

    public static MySearchList newMySearchList() {
        return new MySearchList();
    }

    public MySearchList(List<MySearch> mySearches) {
        this.mySearches = mySearches;
    }

    public static MySearchList newMySearchList(List<MySearch> mySearches) {
        return new MySearchList(mySearches);
    }


    public MySearchList add(MySearch mySearch) {
        mySearches.add(mySearch);
        return this;
    }

    public MySearchList add(String fieldName, JpaDataHandle.Operator operator, Object value) {
        if(operator.equals(JpaDataHandle.Operator.SORT)){
            orderSearches.add(new MySearch(fieldName,operator,value));
            return this;
        }
        if(mainClass != null){
            String tableName = tableAndNickNameMap.get(mainClass);
            mySearches.add(new MySearch(mainClass,tableName,fieldName,operator,value));
        }else {
            mySearches.add(new MySearch(fieldName, operator, value));
        }

        return this;
    }
    public<T> MySearchList add(SupplierFunc<T> supplierFunc, JpaDataHandle.Operator operator, Object value) {
        String name = WsFieldUtils.getFieldName(supplierFunc);
        return add(name,operator,value);
    }

    public MySearch get(int i) {
        return mySearches.get(i);
    }

    public List<MySearch> getAll() {
        return mySearches;
    }

    public boolean isEmpty() {
        return mySearches.isEmpty()&&ands.isEmpty()&&ors.isEmpty();
    }

    public Iterator<MySearch> iterator() {
        return mySearches.iterator();
    }


    public MySearch remove(String name) {
        MySearch mySearch = null;
        for (int i = 0; i < mySearches.size(); i++) {
            mySearch = mySearches.get(i);
            if (mySearch.getFieldName().equals(name)) {
                mySearches.remove(i);
                return mySearch;
            }
        }
        return null;
    }

    public Page getPageVO() {
        return pageVO;
    }

    public MySearchList setPageVO(Page pageVO) {
        this.pageVO = pageVO;
        return this;
    }

    public Map createMybaitsMap() {
        Map map = new HashMap();
        for (MySearch m1 : mySearches) {
            map.put(m1.getFieldName(), m1.getValue());
        }
        if (pageVO != null) {
            map.put("page", pageVO);
        }
        return map;
    }


    public Map createMybaitsMapNoPage() {
        Map map = new HashMap();
        for (MySearch m1 : mySearches) {
            map.put(m1.getFieldName(), m1.getValue());
        }
        return map;
    }


    public MySearch get(String value) {
        for (MySearch m : mySearches) {
            if (m.getFieldName().equals(value)) {
                return m;
            }
        }
        return null;
    }

    public Class getMainClass() {
        return mainClass;
    }

    public MySearchList setMainClass(Class mainClass) {
        if(!tableAndNickNameMap.containsKey(mainClass)){
            tableAndNickNameMap.put(mainClass,mainClass.getName());
        }
        this.mainClass = mainClass;
        return this;
    }

    public <T> MySearchList eq(SupplierFunc<T> supplierFunc, Object value){
        return add(supplierFunc, JpaDataHandle.Operator.EQ,value);
    }

    public <T> MySearchList like(SupplierFunc<T> supplierFunc,Object value){
        return add(supplierFunc, JpaDataHandle.Operator.LIKE,value);
    }

    public <T> MySearchList gt(SupplierFunc<T> supplierFunc,Object value){
        return add(supplierFunc, JpaDataHandle.Operator.GT,value);
    }

    public <T> MySearchList gte(SupplierFunc<T> supplierFunc,Object value){
        return add(supplierFunc, JpaDataHandle.Operator.GTE,value);
    }

    public <T> MySearchList lt(SupplierFunc<T> supplierFunc,Object value){
        return add(supplierFunc, JpaDataHandle.Operator.LT,value);
    }

    public <T> MySearchList lte(SupplierFunc<T> supplierFunc,Object value){
        return add(supplierFunc, JpaDataHandle.Operator.LTE,value);
    }

    public <T> MySearchList in(SupplierFunc<T> supplierFunc,Object value) {
        return add(supplierFunc, JpaDataHandle.Operator.IN,value);
    }
    public <T> MySearchList nin(SupplierFunc<T> supplierFunc,Object value) {
        return add(supplierFunc, JpaDataHandle.Operator.NIN,value);
    }
    public <T> MySearchList notNull(SupplierFunc<T> supplierFunc,Object value) {
        return add(supplierFunc, JpaDataHandle.Operator.NOTNULL,value);
    }
    public <T> MySearchList isNull(SupplierFunc<T> supplierFunc,Object value) {
        return add(supplierFunc, JpaDataHandle.Operator.NULL,value);
    }
    public <T> MySearchList ne(SupplierFunc<T> supplierFunc,Object value) {
        return add(supplierFunc, JpaDataHandle.Operator.NE,value);
    }
    public <T> MySearchList sql(SupplierFunc<T> supplierFunc,Object value) {
        return add(supplierFunc, JpaDataHandle.Operator.SQL,value);
    }
    public <T> MySearchList sort(SupplierFunc<T> supplierFunc,Object value) {
        return add(supplierFunc, JpaDataHandle.Operator.SORT,value);
    }








    public <T> MySearchList eq(String column, Object value){
        return add(column, JpaDataHandle.Operator.EQ,value);
    }

    public <T> MySearchList like(String column,Object value){
        return add(column, JpaDataHandle.Operator.LIKE,value);
    }

    public <T> MySearchList gt(String column,Object value){
        return add(column, JpaDataHandle.Operator.GT,value);
    }

    public <T> MySearchList gte(String column,Object value){
        return add(column, JpaDataHandle.Operator.GTE,value);
    }

    public <T> MySearchList lt(String column,Object value){
        return add(column, JpaDataHandle.Operator.LT,value);
    }

    public <T> MySearchList lte(String column,Object value){
        return add(column, JpaDataHandle.Operator.LTE,value);
    }

    public <T> MySearchList in(String column,Object value) {
        return add(column, JpaDataHandle.Operator.IN,value);
    }
    public <T> MySearchList nin(String column,Object value) {
        return add(column, JpaDataHandle.Operator.NIN,value);
    }
    public <T> MySearchList notNull(String column,Object value) {
        return add(column, JpaDataHandle.Operator.NOTNULL,value);
    }
    public <T> MySearchList isNull(String column,Object value) {
        return add(column, JpaDataHandle.Operator.NULL,value);
    }
    public <T> MySearchList ne(String column,Object value) {
        return add(column, JpaDataHandle.Operator.NE,value);
    }
    public <T> MySearchList sql(String column,Object value) {
        return add(column, JpaDataHandle.Operator.SQL,value);
    }
    public <T> MySearchList sort(String column,Object value) {
        return add(column, JpaDataHandle.Operator.SORT,value);
    }










    public MySearchList and(MySearchList ...mySearchLists){
        ands.addAll(Arrays.asList(mySearchLists));
        return this;
    }

    public MySearchList or(MySearchList ...mySearchLists){
        ors.addAll(Arrays.asList(mySearchLists));
        return this;
    }

    public <T> MySearchList join(String tableNickName,Class<?> joinTableClass,String joinTableNickName,String tableColumn,String joinColumn) {
        TableRelation tableRelation = new TableRelation();
        tableRelation.setJoinTableClass(joinTableClass);
        tableRelation.setTableNickName(tableNickName);
        tableRelation.setJoinTableNickName(joinTableNickName);
        tableRelation.setTableColumn(tableColumn);
        tableRelation.setJoinTableColumn(joinColumn);
        joins.add(tableRelation);
        return this;
    }




    public Map<Class, String> getTableAndNickNameMap() {
        return tableAndNickNameMap;
    }

    public List<TableRelation> getJoins() {
        return joins;
    }


    public List<MySearchList> getAnds() {
        return ands;
    }

    public List<MySearchList> getOrs() {
        return ors;
    }

    public List<MySearch> getOrderSearches() {
        return orderSearches;
    }

    public JoinType getDefaultJoinType() {
        return defaultJoinType;
    }

    public MySearchList setDefaultJoinType(JoinType defaultJoinType) {
        this.defaultJoinType = defaultJoinType;
        return this;
    }
}

