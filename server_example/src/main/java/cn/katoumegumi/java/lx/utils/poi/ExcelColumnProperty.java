package cn.katoumegumi.java.lx.utils.poi;

/**
 * excel行属性
 * @author ws
 */
public class ExcelColumnProperty<T> {

    /**
     * 行号
     */
    private Integer index;

    /**
     * 行名
     */
    private String columnName;

    /**
     * 行的宽度
     */
    private Integer columnWidth;


    /**
     * 行的大小
     */
    private Integer columnSize = 1;


    /**
     * cell填充方法
     */
    private ExcelCellFill<T> excelCellFill;

    /**
     * 表头风格设置
     */
    private ExcelColumnStyle excelColumnStyle;

    /**
     * 表尾部设置
     */
    private ExcelColumnEndFill excelColumnEndFill;


    public Integer getIndex() {
        return index;
    }

    public ExcelColumnProperty<T> setIndex(Integer index) {
        this.index = index;
        return this;
    }

    public String getColumnName() {
        return columnName;
    }

    public ExcelColumnProperty<T> setColumnName(String columnName) {
        this.columnName = columnName;
        return this;
    }

    public ExcelCellFill<T> getExcelCellFill() {
        return excelCellFill;
    }

    public ExcelColumnProperty<T> setExcelCellFill(ExcelCellFill<T> excelCellFill) {
        this.excelCellFill = excelCellFill;
        return this;
    }

    public ExcelColumnStyle getExcelColumnStyle() {
        return excelColumnStyle;
    }

    public ExcelColumnProperty<T> setExcelColumnStyle(ExcelColumnStyle excelColumnStyle) {
        this.excelColumnStyle = excelColumnStyle;
        return this;
    }

    public ExcelColumnProperty<T> setColumnWidth(Integer columnWidth) {
        this.columnWidth = columnWidth;
        return this;
    }

    public Integer getColumnWidth() {
        return columnWidth;
    }

    public ExcelColumnEndFill getExcelColumnEndFill() {
        return excelColumnEndFill;
    }

    public ExcelColumnProperty<T> setExcelColumnEndFill(ExcelColumnEndFill excelColumnEndFill) {
        this.excelColumnEndFill = excelColumnEndFill;
        return this;
    }

    public Integer getColumnSize() {
        return columnSize;
    }

    public ExcelColumnProperty<T> setColumnSize(Integer columnSize) {
        this.columnSize = columnSize;
        return this;
    }
}
