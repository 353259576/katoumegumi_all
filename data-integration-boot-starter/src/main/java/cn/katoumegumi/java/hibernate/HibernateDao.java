package cn.katoumegumi.java.hibernate;

import cn.katoumegumi.java.common.*;
import cn.katoumegumi.java.common.model.WsRun;
import cn.katoumegumi.java.sql.MySearch;
import cn.katoumegumi.java.sql.MySearchList;
import cn.katoumegumi.java.sql.TableRelation;
import cn.katoumegumi.java.sql.common.SqlOperator;
import cn.katoumegumi.java.sql.entity.SqlLimit;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.hibernate.Criteria;
import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.hibernate.criterion.*;
import org.hibernate.proxy.HibernateProxy;
import org.hibernate.query.Query;
import org.hibernate.sql.JoinType;
import org.hibernate.type.Type;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.orm.hibernate5.HibernateTemplate;

import java.lang.reflect.Field;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

@HibernateTransactional
public class HibernateDao {

    public static final Logger log = LoggerFactory.getLogger(HibernateDao.class);
    //private final static String MAIN_TABLE_SIGN = "_1_";
    //@Resource
    private HibernateTemplate hibernateTemplate;

    public static <T> T hibernateObjectConvertor(T object) {
        Field fields[] = WsFieldUtils.getFieldAll(object.getClass());
        Object value = null;
        for (Field field : fields) {
            value = WsFieldUtils.getFieldValueForName(field, object);
            if (value != null) {
                if (value instanceof HibernateProxy) {
                    try {
                        value = ((HibernateProxy) value).getHibernateLazyInitializer().getImplementation();
                    } catch (Exception e) {
                        value = null;
                        e.printStackTrace();
                    }
                    WsFieldUtils.setFieldValueForName(field, object, value);
                }
            }
        }
        return object;
    }

    public HibernateTemplate getHibernateTemplate() {
        return hibernateTemplate;
    }

    public void setHibernateTemplate(HibernateTemplate hibernateTemplate) {
        this.hibernateTemplate = hibernateTemplate;
    }

    public String updateObject(Object bean) throws HibernateException {
        if (bean != null) {
            Object obj = getHibernateTemplate().merge(bean);
            getHibernateTemplate().flush();
            return obj.toString();
        }
        return "";
    }

    public void deleteObject(Object bean) throws HibernateException {
        if (bean != null) {
            getHibernateTemplate().delete(bean);
            getHibernateTemplate().flush();
        }
        return;
    }

    public void deleteObject(String hsql) throws HibernateException {
        Session session = null;
        try {
            session = getHibernateTemplate().getSessionFactory().openSession();
            session.createQuery(hsql).executeUpdate();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            session.close();
        }
        return;
    }

    public String insertObject(Object bean) throws HibernateException {
        if (bean != null) {
            String id = getHibernateTemplate().save(bean).toString();
            getHibernateTemplate().flush();
            return id;
        }

        return "";
    }

    public <T> T insertT(T t) {
        hibernateTemplate.saveOrUpdate(t);
        hibernateTemplate.flush();
        return t;
    }

    public <T> List<T> insertList(List<T> list) {
        if (WsListUtils.isNotEmpty(list)) {
            for (T t : list) {
                hibernateTemplate.saveOrUpdate(t);
            }
            hibernateTemplate.flush();
        }
        return null;
    }

    public <T> T updateT(T t) {
        hibernateTemplate.merge(t);
        return t;
    }

    /**
     * 分页查询
     *
     * @param mySearchList
     * @param clazz
     * @param <T>
     * @return
     */
    public <T> IPage<T> selectValueToPage(MySearchList mySearchList, Class<T> clazz) {
        //DetachedCriteria detachedCriteria = myDetachedCriteriaCreate(clazz, mySearchList);
        mySearchList.setMainClass(clazz);
        DetachedCriteria detachedCriteria = createDetachedCriteria(mySearchList);
        SqlLimit sqlLimit = mySearchList.getSqlLimit();
        if (sqlLimit == null) {
            sqlLimit = new SqlLimit();
        }
        Integer firstResult = Math.toIntExact(sqlLimit.getOffset());
        List<T> list = (List<T>) hibernateTemplate.findByCriteria(detachedCriteria, firstResult, Long.valueOf(sqlLimit.getSize()).intValue());
        Integer count = 0;
        if (list.size() == 0) {
            count = list.size();
        } else {
            detachedCriteria.setProjection(Projections.rowCount());
            List counts = hibernateTemplate.findByCriteria(detachedCriteria, 0, 1);
            count = Integer.parseInt(counts.get(0).toString());
        }
        Page<T> pageVO = new Page<>();
        pageVO.setCurrent(sqlLimit.getCurrent());
        pageVO.setSize(sqlLimit.getSize());
        pageVO.setTotal(count);
        pageVO.setRecords(list);
        return pageVO;
    }

