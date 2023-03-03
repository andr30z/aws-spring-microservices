package br.com.siecola.aws_project02.config.local;

import br.com.siecola.aws_project02.repository.ProductEventLogRepository;
import com.amazonaws.auth.DefaultAWSCredentialsProviderChain;
import com.amazonaws.client.builder.AwsClientBuilder;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClient;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClientBuilder;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapper;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapperConfig;
import com.amazonaws.services.dynamodbv2.document.DynamoDB;
import com.amazonaws.services.dynamodbv2.document.Table;
import com.amazonaws.services.dynamodbv2.model.AttributeDefinition;
import com.amazonaws.services.dynamodbv2.model.BillingMode;
import com.amazonaws.services.dynamodbv2.model.CreateTableRequest;
import com.amazonaws.services.dynamodbv2.model.KeySchemaElement;
import com.amazonaws.services.dynamodbv2.model.KeyType;
import com.amazonaws.services.dynamodbv2.model.ProvisionedThroughput;
import com.amazonaws.services.dynamodbv2.model.ResourceInUseException;
import com.amazonaws.services.dynamodbv2.model.ScalarAttributeType;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.socialsignin.spring.data.dynamodb.repository.config.EnableDynamoDBRepositories;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;

@Configuration
@EnableDynamoDBRepositories(
  basePackageClasses = ProductEventLogRepository.class
)
@Profile("local")
public class DynamoDBConfigLocal {

  @Value("${aws.region}")
  private String awsRegion;

  private static final Logger LOG = LoggerFactory.getLogger(
    DynamoDBConfigLocal.class
  );

  private final AmazonDynamoDB amazonDynamoDB;

  public DynamoDBConfigLocal() {
    this.amazonDynamoDB =
      AmazonDynamoDBClient
        .builder()
        .withEndpointConfiguration(
          new AwsClientBuilder.EndpointConfiguration(
            "http://localhost:4566",
            Regions.US_EAST_1.getName()
          )
        )
        .withCredentials(new DefaultAWSCredentialsProviderChain())
        .build();

    DynamoDB dynamoDB = new DynamoDB(amazonDynamoDB);
    List<AttributeDefinition> attributeDefinitions = List.of(
      new AttributeDefinition()
        .withAttributeName("pk")
        .withAttributeType(ScalarAttributeType.S),
      new AttributeDefinition()
        .withAttributeName("sk")
        .withAttributeType(ScalarAttributeType.S)
    );

    List<KeySchemaElement> keySchema = List.of(
      new KeySchemaElement().withAttributeName("pk").withKeyType(KeyType.HASH),
      new KeySchemaElement().withAttributeName("sk").withKeyType(KeyType.RANGE)
    );

    CreateTableRequest request = new CreateTableRequest()
      .withTableName("product-events")
      .withKeySchema(keySchema)
      .withAttributeDefinitions(attributeDefinitions)
      .withProvisionedThroughput(new ProvisionedThroughput(1L, 1L))
      .withBillingMode(BillingMode.PAY_PER_REQUEST); //no need to do this

    LOG.info("Creating DynamoDB table request...");
    try {
      Table table = dynamoDB.createTable(request);
      table.waitForActive();
    } catch (Exception e) {
      LOG.error(e.getMessage());
    }
  }

  @Bean
  @Primary
  DynamoDBMapperConfig dynamoDBMapperConfig() {
    return DynamoDBMapperConfig.DEFAULT;
  }

  @Bean
  @Primary
  DynamoDBMapper dynamoDBMapper(
    AmazonDynamoDB amazonDynamoDB,
    DynamoDBMapperConfig dbMapperConfig
  ) {
    return new DynamoDBMapper(amazonDynamoDB, dbMapperConfig);
  }

  @Bean
  @Primary
  AmazonDynamoDB amazonDynamoDB() {
    return this.amazonDynamoDB;
  }
}
