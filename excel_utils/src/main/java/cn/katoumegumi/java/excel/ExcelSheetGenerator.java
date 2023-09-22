package cn.katoumegumi.java.excel;


import cn.katoumegumi.java.common.WsCollectionUtils;
import cn.katoumegumi.java.common.WsStringUtils;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.CellRangeAddress;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;

public class ExcelSheetGenerator<T> {

    private final ExcelGenerator<?> excelGenerator;

    private final Workbook workbook;

    private final Sheet sheet;

    private String title;

    /**
     * 表格标题样式
     */
    private ExcelTableTitleCellFill excelTableTitleCellFill;

    /**
     * 行的处理方式
     */
    private final List<ExcelTableColumnProperty<T>> excelTableColumnPropertyList = new ArrayList<>();


    private final AtomicInteger rowNum = new AtomicInteger(0);

    /**
     * 0 待创建 1 创建标题 2 创建表头 3 创建表体 4 创建表尾 5 创建完成
     */
    private volatile int state = 0;


    public ExcelSheetGenerator(ExcelGenerator excelGenerator, Sheet sheet) {
        this.excelGenerator = excelGenerator;
        this.workbook = sheet.getWorkbook();
        this.sheet = sheet;
    }


    public ExcelSheetGenerator<T> createTitle(){
        if (state != 0){
            throw new IllegalStateException("状态错误，当前无法创建标题");
        }
        if (this.state == 0){
            this.state = 1;
        }
        if (WsStringUtils.isNotBlank(title)) {
            int tableColumnWidthSize = getTableWidthCellSize();
            if(tableColumnWidthSize > 1) {
                CellRangeAddress cellAddresses = new CellRangeAddress(0, 0, 0, tableColumnWidthSize - 1);
                excelGenerator.addMergedRegion(sheet,cellAddresses);
            }
            Row row = sheet.createRow(rowNum.getAndAdd(1));
            Cell cell = row.createCell(0);

            CellStyle cellStyle = workbook.createCellStyle();
            cellStyle.setAlignment(HorizontalAlignment.CENTER);
            if (excelTableTitleCellFill != null){
                excelTableTitleCellFill.fill(
                        new ExcelPointLocation<>(cell,null)
                );
            }
            cell.setCellStyle(cellStyle);
            cell.setCellValue(title);
        }
        return this;
    }

    /**
     * 创建表头
     * @return
     */
    public ExcelSheetGenerator<T> createTableHead() throws InterruptedException {
        if (state != 0 && state != 1){
            throw new IllegalStateException("状态错误");
        }
        if (this.state == 0){
            this.state = 2;
        }
        Row row = sheet.createRow(rowNum.get());
        ExcelTableColumnProperty<T> columnProperty;
        int columnNum = 0;
        int maxHeadColumnCellSize = 0;
        for (ExcelTableColumnProperty<T> tExcelTableColumnProperty : excelTableColumnPropertyList) {
            excelGenerator.checkThread(Thread.currentThread());
            Cell cell = row.createCell(columnNum);
            columnProperty = tExcelTableColumnProperty;
            maxHeadColumnCellSize = Math.max(maxHeadColumnCellSize, columnProperty.getColumnHeightCellSize());
            if (columnProperty.getColumnHeightCellSize() > 1 || columnProperty.getColumnWidthCellSize() > 1) {
                CellRangeAddress addresses = new CellRangeAddress(cell.getRowIndex(), cell.getRowIndex() + columnProperty.getColumnHeightCellSize() - 1, columnNum, columnNum + columnProperty.getColumnWidthCellSize() - 1);
                excelGenerator.addMergedRegion(cell.getSheet(), addresses);
            }
            if (columnProperty.getColumnWidth() != null) {
                sheet.setColumnWidth(columnNum, columnProperty.getColumnWidth());
            }
            cell.setCellValue(columnProperty.getColumnName());

            if (columnProperty.getExcelTableHeadCellFill() != null) {
                ExcelPointLocation<T> excelPointLocation = new ExcelPointLocation<>(cell, null);
                columnProperty.getExcelTableHeadCellFill().fill(excelPointLocation);
                if (excelPointLocation.getCellStyleNotAutoCreate() != null){
                    excelPointLocation.getCell().setCellStyle(excelPointLocation.getCellStyle());
                }
            }
            columnNum += columnProperty.getColumnWidthCellSize();
        }
        rowNum.getAndAdd(maxHeadColumnCellSize);
        return this;
    }