    /**
     * 查询
     *
     * @param mySearchList
     * @param clazz
     * @param <T>
     * @return
     */
    public <T> List<T> selectValueToList(MySearchList mySearchList, Class<T> clazz) {
        //DetachedCriteria detachedCriteria = myDetachedCriteriaCreate(clazz, mySearchList);
        mySearchList.setMainClass(clazz);
        DetachedCriteria detachedCriteria = createDetachedCriteria(mySearchList);
        List<T> list = (List<T>) hibernateTemplate.findByCriteria(detachedCriteria);
        return list;
    }

    /**
     * 查询一条数据
     *
     * @param mySearchList
     * @param tClass
     * @param <T>
     * @return
     */
    public <T> T selectValueOne(MySearchList mySearchList, Class<T> tClass) {
        List<T> tList = selectValueToList(mySearchList, tClass);
        if (WsListUtils.isEmpty(tList)) {
            return null;
        }
        if (tList.size() > 1) {
            log.warn("查询到多条数据，但只显示一条数据");
        }
        return tList.get(0);
    }

    private <T> DetachedCriteria myDetachedCriteriaCreate(Class<T> clazz, MySearchList mySearchList) {
        DetachedCriteria detachedCriteria = DetachedCriteria.forClass(clazz);
        Map<String, DetachedCriteria> map = new HashMap<>();
        Conjunction conjunction = myDetachedCriteriaCreate(mySearchList, clazz, true, detachedCriteria, map);
        detachedCriteria.add(conjunction);
        Iterator<MySearch> iterator = mySearchList.getOrderSearches().iterator();
        MySearch mySearch = null;
        while (iterator.hasNext()) {
            mySearch = iterator.next();
            String fieldName = mySearch.getFieldName();
            Field field = null;

            DetachedCriteria last = detachedCriteria;

            List<String> strings = WsStringUtils.split(fieldName, '.');
            StringBuilder stringBuilder = new StringBuilder();
            Class<?> lastc = clazz;
            for (int i = 0; i < strings.size() - 1; i++) {
                if (i != 0) {
                    stringBuilder.append('.');
                }
                stringBuilder.append(strings.get(i));
                field = WsFieldUtils.getFieldForClass(strings.get(i), lastc);
                if (field == null) {
                    throw new RuntimeException(strings.get(i) + "不存在");
                }
                lastc = WsFieldUtils.getClassTypeof(field);
                if (map.get(stringBuilder.toString()) == null) {
                    last = detachedCriteria.createCriteria(strings.get(i), strings.get(i));
                    map.put(stringBuilder.toString(), last);
                } else {
                    last = map.get(stringBuilder.toString());
                }
            }
            if (stringBuilder.length() > 0) {
                fieldName = stringBuilder.append(".").append(strings.get(strings.size() - 1)).toString();
            }


            if ("asc".equals(mySearch.getValue()) || "ASC".equals(mySearch.getValue())) {
                Order order = Order.asc(fieldName);
                detachedCriteria.addOrder(order);
            } else {
                Order order = Order.desc(fieldName);
                detachedCriteria.addOrder(order);
            }

        }
        return detachedCriteria;
    }

