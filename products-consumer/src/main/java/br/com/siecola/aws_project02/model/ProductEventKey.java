package br.com.siecola.aws_project02.model;

import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBHashKey;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
public class ProductEventKey {

  private String pk;
  private String sk;

  @DynamoDBHashKey(attributeName = "pk")
  public String getPk() {
    return pk;
  }

  public void setPk(String pk) {
    this.pk = pk;
  }

  @DynamoDBHashKey(attributeName = "sk")
  public String getSk() {
    return sk;
  }

  public void setSk(String sk) {
    this.sk = sk;
  }
}
