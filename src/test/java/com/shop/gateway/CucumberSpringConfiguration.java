package com.shop.gateway;

import io.cucumber.spring.CucumberContextConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;

/**
 * Boots the real gateway (routes from application.yml) on a random port, with the
 * backend URIs pointed at {@link StubBackends}. Shared Spring context for all steps.
 */
@CucumberContextConfiguration
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class CucumberSpringConfiguration {

    @DynamicPropertySource
    static void backends(DynamicPropertyRegistry registry) {
        registry.add("CATALOG_SERVICE_URI", () -> StubBackends.uri("shop-catalog"));
        registry.add("ORDER_SERVICE_URI", () -> StubBackends.uri("shop-order"));
        registry.add("INVENTORY_SERVICE_URI", () -> StubBackends.uri("shop-inventory"));
    }
}
