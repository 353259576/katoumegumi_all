package cn.katoumegumi.java.lx.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import cn.katoumegumi.java.lx.model.User;
import org.apache.ibatis.annotations.Param;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;

@Transactional
public interface UserMapper extends BaseMapper<User> {

    public IPage<User> selectUserList(Page<User> page,@Param("user") User user);
}
