package cn.katoumegumi.java.hibernate;

import cn.katoumegumi.java.common.*;
import cn.katoumegumi.java.sql.MySearch;
import cn.katoumegumi.java.sql.MySearchList;
import cn.katoumegumi.java.sql.SqlOperator;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.hibernate.criterion.*;
import org.hibernate.proxy.HibernateProxy;
import org.hibernate.query.Query;
import org.hibernate.sql.JoinType;
import org.springframework.orm.hibernate5.HibernateTemplate;

import java.lang.reflect.Field;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@HibernateTransactional
public class HibernateDao {

	//@Resource
	private HibernateTemplate hibernateTemplate;

	private Map<Class<?>,Map<String,Class<?>>> classAndFieldClassNameMap = new ConcurrentHashMap<>();

	
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

	public <T> T insertT(T t){
		hibernateTemplate.saveOrUpdate(t);
		hibernateTemplate.flush();
		return t;
	}

	public <T> List<T> insertList(List<T> list){
		if(WsListUtils.isNotEmpty(list)){
			for(T t:list){
				hibernateTemplate.saveOrUpdate(t);
			}
			hibernateTemplate.flush();
		}
		return null;
	}

	public <T> T updateT(T t){
		hibernateTemplate.merge(t);
		return t;
	}

	public <T> IPage<T> selectValueToPage(MySearchList mySearchList, Class<T> clazz) {
		/*MySearch mySearch = null;
		String fieldName = null;*/
        //Set<String> stringSet = new HashSet<>();


        DetachedCriteria detachedCriteria = myDetachedCriteriaCreate(clazz,mySearchList);
        Page pageVO = mySearchList.getPageVO();
        if (pageVO == null) {
            pageVO = new Page();
        }
        Integer firstResult = Integer.parseInt(Long.valueOf((pageVO.getCurrent() - 1) * pageVO.getSize()).toString());
        List<T> list = (List<T>) hibernateTemplate.findByCriteria(detachedCriteria, firstResult, Integer.parseInt(Long.valueOf(pageVO.getSize()).toString()));
        Integer count = Integer.valueOf(0);
        if (list.size() == 0) {
            count = list.size();
        } else {
            detachedCriteria.setProjection(Projections.rowCount());
            List counts = hibernateTemplate.findByCriteria(detachedCriteria,0,1);
			count = Integer.parseInt(counts.get(0).toString());
		}

        pageVO.setTotal(count);
        pageVO.setRecords(list);
        return pageVO;
    }

    public <T> List<T> selectValueToList(MySearchList mySearchList,Class<T> clazz){

		/*MySearch mySearch = null;
		String fieldName = null;*/
        //Set<String> stringSet = new HashSet<>();
        DetachedCriteria detachedCriteria = myDetachedCriteriaCreate(clazz,mySearchList);

        List<T> list = (List<T>) hibernateTemplate.findByCriteria(detachedCriteria);
        return list;
    }

