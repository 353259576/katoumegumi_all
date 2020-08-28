package cn.katoumegumi.java.sql.entity;

import java.util.ArrayList;
import java.util.List;

/**
 * 返回数据集合
 * @author ws
 */
public class ReturnEntity {

    private List<ReturnColumnEntity> columnEntityList;

    private int idIndex = 0;


    public List<ReturnColumnEntity> getColumnEntityList() {
        return columnEntityList;
    }

    public ReturnEntity setColumnEntityList(List<ReturnColumnEntity> columnEntityList) {
        this.columnEntityList = columnEntityList;
        return this;
    }

    public int getIdIndex() {
        return idIndex;
    }

    public ReturnEntity setIdIndex(int idIndex) {
        this.idIndex = idIndex;
        return this;
    }
}
