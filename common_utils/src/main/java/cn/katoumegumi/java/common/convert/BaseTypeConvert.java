package cn.katoumegumi.java.common.convert;

public interface BaseTypeConvert<T> {

    /**
     * 通用转化
     *
     * @param source
     * @return
     */
    T convert(Object source);

}
