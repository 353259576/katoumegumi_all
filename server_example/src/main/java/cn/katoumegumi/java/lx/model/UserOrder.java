package cn.katoumegumi.java.lx.model;

import lombok.Data;

import java.util.Date;

@Data
public class UserOrder {

    public Long orderId;

    public String orderName;

    public Date createDate;

    public Long userId;
}
