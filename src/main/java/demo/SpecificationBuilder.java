package demo;

import org.apache.commons.collections.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.jpa.domain.Specification;
import sign.*;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * @author keshawn
 * @date 2018/1/2
 */
public final class SpecificationBuilder {

    private static final Logger LOGGER = LoggerFactory.getLogger(SpecificationBuilder.class);

    private static final String PERCENT = "%";

    private static final String SERIAL_VERSION_UID = "serialVersionUID";

    private SpecificationBuilder() {
    }

    public static <T> Specification buildSpecification(final T condition) {
        Class<?> conditionClass = condition.getClass();
        final List<Field> declaredFields = ReflectionUtil.getFields(conditionClass);
        return new Specification<T>() {
            @Override
            public Predicate toPredicate(Root<T> root, CriteriaQuery<?> criteriaQuery, CriteriaBuilder criteriaBuilder) {
                List<Predicate> predicates = new ArrayList<>(declaredFields.size());
                if (CollectionUtils.isNotEmpty(declaredFields)) {
                    for (Field field : declaredFields) {
                        if (checkFiledAccess(field)) {
                            parseField(root, criteriaBuilder, predicates, field);
                        }
                    }
                }
                return criteriaBuilder.and(predicates.toArray(new Predicate[predicates.size()]));
            }

            private void parseField(Root<T> root, CriteriaBuilder criteriaBuilder, List<Predicate> predicates, Field field) {
                try {
                    field.setAccessible(true);
                    //优先从Name注解上取自定义名称
                    String fieldName = field.isAnnotationPresent(Name.class) ? field.getAnnotation(Name.class).value() : field.getName();
                    Object fieldObject = field.get(condition);
                    if (fieldObject != null) {
                        appendPredicate(root, predicates, field, fieldName, fieldObject, criteriaBuilder);
                    }
                } catch (IllegalAccessException e) {
                    LOGGER.error("SpecificationBuilder buildSpecification fieldParse error", e);
                }
            }
        };
    }

    private static Boolean checkFiledAccess(Field field) {
        return !(field.isAnnotationPresent(Page.class) || field.isAnnotationPresent(Ignore.class) || SERIAL_VERSION_UID.equals(field.getName()));
    }


    private static void appendPredicate(Root root, List<Predicate> predicates, Field field, String fieldName, Object fieldObject, CriteriaBuilder cb) {
        //字段如果为集合类型，做In操作
        if (Collection.class.isAssignableFrom(field.getType())) {
            Collection collectionObject = (Collection) fieldObject;
            if (CollectionUtils.isNotEmpty(collectionObject)) {
                if (field.isAnnotationPresent(Join.class)) {
                    Join join = field.getAnnotation(Join.class);
                    predicates.add(root.get(join.value()).get(fieldName).in(collectionObject));
                } else {
                    predicates.add(root.get(fieldName).in(collectionObject));
                }
            }
        } else {
            if (field.isAnnotationPresent(Like.class)) {
                likeHandler(root, predicates, field, fieldName, fieldObject, cb);
            } else if (field.isAnnotationPresent(GreaterThan.class)) {
                predicates.add(cb.greaterThan(root.get(fieldName), (Comparable) fieldObject));
            } else if (field.isAnnotationPresent(GreaterThanEqual.class)) {
                predicates.add(cb.greaterThanOrEqualTo(root.get(fieldName), (Comparable) fieldObject));
            } else if (field.isAnnotationPresent(LessThan.class)) {
                predicates.add(cb.lessThan(root.get(fieldName), (Comparable) fieldObject));
            } else if (field.isAnnotationPresent(LessThanEqual.class)) {
                predicates.add(cb.lessThanOrEqualTo(root.get(fieldName), (Comparable) fieldObject));
            } else {
                if (field.isAnnotationPresent(Join.class)) {
                    predicates.add(cb.equal(root.get(field.getAnnotation(Join.class).value()).get(fieldName), fieldObject));
                } else {
                    predicates.add(cb.equal(root.get(fieldName), fieldObject));
                }
            }
        }
    }

    private static void likeHandler(Root root, List<Predicate> predicates, Field field, String fieldName, Object fieldObject, CriteriaBuilder cb) {
        Like like = field.getAnnotation(Like.class);
        String location = like.location();
        if (Like.AROUND.equals(location)) {
            predicates.add(cb.like(root.get(fieldName), PERCENT + fieldObject + PERCENT));
        }
        if (Like.LEFT.equals(location)) {
            predicates.add(cb.like(root.get(fieldName), PERCENT + fieldObject));
        }
        if (Like.RIGHT.equals(location)) {
            predicates.add(cb.like(root.get(fieldName), fieldObject + PERCENT));
        }
    }
}

