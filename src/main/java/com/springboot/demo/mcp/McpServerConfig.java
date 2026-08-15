package com.springboot.demo.mcp;

import com.ajaxjs.mcp.server.McpServer;
import com.ajaxjs.mcp.server.ServerSse;
import com.ajaxjs.mcp.server.common.ServerConfig;
import com.ajaxjs.mcp.server.feature.FeatureMgr;
import com.ajaxjs.mcp.server.feature.model.ServerStoreTool;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.stereotype.Component;

import java.io.*;
import java.net.InetSocketAddress;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

@Component
public class McpServerConfig implements ApplicationListener<ContextRefreshedEvent> {

    private static final Logger log = LoggerFactory.getLogger(McpServerConfig.class);
    private static final int MCP_PORT = 8988;

    @Autowired
    private SchemaMcpService schemaMcpService;

    @Override
    public void onApplicationEvent(ContextRefreshedEvent event) {
        try {
            startMcpServer();
        } catch (Exception e) {
            log.error("MCP Server 启动失败", e);
        }
    }

    private void startMcpServer() throws Exception {
        // 1. 扫描 @McpService 注解，填充 FeatureMgr 的工具存储
        FeatureMgr mgr = new FeatureMgr();
        mgr.init("com.springboot.demo.mcp");

        // 2. 将 TOOL_STORE 中的实例替换为 Spring 托管的 bean，确保依赖注入生效
        for (Map.Entry<String, ServerStoreTool> entry : FeatureMgr.TOOL_STORE.entrySet()) {
            entry.getValue().setInstance(schemaMcpService);
            log.info("MCP 工具注册: {}", entry.getKey());
        }

        // 3. 创建并配置 McpServer
        McpServer server = new McpServer();
        ServerConfig config = new ServerConfig();
        config.setName("springboot-demo-mcp");
        config.setVersion("1.0");
        server.setServerConfig(config);

        // 4. 创建 SSE 传输层
        ServerSse sse = new ServerSse(server);
        server.setTransport(sse);

        // 5. 启动 HTTP 服务器（SSE 端点 + 消息端点）
        HttpServer httpServer = HttpServer.create(new InetSocketAddress(MCP_PORT), 0);
        httpServer.setExecutor(Executors.newCachedThreadPool());

        // SSE 连接端点：GET /sse
        httpServer.createContext("/sse", exchange -> {
            if (!"GET".equals(exchange.getRequestMethod())) {
                exchange.sendResponseHeaders(405, -1);
                return;
            }
            handleSseConnection(exchange, sse);
        });

        // MCP 消息端点：POST /mcp/message
        httpServer.createContext("/mcp/message", exchange -> {
            if (!"POST".equals(exchange.getRequestMethod())) {
                exchange.sendResponseHeaders(405, -1);
                return;
            }
            handleMessage(exchange, sse);
        });

        httpServer.start();
        log.info("MCP Server (SSE) 已启动，端口: {}", MCP_PORT);
        log.info("SSE 端点: http://localhost:{}/sse", MCP_PORT);
        log.info("消息端点: http://localhost:{}/mcp/message", MCP_PORT);
    }

    private void handleSseConnection(HttpExchange exchange, ServerSse sse) throws IOException {
        exchange.getResponseHeaders().add("Content-Type", "text/event-stream");
        exchange.getResponseHeaders().add("Cache-Control", "no-cache");
        exchange.getResponseHeaders().add("Connection", "keep-alive");
        exchange.getResponseHeaders().add("Access-Control-Allow-Origin", "*");
        exchange.sendResponseHeaders(200, 0);

        String sessionId = UUID.randomUUID().toString();
        PrintWriter writer = new PrintWriter(new OutputStreamWriter(
                exchange.getResponseBody(), StandardCharsets.UTF_8));

        sse.addConnections(sessionId, writer);

        // 发送 endpoint 事件，告知客户端消息发送地址
        writer.write("event: endpoint\n");
        writer.write("data: /mcp/message?sessionId=" + sessionId + "\n\n");
        writer.flush();

        log.info("MCP SSE 客户端已连接: session={}", sessionId);

        // 保持连接，等待客户端断开
        try {
            InputStream is = exchange.getRequestBody();
            byte[] buf = new byte[1024];
            while (is.read(buf) != -1) {
                // 持续读取直到客户端断开
            }
        } catch (IOException e) {
            // 客户端断开连接
        } finally {
            sse.removeConnection(sessionId);
            log.info("MCP SSE 客户端已断开: session={}", sessionId);
        }
    }

    private void handleMessage(HttpExchange exchange, ServerSse sse) {
        try {
            // 读取请求体
            String body = new BufferedReader(new InputStreamReader(
                    exchange.getRequestBody(), StandardCharsets.UTF_8))
                    .lines().collect(Collectors.joining("\n"));

            log.debug("MCP 消息接收: {}", body);

            // 由 ServerSse 处理消息（内部委托给 McpServer.processMessage）
            String response = sse.handle(body);

            // 返回响应
            exchange.getResponseHeaders().add("Content-Type", "application/json");
            exchange.getResponseHeaders().add("Access-Control-Allow-Origin", "*");
            byte[] respBytes = response.getBytes(StandardCharsets.UTF_8);
            exchange.sendResponseHeaders(200, respBytes.length);
            exchange.getResponseBody().write(respBytes);
            exchange.getResponseBody().close();
        } catch (Exception e) {
            log.error("MCP 消息处理失败", e);
            try {
                String errorResp = "{\"jsonrpc\":\"2.0\",\"error\":{\"code\":-32603,\"message\":\"Internal error: " + e.getMessage() + "\"}}";
                byte[] respBytes = errorResp.getBytes(StandardCharsets.UTF_8);
                exchange.sendResponseHeaders(500, respBytes.length);
                exchange.getResponseBody().write(respBytes);
                exchange.getResponseBody().close();
            } catch (IOException ex) {
                log.error("发送错误响应失败", ex);
            }
        }
    }
}
