package demo;

import org.apache.commons.collections.CollectionUtils;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.lang.Nullable;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import java.util.ArrayList;
import java.util.List;

/**
 * @author keshawn
 * @date 2018/1/3
 */
public class BuildSpecification {
    public static void main(String[] args) {

        //1.假设接收到一个Condition
        UserCondition userCondition = new UserCondition();

        //2.之前构造Specification是这样的
        Specification specification = new Specification<User>() {
            @Nullable
            @Override
            public Predicate toPredicate(Root root, CriteriaQuery criteriaQuery, CriteriaBuilder criteriaBuilder) {
                List<Predicate> predicates = new ArrayList<>();
                if (userCondition.getId() != null) {
                    predicates.add(criteriaBuilder.equal(root.get("id"), userCondition.getId()));
                }
                if (userCondition.getAge() != null) {
                    predicates.add(criteriaBuilder.equal(root.get("age"), userCondition.getAge()));
                }
                if (userCondition.getName() != null) {
                    predicates.add(criteriaBuilder.like(root.get("name"), "%" + userCondition.getName() + "%"));
                }
                if (userCondition.getBirthdayStart() != null) {
                    predicates.add(criteriaBuilder.greaterThanOrEqualTo(root.get("birthday"), userCondition.getBirthdayStart()));
                }
                if (userCondition.getBirthdayEnd() != null) {
                    predicates.add(criteriaBuilder.lessThanOrEqualTo(root.get("birthday"), userCondition.getBirthdayEnd()));
                }
                if (CollectionUtils.isNotEmpty(userCondition.getSchools())) {
                    predicates.add(root.get("school").in(userCondition.getSchools()));
                }
                return criteriaBuilder.and(predicates.toArray(new Predicate[predicates.size()]));
            }
        };

        //仅仅五个字段的动态SQL，就写25行代码，大多数都是样板代码
        //3.而使用SpecificationBuilder,仅需一行
        Specification buildSpecification = SpecificationBuilder.buildSpecification(userCondition);
    }
}
