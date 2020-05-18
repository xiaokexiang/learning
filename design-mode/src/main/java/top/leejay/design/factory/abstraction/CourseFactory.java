package top.leejay.design.factory.abstraction;

/**
 * @author xiaokexiang
 * @date 10/30/2019
 * 
 */
public interface CourseFactory {

    IVideo createVideo();

    INote createNote();
}
