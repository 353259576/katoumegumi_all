package cn.katoumegumi.java.common;

import java.io.Serializable;
import java.util.function.Function;

/**
 * @author ws
 */
@FunctionalInterface
public interface SFunction<T, R> extends Function<T, R>, Serializable {

}
