package top.leejay.design.delegate.advance;

import lombok.Data;

import java.lang.reflect.Method;

/**
 * @author xiaokexiang
 * @date 11/7/2019
 * 包裹controller
 */
@Data
public class Handler {
    private String url;
    private Object controller;
    private Method method;

    public Handler(String url, Object controller, Method method) {
        this.url = url;
        this.controller = controller;
        this.method = method;
    }
}
