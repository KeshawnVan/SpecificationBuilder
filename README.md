# SpecificationBuilder
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

        //仅仅五个字段的动态SQL，就写25行代码，大多数都是样板代码,实际使用中一个condition往往大于十个字段
        //3.而使用SpecificationBuilder,仅需一行
        Specification buildSpecification = SpecificationBuilder.buildSpecification(userCondition);
        使用时在Condition的字段上加注解进行必要的说明即可，如UserCondition
        
       private Long id;

       @Like   
       private String name;

       private Integer age;

       @Name("school") 
       private List<String> schools;

       @Name("birthday")
       @GreaterThanEqual 
       private LocalDateTime birthdayStart;

       @Name("birthday")
       @LessThanEqual  
       private LocalDateTime birthdayEnd;
    
    * condition中字段名与domain中的字段不一致需使用@Name显式指明对应的domain中的字段
    * 其中集合类型默认做in操作，单数默认做equal操作
    * @Like模糊查询
    * 大于等于使用@GreaterThanEqual标明，大于使用@GreaterThan
    * 小于等于使用@LessThanEqual标明，小于使用@LessThan
    * 分页相关的字段使用@Page，不参与构建Specification
    * 使用@Ignore可忽略字段

        
