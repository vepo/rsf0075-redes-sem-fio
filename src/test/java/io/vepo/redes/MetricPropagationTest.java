package io.vepo.redes;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.within;

import org.junit.jupiter.api.Test;

public class MetricPropagationTest {

    @Test
    void additiveMetricTest() {
        assertThat(MetricPropagation.fromAdditive(5)
                                    .and(5)
                                    .get()).isEqualTo(10);

        assertThat(MetricPropagation.fromAdditive(5.1)
                                    .and(5.5)
                                    .get()).isEqualTo(10.6);

        assertThat(MetricPropagation.fromAdditive(5.14f)
                                    .and(5.51f)
                                    .get()).isEqualTo(10.65f, within(0.0001f));

        assertThat(MetricPropagation.fromMultiplicative(5)
                                    .and(5)
                                    .get()).isEqualTo(25);

        assertThat(MetricPropagation.fromMultiplicative(0.5)
                                    .and(0.5)
                                    .get()).isEqualTo(0.25);

        assertThat(MetricPropagation.fromMultiplicative(0.5f)
                                    .and(0.5f)
                                    .get()).isEqualTo(0.25f, within(0.0001f));

        assertThat(MetricPropagation.fromComplementaryMultiplicative(0.1f)
                                    .and(0.1f)
                                    .get()).isEqualTo(0.19f, within(0.0001f));
    }
}
