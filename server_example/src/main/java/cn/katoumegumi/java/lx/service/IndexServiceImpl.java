package cn.katoumegumi.java.lx.service;

import cn.katoumegumi.java.hibernate.JpaService;
import cn.katoumegumi.java.lx.jpa.UserJpaDao;
import cn.katoumegumi.java.lx.model.User;
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
