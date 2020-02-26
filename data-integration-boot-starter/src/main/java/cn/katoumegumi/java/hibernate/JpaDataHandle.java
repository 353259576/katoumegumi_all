package cn.katoumegumi.java.hibernate;

import cn.katoumegumi.java.common.WsBeanUtis;
import cn.katoumegumi.java.common.WsDateUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.jpa.domain.Specification;

import javax.persistence.criteria.*;
import java.util.*;

@Slf4j
public class JpaDataHandle {

    public static enum Operator{
        EQ,
        LIKE,
        GT,
        LT,
        GTE,
        LTE,
        IN,
        NIN,
        NOTNULL,
        NULL,
        NE,
        SQL,
        SORT,
        AND,
        OR;
        private Operator(){

        }
    }




    public static <T> Specification<T> getSpecification(MySearchList mySearchList){
        Specification<T> specification = new Specification<T>() {
            @Override
            public Predicate toPredicate(Root<T> root, CriteriaQuery<?> criteriaQuery, CriteriaBuilder criteriaBuilder) {
               Map<String,From> nameMap = new HashMap<>();
                Predicate predicate =  analysisPredicate(root,criteriaQuery,criteriaBuilder,mySearchList,true,nameMap);
                Iterator<MySearch> iterator = mySearchList.getOrderSearches().iterator();
                MySearch mySearch = null;
                Path path = null;
                List<Order> orders = new ArrayList<>();
                while (iterator.hasNext()){
                    mySearch = iterator.next();
                    String mySearchFieldName  = mySearch.getFieldName();
                    if(mySearchFieldName.contains(".")){
                        String fieldPaths[] = mySearchFieldName.split("[.]");
                        if(fieldPaths.length < 2){
                            log.error("输入错误的参数:"+mySearchFieldName);
                            continue;
                        }
                        Join join = null;
                        int i = 0;
                        StringBuffer nickName = new StringBuffer();
                        From last = root;
                        for (;i < fieldPaths.length - 1; i++){
                            if(i != 0){
                                nickName.append("_");
                            }
                            nickName.append(fieldPaths[i]);
                            if(nameMap.containsKey(nickName.toString())){
                                last = nameMap.get(nickName.toString());
                            }else {
                                last = last.join(fieldPaths[i]);
                                nameMap.put(nickName.toString(),last);
                            }
                        }
                        path = last.get(fieldPaths[i]);
                    }else {
                        path = root.get(mySearch.getFieldName());
                    }
                    if ("asc".equals(mySearch.getValue()) || "ASC".equals(mySearch.getValue())) {
                        orders.add(criteriaBuilder.asc(path));
                    } else {
                        orders.add(criteriaBuilder.desc(path));
                    }
                }
                if(!orders.isEmpty()){
                    criteriaQuery.orderBy(orders);
                }
                return predicate;
            }
        };
        return specification;
    }


