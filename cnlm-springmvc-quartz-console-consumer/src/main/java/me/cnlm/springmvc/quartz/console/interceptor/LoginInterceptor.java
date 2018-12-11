package me.cnlm.springmvc.quartz.console.interceptor;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * 接口请求公共拦截器
 *
 * @author liwei
 * @version V1.0
 */
public class LoginInterceptor extends HandlerInterceptorAdapter {
    /**
     * 日志
     */
    private static Logger logger = LoggerFactory.getLogger(LoginInterceptor.class);

    @SuppressWarnings("SpringJavaAutowiringInspection")

    /**
     * 是否是开发模式
     */
    private boolean isDev;

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
    public boolean preHandle(HttpServletRequest request,
                             HttpServletResponse response,
                             Object handler) throws Exception {

        printRequestInfo(request);

        if (isDev == true) {
            return true;
        }
        //从Header获取oauth认证信息
        String oauthInfo = request.getHeader("Authorization");
        logger.info("authInfo:" + oauthInfo);
        if (oauthInfo == null) {
            response.setStatus(401);
            return false;
        }
        Map<String, String> hashMap = new HashMap<String, String>();

        oauthInfo = oauthInfo.replaceAll("\\\"", "");
        String[] oauth = oauthInfo.split(",");


        //验证参数长度
        if (oauth.length < 7) {
            response.setStatus(401);
            return false;
        }

        //将参数转换为map
        for (String oa : oauth) {
            String oas[] = oa.split("=");
            hashMap.put(oas[0], oas[1]);
        }

        //将各参数按照名称排序
        java.util.Arrays.sort(oauth);

        //提取并封装参数
        String baseString = oauth[1] + "&" + oauth[2]
                + "&" + oauth[4] + "&" + oauth[5] + "&" + oauth[6];
        
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


    public void printRequestInfo(HttpServletRequest request) {
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
            logger.info("preHandle info,method=" + method + ",Ip=" + request.getRemoteAddr() + ",headers" + request.getHeaderNames() + ",URI="
                    + request.getRequestURL() + "?" + rmString.substring(0, rmString.length() - 1));
        } else {
            logger.info("preHandle info,method=" + method + ",Ip=" + request.getRemoteAddr() + ",headers=" + request.getHeaderNames().toString() + ",URI="
                    + request.getRequestURL());
        }
    }
}
