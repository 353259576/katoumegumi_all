package cn.katoumegumi.java.excel;

import org.apache.poi.ss.usermodel.*;
import org.openxmlformats.schemas.spreadsheetml.x2006.main.CTWorksheet;

/**
 * 当前位置坐标
 * @param <T> 行数据类型
 * @author ws
 */
public class ExcelPointLocation<T> {

    private final Integer columnNum;

    private final Integer rowNum;

    private final Cell cell;

    private final Row row;

    private final Sheet sheet;

    private final Workbook workbook;

    private final ExcelTableColumnProperty<T> excelTableColumnProperty;

    private CellStyle cellStyle;

    private volatile T rowValue;

    private volatile Object globalValue;


    public ExcelPointLocation(Cell cell,
                              ExcelTableColumnProperty<T> excelTableColumnProperty) {
        this.columnNum = cell.getColumnIndex();
        this.rowNum = cell.getRowIndex();
        this.cell = cell;
        this.row = cell.getRow();
        this.sheet = this.row.getSheet();
        this.workbook = this.sheet.getWorkbook();
        this.excelTableColumnProperty = excelTableColumnProperty;
    }

    public Integer getColumnNum() {
        return columnNum;
    }

    public Integer getRowNum() {
        return rowNum;
    }

    public Cell getCell() {
        return cell;
    }

    public Row getRow() {
        return row;
    }

    public Sheet getSheet() {
        return sheet;
    }

    public Workbook getWorkbook() {
        return workbook;
    }

    public ExcelTableColumnProperty<T> getExcelTableColumnProperty() {
        return excelTableColumnProperty;
    }

    public CellStyle getCellStyle() {
        if (this.cellStyle == null){
            this.cellStyle = workbook.createCellStyle();
        }
        return cellStyle;
    }

    public CellStyle getCellStyleNotAutoCreate() {
        return cellStyle;
    }

    public void setCellStyle(CellStyle cellStyle) {
        this.cellStyle = cellStyle;
    }

    public T getRowValue() {
        return rowValue;
    }

    public void setRowValue(T rowValue) {
        this.rowValue = rowValue;
    }

    public Object getGlobalValue() {
        return globalValue;
    }

    public void setGlobalValue(Object globalValue) {
        this.globalValue = globalValue;
    }
}
