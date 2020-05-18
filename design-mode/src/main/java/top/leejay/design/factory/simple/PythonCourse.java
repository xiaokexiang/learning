package top.leejay.design.factory.simple;

/**
 * @author Jie Li
 * @date 2019/10/29
 * 
 */
public class PythonCourse implements ICourse {
    @Override
    public void record() {
        System.out.println("录制python视频");
    }
}
