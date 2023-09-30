package com.wzy.gateway.config;

import com.alibaba.csp.sentinel.adapter.gateway.sc.callback.BlockRequestHandler;
import com.alibaba.csp.sentinel.adapter.gateway.sc.callback.GatewayCallbackManager;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.ServerResponse;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

@Component
public class CustomBlockRequestHandler implements BlockRequestHandler {

    // 在合适的时机注册自定义的 BlockRequestHandler
    public void registerBlockRequestHandler() {
        GatewayCallbackManager.setBlockHandler(new CustomBlockRequestHandler());
    }

    @Override
    public Mono<ServerResponse> handleRequest(ServerWebExchange serverWebExchange, Throwable throwable) {
        // 自定义返回的响应
        String message = "请求频率过高，请稍后再试";
        int code = HttpStatus.TOO_MANY_REQUESTS.value();
        String data = null;
        return ServerResponse.status(HttpStatus.OK)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(new CustomBlockResponse(code, data, message));
    }

    // 自定义的响应对象
    private static class CustomBlockResponse {
        private int code;
        private Object data;
        private String message;

        public CustomBlockResponse(int code, Object data, String message) {
            this.code = code;
            this.data = data;
            this.message = message;
        }

        public int getCode() {
            return code;
        }

        public Object getData() {
            return data;
        }

        public String getMessage() {
            return message;
        }
    }
}