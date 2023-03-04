package br.com.siecola.products.controller;

import br.com.siecola.products.model.UrlResponse;
import com.amazonaws.HttpMethod;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.GeneratePresignedUrlRequest;
import java.time.Duration;
import java.time.Instant;
import java.util.Date;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/invoices")
public class InvoiceController {

  @Value("${aws.s3.bucket.invoice.name}")
  private String bucketName;

  private AmazonS3 amazonS3;

  public InvoiceController(AmazonS3 amazonS3) {
    this.amazonS3 = amazonS3;
  }

  @PostMapping
  UrlResponse createInvoiceUrl() {
    var urlResponse = new UrlResponse();
    Instant expTime = Instant.now().plus(Duration.ofMinutes(5));
    String processId = UUID.randomUUID().toString();
    GeneratePresignedUrlRequest generatePresignedUrlRequest = new GeneratePresignedUrlRequest(
      bucketName,
      processId
    )
      .withMethod(HttpMethod.PUT)
      .withExpiration(Date.from(expTime));

    urlResponse.setExpirationTime(expTime.getEpochSecond());
    urlResponse.setUrl(
      amazonS3.generatePresignedUrl(generatePresignedUrlRequest).toString()
    );
    return urlResponse;
  }
}