    public Conjunction myDetachedCriteriaCreate(MySearchList mySearchList, Class clazz, boolean isAnd, DetachedCriteria detachedCriteria, Map<String, DetachedCriteria> nameMap) {
        JoinType joinType;
        switch (mySearchList.getDefaultJoinType()) {
            case INNER_JOIN:
                joinType = JoinType.INNER_JOIN;
                break;
            case LEFT_JOIN:
                joinType = JoinType.LEFT_OUTER_JOIN;
                break;
            case RIGHT_JOIN:
                joinType = JoinType.RIGHT_OUTER_JOIN;
                break;
            default:
                joinType = JoinType.INNER_JOIN;
        }
        List<MySearch> mySearches = mySearchList.getAll();
        String fieldName;
        List<Criterion> criterionList = new ArrayList<>();


        for (MySearch mySearch : mySearches) {
            fieldName = mySearch.getFieldName();
            Field field = null;

            DetachedCriteria last = detachedCriteria;

            List<String> strings = WsStringUtils.split(fieldName, '.');
            StringBuilder stringBuilder = new StringBuilder();
            Class<?> lastc = clazz;
            for (int i = 0; i < strings.size() - 1; i++) {
                if (i != 0) {
                    stringBuilder.append('.');
                }
                stringBuilder.append(strings.get(i));
                field = WsFieldUtils.getFieldForClass(strings.get(i), lastc);
                if (field == null) {
                    throw new RuntimeException(strings.get(i) + "不存在");
                }
                lastc = WsFieldUtils.getClassTypeof(field);
                if (nameMap.get(stringBuilder.toString()) == null) {
                    last = detachedCriteria.createCriteria(strings.get(i), strings.get(i), joinType);
                    nameMap.put(stringBuilder.toString(), last);
                } else {
                    last = nameMap.get(stringBuilder.toString());
                }
            }
            if (stringBuilder.length() > 0) {
                fieldName = stringBuilder.append(".").append(strings.get(strings.size() - 1)).toString();
            }
            Class<?> type = WsFieldUtils.getFieldForClass(strings.get(strings.size() - 1), lastc).getType();
            if (!mySearch.getOperator().equals(SqlOperator.SORT)) {
                if (Date.class.isAssignableFrom(type) && !mySearch.getValue().getClass().equals(clazz)) {
                    mySearch.setValue(WsDateUtils.stringToDate(WsDateUtils.objectDateFormatString(mySearch.getValue())));
                }
            }
            switch (mySearch.getOperator()) {
                case IN:
                    if (mySearch.getValue() instanceof Collection) {
                        criterionList.add(Restrictions.in(fieldName, (Collection) mySearch.getValue()));
                    } else {
                        criterionList.add(Restrictions.in(fieldName, (Object[]) mySearch.getValue()));
                    }
                    break;
                case EQ:

                    criterionList.add(Restrictions.eq(fieldName, WsBeanUtils.objectToT(mySearch.getValue(), type)));
                    break;
                case NE:
                    criterionList.add(Restrictions.ne(fieldName, WsBeanUtils.objectToT(mySearch.getValue(), type)));
                    break;
                case LIKE:
                    String value = WsStringUtils.anyToString(mySearch.getValue());
                    if (value != null) {
                        criterionList.add(Restrictions.like(fieldName, value, MatchMode.ANYWHERE));
                    }

                    break;
                case NIN:
                    if (mySearch.getValue() instanceof Collection) {
                        criterionList.add(Restrictions.not(Restrictions.in(fieldName, (Collection) mySearch.getValue())));
                    } else {
                        criterionList.add(Restrictions.not(Restrictions.in(fieldName, (Object[]) mySearch.getValue())));
                    }
                    break;
                case GT:
                    criterionList.add(Restrictions.gt(fieldName, WsBeanUtils.objectToT(mySearch.getValue(), type)));
                    break;
                case GTE:
                    criterionList.add(Restrictions.ge(fieldName, WsBeanUtils.objectToT(mySearch.getValue(), type)));
                    break;
                case LT:
                    criterionList.add(Restrictions.lt(fieldName, WsBeanUtils.objectToT(mySearch.getValue(), type)));
                    break;
                case LTE:
                    criterionList.add(Restrictions.le(fieldName, WsBeanUtils.objectToT(mySearch.getValue(), type)));
                    break;
                case NULL:
                    criterionList.add(Restrictions.isNull(fieldName));
                    break;
                case NOTNULL:
                    criterionList.add(Restrictions.isNotNull(fieldName));
                    break;
                case SQL:
                    String k = WsStringUtils.anyToString(mySearch.getValue());
                    if (k != null) {
                        criterionList.add(Restrictions.sqlRestriction(k));
                    }
                    break;
                case EQP:
                    criterionList.add(Restrictions.eqProperty(fieldName, WsStringUtils.anyToString(mySearch.getValue())));
                    break;
                case GTP:
                    criterionList.add(Restrictions.gtProperty(fieldName, WsStringUtils.anyToString(mySearch.getValue())));
                    break;
                case LTP:
                    criterionList.add(Restrictions.leProperty(fieldName, WsStringUtils.anyToString(mySearch.getValue())));
                    break;
                case GTEP:
                    criterionList.add(Restrictions.gtProperty(fieldName, WsStringUtils.anyToString(mySearch.getValue())));
                    break;
                case LTEP:
                    criterionList.add(Restrictions.leProperty(fieldName, WsStringUtils.anyToString(mySearch.getValue())));
                    break;
                case NEP:
                    criterionList.add(Restrictions.neProperty(fieldName, WsStringUtils.anyToString(mySearch.getValue())));
                    break;
                default:
                    break;

            }
        }

        List<MySearchList> ands = mySearchList.getAnds();
        List<MySearchList> ors = mySearchList.getOrs();
        List<Criterion> andList = new ArrayList<>();
        List<Criterion> orList = new ArrayList<>();
        for (MySearchList searchList : ands) {
            Criterion criterionAnd = myDetachedCriteriaCreate(searchList, clazz, true, detachedCriteria, nameMap);
            andList.add(criterionAnd);
        }

        for (MySearchList searchList : ors) {
            Criterion criterionOr = myDetachedCriteriaCreate(searchList, clazz, false, detachedCriteria, nameMap);
            orList.add(criterionOr);
        }
        if (!andList.isEmpty()) {
            criterionList.add(Restrictions.and(andList.toArray(new Criterion[andList.size()])));
        }

        if (!orList.isEmpty()) {
            criterionList.add(Restrictions.or(orList.toArray(new Criterion[andList.size()])));
        }

        return Restrictions.and(criterionList.toArray(new Criterion[criterionList.size()]));
    }

