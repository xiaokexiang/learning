package top.leejay.design.observer.jdk;

import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * @author xiaokexiang
 * @date 11/11/2019
 * 模拟 学生提出的问题
 */
@Data
@AllArgsConstructor
public class Question {
    private String username;
    private String content;
}
