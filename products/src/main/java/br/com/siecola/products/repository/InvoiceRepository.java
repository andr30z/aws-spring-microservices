package br.com.siecola.products.repository;

import br.com.siecola.products.model.Invoice;
import java.util.Optional;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface InvoiceRepository extends JpaRepository<Invoice, Long> {
  Optional<Invoice> findByInvoiceNumber(String invoiceNumber);

  List<Invoice> findAllByCustomerName(String customerName);
}