    public <T> T selectOneByHql(String hql, Map map) {
        List<T> list = hibernateTemplate.execute(session -> {
            Query query = session.createQuery(hql);
            query.setProperties(map);
            query.setMaxResults(1);
            return query.list();
        });
        if (list == null || list.size() == 0) {
            return null;
        }
        return (T) list.get(0);
    }

    public Long selectCountByHql(String hql, Map map) {
        Long num = hibernateTemplate.execute(session -> {
            Query query = session.createQuery(hql);
            query.setProperties(map);
            query.setMaxResults(1);
            return (Long) query.uniqueResult();
        });
        return num;
    }

    public <T> List<T> selectByHql(String hql, Map map) {
        List<T> list = hibernateTemplate.execute(session -> {
            Query query = session.createQuery(hql);
            query.setProperties(map);
            return query.list();
        });
        return list;
    }

    public <T> List<T> selectTbyT(T t) {
        hibernateObjectConvertor(t);
        return hibernateTemplate.findByExample(t);
    }

    public Object update(String hql, Map map) {
		/*hibernateTemplate.execute(new HibernateCallback<T>() {
			@Override
			public T doInHibernate(Session session) throws HibernateException {
				org.hibernate.query.Query<T> query = session.createQuery(hql);
				query.setProperties(map);
				query.executeUpdate();
				return null;
			}
		});*/
        return hibernateTemplate.execute(session -> {
            Query query = session.createQuery(hql);
            query.setProperties(map);
            return query.executeUpdate();
        });
    }


    private DetachedCriteria createDetachedCriteria(MySearchList mySearchList) {
        AtomicInteger atomicInteger = new AtomicInteger();
        Map<String, DetachedCriteria> detachedCriteriaMap = new HashMap<>();
        Map<String, Class<?>> classMap = new HashMap<>();
        Map<String, String> nickAndRealNameMap = new HashMap<>();
        Map<String, String> realAndNickNameMap = new HashMap<>();
        return createDetachedCriteria(mySearchList, atomicInteger, detachedCriteriaMap, classMap, nickAndRealNameMap, realAndNickNameMap);
    }

