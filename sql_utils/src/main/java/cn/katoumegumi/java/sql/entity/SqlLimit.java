package cn.katoumegumi.java.sql.entity;

/**
 * limit语句
 *
 * @author ws
 */
public class SqlLimit {

    private long current = -1L;

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

    public long getCurrent() {
        return current;
    }

    public SqlLimit setCurrent(long current) {
        this.current = current;
        return this;
    }

    public void build() {
        if (this.current > -1) {
            if (this.current != 0) {
                this.offset = (this.current - 1) * this.size;
            }
        } else {
            this.current = this.offset / this.size + 1;
        }
    }
}
