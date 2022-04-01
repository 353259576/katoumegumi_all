package cn.katoumegumi.java.excel;

import cn.katoumegumi.java.common.WsListUtils;
import cn.katoumegumi.java.common.WsStreamUtils;
import cn.katoumegumi.java.common.WsStringUtils;
import org.apache.poi.ss.SpreadsheetVersion;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.streaming.SXSSFWorkbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.openxmlformats.schemas.spreadsheetml.x2006.main.CTMergeCell;
import org.openxmlformats.schemas.spreadsheetml.x2006.main.CTMergeCells;
import org.openxmlformats.schemas.spreadsheetml.x2006.main.CTWorksheet;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

/**
 * excel表格生成器
 *
 * @author ws
 */
public class ExcelGenerator<T> {
    /**
     * 表格名称
     */
    private String title;

    /**
     * 行的处理方式
     */
    private List<ExcelColumnProperty<T>> excelColumnPropertyList = new ArrayList<>();

    /**
     * 数据
     */
    private List<T> valueList;

    /**
     * 表格标题样式
     */
    private ExcelColumnStyle excelColumnStyle;

    public static <T> ExcelGenerator<T> create(List<T> tList) {
        ExcelGenerator<T> excelGenerator = new ExcelGenerator<T>();
        excelGenerator.setValueList(tList);
        return excelGenerator;
    }

    /**
     * 计算Excel对应横向格子id
     *
     * @param index
     * @return
     */
    public static String getExcelIndex(Integer index) {
        //index -= 1;
        List<Integer> list = new ArrayList<>();
        while (index > 0) {
            int z = index % 26;
            index = index / 26;
            if (z == 0) {
                if (index > 0) {
                    z = 26;
                    index = 0;
                }
            }
            list.add(z);

        }

        StringBuilder sb = new StringBuilder();
        for (int i = list.size() - 1; i >= 0; i--) {
            sb.append((char) (list.get(i) + 64));
        }
        return sb.toString();
    }

    public String getTitle() {
        return title;
    }

    public ExcelGenerator<T> setTitle(String title) {
        this.title = title;
        return this;
    }

    public List<ExcelColumnProperty<T>> getExcelColumnPropertyList() {
        return excelColumnPropertyList;
    }

    public ExcelGenerator<T> setExcelColumnPropertyList(List<ExcelColumnProperty<T>> excelColumnPropertyList) {
        this.excelColumnPropertyList = excelColumnPropertyList;
        return this;
    }

    public List<T> getValueList() {
        return valueList;
    }

    public ExcelGenerator<T> setValueList(List<T> valueList) {
        this.valueList = valueList;
        return this;
    }

    public ExcelGenerator<T> addColumnProperty(Consumer<ExcelColumnProperty<T>> consumer) {
        ExcelColumnProperty<T> excelColumnProperty = new ExcelColumnProperty<>();
        consumer.accept(excelColumnProperty);
        excelColumnPropertyList.add(excelColumnProperty);
        return this;
    }

    public ExcelGenerator<T> setExcelColumnStyle(ExcelColumnStyle excelColumnStyle) {
        this.excelColumnStyle = excelColumnStyle;
        return this;
    }

