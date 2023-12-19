package io.micronaut.starter.analytics.postgres;

import io.micronaut.core.io.Writable;

import java.io.IOException;
import java.io.StringWriter;

public final class WritableUtils {

    private WritableUtils() {

    }

    public static String writeableToString(Writable writable) throws IOException {
        StringWriter sw = new StringWriter();
        writable.writeTo(sw);
        return sw.toString();
    }
}
