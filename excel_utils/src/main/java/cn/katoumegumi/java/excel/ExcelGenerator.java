package cn.katoumegumi.java.excel;

import cn.katoumegumi.java.common.*;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.streaming.SXSSFSheet;
import org.apache.poi.xssf.streaming.SXSSFWorkbook;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.openxmlformats.schemas.spreadsheetml.x2006.main.CTMergeCell;
import org.openxmlformats.schemas.spreadsheetml.x2006.main.CTMergeCells;
import org.openxmlformats.schemas.spreadsheetml.x2006.main.CTWorksheet;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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

    private final Map<CTWorksheet,Integer> mergeCellCountMap = new HashMap<>();

    private static final Field SXSSFSHEET_SH_FIELD = WsReflectUtils.getFieldByName(SXSSFSheet.class,"_sh");

    private static final Field XSSFSHEET_WORKSHEET_FIELD = WsReflectUtils.getFieldByName(XSSFSheet.class,"worksheet");

    public static <T> ExcelGenerator<T> create(List<T> tList) {
        ExcelGenerator<T> excelGenerator = new ExcelGenerator<>();
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
            //CTWorksheet ctWorksheet = workbook.getXSSFWorkbook().getSheetAt(0).getCTWorksheet();
            int rowNum = 0;
            Row row;
            Cell cell;
            if (WsStringUtils.isNotBlank(title)) {
                if(excelColumnPropertyList.size() > 1) {
                    CellRangeAddress cellAddresses = new CellRangeAddress(0, 0, 0, excelColumnPropertyList.size() - 1);
                    //sheet.addMergedRegion(cellAddresses);
                    sheet.addMergedRegionUnsafe(cellAddresses);
                    //ExcelGenerator.addMergedRegion(ctWorksheet, cellAddresses);
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


            ExcelColumnProperty<T> columnProperty;
            int columnNum = 0;
            for (int i = 0; i < excelColumnPropertyList.size(); i++) {
                checkThread(currentThread);
                cell = row.createCell(columnNum);
                columnProperty = excelColumnPropertyList.get(i);
                if (columnProperty.getColumnSize() > 1) {
                    CellRangeAddress addresses = new CellRangeAddress(rowNum, rowNum, columnNum, columnNum + columnProperty.getColumnSize() - 1);
                    sheet.addMergedRegionUnsafe(addresses);
                    //ExcelGenerator.addMergedRegion(ctWorksheet,addresses);
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
                        ExcelPointLocation excelPointLocation = new ExcelPointLocation(columnNum, rowNum, columnProperty.getColumnSize(), columnProperty.getColumnName(), cell, row, sheet,null, workbook);
                        excelPointLocation.setGlobalValue(globalValue);
                        excelPointLocation.setRowValue(rowValue);
                        columnProperty.getExcelCellFill().fill(excelPointLocation, t);
                        globalValue = excelPointLocation.getGlobalValue();
                        rowValue = excelPointLocation.getRowValue();
                    }
                    columnNum += columnProperty.getColumnSize();
                }
                rowNum++;
            }


            row = sheet.createRow(rowNum);
            columnNum = 0;
            for (int i = 0; i < excelColumnPropertyList.size(); i++) {
                checkThread(currentThread);
                ExcelColumnProperty<T> excelColumnProperty = excelColumnPropertyList.get(i);
                ExcelColumnEndFill columnEndFill = excelColumnProperty.getExcelColumnEndFill();
                if (columnEndFill != null) {
                    ExcelPointLocation excelPointLocation = new ExcelPointLocation(columnNum, rowNum, excelColumnProperty.getColumnSize(), excelColumnProperty.getColumnName(), row.createCell(columnNum), row, sheet,null, workbook);
                    columnEndFill.fill(excelPointLocation);
                }
                columnNum += excelColumnProperty.getColumnSize();
            }
        }catch (InterruptedException e){
            WsStreamUtils.close(workbook);
            throw new RuntimeException(e);
        }

        byte[] returnBytes = null;
        try (ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream()){
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
     * @param sheet
     * @param region
     * @return
     */
    public void addMergedRegion(Sheet sheet,CellRangeAddress region) {
        if (region.getNumberOfCells() < 2) {
            throw new IllegalArgumentException("Merged region " + region.formatAsString() + " must contain 2 or more cells");
        }
        //sheet.addMergedRegionUnsafe()
        XSSFSheet xssfSheet = null;
        if (sheet instanceof SXSSFSheet){
            xssfSheet = (XSSFSheet) WsReflectUtils.getValue(sheet,SXSSFSHEET_SH_FIELD);
        }else if(sheet instanceof XSSFSheet){
            xssfSheet = (XSSFSheet) sheet;
        }
        if(xssfSheet == null){
            throw new RuntimeException("xssfSheet获取失败");
        }
        CTWorksheet worksheet = (CTWorksheet) WsReflectUtils.getValue(xssfSheet,XSSFSHEET_WORKSHEET_FIELD);
        Integer count = mergeCellCountMap.getOrDefault(worksheet,0);
        CTMergeCells ctMergeCells = count > 0 ? worksheet.getMergeCells() : worksheet.addNewMergeCells();
        CTMergeCell ctMergeCell = ctMergeCells.addNewMergeCell();
        ctMergeCell.setRef(region.formatAsString());
        mergeCellCountMap.put(worksheet,count + 1);
        //return Math.toIntExact(count-1);
    }


    public void checkThread(Thread thread) throws InterruptedException {
        if(thread.isInterrupted()){
            throw new InterruptedException("当前线程已被标记为中断，excel生成终止");
        }
    }


}
