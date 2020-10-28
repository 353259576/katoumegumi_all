package cn.katoumegumi.java.lx.utils.poi;

import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Sheet;

/**
 * excel表格表头风格
 * @author ws
 */
public interface ExcelColumnStyle {

    /**
     * 设置excel表格表头
     * @param cellStyle 表格风格
     */
    public void setStyle(CellStyle cellStyle);


}
