package com.example.mcpserver;

public class Main {
    public static void main(String[] args) throws Exception {
        int port = 8080;
        if (args.length > 0) {
            port = Integer.parseInt(args[0]);
        }
        McpServer server = new McpServer();
        server.start(port);
        System.out.printf("MCP server running on http://localhost:%d%n", server.getPort());
        Runtime.getRuntime().addShutdownHook(new Thread(server::stop));
    }
}
