package top.leejay.interview.question8;

import java.io.FileWriter;
import java.io.IOException;

/**
 * @author xiaokexiang
 * @date 3/24/2020
 * client change data
 * server save data
 * 此线程设计模式叫：balking 与 Guarded Suspension 不同点在于balking发现条件不满足就直接返回，而Guarded Suspension会等待
 */
@SuppressWarnings("all")
public class Data {
    // filename
    private final String filename;

    // the content of file
    private String content;

    // isChanged
    private boolean changed;

    public Data(String filename, String content, boolean changed) {
        this.filename = filename;
        this.content = content;
        this.changed = changed;
    }

    // 修改内容
    public synchronized void change(String newContent) {
        content = newContent;
        changed = true;
    }

    // 保存内容到文件
    public synchronized void save() throws IOException {
        // 如果文件没有修改就不保存
        if (!changed) {
            return;
        }
        doSave();
        changed = false;
    }

    private void doSave() throws IOException {
        System.out.println(Thread.currentThread().getName() + " call save(), content: " + content);
        FileWriter fileWriter = new FileWriter(filename, true);
        fileWriter.write(content);
        fileWriter.close();
    }
}
