package com.ws.java.common;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.*;

@SuppressWarnings("unchecked")
public class WsBeanUtis {

    public static void main(String[] args) {
        LocalDateTime localDateTime = LocalDateTime.now();
        System.out.println(objectToT(System.currentTimeMillis(),LocalDateTime.class));
        /*int rgb = -1;
        int b = (rgb << 24);
        b = b >>> 24;
        System.out.println(b);*/
    }


    public static  <T> T copyBean(T object){
        try {
            byte[] bytes = serializeObject(object);
            return (T)deSerializeObject(bytes,object.getClass());
        }catch (Exception e){
            e.printStackTrace();
            return null;
        }

    }

    public static <T> T copyField(Object object,T newObject){
        try {
            Class clazz = newObject.getClass();
            Field[] oldFields = WsFieldUtils.getFieldAll(object.getClass());
            if(oldFields == null||oldFields.length == 0){
                return (T)newObject;
            }
            Field field;
            Object value;
            for(int i = 0; i < oldFields.length; i++){
                field = WsFieldUtils.getFieldForClass(oldFields[i].getName(),clazz);
                if(field != null){
                    value = WsFieldUtils.getFieldValueForName(oldFields[i],object);
                    if(value == null){
                        continue;
                    }
                    if(field.getType().getTypeName().equals(oldFields[i].getType().getTypeName())){
                        if(object != null){
                            WsFieldUtils.setFieldValueForName(field,newObject,value);
                        }
                    }else {
                        value = WsBeanUtis.mapToObject(value,field.getType());
                        if(value != null){
                            WsFieldUtils.setFieldValueForName(field,newObject,value);
                        }
                    }
                }
            }

            return (T)newObject;
        }catch (Exception e){
            e.printStackTrace();
            return null;
        }

    }


    public static byte[] serializeObject(Object object){
        try {
            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
            ObjectOutputStream objectOutputStream = new ObjectOutputStream(byteArrayOutputStream);
            objectOutputStream.writeObject(object);
            objectOutputStream.flush();
            objectOutputStream.close();
            byte bytes[] = byteArrayOutputStream.toByteArray();
            byteArrayOutputStream.flush();
            byteArrayOutputStream.close();
            return bytes;
        }catch (Exception e){
            e.printStackTrace();
            return null;
        }

    }


    public static  <T> T deSerializeObject(byte bytes[],Class<T> tClass){
        try {
            ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(bytes);
            ObjectInputStream objectInputStream = new ObjectInputStream(byteArrayInputStream);
            Object object = objectInputStream.readObject();
            objectInputStream.close();
            byteArrayInputStream.close();
            return (T) object;
        }catch (Exception e){
            e.printStackTrace();
            return null;
        }
    }




