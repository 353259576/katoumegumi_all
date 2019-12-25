package cn.katoumegumi.java.utils;

import lombok.Data;

import javax.persistence.criteria.JoinType;
import java.lang.reflect.Field;

/**
 * @author ws
 */
@Data
public class FieldJoinClass {
    private boolean isArray;
    private String nickName;
    private Class joinClass;
    private JoinType joinType;
    private String joinColumn;
    private String anotherJoinColumn;
    private Field field;
}
