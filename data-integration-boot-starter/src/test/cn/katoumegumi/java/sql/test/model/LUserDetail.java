package cn.katoumegumi.java.sql.test.model;

import jakarta.persistence.*;

@Table(name = "l_user_detail")
public class LUserDetail {

    @Id
    private Long id;

    @Column(name = "detail_value")
    private String detailValue;

    @Column(name = "user_id")
    private Long userId;

    @Column
    private Integer status;

    @Column(name = "detail_type")
    private Integer detailType;

    public Long getId() {
        return id;
    }

    public LUserDetail setId(Long id) {
        this.id = id;
        return this;
    }

    public String getDetailValue() {
        return detailValue;
    }

    public LUserDetail setDetailValue(String detailValue) {
        this.detailValue = detailValue;
        return this;
    }

    public Long getUserId() {
        return userId;
    }

    public LUserDetail setUserId(Long userId) {
        this.userId = userId;
        return this;
    }

    public Integer getStatus() {
        return status;
    }

    public LUserDetail setStatus(Integer status) {
        this.status = status;
        return this;
    }

    public Integer getDetailType() {
        return detailType;
    }

    public LUserDetail setDetailType(Integer detailType) {
        this.detailType = detailType;
        return this;
    }
}
