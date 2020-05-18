package top.leejay.interview.question14;

import java.io.IOException;

/**
 * @author xiaokexiang
 * @date 3/28/2020
 */
public class Main {
    public static void main(String[] args) {
        try {
            new MiniServer(8080).execute();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
