package cn.katoumegumi.java.sql.test.model;

import jakarta.persistence.*;

@Table(name = "l_user_detail_remark")
public class LUserDetailRemark {

    @Id
    private Long id;

    @Column
    private String remark;

    @Column(name = "remark_type")
    private String remarkType;

    @Column(name = "user_detail_id")
    private Long userDetailId;

    @Column
    private Integer status;

    public Long getId() {
        return id;
    }

    public LUserDetailRemark setId(Long id) {
        this.id = id;
        return this;
    }

    public String getRemark() {
        return remark;
    }

    public LUserDetailRemark setRemark(String remark) {
        this.remark = remark;
        return this;
    }

    public String getRemarkType() {
        return remarkType;
    }

    public LUserDetailRemark setRemarkType(String remarkType) {
        this.remarkType = remarkType;
        return this;
    }

    public Long getUserDetailId() {
        return userDetailId;
    }

    public LUserDetailRemark setUserDetailId(Long userDetailId) {
        this.userDetailId = userDetailId;
        return this;
    }

    public Integer getStatus() {
        return status;
    }

    public LUserDetailRemark setStatus(Integer status) {
        this.status = status;
        return this;
    }
}
