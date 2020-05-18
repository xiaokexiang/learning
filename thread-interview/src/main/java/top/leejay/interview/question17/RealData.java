package top.leejay.interview.question17;

/**
 * @author xiaokexiang
 * @date 3/28/2020
 */
public class RealData implements Data {
    private final String content;

    public RealData(int count, char c) {
        System.out.println("make realData begin ...");
        char[] buffer = new char[count];
        for (int i = 0; i < count; i++) {
            buffer[i] = c;
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        System.out.println("make realData end ...");
        this.content = new String(buffer);
    }

    @Override
    public String getContent() {
        return content;
    }
}
