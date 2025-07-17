package cn.katoumegumi.java.excel;

/**
 * excel表列属性
 * @param <T> 行数据类型
 * @author ws
 */
public class ExcelTableColumnProperty<T> {

    /**
     * 行名
     */
    private String columnName;

    /**
     * 当前列单个单元格宽度
     */
    private Integer columnWidth;

    /**
     * 当前列单元格高度
     */
    private Short columnHeight;

    /**
     * 列横向占用单元格数量
     */
    private Integer columnWidthCellSize = 1;
    /**
     * 列纵向占用单元格数量
     */
    private Integer columnHeightCellSize = 1;

    /**
     * 表头设置
     */
    private ExcelTableHeadCellFill excelTableHeadCellFill;

    /**
     * 表体设置
     */
    private ExcelTableBodyCellFill<T> excelTableBodyCellFill;

    /**
     * 表尾设置
     */
    private ExcelTableFootCellFill excelTableFootCellFill;

    public String getColumnName() {
        return columnName;
    }

    public ExcelTableColumnProperty<T> setColumnName(String columnName) {
        this.columnName = columnName;
        return this;
    }

    public Integer getColumnWidth() {
        return columnWidth;
    }

    public ExcelTableColumnProperty<T> setColumnWidth(Integer columnWidth) {
        this.columnWidth = columnWidth;
        return this;
    }

    public Short getColumnHeight() {
        return columnHeight;
    }

    public ExcelTableColumnProperty<T> setColumnHeight(Short columnHeight) {
        this.columnHeight = columnHeight;
        return this;
    }

    public Integer getColumnWidthCellSize() {
        return columnWidthCellSize;
    }

    public ExcelTableColumnProperty<T> setColumnWidthCellSize(Integer columnWidthCellSize) {
        this.columnWidthCellSize = columnWidthCellSize;
        return this;
    }

    public Integer getColumnHeightCellSize() {
        return columnHeightCellSize;
    }

    public ExcelTableColumnProperty<T> setColumnHeightCellSize(Integer columnHeightCellSize) {
        this.columnHeightCellSize = columnHeightCellSize;
        return this;
    }

    public ExcelTableHeadCellFill getExcelTableHeadCellFill() {
        return excelTableHeadCellFill;
    }

    public ExcelTableColumnProperty<T> setExcelTableHeadCellFill(ExcelTableHeadCellFill excelTableHeadCellFill) {
        this.excelTableHeadCellFill = excelTableHeadCellFill;
        return this;
    }

    public ExcelTableBodyCellFill<T> getExcelTableBodyCellFill() {
        return excelTableBodyCellFill;
    }

    public ExcelTableColumnProperty<T> setExcelTableBodyCellFill(ExcelTableBodyCellFill<T> excelTableBodyCellFill) {
        this.excelTableBodyCellFill = excelTableBodyCellFill;
        return this;
    }

    public ExcelTableFootCellFill getExcelTableFootCellFill() {
        return excelTableFootCellFill;
    }

    public ExcelTableColumnProperty<T> setExcelTableFootCellFill(ExcelTableFootCellFill excelTableFootCellFill) {
        this.excelTableFootCellFill = excelTableFootCellFill;
        return this;
    }
}
