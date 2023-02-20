package br.com.siecola.products.model;

import br.com.siecola.products.enums.EventType;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class Envelope {

  private EventType eventType;
  private String data;
}
