package com.ws.java.lx.jpa;

import com.ws.java.lx.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface UserJpaDao extends JpaRepository<User,Long>, JpaSpecificationExecutor<User> {


    @Query("select u from User u")
    public List<User> selectUser();

}
