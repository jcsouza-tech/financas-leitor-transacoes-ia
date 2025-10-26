package br.com.financas.leitor_transacoes_ia.config;

import io.awspring.cloud.sqs.config.SqsMessageListenerContainerFactory;
import io.awspring.cloud.sqs.listener.acknowledgement.handler.AcknowledgementMode;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.AwsCredentialsProvider;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.sqs.SqsAsyncClient;

import java.time.Duration;
import java.net.URI;

@Configuration
public class SQSConfig {

    // Nomes das filas - podem ser injetados via properties também
    public static final String TRANSACOES_QUEUE = "financas-transacoes-processadas";
    public static final String DLQ_QUEUE = "financas-transacoes-dlq";

    @Value("${aws.region:us-east-1}")
    private String awsRegion;

    @Value("${aws.sqs.endpoint:}")
    private String sqsEndpoint;

    @Bean
    @Profile("!dev") // Para produção e outros ambientes
    public SqsAsyncClient sqsAsyncClientProduction(AwsCredentialsProvider credentialsProvider) {
        return SqsAsyncClient.builder()
                .region(Region.of(awsRegion))
                .credentialsProvider(credentialsProvider)
                .build();
    }

    @Bean
    @Profile("dev") // Para desenvolvimento local
    public SqsAsyncClient sqsAsyncClientLocal() {
        // LocalStack ou ElasticMQ
        return SqsAsyncClient.builder()
                .endpointOverride(URI.create(sqsEndpoint))
                .region(Region.of(awsRegion))
                .credentialsProvider(StaticCredentialsProvider.create(
                        AwsBasicCredentials.create("test", "test")))
                .build();
    }

    @Bean
    public SqsMessageListenerContainerFactory<Object> defaultSqsListenerContainerFactory(
            SqsAsyncClient sqsAsyncClient) {

        return SqsMessageListenerContainerFactory.builder()
                .sqsAsyncClient(sqsAsyncClient)
                .configure(options -> options
                        .acknowledgementMode(AcknowledgementMode.ALWAYS)
                        .maxMessagesPerPoll(5)
                        .pollTimeout(Duration.ofSeconds(20))
                        .maxConcurrentMessages(5) // Bom para controlar concorrência
                )
                .build();
    }
}