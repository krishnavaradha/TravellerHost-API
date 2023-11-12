package com.development.travellerhost;


import java.io.IOException;
import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;
import java.util.Collections;

import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManagerFactory;

import org.springframework.cloud.client.loadbalancer.LoadBalanced;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.util.ResourceUtils;
import org.springframework.web.client.RestTemplate;
@Configuration
public class RestTemplateConfig {

    @LoadBalanced
    @Bean
    public RestTemplate restTemplate() throws KeyStoreException, IOException, CertificateException, NoSuchAlgorithmException, UnrecoverableKeyException, KeyManagementException {
        SSLContext sslContext = SSLContext.getInstance("TLS");

        // Load KeyStore
        KeyStore keyStore = KeyStore.getInstance("PKCS12");
        keyStore.load(ResourceUtils.getFile("file:src/main/resources/client-certificate.p12").toURI().toURL().openStream(), "changeit".toCharArray());

        // Load TrustStore
        KeyStore trustStore = KeyStore.getInstance("PKCS12");
        trustStore.load(ResourceUtils.getFile("file:src/main/resources/client-truststore.p12").toURI().toURL().openStream(), "changeit".toCharArray());

        KeyManagerFactory keyManagerFactory = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
        keyManagerFactory.init(keyStore, "changeit".toCharArray());

        TrustManagerFactory trustManagerFactory = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
        trustManagerFactory.init(trustStore);

        sslContext.init(keyManagerFactory.getKeyManagers(), trustManagerFactory.getTrustManagers(), new SecureRandom());

        // Create a custom SimpleClientHttpRequestFactory with the configured SSLContext
        SimpleClientHttpRequestFactory requestFactory = new SimpleClientHttpRequestFactory();
        requestFactory.setReadTimeout(5000);
        requestFactory.setConnectTimeout(5000);

        // Set the custom request factory to the RestTemplate
        RestTemplate restTemplate = new RestTemplate(requestFactory);

        // Add interceptors or other configurations if needed
        restTemplate.setInterceptors(Collections.singletonList((ClientHttpRequestInterceptor) (request, body, execution) -> {
            // Add custom logic if needed
            return execution.execute(request, body);
        }));

        return restTemplate;
    }
}