package cn.katoumegumi.java.sql.model.condition;

/**
 * 多表达式条件(例如：a + b = c)
 */
public class MultiExpressionCondition extends AbstractExpressionCondition {

    private int index;
    private final int length;

    public MultiExpressionCondition(int length) {
        super(length);
        this.index = 0;
        this.length = length;
    }

    public MultiExpressionCondition(Object... values) {
        super(values);
        this.index = values.length;
        this.length = values.length;
    }

    public MultiExpressionCondition add(Object value) {
        if (this.index >= this.length) {
            throw new ArrayIndexOutOfBoundsException("已达到最大数量,禁止添加");
        }
        this.values[this.index] = value;
        this.types[this.index] = getValueType(value);
        this.index++;
        return this;
    }

    public int getIndex() {
        return index;
    }

    public int getLength() {
        return length;
    }
}
