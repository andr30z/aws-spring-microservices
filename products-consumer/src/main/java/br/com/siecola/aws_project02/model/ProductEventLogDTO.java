package br.com.siecola.aws_project02.model;

import br.com.siecola.aws_project02.enums.EventType;
import lombok.AllArgsConstructor;
import lombok.Data;
import org.springframework.beans.BeanUtils;

@Data
@AllArgsConstructor
public class ProductEventLogDTO {

  private String code;

  private EventType eventType;

  private long productId;

  private String username;

  private long timestamp;

  public ProductEventLogDTO(ProductEventLog productEventLog) {
    BeanUtils.copyProperties(productEventLog, this);
    setCode(productEventLog.getPk());
  }
}
