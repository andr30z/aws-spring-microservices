package br.com.siecola.products.controller;

import br.com.siecola.products.enums.*;
import br.com.siecola.products.model.Product;
import br.com.siecola.products.repository.ProductRepository;
import br.com.siecola.products.service.ProductPublisher;
import java.util.List;
import java.util.Optional;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/products")
public class ProductController {

  private final ProductRepository productRepository;
  private final ProductPublisher productPublisher;

  public ProductController(
    ProductRepository productRepository,
    ProductPublisher productPublisher
  ) {
    this.productRepository = productRepository;
    this.productPublisher = productPublisher;
  }

  @GetMapping
  List<Product> getAll() {
    return this.productRepository.findAll();
  }

  @GetMapping("/{id}")
  ResponseEntity<Product> getById(@PathVariable Long id) {
    Optional<Product> product = this.productRepository.findById(id);
    return !product.isEmpty()
      ? new ResponseEntity<Product>(product.get(), HttpStatus.OK)
      : new ResponseEntity<Product>(HttpStatus.NOT_FOUND);
  }

  @PostMapping
  public ResponseEntity<Product> saveProduct(@RequestBody Product product) {
    Product productCreated = productRepository.save(product);
    productPublisher.publishProductEvent(
      productCreated,
      EventType.PRODUCT_CREATED,
      "service01-create-product"
    );
    return new ResponseEntity<Product>(productCreated, HttpStatus.CREATED);
  }

  @PutMapping(path = "/{id}")
  public ResponseEntity<Product> updateProduct(
    @RequestBody Product product,
    @PathVariable("id") long id
  ) {
    if (productRepository.existsById(id)) {
      product.setId(id);

      Product productUpdated = productRepository.save(product);
      productPublisher.publishProductEvent(
        productUpdated,
        EventType.PRODUCT_UPDATE,
        "service01-update-product"
      );
      return new ResponseEntity<Product>(productUpdated, HttpStatus.OK);
    } else {
      return new ResponseEntity<>(HttpStatus.NOT_FOUND);
    }
  }

  @DeleteMapping(path = "/{id}")
  public ResponseEntity<Product> deleteProduct(@PathVariable("id") long id) {
    Optional<Product> optProduct = productRepository.findById(id);
    if (optProduct.isPresent()) {
      Product product = optProduct.get();

      productRepository.delete(product);
      productPublisher.publishProductEvent(
        product,
        EventType.PRODUCT_UPDATE,
        "service01-delete-product"
      );
      return new ResponseEntity<Product>(product, HttpStatus.OK);
    } else {
      return new ResponseEntity<>(HttpStatus.NOT_FOUND);
    }
  }

  @GetMapping(path = "/code")
  public ResponseEntity<Product> findByCode(@RequestParam String code) {
    Optional<Product> optProduct = productRepository.findByCode(code);
    if (optProduct.isPresent()) {
      return new ResponseEntity<Product>(optProduct.get(), HttpStatus.OK);
    } else {
      return new ResponseEntity<>(HttpStatus.NOT_FOUND);
    }
  }
}
