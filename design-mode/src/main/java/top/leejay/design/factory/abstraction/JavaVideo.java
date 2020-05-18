package top.leejay.design.factory.abstraction;

/**
 * @author xiaokexiang
 * @date 10/30/2019
 * 
 */
public class JavaVideo implements IVideo {
    @Override
    public void record() {
        System.out.println("录制JAVA视频");
    }
}
