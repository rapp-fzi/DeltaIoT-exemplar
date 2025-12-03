package simulator;

import static org.junit.Assert.assertEquals;

import java.util.Arrays;
import java.util.List;

import org.apache.commons.lang3.Range;
import org.junit.Before;
import org.junit.Test;

public class QoSCalculatorTest {
    private static final double EPSILON = 0.00001d;

    private QoSCalculator calculator;

    @Before
    public void setUp() {
        calculator = new QoSCalculator();
    }

    @Test
    public void testCalcPowerAverage() {
        List<QoS> qos = Arrays.asList(new QoS(0, 0, 10), new QoS(0, 0, 20));

        double actualAverage = calculator.calcEnergyConsumptionAverage(qos);

        assertEquals(15, actualAverage, EPSILON);
    }

    @Test
    public void testCalcPacketLossAverage() {
        List<QoS> qos = Arrays.asList(new QoS(0, 0.1, 12), new QoS(0, 0.2, 10));

        double actualAverage = calculator.calcPacketLossAverage(qos);

        assertEquals(0.15, actualAverage, EPSILON);
    }

    @Test
    public void testAverageScore() {
        List<QoS> qos = Arrays.asList(new QoS(0, 0.1, 10), new QoS(0, 0.2, 20));

        double actualScore = calculator.averageScore(qos);

        assertEquals(7.575, actualScore, EPSILON);
    }

    @Test
    public void testNormalizedScore() {
        List<QoS> qos = Arrays.asList(new QoS(0, 0.1, 10), new QoS(0, 0.2, 20));

        double actualScore = calculator.normalizedScore(qos);

        assertEquals(0.51339, actualScore, EPSILON);
    }

    @Test
    public void testNormalize() {
        Range<Double> range = Range.of(0.0, 20.0);

        double actualNormalized = calculator.normalize(5, range);

        assertEquals(0.25, actualNormalized, EPSILON);
    }

    @Test
    public void testNormalizeBelow() {
        Range<Double> range = Range.of(10.0, 20.0);

        double actualNormalized = calculator.normalize(5, range);

        assertEquals(0.0, actualNormalized, EPSILON);
    }

    @Test
    public void testNormalizeAbove() {
        Range<Double> range = Range.of(10.0, 20.0);

        double actualNormalized = calculator.normalize(22, range);

        assertEquals(1.0, actualNormalized, EPSILON);
    }
}
