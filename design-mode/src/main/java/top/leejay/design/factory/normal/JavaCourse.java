package top.leejay.design.factory.normal;

/**
 * @author Jie Li
 * @date 2019/10/29
 * 
 */
public class JavaCourse implements ICourse {
    @Override
    public void record() {
        System.out.println("录制JAVA视频");
    }
}
