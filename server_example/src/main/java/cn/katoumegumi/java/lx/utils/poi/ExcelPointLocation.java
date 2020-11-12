package cn.katoumegumi.java.lx.utils.poi;

import org.apache.poi.ss.usermodel.*;

/**
 * 当前位置坐标
 * @author ws
 */
public class ExcelPointLocation {

    private final Integer columnNum;

    private final Integer rowNum;

    private final String columnName;

    private final Cell cell;

    private final Row row;

    private final Sheet sheet;

    private final Workbook workbook;

    private final Integer columnSize;

    private CellStyle cellStyle;


    public ExcelPointLocation(Integer columnNum, Integer rowNum,Integer columnSize, String columnName, Cell cell,Row row,Sheet sheet,Workbook workbook) {
        this.columnNum = columnNum;
        this.rowNum = rowNum;
        this.columnSize = columnSize;
        this.columnName = columnName;
        this.cell = cell;
        this.row = row;
        this.sheet = sheet;
        this.workbook = workbook;
    }

    public Integer getColumnNum() {
        return columnNum;
    }

    public Integer getRowNum() {
        return rowNum;
    }

    public String getColumnName() {
        return columnName;
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

    public CellStyle getCellStyle() {
        if(cellStyle == null){
            this.cellStyle = workbook.createCellStyle();
        }
        return cellStyle;
    }

    public Integer getColumnSize() {
        return columnSize;
    }
}
