package cn.katoumegumi.java.common;

import com.alibaba.fastjson.JSON;

import java.lang.reflect.Field;
import java.util.*;

public class WsListUtils {

    public static void main(String[] args) {

        /*ArrayList<String> l1 = new ArrayList<>();
        List<String> l2 = new ArrayList<>();
        l1.add("dfsd");
        l2.add("dfs");
        List<String> list = mergeList(l1,l2);
        System.out.println(JSON.toJSONString(list));*/
        String str = "1,2,3,4,5,6,7,8,9";
        String str2 = "123456789";
        List list2 = Arrays.asList(str2.split(""));
        List list = Arrays.asList(str.split(","));
        System.out.println(JSON.toJSONString(list));
        System.out.println(JSON.toJSONString(list2));
        for(int i = 0; i < list.size(); i++){
            if(list.get(i).equals(list2.get(i))){
                System.out.println(true);
            }else {
                System.out.println(false);
            }
        }
    }


    public static boolean isEmpty(Collection collection){
        return (collection == null || collection.size() == 0);
    }

    public static boolean isEmpty(Object[] os){
        return os == null || os.length == 0;
    }

    public static boolean isEmpty(Map map){
        return (map == null || map.isEmpty());
    }


    public static boolean isNotEmpty(Collection collection){
        return !isEmpty(collection);
    }


    public static boolean isNotEmpty(Map map){
        return !isEmpty(map);
    }

    public static <T> List<T> mergeList(List<T> l1,List<T> l2){
        if(isEmpty(l1)){
            return l2;
        }
        if(isEmpty(l2)){
            return l1;
        }
        List<T> list = new ArrayList<>(l1.size() + l2.size());
        list.addAll(l1);
        list.addAll(l2);
        return list;
    }







    public static <T,E> List<T>ListToList(List<E> list,Class<T> tClass){
        try {
            List<T> list1 = new ArrayList<>(list.size());
            for(E o1:list){
                Object o2 = (T)tClass.getConstructor(tClass).newInstance();
            }
        }catch (Exception e){
            e.printStackTrace();
        }
        return null;

    }

    public static <T> List<T> arrayToList(T[] array){
        List<T> list = new ArrayList<>();
        for(int i = 0; i < array.length; i++){
            list.add(array[i]);
        }
        return list;
    }


    public static <T,E> T copyObject(E object,Class<T> tClass){
        Class c1 = object.getClass();
        Field fields1[] = c1.getDeclaredFields();
        Field fields2[] = c1.getFields();
        HashSet<Field> fieldHashSet = new HashSet<>();
        for(int i = 0; i < fields1.length; i++){
            fieldHashSet.add(fields1[i]);
        }
        for(int i = 0; i < fields2.length; i++){
            fieldHashSet.add(fields2[i]);
        }
        return null;
    }

    public static String objectGetMethodName(Field field){
        String fieldName = field.getName();
        return "get"+fieldName.substring(0,1).toUpperCase()+fieldName.substring(1,fieldName.length());
    }

    public static String objectSetMethodName(Field field){
        String fieldName = field.getName();
        return "set"+fieldName.substring(0,1).toUpperCase()+fieldName.substring(1,fieldName.length());
    }
}
