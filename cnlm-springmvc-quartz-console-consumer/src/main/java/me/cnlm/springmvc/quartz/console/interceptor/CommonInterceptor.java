package me.cnlm.springmvc.quartz.console.interceptor;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.InputStream;
import java.util.Collection;
import java.util.Iterator;
import java.util.Map;

/**
 * 接口请求公共拦截器
 *
 * @author liwei
 * @version V1.0
 */
public class CommonInterceptor extends HandlerInterceptorAdapter {
    /**
     * 日志
     */
    private static Logger logger = LoggerFactory.getLogger(CommonInterceptor.class);

    @SuppressWarnings("SpringJavaAutowiringInspection")

    /**
     * 是否是开发模式
     */
    private boolean isDev;
    private Long userId;
    private String employeeNo;

    public boolean getIsDev() {
        return isDev;
    }

    public void setIsDev(boolean isDev) {
        this.isDev = isDev;
    }

    /**
     * 前置处理：参数封装，鉴权
     */
    @SuppressWarnings("unchecked")
    @Override
    public boolean preHandle(
            HttpServletRequest request,
            HttpServletResponse response,
            Object handler) throws Exception {
        if (isDev) {
            printRequestInfo(request, employeeNo);
            return true;
        }

        //从Header获取Token
        //更新token过期时间 取消
//        employeeService.updateExpiredTime(token);
        return true;
    }

    /**
     * 后置处理
     */
    @Override
    public void afterCompletion(HttpServletRequest request,
                                HttpServletResponse response, Object handler, Exception ex)
            throws Exception {
        // 暂无操作
    }


    public void printRequestInfo(HttpServletRequest request, String employeeNo) throws IOException {
        String method = request.getMethod();
        if (method.equals("GET") && request.getParameterMap().size() != 0) {
            String rmString = "";
            Map rMap = request.getParameterMap();
            Collection rKeys = rMap.keySet();
            for (Iterator iterator = rKeys.iterator(); iterator.hasNext(); ) {
                Object key = iterator.next();
                Object[] v = (String[]) rMap.get(key);
                String itm = key + "=" + StringUtils.join(v, ",") + "&";
                rmString += itm;
            }
            //employeeNo
            logger.info("preHandle info,employeeNo=[{}],method=[{}],Ip=,URI=[{}]?[{}]",
                    employeeNo, method, request.getRemoteAddr(),
                    request.getRequestURL(),
                    rmString.substring(0, rmString.length() - 1));
        } /*else if (method.equals("POST") || method.equals("PUT")) {
            String res = "";
            try {
                request.setCharacterEncoding("UTF-8");
                int size = request.getContentLength();
                if (size > 0) {
                    InputStream is = request.getInputStream();
                    byte[] reqBodyBytes = readBytes(is, size);
                    res = new String(reqBodyBytes);
                }
                logger.info("preHandle info,employeeNo=[{}],method=[{}],Ip=[{}],URI=[{}],body={}",
                        employeeNo,method, request.getRemoteAddr(),
                        request.getRequestURL(), res);

            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }

        }*/ else {
            logger.info("preHandle info,employeeNo=[{}],method=[{}],Ip=[{}],URI=[{}]",
                    employeeNo, method, request.getRemoteAddr(),
                    request.getRequestURL());
        }
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public void setEmployeeNo(String employeeNo) {
        this.employeeNo = employeeNo;
    }

    public static final byte[] readBytes(InputStream is, int contentLen) {
        if (contentLen > 0) {
            int readLen = 0;

            int readLengthThisTime = 0;

            byte[] message = new byte[contentLen];

            try {

                while (readLen != contentLen) {

                    readLengthThisTime = is.read(message, readLen, contentLen
                            - readLen);

                    if (readLengthThisTime == -1) {// Should not happen.
                        break;
                    }

                    readLen += readLengthThisTime;
                }

                return message;
            } catch (IOException e) {
                // Ignore
                // e.printStackTrace();
            }
        }

        return new byte[]{};
    }
}
