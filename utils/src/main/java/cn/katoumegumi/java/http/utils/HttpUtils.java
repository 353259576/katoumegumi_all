package cn.katoumegumi.java.http.utils;

import cn.katoumegumi.java.common.WsDateUtils;
import cn.katoumegumi.java.common.WsListUtils;
import cn.katoumegumi.java.common.WsStringUtils;
import cn.katoumegumi.java.http.model.BaseEntity;
import cn.katoumegumi.java.http.model.FileEntity;
import cn.katoumegumi.java.http.model.ValueEntity;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;

/**
 * @author ws
 */
public class HttpUtils {

    private static final char[] MULTIPART_CHARS = "-_1234567890abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ".toCharArray();


    /**
     * formData格式参数开头
     */
    private static final String FORM_DATA_PART_START = "Content-Disposition: form-data;";


    private static final String FORM_DATA_NAME_STR = "name=";

    private static final String FORM_DATA_FILENAME_STR = "filename=";


    private static final String FORM_DATA_CONTENT_TYPE_STR = "Content-Type: ";


    /**
     * formData boundary开头部分
     */
    private static final String BOUNDARY_START = "----WebKitFormBoundary";

    private static final String BOUNDARY_TAG = "--";

    private static final int BOUNDARY_LENGTH = 16;

    /**
     * 换行
     */
    private static final String LF_CHARACTER = "\r\n";

    /**
     * 空格
     */
    private static final String SPACE_CHARACTER = " ";

    /**
     * 双引号
     */
    private static final String DOUBLE_QUOTATION_CHARACTER = "\"";

    private static final String SEMICOLON_CHARACTER = ";";



    public static void main(String[] args) {
        String boundary = getFormDataBoundary();
        String charset = "UTF-8";
        List<BaseEntity> entityList = new ArrayList<>();
        entityList.add(new ValueEntity().setValue("你好世界").setName("name"));
        entityList.add(new ValueEntity().setValue("你好世界1321984865456").setName("password"));
        entityList.add(new FileEntity().setValue(new File("D:\\10480\\Documents\\练习.postman_collection.json")).setName("file"));
        String str = new String(toBaseForm(entityList,charset));
        System.out.println(str);
    }

    /**
     * 获取formData分隔符
     * @return
     */
    public static String getFormDataBoundary(){
        Random random = new Random();
        StringBuilder stringBuilder = new StringBuilder(BOUNDARY_START);
        int j;
        for (int i = 0; i < BOUNDARY_LENGTH; i++) {
            j = random.nextInt(MULTIPART_CHARS.length - 2) + 2;
            stringBuilder.append(MULTIPART_CHARS[j]);
        }
        return stringBuilder.toString();
    }


    /**
     * 转换为formData格式
     * @return
     */
    public static byte[] toFormData(List<BaseEntity> entityList,String boundary,String charset){
        if(WsListUtils.isEmpty(entityList)){
            return null;
        }
        try {
            FileEntity fileEntity;
            ValueEntity valueEntity;
            byte[] boundaryByteArray = boundary.getBytes(charset);
            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
            for (BaseEntity entity:entityList){
                byteArrayOutputStream.write(BOUNDARY_TAG.getBytes(charset));
                byteArrayOutputStream.write(boundaryByteArray);
                byteArrayOutputStream.write(LF_CHARACTER.getBytes(charset));
                byteArrayOutputStream.write(FORM_DATA_PART_START.getBytes(charset));
                byteArrayOutputStream.write(SPACE_CHARACTER.getBytes(charset));
                byteArrayOutputStream.write(FORM_DATA_NAME_STR.getBytes(charset));
                byteArrayOutputStream.write(DOUBLE_QUOTATION_CHARACTER.getBytes(charset));
                byteArrayOutputStream.write(entity.getName().getBytes(charset));
                byteArrayOutputStream.write(DOUBLE_QUOTATION_CHARACTER.getBytes(charset));
                if(entity instanceof ValueEntity){
                    valueEntity = (ValueEntity) entity;
                    byteArrayOutputStream.write(LF_CHARACTER.getBytes(charset));
                    byteArrayOutputStream.write(LF_CHARACTER.getBytes(charset));
                    byteArrayOutputStream.write(valueEntity.getStringValue().getBytes(charset));
                }else {
                    fileEntity = (FileEntity) entity;
                    if(WsStringUtils.isNotBlank(fileEntity.getFileName())){
                        byteArrayOutputStream.write(SEMICOLON_CHARACTER.getBytes(charset));
                        byteArrayOutputStream.write(SPACE_CHARACTER.getBytes(charset));
                        byteArrayOutputStream.write(FORM_DATA_FILENAME_STR.getBytes(charset));
                        byteArrayOutputStream.write(DOUBLE_QUOTATION_CHARACTER.getBytes(charset));
                        byteArrayOutputStream.write(fileEntity.getFileName().getBytes(charset));
                        byteArrayOutputStream.write(DOUBLE_QUOTATION_CHARACTER.getBytes(charset));

                    }
                    byteArrayOutputStream.write(LF_CHARACTER.getBytes(charset));
                    byteArrayOutputStream.write(FORM_DATA_CONTENT_TYPE_STR.getBytes(charset));
                    byteArrayOutputStream.write(" application/octet-stream".getBytes(charset));
                    byteArrayOutputStream.write(LF_CHARACTER.getBytes(charset));
                    byteArrayOutputStream.write(LF_CHARACTER.getBytes(charset));
                    byteArrayOutputStream.write(fileEntity.getValue());
                }
                byteArrayOutputStream.write(LF_CHARACTER.getBytes(charset));
            }
            byteArrayOutputStream.write(BOUNDARY_TAG.getBytes(charset));
            byteArrayOutputStream.write(boundaryByteArray);
            byteArrayOutputStream.write(BOUNDARY_TAG.getBytes(charset));
            byte[] bytes = byteArrayOutputStream.toByteArray();
            byteArrayOutputStream.close();
            return bytes;
        }catch (IOException e){
            e.printStackTrace();
        }

        return null;
    }

    /**
     * 转换为x-www-form-urlencoded
     * @param entityList
     * @return
     */
    public static byte[] toBaseForm(List<BaseEntity> entityList,String charset){
        if(WsListUtils.isEmpty(entityList)){
            return null;
        }
        ValueEntity valueEntity = null;
        List<String> valueList = new ArrayList<>(entityList.size());
        for(BaseEntity entity:entityList){
            if(entity instanceof ValueEntity){
                valueEntity = (ValueEntity) entity;
                valueList.add(valueEntity.getName() +"=" + valueEntity.getStringValue());
            }
        }
        if(WsListUtils.isEmpty(valueList)){
            return null;
        }
        try {
            return String.join("&", valueList).getBytes(charset);
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        return null;

    }





}
