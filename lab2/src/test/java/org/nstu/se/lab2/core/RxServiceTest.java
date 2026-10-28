package org.nstu.se.lab2.core;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.nstu.se.lab2.demo.DemoService;
import org.nstu.se.lab2.types.RxIntegerValue;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

@SuppressWarnings({
    "PMD.TooManyMethods",
    "PMD.UnitTestContainsTooManyAsserts",
    "PMD.SignatureDeclareThrowsException"
})
class RxServiceTest {
    private static final int TIMEOUT_SECONDS = 2;
    private static final int EXPECTED_SUM = 35;

    private static final String METHOD_SUM = "sum";
    private static final String KEY_NAME = "name";
    private static final String KEY_INFO = "info";
    private static final String KEY_PARAMETERS = "parameters";
    private static final String INFO_SUM = "output = sum of 3 inputs";

    private static final String REQUEST = METHOD_SUM + ":inp1=15,inp2=16,inp3=4";
    private static final String SPACED_REQUEST = METHOD_SUM + ": inp3=4 , inp1=15 , inp2=16";
    private static final String SEMICOLON_REQUEST = METHOD_SUM + ":inp1=15;inp2=16;inp3=4";
    private static final String UNKNOWN_REQUEST = "unknown:inp1=1";
    private static final String MISSING_REQUEST = METHOD_SUM + ":inp1=15,inp2=16";
    private static final String INVALID_CONVERSION = METHOD_SUM + ":inp1=abc,inp2=16,inp3=4";
    private static final String NEGATIVE_REQUEST = METHOD_SUM + ":inp1=-1,inp2=16,inp3=4";
    private static final String DUPLICATE_REQUEST = METHOD_SUM + ":inp1=1,inp1=2,inp2=3,inp3=4";
    private static final String MALFORMED_REQUEST = METHOD_SUM + ":inp1";

    private static final String ERROR_EMPTY_REQUEST = "Пустой запрос";
    private static final String ERROR_NO_COLON = "Нет ':'";
    private static final String ERROR_METHOD_NOT_FOUND = "Метод не найден";
    private static final String ERROR_MISSING_PARAMETER = "Не задан параметр";
    private static final String ERROR_CONVERT = "Нельзя преобразовать";
    private static final String ERROR_DUPLICATE_PARAMETER = "Дублирующийся параметр";
    private static final String ERROR_NO_EQUALS = "Нет '='";
    private static final String ERROR_NEGATIVE = "Отрицательные входы не поддерживаются";

    private DemoService demo;
    private RxService service;

    @BeforeEach
    void setUp() {
        demo = new DemoService();
        service = new RxService(demo);
        service.registerConverter(
                RxIntegerValue.class, value -> new RxIntegerValue(Integer.parseInt(value.trim())));
    }

    @AfterEach
    void tearDown() {
        service.shutdown();
    }

    @Test
    @DisplayName("Сканирует аннотированные методы")
    void scansAnnotatedMethods() {
        final var methods = service.getAnnotatedMethods();
        assertThat(methods).hasSize(1);

        final var method = methods.getFirst();
        assertThat(method).containsEntry(KEY_NAME, METHOD_SUM);
        assertThat(method).containsEntry(KEY_INFO, INFO_SUM);
        assertThat(method).containsKey(KEY_PARAMETERS);
    }

    @Test
    @DisplayName("Создаёт сервис через объект, Class и имя класса")
    void createsFromObjectClassAndName() throws Exception {
        final var fromObject = new RxService(demo);
        final var fromClass = new RxService(DemoService.class);
        final var fromName = new RxService(DemoService.class.getName());

        try {
            assertThat(fromObject.getAnnotatedMethods()).hasSize(1);
            assertThat(fromClass.getAnnotatedMethods()).hasSize(1);
            assertThat(fromName.getAnnotatedMethods()).hasSize(1);
        } finally {
            fromObject.shutdown();
            fromClass.shutdown();
            fromName.shutdown();
        }
    }

    @Test
    @DisplayName("Асинхронно вызывает аннотированный метод")
    void invokesAnnotatedMethodAsynchronously() throws Exception {
        final var future = service.submit(REQUEST);

        assertThat(await(future)).isNull();
        assertThat(demo.getLastResult()).isEqualTo(EXPECTED_SUM);
    }

    @Test
    @DisplayName("Поддерживает пробелы, запятые и точку с запятой")
    void acceptsFlexibleRequestFormats() throws Exception {
        await(service.submit(SPACED_REQUEST));
        assertThat(demo.getLastResult()).isEqualTo(EXPECTED_SUM);

        await(service.submit(SEMICOLON_REQUEST));
        assertThat(demo.getLastResult()).isEqualTo(EXPECTED_SUM);
    }

    @Test
    @DisplayName("Ошибка при неизвестном методе")
    void rejectsUnknownMethod() {
        assertThatThrownBy(() -> service.submit(UNKNOWN_REQUEST))
                .isInstanceOf(RxException.class)
                .hasMessageContaining(ERROR_METHOD_NOT_FOUND);
    }

    @Test
    @DisplayName("Ошибка при отсутствующем параметре")
    void rejectsMissingParameter() {
        assertThatThrownBy(() -> service.submit(MISSING_REQUEST))
                .isInstanceOf(RxException.class)
                .hasMessageContaining(ERROR_MISSING_PARAMETER);
    }

    @Test
    @DisplayName("Ошибка конвертации параметра")
    void rejectsInvalidConversion() {
        assertThatThrownBy(() -> service.submit(INVALID_CONVERSION))
                .isInstanceOf(RxException.class)
                .hasMessageContaining(ERROR_CONVERT);
    }

    @Test
    @DisplayName("Ошибка при дублирующемся параметре")
    void rejectsDuplicateParameter() {
        assertThatThrownBy(() -> service.parse(DUPLICATE_REQUEST))
                .isInstanceOf(RxException.class)
                .hasMessageContaining(ERROR_DUPLICATE_PARAMETER);
    }

    @Test
    @DisplayName("Ошибка при паре без '='")
    void rejectsMalformedPair() {
        assertThatThrownBy(() -> service.parse(MALFORMED_REQUEST))
                .isInstanceOf(RxException.class)
                .hasMessageContaining(ERROR_NO_EQUALS);
    }

    @Test
    @DisplayName("Ошибка при пустом запросе")
    void rejectsEmptyRequest() {
        assertThatThrownBy(() -> service.parse(" "))
                .isInstanceOf(RxException.class)
                .hasMessageContaining(ERROR_EMPTY_REQUEST);
    }

    @Test
    @DisplayName("Ошибка при запросе без ':'")
    void rejectsRequestWithoutColon() {
        assertThatThrownBy(() -> service.parse(METHOD_SUM))
                .isInstanceOf(RxException.class)
                .hasMessageContaining(ERROR_NO_COLON);
    }

    @Test
    @DisplayName("Ошибка внутри метода приходит через Future")
    void propagatesMethodErrorThroughFuture() throws Exception {
        final Future<Object> future = service.submit(NEGATIVE_REQUEST);

        assertThatThrownBy(() -> await(future))
                .isInstanceOf(ExecutionException.class)
                .hasRootCauseInstanceOf(RxException.class)
                .hasRootCauseMessage(ERROR_NEGATIVE);
    }

    private static Object await(final Future<Object> future) throws Exception {
        return future.get(TIMEOUT_SECONDS, TimeUnit.SECONDS);
    }
}
