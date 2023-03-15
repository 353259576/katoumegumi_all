package cn.katoumegumi.java.sql.test.model;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

import javax.persistence.*;
import java.time.LocalDateTime;
import java.util.List;

//@Entity
@Table(name = "ws_user_CC")
@TableName(value = "ws_user_cc")
public class UserCC {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @TableId(type = IdType.AUTO)
    private Long id;

    @Column(name = "name", length = 20)
    @TableField(value = "name")
    private String name;

    @Column(name = "password", length = 20)
    @TableField(value = "password")
    private String password;

    @Column(name = "create_date")
    @TableField(value = "create_date")
    private LocalDateTime createDate;

    /*@OneToMany
    @JoinColumn(name = "user_id")*/
    private List<UserDetailsRemake> userDetails;

    public Long getId() {
        return id;
    }

    public UserCC setId(Long id) {
        this.id = id;
        return this;
    }

    public String getName() {
        return name;
    }

    public UserCC setName(String name) {
        this.name = name;
        return this;
    }

    public String getPassword() {
        return password;
    }

    public UserCC setPassword(String password) {
        this.password = password;
        return this;
    }

    public LocalDateTime getCreateDate() {
        return createDate;
    }

    public UserCC setCreateDate(LocalDateTime createDate) {
        this.createDate = createDate;
        return this;
    }

    public List<UserDetailsRemake> getUserDetails() {
        return userDetails;
    }

    public UserCC setUserDetails(List<UserDetailsRemake> userDetails) {
        this.userDetails = userDetails;
        return this;
    }
}
