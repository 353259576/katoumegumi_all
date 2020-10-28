package cn.katoumegumi.java.lx.utils.poi;

import cn.katoumegumi.java.common.WsListUtils;
import cn.katoumegumi.java.common.WsStringUtils;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

/**
 * excel表格生成器
 * @author ws
 */
public class ExcelGenerator<T> {

    private String title;

    private List<ExcelColumnProperty<T>> excelColumnPropertyList = new ArrayList<>();

    private List<T> valueList;

    private ExcelColumnStyle excelColumnStyle;

    public static <T> ExcelGenerator<T> create(List<T> tList){
        ExcelGenerator<T> excelGenerator = new ExcelGenerator<T>();
        excelGenerator.setValueList(tList);
        return excelGenerator;
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

    public byte[] build(){
        if(WsListUtils.isEmpty(excelColumnPropertyList)) {
            throw new RuntimeException("行配置为空");
        }
        Workbook workbook = new XSSFWorkbook();
        Sheet sheet = workbook.createSheet();
        int rowNum = 0;
        Row row = null;
        Cell cell = null;
        if(WsStringUtils.isNotBlank(title)) {
            CellRangeAddress cellAddresses = new CellRangeAddress(0, 0, 0, excelColumnPropertyList.size() - 1);
            sheet.addMergedRegion(cellAddresses);
            CellStyle cellStyle = workbook.createCellStyle();
            cellStyle.setAlignment(HorizontalAlignment.CENTER);
            if(excelColumnStyle != null){
                excelColumnStyle.setStyle(cellStyle);
            }
            row = sheet.createRow(rowNum);
            cell = row.createCell(0);
            cell.setCellStyle(cellStyle);
            cell.setCellValue(title);
            rowNum++;
        }
        row = sheet.createRow(rowNum);
        rowNum++;
        ExcelColumnProperty<T> columnProperty = null;
        for(int i = 0; i < excelColumnPropertyList.size(); i++){
            cell = row.createCell(i);
            columnProperty = excelColumnPropertyList.get(i);
            if(columnProperty.getExcelColumnStyle() != null){
                CellStyle cellStyle = workbook.createCellStyle();
                columnProperty.getExcelColumnStyle().setStyle(cellStyle);
                cell.setCellStyle(cellStyle);
            }
            if(columnProperty.getColumnWidth() != null){
                sheet.setColumnWidth(i,columnProperty.getColumnWidth());
            }
            cell.setCellValue(columnProperty.getColumnName());
        }
        for (T t : valueList) {
            row = sheet.createRow(rowNum);
            rowNum++;
            for (int i = 0; i < excelColumnPropertyList.size(); i++) {
                cell = row.createCell(i);
                columnProperty = excelColumnPropertyList.get(i);
                if (columnProperty.getExcelCellFill() != null) {
                    ExcelPointLocation excelPointLocation = new ExcelPointLocation(i,rowNum,columnProperty.getColumnName(),cell,row,sheet,workbook);
                    columnProperty.getExcelCellFill().fill(excelPointLocation, t);
                }
            }
        }
        byte[] returnBytes = null;
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        try {
            workbook.write(byteArrayOutputStream);
            returnBytes = byteArrayOutputStream.toByteArray();
        } catch (IOException e) {
            e.printStackTrace();
        }finally {
            try {
                workbook.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
            try {
                byteArrayOutputStream.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return returnBytes;
    }

}
