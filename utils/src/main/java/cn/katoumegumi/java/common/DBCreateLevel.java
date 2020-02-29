package cn.katoumegumi.java.common;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 自用数据库处理类
 */
public class DBCreateLevel {
    private static String host = "localhost:3306";//47.100.63.240:3306
    private static String dataBaseName = "db_ymm";
    private static String url = "jdbc:mysql://"+host+"/"+dataBaseName+"?useUnicode=true&characterEncoding=utf-8&useSSL=false&tinyInt1isBit=false&serverTimezone=GMT%2B8";
    //private static String url1 = "jdbc:mysql://116.62.144.206:3306/qupu1?useUnicode=true&characterEncoding=utf-8&useSSL=false";
    //private static String url = "jdbc:sqlserver://localhost:1433; DatabaseName=myblog";
    private static String driver = "com.mysql.cj.jdbc.Driver";
    private static String name = "root";
    private static String password = "199645";//zms@lsapp123456
    private static Connection conn = null;
    private static PreparedStatement preparedStatement = null;
    private static ResultSet resultSet = null;

    public static void prepare(String dbHost,String daName,String userName,String password,String tableName,String tableNick){
        host = dbHost;
        dataBaseName = daName;
        name = userName;
        DBCreateLevel.password = password;

        Long start = System.currentTimeMillis();
        String str1 = getMyResultMap(tableName);
        String str2 = getJavaBean(tableName);
        String str3 = getInsertSql(tableName);
        String str4 = getBaseSQL(tableName,tableNick);
        String str5 = getUpdate(tableName);
        String str6 = getSelect(tableName);

        System.out.println(str1);
        System.out.println(str2);
        System.out.println(str3);
        System.out.println(str4);
        System.out.println(str5);
        System.out.println(str6);
        System.out.println("执行完成一个用时："+(System.currentTimeMillis()-start)+"毫秒");
    }


