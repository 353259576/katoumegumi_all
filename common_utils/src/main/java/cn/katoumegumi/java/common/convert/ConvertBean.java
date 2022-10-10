package cn.katoumegumi.java.common.convert;

public interface ConvertBean<T> {

    /**
     * 通用转化
     *
     * @param bean
     * @return
     */
    T convert(Object bean);

}
