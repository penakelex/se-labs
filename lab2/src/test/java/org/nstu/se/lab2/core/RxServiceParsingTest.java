package org.nstu.se.lab2.core;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.nstu.se.lab2.core.exceptions.RxException;
import org.nstu.se.lab2.core.parsing.RequestParser;
import org.nstu.se.lab2.services.SumService;

class RxServiceParsingTest {
    private static final String LEFT = "left";
    private static final String RIGHT = "right";

    private RxService service;

    @BeforeEach
    void setUp() {
        final var sum = new SumService();
        service = new RxService(sum);
        sum.configure(service);
    }

    @AfterEach
    void tearDown() {
        service.shutdown();
    }

    @ParameterizedTest
    @ValueSource(
            strings = {
                "sum:inp1=15,inp2=16,inp3=4",
                "sum: inp1=15 , inp2=16 , inp3=4",
                "sum:inp1=15;inp2=16;inp3=4"
            })
    @DisplayName("Разбирает запрос в любом формате")
    void parsesValidRequest(final String request) throws RxException {
        assertThat(RequestParser.parse(request).parameters())
                .containsEntry("inp1", "15")
                .containsEntry("inp2", "16")
                .containsEntry("inp3", "4");
    }

    @ParameterizedTest
    @CsvSource(
            delimiter = '|',
            value = {
                "unknown:inp1=1 | Метод не найден",
                "sum:inp1=15,inp2=16 | Не задан параметр",
                "sum:inp1=15,inp2=16,inp3=4,typo=1 | Лишний параметр",
                "sum:inp1=1,inp1=2,inp2=3,inp3=4 | Дублирующийся параметр",
                "sum:inp1 | Нет '='",
                "' ' | Пустой запрос",
                "sum | Нет ':'"
            })
    @DisplayName("Отклоняет некорректные запросы")
    void rejectsInvalidRequests(final String request, final String expectedMessage) {
        assertThatThrownBy(() -> service.submit(request))
                .isInstanceOf(RxException.class)
                .hasMessageContaining(expectedMessage);
    }

    @Test
    @DisplayName("Поддерживает значения в кавычках")
    void parsesQuotedValues() throws RxException {
        final var request = RequestParser.parse("concat:left=\"foo,bar\",right=\"baz;qux\"");

        assertThat(request.parameters())
                .containsEntry(LEFT, "foo,bar")
                .containsEntry(RIGHT, "baz;qux");
    }

    @Test
    @DisplayName("Поддерживает экранированные кавычки")
    void parsesEscapedQuotes() throws RxException {
        final var request = RequestParser.parse("concat:left=\"a\\\"b\",right=plain");

        assertThat(request.parameters()).containsEntry(LEFT, "a\"b");
    }

    @Test
    @DisplayName("Отклоняет незакрытую кавычку")
    void rejectsUnterminatedQuote() {
        assertThatThrownBy(() -> RequestParser.parse("concat:left=\"foo,right=bar"))
                .isInstanceOf(RxException.class)
                .hasMessageContaining("Незакрытая кавычка");
    }
}
