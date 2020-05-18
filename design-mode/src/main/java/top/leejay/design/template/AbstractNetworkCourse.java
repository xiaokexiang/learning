package top.leejay.design.template;

/**
 * @author xiaokexiang
 * @date 11/7/2019
 * 模板方法的抽象类
 */
public abstract class AbstractNetworkCourse {
    protected final void createCourse() {
        this.postPreResource();
        this.createPpt();
        this.liveVideo();
        this.postNote();
        this.postSource();
        if (needHomework()) {
            checkHomework();
        }
    }

    /**
     * 钩子方法 主要目的是干预流程 子类可实现
     */
    protected boolean needHomework() {
        return false;
    }

    /**
     * 模板方法 由子类实现
     */
    abstract void checkHomework();

    final void postSource() {
        System.out.println("提交源代码");
    }

    final void postNote() {
        System.out.println("提交课件和笔记");
    }

    final void liveVideo() {
        System.out.println("直播授课");
    }

    final void createPpt() {
        System.out.println("提交ppt");
    }

    final void postPreResource() {
        System.out.println("发布预习资料");
    }
}
