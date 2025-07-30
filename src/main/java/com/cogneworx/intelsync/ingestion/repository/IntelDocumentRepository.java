package com.cogneworx.intelsync.ingestion.repository;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch.core.*;
import com.cogneworx.intelsync.ingestion.model.IntelDocument;
import org.springframework.stereotype.Repository;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

@Repository
public class IntelDocumentRepository {

    private static final String INDEX_NAME = "intel-documents";

    private final ElasticsearchClient client;

    public IntelDocumentRepository(ElasticsearchClient client) {
        this.client = client;
    }

    public void save(IntelDocument doc) {
        try {
            client.index(i -> i
                .index(INDEX_NAME)
                .id(doc.getId())
                .document(doc)
            );
        } catch (IOException e) {
            throw new RuntimeException("Failed to index document", e);
        }
    }

    public IntelDocument findById(String id) {
        try {
            GetResponse<IntelDocument> response = client.get(g -> g
                .index(INDEX_NAME)
                .id(id), IntelDocument.class
            );

            return response.found() ? response.source() : null;
        } catch (IOException e) {
            throw new RuntimeException("Failed to get document", e);
        }
    }

    public List<IntelDocument> findAll() {
        try {
            SearchResponse<IntelDocument> response = client.search(s -> s
                .index(INDEX_NAME)
                .query(q -> q.matchAll(m -> m)), IntelDocument.class
            );

            return response.hits().hits().stream()
                .map(hit -> hit.source())
                .collect(Collectors.toList());
        } catch (IOException e) {
            throw new RuntimeException("Failed to search documents", e);
        }
    }

    public void deleteById(String id) {
        try {
            client.delete(d -> d
                .index(INDEX_NAME)
                .id(id)
            );
        } catch (IOException e) {
            throw new RuntimeException("Failed to delete document", e);
        }
    }
}