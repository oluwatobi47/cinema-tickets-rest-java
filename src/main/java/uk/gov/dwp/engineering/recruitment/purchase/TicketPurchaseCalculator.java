package uk.gov.dwp.engineering.recruitment.purchase;

import java.math.BigDecimal;
import java.util.Map;
import org.springframework.stereotype.Component;
import uk.gov.dwp.engineering.recruitment.domain.TicketRequest;
import uk.gov.dwp.engineering.recruitment.domain.TicketType;
import uk.gov.dwp.engineering.recruitment.exception.InvalidBookingException;

@Component
public class TicketPurchaseCalculator {

  private static final Map<TicketType, BigDecimal> TICKET_PRICES = Map.of(
      TicketType.INFANT, BigDecimal.ZERO,
      TicketType.CHILD, new BigDecimal("17.50"),
      TicketType.ADULT, new BigDecimal("25.99")
  );

  public TicketPurchaseSummary calculate(final TicketRequest... ticketRequests) {
    int adultTickets = 0;
    int childTickets = 0;
    int infantTickets = 0;
    BigDecimal paymentAmount = BigDecimal.ZERO;

    for (TicketRequest ticketRequest : ticketRequests) {
      paymentAmount = paymentAmount.add(ticketPrice(ticketRequest.type())
          .multiply(BigDecimal.valueOf(ticketRequest.ticketCount())));

      switch (ticketRequest.type()) {
        case ADULT -> adultTickets += ticketRequest.ticketCount();
        case CHILD -> childTickets += ticketRequest.ticketCount();
        case INFANT -> infantTickets += ticketRequest.ticketCount();
      }
    }

    final long seatCount = adultTickets + childTickets;

    return new TicketPurchaseSummary(adultTickets, childTickets, infantTickets, seatCount,
        paymentAmount);
  }

  private BigDecimal ticketPrice(final TicketType ticketType) {
    final BigDecimal price = TICKET_PRICES.get(ticketType);
    if (price == null) {
      throw new InvalidBookingException("Unsupported ticket type");
    }
    return price;
  }
}
