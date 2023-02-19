package br.com.siecola.products.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import br.com.siecola.products.model.Product;

public interface ProductRepository extends JpaRepository<Product, Long> {
  Optional<Product> findByCode(String code);
}
