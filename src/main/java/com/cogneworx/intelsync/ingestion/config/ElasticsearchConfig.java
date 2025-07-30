package com.cogneworx.intelsync.ingestion.config;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.json.jackson.JacksonJsonpMapper;
import co.elastic.clients.transport.rest_client.RestClientTransport;
import org.apache.http.HttpHost;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.conn.ssl.NoopHostnameVerifier;
import org.apache.http.conn.ssl.TrustStrategy;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.ssl.SSLContextBuilder;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestClientBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.net.ssl.SSLContext;
import java.security.cert.X509Certificate;

@Configuration
public class ElasticsearchConfig {

    @Value("${elasticsearch.host}")
    private String host; // e.g. https://localhost:9200

    @Value("${elasticsearch.username}")
    private String username;

    @Value("${elasticsearch.password}")
    private String password;

    @Value("${elasticsearch.allow-insecure:true}")
    private boolean allowInsecure;

    @Bean
    public ElasticsearchClient elasticsearchClient() throws Exception {
        HttpHost httpHost = HttpHost.create(host);
        BasicCredentialsProvider credentialsProvider = new BasicCredentialsProvider();
        credentialsProvider.setCredentials(
                new AuthScope(httpHost),
                new UsernamePasswordCredentials(username, password)
        );

        RestClientBuilder builder = RestClient.builder(httpHost)
            .setHttpClientConfigCallback(httpClientBuilder -> {
                httpClientBuilder.setDefaultCredentialsProvider(credentialsProvider);

                if (allowInsecure) {
                    try {
                        TrustStrategy trustAllStrategy = (X509Certificate[] chain, String authType) -> true;
                        SSLContext sslContext = SSLContextBuilder.create()
                                .loadTrustMaterial(null, trustAllStrategy)
                                .build();

                        httpClientBuilder.setSSLContext(sslContext);
                        httpClientBuilder.setSSLHostnameVerifier(NoopHostnameVerifier.INSTANCE);
                    } catch (Exception e) {
                        throw new RuntimeException("Failed to set insecure SSL context", e);
                    }
                }

                return httpClientBuilder;
            });

        RestClient restClient = builder.build();
        RestClientTransport transport = new RestClientTransport(restClient, new JacksonJsonpMapper());
        return new ElasticsearchClient(transport);
    }
}
