package uk.gov.dwp.engineering.recruitment.purchase;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.math.BigDecimal;
import org.junit.jupiter.api.Test;

class TicketPurchaseSummaryTest {

  @Test
  void shouldReturnExpectedValues_whenPurchaseSummaryIsCreated() {
    final TicketPurchaseSummary ticketPurchaseSummary = new TicketPurchaseSummary(2, 3, 1, 5L,
        new BigDecimal("104.48"));

    assertEquals(2, ticketPurchaseSummary.adultTickets());
    assertEquals(3, ticketPurchaseSummary.childTickets());
    assertEquals(1, ticketPurchaseSummary.infantTickets());
    assertEquals(5L, ticketPurchaseSummary.seatCount());
    assertEquals(new BigDecimal("104.48"), ticketPurchaseSummary.paymentAmount());
  }
}