    public byte[] build() {
        if (WsListUtils.isEmpty(excelColumnPropertyList)) {
            throw new RuntimeException("行配置为空");
        }
        Thread currentThread = Thread.currentThread();
        SXSSFWorkbook workbook = null;
        try {
            workbook = new SXSSFWorkbook();
            Sheet sheet = workbook.createSheet();
            CTWorksheet ctWorksheet = workbook.getXSSFWorkbook().getSheetAt(0).getCTWorksheet();
            int rowNum = 0;
            Row row = null;
            Cell cell = null;
            if (WsStringUtils.isNotBlank(title)) {
                if(excelColumnPropertyList.size() > 1) {
                    CellRangeAddress cellAddresses = new CellRangeAddress(0, 0, 0, excelColumnPropertyList.size() - 1);
                    //sheet.addMergedRegion(cellAddresses);
                    ExcelGenerator.addMergedRegion(ctWorksheet, cellAddresses);
                }
                CellStyle cellStyle = workbook.createCellStyle();
                cellStyle.setAlignment(HorizontalAlignment.CENTER);
                if (excelColumnStyle != null) {
                    excelColumnStyle.setStyle(cellStyle);
                }
                row = sheet.createRow(rowNum);
                cell = row.createCell(0);
                cell.setCellStyle(cellStyle);
                cell.setCellValue(title);
                rowNum++;
            }
            checkThread(currentThread);

            row = sheet.createRow(rowNum);


            ExcelColumnProperty<T> columnProperty = null;
            int columnNum = 0;
            for (int i = 0; i < excelColumnPropertyList.size(); i++) {
                checkThread(currentThread);
                cell = row.createCell(columnNum);
                columnProperty = excelColumnPropertyList.get(i);
                if (columnProperty.getColumnSize() > 1) {
                    CellRangeAddress addresses = new CellRangeAddress(rowNum, rowNum, columnNum, columnNum + columnProperty.getColumnSize() - 1);
                    //sheet.addMergedRegion(addresses);
                    ExcelGenerator.addMergedRegion(ctWorksheet,addresses);
                }

                if (columnProperty.getExcelColumnStyle() != null) {
                    CellStyle cellStyle = workbook.createCellStyle();
                    columnProperty.getExcelColumnStyle().setStyle(cellStyle);
                    cell.setCellStyle(cellStyle);
                }
                if (columnProperty.getColumnWidth() != null) {
                    sheet.setColumnWidth(columnNum, columnProperty.getColumnWidth());
                }
                cell.setCellValue(columnProperty.getColumnName());
                columnNum += columnProperty.getColumnSize();
            }
            rowNum++;

            Object globalValue = null;
            for (T t : valueList) {
                checkThread(currentThread);
                row = sheet.createRow(rowNum);
                columnNum = 0;
                Object rowValue = null;
                for (int i = 0; i < excelColumnPropertyList.size(); i++) {
                    checkThread(currentThread);
                    cell = row.createCell(columnNum);
                    columnProperty = excelColumnPropertyList.get(i);
                    if (columnProperty.getExcelCellFill() != null) {
                        ExcelPointLocation excelPointLocation = new ExcelPointLocation(columnNum, rowNum, columnProperty.getColumnSize(), columnProperty.getColumnName(), cell, row, sheet,ctWorksheet, workbook);
                        excelPointLocation.setGlobalValue(globalValue);
                        excelPointLocation.setRowValue(rowValue);
                        columnProperty.getExcelCellFill().fill(excelPointLocation, t);
                        globalValue = excelPointLocation.getGlobalValue();
                        rowValue = excelPointLocation.getRowValue();
                    }
                    columnNum += columnProperty.getColumnSize();
                }
                rowValue = null;
                rowNum++;
            }


            row = sheet.createRow(rowNum);
            columnNum = 0;
            for (int i = 0; i < excelColumnPropertyList.size(); i++) {
                checkThread(currentThread);
                ExcelColumnProperty<T> excelColumnProperty = excelColumnPropertyList.get(i);
                ExcelColumnEndFill columnEndFill = excelColumnProperty.getExcelColumnEndFill();
                if (columnEndFill != null) {
                    ExcelPointLocation excelPointLocation = new ExcelPointLocation(columnNum, rowNum, excelColumnProperty.getColumnSize(), excelColumnProperty.getColumnName(), row.createCell(columnNum), row, sheet,ctWorksheet, workbook);
                    columnEndFill.fill(excelPointLocation);
                }
                columnNum += excelColumnProperty.getColumnSize();
            }
        }catch (InterruptedException e){
            if(workbook != null){
                WsStreamUtils.close(workbook);
            }
            throw new RuntimeException(e);
        }

        if(workbook == null){
            return new byte[0];
        }

        byte[] returnBytes = null;
        try (ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();){
            workbook.write(byteArrayOutputStream);
            returnBytes = byteArrayOutputStream.toByteArray();
        } catch (IOException e) {
            e.printStackTrace();
        }
        WsStreamUtils.close(workbook);
        return returnBytes;
    }

    /**
     * 合并单元格去除了验证
     * @param worksheet
     * @param region
     * @return
     */
    public static int addMergedRegion(CTWorksheet worksheet,CellRangeAddress region) {
        if (region.getNumberOfCells() < 2) {
            throw new IllegalArgumentException("Merged region " + region.formatAsString() + " must contain 2 or more cells");
        }
        CTMergeCells ctMergeCells = worksheet.isSetMergeCells() ? worksheet.getMergeCells() : worksheet.addNewMergeCells();
        CTMergeCell ctMergeCell = ctMergeCells.addNewMergeCell();
        ctMergeCell.setRef(region.formatAsString());
        long count = ctMergeCells.getCount();
        if (count == 0) {
            count=ctMergeCells.sizeOfMergeCellArray();
        } else {
            count++;
        }
        ctMergeCells.setCount(count);
        return Math.toIntExact(count-1);
    }


    public void checkThread(Thread thread) throws InterruptedException {
        if(thread.isInterrupted()){
            throw new InterruptedException("当前线程已被标记为中断，excel生成终止");
        }
    }


}
