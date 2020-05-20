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

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Slf4j
public class PoiUtis {

    public static void main(String[] args) {
        executorMyExcel();
    }


    public static void executorMyExcel(){
        File oldFile = new File("C:\\Users\\10480\\Downloads\\test\\1.xls");
        try {
            Workbook workbook = HSSFWorkbookFactory.create(oldFile);
            Sheet sheet = workbook.getSheetAt(0);
            Integer rowNum = sheet.getPhysicalNumberOfRows();
            log.info("一共{}行",rowNum);
            int startRow = 3;
            int startColumn = 2;

            List<List<ObjValue>> lists = new ArrayList<>();

            while (startRow < rowNum){
                Row row = sheet.getRow(startRow);
                Integer colLength = row.getPhysicalNumberOfCells();


                while (startColumn <= colLength){
                    List<ObjValue> list = new ArrayList<>();
                    lists.add(list);
                    ObjValue objValue = new ObjValue();
                    objValue.setName("名称");
                    row = sheet.getRow(startRow);
                    Cell cell = row.getCell(startColumn);
                    objValue.setValue(cell.getStringCellValue());
                    list.add(objValue);

                    startRow += 2;

                    for(int i = 0; i < 6; i++){
                        objValue = new ObjValue();
                        row = sheet.getRow(startRow);
                        cell = row.getCell(startColumn);
                        objValue.setName(cell.getStringCellValue());
                        cell = row.getCell(startColumn + 1);
                        objValue.setValue(cell.getNumericCellValue()+"");
                        list.add(objValue);
                        startRow += 1;
                    }
                    startRow -= 6;
                    startColumn += 3;

                    for(int i = 0; i < 7; i++){
                        objValue = new ObjValue();
                        row = sheet.getRow(startRow);
                        cell = row.getCell(startColumn);
                        objValue.setName(cell.getStringCellValue());
                        cell = row.getCell(startColumn - 1);
                        objValue.setValue(cell.getNumericCellValue()+"");
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

            XSSFWorkbook xssfWorkbook = XSSFWorkbookFactory.createWorkbook();
            XSSFSheet xssfSheet = xssfWorkbook.createSheet();
            for (int i = 0; i < lists.size(); i++){
                List<ObjValue> list = lists.get(i);
                if(i == 0){
                    XSSFRow xssfRow = xssfSheet.createRow(0);
                    for(int j = 0; j < list.size();j++){
                        XSSFCell xssfCell = xssfRow.createCell(j);
                        xssfCell.setCellValue(list.get(j).getName());
                    }
                }

                XSSFRow xssfRow = xssfSheet.createRow(i + 1);
                for(int j = 0; j < list.size();j++){
                    XSSFCell xssfCell = xssfRow.createCell(j);
                    xssfCell.setCellValue(list.get(j).getValue());
                }
            }
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            xssfWorkbook.write(outputStream);
            outputStream.close();
            byte[] bytes = outputStream.toByteArray();
            WsImageUtils.byteToFile(bytes,"2","xls","C:\\Users\\10480\\Downloads\\test\\");

        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    @Data
    static class ObjValue{
        private String name;
        private String value;

        public ObjValue(){
        }

        public ObjValue(String name,String value){
            this.name = name;
            this.value = value;
        }

    }

}
