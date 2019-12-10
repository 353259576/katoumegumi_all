package com.ws.java.lx.model;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import javax.persistence.*;

@Entity
@Table(name = "ws_user")
@Data
@TableName(value = "ws_user")
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @TableId(type = IdType.AUTO)
    private Long id;

    @Column(name = "name")
    @TableField(value = "name")
    private String name;

    @Column(name = "password")
    @TableField(value = "password")
    private String password;

    @OneToOne
    @JoinColumn(name = "id",referencedColumnName = "user_id")
    private UserDetails userDetails;
}
