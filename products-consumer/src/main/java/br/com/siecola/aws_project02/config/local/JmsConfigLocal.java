package br.com.siecola.aws_project02.config.local;

import com.amazon.sqs.javamessaging.ProviderConfiguration;
import com.amazon.sqs.javamessaging.SQSConnectionFactory;
import com.amazonaws.auth.DefaultAWSCredentialsProviderChain;
import com.amazonaws.client.builder.AwsClientBuilder;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.sqs.AmazonSQSClient;
import com.amazonaws.services.sqs.AmazonSQSClientBuilder;
import javax.jms.Session;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.jms.annotation.EnableJms;
import org.springframework.jms.config.DefaultJmsListenerContainerFactory;
import org.springframework.jms.support.destination.DynamicDestinationResolver;

@Configuration
@EnableJms
@Profile("local")
public class JmsConfigLocal {

  @Value("${aws.region}")
  private String awsRegion;

  private SQSConnectionFactory sqsConnectionFactory;

  @Bean
  DefaultJmsListenerContainerFactory jmsListenerContainerFactory() {
    sqsConnectionFactory =
      new SQSConnectionFactory(
        new ProviderConfiguration(),
        AmazonSQSClient
          .builder()
          .withEndpointConfiguration(
            new AwsClientBuilder.EndpointConfiguration(
              "http://localhost:4566",
              Regions.US_EAST_1.getName()
            )
          )
          .build()
      );

    DefaultJmsListenerContainerFactory factory = new DefaultJmsListenerContainerFactory();
    factory.setConnectionFactory(sqsConnectionFactory);
    factory.setDestinationResolver(new DynamicDestinationResolver());
    factory.setConcurrency("2");
    factory.setSessionAcknowledgeMode(Session.CLIENT_ACKNOWLEDGE);

    return factory;
  }
}
