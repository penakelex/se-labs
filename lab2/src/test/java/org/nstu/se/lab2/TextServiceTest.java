package org.nstu.se.lab2;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.nstu.se.lab2.core.RxService;
import org.nstu.se.lab2.services.TextService;

import java.util.concurrent.TimeUnit;

class TextServiceTest {
    private RxService service;

    @BeforeEach
    void setUp() {
        final var text = new TextService();
        service = new RxService(text);
        text.configure(service);
    }

    @AfterEach
    void tearDown() {
        service.shutdown();
    }

    @ParameterizedTest
    @CsvSource(
            delimiter = '|',
            value = {
                "concat:left=foo,right=bar | foobar",
                "upper:text=hello | HELLO",
                "length:text=hello | 5"
            })
    void textWorks(final String request, final String expected) throws Exception {
        assertThat(service.submit(request).get(2, TimeUnit.SECONDS)).hasToString(expected);
    }

    @Test
    @DisplayName("Поддерживает значения в кавычках")
    void textWorksWithQuotedValues() throws Exception {
        assertThat(
                        service.submit("concat:left=\"foo,bar\",right=\"baz;qux\"")
                                .get(2, TimeUnit.SECONDS))
                .hasToString("foo,barbaz;qux");
    }
}
