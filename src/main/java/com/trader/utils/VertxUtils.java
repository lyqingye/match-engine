package com.trader.utils;

import io.vertx.core.Vertx;

public class VertxUtils {
    private static final Vertx vertx = Vertx.vertx();

    public static Vertx vertx() {
        return vertx;
    }
}
