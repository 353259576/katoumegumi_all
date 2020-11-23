package cn.katoumegumi.java.lx.model;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import org.hibernate.annotations.Fetch;
import org.hibernate.annotations.FetchMode;

import javax.persistence.*;
import java.util.List;

@Entity
@Table(name = "ws_user_details")
@Data
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
    @JoinColumn(name = "user_details_id")
    //@Fetch(FetchMode.JOIN)
    //@Transient
    private List<UserDetailsRemake> userDetailsRemake;

    //private List<UserDetailsRemake> userDetailsRemakeList;
}
