package uk.gov.dwp.engineering.recruitment.domain;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.math.BigDecimal;
import org.junit.jupiter.api.Test;

class BookingConfirmationTest {

  @Test
  void givenBookingConfirmation_whenGetAccount_thenExpectedValueReturned() {
    final BookingConfirmation bookingConfirmation = new BookingConfirmation(99L);
    assertEquals(99L, bookingConfirmation.accountId());
  }

  @Test
  void shouldReturnExpectedValues_whenExtendedBookingConfirmationIsCreated() {
    final BookingConfirmation bookingConfirmation = new BookingConfirmation(99L, 2L,
        new BigDecimal("43.49"));

    assertEquals(99L, bookingConfirmation.accountId());
    assertEquals(2L, bookingConfirmation.seatCount());
    assertEquals(new BigDecimal("43.49"), bookingConfirmation.totalCost());
  }
}