    private DetachedCriteria createDetachedCriteria(MySearchList mySearchList,
                                                    AtomicInteger atomicInteger,
                                                    Map<String, DetachedCriteria> detachedCriteriaMap,
                                                    Map<String, Class<?>> classMap,
                                                    Map<String, String> nickAndRealNameMap,
                                                    Map<String, String> realAndNickNameMap) {
        String rootAlias = WsStringUtils.isBlank(mySearchList.getAlias()) ? mySearchList.getMainClass().getSimpleName().substring(0, 1).toUpperCase() + "_" + atomicInteger.getAndAdd(1) : mySearchList.getAlias();
        String defaultPrefix = mySearchList.getMainClass().getSimpleName();
        DetachedCriteria detachedCriteria = DetachedCriteria.forClass(mySearchList.getMainClass(), rootAlias);
        nickAndRealNameMap.put(rootAlias, defaultPrefix);
        realAndNickNameMap.put(defaultPrefix, rootAlias);
        detachedCriteriaMap.put(defaultPrefix, detachedCriteria);
        classMap.put(defaultPrefix, mySearchList.getMainClass());

        if (WsListUtils.isNotEmpty(mySearchList.getJoins())) {
            List<WsRun> runList = new ArrayList<>();
            for (TableRelation tableRelation : mySearchList.getJoins()) {
                JoinType joinType;
                switch (tableRelation.getJoinType()) {
                    case INNER_JOIN:
                        joinType = JoinType.INNER_JOIN;
                        break;
                    case LEFT_JOIN:
                        joinType = JoinType.LEFT_OUTER_JOIN;
                        break;
                    case RIGHT_JOIN:
                        joinType = JoinType.RIGHT_OUTER_JOIN;
                        break;
                    default:
                        joinType = JoinType.INNER_JOIN;
                }
                String joinTableNickName = tableRelation.getJoinTableNickName();
                String joinTableName = null;
                List<String> stringList = WsStringUtils.split(joinTableNickName, '.');
                if (stringList.size() > 1) {
                    String startName = stringList.get(0);
                    String startNickName = getRealName(startName, nickAndRealNameMap);
                    if (startName == startNickName) {
                        stringList.set(0, defaultPrefix + '.' + startName);
                    } else {
                        stringList.set(0, startNickName);
                    }
                    joinTableName = WsStringUtils.jointListString(stringList, ".");
                } else {
                    joinTableName = defaultPrefix + '.' + joinTableNickName;
                }
                DetachedCriteria root;
                Class<?> rootClass;
                String rootName = null;
                String nodeName = stringList.get(stringList.size() - 1);
                if (stringList.size() == 1) {
                    root = detachedCriteria;
                    rootClass = mySearchList.getMainClass();
                } else {
                    stringList.remove(stringList.size() - 1);
                    rootName = WsStringUtils.jointListString(stringList, ".");
                    root = detachedCriteriaMap.get(rootName);
                    rootClass = classMap.get(rootName);
                }
                String nodeAlias = WsStringUtils.isBlank(tableRelation.getAlias()) ? tableRelation.getJoinTableNickName().substring(0, 1).toUpperCase() + "_" + atomicInteger.getAndAdd(1) : tableRelation.getAlias();
                DetachedCriteria node = root.createCriteria(nodeName, nodeAlias, joinType);
                realAndNickNameMap.put(joinTableName, nodeAlias);
                nickAndRealNameMap.put(nodeAlias, joinTableName);
                detachedCriteriaMap.put(joinTableName, node);
                classMap.put(joinTableName, WsFieldUtils.getClassTypeof(WsFieldUtils.getFieldByName(rootClass, nodeName)));
                if (tableRelation.getConditionSearchList() != null && tableRelation.getConditionSearchList().getAll().size() > 0) {

                    runList.add(() -> {
                        List<Criterion> criterionList = createDetachedCriteria(tableRelation.getConditionSearchList(), defaultPrefix, detachedCriteriaMap, classMap, nickAndRealNameMap, realAndNickNameMap, atomicInteger);
                        if (WsListUtils.isNotEmpty(criterionList)) {
                            for (Criterion criterion : criterionList) {
                                node.add(criterion);
                            }
                        }
                    });
                }
            }
            if (WsListUtils.isNotEmpty(runList)) {
                for (WsRun run : runList) {
                    run.run();
                }
            }

        }
        List<Criterion> criterionList = createDetachedCriteria(mySearchList, defaultPrefix, detachedCriteriaMap, classMap, nickAndRealNameMap, realAndNickNameMap, atomicInteger);
        if (WsListUtils.isNotEmpty(criterionList)) {
            for (Criterion criterion : criterionList) {
                detachedCriteria.add(criterion);
            }
        }
        if (mySearchList.getColumnNameList().size() > 0) {
            detachedCriteria.setProjection(Property.forName(getFieldName(mySearchList.getColumnNameList().get(0), defaultPrefix, detachedCriteriaMap, classMap, nickAndRealNameMap, realAndNickNameMap, atomicInteger)));
        }
        return detachedCriteria;
    }


