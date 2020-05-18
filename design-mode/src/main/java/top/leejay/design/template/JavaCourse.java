package top.leejay.design.template;

/**
 * @author xiaokexiang
 * @date 11/7/2019
 * 
 */
public class JavaCourse extends AbstractNetworkCourse {
    @Override
    void checkHomework() {
        System.out.println("检查Java作业");
    }

    @Override
    protected boolean needHomework() {
        return true;
    }
}
