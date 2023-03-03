package br.com.siecola.aws_project02.controller;

import br.com.siecola.aws_project02.model.ProductEventLogDTO;
import br.com.siecola.aws_project02.repository.ProductEventLogRepository;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/events")
public class ProductEventLogController {

  private final ProductEventLogRepository productEventLogRepository;

  public ProductEventLogController(
    ProductEventLogRepository productEventLogRepository
  ) {
    this.productEventLogRepository = productEventLogRepository;
  }

  @GetMapping
  List<ProductEventLogDTO> getAllEvents() {
    return StreamSupport
      .stream(productEventLogRepository.findAll().spliterator(), false)
      .map(ProductEventLogDTO::new)
      .collect(Collectors.toList());
  }

  @GetMapping("/{code}")
  List<ProductEventLogDTO> findByCode(@PathVariable String code) {
    return productEventLogRepository
      .findAllByPk(code)
      .stream()
      .map(ProductEventLogDTO::new)
      .collect(Collectors.toList());
  }

  @GetMapping("/{code}/{event}")
  List<ProductEventLogDTO> findByCodeAndEventType(
    @PathVariable String code,
    @PathVariable String event
  ) {
    return productEventLogRepository
      .findAllByPkAndSkStartsWith(code, event)
      .stream()
      .map(ProductEventLogDTO::new)
      .collect(Collectors.toList());
  }
}
