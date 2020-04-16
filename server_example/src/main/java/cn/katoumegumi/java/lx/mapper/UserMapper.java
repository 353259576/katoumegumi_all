package cn.katoumegumi.java.lx.mapper;

import cn.katoumegumi.java.lx.model.User;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Transactional
public interface UserMapper extends BaseMapper<User> {

    public List<User> selectUserList();
}
