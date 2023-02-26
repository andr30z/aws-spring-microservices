package br.com.siecola.products.config.local;

import com.amazonaws.client.builder.AwsClientBuilder;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.sns.AmazonSNS;
import com.amazonaws.services.sns.model.Topic;
import com.amazonaws.services.sns.util.Topics;
import com.amazonaws.services.sqs.AmazonSQS;
import com.amazonaws.services.sqs.AmazonSQSClient;
import com.amazonaws.services.sqs.model.CreateQueueRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

@Configuration
@Profile("local")
public class SqsCreateSubscriber {

  private static final Logger LOG = LoggerFactory.getLogger(
    SqsCreateSubscriber.class
  );

  public SqsCreateSubscriber(
    AmazonSNS snsClient,
    @Qualifier("productEventsTopic") Topic productEventsTopic
  ) {

    AmazonSQS sqsClient = AmazonSQSClient
      .builder()
      .withEndpointConfiguration(
        new AwsClientBuilder.EndpointConfiguration(
          "http://localhost:4566",
          Regions.US_EAST_1.getName()
        )
      )
      .build();
    String productEventsQueueUrl = sqsClient
      .createQueue(new CreateQueueRequest("product-events"))
      .getQueueUrl();

    Topics.subscribeQueue(
      snsClient,
      sqsClient,
      productEventsTopic.getTopicArn(),
      productEventsQueueUrl
    );
    LOG.info("CREATING SQS LOCAL QUEUE");
  }
}
