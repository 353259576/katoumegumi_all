package cn.katoumegumi.java.common;

import java.io.Serializable;
import java.util.function.Supplier;

/**
 * @author ws
 */
@FunctionalInterface
public interface SupplierFunc<T> extends Supplier<T>, Serializable {
}
