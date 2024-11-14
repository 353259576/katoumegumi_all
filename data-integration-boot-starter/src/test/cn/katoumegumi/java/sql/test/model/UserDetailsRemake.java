package cn.katoumegumi.java.sql.test.model;


import jakarta.persistence.*;

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

    @Column(name = "remake", length = 250, nullable = true)
    private String remake;

    public Long getId() {
        return id;
    }

    public UserDetailsRemake setId(Long id) {
        this.id = id;
        return this;
    }

    public Long getUserDetailsId() {
        return userDetailsId;
    }

    public UserDetailsRemake setUserDetailsId(Long userDetailsId) {
        this.userDetailsId = userDetailsId;
        return this;
    }

    public String getRemake() {
        return remake;
    }

    public UserDetailsRemake setRemake(String remake) {
        this.remake = remake;
        return this;
    }
}
