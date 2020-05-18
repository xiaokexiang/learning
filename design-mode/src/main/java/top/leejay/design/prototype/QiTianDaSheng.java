package top.leejay.design.prototype;

import java.io.*;
import java.util.Date;

/**
 * @author xiaokexiang
 * @date 11/5/2019
 * 
 */
public class QiTianDaSheng extends Monkey implements Cloneable, Serializable {
    public JinGuBang jinGuBang;

    public QiTianDaSheng() {
        this.birthday = new Date();
        this.jinGuBang = new JinGuBang();
    }

    @Override
    protected Object clone() {
        return this.deepClone();
    }

    public Object deepClone() {
        try {
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            ObjectOutputStream os = new ObjectOutputStream(outputStream);
            // 将this 写入到ByteArrayOutputStream
            os.writeObject(this);

            ByteArrayInputStream inputStream = new ByteArrayInputStream(outputStream.toByteArray());
            ObjectInputStream ois = new ObjectInputStream(inputStream);
            // 从ByteArrayInputStream读取
            QiTianDaSheng object = (QiTianDaSheng) ois.readObject();
            object.birthday = new Date();
            return object;
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage());
        }
    }

    public QiTianDaSheng shallowClone(QiTianDaSheng target) {

        QiTianDaSheng qiTianDaSheng = new QiTianDaSheng();
        qiTianDaSheng.height = target.height;
        qiTianDaSheng.weight = target.weight;
        qiTianDaSheng.jinGuBang = target.jinGuBang;
        qiTianDaSheng.birthday = new Date();
        return qiTianDaSheng;
    }
}
