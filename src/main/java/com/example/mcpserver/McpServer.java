package com.example.mcpserver;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.sun.net.httpserver.Headers;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class McpServer {
    private final Gson gson = new GsonBuilder().setPrettyPrinting().create();
    private final List<Resource> resources;
    private HttpServer httpServer;
    private ExecutorService executorService;

    public McpServer() {
        this.resources = new ArrayList<>();
        this.resources.add(new Resource("resource-1", "Quickstart Guide", "How to use this MCP server."));
        this.resources.add(new Resource("resource-2", "Java Example", "Demonstrates MCP style tools and resources."));
    }

    public void start(int port) throws IOException {
        httpServer = HttpServer.create(new InetSocketAddress(port), 0);
        executorService = Executors.newFixedThreadPool(4);
        httpServer.setExecutor(executorService);
        httpServer.createContext("/health", new HealthHandler());
        httpServer.createContext("/resources", new ResourcesHandler());
        httpServer.createContext("/tools/echo", new EchoToolHandler());
        httpServer.start();
    }

    public void stop() {
        if (httpServer != null) {
            httpServer.stop(0);
            httpServer = null;
        }
        if (executorService != null) {
            executorService.shutdownNow();
            executorService = null;
        }
    }

    public int getPort() {
        if (httpServer == null) {
            throw new IllegalStateException("Server not started");
        }
        return httpServer.getAddress().getPort();
    }

    private void writeJson(HttpExchange exchange, int statusCode, JsonObject body) throws IOException {
        byte[] data = gson.toJson(body).getBytes(StandardCharsets.UTF_8);
        Headers headers = exchange.getResponseHeaders();
        headers.add("Content-Type", "application/json; charset=utf-8");
        exchange.sendResponseHeaders(statusCode, data.length);
        try (OutputStream os = exchange.getResponseBody()) {
            os.write(data);
        }
    }

    private String readBody(HttpExchange exchange) throws IOException {
        try (InputStream is = exchange.getRequestBody()) {
            return new String(is.readAllBytes(), StandardCharsets.UTF_8);
        }
    }

    private class HealthHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            if (!"GET".equalsIgnoreCase(exchange.getRequestMethod())) {
                exchange.sendResponseHeaders(405, -1);
                return;
            }
            JsonObject body = new JsonObject();
            body.addProperty("status", "ok");
            body.addProperty("timestamp", Instant.now().toString());
            writeJson(exchange, 200, body);
        }
    }

    private class ResourcesHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            if (!"GET".equalsIgnoreCase(exchange.getRequestMethod())) {
                exchange.sendResponseHeaders(405, -1);
                return;
            }
            JsonObject body = new JsonObject();
            body.add("resources", gson.toJsonTree(resources));
            writeJson(exchange, 200, body);
        }
    }

    private class EchoToolHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            if (!"POST".equalsIgnoreCase(exchange.getRequestMethod())) {
                exchange.sendResponseHeaders(405, -1);
                return;
            }
            String request = readBody(exchange);
            JsonObject input = gson.fromJson(request, JsonObject.class);
            String message = input != null && input.has("message")
                    ? input.get("message").getAsString()
                    : "";
            JsonObject result = new JsonObject();
            result.addProperty("result", "echo: " + message);
            result.addProperty("receivedAt", Instant.now().toString());
            writeJson(exchange, 200, result);
        }
    }

    public record Resource(String id, String title, String content) { }
}
