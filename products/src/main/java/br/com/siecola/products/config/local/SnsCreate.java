package br.com.siecola.products.config.local;

import com.amazonaws.client.builder.AwsClientBuilder;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.sns.*;
import com.amazonaws.services.sns.AmazonSNS;
import com.amazonaws.services.sns.model.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

@Configuration
@Profile("local")
public class SnsCreate {

  private static final Logger LOG = LoggerFactory.getLogger(SnsCreate.class);

  private final AmazonSNS amazonSNS;
  private final String productEventsTopic;

  public SnsCreate() {
    this.amazonSNS =
      AmazonSNSClient
        .builder()
        .withEndpointConfiguration(
          new AwsClientBuilder.EndpointConfiguration(
            "http://localhost:4566",
            Regions.US_EAST_1.getName()
          )
        )
        .build();
    CreateTopicRequest createTopicRequest = new CreateTopicRequest(
      "product-events"
    );
    this.productEventsTopic =
      this.amazonSNS.createTopic(createTopicRequest).getTopicArn();
    LOG.info("SNS TOPIC ARN: {}", this.productEventsTopic);
  }

  @Bean
  AmazonSNS snsClient() {
    return this.amazonSNS;
  }

  @Bean(name = "productEventsTopic")
  Topic snsProductEventsTopic() {
    return new Topic().withTopicArn(productEventsTopic);
  }
}
