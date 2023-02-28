package br.com.siecola.aws_project02.config;

import com.amazonaws.auth.DefaultAWSCredentialsProviderChain;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClientBuilder;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapper;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapperConfig;

import br.com.siecola.aws_project02.repository.ProductEventLogRepository;

import org.socialsignin.spring.data.dynamodb.repository.config.EnableDynamoDBRepositories;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

@Configuration
 @EnableDynamoDBRepositories(basePackageClasses = ProductEventLogRepository.class)
public class DynamoDBConfig {

  @Value("${aws.region}")
  private String awsRegion;

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
    return AmazonDynamoDBClientBuilder
      .standard()
      .withCredentials(new DefaultAWSCredentialsProviderChain())
      .withRegion(Regions.fromName(awsRegion))
      .build();
  }
}