    public ExcelSheetGenerator<T> createTableBody(List<T> tList) throws InterruptedException {
        if (state >= 4){
            throw new IllegalStateException("状态错误");
        }
        state = 3;
        if (WsCollectionUtils.isEmpty(tList)){
            return this;
        }

        Object globalValue = null;
        for (T t : tList) {
            excelGenerator.checkThread(Thread.currentThread());
            Row row = sheet.createRow(rowNum.get());
            int columnNum = 0;
            int maxHeadColumnCellSize = 0;
            short rowHeight = -1;
            for (ExcelTableColumnProperty<T> columnProperty : excelTableColumnPropertyList) {
                Cell cell = row.createCell(columnNum);
                if (columnProperty.getExcelTableBodyCellFill() != null) {
                    ExcelPointLocation<T> excelPointLocation = new ExcelPointLocation<>(cell,columnProperty);
                    excelPointLocation.setGlobalValue(globalValue);
                    excelPointLocation.setRowValue(t);
                    columnProperty.getExcelTableBodyCellFill().fill(excelPointLocation, t);
                    globalValue = excelPointLocation.getGlobalValue();
                    if (excelPointLocation.getCellStyleNotAutoCreate() != null){
                        excelPointLocation.getCell().setCellStyle(excelPointLocation.getCellStyle());
                    }
                }
                columnNum += columnProperty.getColumnWidthCellSize();
                maxHeadColumnCellSize = Math.max(maxHeadColumnCellSize,columnProperty.getColumnHeightCellSize());
                if (columnProperty.getColumnHeight() != null){
                    rowHeight = rowHeight > columnProperty.getColumnHeight()?rowHeight:columnProperty.getColumnHeight();
                }
            }
            if (rowHeight >= 0){
                row.setHeight(rowHeight);
            }
            rowNum.addAndGet(maxHeadColumnCellSize);
        }
        return this;

    }

    public ExcelSheetGenerator<T> createTableFoot() throws InterruptedException {
        if (state > 4){
            throw new IllegalStateException("状态错误");
        }
        state = 4;
        Row row = sheet.createRow(rowNum.get());
        int columnNum = 0;
        int maxHeadColumnCellSize = 0;
        for (ExcelTableColumnProperty<T> columnProperty : excelTableColumnPropertyList) {
            excelGenerator.checkThread(Thread.currentThread());
            ExcelTableFootCellFill columnEndFill = columnProperty.getExcelTableFootCellFill();
            Cell cell = row.createCell(columnNum);
            if (columnProperty.getColumnHeightCellSize() > 1 || columnProperty.getColumnWidthCellSize() > 1) {
                CellRangeAddress addresses = new CellRangeAddress(cell.getRowIndex(), cell.getRowIndex() + columnProperty.getColumnHeightCellSize() - 1, columnNum, columnNum + columnProperty.getColumnWidthCellSize() - 1);
                excelGenerator.addMergedRegion(cell.getSheet(), addresses);
            }
            if (columnEndFill != null) {
                ExcelPointLocation<T> excelPointLocation = new ExcelPointLocation<>(cell, null);
                columnEndFill.fill(excelPointLocation);
                if (excelPointLocation.getCellStyleNotAutoCreate() != null){
                    excelPointLocation.getCell().setCellStyle(excelPointLocation.getCellStyle());
                }
            }
            columnNum += columnProperty.getColumnWidthCellSize();
            maxHeadColumnCellSize = Math.max(maxHeadColumnCellSize, columnProperty.getColumnHeightCellSize());
        }
        rowNum.getAndAdd(maxHeadColumnCellSize);
        return this;
    }


    /**
     * 获取表列宽所占的单元格大小
     * @return
     */
    private int getTableWidthCellSize(){
        int cellSize = 0;
        for (ExcelTableColumnProperty<T> tExcelTableColumnProperty : excelTableColumnPropertyList) {
            cellSize += tExcelTableColumnProperty.getColumnWidthCellSize();
        }
        return cellSize;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public ExcelTableTitleCellFill getExcelTableTitleCellFill() {
        return excelTableTitleCellFill;
    }

    public void setExcelTableTitleCellFill(ExcelTableTitleCellFill excelTableTitleCellFill) {
        this.excelTableTitleCellFill = excelTableTitleCellFill;
    }

    public ExcelSheetGenerator<T> addColumnProperty(Consumer<ExcelTableColumnProperty<T>> consumer) {
        ExcelTableColumnProperty<T> excelColumnProperty = new ExcelTableColumnProperty<>();
        consumer.accept(excelColumnProperty);
        excelTableColumnPropertyList.add(excelColumnProperty);
        return this;
    }


}
