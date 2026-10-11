# Cinema Tickets Code Test

## Objective

To assess your ability to build a simple API.

The scenario and requirements will be provided separately.

## Implementation Approach

The implementation keeps `CinemaTicketsServiceImpl` as the orchestration point for a purchase. It
validates the incoming request, calculates the purchase details, validates the resulting ticket mix,
then calls the external payment and seat reservation services.

The flow is:

1. Validate the account id and ticket requests.
2. Calculate the ticket counts, payment amount, and seat count with `TicketPurchaseCalculator`.
   This returns an immutable `TicketPurchaseSummary` record.
3. Validate the calculated purchase summary.
4. Execute the payment for the account with the computed amount using the `PaymentService`.
5. Reserve seats with `SeatReservationService`.
6. Return a `BookingConfirmation` containing the account id, reserved seat count, and total cost.

The purchase calculation classes live in the `uk.gov.dwp.engineering.recruitment.purchase`
package. This keeps the calculation-specific code separate from the service orchestration code while
leaving the constrained `domain` package unchanged.

`TicketPurchaseCalculator` owns the ticket price registry and the calculation rules:

- INFANT tickets cost £0.00.
- CHILD tickets cost £17.50.
- ADULT tickets cost £25.99.
- INFANT tickets do not require seats.
- CHILD and ADULT tickets require seats.

`TicketPurchaseSummary` is used to pass the calculated counts, payment amount, and seat count back
to the service. It is not part of the API response.

`BookingConfirmation` has been extended in a backwards-compatible way to include `seatCount` and
`totalCost`. The original `BookingConfirmation(Long accountId)` constructor is still available, so
existing callers are not broken.

A successful booking response includes:

- `accountId`: the account used for the purchase;
- `seatCount`: the number of seats reserved, excluding INFANT tickets;
- `totalCost`: the amount charged, excluding INFANT tickets.

Invalid bookings are rejected with `InvalidBookingException`. The REST exception handler maps this
exception to HTTP 400 because these failures are caused by client requests that do not satisfy the
business rules.

## Validation Rules

The implementation rejects a booking when:

- the account id is missing or is not greater than zero;
- no ticket requests are supplied;
- any ticket request is null;
- any ticket type is missing;
- any ticket count is zero or negative;
- more than 25 tickets are requested in total;
- CHILD or INFANT tickets are requested without at least one ADULT ticket in the same booking.

The 25-ticket limit is based on the sum of all requested ticket counts, not the number of
`TicketRequest` entries. For example, one request entry for 26 ADULT tickets is rejected.

## Assumptions

The task states that all account ids greater than zero are valid and have sufficient funds, so this
implementation treats `accountId <= 0` or a missing account id as invalid.

The task does not define an adult-to-infant ratio. I have assumed that at least one ADULT ticket is
enough for a booking containing CHILD or INFANT tickets.

A ticket request with a count of zero is treated as invalid. Callers should omit ticket types they do
not want to purchase.

Third-party services are called only after validation succeeds. Their responses are not used for
additional validation because the task states that payment and seat reservation providers have no
defects and always complete once requested.

Additional implementation detail:

- Purchase validation is currently implemented as a private method in `CinemaTicketsServiceImpl`.
  If the purchase rules become more complex or need to be reused, they could be extracted into a
  dedicated validator.

## Testing

The test suite separates concerns in the same way as the implementation:

- `CinemaTicketsServiceImplTest` covers service orchestration and service-owned validation.
- `purchase/TicketPurchaseCalculatorTest` covers price, ticket count, and seat count calculations.
- `purchase/TicketPurchaseSummaryTest` follows the existing project style for simple record tests.
- `BookingConfirmationTest` covers the extended confirmation response fields.
- `CinemaTicketsControllerTest` covers the controller boundary: successful responses and invalid
  booking error mapping.

Business-rule validation is tested at service level, while controller tests verify request
delegation and error mapping for rejected bookings.

Run the tests with:

```bash
mvn test
```

The OpenAPI specification file was also updated to correctly reflect the new model fields.
