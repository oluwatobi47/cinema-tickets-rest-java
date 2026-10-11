package uk.gov.dwp.engineering.recruitment;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import uk.gov.dwp.engineering.recruitment.domain.BookingConfirmation;
import uk.gov.dwp.engineering.recruitment.domain.TicketRequest;
import uk.gov.dwp.engineering.recruitment.exception.InvalidBookingException;
import uk.gov.dwp.engineering.recruitment.exception.RestExceptionHandler;

class CinemaTicketsControllerTest {

  private CinemaTicketsService cinemaTicketsService;

  private MockMvc mockMvc;

  @BeforeEach
  void setUp() {
    cinemaTicketsService = mock(CinemaTicketsService.class);

    final CinemaTicketsController controller = new CinemaTicketsController(cinemaTicketsService);

    mockMvc = MockMvcBuilders.standaloneSetup(controller)
        .setControllerAdvice(new RestExceptionHandler())
        .build();
  }

  @Test
  void shouldReturnCreatedResponse_whenBookingIsValid() throws Exception {
    when(cinemaTicketsService.purchaseTickets(eq(99L), any(TicketRequest[].class)))
        .thenReturn(new BookingConfirmation(99L));

    mockMvc.perform(post("/cinema/bookings")
            .contentType(MediaType.APPLICATION_JSON)
            .content("""
                {
                  "accountId": 99,
                  "ticketRequests": [
                    {"type": "ADULT", "ticketCount": 2},
                    {"type": "CHILD", "ticketCount": 1},
                    {"type": "INFANT", "ticketCount": 1}
                  ]
                }
                """))
        .andExpect(status().isCreated())
        .andExpect(jsonPath("$.accountId").value(99));

    verify(cinemaTicketsService).purchaseTickets(eq(99L), any(TicketRequest[].class));
  }

  @Test
  void shouldReturnBadRequestResponse_whenBookingIsInvalid() throws Exception {
    final String expectedDetail = "Ticket count must be greater than zero";
    when(cinemaTicketsService.purchaseTickets(any(), any(TicketRequest[].class)))
        .thenThrow(new InvalidBookingException(expectedDetail));

    mockMvc.perform(post("/cinema/bookings")
            .contentType(MediaType.APPLICATION_JSON)
            .content("""
                {
                  "accountId": 99,
                  "ticketRequests": [{"type": "ADULT", "ticketCount": 0}]
                }
                """))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.detail").value(expectedDetail));
  }
}
