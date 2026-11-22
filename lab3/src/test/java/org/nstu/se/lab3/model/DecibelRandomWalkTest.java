package org.nstu.se.lab3.model;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.random.RandomGenerator;

class DecibelRandomWalkTest {
    private static RandomGenerator constantRandom(final double value) {
        return new RandomGenerator() {
            @Override
            public double nextDouble() {
                return value;
            }

            @Override
            public long nextLong() {
                return 0;
            }
        };
    }

    @Test
    @DisplayName("Сдвигает значение на максимальный шаг вверх при nextDouble == 1")
    void stepsUp() {
        final var walk = new DecibelRandomWalk(constantRandom(1.0), -72.0, 15.0, 6.0);
        assertThat(walk.next(0.0)).isEqualTo(6.0);
    }

    @Test
    @DisplayName("Сдвигает значение на максимальный шаг вниз при nextDouble == 0")
    void stepsDown() {
        final var walk = new DecibelRandomWalk(constantRandom(0.0), -72.0, 15.0, 6.0);
        assertThat(walk.next(0.0)).isEqualTo(-6.0);
    }

    @Test
    @DisplayName("Отражает значение от верхней границы")
    void reflectsAtUpperBound() {
        final var walk = new DecibelRandomWalk(constantRandom(1.0), -72.0, 15.0, 6.0);
        assertThat(walk.next(12.0)).isEqualTo(12.0);
    }

    @Test
    @DisplayName("Отражает значение от нижней границы")
    void reflectsAtLowerBound() {
        final var walk = new DecibelRandomWalk(constantRandom(0.0), -72.0, 15.0, 6.0);
        assertThat(walk.next(-70.0)).isEqualTo(-68.0);
    }

    @Test
    @DisplayName("Не выходит за границы при большом шаге")
    void staysWithinBounds() {
        final var walk = new DecibelRandomWalk(constantRandom(1.0), -10.0, 10.0, 100.0);
        final var next = walk.next(0.0);
        assertThat(next).isBetween(-10.0, 10.0);
    }

    @Test
    @DisplayName("Учитывает обновленный шаг")
    void appliesUpdatedStep() {
        final var walk = new DecibelRandomWalk(constantRandom(1.0), -72.0, 15.0, 6.0);
        walk.setStepDb(3.0);
        assertThat(walk.next(0.0)).isEqualTo(3.0);
    }
}
