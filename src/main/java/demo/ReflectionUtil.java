package demo;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sun.reflect.generics.reflectiveObjects.ParameterizedTypeImpl;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.stream.Collectors;

/**
 * @author keshawn
 * @date 2017/11/9
 */
public final class ReflectionUtil {

    private static final Logger LOGGER = LoggerFactory.getLogger(ReflectionUtil.class);

    private ReflectionUtil() {
    }

    public static <T> T newInstance(Class<T> cls) {
        Object instance;
        try {
            instance = cls.newInstance();
        } catch (Exception e) {
            LOGGER.error("new instance failure", e);
            throw new RuntimeException(e);
        }
        return (T) instance;
    }

    public static Object invokeMethod(Object object, Method method, Object... args) {
        Object result;
        try {
            method.setAccessible(true);
            result = method.invoke(object, args);
        } catch (Exception e) {
            LOGGER.error("invoke method failure", e);
            throw new RuntimeException(e);
        }
        return result;
    }

    public static void setField(Object object, Field field, Object value) {
        try {
            field.setAccessible(true);
            field.set(object, value);
        } catch (Exception e) {
            LOGGER.error("set field failure", e);
            throw new RuntimeException(e);
        }
    }

    public static Object getField(Field field, Object object) {
        field.setAccessible(true);
        try {
            return field.get(object);
        } catch (IllegalAccessException e) {
            LOGGER.error("get field failure", e);
            throw new RuntimeException(e);
        }
    }

    public static List<Field> getFields(Class clazz) {
        List<Field> result = new ArrayList<>(Arrays.asList(clazz.getDeclaredFields()));
        Optional.ofNullable(clazz.getSuperclass()).ifPresent(addSuperclassFields(result));
        return result;
    }

    private static Consumer<Class> addSuperclassFields(List<Field> result) {
        return superClass -> {
            List<Field> superclassFields = getFields(superClass);
            List<String> resultNames = result.stream().map(Field::getName).collect(Collectors.toList());
            List<Field> validSuperclassFields = superclassFields.stream().filter(superclassField -> !resultNames.contains(superclassField.getName())).collect(Collectors.toList());
            result.addAll(validSuperclassFields);
        };
    }

    public static Type[] getActualTypeArguments(Type type) {
        return ((ParameterizedType) type).getActualTypeArguments();
    }

    /**
     * ParameterizedType类型的Type获取外层类型
     * 如：传入List<User>，返回List
     *
     * @param type
     * @return
     */
    public static Class<?> getRawClass(Type type) {
        return ((ParameterizedTypeImpl) type).getRawType();
    }


    /**
     * 检查是否是基本类型或其包装类型
     *
     * @param type
     * @return
     */
    public static boolean isPrimitive(Class<?> type) {
        return type.isPrimitive()
                || type.equals(Integer.class)
                || type.equals(String.class)
                || type.equals(Long.class)
                || type.equals(Double.class)
                || type.equals(Byte.class)
                || type.equals(Short.class)
                || type.equals(Float.class)
                || type.equals(Character.class);
    }
}
