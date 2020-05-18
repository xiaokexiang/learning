package top.leejay.design.factory.normal;

/**
 * @author xiaokexiang
 * @date 10/30/2019
 * 工厂方法模式适用于 1.创建对象需要大量的代码 2.应用层不关注于实例如何创建,如何实现 3.一个类通过子类来指定创建哪个对象
 */
public class NormalFactoryTest {
    public static void main(String[] args) {
        // 生成Java实例工厂
        JavaCourseFactory javaCourseFactory = new JavaCourseFactory();
        // Java实例工厂创建JavaCourse实例
        ICourse iCourse = javaCourseFactory.create();
        // 调用JavaCourse record()
        iCourse.record();
    }
}
