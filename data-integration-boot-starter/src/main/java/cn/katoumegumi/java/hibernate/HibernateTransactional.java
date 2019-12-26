package cn.katoumegumi.java.hibernate;

import org.springframework.core.annotation.AliasFor;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.lang.annotation.*;

/**
 * @author ws
 * jpa与hibernate事务管理不兼容
 */
@Target({ElementType.TYPE, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Inherited
@Transactional(transactionManager = "hibernateTransactionManager")
@Documented
public @interface HibernateTransactional {



    /**
     * A <em>qualifier</em> value for the specified transaction.
     * <p>May be used to determine the target transaction manager,
     * matching the qualifier value (or the bean name) of a specific
     * {@link org.springframework.transaction.PlatformTransactionManager}
     * bean definition.
     * @since 4.2
     * @see #value
     */


    /**
     * The transaction propagation type.
     * <p>Defaults to {@link Propagation#REQUIRED}.
     * @see org.springframework.transaction.interceptor.TransactionAttribute#getPropagationBehavior()
     */
    @AliasFor(annotation = Transactional.class,value = "propagation")
    Propagation propagation() default Propagation.REQUIRED;

    /**
     * The transaction isolation level.
     * <p>Defaults to {@link Isolation#DEFAULT}.
     * <p>Exclusively designed for use with {@link Propagation#REQUIRED} or
     * {@link Propagation#REQUIRES_NEW} since it only applies to newly started
     * transactions. Consider switching the "validateExistingTransactions" flag to
     * "true" on your transaction manager if you'd like isolation level declarations
     * to get rejected when participating in an existing transaction with a different
     * isolation level.
     * @see org.springframework.transaction.interceptor.TransactionAttribute#getIsolationLevel()
     * @see org.springframework.transaction.support.AbstractPlatformTransactionManager#setValidateExistingTransaction
     */
    @AliasFor(annotation = Transactional.class,value = "isolation")
    Isolation isolation() default Isolation.DEFAULT;

    /**
     * The timeout for this transaction (in seconds).
     * <p>Defaults to the default timeout of the underlying transaction system.
     * <p>Exclusively designed for use with {@link Propagation#REQUIRED} or
     * {@link Propagation#REQUIRES_NEW} since it only applies to newly started
     * transactions.
     * @see org.springframework.transaction.interceptor.TransactionAttribute#getTimeout()
     */
    @AliasFor(annotation = Transactional.class,value = "timeout")
    int timeout() default TransactionDefinition.TIMEOUT_DEFAULT;

    /**
     * A boolean flag that can be set to {@code true} if the transaction is
     * effectively read-only, allowing for corresponding optimizations at runtime.
     * <p>Defaults to {@code false}.
     * <p>This just serves as a hint for the actual transaction subsystem;
     * it will <i>not necessarily</i> cause failure of write access attempts.
     * A transaction manager which cannot interpret the read-only hint will
     * <i>not</i> throw an exception when asked for a read-only transaction
     * but rather silently ignore the hint.
     * @see org.springframework.transaction.interceptor.TransactionAttribute#isReadOnly()
     * @see org.springframework.transaction.support.TransactionSynchronizationManager#isCurrentTransactionReadOnly()
     */
    @AliasFor(annotation = Transactional.class,value = "readOnly")
    boolean readOnly() default false;

    /**
     * Defines zero (0) or more exception {@link Class classes}, which must be
     * subclasses of {@link Throwable}, indicating which exception types must cause
     * a transaction rollback.
     * <p>By default, a transaction will be rolling back on {@link RuntimeException}
     * and {@link Error} but not on checked exceptions (business exceptions). See
     * {@link org.springframework.transaction.interceptor.DefaultTransactionAttribute#rollbackOn(Throwable)}
     * for a detailed explanation.
     * <p>This is the preferred way to construct a rollback rule (in contrast to
     * {@link #rollbackForClassName}), matching the exception class and its subclasses.
     * <p>Similar to {@link org.springframework.transaction.interceptor.RollbackRuleAttribute#RollbackRuleAttribute(Class clazz)}.
     * @see #rollbackForClassName
     * @see org.springframework.transaction.interceptor.DefaultTransactionAttribute#rollbackOn(Throwable)
     */
    @AliasFor(annotation = Transactional.class,value = "rollbackFor")
    Class<? extends Throwable>[] rollbackFor() default {};

    /**
     * Defines zero (0) or more exception names (for exceptions which must be a
     * subclass of {@link Throwable}), indicating which exception types must cause
     * a transaction rollback.
     * <p>This can be a substring of a fully qualified class name, with no wildcard
     * support at present. For example, a value of {@code "ServletException"} would
     * match {@code javax.servlet.ServletException} and its subclasses.
     * <p><b>NB:</b> Consider carefully how specific the pattern is and whether
     * to include package information (which isn't mandatory). For example,
     * {@code "Exception"} will match nearly anything and will probably hide other
     * rules. {@code "java.lang.Exception"} would be correct if {@code "Exception"}
     * were meant to define a rule for all checked exceptions. With more unusual
     * {@link Exception} names such as {@code "BaseBusinessException"} there is no
     * need to use a FQN.
     * <p>Similar to {@link org.springframework.transaction.interceptor.RollbackRuleAttribute#RollbackRuleAttribute(String exceptionName)}.
     * @see #rollbackFor
     * @see org.springframework.transaction.interceptor.DefaultTransactionAttribute#rollbackOn(Throwable)
     */
    @AliasFor(annotation = Transactional.class,value = "rollbackForClassName")
    String[] rollbackForClassName() default {};

    /**
     * Defines zero (0) or more exception {@link Class Classes}, which must be
     * subclasses of {@link Throwable}, indicating which exception types must
     * <b>not</b> cause a transaction rollback.
     * <p>This is the preferred way to construct a rollback rule (in contrast
     * to {@link #noRollbackForClassName}), matching the exception class and
     * its subclasses.
     * <p>Similar to {@link org.springframework.transaction.interceptor.NoRollbackRuleAttribute#NoRollbackRuleAttribute(Class clazz)}.
     * @see #noRollbackForClassName
     * @see org.springframework.transaction.interceptor.DefaultTransactionAttribute#rollbackOn(Throwable)
     */
    @AliasFor(annotation = Transactional.class,value = "noRollbackFor")
    Class<? extends Throwable>[] noRollbackFor() default {};

    /**
     * Defines zero (0) or more exception names (for exceptions which must be a
     * subclass of {@link Throwable}) indicating which exception types must <b>not</b>
     * cause a transaction rollback.
     * <p>See the description of {@link #rollbackForClassName} for further
     * information on how the specified names are treated.
     * <p>Similar to {@link org.springframework.transaction.interceptor.NoRollbackRuleAttribute#NoRollbackRuleAttribute(String exceptionName)}.
     * @see #noRollbackFor
     * @see org.springframework.transaction.interceptor.DefaultTransactionAttribute#rollbackOn(Throwable)
     */
    @AliasFor(annotation = Transactional.class,value = "noRollbackForClassName")
    String[] noRollbackForClassName() default {};


}
