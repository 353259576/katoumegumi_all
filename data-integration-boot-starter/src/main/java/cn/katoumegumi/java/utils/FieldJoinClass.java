package cn.katoumegumi.java.utils;

import lombok.Data;

import javax.persistence.criteria.JoinType;

/**
 * @author ws
 */
@Data
public class FieldJoinClass {
    private String nickName;
    private Class joinClass;
    private JoinType joinType;
    private String joinColumn;
    private String anotherJoinColumn;
}
