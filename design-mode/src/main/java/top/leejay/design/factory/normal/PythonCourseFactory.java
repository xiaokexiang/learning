package top.leejay.design.factory.normal;

/**
 * @author xiaokexiang
 * @date 10/30/2019
 * 
 */
public class PythonCourseFactory implements ICourseFactory {
    @Override
    public ICourse create() {
        return new PythonCourse();
    }
}
