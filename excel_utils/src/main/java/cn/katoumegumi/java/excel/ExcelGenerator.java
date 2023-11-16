package cn.katoumegumi.java.excel;

import cn.katoumegumi.java.common.*;
import org.apache.poi.ss.SpreadsheetVersion;
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
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;

/**
 * excel表格生成器
 *
 * @author ws
 */
public class ExcelGenerator<T> {

    private final Workbook workbook;

    /**
     * 数据
     */
    private List<T> valueList;

    private static final Field SXSSFSHEET_SH_FIELD = WsReflectUtils.getFieldByName(SXSSFSheet.class,"_sh");

    private static final Field XSSFSHEET_WORKSHEET_FIELD = WsReflectUtils.getFieldByName(XSSFSheet.class,"worksheet");

    private ExcelSheetGenerator<T> excelSheetGenerator;



    public static <T> ExcelGenerator<T> create(List<T> tList) {
        ExcelGenerator<T> excelGenerator = new ExcelGenerator<>(new SXSSFWorkbook());
        excelGenerator.setValueList(tList);
        return excelGenerator;
    }

    private ExcelGenerator(Workbook workbook) {
        this.workbook = workbook;
        excelSheetGenerator = new ExcelSheetGenerator<>(this,workbook.createSheet());
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
        return excelSheetGenerator.getTitle();
    }

    public ExcelGenerator<T> setTitle(String title) {
        this.excelSheetGenerator.setTitle(title);
        return this;
    }

    public ExcelGenerator<T> setValueList(List<T> valueList) {
        this.valueList = valueList;
        return this;
    }

    public ExcelGenerator<T> addColumnProperty(Consumer<ExcelTableColumnProperty<T>> consumer) {
        excelSheetGenerator.addColumnProperty(consumer);
        return this;
    }

    public ExcelGenerator<T> setExcelTableTitleCellFill(ExcelTableTitleCellFill excelTableTitleCellFill) {
        excelSheetGenerator.setExcelTableTitleCellFill(excelTableTitleCellFill);
        return this;
    }

    public byte[] build() {
        try {
            excelSheetGenerator.createTitle()
                    .createTableHead()
                    .createTableBody(valueList)
                    .createTableFoot();
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
        } else {
            region.validate(SpreadsheetVersion.EXCEL2007);

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

            CTMergeCells ctMergeCells = worksheet.isSetMergeCells() ? worksheet.getMergeCells() : worksheet.addNewMergeCells();
            CTMergeCell ctMergeCell = ctMergeCells.addNewMergeCell();
            ctMergeCell.setRef(region.formatAsString());
            long count = ctMergeCells.getCount();
            if (count == 0L) {
                count = ctMergeCells.sizeOfMergeCellArray();
            } else {
                ++count;
            }

            ctMergeCells.setCount(count);
        }
    }


    public void checkThread(Thread thread) throws InterruptedException {
        if(thread.isInterrupted()){
            throw new InterruptedException("当前线程已被标记为中断，excel生成终止");
        }
    }

    public Workbook getWorkbook() {
        return workbook;
    }
}
