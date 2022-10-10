package cn.katoumegumi.java.excel;

/**
 * excel单元格填充
 * @author ws
 */
public interface ExcelCellFill<T> {

    /**
     * 填充单元格的方法
     * @param location 当前位置
     * @param t 参数
     */
    void fill(ExcelPointLocation location, T t);


}
