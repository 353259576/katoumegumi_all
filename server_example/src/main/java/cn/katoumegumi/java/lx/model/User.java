package cn.katoumegumi.java.lx.model;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import javax.persistence.*;
import java.time.LocalDateTime;
import java.util.List;

//@Entity
//@Table(name = "ws_user")
@Data
@TableName(value = "ws_user")
public class User {
    //@Id
    //@GeneratedValue(strategy = GenerationType.IDENTITY)
    @TableId(type = IdType.AUTO)
    private Long id;

    //@Column(name = "name")
    @TableField(value = "name")
    private String name;

    //@Column(name = "password")
    @TableField(value = "password")
    private String password;

    //@Column(name = "create_date")
    @TableField(value = "create_date")
    private LocalDateTime createDate;

    //@OneToMany
    //@JoinColumn(name = "user_id")
    private List<UserDetails> userDetails;
}
