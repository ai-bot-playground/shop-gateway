package com.shop.gateway;

import com.sun.net.httpserver.HttpServer;

import java.io.IOException;
import java.io.OutputStream;
import java.io.UncheckedIOException;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * One stub HTTP server per backend service, started once for the whole suite.
 * Each records the request paths it receives and answers 200 with its own name,
 * so a test can tell WHICH service a gateway route reached and with what path.
 */
public final class StubBackends {

    public static final List<String> SERVICES = List.of("shop-catalog", "shop-order", "shop-inventory");

    private static final Map<String, Stub> STUBS = SERVICES.stream()
            .collect(Collectors.toUnmodifiableMap(Function.identity(), Stub::start));

    private StubBackends() {
    }

    public static String uri(String service) {
        return "http://localhost:" + STUBS.get(service).server.getAddress().getPort();
    }

    public static List<String> pathsReceivedBy(String service) {
        return List.copyOf(STUBS.get(service).paths);
    }

    public static void reset() {
        STUBS.values().forEach(s -> s.paths.clear());
    }

    private static final class Stub {
        private final HttpServer server;
        private final List<String> paths = new CopyOnWriteArrayList<>();

        private Stub(HttpServer server) {
            this.server = server;
        }

        static Stub start(String name) {
            try {
                HttpServer server = HttpServer.create(new InetSocketAddress("localhost", 0), 0);
                Stub stub = new Stub(server);
                server.createContext("/", exchange -> {
                    stub.paths.add(exchange.getRequestURI().getPath());
                    byte[] body = name.getBytes(StandardCharsets.UTF_8);
                    exchange.sendResponseHeaders(200, body.length);
                    try (OutputStream out = exchange.getResponseBody()) {
                        out.write(body);
                    }
                });
                server.start();
                return stub;
            } catch (IOException e) {
                throw new UncheckedIOException(e);
            }
        }
    }
}
