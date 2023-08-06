package cn.katoumegumi.java.sql.page;

import java.util.List;

public class ReturnPage<T> {

    private final long currentPage;

    private final long allPage;

    private final long pageSize;

    private final long total;

    private final List<T> data;

    public ReturnPage(long currentPage, long pageSize, long total, List<T> data) {
        this.currentPage = currentPage;
        this.pageSize = pageSize;
        this.total = total;
        this.data = data;
        this.allPage = (total + pageSize - 1)/pageSize;
    }

    public long getCurrentPage() {
        return currentPage;
    }

    public long getPageSize() {
        return pageSize;
    }

    public long getTotal() {
        return total;
    }

    public List<T> getData() {
        return data;
    }
}
