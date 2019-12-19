package cn.katoumegumi.java.lx.jpa;

import cn.katoumegumi.java.hibernate.JpaDao;
import cn.katoumegumi.java.lx.model.User;
import org.springframework.data.jpa.repository.Query;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Transactional
public interface UserJpaDao extends JpaDao<Long, User> {


    @Query("select u from User u")
    public List<User> selectUser();

}
