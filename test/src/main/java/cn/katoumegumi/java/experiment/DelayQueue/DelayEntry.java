package cn.katoumegumi.java.experiment.DelayQueue;


import java.util.Date;
import java.util.concurrent.*;

public class DelayEntry<T> implements Delayed {
    private T entry;
    private Date createDate;
    private Date executeDate;

    public static void main(String[] args) {
        try {
            Thread.sleep(10000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        DelayQueue<DelayEntry<Integer>> delayQueue = new DelayQueue<>();
        Executor executor = Executors.newFixedThreadPool(4);
        Executor executor2 = Executors.newFixedThreadPool(4);

        executor2.execute(() -> {
            try {
                while (true) {
                    DelayEntry<Integer> delayEntry = delayQueue.take();
                    System.out.println(delayEntry.getEntry());
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        });
        /*new Thread(()->{


        }).start();*/

        long startTime = System.currentTimeMillis();
        String str = null;
        executor.execute(() -> {
            for (int i = 0; i < 10000000; i++) {
                Integer k = i;
                DelayEntry<Integer> delayEntry1 = new DelayEntry<>();
                delayEntry1.setEntry(k);
                delayEntry1.setCreateDate(new Date());
                delayEntry1.setExecuteDate(new Date(System.currentTimeMillis() + 1000L * 60 / 2));
                delayQueue.put(delayEntry1);
            }
        });
        executor.execute(() -> {
            for (int i = 10000000; i < 20000000; i++) {
                Integer k = i;
                DelayEntry<Integer> delayEntry1 = new DelayEntry<>();
                delayEntry1.setEntry(k);
                delayEntry1.setCreateDate(new Date());
                delayEntry1.setExecuteDate(new Date(System.currentTimeMillis() + 1000L * 60 / 2));
                delayQueue.put(delayEntry1);
            }
        });
        executor.execute(() -> {
            for (int i = 20000000; i < 30000000; i++) {
                Integer k = i;
                DelayEntry<Integer> delayEntry1 = new DelayEntry<>();
                delayEntry1.setEntry(k);
                delayEntry1.setCreateDate(new Date());
                delayEntry1.setExecuteDate(new Date(System.currentTimeMillis() + 1000L * 60 / 2));
                delayQueue.put(delayEntry1);
            }
        });


        long endTime = System.currentTimeMillis();
        System.out.println(endTime - startTime);


    }

    @Override
    public long getDelay(TimeUnit unit) {
        return unit.convert(executeDate.getTime(), TimeUnit.MILLISECONDS) - unit.convert(System.currentTimeMillis(), TimeUnit.MILLISECONDS);
    }

    @Override
    public int compareTo(Delayed o) {
        long own = this.getDelay(TimeUnit.NANOSECONDS);
        long compare = o.getDelay(TimeUnit.NANOSECONDS);
        long v = own - compare;
        if (v < 0) {
            return -1;
        } else if (v > 0) {
            return 1;
        } else {
            return 0;
        }
    }


    public T getEntry() {
        return entry;
    }

    public void setEntry(T entry) {
        this.entry = entry;
    }

    public Date getCreateDate() {
        return createDate;
    }

    public void setCreateDate(Date createDate) {
        this.createDate = createDate;
    }

    public Date getExecuteDate() {
        return executeDate;
    }

    public void setExecuteDate(Date executeDate) {
        this.executeDate = executeDate;
    }
}
