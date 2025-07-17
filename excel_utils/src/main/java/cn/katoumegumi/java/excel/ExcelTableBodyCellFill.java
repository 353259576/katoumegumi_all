package cn.katoumegumi.java.excel;

/**
 * excel表体单元格填充
 * @param <T> 行数据类型
 * @author ws
 */
public interface ExcelTableBodyCellFill<T> {

    /**
     * 填充单元格的方法
     * @param location 当前位置
     * @param t 参数
     */
    void fill(ExcelPointLocation<T> location, T t);


}
