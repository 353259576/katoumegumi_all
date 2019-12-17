package com.ws.java.lx.service;

import com.ws.java.hibernate.JpaDao;
import com.ws.java.hibernate.JpaService;
import com.ws.java.hibernate.PagingService;
import com.ws.java.lx.jpa.UserJpaDao;
import com.ws.java.lx.model.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * @author ws
 * @date Created by Administrator on 2019/12/17 10:01
 */
@Service
public class IndexServiceImpl extends JpaService<Long, User,UserJpaDao> implements UserService {

    @Autowired
    private UserJpaDao userJpaDao;
}
