# shop-gateway

Reaktywny API Gateway (Spring Cloud Gateway 2025.1 / Spring Boot 4 / Netty) — jedyny publiczny punkt wejścia do backendu.

## Stack

- Java 25, Spring Boot 4.0.7, Spring Cloud Gateway 2025.1.2 (WebFlux)
- Budowanie: Gradle 9.6, multi-stage Docker (`gradle:9.6.0-jdk25` → `eclipse-temurin:25-jre-alpine`)
- Obserwowalność: Actuator (`/actuator/health`, `/actuator/info`)

## Routing

`StripPrefix=1` na każdej trasie (usuwa `/api` przed forwardem).

| Ścieżka             | Env var               | Domyślny cel               |
|---------------------|-----------------------|----------------------------|
| `/api/products/**`  | `CATALOG_SERVICE_URI` | `http://shop-catalog:8080` |
| `/api/orders/**`    | `ORDER_SERVICE_URI`   | `http://shop-order:8080`   |
| `/api/inventory/**` | `INVENTORY_SERVICE_URI` | `http://shop-inventory:8080` |

## Uruchomienie

```bash
# lokalnie
./gradlew bootRun

# Docker
docker build -t shop-gateway:local .
docker run -p 8080:8080 shop-gateway:local
```

## Kubernetes (preprod)

Namespace `shop`, ClusterIP na porcie 8080, health probe na `/actuator/health`.

```bash
kubectl --context kind-preprod apply -f k8s/shop-gateway.yaml
```

## CI

Każdy PR uruchamia preprod gate (`.github/workflows/pr-to-main.yml`) — buduje obraz, wdraża na `kind-preprod` i odpala acceptance testy z `shop-acceptance-tests`.
