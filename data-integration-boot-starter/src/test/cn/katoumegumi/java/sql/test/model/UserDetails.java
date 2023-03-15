package cn.katoumegumi.java.sql.test.model;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

import javax.persistence.*;
import java.util.List;

@Entity
@Table(name = "ws_user_details")
@TableName(value = "ws_user_details")
public class UserDetails {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @TableId(type = IdType.AUTO)
    private Long id;

    @Column(name = "user_id")
    @TableField(value = "user_id")
    private Long userId;

    @Column(name = "nick_name")
    @TableField(value = "nick_name")
    private String nickName;

    @Column(name = "sex")
    private String sex;


    @OneToMany
    @JoinColumn(name = "user_details_id",referencedColumnName = "id")
    private List<UserDetailsRemake> userDetailsRemake;

    //private List<UserDetailsRemake> userDetailsRemakeList;


    public Long getId() {
        return id;
    }

    public UserDetails setId(Long id) {
        this.id = id;
        return this;
    }

    public Long getUserId() {
        return userId;
    }

    public UserDetails setUserId(Long userId) {
        this.userId = userId;
        return this;
    }

    public String getNickName() {
        return nickName;
    }

    public UserDetails setNickName(String nickName) {
        this.nickName = nickName;
        return this;
    }

    public String getSex() {
        return sex;
    }

    public UserDetails setSex(String sex) {
        this.sex = sex;
        return this;
    }

    public List<UserDetailsRemake> getUserDetailsRemake() {
        return userDetailsRemake;
    }

    public UserDetails setUserDetailsRemake(List<UserDetailsRemake> userDetailsRemake) {
        this.userDetailsRemake = userDetailsRemake;
        return this;
    }
}
