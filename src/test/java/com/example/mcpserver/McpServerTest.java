package com.example.mcpserver;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class McpServerTest {
    private final Gson gson = new Gson();
    private McpServer server;
    private HttpClient client;
    private int port;

    @BeforeEach
    void setUp() throws IOException {
        server = new McpServer();
        server.start(0);
        port = server.getPort();
        client = HttpClient.newHttpClient();
    }

    @AfterEach
    void tearDown() {
        server.stop();
    }

    @Test
    void healthEndpointReturnsOk() throws Exception {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:" + port + "/health"))
                .GET()
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(200, response.statusCode());
        JsonObject json = gson.fromJson(response.body(), JsonObject.class);
        assertEquals("ok", json.get("status").getAsString());
        assertTrue(json.has("timestamp"));
    }

    @Test
    void resourcesEndpointListsResources() throws Exception {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:" + port + "/resources"))
                .GET()
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(200, response.statusCode());
        JsonObject json = gson.fromJson(response.body(), JsonObject.class);
        JsonArray resources = json.getAsJsonArray("resources");
        assertTrue(resources.size() >= 2);
        assertTrue(resources.get(0).getAsJsonObject().has("id"));
    }

    @Test
    void echoToolEchoesMessage() throws Exception {
        JsonObject payload = new JsonObject();
        payload.addProperty("message", "hello");

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:" + port + "/tools/echo"))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(payload.toString(), StandardCharsets.UTF_8))
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(200, response.statusCode());
        JsonObject json = gson.fromJson(response.body(), JsonObject.class);
        assertEquals("echo: hello", json.get("result").getAsString());
        assertTrue(json.has("receivedAt"));
    }
}