    private String getFieldName(String name,
                                final String defaultPrefix,
                                Map<String, DetachedCriteria> detachedCriteriaMap,
                                Map<String, Class<?>> classMap,
                                Map<String, String> nickAndRealNameMap,
                                Map<String, String> realAndNickNameMap,
                                AtomicInteger atomicInteger) {
        String fieldName = null;
        DetachedCriteria local = detachedCriteriaMap.get(defaultPrefix);
        Class<?> localClass = classMap.get(defaultPrefix);
        List<String> stringList = WsStringUtils.split(name, '.');
        if (stringList.size() == 1) {
            fieldName = name;
            classMap.put(defaultPrefix + '.' + fieldName, WsFieldUtils.getClassTypeof(WsFieldUtils.getFieldByName(localClass, fieldName)));
        } else {
            String startName = stringList.get(0);
            String startRealName = getRealName(startName, nickAndRealNameMap);
            if (startRealName == startName) {
                stringList.set(0, defaultPrefix + '.' + startName);
            } else {
                stringList.set(0, startRealName);
            }
            fieldName = WsStringUtils.jointListString(stringList, ".");

            String shortFieldName = stringList.get(stringList.size() - 1);
            stringList.remove(stringList.size() - 1);
            List<String> strings = new ArrayList<>();
            for (String str : stringList) {
                strings.add(str);
                String nodeName = WsStringUtils.jointListString(strings, ".");
                if (!detachedCriteriaMap.containsKey(nodeName)) {
                    localClass = WsFieldUtils.getClassTypeof(WsFieldUtils.getFieldByName(localClass, str));
                    String alias = localClass.getSimpleName().substring(0, 1).toUpperCase() + "_" + atomicInteger.getAndAdd(1);
                    local = local.createCriteria(str, alias);
                    detachedCriteriaMap.put(nodeName, local);
                    classMap.put(nodeName, localClass);
                    nickAndRealNameMap.put(alias, nodeName);
                    realAndNickNameMap.put(nodeName, alias);

                } else {
                    local = detachedCriteriaMap.get(nodeName);
                    localClass = classMap.get(nodeName);
                }
            }
            classMap.put(fieldName, WsFieldUtils.getClassTypeof(WsFieldUtils.getFieldByName(localClass, shortFieldName)));
            fieldName = getNickName(WsStringUtils.jointListString(stringList, "."), realAndNickNameMap) + '.' + shortFieldName;
        }
        return fieldName;
    }

