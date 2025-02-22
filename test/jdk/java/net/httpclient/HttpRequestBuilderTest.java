/*
 * Copyright (c) 2016, 2025, Oracle and/or its affiliates. All rights reserved.
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER.
 *
 * This code is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License version 2 only, as
 * published by the Free Software Foundation.
 *
 * This code is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License
 * version 2 for more details (a copy is included in the LICENSE file that
 * accompanied this code).
 *
 * You should have received a copy of the GNU General Public License version
 * 2 along with this work; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 * Please contact Oracle, 500 Oracle Parkway, Redwood Shores, CA 94065 USA
 * or visit www.oracle.com if you need additional information or have any
 * questions.
 */

import java.net.URI;
import java.net.http.HttpClient;
import java.time.Duration;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.net.http.HttpRequest;
import static java.net.http.HttpRequest.BodyPublishers.ofString;
import static java.net.http.HttpRequest.BodyPublishers.noBody;

/*
 * @test
 * @bug 8170064 8276559
 * @summary HttpRequest[.Builder] API and behaviour checks
 */

public class HttpRequestBuilderTest {

    static final URI TEST_URI = URI.create("http://www.foo.com/");


    public static void main(String[] args) throws Exception {

        test0("newBuilder().build()",
              () -> HttpRequest.newBuilder().build(),
              IllegalStateException.class);

        test0("newBuilder(null)",
              () -> HttpRequest.newBuilder(null),
              NullPointerException.class);

        test0("newBuilder(URI.create(\"badScheme://www.foo.com/\")",
              () -> HttpRequest.newBuilder(URI.create("badScheme://www.foo.com/")),
              IllegalArgumentException.class);

        test0("newBuilder(URI.create(\"http://www.foo.com:-1/\")",
                () -> HttpRequest.newBuilder(URI.create("http://www.foo.com:-1/")),
                IllegalArgumentException.class);

        test0("newBuilder(URI.create(\"https://www.foo.com:-1/\")",
                () -> HttpRequest.newBuilder(URI.create("https://www.foo.com:-1/")),
                IllegalArgumentException.class);

        test0("newBuilder(" + TEST_URI + ").uri(null)",
              () -> HttpRequest.newBuilder(TEST_URI).uri(null),
              NullPointerException.class);

        test0("newBuilder(uri).build()",
              () -> HttpRequest.newBuilder(TEST_URI).build()
              /* no expected exceptions */ );

        HttpRequest.Builder builder = HttpRequest.newBuilder();

        builder = test1("uri", builder, builder::uri, (URI)null,
                        NullPointerException.class);

        builder = test1("uri", builder, builder::uri, URI.create("http://www.foo.com:-1/"),
                        IllegalArgumentException.class);

        builder = test1("uri", builder, builder::uri, URI.create("https://www.foo.com:-1/"),
                        IllegalArgumentException.class);

        builder = test2("header", builder, builder::header, (String) null, "bar",
                        NullPointerException.class);

        builder = test2("header", builder, builder::header, "foo", (String) null,
                        NullPointerException.class);

        builder = test2("header", builder, builder::header, (String)null,
                        (String) null, NullPointerException.class);

        builder = test2("header", builder, builder::header, "", "bar",
                        IllegalArgumentException.class);

        builder = test2("header", builder, builder::header, "foo", "\r",
                        IllegalArgumentException.class);

        builder = test1("headers", builder, builder::headers, (String[]) null,
                        NullPointerException.class);

        builder = test1("headers", builder, builder::headers, new String[0],
                        IllegalArgumentException.class);

        builder = test1("headers", builder, builder::headers,
                        (String[]) new String[] {null, "bar"},
                        NullPointerException.class);

        builder = test1("headers", builder, builder::headers,
                        (String[]) new String[] {"foo", null},
                        NullPointerException.class);

        builder = test1("headers", builder, builder::headers,
                        (String[]) new String[] {null, null},
                        NullPointerException.class);

        builder = test1("headers", builder, builder::headers,
                        (String[]) new String[] {"foo", "bar", null},
                        NullPointerException.class,
                        IllegalArgumentException.class);

        builder = test1("headers", builder, builder::headers,
                        (String[]) new String[] {"foo", "bar", null, null},
                        NullPointerException.class);

        builder = test1("headers", builder, builder::headers,
                        (String[]) new String[] {"foo", "bar", "baz", null},
                        NullPointerException.class);

        builder = test1("headers", builder, builder::headers,
                        (String[]) new String[] {"foo", "bar", "\r", "baz"},
                        IllegalArgumentException.class);

        builder = test1("headers", builder, builder::headers,
                        (String[]) new String[] {"foo", "bar", "baz", "\n"},
                        IllegalArgumentException.class);

        builder = test1("headers", builder, builder::headers,
                        (String[]) new String[] {"foo", "bar", "", "baz"},
                        IllegalArgumentException.class);

        builder = test1("headers", builder, builder::headers,
                        (String[]) new String[] {"foo", "bar", null, "baz"},
                        NullPointerException.class);

        builder = test1("headers", builder, builder::headers,
                        (String[]) new String[] {"foo", "bar", "baz"},
                        IllegalArgumentException.class);

        builder = test1("headers", builder, builder::headers,
                        (String[]) new String[] {"foo"},
                        IllegalArgumentException.class);

        test0("DELETE", () -> HttpRequest.newBuilder(TEST_URI).DELETE().build(), null);
        test0("HEAD", () -> HttpRequest.newBuilder(TEST_URI).HEAD().build(), null);

        builder = test1("POST", builder, builder::POST,
                        noBody(), null);

        builder = test1("PUT", builder, builder::PUT,
                        noBody(), null);

        builder = test2("method", builder, builder::method, "GET",
                        noBody(), null);

        builder = test1("POST", builder, builder::POST,
                        (HttpRequest.BodyPublisher)null,
                        NullPointerException.class);

        builder = test1("PUT", builder, builder::PUT,
                        (HttpRequest.BodyPublisher)null,
                        NullPointerException.class);

        builder = test2("method", builder, builder::method, "GET",
                        (HttpRequest.BodyPublisher) null,
                        NullPointerException.class);

        builder = test2("setHeader", builder, builder::setHeader,
                        (String) null, "bar",
                        NullPointerException.class);

        builder = test2("setHeader", builder, builder::setHeader,
                        "foo", (String) null,
                        NullPointerException.class);

        builder = test2("setHeader", builder, builder::setHeader,
                        (String)null, (String) null,
                        NullPointerException.class);

        builder = test1("timeout", builder, builder::timeout,
                        (Duration)null,
                        NullPointerException.class);

        builder = test1("version", builder, builder::version,
                        (HttpClient.Version)null,
                        NullPointerException.class);

        builder = test2("method", builder, builder::method, null,
                        ofString("foo"),
                        NullPointerException.class);
// see JDK-8170093
//
//        builder = test2("method", builder, builder::method, "foo",
//                       HttpRequest.BodyProcessor.ofString("foo"),
//                       IllegalArgumentException.class);
//
//        builder.build();


        method("newBuilder(TEST_URI).build().method() == GET",
               () -> HttpRequest.newBuilder(TEST_URI),
               "GET");

        method("newBuilder(TEST_URI).GET().build().method() == GET",
               () -> HttpRequest.newBuilder(TEST_URI).GET(),
               "GET");

        method("newBuilder(TEST_URI).POST(ofString(\"\")).GET().build().method() == GET",
               () -> HttpRequest.newBuilder(TEST_URI).POST(ofString("")).GET(),
               "GET");

        method("newBuilder(TEST_URI).PUT(ofString(\"\")).GET().build().method() == GET",
               () -> HttpRequest.newBuilder(TEST_URI).PUT(ofString("")).GET(),
               "GET");

        method("newBuilder(TEST_URI).DELETE().GET().build().method() == GET",
               () -> HttpRequest.newBuilder(TEST_URI).DELETE().GET(),
               "GET");

        method("newBuilder(TEST_URI).POST(ofString(\"\")).build().method() == POST",
               () -> HttpRequest.newBuilder(TEST_URI).POST(ofString("")),
               "POST");

        method("newBuilder(TEST_URI).PUT(ofString(\"\")).build().method() == PUT",
               () -> HttpRequest.newBuilder(TEST_URI).PUT(ofString("")),
               "PUT");

        method("newBuilder(TEST_URI).DELETE().build().method() == DELETE",
               () -> HttpRequest.newBuilder(TEST_URI).DELETE(),
               "DELETE");

        method("newBuilder(TEST_URI).GET().POST(ofString(\"\")).build().method() == POST",
               () -> HttpRequest.newBuilder(TEST_URI).GET().POST(ofString("")),
               "POST");

        method("newBuilder(TEST_URI).GET().PUT(ofString(\"\")).build().method() == PUT",
               () -> HttpRequest.newBuilder(TEST_URI).GET().PUT(ofString("")),
               "PUT");

        method("newBuilder(TEST_URI).GET().DELETE().build().method() == DELETE",
               () -> HttpRequest.newBuilder(TEST_URI).GET().DELETE(),
               "DELETE");

        method("newBuilder(TEST_URI).HEAD().build().method() == HEAD",
                () -> HttpRequest.newBuilder(TEST_URI).HEAD(),
                "HEAD");

        // verify that the default HEAD() method implementation in HttpRequest.Builder
        // interface works as expected
        HttpRequest defaultHeadReq = new NotOverriddenHEADImpl().HEAD().uri(TEST_URI).build();
        assertEquals("HEAD", defaultHeadReq.method(), "Method");
        assertEquals(false, defaultHeadReq.bodyPublisher().isEmpty(), "Body publisher absence");

        verifyCopy();

    }

