package cn.katoumegumi.java.sql.entity;

import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;

/**
 * @author ws
 */
public class SelectCallable<T> implements Callable<List<T>> {

    private final CountDownLatch countDownLatch = new CountDownLatch(1);

    private List<T> valueList = null;

    @Override
    public List<T> call() throws Exception {
        countDownLatch.await();
        return valueList;
    }

    public void put(List<T> list){
        this.valueList = list;
        countDownLatch.countDown();
    }


}
