package com.cogneworx.intelsync.ingestion.controller;

import org.apache.tika.Tika;
import org.apache.tika.exception.TikaException;
import org.apache.tika.metadata.Metadata;
import org.apache.tika.parser.AutoDetectParser;
import org.apache.tika.parser.ParseContext;
import org.apache.tika.sax.BodyContentHandler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.xml.sax.ContentHandler;
import org.xml.sax.SAXException;

import com.cogneworx.intelsync.ingestion.model.IntelDocument;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch.core.IndexResponse;
import co.elastic.clients.elasticsearch.core.SearchResponse;

import java.io.IOException;
import java.io.InputStream;
import java.io.BufferedInputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.Objects;

@RestController
@RequestMapping("/api/ingest")
public class IngestionController {

    private final Tika tika = new Tika();

    private final ElasticsearchClient elasticsearchClient;

    @Autowired
    public IngestionController(ElasticsearchClient elasticsearchClient) {
        this.elasticsearchClient = elasticsearchClient;
    }

    @PostMapping(value = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<Map<String, Object>> uploadFile(@RequestParam("file") MultipartFile file) {
        if (file.isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("error", "File is empty"));
        }

        Map<String, Object> response = new HashMap<>();
        response.put("filename", file.getOriginalFilename());
        response.put("contentType", file.getContentType());

        try (InputStream stream = file.getInputStream();
             InputStream streamWithMark = new BufferedInputStream(stream)) {

            // Mark the stream to allow reset (use a large enough readlimit)
            streamWithMark.mark(10 * 1024 * 1024); // 10MB limit for mark/reset buffer

            // Detect MIME type
            String detectedType = tika.detect(streamWithMark);
            response.put("detectedType", detectedType);

            // Reset stream
            streamWithMark.reset();

            // Parse content with Tika
            ContentHandler handler = new BodyContentHandler(-1); // unlimited
            Metadata metadata = new Metadata();
            AutoDetectParser parser = new AutoDetectParser();
            ParseContext context = new ParseContext();

            parser.parse(streamWithMark, handler, metadata, context);

            String extractedText = handler.toString();
            String[] metadataNames = metadata.names();

            // Prepare IntelDocument
            IntelDocument doc = new IntelDocument();
            doc.setId(UUID.randomUUID().toString());
            doc.setFilename(file.getOriginalFilename());
            doc.setContentType(file.getContentType());
            doc.setDetectedType(detectedType);
            doc.setExtractedText(extractedText);
            doc.setMetadata(metadataNames);

            // Index into Elasticsearch using Java API Client
            IndexResponse indexResponse = elasticsearchClient.index(i -> i
                .index("intel-documents")     // Matches your @Document index
                .id(doc.getId())              // Use the generated UUID
                .document(doc)                // The full POJO object
            );

            return ResponseEntity.ok(Map.of(
                "message", "File processed and indexed successfully",
                "documentId", indexResponse.id()
            ));  
        } catch (IOException | SAXException | TikaException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Failed to parse file: " + e.getMessage()));
        }
    }

   @GetMapping("/search")
    public ResponseEntity<Map<String, Object>> search(
            @RequestParam("q") String query,
            @RequestParam(value = "size", defaultValue = "10") int size) {

        try {
            SearchResponse<IntelDocument> response = elasticsearchClient.search(s -> s
                            .index("intel-documents")
                            .query(q -> q
                                .multiMatch(m -> m
                                    .query(query)
                                    .fields("extractedText", "filename", "detectedType", "contentType")
                                )
                            )
                            .size(size),
                    IntelDocument.class);

            List<IntelDocument> results = response.hits().hits().stream()
                    .map(hit -> hit.source())
                    .filter(Objects::nonNull)
                    .collect(Collectors.toList());

            Map<String, Object> result = new HashMap<>();
            Long totalHits = response.hits().total() != null ? response.hits().total().value() : 0L;
            result.put("total", totalHits);
            result.put("results", results);

            return ResponseEntity.ok(result);

        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Search failed: " + e.getMessage()));
        }
    } 
}
