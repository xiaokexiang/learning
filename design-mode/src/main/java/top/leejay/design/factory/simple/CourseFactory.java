package top.leejay.design.factory.simple;

/**
 * @author Jie Li
 * @date 2019/10/29
 * 简单工厂模式(非23种设计模式中的一种) 缺点: 工厂职责过重 不易于扩展复杂的结构
 */
public class CourseFactory {
    public static ICourse create(Class<? extends ICourse> clazz) {
        try {
            if (null != clazz) {
                return clazz.newInstance();
            }
        } catch (InstantiationException | IllegalAccessException e) {
            throw new Error(e);
        }
        return null;
    }
}
