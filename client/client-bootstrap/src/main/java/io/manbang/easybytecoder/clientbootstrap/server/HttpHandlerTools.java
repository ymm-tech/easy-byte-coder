package io.manbang.easybytecoder.clientbootstrap.server;

import com.sun.net.httpserver.Headers;
import com.sun.net.httpserver.HttpExchange;
import io.manbang.easybytecoder.clientbootstrap.server.model.AgentInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.lang.instrument.Instrumentation;
import java.lang.instrument.UnmodifiableClassException;
import java.util.List;
import java.util.Map;


/**
 * @author xujie
 */
public class HttpHandlerTools implements com.sun.net.httpserver.HttpHandler {

    private static Logger logger = LoggerFactory.getLogger(HttpHandlerTools.class);

    private List<AgentInfo> agentInfos;
    private Instrumentation inst;
    private String commandUninstall = "uninstall";

    public HttpHandlerTools(Instrumentation inst, List<AgentInfo> agentInfos) {
        this.agentInfos = agentInfos;
        this.inst = inst;
    }

    @Override
    public void handle(HttpExchange httpExchange) {
        try {
            StringBuilder responseText = new StringBuilder();
            String param = getRequestParam(httpExchange);

            if (commandUninstall.equals(param)) {
                uninstallTransformer();
            }
            handleResponse(httpExchange, responseText.toString());
        } catch (Exception ex) {
            logger.error("headler fail error", ex);
        }
    }


    private void uninstallTransformer() {
        for (AgentInfo agentInfo : agentInfos) {
            inst.removeTransformer(agentInfo.getClassFileTransformer());
            for (Class<?> clazz : agentInfo.getClazzs()) {
                try {
                    inst.retransformClasses(clazz);
                } catch (UnmodifiableClassException e) {
                    logger.error("uninstall Transformer  faill error ", e);
                }
                logger.info("Remove Instrumentation  className:{}", clazz.getSimpleName());
            }
        }
    }

    /**
     * 获取请求头
     *
     * @param httpExchange
     * @return
     */
    private String getRequestHeader(HttpExchange httpExchange) {
        Headers headers = httpExchange.getRequestHeaders();

        StringBuilder builder = new StringBuilder();
        for (Map.Entry<String, List<String>> entry : headers.entrySet()) {
            builder.append(entry.getKey());
            builder.append(":");
            builder.append(entry.getValue().toString());
            builder.append("<br/>");
        }
        //java8
        // return headers.entrySet().stream().map((Map.Entry<String, List<String>> entry) -> entry.getKey() + ":" + entry.getValue().toString()).collect(Collectors.joining("<br/>"));
        return builder.toString();
    }

    /**
     * 获取请求参数
     *
     * @param httpExchange
     * @return
     * @throws Exception
     */
    private String getRequestParam(HttpExchange httpExchange) throws Exception {
        String paramStr = "";

        if (httpExchange.getRequestMethod().equals("GET")) {
            //GET请求读queryString
            paramStr = httpExchange.getRequestURI().getQuery();
        } else {
            //非GET请求读请求体
            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(httpExchange.getRequestBody(), "utf-8"));
            StringBuilder requestBodyContent = new StringBuilder();
            String line = null;
            while ((line = bufferedReader.readLine()) != null) {
                requestBodyContent.append(line);
            }
            paramStr = requestBodyContent.toString();
        }

        return paramStr;
    }

    /**
     * 处理响应
     *
     * @param httpExchange
     * @param responsetext
     * @throws Exception
     */
    private void handleResponse(HttpExchange httpExchange, String responsetext) throws Exception {
        //生成html
        StringBuilder responseContent = new StringBuilder();
        responseContent.append("<html>")
                .append("<body>")
                .append(responsetext)
                .append("</body>")
                .append("</html>");
        String responseContentStr = responseContent.toString();
        byte[] responseContentByte = responseContentStr.getBytes("utf-8");

        //设置响应头，必须在sendResponseHeaders方法之前设置！
        httpExchange.getResponseHeaders().add("Content-Type:", "text/html;charset=utf-8");

        //设置响应码和响应体长度，必须在getResponseBody方法之前调用！
        httpExchange.sendResponseHeaders(200, responseContentByte.length);

        OutputStream out = httpExchange.getResponseBody();
        out.write(responseContentByte);
        out.flush();
        out.close();
    }
}
