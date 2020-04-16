package cn.katoumegumi.java.utils;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.DefaultTransactionDefinition;

@Component
public class WsTranactionUtils {

    @Autowired
    private PlatformTransactionManager platformTransactionManager;

    public static void main(String[] args) {

    }

    public Object runMethod(WsTranactionRun runnable) {
        DefaultTransactionDefinition transactionDefinition = new DefaultTransactionDefinition();
        transactionDefinition.setIsolationLevel(TransactionDefinition.ISOLATION_READ_COMMITTED);
        transactionDefinition.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRED);
        TransactionStatus transactionStatus = platformTransactionManager.getTransaction(transactionDefinition);
        Object object = null;
        try {
            object = runnable.run();
            platformTransactionManager.commit(transactionStatus);
        } catch (Exception e) {
            e.printStackTrace();
            platformTransactionManager.rollback(transactionStatus);
        }
        return object;
    }

    public interface WsTranactionRun {
        public Object run() throws RuntimeException;
    }

}

