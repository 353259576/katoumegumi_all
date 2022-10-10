package cn.katoumegumi.java.sql.model;

import java.util.List;

/**
 * sal方法
 */
public class SqlFunctionCondition extends AbstractExpressionCondition {

    private final String functionName;

    private final boolean needBrackets;

    public SqlFunctionCondition(int length, String functionName) {
        super(length);
        this.needBrackets = true;
        this.functionName = functionName;
    }

    public SqlFunctionCondition(Object[] values,String functionName) {
        super(values);
        this.needBrackets = true;
        this.functionName = functionName;
    }

    public SqlFunctionCondition(String functionName,Object... values) {
        super(values);
        this.needBrackets = true;
        this.functionName = functionName;
    }

    public SqlFunctionCondition(boolean needBrackets,int length, String functionName) {
        super(length);
        this.needBrackets = needBrackets;
        this.functionName = functionName;
    }

    public SqlFunctionCondition(boolean needBrackets,Object[] values,String functionName) {
        super(values);
        this.needBrackets = needBrackets;
        this.functionName = functionName;
    }

    public SqlFunctionCondition(boolean needBrackets,String functionName,Object... values) {
        super(values);
        this.needBrackets = needBrackets;
        this.functionName = functionName;
    }

    public String getFunctionName() {
        return functionName;
    }

    public boolean isNeedBrackets() {
        return needBrackets;
    }
}
