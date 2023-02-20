package com.myorg;

import java.util.HashMap;
import java.util.Map;
import software.amazon.awscdk.Duration;
import software.amazon.awscdk.Fn;
import software.amazon.awscdk.RemovalPolicy;
import software.amazon.awscdk.Stack;
import software.amazon.awscdk.StackProps;
import software.amazon.awscdk.services.applicationautoscaling.EnableScalingProps;
import software.amazon.awscdk.services.ecs.AwsLogDriverProps;
import software.amazon.awscdk.services.ecs.Cluster;
import software.amazon.awscdk.services.ecs.ContainerImage;
import software.amazon.awscdk.services.ecs.CpuUtilizationScalingProps;
import software.amazon.awscdk.services.ecs.LogDriver;
import software.amazon.awscdk.services.ecs.ScalableTaskCount;
import software.amazon.awscdk.services.ecs.patterns.ApplicationLoadBalancedFargateService;
import software.amazon.awscdk.services.ecs.patterns.ApplicationLoadBalancedTaskImageOptions;
import software.amazon.awscdk.services.elasticloadbalancingv2.HealthCheck;
import software.amazon.awscdk.services.events.targets.SnsTopic;
import software.amazon.awscdk.services.logs.LogGroup;
import software.constructs.Construct;

public class Service01Stack extends Stack {

  public Service01Stack(
    final Construct scope,
    String id,
    Cluster cluster,
    SnsTopic productEventsTopic
  ) {
    this(scope, id, null, cluster, productEventsTopic);
  }

  public Service01Stack(
    final Construct scope,
    final String id,
    final StackProps props,
    Cluster cluster,
    SnsTopic productEventsTopic
  ) {
    super(scope, id, props);
    Map<String, String> envVariables = new HashMap<>();
    envVariables.put(
      "SPRING_DATASOURCE_URL",
      "jdbc:postgresql://" +
      Fn.importValue("rds-endpoint") +
      ":3306/aws_project01"
    );
    envVariables.put("SPRING_DATASOURCE_USERNAME", "admin");
    envVariables.put(
      "SPRING_DATASOURCE_PASSWORD",
      Fn.importValue("rds-password")
    );

    ApplicationLoadBalancedFargateService service01 = ApplicationLoadBalancedFargateService.Builder
      .create(this, "ALB01")
      .serviceName("service01")
      .cluster(cluster)
      .cpu(512)
      .memoryLimitMiB(1024)
      .desiredCount(2)
      .listenerPort(8080)
      .taskImageOptions(
        ApplicationLoadBalancedTaskImageOptions
          .builder()
          .containerName("aws-project01")
          .image(
            ContainerImage.fromRegistry("andr30z/curso_aws_project01:1.2.0")
          )
          .containerPort(8080)
          .logDriver(
            LogDriver.awsLogs(
              AwsLogDriverProps
                .builder()
                .logGroup(
                  LogGroup.Builder
                    .create(this, "Service01LogGroup")
                    .logGroupName("Service01")
                    .removalPolicy(RemovalPolicy.DESTROY)
                    .build()
                )
                .streamPrefix("Service01")
                .build()
            )
          )
          .environment(envVariables)
          .build()
      )
      .publicLoadBalancer(true)
      .build();

    service01
      .getTargetGroup()
      .configureHealthCheck(
        new HealthCheck.Builder()
          .path("/actuator/health")
          .port("8080")
          .healthyHttpCodes("200")
          .build()
      );

    ScalableTaskCount scalableTaskCount = service01
      .getService()
      .autoScaleTaskCount(
        EnableScalingProps.builder().minCapacity(2).maxCapacity(4).build()
      );

    scalableTaskCount.scaleOnCpuUtilization(
      "Service01AutoScaling",
      CpuUtilizationScalingProps
        .builder()
        .targetUtilizationPercent(50)
        .scaleInCooldown(Duration.seconds(60))
        .scaleOutCooldown(Duration.seconds(60))
        .build()
    );

    productEventsTopic
      .getTopic()
      .grantPublish(service01.getTaskDefinition().getTaskRole());
  }
}
