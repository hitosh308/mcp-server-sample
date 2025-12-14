# mcp-server-sample

シンプルなJava製のMCP風サーバーです。`com.sun.net.httpserver.HttpServer` を使い、ヘルスチェック、リソース一覧、エコーツールのエンドポイントを提供します。

## 動かし方

```bash
mvn clean package
java -jar target/mcp-server-0.1.0.jar # デフォルトでは 8080 ポートで待ち受け
```

任意のポートで起動する場合は引数にポート番号を渡してください。

```bash
java -jar target/mcp-server-0.1.0.jar 9090
```

### エンドポイント

- `GET /health` — サーバーの稼働状況を返します。
- `GET /resources` — 2件のサンプルリソースをJSONで返します。
- `POST /tools/echo` — JSON `{ "message": "..." }` を渡すとエコー結果を返します。

## テスト

```bash
mvn test
```
