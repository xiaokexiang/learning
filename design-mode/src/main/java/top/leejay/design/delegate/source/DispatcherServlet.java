package top.leejay.design.delegate.source;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * @author xiaokexiang
 * @date 11/7/2019
 * 委派 相当于Leader的角色
 */
public class DispatcherServlet extends HttpServlet {

    private void doDispatch(HttpServletRequest request, HttpServletResponse response) throws IOException {
        String requestURI = request.getRequestURI();
        String mid = request.getParameter("mid");
        if ("getMemberId".equals(requestURI)) {
            new MemberController().getMemberById(mid);
        } else if ("getOrderId".equals(requestURI)) {
            new OrderController().getOrderById(mid);
        } else if ("logOut".equals(requestURI)) {
            new SystemController().logout();
        } else {
            response.getWriter().write("404 NOT FOUND");
        }

    }

    @Override
    protected void service(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        doDispatch(req, resp);
    }
}
