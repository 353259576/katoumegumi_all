package cn.katoumegumi.java.sql.test.model;

import cn.katoumegumi.java.common.model.KeyValue;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

import javax.persistence.Column;
import javax.persistence.Id;
import javax.persistence.Table;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;


@TableName(value = "ws_user")
@Table(name = "ws_user")
public class User implements Serializable {



    @TableId
    @Id
    private Long id;

    @TableField(value = "name")
    @Column(name = "name")
    private String name;

    @TableField(value = "password")
    @Column(name = "password")
    private String password;

    @TableField(value = "create_date",exist = false)
    //@Column(name = "create_date")
    private LocalDateTime createDate;

    //private User user;


    private List<UserDetails> userDetails;
    //private UserDetails userDetails;

    private List<KeyValue<String,? extends UserDetails>> keyValueList;

    public Long getId() {
        return id;
    }

    public User setId(Long id) {
        this.id = id;
        return this;
    }

    public String getName() {
        return name;
    }

    public User setName(String name) {
        this.name = name;
        return this;
    }

    public String getPassword() {
        return password;
    }

    public User setPassword(String password) {
        this.password = password;
        return this;
    }

    public LocalDateTime getCreateDate() {
        return createDate;
    }

    public User setCreateDate(LocalDateTime createDate) {
        this.createDate = createDate;
        return this;
    }

    public List<UserDetails> getUserDetails() {
        return userDetails;
    }

    public User setUserDetails(List<UserDetails> userDetails) {
        this.userDetails = userDetails;
        return this;
    }

    public List<KeyValue<String, ? extends UserDetails>> getKeyValueList() {
        return keyValueList;
    }

    public User setKeyValueList(List<KeyValue<String, ? extends UserDetails>> keyValueList) {
        this.keyValueList = keyValueList;
        return this;
    }
}
