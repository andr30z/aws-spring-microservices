package com.myorg;

import java.util.Collections;
import software.amazon.awscdk.*;
import software.amazon.awscdk.services.ec2.ISecurityGroup;
import software.amazon.awscdk.services.ec2.InstanceClass;
import software.amazon.awscdk.services.ec2.InstanceSize;
import software.amazon.awscdk.services.ec2.InstanceType;
import software.amazon.awscdk.services.ec2.Peer;
import software.amazon.awscdk.services.ec2.Port;
import software.amazon.awscdk.services.ec2.SecurityGroup;
import software.amazon.awscdk.services.ec2.SubnetSelection;
import software.amazon.awscdk.services.ec2.Vpc;
import software.amazon.awscdk.services.rds.Credentials;
import software.amazon.awscdk.services.rds.CredentialsFromUsernameOptions;
import software.amazon.awscdk.services.rds.DatabaseInstance;
import software.amazon.awscdk.services.rds.DatabaseInstanceEngine;
import software.amazon.awscdk.services.rds.PostgresEngineVersion;
import software.amazon.awscdk.services.rds.PostgresInstanceEngineProps;
import software.constructs.Construct;

public class RdsStack extends Stack {

  public RdsStack(final Construct scope, final String id, Vpc vpc) {
    this(scope, id, null, vpc);
  }

  public RdsStack(
    final Construct scope,
    final String id,
    final StackProps props,
    Vpc vpc
  ) {
    super(scope, id, props);
    CfnParameter databasePassword = CfnParameter.Builder
      .create(this, "databasePassword")
      .type("String")
      .description("RDS instance password")
      .build();

    ISecurityGroup iSecurityGroup = SecurityGroup.fromSecurityGroupId(
      this,
      id,
      vpc.getVpcDefaultSecurityGroup()
    );

    iSecurityGroup.addIngressRule(Peer.anyIpv4(), Port.tcp(3306));

    DatabaseInstance databaseInstance = DatabaseInstance.Builder
      .create(this, "Rds01")
      .instanceIdentifier("aws-project01-db")
      .engine(
        DatabaseInstanceEngine.postgres(
          PostgresInstanceEngineProps
            .builder()
            .version(PostgresEngineVersion.VER_13_2)
            .build()
        )
      )
      .vpc(vpc)
      .credentials(
        Credentials.fromUsername(
          "admin",
          CredentialsFromUsernameOptions
            .builder()
            .password(
              SecretValue.unsafePlainText(databasePassword.getValueAsString())
            )
            .build()
        )
      )
      .instanceType(
        InstanceType.of(InstanceClass.BURSTABLE2, InstanceSize.MICRO)
      )
      .multiAz(false)
      .allocatedStorage(10)
      .securityGroups(Collections.singletonList(iSecurityGroup))
      .vpcSubnets(
        SubnetSelection.builder().subnets(vpc.getPublicSubnets()).build()
      )
      .build();

    CfnOutput.Builder
      .create(this, "rds-endpoint")
      .exportName("rds-endpoint")
      .value(databaseInstance.getDbInstanceEndpointAddress())
      .build();

    CfnOutput.Builder
      .create(this, "rds-endpoint")
      .exportName("rds-password")
      .value(databaseInstance.getDbInstanceEndpointAddress())
      .build();
  }
}
