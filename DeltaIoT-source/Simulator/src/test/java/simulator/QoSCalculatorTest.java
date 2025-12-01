package simulator;

import static org.junit.Assert.assertEquals;

import java.util.Arrays;
import java.util.List;

import org.junit.Before;
import org.junit.Test;

public class QoSCalculatorTest {
    private static final double EPSILON = 0.000001d;

    private QoSCalculator calculator;

    @Before
    public void setUp() {
        calculator = new QoSCalculator();
    }

    @Test
    public void testCalcPowerAverage() {
        List<QoS> qos = Arrays.asList(new QoS(0, 0, 10), new QoS(0, 0, 20));

        double actualAverage = calculator.calcEnergyConsumptionAverage(qos);

        assertEquals(actualAverage, 15, EPSILON);
    }

    @Test
    public void testCalcPacketLossAverage() {
        List<QoS> qos = Arrays.asList(new QoS(0, 0.1, 12), new QoS(0, 0.2, 10));

        double actualAverage = calculator.calcPacketLossAverage(qos);

        assertEquals(actualAverage, 0.15, EPSILON);
    }

    @Test
    public void testCalcAverageScore() {
        List<QoS> qos = Arrays.asList(new QoS(0, 0.1, 10), new QoS(0, 0.2, 20));

        double actualScore = calculator.averageScore(qos);

        assertEquals(actualScore, 7.575, EPSILON);
    }
}
