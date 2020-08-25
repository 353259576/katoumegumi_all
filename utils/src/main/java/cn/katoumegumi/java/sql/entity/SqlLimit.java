package cn.katoumegumi.java.sql.entity;

/**
 * @author ws
 */
public class SqlLimit {

    private long current = 1L;

    private long offset = 0L;

    private long size = 10L;


    public long getOffset() {
        return offset;
    }

    public SqlLimit setOffset(long offset) {
        this.offset = offset;
        return this;
    }

    public long getSize() {
        return size;
    }

    public SqlLimit setSize(long size) {
        this.size = size;
        return this;
    }

    public Long getCurrent() {
        return current;
    }

    public SqlLimit setCurrent(Long current) {
        this.current = current;
        return this;
    }
}
