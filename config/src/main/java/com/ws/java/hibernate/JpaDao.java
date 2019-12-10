package com.ws.java.hibernate;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface JpaDao<ID,M> extends JpaRepository<M,ID>, JpaSpecificationExecutor<M> {

}
