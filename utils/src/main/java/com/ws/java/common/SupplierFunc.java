package com.ws.java.common;

import java.io.Serializable;
import java.util.function.Supplier;

/**
 * @author ws
 * @date Created by Administrator on 2019/12/9 9:33
 */
public interface SupplierFunc<T> extends Supplier<T>, Serializable{
}
