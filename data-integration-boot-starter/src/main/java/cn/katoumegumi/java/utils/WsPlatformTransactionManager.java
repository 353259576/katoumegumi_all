package cn.katoumegumi.java.utils;

import lombok.Data;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.orm.hibernate5.HibernateTransactionManager;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.TransactionException;
import org.springframework.transaction.TransactionStatus;

@Data
public class WsPlatformTransactionManager implements PlatformTransactionManager {
    private static ThreadLocal threadLocal = new ThreadLocal();
    @Autowired(required = false)
    private HibernateTransactionManager hibernateTransactionManager;

    @Autowired(required = false)
    private JpaTransactionManager jpaTransactionManager;

    @Autowired(required = false)
    private DataSourceTransactionManager dataSourceTransactionManager;


    @Override
    public TransactionStatus getTransaction(TransactionDefinition transactionDefinition) throws TransactionException {
        return jpaTransactionManager.getTransaction(transactionDefinition);
    }

    @Override
    public void commit(TransactionStatus transactionStatus) throws TransactionException {
        jpaTransactionManager.commit(transactionStatus);
    }

    @Override
    public void rollback(TransactionStatus transactionStatus) throws TransactionException {
        jpaTransactionManager.rollback(transactionStatus);
    }
}
