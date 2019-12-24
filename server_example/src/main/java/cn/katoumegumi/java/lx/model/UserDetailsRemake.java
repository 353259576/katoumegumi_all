package cn.katoumegumi.java.lx.model;

import com.baomidou.mybatisplus.annotation.TableField;

import javax.persistence.*;

/**
 * @author ws
 */
@Entity
@Table(name = "user_details_remake")
public class UserDetailsRemake {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    private Long id;

    @Column(name = "user_details_id")
    private Long userDetailsId;

    @Column(name = "remake",length = 250,nullable = true)
    private String remake;
}