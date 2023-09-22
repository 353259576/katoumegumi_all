package cn.katoumegumi.java.excel;

import cn.katoumegumi.java.common.WsBeanUtils;
import org.apache.poi.ss.usermodel.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.util.*;

/**
 * excel读取
 * @author 星梦苍天
 */
public class ExcelReader {

//    private static final Logger log = LoggerFactory.getLogger(ExcelReader.class);
//
//    public static void main(String[] args) {
//        /*try {
//            List<List<List<ExcelPointLocation>>> list = read(new FileInputStream("C:\\360极速浏览器下载\\门店销售顾问业绩表 (1).xls"));
//            for(List<List<ExcelPointLocation>> sheet:list){
//                for (List<ExcelPointLocation> row:sheet){
//                    System.out.println(row.stream().map(ExcelPointLocation::getCell).filter(Objects::nonNull).map(cell -> {
//                        if(cell.getCellType().equals(CellType.NUMERIC)){
//                            return cell.getNumericCellValue()+"";
//                        }else {
//                            return cell.getStringCellValue();
//                        }
//
//                    }).collect(Collectors.joining(",")));
//                }
//            }
//        } catch (FileNotFoundException e) {
//            e.printStackTrace();
//        }*/
//    }
//
//
//    public static List<List<List<ExcelPointLocation>>> read(InputStream inputStream){
//        try {
//            Workbook workbook = WorkbookFactory.create(inputStream);
//            List<List<List<ExcelPointLocation>>> sheetList = new ArrayList<>(workbook.getNumberOfSheets());
//            Iterator<Sheet> iterator = workbook.sheetIterator();
//            while (iterator.hasNext()){
//                Sheet sheet = iterator.next();
//                int lastRow = sheet.getLastRowNum();
//                List<List<ExcelPointLocation>> rowList = new ArrayList<>(lastRow);
//                for(int rowIndex = 0; rowIndex < lastRow; rowIndex++){
//                    Row row = sheet.getRow(rowIndex);
//                    int lastColumnNum = row.getLastCellNum();
//                    List<ExcelPointLocation> cellList = new ArrayList<>(lastColumnNum);
//                    for(int cellIndex = 0; cellIndex < lastColumnNum; cellIndex++){
//                        Cell cell = row.getCell(cellIndex);
//                        ExcelPointLocation location = new ExcelPointLocation(cellIndex,rowIndex,1,cell,row,sheet,workbook);
//                        cellList.add(location);
//                    }
//                    rowList.add(cellList);
//                }
//                sheetList.add(rowList);
//            }
//            workbook.close();
//            return sheetList;
//        } catch (IOException e) {
//            log.error("文件读取错误");
//            e.printStackTrace();
//            return null;
//        }
//    }
//
//
//    public static <T> T getCellValue(Cell cell,Class<T> clazz){
//        if(cell == null){
//            return null;
//        }
//        CellType cellType = cell.getCellType();
//        switch (cellType){
//            case _NONE:
//            case BLANK:
//            case ERROR:
//                return null;
//            case BOOLEAN:return WsBeanUtils.objectToT(cell.getBooleanCellValue(),clazz);
//            case NUMERIC:return WsBeanUtils.objectToT(cell.getNumericCellValue(),clazz);
//            case STRING:return WsBeanUtils.objectToT(cell.getStringCellValue(),clazz);
//            case FORMULA:return WsBeanUtils.objectToT(cell.getCellFormula(),clazz);
//            default:throw new RuntimeException("不支持的类型");
//        }
//
//    }

}
