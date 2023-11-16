package cn.katoumegumi.java.sql.common;

public enum OrderByTypeEnums {
    ASC("asc"),
    DESC("desc");

    private final String orderByType;

    OrderByTypeEnums(String orderByType) {
        this.orderByType = orderByType;
    }

    public String getOrderByType() {
        return orderByType;
    }

    public static OrderByTypeEnums getByType(String orderByType){
        if (orderByType == null){
            throw new NullPointerException("orderByTypeEnums type is null");
        }
        if (OrderByTypeEnums.ASC.getOrderByType().equals(orderByType)){
            return OrderByTypeEnums.ASC;
        }else if (OrderByTypeEnums.DESC.getOrderByType().equals(orderByType)){
            return OrderByTypeEnums.DESC;
        }
        return OrderByTypeEnums.valueOf(orderByType);
    }
}
