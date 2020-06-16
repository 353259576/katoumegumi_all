package cn.katoumegumi.java.lx.utils;

import cn.katoumegumi.java.common.WsImageUtils;
import com.alibaba.fastjson.JSON;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.hssf.usermodel.HSSFWorkbookFactory;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.*;
import org.springframework.util.StringUtils;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
public class PoiUtis {

    public static void main(String[] args) {
        executorMyExcel();
    }


    public static void executorMyExcel() {
        File oldFile = new File("D:\\网页\\含山南.xls");
        try {
            Workbook workbook = HSSFWorkbookFactory.create(oldFile);
            Sheet sheet = workbook.getSheetAt(0);
            Integer rowNum = sheet.getPhysicalNumberOfRows();
            log.info("一共{}行", rowNum);
            int startRow = 3;
            int startColumn = 2;

            List<List<ObjValue>> lists = new ArrayList<>();

            while (true) {
                Row row = sheet.getRow(startRow);
                if (row == null) {
                    break;
                }
                Integer colLength = row.getPhysicalNumberOfCells();


                while (true) {


                    row = sheet.getRow(startRow);
                    Cell cell = row.getCell(startColumn);


                    if (cell == null) {
                        break;
                    }


                    ObjValue objValue = new ObjValue();
                    objValue.setName("区段");
                    objValue.setValue(cell.getStringCellValue());
                    if (org.apache.commons.lang3.StringUtils.isBlank(objValue.getValue())) {
                        break;
                    }

                    List<ObjValue> list = new ArrayList<>();
                    lists.add(list);
                    list.add(objValue);

                    startRow += 2;

                    for (int i = 0; i < 6; i++) {
                        objValue = new ObjValue();
                        row = sheet.getRow(startRow);
                        cell = row.getCell(startColumn);
                        objValue.setName(cell.getStringCellValue());
                        cell = row.getCell(startColumn + 1);
                        objValue.setValue(cell.getNumericCellValue() + "");
                        list.add(objValue);
                        startRow += 1;
                    }
                    startRow -= 6;
                    startColumn += 3;

                    for (int i = 0; i < 7; i++) {
                        objValue = new ObjValue();
                        row = sheet.getRow(startRow);
                        cell = row.getCell(startColumn);
                        objValue.setName(cell.getStringCellValue());
                        cell = row.getCell(startColumn - 1);
                        objValue.setValue(cell.getNumericCellValue() + "");
                        list.add(objValue);
                        startRow += 1;
                    }
                    startRow -= 9;
                    startColumn += 2;


                }
                startColumn = 2;
                startRow += 10;


            }


            log.info(JSON.toJSONString(lists));

            Map<String, String> bdName = new HashMap<>();

            sheet = workbook.getSheetAt(1);
            Integer maxRowNum = sheet.getPhysicalNumberOfRows();
            Integer nowIndex = 0;
            while (maxRowNum > 0) {
                Row row = sheet.getRow(nowIndex);
                if (row != null) {
                    maxRowNum--;
                    Integer columnNum = row.getPhysicalNumberOfCells();
                    for (int j = 0; j < columnNum; j++) {
                        Cell cell = row.getCell(j);
                        if (cell != null) {
                            switch (cell.getCellType()) {
                                case STRING:
                                    String value = cell.getStringCellValue();
                                    if (!StringUtils.isEmpty(value)) {
                                        if (value.contains("(")) {
                                            log.info(value);
                                            String strs[] = value.split(" ");
                                            bdName.put(strs[0], strs[1].substring(1, strs[1].length() - 1));
                                        }
                                    }
                                    break;
                                default:
                                    break;
                            }
                        }
                    }
                }
                nowIndex++;
            }


            XSSFWorkbook xssfWorkbook = XSSFWorkbookFactory.createWorkbook();
            XSSFSheet xssfSheet = xssfWorkbook.createSheet();
            for (int i = 0; i < lists.size(); i++) {
                List<ObjValue> list = lists.get(i);
                if (i == 0) {
                    XSSFRow xssfRow = xssfSheet.createRow(0);
                    for (int j = 0; j < list.size(); j++) {
                        XSSFCell xssfCell = xssfRow.createCell(j);
                        xssfCell.setCellValue(list.get(j).getName());
                    }
                    XSSFCell xssfCell = xssfRow.createCell(list.size());
                    xssfCell.setCellValue("频段");
                }

                XSSFRow xssfRow = xssfSheet.createRow(i + 1);
                for (int j = 0; j < list.size(); j++) {
                    XSSFCell xssfCell = xssfRow.createCell(j);
                    xssfCell.setCellValue(list.get(j).getValue());
                }
                String v = bdName.get(list.get(0).getValue());
                if (!StringUtils.isEmpty(v)) {
                    XSSFCell xssfCell = xssfRow.createCell(list.size());
                    xssfCell.setCellValue(v);
                }
            }
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            xssfWorkbook.write(outputStream);
            outputStream.close();
            byte[] bytes = outputStream.toByteArray();
            WsImageUtils.byteToFile(bytes, "2", "xls", "D:\\网页\\");

        } catch (IOException e) {
            e.printStackTrace();
        }

    }


    @Data
    static class ObjValue {
        private String name;
        private String value;

        public ObjValue() {
        }

        public ObjValue(String name, String value) {
            this.name = name;
            this.value = value;
        }

    }

}
