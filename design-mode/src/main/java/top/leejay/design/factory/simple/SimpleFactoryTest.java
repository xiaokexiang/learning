package top.leejay.design.factory.simple;

import java.util.Objects;

/**
 * @author Jie Li
 * @date 2019/10/29
 * 
 */
public class SimpleFactoryTest {
    public static void main(String[] args) {
        // 父类的引用指向子类的实现
        ICourse iCourse = CourseFactory.create(JavaCourse.class);
        Objects.requireNonNull(iCourse);
        iCourse.record();
    }
}
