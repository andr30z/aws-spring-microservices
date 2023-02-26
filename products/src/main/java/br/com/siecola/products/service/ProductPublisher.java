package br.com.siecola.products.service;

import br.com.siecola.products.enums.EventType;
import br.com.siecola.products.model.Envelope;
import br.com.siecola.products.model.Product;
import br.com.siecola.products.model.ProductEvent;
import com.amazonaws.services.sns.AmazonSNS;
import com.amazonaws.services.sns.model.Topic;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

@Service
public class ProductPublisher {

  private static final Logger LOG = LoggerFactory.getLogger(
    ProductPublisher.class
  );

  private final AmazonSNS amazonSNS;
  private final Topic snsProductEventsTopic;
  private final ObjectMapper objectMapper;

  public ProductPublisher(
    AmazonSNS amazonSNS,
    @Qualifier("productEventsTopic") Topic snsProductEventsTopic,
    ObjectMapper objectMapper
  ) {
    this.amazonSNS = amazonSNS;
    this.objectMapper = objectMapper;
    this.snsProductEventsTopic = snsProductEventsTopic;
  }

  public void publishProductEvent(
    Product product,
    EventType eventType,
    String username
  ) {
    var productEvent = new ProductEvent();
    productEvent.setCode(product.getCode());
    productEvent.setProductId(product.getId());
    productEvent.setUsername(username);

    var envelope = new Envelope();
    envelope.setEventType(eventType);
    try {
      String productStringfied = objectMapper.writeValueAsString(productEvent);
      envelope.setData(productStringfied);
      amazonSNS.publish(
        snsProductEventsTopic.getTopicArn(),
        objectMapper.writeValueAsString(envelope)
      );
    } catch (JsonProcessingException e) {
      LOG.error("Failed to create event message");
    }
  }
}
