package com.myorg;

import software.amazon.awscdk.Duration;
import software.amazon.awscdk.RemovalPolicy;
import software.amazon.awscdk.Stack;
import software.amazon.awscdk.StackProps;
import software.amazon.awscdk.services.dynamodb.Attribute;
import software.amazon.awscdk.services.dynamodb.AttributeType;
import software.amazon.awscdk.services.dynamodb.BillingMode;
import software.amazon.awscdk.services.dynamodb.EnableScalingProps;
// import software.amazon.awscdk.Duration;
// import software.amazon.awscdk.services.sqs.Queue;
import software.amazon.awscdk.services.dynamodb.Table;
import software.amazon.awscdk.services.dynamodb.UtilizationScalingProps;
import software.constructs.Construct;

public class DynamoDBStack extends Stack {

  private final Table productEventsDdb;

  public DynamoDBStack(final Construct scope, final String id) {
    this(scope, id, null);
  }

  public DynamoDBStack(
    final Construct scope,
    final String id,
    final StackProps props
  ) {
    super(scope, id, props);
    this.productEventsDdb =
      Table.Builder
        .create(this, "ProductEventsDb")
        .tableName("product-events")
        .billingMode(BillingMode.PROVISIONED)
        .readCapacity(1)
        .writeCapacity(1)
        .partitionKey(
          Attribute.builder().name("pk").type(AttributeType.STRING).build()
        )
        .sortKey(
          Attribute.builder().name("sk").type(AttributeType.STRING).build()
        )
        .timeToLiveAttribute("ttl")
        .removalPolicy(RemovalPolicy.DESTROY)
        .build();

    //auto scaling configs below
    productEventsDdb
      .autoScaleReadCapacity(
        EnableScalingProps.builder().minCapacity(1).maxCapacity(4).build()
      )
      .scaleOnUtilization(
        UtilizationScalingProps
          .builder()
          .targetUtilizationPercent(50)
          .scaleInCooldown(Duration.seconds(30))
          .scaleOutCooldown(Duration.seconds(30))
          .build()
      );

    productEventsDdb
      .autoScaleWriteCapacity(
        EnableScalingProps.builder().minCapacity(1).maxCapacity(4).build()
      )
      .scaleOnUtilization(
        UtilizationScalingProps
          .builder()
          .targetUtilizationPercent(50)
          .scaleInCooldown(Duration.seconds(30))
          .scaleOutCooldown(Duration.seconds(30))
          .build()
      );
  }

  Table getProductEventsDdb() {
    return this.productEventsDdb;
  }
}
