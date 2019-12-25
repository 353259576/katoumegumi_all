package cn.katoumegumi.java.hibernate;

import lombok.Data;

import javax.persistence.criteria.JoinType;

/**
 * @author ws
 */
@Data
public class TableRelation {

    private JoinType joinType;

    private Class<?> joinTableClass;

    private String tableNickName;

    private String tableColumn;

    private String joinTableName;

    private String joinTableNickName;

    private String joinTableColumn;

}
