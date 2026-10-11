package uk.gov.dwp.engineering.recruitment.purchase;

import java.math.BigDecimal;

public record TicketPurchaseSummary(int adultTickets, int childTickets, int infantTickets, long seatCount,
    BigDecimal paymentAmount) {

}
