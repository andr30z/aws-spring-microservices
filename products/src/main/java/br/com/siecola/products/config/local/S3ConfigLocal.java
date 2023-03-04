package br.com.siecola.products.config.local;

import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.auth.DefaultAWSCredentialsProviderChain;
import com.amazonaws.client.builder.AwsClientBuilder;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.amazonaws.services.s3.model.BucketNotificationConfiguration;
import com.amazonaws.services.s3.model.S3Event;
import com.amazonaws.services.s3.model.TopicConfiguration;
import com.amazonaws.services.sns.AmazonSNS;
import com.amazonaws.services.sns.AmazonSNSClient;
import com.amazonaws.services.sns.model.CreateTopicRequest;
import com.amazonaws.services.sns.util.Topics;
import com.amazonaws.services.sqs.AmazonSQS;
import com.amazonaws.services.sqs.AmazonSQSClient;
import com.amazonaws.services.sqs.model.CreateQueueRequest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

@Configuration
@Profile("local")
public class S3ConfigLocal {

  private static final String BUCKET_NAME = "pcs-invoice";

  private final AmazonS3 amazonS3;

  public S3ConfigLocal() {
    this.amazonS3 = getAmazonS3();
    createBucket();

    AmazonSNS snsClient = getAmazonSNS();

    String s3InvoiceEventsTopicArn = createTopic(snsClient);
    AmazonSQS sqsClient = getAmazonSQS();

    createQueue(snsClient, sqsClient, s3InvoiceEventsTopicArn);
    configureBucket(s3InvoiceEventsTopicArn);
  }

  private void configureBucket(String s3InvoiceEventsTopicArn) {
    TopicConfiguration topicConfiguration = new TopicConfiguration();
    topicConfiguration.setTopicARN(s3InvoiceEventsTopicArn);
    topicConfiguration.addEvent(S3Event.ObjectCreatedByPut);

    this.amazonS3.setBucketNotificationConfiguration(
        BUCKET_NAME,
        new BucketNotificationConfiguration()
          .addConfiguration("putObject", topicConfiguration)
      );
  }

  private void createQueue(
    AmazonSNS snsClient,
    AmazonSQS sqsClient,
    String s3InvoiceEventsTopicArn
  ) {
    String s3EventsQueueUrl = sqsClient
      .createQueue(new CreateQueueRequest("s3-invoice-events"))
      .getQueueUrl();

    Topics.subscribeQueue(
      snsClient,
      sqsClient,
      s3InvoiceEventsTopicArn,
      s3EventsQueueUrl
    );
  }

  private String createTopic(AmazonSNS snsClient) {
    CreateTopicRequest createTopicRequest = new CreateTopicRequest(
      "s3-invoice-events"
    );
    return snsClient.createTopic(createTopicRequest).getTopicArn();
  }

  private AmazonSNS getAmazonSNS() {
    return AmazonSNSClient
      .builder()
      .withEndpointConfiguration(
        new AwsClientBuilder.EndpointConfiguration(
          "http://localhost:4566",
          Regions.US_EAST_1.getName()
        )
      )
      .withCredentials(new DefaultAWSCredentialsProviderChain())
      .build();
  }

  private AmazonSQS getAmazonSQS() {
    return AmazonSQSClient
      .builder()
      .withEndpointConfiguration(
        new AwsClientBuilder.EndpointConfiguration(
          "http://localhost:4566",
          Regions.US_EAST_1.getName()
        )
      )
      .withCredentials(new DefaultAWSCredentialsProviderChain())
      .build();
  }

  private void createBucket() {
    this.amazonS3.createBucket(BUCKET_NAME);
  }

  private AmazonS3 getAmazonS3() {
    return AmazonS3ClientBuilder
      .standard()
      .withEndpointConfiguration(
        new AwsClientBuilder.EndpointConfiguration(
          "http://localhost:4566",
          Regions.US_EAST_1.getName()
        )
      )
      .withCredentials(
        new AWSStaticCredentialsProvider(
          new BasicAWSCredentials("test", "test")
        )
      )
      .enablePathStyleAccess()
      .build();
  }

  @Bean
  AmazonS3 amazonS3Client() {
    return this.amazonS3;
  }
}