    private static Connection getConn(){
        try {
            Class.forName(driver);
            try {
                conn = DriverManager.getConnection(url,name,password);
                return conn;
            } catch (SQLException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        } catch (ClassNotFoundException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return conn;
    }
    public static void getClose(){

        try {
            if(resultSet != null){
                resultSet.close();
            }
            if(preparedStatement != null){
                preparedStatement.close();
            }
            if(conn!=null){
                conn.close();
            }
        } catch (SQLException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

    }

    /**
     * 将数据库列名更改为java属性名称
     * @param str
     * @return
     */
    public static String DBStringChange(String str){
        str = str.toLowerCase();
        Boolean k = false;
        String strs[] = str.split("_");
        if(strs.length<=1){
            return str;
        }else {
            for(int i = 0; i < strs.length; i++){

                if(i==0){
                    if(strs[i]==null||strs[i].length()<=1){

                    }else {
                        if(k){
                            strs[i] = strs[i].substring(0,1).toUpperCase()+strs[i].substring(1);
                        }
                        k=true;

                    }
                }else {
                    if(strs[i]==null){

                    }else if(strs[i].length()<=1){
                        if(k){
                            strs[i] = strs[i].toUpperCase();
                        }
                        k=true;

                    }
                    else {
                        if(k){
                            strs[i] = strs[i].substring(0,1).toUpperCase()+strs[i].substring(1);
                        }
                        k=true;

                    }
                }
            }
            StringBuffer sb = new StringBuffer();
            for(int i = 0; i < strs.length; i++){
                if(i==0&&(strs[i]==null||strs[i].length()<=1)){
                    continue;
                }
                if(strs[i]!=null){
                    sb.append(strs[i]);
                }
            }
            return new String(sb);
        }
    }


    /**
     * 获取表的数据字段名以及注释
     * @param tableName
     * @return
     */
    public static Map<String,String> getColumnNameAndNotes(String  tableName){
        getConn();
        Map<String,String> map = new HashMap<>();
        try {
            String sql = "select COLUMN_NAME,column_comment from INFORMATION_SCHEMA.Columns where table_name=? and table_schema='"+dataBaseName+"'";
            //PreparedStatement
            preparedStatement = conn.prepareStatement(sql);
            preparedStatement.setString(1,tableName);
            //ResultSet
            resultSet =  preparedStatement.executeQuery();

            while (resultSet.next()){
                map.put(resultSet.getString(1),resultSet.getString(2));
            }
        }catch (SQLException e){
            e.printStackTrace();
        }finally {
            getClose();

        }
        return map;
    }

    /**
     * 获取表的数据字段名以及类型
     * @param tableName
     * @return
     */
    public static Map<String,String> getColumnNameAndType(String tableName){
        getConn();
        Map<String,String> map = new HashMap<>();
        try {
            String sql = "select * from `"+tableName+"` LIMIT 1";
            //PreparedStatement
            preparedStatement = conn.prepareStatement(sql);
            //ResultSet
            resultSet = preparedStatement.executeQuery();
            ResultSetMetaData resultSetMetaData = resultSet.getMetaData();
            int Type;
            String name;
            for (int i = 0; i < resultSetMetaData.getColumnCount();i++){
                name = resultSetMetaData.getColumnName(i+1);
                Type = resultSetMetaData.getColumnType(i+1);
                switch (Type){
                    case Types.INTEGER:map.put(name,"Integer");break;
                    case Types.TINYINT:map.put(name,"Integer");break;
                    case Types.BIGINT:map.put(name,"Long");break;
                    case Types.DOUBLE:map.put(name,"Double");break;
                    case Types.DECIMAL:map.put(name,"Double");break;
                    case Types.VARCHAR:map.put(name,"String");break;
                    case Types.CHAR:map.put(name,"String");break;
                    case Types.BIT:map.put(name,"Boolean");break;
                    case Types.DATE:map.put(name,"Date");break;
                    case Types.TIME:map.put(name,"Date");break;
                    case Types.TIMESTAMP:map.put(name,"Date");break;
                    case Types.SMALLINT:map.put(name,"Integer");break;
                    default:map.put(name,"Object");break;
                }
            }

        }catch (Exception e){
            e.printStackTrace();
        }finally {
            getClose();

        }
        return map;

    }

    /**
     * 表列名与Java属性名图
     * @param tableName
     * @return
     */
    public static Map<String,String> getColumnNameAndField(String tableName){
        getConn();
        Map<String,String> map = new HashMap<>();
        try {
            String sql = "select * from `"+tableName+"` LIMIT 1";
            preparedStatement = conn.prepareStatement(sql);
            resultSet = preparedStatement.executeQuery();
            ResultSetMetaData resultSetMetaData = resultSet.getMetaData();
            String typename;
            String column;
            for(int i = 0; i < resultSetMetaData.getColumnCount(); i++){
                column = resultSetMetaData.getColumnName(i+1);
                typename = DBStringChange(column);
                map.put(typename,column);
            }


        }catch (Exception e){
            e.printStackTrace();
        }finally {
            getClose();
        }
        return map;

    }

    /**
     * 生成java的属性
     * @return
     */
    public static List<String> getJavaTypeList(String tableName){
        getConn();
        List<String> list = new ArrayList<>();
        try{
            String sql = "select * from `"+tableName+"` LIMIT 1";
            preparedStatement = conn.prepareStatement(sql);
            resultSet = preparedStatement.executeQuery();
            ResultSetMetaData resultSetMetaData = resultSet.getMetaData();
            for(int i = 0; i < resultSetMetaData.getColumnCount(); i++){
                list.add(DBStringChange(resultSetMetaData.getColumnName(i+1)));
            }
        }catch (Exception e){
            e.printStackTrace();
        }finally {
            getClose();
        }
        return list;

    }

    /**
     * 拼写mybaits的ResultMap
     * @param tableName
     * @return
     */
    public static String getMyResultMap(String tableName){
        StringBuffer sb = new StringBuffer();
        List<String> list = getJavaTypeList(tableName);
        Map<String,String> map = getColumnNameAndField(tableName);
        String typename;
        String columnname;
        for(int i = 0; i < list.size(); i++){
            typename = list.get(i);
            columnname = map.get(typename);
            if(i==0){
                sb.append("<id property=\"");
                sb.append(typename);
                sb.append("\" column=\"");
                sb.append(columnname);
                sb.append("\"/>");
                sb.append("\r\n");
            }else {
                sb.append("<result property=\"");
                sb.append(typename);
                sb.append("\" column=\"");
                sb.append(columnname);
                sb.append("\"/>");
                sb.append("\r\n");
            }
        }
        return new String(sb);
    }

    /**
     * 生成javabean
     * @param tableName
     * @return
     */
    public static String getJavaBean(String tableName){
        StringBuffer sb = new StringBuffer();
        List<String> list = getJavaTypeList(tableName);
        Map<String,String> map1 = getColumnNameAndNotes(tableName);
        Map<String,String> map2 = getColumnNameAndField(tableName);
        Map<String,String> map3 = getColumnNameAndType(tableName);
        String type;
        String name;
        String notes;
        String column;
        for(int i = 0; i < list.size(); i++){
            name = list.get(i);
            column = map2.get(name);
            notes = map1.get(column);
            type = map3.get(column);
            sb.append("@Column(name = \""+column+"\")\r\n@TableName(value = \""+column+"\")\r\nprivate "+type+" "+name+"; "+"//"+notes+"\r\n\r\n");
        }
        return new String(sb);

    }

    /**
     * 获取添加SQL语句
     * @param tableName
     * @return
     */
    public static String getInsertSql(String tableName){
        StringBuffer sb = new StringBuffer();
        StringBuffer sb2 = new StringBuffer();
        List<String> list = getJavaTypeList(tableName);
        Map<String,String> map = getColumnNameAndField(tableName);
        String name;
        String column;
        for(int i = 0;i < list.size();i++) {
            name = list.get(i);
            column = map.get(name);
            sb.append("<if test=\""+name+"!=null\">\r\n "+column+",\r\n</if>\r\n");
            sb2.append("<if test=\""+name+"!=null\">\r\n #{"+name+"},\r\n</if>\r\n");

        }
        sb.append(sb2);
        return new String(sb);
    }

    /**
     * 获得SQL查询的基础语句
     * @param tableName
     * @param biaoji
     * @return
     */
    public static String getBaseSQL(String tableName,String biaoji){
        StringBuffer sb = new StringBuffer();
        List<String> list = getJavaTypeList(tableName);
        Map<String,String> map = getColumnNameAndField(tableName);
        String name;
        String column;
        for(int i = 0;i < list.size(); i++){
            name = list.get(i);
            column = map.get(name);
            sb.append(biaoji+"."+column+",\r\n");
        }
        return new String(sb);
    }

    /**
     * 获取修改SQL语句
     * @param tableName
     * @return
     */
    public static String getUpdate(String tableName){
        StringBuffer sb = new StringBuffer();
        List<String> list = getJavaTypeList(tableName);
        Map<String,String> map = getColumnNameAndField(tableName);
        String name;
        String column;
        for (int i = 0;i < list.size(); i++){
            name = list.get(i);
            column = map.get(name);
            sb.append("<if test=\""+name+"!=null\">\r\n "+column+"=#{"+name+"},\r\n</if>\r\n");
        }
        return new String(sb);

    }


    /**
     * 获取查询SQL语句
     * @param tableName
     * @return
     */
    public static String getSelect(String tableName){
        StringBuffer sb = new StringBuffer();
        List<String> list = getJavaTypeList(tableName);
        Map<String,String> map = getColumnNameAndField(tableName);
        String name;
        String column;
        for (int i = 0;i < list.size(); i++){
            name = list.get(i);
            column = map.get(name);
            sb.append("<if test=\""+name+"!=null\">\r\n "+"\t and "+column+"=#{"+name+"}\r\n</if>\r\n");
        }
        return new String(sb);

    }


    public static List<String> getAllTableName(){
        List<String> list = new ArrayList<>();
        getConn();
        try {
            String sql = "show tables";
            preparedStatement = conn.prepareStatement(sql);
            resultSet = preparedStatement.executeQuery();
            while (resultSet.next()){
                list.add(resultSet.getString(1));
            }
        }catch (Exception e){
            e.printStackTrace();
        }finally {
            getClose();
        }
        return  list;

    }

    /**
     * 将以上生成的数据保存在电脑
     * @param text
     * @param path
     */
    public static void makeFile(String text,String path){
        try {
            File file = new File(path);
            if(!file.exists()){
                file.createNewFile();
            }
            //InputStream in = new FileInputStream(text);
            OutputStream out = new FileOutputStream(file);
            //BufferedInputStream bin = new BufferedInputStream(in);
            BufferedOutputStream bout = new BufferedOutputStream(out);
            int k = 0;
            byte[] by = text.getBytes("UTF-8");
            bout.write(by);
            bout.close();
            out.close();
        }catch (Exception e){
            e.printStackTrace();
        }
    }


    public static List<String> selectTableName(){
        try {
            List<String> list = new ArrayList<>();
            String sql = "SELECT table_name FROM INFORMATION_SCHEMA.TABLES\n" +
                    "WHERE table_schema='"+dataBaseName+"'";
            conn = getConn();
            preparedStatement = conn.prepareStatement(sql);
            resultSet = preparedStatement.executeQuery();
            while (resultSet.next()){
                list.add(resultSet.getString(1));
            }
            return list;
        }catch (Exception e){
            e.printStackTrace();
            return null;
        }

    }




    public static void main(String args[]){

        /*List<String> list = selectTableName();
        for(String string:list){
            Map<String,String> map = getColumnNameAndType(string);
            if(map.get("user_id") != null){
                System.out.println(string);
            }
        }
        System.out.println(JSON.toJSONString(list));*/
        Long start = System.currentTimeMillis();
        String tablename = "ym_user_distributor_report";
		String str1 = getMyResultMap(tablename);
		String str2 = getJavaBean(tablename);
		String str3 = getInsertSql(tablename);
		String str4 = getBaseSQL(tablename,"g");
		String str5 = getUpdate(tablename);
		String str6 = getSelect(tablename);

		System.out.println(str1);
		System.out.println(str2);
		System.out.println(str3);
		System.out.println(str4);
		System.out.println(str5);
        System.out.println(str6);
		//String str = str1 + str2 + str3 + str4 + str5;


       /* List<String> list = getAllTableName();
        for(int i = 0; i < list.size(); i++){

            String str1 = getMyResultMap(list.get(i));
            String str2 = getJavaBean(list.get(i));
            String str3 = getInsertSql(list.get(i));
            String str4 = getBaseSQL(list.get(i),"m");
            String str5 = getUpdate(list.get(i));

            System.out.println(str1);
            System.out.println(str2);
            System.out.println(str3);
            System.out.println(str4);
            System.out.println(str5);
            String str = str1 + str2 + str3 + str4 + str5;
            makeFile(str,"C:\\Users\\ws\\项目\\文件\\"+list.get(i)+".txt");
        }
*/
        System.out.println("执行完成一个用时："+(System.currentTimeMillis()-start)+"毫秒");
    }
}
