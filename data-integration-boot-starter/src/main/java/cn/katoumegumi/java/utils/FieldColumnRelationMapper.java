package cn.katoumegumi.java.utils;

import lombok.Data;

import java.lang.reflect.Field;
import java.util.*;

/**
 * @author ws
 */
@Data
public class FieldColumnRelationMapper {
    private String tableName;
    private List<FieldColumnRelation> idSet = new ArrayList<>();
    private List<FieldColumnRelation> fieldColumnRelations = new ArrayList<>();
    private Map<String,Class> joinMap = new HashMap<>();



}
