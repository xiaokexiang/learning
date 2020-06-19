package top.leejay.design.delegate.advance;

import com.google.common.collect.Lists;
import top.leejay.design.delegate.source.MemberController;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;

/**
 * @author xiaokexiang
 * @date 11/7/2019
 * 优化版委派模式 结合 策略模式
 */
public class DispatcherServletDelegate extends HttpServlet {
    private static List<Handler> handlerMapping = Lists.newArrayList();

    @Override
    public void init() throws ServletException {
        Class<?> clazz = MemberController.class;
        try {
            handlerMapping.add(new Handler("/getMemberId", clazz.newInstance(), clazz.getMethod("getMemberId", new Class[]{String.class})));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void doDispatcher(HttpServletRequest request, HttpServletResponse response) {
        String requestUri = request.getRequestURI();
        Handler handler = null;

        for (Handler h : handlerMapping) {
            if (requestUri.equals(h.getUrl())) {
                handler = h;
                break;
            }
        }
        /*调用相应的方法*/
        Object object = null;
        try {
            object = handler.getMethod().invoke(handler.getController(), request.getParameter("mid"));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void service(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        doDispatcher(req, resp);
    }
}
