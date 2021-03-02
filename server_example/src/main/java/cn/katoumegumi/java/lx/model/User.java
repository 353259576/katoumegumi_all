package cn.katoumegumi.java.lx.model;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import org.hibernate.annotations.Fetch;
import org.hibernate.annotations.FetchMode;

import javax.persistence.*;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "ws_user")
@Data
@TableName(value = "ws_user")
public class User implements Serializable {


    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @TableId
    private Long id;

    @Column(name = "name")
    @TableField(value = "name")
    private String name;

    @Column(name = "password")
    @TableField(value = "password")
    private String password;

    @Column(name = "create_date")
    @TableField(value = "create_date")
    private LocalDateTime createDate;

    //@Transient
    @OneToMany(fetch = FetchType.EAGER,mappedBy = "userId")
    //@JoinColumn(name= "user_id")
    //Fetch(FetchMode.JOIN)
    private List<UserDetails> userDetails;

    @Column(name = "user_id")
    private Long userId;

    //private int[] ints = new int[]{1,2,3};

}
