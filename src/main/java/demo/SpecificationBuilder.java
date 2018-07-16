package demo;

import org.apache.commons.collections.CollectionUtils;
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
import java.util.function.Consumer;

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

    public static <T> Specification buildSpecification(T condition) {
        Class<?> conditionClass = condition.getClass();
        List<Field> declaredFields = ReflectionUtil.getFields(conditionClass);
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>(declaredFields.size());
            Nullable.of(declaredFields).ifPresent(fields -> fields.stream().filter(checkFiledAccess()).forEach(fieldParse(root, predicates, cb, condition)));
            return cb.and(predicates.toArray(new Predicate[predicates.size()]));
        };
    }

    private static java.util.function.Predicate<Field> checkFiledAccess() {
        return field -> !(field.isAnnotationPresent(Page.class) || field.isAnnotationPresent(Ignore.class) || SERIAL_VERSION_UID.equals(field.getName()));
    }

    private static <T> Consumer<Field> fieldParse(Root root, List<Predicate> predicates, CriteriaBuilder cb, T condition) {
        return field -> {
            try {
                field.setAccessible(true);
                //优先从Name注解上取自定义名称
                String fieldName = field.isAnnotationPresent(Name.class) ? field.getAnnotation(Name.class).value() : field.getName();
                Nullable.of(field.get(condition)).ifPresent(fieldObject -> appendPredicate(root, predicates, field, fieldName, fieldObject, cb));
            } catch (IllegalAccessException e) {
                LOGGER.error("SpecificationBuilder buildSpecification fieldParse error", e);
            }
        };
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

