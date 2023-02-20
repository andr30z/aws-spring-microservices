package br.com.siecola.products.model;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ProductEvent {

  private String code;
  private Long productId;
  private String username;
}
