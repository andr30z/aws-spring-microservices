package br.com.siecola.products.controller;

import br.com.siecola.products.model.Invoice;
import br.com.siecola.products.model.UrlResponse;
import br.com.siecola.products.repository.InvoiceRepository;

import com.amazonaws.HttpMethod;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.GeneratePresignedUrlRequest;
import java.time.Duration;
import java.time.Instant;
import java.util.Date;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/invoices")
public class InvoiceController {

  @Value("${aws.s3.bucket.invoice.name}")
  private String bucketName;

  private final AmazonS3 amazonS3;

  private final InvoiceRepository invoiceRepository;

  public InvoiceController(AmazonS3 amazonS3, InvoiceRepository invoiceRepository) {
    this.amazonS3 = amazonS3;
    this.invoiceRepository = invoiceRepository;
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

  @GetMapping 
  Iterable<Invoice> findAll(){
    return invoiceRepository.findAll();
  }

  @GetMapping(path = "/customer/{customerName}")
  Iterable<Invoice> findByCustomerName(@PathVariable  String customerName){
    return invoiceRepository.findAllByCustomerName(customerName);
  }

}