    private static boolean shouldFail(Class<? extends Exception> ...exceptions) {
        return exceptions != null && exceptions.length > 0;
    }

    private static String expectedNames(Class<? extends Exception> ...exceptions) {
        return Stream.of(exceptions).map(Class::getSimpleName)
                .collect(Collectors.joining("|"));
    }
    private static boolean isExpected(Exception x,
                                     Class<? extends Exception> ...expected) {
        return expected != null && Stream.of(expected)
                .filter(c -> c.isInstance(x))
                .findAny().isPresent();
    }

    static void method(String name,
                       Supplier<HttpRequest.Builder> supplier,
                       String expectedMethod) {
        HttpRequest request = supplier.get().build();
        String method = request.method();
        if (request.method().equals("GET") && request.bodyPublisher().isPresent())
            throw new AssertionError("failed: " + name
                    + ". Unexpected body processor for GET: "
                    + request.bodyPublisher().get());
        assertEquals(expectedMethod, method, "Method");
    }

    static void test0(String name,
                      Runnable r,
                      Class<? extends Exception> ...ex) {
        try {
            r.run();
            if (!shouldFail(ex)) {
                System.out.println("success: " + name);
                return;
            } else {
                throw new AssertionError("Expected " + expectedNames(ex)
                        + " not raised for " + name);
            }
        } catch (Exception x) {
            if (!isExpected(x, ex)) {
                throw x;
            } else {
                System.out.println("success: " + name +
                        " - Got expected exception: " + x);
            }
        }
    }