    public static<T> T mapToObject(Object object,Class<T> clazz){
        if(!(object instanceof Map)){
            Object o = objectToT(object,clazz);
            if(object == null){
                return (T)object;
            }else {
                return (T)o;
            }
        }
        Map map = (Map)object;
        Set<Map.Entry> entrySet = map.entrySet();
        Iterator<Map.Entry> iterator = entrySet.iterator();
        Map.Entry entry;
        Method methods[] = clazz.getMethods();
        T dx = null;
        try {
            dx = clazz.getDeclaredConstructor().newInstance();
        }catch (Exception e){
            e.printStackTrace();
            return null;
        }
        while (iterator.hasNext()){
            entry = iterator.next();
            Object key = entry.getKey();
            Object value = entry.getValue();
            if(value==null){
                continue;
            }
            String sKey = (String) key;
            String methodName = "set"+sKey.substring(0,1).toUpperCase()+sKey.substring(1,sKey.length());
            ws:for(Method method:methods){
                if(method.getName().equals(methodName)){
                    Class c[] = method.getParameterTypes();
                    if(c.length==1){
                        if(c[0]==int.class||c[0]==Integer.class){
                            try {
                                method.invoke(dx,Integer.parseInt(String.valueOf(value)));
                                break;
                            }catch (Exception e){
                                e.printStackTrace();
                            }

                        }
                        else if(c[0]==byte.class||c[0]==Byte.class){
                            try {
                                method.invoke(dx,Byte.parseByte(String.valueOf(value)));
                                break;
                            }catch (Exception e){
                                e.printStackTrace();
                            }
                        }
                        else if(c[0]==char.class||c[0]==Character.class){
                            try {
                                method.invoke(dx,value);
                                break;
                            }catch (Exception e){
                                e.printStackTrace();
                            }
                        }
                        else if(c[0]==boolean.class||c[0]==Boolean.class){
                            try {
                                method.invoke(dx,Boolean.parseBoolean(String.valueOf(value)));
                                break;
                            }catch (Exception e){
                                e.printStackTrace();
                            }
                        }
                        else if(c[0]==short.class||c[0]==Short.class){
                            try {
                                method.invoke(dx,Short.parseShort(String.valueOf(value)));
                                break;
                            }catch (Exception e){
                                e.printStackTrace();
                            }
                        }
                        else if(c[0]==long.class||c[0]==Long.class){
                            try {
                                method.invoke(dx,Long.parseLong(String.valueOf(value)));
                                break;
                            }catch (Exception e){
                                e.printStackTrace();
                            }
                        }
                        else if(c[0]==float.class||c[0]==Float.class){
                            try {
                                method.invoke(dx,Float.parseFloat(String.valueOf(value)));
                                break;
                            }catch (Exception e){
                                e.printStackTrace();
                            }
                        }
                        else if(c[0]==double.class||c[0]==Double.class){
                            try {
                                method.invoke(dx,Double.parseDouble(String.valueOf(value)));
                                break;
                            }catch (Exception e){
                                e.printStackTrace();
                            }
                        }
                        else if(c[0]==String.class){
                            try {
                                method.invoke(dx,String.valueOf(value));
                                break;
                            }catch (Exception e){
                                e.printStackTrace();
                            }
                        }else if(c[0]== BigInteger.class){
                            try {
                                method.invoke(dx,BigInteger.valueOf(Long.parseLong(String.valueOf(value))));
                                break;
                            }catch (Exception e){
                                e.printStackTrace();
                            }
                        }else if(c[0]== BigDecimal.class){
                            try {
                                method.invoke(dx,new BigDecimal(String.valueOf(value)));
                                break;
                            }catch (Exception e){
                                e.printStackTrace();
                            }
                        }else if(c[0]==Date.class){
                            try {
                                if(value instanceof Long){
                                    method.invoke(dx,new Date((Long)value));
                                    break;
                                }else if(value.getClass()==long.class){
                                    method.invoke(dx,new Date((long)value));
                                    break;
                                }else {
                                    if(WsStringUtils.isNumber((String)value)){
                                        method.invoke(dx,new Date(Long.parseLong((String) value)));
                                        break;
                                    }else {
                                        //SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ");
                                        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                                        simpleDateFormat.setLenient(false);
                                        method.invoke(dx,simpleDateFormat.parse(WsStringUtils.dateToDate((String) value)));
                                        break;
                                    }

                                }

                            }catch (Exception e){
                                e.printStackTrace();
                            }
                        }
                        //对于数组的转换
                        else if(c[0]==int[].class||c[0]==Integer[].class){
                            try {

                                if(c[0]==int[].class){
                                    int[] objects = (int[])value;
                                    int[] ints = new int[objects.length];
                                    for(int i = 0; i < ints.length; i++){
                                        ints[i] = Integer.parseInt(String.valueOf(objects[i]));
                                    }
                                    method.invoke(dx,(Object)ints);
                                }
                                if(c[0]==Integer[].class){
                                    Integer[] objects = (Integer[])value;
                                    Integer[] ints = new Integer[objects.length];
                                    for(int i = 0; i < ints.length; i++){
                                        ints[i] = Integer.parseInt(String.valueOf(objects[i]));
                                    }
                                    method.invoke(dx,(Object)ints);
                                }
                                break ws;
                            }catch (Exception e){
                                e.printStackTrace();
                            }

                        }
                        else if(c[0]==byte[].class||c[0]==Byte[].class){
                            try {
                                if(c[0]==byte[].class){
                                    byte[] objects = (byte[])value;
                                    byte[] bytes = new byte[objects.length];
                                    for(int i = 0; i < bytes.length; i++){
                                        bytes[i] = Byte.parseByte(String.valueOf(objects[i]));
                                    }
                                    method.invoke(dx,(Object)bytes);
                                }
                                if(c[0]==Byte[].class){
                                    Byte[] objects = (Byte[])value;
                                    Byte[] bytes = new Byte[objects.length];
                                    for(int i = 0; i < bytes.length; i++){
                                        bytes[i] = Byte.parseByte(String.valueOf(objects[i]));
                                    }
                                    method.invoke(dx,(Object)bytes);
                                }
                                break ws;
                            }catch (Exception e){
                                e.printStackTrace();
                            }
                        }
                        else if(c[0]==char[].class||c[0]==Character[].class){
                            try {


                                if(c[0]==char[].class){
                                    char[] objects = (char[])value;
                                    char[] chars = new char[objects.length];
                                    for(int i = 0; i < chars.length; i++){
                                        chars[i] = (char)objects[i];
                                    }
                                    method.invoke(dx,(Object)chars);
                                }
                                if(c[0]==Character[].class){
                                    Character[] objects = (Character[])value;
                                    Character[] characters = new Character[objects.length];
                                    for(int i = 0; i < characters.length; i++){
                                        characters[i] = (Character)objects[i];
                                    }
                                    method.invoke(dx,(Object)characters);
                                }
                                break ws;
                            }catch (Exception e){
                                e.printStackTrace();
                            }
                        }
                        else if(c[0]==boolean[].class||c[0]==Boolean[].class){
                            try {
                                if(c[0]==boolean[].class){
                                    boolean[] objects = (boolean[])value;
                                    boolean[] booleans = new boolean[objects.length];
                                    for(int i = 0; i < booleans.length; i++){
                                        booleans[i] = Boolean.parseBoolean(String.valueOf(objects[i]));
                                    }
                                    method.invoke(dx,(Object)booleans);
                                }
                                if(c[0]==Boolean[].class){
                                    Boolean[] objects = (Boolean[])value;
                                    Boolean[] booleans = new Boolean[objects.length];
                                    for(int i = 0; i < booleans.length; i++){
                                        booleans[i] = Boolean.parseBoolean(String.valueOf(objects[i]));
                                    }
                                    method.invoke(dx,(Object)booleans);
                                }
                                break ws;
                            }catch (Exception e){
                                e.printStackTrace();
                            }
                        }
                        else if(c[0]==short[].class||c[0]==Short[].class){
                            try {


                                if(c[0]==short[].class){
                                    short[] objects = (short[])value;
                                    short[] shorts = new short[objects.length];
                                    for(int i = 0; i < shorts.length; i++){
                                        shorts[i] = Short.parseShort(String.valueOf(objects[i]));
                                    }
                                    method.invoke(dx,(Object)shorts);
                                }
                                if(c[0]==Short[].class) {
                                    Short[] objects = (Short[])value;
                                    Short[] shorts = new Short[objects.length];
                                    for (int i = 0; i < shorts.length; i++) {
                                        shorts[i] = Short.parseShort(String.valueOf(objects[i]));
                                    }
                                    method.invoke(dx, (Object)shorts);
                                }
                                break ws;
                            }catch (Exception e){
                                e.printStackTrace();
                            }
                        }
                        else if(c[0]==long[].class||c[0]==Long[].class){
                            try {



                                if(c[0]==long[].class){
                                    long[] objects = (long[])value;
                                    long[] longs = new long[objects.length];
                                    for(int i = 0; i < longs.length; i++){
                                        longs[i] = Long.parseLong(String.valueOf(objects[i]));
                                    }
                                    method.invoke(dx,(Object)longs);
                                }
                                if(c[0]==Long[].class) {
                                    Long[] objects = (Long[])value;
                                    Long[] longs = new Long[objects.length];
                                    for (int i = 0; i < longs.length; i++) {
                                        longs[i] = Long.parseLong(String.valueOf(objects[i]));
                                    }
                                    method.invoke(dx, (Object) longs);
                                }
                                break ws;
                            }catch (Exception e){
                                e.printStackTrace();
                            }
                        }
                        else if(c[0]==float[].class||c[0]==Float[].class){
                            try {
                                if(c[0]==float[].class){
                                    float[] objects = (float[])value;
                                    float[] floats = new float[objects.length];
                                    for(int i = 0; i < floats.length; i++){
                                        floats[i] = Float.parseFloat(String.valueOf(objects[i]));
                                    }
                                    method.invoke(dx,(Object)floats);
                                }
                                if(c[0]==Float[].class) {
                                    Float[] objects = (Float[])value;
                                    Float[] floats = new Float[objects.length];
                                    for (int i = 0; i < floats.length; i++) {
                                        floats[i] = Float.parseFloat(String.valueOf(objects[i]));
                                    }
                                    method.invoke(dx, (Object)floats);
                                }
                                break ws;
                            }catch (Exception e){
                                e.printStackTrace();
                            }
                        }
                        else if(c[0]==double[].class||c[0]==Double[].class){
                            try {
                                if(c[0]==double[].class){
                                    double[] objects = (double[])value;
                                    double[] doubles = new double[objects.length];
                                    for(int i = 0; i < doubles.length; i++){
                                        doubles[i] = Double.parseDouble(String.valueOf(objects[i]));
                                    }
                                    method.invoke(dx,(Object)doubles);
                                }
                                if(c[0]==Double[].class) {
                                    Double[] objects = (Double[])value;
                                    Double[] doubles = new Double[objects.length];
                                    for (int i = 0; i < doubles.length; i++) {
                                        doubles[i] = Double.parseDouble(String.valueOf(objects[i]));
                                    }
                                    method.invoke(dx, (Object)doubles);
                                }
                                break ws;
                            }catch (Exception e){
                                e.printStackTrace();
                            }
                        }
                        else if(c[0]==String[].class){
                            try {
                                Object[] objects = (Object[])value;
                                String[] strings = new String[objects.length];
                                for(int i = 0; i < strings.length; i++){
                                    strings[i] = String.valueOf(objects[i]);
                                }
                                method.invoke(dx,(Object)strings);
                                break ws;
                            }catch (Exception e){
                                e.printStackTrace();
                            }
                        }else if(c[0]==BigInteger[].class){
                            try {
                                Object[] objects = (Object[])value;
                                BigInteger[] bigIntegers = new BigInteger[objects.length];
                                for(int i = 0; i < bigIntegers.length; i++){
                                    bigIntegers[i] = BigInteger.valueOf(Long.parseLong(String.valueOf(objects[i])));
                                }
                                method.invoke(dx,(Object)bigIntegers);
                                break ws;
                            }catch (Exception e){
                                e.printStackTrace();
                            }
                        }else if(c[0]==BigDecimal[].class){
                            try {

                                Object[] objects = (Object[])value;
                                BigDecimal[] bigDecimals = new BigDecimal[objects.length];
                                for(int i = 0; i < bigDecimals.length; i++){
                                    bigDecimals[i] = new BigDecimal(String.valueOf(objects[i]));
                                }
                                method.invoke(dx,(Object)bigDecimals);

                                break ws;
                            }catch (Exception e){
                                e.printStackTrace();
                            }
                        }else if(c[0]==Date[].class){
                            try {

                                Object[] objects = (Object[])value;
                                Date[] dates = new Date[objects.length];
                                for(int i = 0; i < dates.length; i++) {
                                    dates[i] = (Date) objects[i];
                                }
                                method.invoke(dx,(Object)dates);
                                break ws;
                            }catch (Exception e){
                                e.printStackTrace();
                            }
                        }else if(c[0].isArray()){
                            Object[] objects = (Object[]) value;
                            for(int i = 0; i < objects.length; i++){
                                try {
                                    objects[i] = mapToObject(objects[i],Class.forName(c[0].toString().substring(8,c[0].toString().length()-1)));
                                }catch (Exception e){
                                    e.printStackTrace();
                                }
                            }
                            try {
                                method.invoke(dx,(Object)objects);
                            }catch (Exception e){
                                e.printStackTrace();
                            }

                        }else if(Collection.class.isAssignableFrom(c[0])){
                            String typeString = null;
                            try {
                                Field field = clazz.getDeclaredField(sKey);
                                if(field != null){
                                    ParameterizedType parameterizedType = (ParameterizedType)field.getGenericType();
                                    Type type = parameterizedType.getActualTypeArguments()[0];
                                    typeString = type.getTypeName();
                                }
                            }catch (Exception e){
                                e.printStackTrace();
                            }

                            if(typeString==null){
                                if(value instanceof Collection){
                                    try {
                                        method.invoke(dx,value);
                                    }catch (Exception e){
                                        e.printStackTrace();
                                    }
                                }else {
                                    try {
                                        List list = new ArrayList();
                                        list.add(value);
                                        method.invoke(dx,list);
                                    }catch (Exception e){
                                        e.printStackTrace();
                                    }
                                }
                            }else {
                                try {
                                    Collection collection = (Collection)value;
                                    Collection collection1 = new ArrayList();
                                    Class clazz1 = Class.forName(typeString);
                                    Iterator iterator1 = collection.iterator();
                                    while (iterator1.hasNext()){
                                        Object o = iterator1.next();
                                        if(o instanceof Map&&!Map.class.isAssignableFrom(clazz1)){
                                            collection1.add(mapToObject(o,clazz1));
                                        }else {
                                            collection1.add(o);
                                        }
                                    }
                                    method.invoke(dx,collection1);
                                }catch (Exception e){
                                    if(value instanceof Collection){
                                        try {
                                            method.invoke(dx,value);
                                        }catch (Exception e1){
                                            e.printStackTrace();
                                        }
                                    }else {
                                        try {
                                            List list = new ArrayList();
                                            list.add(value);
                                            method.invoke(dx,list);
                                        }catch (Exception e1){
                                            e.printStackTrace();
                                        }
                                    }
                                }

                            }
                           /* List list = (List) value;
                            for(int i = 0; i < list.size(); i++){
                                if(list.get(i) instanceof Map){
                                    try {
                                        Field field = clazz.getDeclaredField(sKey);
                                        if(field != null){
                                            ParameterizedType parameterizedType = (ParameterizedType)field.getGenericType();
                                            Type type = parameterizedType.getActualTypeArguments()[0];
                                            if(type != null){
                                                Object o = mapToObject(list.get(i),Class.forName(type.getTypeName()));

                                            }
                                        }
                                    }catch (Exception e){
                                        e.printStackTrace();



                                    }

                                    //c[0].getComponentType()
                                }
                            }*/
                        }
                        else {
                            if(value instanceof Map){
                                try {
                                    method.invoke(dx,mapToObject(value,c[0]));
                                    break ws;
                                }catch (Exception e){
                                    e.printStackTrace();
                                }
                            }
                        }
                    }

                }
            }
        }
        return dx;
    }


