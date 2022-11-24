package cn.katoumegumi.java.sql.test.model;

import javax.persistence.Column;
import javax.persistence.Id;
import javax.persistence.Table;

@Table(name = "l_user")
public class LUser {

    @Id
    private Long id;

    @Column
    private String name;

    @Column
    private String password;

    @Column
    private Integer status;

    @Column
    private Integer sex;

    @Column
    private Integer age;


    public Long getId() {
        return id;
    }

    public LUser setId(Long id) {
        this.id = id;
        return this;
    }

    public String getName() {
        return name;
    }

    public LUser setName(String name) {
        this.name = name;
        return this;
    }

    public String getPassword() {
        return password;
    }

    public LUser setPassword(String password) {
        this.password = password;
        return this;
    }

    public Integer getStatus() {
        return status;
    }

    public LUser setStatus(Integer status) {
        this.status = status;
        return this;
    }

    public Integer getSex() {
        return sex;
    }

    public LUser setSex(Integer sex) {
        this.sex = sex;
        return this;
    }

    public Integer getAge() {
        return age;
    }

    public LUser setAge(Integer age) {
        this.age = age;
        return this;
    }
}
