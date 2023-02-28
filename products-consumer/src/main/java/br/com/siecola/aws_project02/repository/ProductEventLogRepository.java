package br.com.siecola.aws_project02.repository;

import java.util.List;

import org.springframework.data.repository.CrudRepository;
import br.com.siecola.aws_project02.model.*;

public interface ProductEventLogRepository extends CrudRepository<ProductEventLog, ProductEventKey> {
    List<ProductEventLog> findAllByPk(String code);
    List<ProductEventLog> findAllByPkAndSkStartsWith(String code, String eventType);
    
}
