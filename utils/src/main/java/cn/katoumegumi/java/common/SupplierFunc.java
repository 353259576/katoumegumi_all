package cn.katoumegumi.java.common;

import java.io.Serializable;
import java.util.function.Supplier;

/**
 * @author ws
 */
public interface SupplierFunc<T> extends Supplier<T>, Serializable{
}
