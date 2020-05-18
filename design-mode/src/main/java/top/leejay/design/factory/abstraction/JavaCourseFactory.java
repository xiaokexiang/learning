package top.leejay.design.factory.abstraction;

/**
 * @author xiaokexiang
 * @date 10/30/2019
 * 
 */
public class JavaCourseFactory implements CourseFactory {
    @Override
    public IVideo createVideo() {
        return new JavaVideo();
    }

    @Override
    public INote createNote() {
        return new JavaNote();
    }
}
