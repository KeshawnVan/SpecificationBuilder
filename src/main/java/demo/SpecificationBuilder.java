package demo;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.jpa.domain.Specification;
import sign.*;

import javax.persistence.criteria.CriteriaBuilder;
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

    private SpecificationBuilder() {
    }

    public static <T> Specification buildSpecification(T condition) {
        Class<?> conditionClass = condition.getClass();
        Field[] declaredFields = conditionClass.getDeclaredFields();
        Specification specification = (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            if (ArrayUtils.isNotEmpty(declaredFields)) {
                fieldParse(root, predicates, cb, condition, declaredFields);
            }
            return cb.and(predicates.toArray(new Predicate[predicates.size()]));
        };
        return specification;
    }

    private static <T> void fieldParse(Root root, List<Predicate> predicates, CriteriaBuilder cb, T condition, Field[] declaredFields) {
        for (Field field : declaredFields) {
            try {
                if (field.isAnnotationPresent(Page.class)) {
                    continue;
                }
                field.setAccessible(true);
                //优先从Name注解上取自定义名称
                String fieldName = field.isAnnotationPresent(Name.class) ? field.getAnnotation(Name.class).value() : field.getName();
                Object fieldObject = field.get(condition);
                if (fieldObject != null) {
                    appendPredicate(root, predicates, field, fieldName, fieldObject, cb);
                }
            } catch (IllegalAccessException e) {
                LOGGER.error("demo.SpecificationBuilder buildSpecification fieldParse error", e);
            }
        }
    }

    private static void appendPredicate(Root root, List<Predicate> predicates, Field field, String fieldName, Object fieldObject, CriteriaBuilder cb) {
        //字段如果为集合类型，做In操作
        if (Collection.class.isAssignableFrom(field.getType())) {
            Collection collectionObject = (Collection) fieldObject;
            if (CollectionUtils.isNotEmpty(collectionObject)) {
                predicates.add(root.get(fieldName).in(collectionObject));
            }
        } else {
            if (field.isAnnotationPresent(Like.class)) {
                predicates.add(cb.like(root.get(fieldName), PERCENT + fieldObject + PERCENT));
            } else if (field.isAnnotationPresent(GreaterThan.class)) {
                predicates.add(cb.greaterThan(root.get(fieldName), (Comparable) fieldObject));
            } else if (field.isAnnotationPresent(GreaterThanEqual.class)) {
                predicates.add(cb.greaterThanOrEqualTo(root.get(fieldName), (Comparable) fieldObject));
            } else if (field.isAnnotationPresent(LessThan.class)) {
                predicates.add(cb.lessThan(root.get(fieldName), (Comparable) fieldObject));
            } else if (field.isAnnotationPresent(LessThanEqual.class)) {
                predicates.add(cb.lessThanOrEqualTo(root.get(fieldName), (Comparable) fieldObject));
            } else if (field.isAnnotationPresent(Ignore.class)) {
                return;
            } else {
                predicates.add(cb.equal(root.get(fieldName), fieldObject));
            }
        }
    }
}
