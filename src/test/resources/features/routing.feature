Feature: API gateway routing
  The gateway is the only public entry point and routes /api/* to backend
  services, stripping the /api prefix.

  Scenario Outline: Public paths are routed to the right service
    When a request hits "<path>"
    Then it is forwarded to "<service>" with the /api prefix stripped

    Examples:
      | path              | service        |
      | /api/products/1   | shop-catalog   |
      | /api/orders       | shop-order     |
      | /api/inventory/P1 | shop-inventory |

  @future
  Scenario: Excessive requests are rate limited
    Given a single client has exceeded the configured rate limit
    When the client sends another request
    Then the response status is 429