    public static <T> Predicate analysisPredicate(Root<T> root, CriteriaQuery<?> criteriaQuery, CriteriaBuilder criteriaBuilder,MySearchList mySearchList,boolean isAnd,Map<String,From> nameMap){

        if(mySearchList == null || mySearchList.isEmpty()){
            criteriaBuilder.conjunction();
        }else {
            Iterator<MySearch> mySearchIterator = mySearchList.iterator();
            List<Predicate> predicates = new ArrayList<>();
            List<Order> orders = new ArrayList<>();
            Path path = null;
            MySearch mySearch = null;
            while (mySearchIterator.hasNext()) {
                mySearch = mySearchIterator.next();
                try {
                    String mySearchFieldName  = mySearch.getFieldName();
                    if(mySearchFieldName.contains(".")){
                        String fieldPaths[] = mySearchFieldName.split("[.]");
                        if(fieldPaths.length < 2){
                            log.error("输入错误的参数:"+mySearchFieldName);
                            continue;
                        }
                        Join join = null;
                        int i = 0;
                        StringBuffer nickName = new StringBuffer();
                        From last = root;
                        for (;i < fieldPaths.length - 1; i++){
                            if(i != 0){
                                nickName.append("_");
                            }
                            nickName.append(fieldPaths[i]);
                            if(nameMap.containsKey(nickName.toString())){
                                last = nameMap.get(nickName.toString());
                            }else {
                                last = last.join(fieldPaths[i],mySearchList.getDefaultJoinType());
                                nameMap.put(nickName.toString(), last);
                            }

                        }
                        path = last.get(fieldPaths[i]);
                    }else {
                        path = root.get(mySearch.getFieldName());
                    }

                }catch (Exception e){
                    e.printStackTrace();
                    log.error(mySearch.getFieldName()+"不存在");
                    continue;
                }
                if (path != null) {
                    if (!mySearch.getOperator().equals(Operator.SORT)) {
                        Class clazz = path.getJavaType();
                        if (Date.class.isAssignableFrom(clazz) && !mySearch.getValue().getClass().equals(clazz)) {
                            mySearch.setValue(WsDateUtils.stringToDate(WsDateUtils.objectDateFormatString(mySearch.getValue())));
                        }
                    }
                    switch (mySearch.getOperator()) {
                        case EQ:
                            if(mySearch.getValue() == null){
                                break;
                            }
                            predicates.add(criteriaBuilder.equal(path, mySearch.getValue()));
                            break;
                        case LIKE:
                            if(mySearch.getValue() == null){
                                break;
                            }
                            predicates.add(criteriaBuilder.like(path, "%" + mySearch.getValue() + "%"));
                            break;
                        case GT:
                            if(mySearch.getValue() == null){
                                break;
                            }
                            predicates.add(criteriaBuilder.greaterThan(path, (Comparable) mySearch.getValue()));
                            break;
                        case LT:
                            if(mySearch.getValue() == null){
                                break;
                            }
                            predicates.add(criteriaBuilder.lessThan(path, (Comparable) WsBeanUtis.objectToT(mySearch.getValue(),path.getJavaType())));
                            break;
                        case GTE:
                            if(mySearch.getValue() == null){
                                break;
                            }
                            predicates.add(criteriaBuilder.greaterThanOrEqualTo(path, (Comparable) WsBeanUtis.objectToT(mySearch.getValue(),path.getJavaType())));
                            break;
                        case LTE:
                            if(mySearch.getValue() == null){
                                break;
                            }
                            predicates.add(criteriaBuilder.lessThanOrEqualTo(path, (Comparable) WsBeanUtis.objectToT(mySearch.getValue(),path.getJavaType())));
                            break;
                        case IN:
                            if(mySearch.getValue() == null){
                                break;
                            }
                            if (mySearch.getValue() instanceof List) {
                                List list = (List) mySearch.getValue();
                                if(list.isEmpty()){
                                    break;
                                }
                                predicates.add(criteriaBuilder.and(new Predicate[]{path.in(list.toArray())}));
                            } else if (mySearch.getValue() instanceof Set) {
                                Set set = (Set) mySearch.getValue();
                                if(set.isEmpty()){
                                    break;
                                }
                                predicates.add(criteriaBuilder.and(new Predicate[]{path.in(set.toArray())}));
                            } else if (mySearch.getValue().getClass().isArray()) {
                                if(((Object[])mySearch.getValue()).length == 0 ) {
                                    break;
                                }
                                predicates.add(criteriaBuilder.and(new Predicate[]{path.in((Object[]) mySearch.getValue())}));
                            }
                            break;
                        case NIN:
                            if(mySearch.getValue() == null){
                                break;
                            }
                            CriteriaBuilder.In in = criteriaBuilder.in(path);
                            if (mySearch.getValue() instanceof List) {
                                List list = (List) mySearch.getValue();
                                if(list.isEmpty()){
                                    break;
                                }
                                Iterator iterator = list.iterator();
                                Object object = null;
                                Object value = null;
                                while (iterator.hasNext()) {
                                    object = iterator.next();
                                    value = WsBeanUtis.objectToT(object,path.getJavaType());
                                    if(value != null){
                                        in.value(value);
                                    }

                                }
                                predicates.add(criteriaBuilder.not(in));
                            } else if (mySearch.getValue() instanceof Set) {
                                Set set = (Set) mySearch.getValue();
                                if(set.isEmpty()){
                                    break;
                                }
                                Iterator iterator = set.iterator();
                                Object object = null;
                                Object value = null;
                                while (iterator.hasNext()) {
                                    object = iterator.next();
                                    value = WsBeanUtis.objectToT(object,path.getJavaType());
                                    if(value != null){
                                        in.value(value);
                                    }
                                }
                                predicates.add(criteriaBuilder.not(in));
                            } else if (mySearch.getValue().getClass().isArray()) {
                                Object objects[] = (Object[]) mySearch.getValue();
                                if(objects.length == 0){
                                    break;
                                }
                                Object value = null;
                                for (int i = 0; i < objects.length; i++) {
                                    //in.value(RestTempLateUtil.objectToT(objects[i],path.getJavaType()));
                                    value = WsBeanUtis.objectToT(objects[i],path.getJavaType());
                                    if(value != null){
                                        in.value(value);
                                    }
                                }
                                predicates.add(criteriaBuilder.not(in));
                            }

                            break;
                        case NULL:
                            predicates.add(criteriaBuilder.isNull(path));
                            break;
                        case NOTNULL:
                            predicates.add(criteriaBuilder.isNotNull(path));
                            break;
                        case NE:
                            if(mySearch.getValue() == null){
                                break;
                            }
                            predicates.add(criteriaBuilder.notEqual(path, mySearch.getValue()));
                            break;
                        case SORT:
                            if ("asc".equals(mySearch.getValue()) || "ASC".equals(mySearch.getValue())) {
                                orders.add(criteriaBuilder.asc(path));
                            } else {
                                orders.add(criteriaBuilder.desc(path));
                            }
                            break;
                        default:break;
                    }
                }
            }

            List<Predicate> andList = new ArrayList<>();
            List<Predicate> orList = new ArrayList<>();
            List<MySearchList> ands = mySearchList.getAnds();
            for(MySearchList searchList:ands){
                Predicate predicate = analysisPredicate(root, criteriaQuery, criteriaBuilder, searchList, true,nameMap);
                andList.add(predicate);
            }
            List<MySearchList> ors = mySearchList.getOrs();
            for (MySearchList searchList:ors){
                Predicate predicate = analysisPredicate(root, criteriaQuery, criteriaBuilder, searchList, false,nameMap);
                orList.add(predicate);
            }

            if(andList.size() != 0){
                predicates.add(criteriaBuilder.and((Predicate[]) andList.toArray(new Predicate[andList.size()])));
            }
            if(orList.size() != 0){
                predicates.add(criteriaBuilder.or((Predicate[]) orList.toArray(new Predicate[orList.size()])));
            }

            criteriaQuery.orderBy(orders);
            if(predicates.size() > 0){
                /*if(isAnd){
                    return criteriaBuilder.and((Predicate[]) predicates.toArray(new Predicate[predicates.size()]));
                }else {
                    return criteriaBuilder.or((Predicate[]) predicates.toArray(new Predicate[predicates.size()]));
                }*/
                return criteriaBuilder.and((Predicate[]) predicates.toArray(new Predicate[predicates.size()]));
            }else {
                return criteriaBuilder.conjunction();
            }

        }
        return criteriaBuilder.conjunction();






    }





}
