package com.myorg;

import software.amazon.awscdk.App;

public class AwsCourseApp {

  public static void main(final String[] args) {
    App app = new App();

    VpcStack vpcStack = new VpcStack(app, "Vpc");
    ClusterStack clusterStack = new ClusterStack(
      app,
      "Cluster",
      vpcStack.getVpc()
    );
    clusterStack.addDependency(vpcStack);

    RdsStack rdsStack = new RdsStack(app, "RDS", vpcStack.getVpc());
    rdsStack.addDependency(vpcStack);

    InvoiceAppStack invoiceAppStack = new InvoiceAppStack(app, "InvoiceApp");

    SnsStack snsStack = new SnsStack(app, "Sns");
    Service01Stack service01Stack = new Service01Stack(
      app,
      "Service01",
      clusterStack.getCluster(),
      snsStack.getProductEventsTopic(),
      invoiceAppStack.getBucket(),
      invoiceAppStack.getS3InvoiceQueue()
    );
    service01Stack.addDependency(invoiceAppStack);
    service01Stack.addDependency(clusterStack);
    service01Stack.addDependency(rdsStack);
    service01Stack.addDependency(snsStack);

    DynamoDBStack dbStack = new DynamoDBStack(app, "Ddb");

    Service02Stack service02Stack = new Service02Stack(
      app,
      "Service02",
      clusterStack.getCluster(),
      snsStack.getProductEventsTopic(),
      dbStack.getProductEventsDdb()
    );
    service02Stack.addDependency(clusterStack);
    service02Stack.addDependency(snsStack);
    service02Stack.addDependency(dbStack);

    app.synth();
  }
}