    public static <R,P> R test1(String name, R receiver, Function<P, R> m, P arg,
                               Class<? extends Exception> ...ex) {
        String argMessage = arg == null ? "null" : arg.toString();
        if (arg instanceof String[]) {
            argMessage = Arrays.asList((String[])arg).toString();
        }
        try {
            R result =  m.apply(arg);
            if (!shouldFail(ex)) {
                System.out.println("success: " + name + "(" + argMessage + ")");
                return result;
            } else {
                throw new AssertionError("Expected " + expectedNames(ex)
                    + " not raised for " + name + "(" + argMessage + ")");
            }
        } catch (Exception x) {
            if (!isExpected(x, ex)) {
                throw x;
            } else {
                System.out.println("success: " + name + "(" + argMessage + ")" +
                        " - Got expected exception: " + x);
                return receiver;
            }
        }
    }


    public static <R,P1, P2> R test2(String name, R receiver, BiFunction<P1, P2, R> m,
                               P1 arg1, P2 arg2,
                               Class<? extends Exception> ...ex) {
        try {
            R result =  m.apply(arg1, arg2);
            if (!shouldFail(ex)) {
                System.out.println("success: " + name + "(" + arg1 + ", "
                                   + arg2 + ")");
                return result;
            } else {
                throw new AssertionError("Expected " + expectedNames(ex)
                    + " not raised for "
                    + name + "(" + arg1 +", " + arg2 + ")");
            }
        } catch (Exception x) {
            if (!isExpected(x, ex)) {
                throw x;
            } else {
                System.out.println("success: " + name + "(" + arg1 + ", "
                        + arg2 + ") - Got expected exception: " + x);
                return receiver;
            }
        }
    }

