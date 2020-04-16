package cn.katoumegumi.java.sql;

import java.io.Serializable;

public class PageVO implements Serializable {
    private Integer page = 1;
    private Integer rows = 10;
    private Integer totalRows;

    public PageVO() {
    }

    public PageVO(Integer page, Integer rows) {
        this.page = page;
        this.rows = rows;
    }

    public Integer getPage() {
        return this.page;
    }

    public void setPage(Integer page) {
        if (page < 1) {
            page = 1;
        }

        this.page = page;
    }

    public Integer getRows() {
        return this.rows;
    }

    public void setRows(Integer rows) {
        if (rows < 1) {
            rows = 1;
        }
        this.rows = rows;
    }

    public Integer getTotalRows() {
        return this.totalRows;
    }

    public void setTotalRows(Integer totalRows) {
        if (totalRows < 0) {
            totalRows = 0;
        }
        this.totalRows = totalRows;
    }
}