	private <T> DetachedCriteria myDetachedCriteriaCreate(Class<T> clazz, MySearchList mySearchList) {



        DetachedCriteria detachedCriteria = DetachedCriteria.forClass(clazz);
        Map<String,DetachedCriteria> map = new HashMap<>();
        Conjunction conjunction = myDetachedCriteriaCreate(mySearchList,clazz,true,detachedCriteria,map);
        detachedCriteria.add(conjunction);
        Iterator<MySearch> iterator = mySearchList.getOrderSearches().iterator();
		MySearch mySearch = null;
		while (iterator.hasNext()){
			/*mySearch = iterator.next();
			String fieldName = mySearch.getFieldName();
			if(fieldName.contains(".")){
				String strings[] = fieldName.split("[.]");
				StringBuffer nickName = new StringBuffer();
				DetachedCriteria last = detachedCriteria;
				DetachedCriteria criteria = null;
				for(int k = 0; k < strings.length -1; k++){
					if(k != 0){
						nickName.append("_");
					}
					nickName.append(strings[k]);
					if(!map.containsKey(nickName.toString())){
						criteria = last.createCriteria(strings[k],nickName.toString());
						map.put(nickName.toString(),criteria);
					}else {
						criteria = map.get(nickName.toString());
					}
					last = criteria;
				}
				fieldName = nickName.append(".").append(strings[strings.length - 1]).toString();
			}*/

			mySearch = iterator.next();
			String fieldName = mySearch.getFieldName();
			Field field = null;

			DetachedCriteria last = detachedCriteria;

			List<String> strings = WsStringUtils.split(fieldName,'.');
			StringBuilder stringBuilder = new StringBuilder();
			Class<?> lastc = clazz;
			for(int i = 0; i < strings.size() - 1; i++){
				if(i != 0){
					stringBuilder.append('.');
				}
				stringBuilder.append(strings.get(i));
				field = WsFieldUtils.getFieldForClass(strings.get(i),lastc);
				if(field == null){
					throw new RuntimeException(strings.get(i)+"不存在");
				}
				lastc = WsFieldUtils.getClassListType(field);
				if(map.get(stringBuilder.toString())== null){
					last = detachedCriteria.createCriteria(strings.get(i),strings.get(i));
					map.put(stringBuilder.toString(),last);
				}else {
					last = map.get(stringBuilder.toString());
				}
			}
			if(stringBuilder.length() > 0) {
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


	public Conjunction myDetachedCriteriaCreate(MySearchList mySearchList,Class clazz,boolean isAnd,DetachedCriteria detachedCriteria,Map<String,DetachedCriteria> nameMap){
		JoinType joinType;
		switch (mySearchList.getDefaultJoinType()){
			case INNER:joinType = JoinType.INNER_JOIN;break;
			case LEFT:joinType = JoinType.LEFT_OUTER_JOIN;break;
			case RIGHT:joinType = JoinType.RIGHT_OUTER_JOIN;break;
			default:joinType = JoinType.INNER_JOIN;
		}


		List<MySearch> mySearches = mySearchList.getAll();
		//MySearch mySearch;
		String fieldName;
		List<Criterion> criterionList = new ArrayList<>();



		for(MySearch mySearch:mySearches) {
			//mySearch = mySearches.get(i);
			fieldName = mySearch.getFieldName();
			Field field = null;

			DetachedCriteria last = detachedCriteria;

			List<String> strings = WsStringUtils.split(fieldName,'.');
			StringBuilder stringBuilder = new StringBuilder();
			Class<?> lastc = clazz;
			for(int i = 0; i < strings.size() - 1; i++){
				if(i != 0){
					stringBuilder.append('.');
				}
				stringBuilder.append(strings.get(i));
				field = WsFieldUtils.getFieldForClass(strings.get(i),lastc);
				if(field == null){
					throw new RuntimeException(strings.get(i)+"不存在");
				}
				lastc = WsFieldUtils.getClassListType(field);
				if(nameMap.get(stringBuilder.toString())== null){
					last = detachedCriteria.createCriteria(strings.get(i),strings.get(i),joinType);
					nameMap.put(stringBuilder.toString(),last);
				}else {
					last = nameMap.get(stringBuilder.toString());
				}
			}
			if(stringBuilder.length() > 0){
				fieldName = stringBuilder.append(".").append(strings.get(strings.size() - 1)).toString();
			}
			Class<?> type = WsFieldUtils.getFieldForClass(strings.get(strings.size() - 1),lastc).getType();




			/*if(fieldName.contains(".")){
				List<String> strings = WsStringUtils.split(fieldName,'.');
				Field ffield = WsFieldUtils.getFieldForClass(strings.get(0),clazz);
				if(ffield == null){
					continue;
				}else {
					Field lfield = WsFieldUtils.getFieldForClass(strings.get(1),ffield.getType());
					if(lfield == null) {
						continue;
					}
					field = lfield;
				}
				StringBuffer nickName = new StringBuffer();
				DetachedCriteria last = detachedCriteria;
				DetachedCriteria criteria = null;
				for(int k = 0; k < strings.size() -1; k++){
					if(k != 0){
						nickName.append("_");
					}
					nickName.append(strings.get(k));
					if(!nameMap.containsKey(nickName.toString())){
						criteria = last.createCriteria(strings.get(k),nickName.toString(),joinType);
						nameMap.put(nickName.toString(),criteria);
					}else {
						criteria = nameMap.get(nickName.toString());
					}
					last = criteria;
				}
				fieldName = nickName.append(".").append(strings.get(strings.size() - 1)).toString();
			}else {
				field = WsFieldUtils.getFieldForClass(fieldName, clazz);
				if (field == null) {
					continue;
				}
			}*/



			if(!mySearch.getOperator().equals(SqlOperator.SORT)){
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

					criterionList.add(Restrictions.eq(fieldName, WsBeanUtis.objectToT(mySearch.getValue(),type)));
					break;
				case NE:
					criterionList.add(Restrictions.ne(fieldName, WsBeanUtis.objectToT(mySearch.getValue(),type)));
					break;
				case LIKE:
					String value = WsStringUtils.anyToString(mySearch.getValue());
					if(value != null){
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
					criterionList.add(Restrictions.gt(fieldName, WsBeanUtis.objectToT(mySearch.getValue(),type)));
					break;
				case GTE:
					criterionList.add(Restrictions.ge(fieldName, WsBeanUtis.objectToT(mySearch.getValue(),type)));
					break;
				case LT:
					criterionList.add(Restrictions.lt(fieldName, WsBeanUtis.objectToT(mySearch.getValue(),type)));
					break;
				case LTE:
					criterionList.add(Restrictions.le(fieldName, WsBeanUtis.objectToT(mySearch.getValue(),type)));
					break;
				case NULL:
					criterionList.add(Restrictions.isNull(fieldName));
					break;
				case NOTNULL:
					criterionList.add(Restrictions.isNotNull(fieldName));
					break;
				case SQL:
					String k = WsStringUtils.anyToString(mySearch.getValue());
					if(k != null){
						criterionList.add(Restrictions.sqlRestriction(k));
					}
					break;
				case EQP:
					criterionList.add(Restrictions.eqProperty(fieldName,WsStringUtils.anyToString(mySearch.getValue())));
					break;
				case GTP:
					criterionList.add(Restrictions.gtProperty(fieldName,WsStringUtils.anyToString(mySearch.getValue())));
					break;
				case LTP:
					criterionList.add(Restrictions.leProperty(fieldName,WsStringUtils.anyToString(mySearch.getValue())));
					break;
				case GTEP:
					criterionList.add(Restrictions.gtProperty(fieldName,WsStringUtils.anyToString(mySearch.getValue())));
					break;
				case LTEP:
					criterionList.add(Restrictions.leProperty(fieldName,WsStringUtils.anyToString(mySearch.getValue())));
					break;
				case NEP:
					criterionList.add(Restrictions.neProperty(fieldName,WsStringUtils.anyToString(mySearch.getValue())));
					break;
				default:
					break;

			}
		}

		List<MySearchList> ands = mySearchList.getAnds();
		List<MySearchList> ors = mySearchList.getOrs();
		List<Criterion> andList = new ArrayList<>();
		List<Criterion> orList = new ArrayList<>();
		for(MySearchList searchList:ands){
			Criterion criterionAnd = myDetachedCriteriaCreate(searchList,clazz,true,detachedCriteria,nameMap);
			andList.add(criterionAnd);
		}

		for(MySearchList searchList:ors){
			Criterion criterionOr = myDetachedCriteriaCreate(searchList,clazz,false,detachedCriteria,nameMap);
			orList.add(criterionOr);
		}
		if(!andList.isEmpty()){
			criterionList.add(Restrictions.and(andList.toArray(new Criterion[andList.size()])));
		}

		if(!orList.isEmpty()){
			criterionList.add(Restrictions.or(orList.toArray(new Criterion[andList.size()])));
		}

		return Restrictions.and(criterionList.toArray(new Criterion[criterionList.size()]));
	}



	public <T> T selectOneByHql(String hql,Map map){
        List<T> list = hibernateTemplate.execute(session -> {
            Query query = session.createQuery(hql);
            query.setProperties(map);
            query.setMaxResults(1);
            return query.list();
        });
        if(list == null || list.size() == 0){
            return null;
        }
        return (T)list.get(0);
	}


	public Long selectCountByHql(String hql,Map map){
		Long num = hibernateTemplate.execute(session -> {
			Query query = session.createQuery(hql);
			query.setProperties(map);
			query.setMaxResults(1);
			return (Long) query.uniqueResult();
		});
		return num;
	}


	public <T> List<T> selectByHql(String hql,Map map){
		List<T> list = hibernateTemplate.execute(session -> {
			Query query = session.createQuery(hql);
			query.setProperties(map);
			return query.list();
		});
		return list;
	}


	public static  <T> T hibernateObjectConvertor(T object){
		Field fields[] = WsFieldUtils.getFieldAll(object.getClass());
		Object value = null;
		for(Field field:fields){
			value = WsFieldUtils.getFieldValueForName(field,object);
			if(value != null){
				if(value instanceof HibernateProxy){
					try {
						value = ((HibernateProxy)value).getHibernateLazyInitializer().getImplementation();
					}catch (Exception e) {
                        value = null;
						e.printStackTrace();
					}
					WsFieldUtils.setFieldValueForName(field,object,value);
				}
			}
		}
		return object;
	}


	public <T> List<T> selectTbyT(T t) {
		hibernateObjectConvertor(t);
		return hibernateTemplate.findByExample(t);
	}


	public Object update(String hql,Map map){
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
}

