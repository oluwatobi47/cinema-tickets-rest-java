package uk.gov.dwp.engineering.recruitment.purchase;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static uk.gov.dwp.engineering.recruitment.domain.TicketType.ADULT;
import static uk.gov.dwp.engineering.recruitment.domain.TicketType.CHILD;
import static uk.gov.dwp.engineering.recruitment.domain.TicketType.INFANT;

import java.math.BigDecimal;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import uk.gov.dwp.engineering.recruitment.domain.TicketRequest;

class TicketPurchaseCalculatorTest {

  private TicketPurchaseCalculator ticketPurchaseCalculator;

  @BeforeEach
  void setUp() {
    ticketPurchaseCalculator = new TicketPurchaseCalculator();
  }

  @Test
  void shouldReturnPurchaseSummary_whenTicketRequestsAreMixed() {
    final TicketPurchaseSummary ticketPurchaseSummary = ticketPurchaseCalculator.calculate(
        new TicketRequest(ADULT, 2),
        new TicketRequest(CHILD, 3),
        new TicketRequest(INFANT, 1));

    assertEquals(2, ticketPurchaseSummary.adultTickets());
    assertEquals(3, ticketPurchaseSummary.childTickets());
    assertEquals(1, ticketPurchaseSummary.infantTickets());
    assertEquals(5L, ticketPurchaseSummary.seatCount());
    assertEquals(new BigDecimal("104.48"), ticketPurchaseSummary.paymentAmount());
  }

  @Test
  void shouldReturnAdultPriceAndSeatCount_whenOnlyAdultTicketsAreRequested() {
    final TicketPurchaseSummary ticketPurchaseSummary = ticketPurchaseCalculator.calculate(
        new TicketRequest(ADULT, 1));

    assertEquals(1, ticketPurchaseSummary.adultTickets());
    assertEquals(0, ticketPurchaseSummary.childTickets());
    assertEquals(0, ticketPurchaseSummary.infantTickets());
    assertEquals(1L, ticketPurchaseSummary.seatCount());
    assertEquals(new BigDecimal("25.99"), ticketPurchaseSummary.paymentAmount());
  }
}
