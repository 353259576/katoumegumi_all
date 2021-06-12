package cn.katoumegumi.java.common;

import java.lang.reflect.Array;
import java.util.*;
import java.util.function.Function;

/**
 * @author ws
 */
public class WsListUtils {


    /**
     * 判断是不是为空
     *
     * @param collection 集合
     * @return 布尔
     */
    public static boolean isEmpty(Collection collection) {
        return (collection == null || collection.size() == 0);
    }

    /**
     * 判断是否为空
     *
     * @param os 数组
     * @return 布尔
     */
    public static boolean isEmpty(Object[] os) {
        return os == null || os.length == 0;
    }

    /**
     * 判断是否为空
     *
     * @param map map
     * @return 布尔
     */
    public static boolean isEmpty(Map map) {
        return (map == null || map.isEmpty());
    }

    /**
     * 判断数组是不是为空
     * @param obj
     * @return
     */
    public static boolean isEmpty(Object obj) {
        if(obj == null){
            return true;
        }
        if(obj instanceof Collection){
            return ((Collection<?>) obj).size() == 0;
        }else if(obj instanceof Map){
            return ((Map<?,?>)obj).size() == 0;
        }else {
            if(obj.getClass().isArray()){
                if(BaseTypeCommon.verifyArray(obj.getClass())){
                    return Array.getLength(obj) == 0;
                }else {
                    return ((Object[])obj).length == 0;
                }
            }else {
                return false;
            }
        }
    }

    /**
     * 判断是否不为空
     *
     * @param collection 集合
     * @return 布尔
     */
    public static boolean isNotEmpty(Collection collection) {
        return !isEmpty(collection);
    }

    /**
     * 判断是否不为空
     *
     * @param map 集合
     * @return 布尔
     */
    public static boolean isNotEmpty(Map map) {
        return !isEmpty(map);
    }

    public static boolean isNotEmpty(Object object) {
        return !isEmpty(object);
    }

    /**
     * 合并数组
     *
     * @param l1  参数
     * @param l2  参数
     * @param <T> 泛型
     * @return 合并后数组
     */
    public static <T> List<T> mergeList(List<T> l1, List<T> l2) {
        if (isEmpty(l1) && isEmpty(l2)) {
            return null;
        }
        if (isEmpty(l1)) {
            return l2;
        }
        if (isEmpty(l2)) {
            return l1;
        }
        List<T> list = new ArrayList<>(l1.size() + l2.size());
        list.addAll(l1);
        list.addAll(l2);
        return list;
    }


    /**
     * 原生数组转化为list
     *
     * @param array 数组
     * @param <T>   泛型
     * @return list
     */
    public static <T> List<T> arrayToList(T[] array) {
        if (array == null || array.length == 0) {
            return null;
        }
        List<T> list = new ArrayList<>();
        for (int i = 0; i < array.length; i++) {
            list.add(array[i]);
        }
        return list;
    }

    /**
     * list转为map
     *
     * @param tList    数组
     * @param function 方法
     * @param <T>      对象
     * @param <I>      id
     * @return 图
     */
    public static <T, I> Map<I, List<T>> listToMapList(List<T> tList, Function<T, I> function) {
        Map<I, List<T>> map = new HashMap<>();
        if (isEmpty(tList)) {
            return new HashMap<>();
        }
        for (T t : tList) {
            I i = function.apply(t);
            List<T> list = map.computeIfAbsent(i, id -> {
                return new ArrayList<>();
            });
            list.add(t);
        }
        return map;
    }

    /**
     * list转为map
     *
     * @param tList    数组
     * @param function 方法
     * @param <T>      对象
     * @param <I>      id
     * @return 图
     */
    public static <T, I> Map<I, Set<T>> listToMapSet(List<T> tList, Function<T, I> function) {
        Map<I, Set<T>> map = new HashMap<>();
        if (isEmpty(tList)) {
            return new HashMap<>();
        }
        for (T t : tList) {
            I i = function.apply(t);
            Set<T> set = map.computeIfAbsent(i, id -> {
                return new HashSet<>();
            });
            set.add(t);
        }
        return map;
    }

    /**
     * list转为map
     *
     * @param tList    list
     * @param function 方法
     * @param <T>      泛型
     * @param <I>      泛型
     * @return map
     */
    public static <T, I> Map<I, T> listToMap(List<T> tList, Function<T, I> function) {
        Map<I, T> map = new HashMap<>();
        if (isEmpty(tList)) {
            return new HashMap<>();
        }
        for (T t : tList) {
            I i = function.apply(t);
            map.put(i, t);
        }
        return map;
    }

    /**
     * 获取list的子list
     *
     * @param tList    父list
     * @param function 方法
     * @param <T>      父
     * @param <I>      子
     * @return 子list
     */
    public static <T, I> List<I> listToList(List<T> tList, Function<T, I> function) {
        List<I> iList = new ArrayList<>(tList.size());
        for (T t : tList) {
            iList.add(function.apply(t));
        }
        return iList;
    }

    /**
     * 获取list的子Set
     *
     * @param tList    父list
     * @param function 方法
     * @param <T>      父
     * @param <I>      子
     * @return 子Set
     */
    public static <T, I> Set<I> listToSet(List<T> tList, Function<T, I> function) {
        Set<I> iSet = new HashSet<>(tList.size());
        for (T t : tList) {
            iSet.add(function.apply(t));
        }
        return iSet;
    }


    /**
     * 原生数组转化为list
     *
     * @param tList 数组
     * @param <T>   泛型
     * @return 数组
     */
    public static <T> T[] listToArray(List<T> tList) {
        if (isEmpty(tList)) {
            return null;
        }
        T[] ts = (T[]) new Object[tList.size()];
        for (int i = 0; i < tList.size(); i++) {
            ts[i] = tList.get(i);
        }
        return ts;
    }

    /**
     * 合并两个数组成一个新的数组
     *
     * @param list1
     * @param list2
     * @param <T>
     * @return
     */
    public static <T> T[] mergeList(T[] list1, T[] list2) {
        if (list1 == null || list2 == null || list1.length == 0 || list2.length == 0) {
            if (!(list1 == null || list1.length == 0)) {
                return list1;
            }
            if (!(list2 == null || list2.length == 0)) {
                return list2;
            }
        }
        T[] list = (T[]) new Object[list1.length + list2.length];
        int k = 0;
        for (int i = 0; i < list1.length; i++) {
            list[k] = list1[i];
            k++;
        }
        for (int i = 0; i < list2.length; i++) {
            list[k] = list2[i];
            k++;
        }
        return list;
    }


}
