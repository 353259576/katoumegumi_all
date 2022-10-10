package cn.katoumegumi.java.excel;

import org.apache.poi.ss.usermodel.CellStyle;

/**
 * excel表格表头风格
 * @author ws
 */
public interface ExcelColumnStyle {

    /**
     * 设置excel表格表头
     * @param cellStyle 表格风格
     */
    void setStyle(CellStyle cellStyle);


}
