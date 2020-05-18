package top.leejay.design.factory.abstraction;

/**
 * @author xiaokexiang
 * @date 10/30/2019
 * 
 */
public class PythonCourseFactory implements CourseFactory {
    @Override
    public IVideo createVideo() {
        return new PythonVideo();
    }

    @Override
    public INote createNote() {
        return new PythonNote();
    }
}
