package br.com.siecola.products.consumer;

import br.com.siecola.products.model.Invoice;
import br.com.siecola.products.model.SnsMessage;
import br.com.siecola.products.repository.InvoiceRepository;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.event.S3EventNotification;
import com.amazonaws.services.s3.model.S3Object;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import javax.jms.JMSException;
import javax.jms.TextMessage;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jms.annotation.JmsListener;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class InvoiceConsumer {

  private final ObjectMapper objectMapper;

  private final InvoiceRepository invoiceRepository;

  private final AmazonS3 amazonS3;

  public InvoiceConsumer(
    ObjectMapper objectMapper,
    InvoiceRepository invoiceRepository,
    AmazonS3 amazonS3
  ) {
    this.objectMapper = objectMapper;
    this.invoiceRepository = invoiceRepository;
    this.amazonS3 = amazonS3;
  }

  @JmsListener(destination = "${aws.sqs.queue.invoice.events.name}")
  void receiveS3Event(TextMessage textMessage)
    throws JMSException, IOException {
    SnsMessage snsMessage = objectMapper.readValue(
      textMessage.getText(),
      SnsMessage.class
    );
    S3EventNotification s3EventNotification = objectMapper.readValue(
      snsMessage.getMessage(),
      S3EventNotification.class
    );

    processInvoiceNotification(s3EventNotification);
  }

  private void processInvoiceNotification(
    S3EventNotification s3EventNotification
  ) throws IOException {
    for (S3EventNotification.S3EventNotificationRecord s3EventNotificationRecord : s3EventNotification.getRecords()) {
      S3EventNotification.S3Entity s3EventNotificationEntity = s3EventNotificationRecord.getS3();
      String bucketName = s3EventNotificationEntity.getBucket().getName();
      String objectKey = s3EventNotificationEntity.getObject().getKey();
      String invoiceFile = downloadObject(bucketName, objectKey);

      Invoice invoice = objectMapper.readValue(invoiceFile, Invoice.class);

      log.info("Invoice Received: {}", invoice.getInvoiceNumber());

      invoiceRepository.save(invoice);

      amazonS3.deleteObject(bucketName, objectKey);
    }
  }

  private String downloadObject(String bucketName, String objectKey)
    throws IOException {
    S3Object s3Object = amazonS3.getObject(bucketName, objectKey);
    var stringBuilder = new StringBuilder();
    var bufferedReader = new BufferedReader(
      new InputStreamReader(s3Object.getObjectContent())
    );
    String content = null;

    while ((content = bufferedReader.readLine()) != null) {
      stringBuilder.append(content);
    }

    String downloadedString = stringBuilder.toString();

    log.info("Downloaded File: {}", downloadedString);
    return downloadedString;
  }
}
