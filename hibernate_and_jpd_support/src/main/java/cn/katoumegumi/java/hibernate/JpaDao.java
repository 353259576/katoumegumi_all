package cn.katoumegumi.java.hibernate;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface JpaDao<ID, T> extends JpaRepository<T, ID>, JpaSpecificationExecutor<T> {

}
