# IntelSync Ingestion Service

IntelSync Ingestion Service is responsible for ingesting, parsing, and indexing files (e.g., PDFs, DOCX) into Elasticsearch using Apache Tika and the official Elasticsearch Java client.

## 📦 Features

- Upload files via REST endpoint
- Parse and extract metadata and text using Apache Tika
- Store documents in Elasticsearch 8.x using `elasticsearch-java` client
- HTTPS support with basic authentication
- MIME type detection
- Spring Boot based architecture

## 🚀 Getting Started

### Prerequisites

- Java 21
- Maven 3.6+
- Elasticsearch 8.10.0
- Docker (optional, for running ES locally)

### Configuration

Set the following properties in `application.properties`:

```
elasticsearch.host=https://localhost:9200
elasticsearch.username=elastic
elasticsearch.password=changeme
elasticsearch.allow-insecure=true
```

### Build

```bash
mvn clean install
```

### Run

```bash
mvn spring-boot:run
```

## 📤 File Upload API

### Endpoint

```
POST /api/ingest/upload
```

### Request

```bash
curl -k -F "file=@/path/to/file.pdf" http://localhost:8081/api/ingest/upload
```

### Response

```json
{
  "message": "File processed and indexed successfully",
  "documentId": "generated-doc-id"
}
```

## 🧪 Testing

To verify Elasticsearch connection:

```bash
curl -u elastic:changeme -k https://localhost:9200
```

## 🛠 Dependencies

- Spring Boot
- Apache Tika
- Elasticsearch Java Client 8.10.0
- Apache HttpClient 5.x
- Lombok

## 📄 License

MIT License
