package uk.gov.dwp.engineering.recruitment;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static uk.gov.dwp.engineering.recruitment.domain.TicketType.ADULT;
import static uk.gov.dwp.engineering.recruitment.domain.TicketType.CHILD;
import static uk.gov.dwp.engineering.recruitment.domain.TicketType.INFANT;

import java.math.BigDecimal;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import uk.gov.dwp.engineering.recruitment.domain.BookingConfirmation;
import uk.gov.dwp.engineering.recruitment.domain.TicketRequest;
import uk.gov.dwp.engineering.recruitment.exception.InvalidBookingException;
import uk.gov.dwp.engineering.recruitment.purchase.TicketPurchaseCalculator;
import uk.gov.dwp.engineering.recruitment.purchase.TicketPurchaseSummary;
import uk.gov.dwp.engineering.recruitment.thirdparty.PaymentService;
import uk.gov.dwp.engineering.recruitment.thirdparty.SeatReservationService;

class CinemaTicketsServiceImplTest {

  private PaymentService paymentService;

  private SeatReservationService seatReservationService;

  private TicketPurchaseCalculator ticketPurchaseCalculator;

  private CinemaTicketsService cinemaTicketsService;

  @BeforeEach
  void setUp() {
    paymentService = mock(PaymentService.class);
    seatReservationService = mock(SeatReservationService.class);
    ticketPurchaseCalculator = mock(TicketPurchaseCalculator.class);
    cinemaTicketsService = new CinemaTicketsServiceImpl(paymentService, seatReservationService,
        ticketPurchaseCalculator);
  }

  @Nested
  class ValidBookings {

    @Test
    void shouldTakePaymentAndReserveSeats_whenTicketRequestsAreValid() {
      final TicketRequest[] ticketRequests = {
          new TicketRequest(ADULT, 2),
          new TicketRequest(CHILD, 3),
          new TicketRequest(INFANT, 1)
      };
      when(ticketPurchaseCalculator.calculate(ticketRequests)).thenReturn(
          new TicketPurchaseSummary(2, 3, 1, 5L, new BigDecimal("104.48")));

      final BookingConfirmation bookingConfirmation = cinemaTicketsService.purchaseTickets(123L,
          ticketRequests);

      assertEquals(123L, bookingConfirmation.accountId());
      verify(ticketPurchaseCalculator).calculate(ticketRequests);
      verify(paymentService).debitAccount(123L, new BigDecimal("104.48"));
      verify(seatReservationService).reserveSeats(123L, 5L);
    }
  }

  @Nested
  class RequestValidation {

    @Test
    void shouldRejectBooking_whenAccountIdIsInvalid() {
      assertInvalidRequestBooking("Account id must be greater than zero", 0L,
          new TicketRequest(ADULT, 1));
    }

    @Test
    void shouldRejectBooking_whenTicketRequestsAreMissing() {
      assertInvalidRequestBooking("At least one ticket request is required", 123L);
    }

    @Test
    void shouldRejectBooking_whenTicketRequestIsNull() {
      assertInvalidRequestBooking("Ticket request must not be null", 123L,
          (TicketRequest) null);
    }

    @Test
    void shouldRejectBooking_whenTicketTypeIsMissing() {
      assertInvalidRequestBooking("Ticket type must be provided", 123L,
          new TicketRequest(null, 1));
    }

    @Test
    void shouldRejectBooking_whenTicketCountIsZero() {
      assertInvalidRequestBooking("Ticket count must be greater than zero", 123L,
          new TicketRequest(ADULT, 0));
    }

    @Test
    void shouldRejectBooking_whenMoreThanTwentyFiveTicketsAreRequested() {
      assertInvalidRequestBooking("A maximum of 25 tickets can be purchased at a time", 123L,
          new TicketRequest(ADULT, 26));
    }

    @Test
    void shouldRejectBooking_whenMoreThanTwentyFiveTicketsIncludingInfantsAreRequested() {
      assertInvalidRequestBooking("A maximum of 25 tickets can be purchased at a time", 123L,
          new TicketRequest(ADULT, 1), new TicketRequest(INFANT, 25));
    }
  }

  @Nested
  class PurchaseValidation {

    @Test
    void shouldRejectBooking_whenChildTicketsAreRequestedWithoutAdult() {
      final TicketRequest[] ticketRequests = {new TicketRequest(CHILD, 1)};
      when(ticketPurchaseCalculator.calculate(ticketRequests)).thenReturn(
          new TicketPurchaseSummary(0, 1, 0, 1L, new BigDecimal("17.50")));

      assertInvalidPurchaseBooking(
              123L,
          ticketRequests);

      verify(ticketPurchaseCalculator).calculate(ticketRequests);
    }

    @Test
    void shouldRejectBooking_whenInfantTicketsAreRequestedWithoutAdult() {
      final TicketRequest[] ticketRequests = {new TicketRequest(INFANT, 1)};
      when(ticketPurchaseCalculator.calculate(ticketRequests)).thenReturn(
          new TicketPurchaseSummary(0, 0, 1, 0L, BigDecimal.ZERO));

      assertInvalidPurchaseBooking(
              123L,
          ticketRequests);

      verify(ticketPurchaseCalculator).calculate(ticketRequests);
    }
  }

  private void assertInvalidRequestBooking(final String expectedMessage, final Long accountId,
      final TicketRequest... ticketRequests) {
    final InvalidBookingException exception = assertThrows(InvalidBookingException.class,
        () -> cinemaTicketsService.purchaseTickets(accountId, ticketRequests));

    assertEquals(expectedMessage, exception.getMessage());
    verifyNoInteractions(ticketPurchaseCalculator, paymentService, seatReservationService);
  }

  private void assertInvalidPurchaseBooking(final Long accountId,
                                            final TicketRequest... ticketRequests) {
    final InvalidBookingException exception = assertThrows(InvalidBookingException.class,
        () -> cinemaTicketsService.purchaseTickets(accountId, ticketRequests));

    assertEquals("Child and infant tickets cannot be purchased without an adult ticket", exception.getMessage());
    verifyNoInteractions(paymentService, seatReservationService);
  }
}
