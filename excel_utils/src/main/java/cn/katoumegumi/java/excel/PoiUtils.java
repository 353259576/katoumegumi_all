package cn.katoumegumi.java.excel;


import org.apache.poi.hssf.util.HSSFColor;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.FillPatternType;
import org.apache.poi.ss.usermodel.HorizontalAlignment;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * poi工具类
 *
 * @author ws
 */
public class PoiUtils {

    public static void main(String[] args) throws IOException {
        List<String[]> list = new ArrayList<>();
        for (int i = 0; i < 10; i++){
            list.add(
                    new String[]{
                            "测试姓名" + i,
                             (i & 1) == 0? "男":"女",
                            "测试密码：" + i
                    }
            );
        }

        ExcelTableHeadCellFill excelTableHeadCellFill = location -> {
            CellStyle cellStyle = location.getCellStyle();
            cellStyle.setAlignment(HorizontalAlignment.CENTER);
            cellStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
            cellStyle.setFillForegroundColor(HSSFColor.HSSFColorPredefined.GREEN.getIndex());
        };
        ExcelGenerator<String[]> excelGenerator = ExcelGenerator.create(list)
                .setTitle("测试")
                .addColumnProperty(c->c.setColumnName("姓名")
                        .setColumnWidthCellSize(2)
                        .setColumnHeightCellSize(2)
                        //.setColumnWidth(100)
                        .setExcelTableBodyCellFill((location,strings)-> location.getCell().setCellValue(strings[0]))
                        .setExcelTableHeadCellFill(excelTableHeadCellFill)
                        .setExcelTableFootCellFill(location -> location.getCell().setCellValue("姓名"))
                ).addColumnProperty(c->c.setColumnName("性别")
                        .setColumnWidthCellSize(2)
                        .setColumnHeightCellSize(2)
                        //.setColumnWidth(100)
                        .setExcelTableBodyCellFill((location,strings)->{
                            location.getCell().setCellValue(strings[1]);
                        })
                        .setExcelTableHeadCellFill(excelTableHeadCellFill)
                        .setExcelTableFootCellFill(location -> location.getCell().setCellValue("性别"))
                ).addColumnProperty(c->c.setColumnName("密码")
                        .setColumnWidthCellSize(2)
                        .setColumnHeightCellSize(2)
                        //.setColumnWidth(100)
                        .setExcelTableBodyCellFill((location,strings)->{
                            location.getCell().setCellValue(strings[2]);
                        })
                        .setExcelTableHeadCellFill(excelTableHeadCellFill)
                        .setExcelTableFootCellFill(location -> location.getCell().setCellValue("密码"))
                );

        byte[] bytes = excelGenerator.build();
        File file = new File("D:\\网页\\test\\1.xlsx");
        if (!file.exists()){
            file.createNewFile();
        }
        FileOutputStream fileOutputStream = new FileOutputStream(file);
        fileOutputStream.write(bytes);
        fileOutputStream.close();
    }


}