    private List<Criterion> createDetachedCriteria(MySearchList mySearchList,
                                                   final String defaultPrefix,
                                                   Map<String, DetachedCriteria> detachedCriteriaMap,
                                                   Map<String, Class<?>> classMap,
                                                   Map<String, String> nickAndRealNameMap,
                                                   Map<String, String> realAndNickNameMap,
                                                   AtomicInteger atomicInteger) {
        List<MySearch> searchList = mySearchList.getAll();
        List<Criterion> criterionList = new ArrayList<>();
        for (MySearch search : searchList) {
            if (search.getOperator().equals(SqlOperator.SQL)) {
                criterionList.add(Restrictions.sqlRestriction(search.getFieldName()));
                continue;
            }

            String fieldName = getFieldName(search.getFieldName(), defaultPrefix, detachedCriteriaMap, classMap, nickAndRealNameMap, realAndNickNameMap, atomicInteger);
            Class<?> type = classMap.get(fieldName);

            switch (search.getOperator()) {
                case EQ:
                    criterionList.add(Restrictions.eq(fieldName, WsBeanUtils.objectToT(search.getValue(), type)));
                    break;
                case NE:
                    criterionList.add(Restrictions.ne(fieldName, WsBeanUtils.objectToT(search.getValue(), type)));
                    break;
                case GT:
                    criterionList.add(Restrictions.gt(fieldName, WsBeanUtils.objectToT(search.getValue(), type)));
                    break;
                case GTE:
                    criterionList.add(Restrictions.ge(fieldName, WsBeanUtils.objectToT(search.getValue(), type)));
                    break;
                case LT:
                    criterionList.add(Restrictions.lt(fieldName, WsBeanUtils.objectToT(search.getValue(), type)));
                    break;
                case LTE:
                    criterionList.add(Restrictions.le(fieldName, WsBeanUtils.objectToT(search.getValue(), type)));
                    break;
                case LIKE:
                    MatchMode matchMode;
                    boolean start;
                    boolean end;
                    String likeValue = WsStringUtils.anyToString(search.getValue());
                    end = likeValue.startsWith("%");
                    start = likeValue.endsWith("%");
                    if (start == end) {
                        matchMode = MatchMode.ANYWHERE;
                    } else {
                        if (start) {
                            matchMode = MatchMode.START;
                        } else {
                            matchMode = MatchMode.END;
                        }
                    }
                    criterionList.add(Restrictions.like(fieldName, likeValue, matchMode));
                    break;
                case NULL:
                    criterionList.add(Restrictions.isNull(fieldName));
                    break;
                case NOTNULL:
                    criterionList.add(Restrictions.isNotNull(fieldName));
                    break;
                case BETWEEN:
                    if (WsBeanUtils.isArray(search.getValue().getClass())) {
                        if (search.getValue().getClass().isArray()) {
                            Object[] objects = (Object[]) search.getValue();
                            criterionList.add(Restrictions.between(fieldName, WsBeanUtils.objectToT(objects[0], type), WsBeanUtils.objectToT(objects[1], type)));
                        } else {
                            Collection<?> collection = (Collection<?>) search.getValue();
                            Iterator<?> iterator = collection.iterator();
                            criterionList.add(Restrictions.between(fieldName, WsBeanUtils.objectToT(iterator.next(), type), WsBeanUtils.objectToT(iterator.next(), type)));
                        }
                    } else {
                        throw new RuntimeException("数据格式错误，当前需要数组类型,当前：" + search.getValue().getClass());
                    }
                    break;
                case IN:
                    if (WsBeanUtils.isArray(search.getValue().getClass())) {
                        if (search.getValue().getClass().isArray()) {
                            Object[] objects = (Object[]) search.getValue();
                            for (int i = 0; i < objects.length; i++) {
                                objects[i] = WsBeanUtils.objectToT(objects[i], type);
                            }
                            criterionList.add(Restrictions.in(fieldName, objects));
                        } else {
                            Collection<?> collection = (Collection<?>) search.getValue();
                            List<Object> list = new ArrayList<>(collection.size());
                            for (Object o : collection) {
                                list.add(WsBeanUtils.objectToT(o, type));
                            }
                            criterionList.add(Restrictions.in(fieldName, list));
                        }
                    } else if (search.getValue() instanceof MySearchList) {
                        criterionList.add(Property
                                .forName(fieldName)
                                .in(createDetachedCriteria((MySearchList) search.getValue(), atomicInteger, detachedCriteriaMap, classMap, nickAndRealNameMap, realAndNickNameMap)));
                    } else {
                        throw new RuntimeException("数据格式错误，当前需要数组类型,当前：" + search.getValue().getClass());
                    }
                    break;
                case NIN:
                    if (WsBeanUtils.isArray(search.getValue().getClass())) {
                        if (search.getValue().getClass().isArray()) {
                            Object[] objects = (Object[]) search.getValue();
                            for (int i = 0; i < objects.length; i++) {
                                objects[i] = WsBeanUtils.objectToT(objects[i], type);
                            }
                            criterionList.add(Restrictions.not(Restrictions.in(fieldName, objects)));
                        } else {
                            Collection<?> collection = (Collection<?>) search.getValue();
                            List<Object> list = new ArrayList<>(collection.size());
                            for (Object o : collection) {
                                list.add(WsBeanUtils.objectToT(o, type));
                            }
                            criterionList.add(Restrictions.not(Restrictions.in(fieldName, collection)));
                        }
                    } else if (search.getValue() instanceof MySearchList) {
                        criterionList.add(Property
                                .forName(fieldName)
                                .notIn(createDetachedCriteria((MySearchList) search.getValue(), atomicInteger, detachedCriteriaMap, classMap, nickAndRealNameMap, realAndNickNameMap)));
                    } else {
                        throw new RuntimeException("数据格式错误，当前需要数组类型,当前：" + search.getValue().getClass());
                    }
                    break;
                case EQP:
                    criterionList.add(Restrictions.eqProperty(fieldName, getFieldName((String) search.getValue(), defaultPrefix, detachedCriteriaMap, classMap, nickAndRealNameMap, realAndNickNameMap, atomicInteger)));
                    break;
                case GTP:
                    criterionList.add(Restrictions.gtProperty(fieldName, getFieldName((String) search.getValue(), defaultPrefix, detachedCriteriaMap, classMap, nickAndRealNameMap, realAndNickNameMap, atomicInteger)));
                    break;
                case GTEP:
                    criterionList.add(Restrictions.geProperty(fieldName, getFieldName((String) search.getValue(), defaultPrefix, detachedCriteriaMap, classMap, nickAndRealNameMap, realAndNickNameMap, atomicInteger)));
                    break;
                case LTP:
                    criterionList.add(Restrictions.ltProperty(fieldName, getFieldName((String) search.getValue(), defaultPrefix, detachedCriteriaMap, classMap, nickAndRealNameMap, realAndNickNameMap, atomicInteger)));
                    break;
                case LTEP:
                    criterionList.add(Restrictions.leProperty(fieldName, getFieldName((String) search.getValue(), defaultPrefix, detachedCriteriaMap, classMap, nickAndRealNameMap, realAndNickNameMap, atomicInteger)));
                    break;
                case NEP:
                    criterionList.add(Restrictions.neProperty(fieldName, getFieldName((String) search.getValue(), defaultPrefix, detachedCriteriaMap, classMap, nickAndRealNameMap, realAndNickNameMap, atomicInteger)));
                    break;
                case SORT:
                    String order = (String) search.getValue();
                    if (order.equalsIgnoreCase("asc")) {
                        detachedCriteriaMap.get(defaultPrefix).addOrder(Order.asc(fieldName));
                    } else {
                        detachedCriteriaMap.get(defaultPrefix).addOrder(Order.desc(fieldName));
                    }
                    break;
                default:
                    throw new RuntimeException("暂不支持");
            }


        }
        List<MySearchList> andList = mySearchList.getAnds();
        List<MySearchList> orList = mySearchList.getOrs();
        if (WsListUtils.isNotEmpty(andList)) {
            List<Criterion> andCriterionList = new ArrayList<>();
            for (MySearchList and : andList) {
                List<Criterion> list = createDetachedCriteria(and, defaultPrefix, detachedCriteriaMap, classMap, nickAndRealNameMap, realAndNickNameMap, atomicInteger);
                if (WsListUtils.isNotEmpty(list)) {
                    andCriterionList.addAll(list);
                }

            }
            if (WsListUtils.isNotEmpty(andCriterionList)) {
                criterionList.add(Restrictions.and(andCriterionList.toArray(new Criterion[0])));
            }
        }
        if (WsListUtils.isNotEmpty(orList)) {
            List<Criterion> orCriterionList = new ArrayList<>();
            for (MySearchList or : orList) {
                List<Criterion> list = createDetachedCriteria(or, defaultPrefix, detachedCriteriaMap, classMap, nickAndRealNameMap, realAndNickNameMap, atomicInteger);
                if (WsListUtils.isNotEmpty(list)) {
                    orCriterionList.addAll(list);
                }

            }
            if (WsListUtils.isNotEmpty(orCriterionList)) {
                criterionList.add(Restrictions.or(orCriterionList.toArray(new Criterion[0])));
            }
        }


        return criterionList;
    }


    private String getRealName(String nickName, Map<String, String> nickAndRealNameMap) {
        String realName = nickAndRealNameMap.get(nickName);
        if (realName == null) {
            realName = nickName;
        }
        return realName;
    }

    private String getNickName(String realName, Map<String, String> realAndNickNameMap) {
        String nickName = realAndNickNameMap.get(realName);
        if (nickName == null) {
            nickName = realName;
        }
        return nickName;
    }




}

