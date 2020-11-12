package cn.katoumegumi.java.lx.utils.poi;

import cn.katoumegumi.java.common.WsBeanUtils;
import cn.katoumegumi.java.common.WsDateUtils;
import cn.katoumegumi.java.common.WsFieldUtils;
import cn.katoumegumi.java.common.WsFileUtils;
import cn.katoumegumi.java.lx.model.User;
import org.apache.poi.hssf.util.HSSFColor;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.model.StylesTable;
import org.apache.poi.xssf.model.ThemesTable;
import org.apache.poi.xssf.usermodel.XSSFCellStyle;

import java.io.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * poi工具类
 *
 * @author ws
 */
public class PoiUtils {

    public static void main(String[] args) {
        List<User> userList = new ArrayList<>();
        for(int i = 0; i < 1000; i++){
            User user = new User();
            user.setName("你好" + i);
            user.setPassword("世界" + i);
            user.setCreateDate(LocalDateTime.now());
            userList.add(user);
        }
        byte[] bytes = ExcelGenerator.create(userList)
                .setTitle("测试数据")
                .addColumnProperty(p -> p.setColumnName("姓名")
                        .setColumnWidth(100*256)
                        .setColumnSize(2)
                        .setExcelColumnStyle(cellStyle -> {
                            cellStyle.setFillForegroundColor(HSSFColor.HSSFColorPredefined.RED.getIndex());
                            cellStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
                        })
                        .setExcelCellFill(((location, user) -> {
                    CellStyle cellStyle = location.getCellStyle();
                    cellStyle.setFillForegroundColor(HSSFColor.HSSFColorPredefined.BLUE.getIndex());
                    cellStyle.setFillPattern(FillPatternType.THIN_BACKWARD_DIAG);
                    Cell cell = location.getCell();
                    cell.setCellStyle(cellStyle);
                    cell.setCellValue(user.getName());
                })).setExcelColumnEndFill(e->{
                    Cell cell = e.getCell();
                    cell.setCellValue("结束1");
                        }))
                .addColumnProperty(p -> p.setColumnName("密码").setColumnSize(3).setExcelCellFill(((location, user) -> {
                    location.getCell().setCellValue(user.getPassword());
                })).setExcelColumnEndFill(e->{
                    Cell cell = e.getCell();
                    cell.setCellValue("结束2");
                }))
                .addColumnProperty(p -> p.setColumnName("创建时间").setColumnSize(4).setExcelCellFill(((location, user) -> {
                    location.getCell().setCellValue(WsBeanUtils.objectToT(user.getCreateDate(),String.class));
                })).setExcelColumnEndFill(e->{
                    Cell cell = e.getCell();
                    cell.setCellValue("结束3");
                }))
                .build();
        File file = WsFileUtils.createFile("D:\\网页\\1.xls");
        try {
            FileOutputStream fileOutputStream = new FileOutputStream(file);
            try {
                fileOutputStream.write(bytes);
                fileOutputStream.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }


    }


}
