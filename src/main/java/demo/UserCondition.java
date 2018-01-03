package demo;

import sign.GreaterThanEqual;
import sign.LessThanEqual;
import sign.Like;
import sign.Name;

import java.time.LocalDateTime;
import java.util.List;

/**
 * @author keshawn
 * @date 2018/1/3
 */
public class UserCondition {

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

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Integer getAge() {
        return age;
    }

    public void setAge(Integer age) {
        this.age = age;
    }

    public List<String> getSchools() {
        return schools;
    }

    public void setSchools(List<String> schools) {
        this.schools = schools;
    }

    public LocalDateTime getBirthdayStart() {
        return birthdayStart;
    }

    public void setBirthdayStart(LocalDateTime birthdayStart) {
        this.birthdayStart = birthdayStart;
    }

    public LocalDateTime getBirthdayEnd() {
        return birthdayEnd;
    }

    public void setBirthdayEnd(LocalDateTime birthdayEnd) {
        this.birthdayEnd = birthdayEnd;
    }
}
