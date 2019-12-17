package cn.katoumegumi.java.hibernate;

import lombok.Data;

import javax.persistence.criteria.JoinType;

/**
 * @author ws
 * @date Created by Administrator on 2019/12/9 14:53
 */
@Data
public class TableRelation {

    private JoinType joinType;

    private String tableName;

    private String tableColumn;

    private String joinTableName;

    private String joinTableColumn;

}