    public static  <T> T  objectToT(Object object,Class<T> tClass){
        try {
            if(tClass==int.class){
                if(object.getClass()==int.class){
                    return (T)object;
                }else if(object.getClass()==Integer.class){
                    return (T)Integer.valueOf((int)object);
                }else {
                    return (T)Integer.valueOf(Double.valueOf(String.valueOf(object)).intValue());
                }
            }else if(tClass==Integer.class){
                if(object.getClass()==int.class){
                    return (T)Integer.valueOf((int)object);
                }else if(object.getClass()==Integer.class){
                    return (T)object;
                }else {
                    return (T)Integer.valueOf(Double.valueOf(String.valueOf(object)).intValue());
                }
            }else if(tClass==short.class){
                if(object.getClass()==short.class){
                    return (T)object;
                }else if(object.getClass()==Short.class){
                    return (T)((Object)((Short)object).shortValue());
                }else {
                    return (T)Short.valueOf(Double.valueOf(String.valueOf(object)).shortValue());
                }
            }else if(tClass==Short.class){
                if(object.getClass()==short.class){
                    return (T)Short.valueOf((short)object);
                }else if(object.getClass()==Short.class){
                    return (T)object;
                }else {
                    return  (T)Short.valueOf(Double.valueOf(String.valueOf(object)).shortValue());
                }
            }else if(tClass==char.class){
                if(object.getClass()==char.class){
                    return (T)object;
                }else if(object.getClass()==Character.class){
                    return (T)((Object)((Character)object).charValue());
                }else {
                    return (T)(Object)String.valueOf(object).charAt(0);
                }

            }else if(tClass==Character.class){
                if(object.getClass()==char.class){
                    return (T)object;
                }else if(object.getClass()==Character.class){
                    return (T)object;
                }else {
                    return (T)(Object)String.valueOf(object).charAt(0);
                }
            }else if(tClass==byte.class){
                if(object.getClass()==byte.class){
                    return (T)object;
                }else if(object.getClass()==Byte.class){
                    return (T)((Object)((Byte)object).byteValue());
                }else {
                    return (T)((Object)Byte.parseByte(String.valueOf(object)));
                }
            }else if(tClass==Byte.class){
                if(object.getClass()==byte.class){
                    return (T)Byte.valueOf((byte)object);
                }else if(object.getClass()==Byte.class){
                    return (T)object;
                }else {
                    return (T)((Object)Byte.valueOf(String.valueOf(object)));
                }
            }else if(tClass==boolean.class){
                if(object.getClass()==boolean.class){
                    return (T)object;
                }else if(object.getClass()==Boolean.class){
                    return (T)((Object)((Boolean)object).booleanValue());
                }else {
                    return (T)((Object)Boolean.parseBoolean(String.valueOf(object)));
                }
            }else if(tClass==Boolean.class){
                if(object.getClass()==boolean.class){
                    return (T)Boolean.valueOf((boolean)object);
                }else if(object.getClass()==Boolean.class){
                    return (T)object;
                }else {
                    return (T)((Object)Boolean.valueOf(String.valueOf(object)));
                }
            }else if(tClass==float.class){
                if(object.getClass()==float.class){
                    return (T)object;
                }else if(object.getClass()==Float.class){
                    return (T)((Object)((Float)object).floatValue());
                }else {
                    return (T)((Object)Float.parseFloat(String.valueOf(object)));
                }
            }else if(tClass==Float.class){
                if(object.getClass()==float.class){
                    return (T)Float.valueOf((float)object);
                }else if(object.getClass()==Float.class){
                    return (T)object;
                }else {
                    return (T)((Object)Float.valueOf(String.valueOf(object)));
                }
            }else if(tClass==double.class){
                if(object.getClass()==double.class){
                    return (T)object;
                }else if(object.getClass()==Double.class){
                    return (T)((Object)((Double)object).doubleValue());
                }else {
                    return (T)((Object)Double.parseDouble(String.valueOf(object)));
                }
            }else if(tClass==Double.class){
                if(object.getClass()==double.class){
                    return (T)Double.valueOf((double)object);
                }else if(object.getClass()==Double.class){
                    return (T)object;
                }else {
                    return (T)((Object)Double.valueOf(String.valueOf(object)));
                }
            }else if(tClass==long.class){
                if(object.getClass()==long.class){
                    return (T)object;
                }else if(object.getClass()==Long.class){
                    return (T)((Object)((Long)object).longValue());
                }else {
                    return  (T)Long.valueOf(Double.valueOf(String.valueOf(object)).longValue());
                }
            }else if(tClass==Long.class){
                if(object.getClass()==long.class){
                    return (T)Long.valueOf((long)object);
                }else if(object.getClass()==Long.class){
                    return (T)object;
                }else {
                    return (T)Long.valueOf(Double.valueOf(String.valueOf(object)).longValue());
                }
            }else if(tClass==BigInteger.class){
                if(object.getClass()==BigInteger.class){
                    return (T)object;
                }else {
                    return (T)new BigInteger(String.valueOf(object));
                }
            }else if(tClass==BigDecimal.class){
                if(object.getClass()==BigDecimal.class){
                    return (T)object;
                }else {
                    return (T)new BigDecimal(String.valueOf(object));
                }
            }else if(tClass==String.class){
                if(object.getClass()==String.class){
                    return (T)object;
                }else {
                    return (T) WsStringUtils.anyToString(object);
                }
            }else if(tClass==Date.class || tClass == LocalDateTime.class || tClass == LocalDate.class){
                if(object.getClass()==Date.class) {
                    if(tClass == LocalDate.class){
                        return (T)LocalDate.ofInstant(((Date) object).toInstant(), ZoneId.systemDefault());
                    }
                    if(tClass == LocalDateTime.class){
                        return (T)LocalDateTime.ofInstant(((Date) object).toInstant(),ZoneId.systemDefault());
                    }
                    return (T) object;
                }else {
                    /*Date date = null;
                    try {
                        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ");
                        date = simpleDateFormat.parse(String.valueOf(object));
                    }catch (Exception e){
                        try {
                            Long dateNum = Long.valueOf(new BigDecimal(String.valueOf(object)).longValue());
                            if(dateNum != null){
                                return (T)new Date(dateNum);
                            }else {
                                return null;
                            }
                        }catch (Exception e1){
                            return (T) WsDateUtils.objectToDate(object);
                        }

                    }*/


                    Date date = WsDateUtils.objectToDate(object);
                    if(date == null){
                        return null;
                    }else {
                        if(tClass == LocalDate.class){
                            return (T)LocalDate.ofInstant(date.toInstant(),ZoneId.systemDefault());
                        }
                        if(tClass == LocalDateTime.class){
                            return (T) LocalDateTime.ofInstant(date.toInstant(),ZoneId.systemDefault());
                        }
                        return (T)date;
                    }

                }
            }else {
                try {
                    Object newObject = tClass.getDeclaredConstructor().newInstance();
                    newObject = WsBeanUtis.copyField(object,newObject);
                    return (T)newObject;
                }catch (Exception e){
                    e.printStackTrace();
                    return null;
                }

            }
        } catch (Exception e){
            return null;
        }

    }


    public static<T> List<T> mapToObjectList(List list,Class<T> clazz){
        if(list==null||list.size()==0){
            return null;
        }
        List<T> list1 = new ArrayList<>();
        for(Object object:list){
            if(object instanceof Map){
                list1.add(mapToObject((Map)object,clazz));
            }
        }
        return list1;
    }

}