    private static void verifyCopy() {

        // Create the request builder
        HttpRequest.Builder requestBuilder = HttpRequest
                .newBuilder(TEST_URI)
                .header("X-Foo", "1")
                .method("GET", noBody())
                .expectContinue(true)
                .timeout(Duration.ofSeconds(0xBEEF))
                .version(HttpClient.Version.HTTP_2);

        // Create the original and the _copy_ requests
        HttpRequest request = requestBuilder.build();
        HttpRequest copiedRequest = requestBuilder
                .copy()
                .header("X-Foo", "2")
                .header("X-Bar", "3")
                .build();

        // Verify copied _references_
        assertEquals(request.uri(), copiedRequest.uri(), "URI");
        assertEquals(request.method(), copiedRequest.method(), "Method");
        assertEquals(request.expectContinue(), copiedRequest.expectContinue(), "Expect continue setting");
        assertEquals(request.timeout(), copiedRequest.timeout(), "Timeout");
        assertEquals(request.version(), copiedRequest.version(), "Version");

        // Verify headers
        assertEquals(request.headers().map(), Map.of("X-Foo", List.of("1")), "Request headers");
        assertEquals(copiedRequest.headers().map(), Map.of("X-Foo", List.of("1", "2"), "X-Bar", List.of("3")), "Copied request headers");

    }

    private static void assertEquals(Object expected, Object actual, Object name) {
        if (!Objects.equals(expected, actual)) {
            String message = String.format("%s mismatch!%nExpected: %s%nActual: %s", name, expected, actual);
            throw new AssertionError(message);
        }
    }

    // doesn't override the default HEAD() method
    private static final class NotOverriddenHEADImpl implements HttpRequest.Builder {
        private final HttpRequest.Builder underlying = HttpRequest.newBuilder();

        @Override
        public HttpRequest.Builder uri(URI uri) {
            return this.underlying.uri(uri);
        }

        @Override
        public HttpRequest.Builder expectContinue(boolean enable) {
            return this.underlying.expectContinue(enable);
        }

        @Override
        public HttpRequest.Builder version(HttpClient.Version version) {
            return this.underlying.version(version);
        }

        @Override
        public HttpRequest.Builder header(String name, String value) {
            return this.underlying.header(name, value);
        }

        @Override
        public HttpRequest.Builder headers(String... headers) {
            return this.underlying.headers(headers);
        }

        @Override
        public HttpRequest.Builder timeout(Duration duration) {
            return this.underlying.timeout(duration);
        }

        @Override
        public HttpRequest.Builder setHeader(String name, String value) {
            return this.underlying.setHeader(name, value);
        }

        @Override
        public HttpRequest.Builder GET() {
            return this.underlying.GET();
        }

        @Override
        public HttpRequest.Builder POST(HttpRequest.BodyPublisher bodyPublisher) {
            return this.underlying.POST(bodyPublisher);
        }

        @Override
        public HttpRequest.Builder PUT(HttpRequest.BodyPublisher bodyPublisher) {
            return this.underlying.PUT(bodyPublisher);
        }

        @Override
        public HttpRequest.Builder DELETE() {
            return this.underlying.DELETE();
        }

        @Override
        public HttpRequest.Builder method(String method, HttpRequest.BodyPublisher bodyPublisher) {
            return this.underlying.method(method, bodyPublisher);
        }

        @Override
        public HttpRequest build() {
            return this.underlying.build();
        }

        @Override
        public HttpRequest.Builder copy() {
            return this.underlying.copy();
        }
    }
}
