package cn.katoumegumi.java.common;

import java.lang.reflect.Array;
import java.util.*;
import java.util.function.Function;

/**
 * @author ws
 */
public class WsCollectionUtils {

    private static final Object[] EMPTY_OBJECT_ARRAY = new Object[0];

    /**
     * 判断是不是为空
     *
     * @param collection 集合
     * @return 布尔
     */
    public static <T> boolean isEmpty(Collection<T> collection) {
        return (collection == null || collection.isEmpty());
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
    public static <K,V> boolean isEmpty(Map<K,V> map) {
        return (map == null || map.isEmpty());
    }

    /**
     * 判断数组是不是为空
     *
     * @param obj
     * @return
     */
    public static boolean isEmpty(Object obj) {
        if (obj == null) {
            return true;
        }
        if (obj instanceof Collection) {
            return ((Collection<?>) obj).isEmpty();
        } else if (obj instanceof Map) {
            return ((Map<?, ?>) obj).isEmpty();
        } else {
            if (obj.getClass().isArray()) {
                if (BaseTypeCommon.verifyArray(obj.getClass())) {
                    return Array.getLength(obj) == 0;
                } else {
                    return ((Object[]) obj).length == 0;
                }
            } else {
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
    public static <T> boolean isNotEmpty(Collection<T> collection) {
        return !isEmpty(collection);
    }

    /**
     * 判断是否不为空
     *
     * @param map 集合
     * @return 布尔
     */
    public static <K,V> boolean isNotEmpty(Map<K,V> map) {
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
            return Collections.emptyList();
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
        Collections.addAll(list, array);
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
            List<T> list = map.computeIfAbsent(i, id -> new ArrayList<>());
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
            Set<T> set = map.computeIfAbsent(i, id -> new HashSet<>());
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

    public static <T, I> I[] listToArray(List<T> tList, Function<T, I> function) {
        int length = tList.size();
        Object[] objects = new Object[length];

        for (int i = 0; i < length; ++i) {
            objects[i] = function.apply(tList.get(i));
        }


        return (I[]) objects;
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
        return (T[]) tList.toArray();
    }

    /**
     * 合并两个数组成一个新的数组
     *
     * @param array1
     * @param array2
     * @param <T>
     * @return
     */
    public static <T> T[] mergeList(T[] array1, T[] array2) {
        int l1 = array1 == null?0:array1.length;
        int l2 = array2 == null?0:array2.length;
        int l = l1 + l2;
        if (l == 0){
            return (T[]) EMPTY_OBJECT_ARRAY;
        }
        if (l1 == 0){
            return array2;
        }
        if (l2 == 0){
            return array1;
        }
        T[] array = (T[]) new Object[l];
        int k = 0;
        for (T t : array1) {
            array[k] = t;
            k++;
        }
        for (T t : array2) {
            array[k] = t;
            k++;
        }
        return array;
    }

    /**
     * 移动数组元素（如果超过数组长度移动到数组头部）
     *
     * @param list 原始数组
     * @param move 移动格数
     * @param <T>
     * @return
     */
    public static <T> List<T> moveListElement(List<T> list, int move) {
        int arrayLength = list.size();
        if (arrayLength < 2) {
            return list;
        }
        move = move % arrayLength;
        if (move == 0) {
            return list;
        }

        boolean right = move > 0;

        if ((right ? move : Math.abs(move)) > arrayLength / 2) {
            right = !right;
            move = move > 0 ? arrayLength - move : arrayLength + move;
        } else {
            move = Math.abs(move);
        }
        Object[] objects = new Object[move];
        if (right) {
            for (int i = 0; i < move; i++) {
                objects[move - 1 - i] = list.get(arrayLength - 1 - i);
            }
            for (int i = arrayLength - 1 - move; i >= 0; i--) {
                list.set(i + move, list.get(i));
            }
            for (int i = 0; i < move; i++) {
                list.set(i, (T) objects[i]);
            }
        } else {
            for (int i = 0; i < move; i++) {
                objects[i] = list.get(i);
            }
            for (int i = move; i < arrayLength; i++) {
                list.set(i - move, list.get(i));
            }
            for (int i = 0; i < move; i++) {
                list.set(arrayLength - move + i, (T) objects[i]);
            }
        }
        return list;
    }

    /**
     * 获取一个空的对象数组
     * @return
     */
    public static Object[] getEmptyObjectArray(){
        return EMPTY_OBJECT_ARRAY;
    }

}
