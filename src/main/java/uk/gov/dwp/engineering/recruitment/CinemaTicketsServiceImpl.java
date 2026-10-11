package uk.gov.dwp.engineering.recruitment;

import org.springframework.stereotype.Service;
import uk.gov.dwp.engineering.recruitment.domain.BookingConfirmation;
import uk.gov.dwp.engineering.recruitment.domain.TicketRequest;
import uk.gov.dwp.engineering.recruitment.exception.InvalidBookingException;
import uk.gov.dwp.engineering.recruitment.purchase.TicketPurchaseCalculator;
import uk.gov.dwp.engineering.recruitment.purchase.TicketPurchaseSummary;
import uk.gov.dwp.engineering.recruitment.thirdparty.PaymentService;
import uk.gov.dwp.engineering.recruitment.thirdparty.SeatReservationService;

@Service
public class CinemaTicketsServiceImpl implements CinemaTicketsService {

  private static final int MAX_TICKETS_PER_BOOKING = 25;

  private final PaymentService paymentService;

  private final SeatReservationService seatReservationService;

  private final TicketPurchaseCalculator ticketPurchaseCalculator;

  public CinemaTicketsServiceImpl(PaymentService paymentService,
      SeatReservationService seatReservationService,
      TicketPurchaseCalculator ticketPurchaseCalculator) {
    this.paymentService = paymentService;
    this.seatReservationService = seatReservationService;
    this.ticketPurchaseCalculator = ticketPurchaseCalculator;
  }

  @Override
  public BookingConfirmation purchaseTickets(final Long accountId,
      final TicketRequest... ticketRequests)
      throws InvalidBookingException {

    validateTicketRequests(accountId, ticketRequests);

    final TicketPurchaseSummary ticketPurchaseSummary = ticketPurchaseCalculator.calculate(
        ticketRequests);

    validateTicketPurchase(ticketPurchaseSummary);

    paymentService.debitAccount(accountId, ticketPurchaseSummary.paymentAmount());
    seatReservationService.reserveSeats(accountId, ticketPurchaseSummary.seatCount());

    return new BookingConfirmation(accountId, ticketPurchaseSummary.seatCount(), ticketPurchaseSummary.paymentAmount());
  }

  private void validateTicketRequests(final Long accountId, final TicketRequest[] ticketRequests) {
    if (accountId == null || accountId <= 0) {
      throw new InvalidBookingException("Account id must be greater than zero");
    }
    if (ticketRequests == null || ticketRequests.length == 0) {
      throw new InvalidBookingException("At least one ticket request is required");
    }

    int totalTicketCount = 0;

    for (TicketRequest ticketRequest : ticketRequests) {
      validateTicketRequest(ticketRequest);
      totalTicketCount += ticketRequest.ticketCount();
    }

    validateTicketLimit(totalTicketCount);
  }

  private void validateTicketPurchase(final TicketPurchaseSummary ticketPurchaseSummary) {
    if (ticketPurchaseSummary.adultTickets() == 0
            && (ticketPurchaseSummary.childTickets() > 0
            || ticketPurchaseSummary.infantTickets() > 0)) {
      throw new InvalidBookingException(
              "Child and infant tickets cannot be purchased without an adult ticket");
    }
  }

  private void validateTicketRequest(final TicketRequest ticketRequest) {
    if (ticketRequest == null) {
      throw new InvalidBookingException("Ticket request must not be null");
    }
    if (ticketRequest.type() == null) {
      throw new InvalidBookingException("Ticket type must be provided");
    }
    if (ticketRequest.ticketCount() <= 0) {
      throw new InvalidBookingException("Ticket count must be greater than zero");
    }
  }

  private void validateTicketLimit(final int totalTickets) {
    if (totalTickets > MAX_TICKETS_PER_BOOKING) {
      throw new InvalidBookingException(
          "A maximum of " + MAX_TICKETS_PER_BOOKING + " tickets can be purchased at a time");
    }
  }

}
