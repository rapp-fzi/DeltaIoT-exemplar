package simulator;

import static org.junit.Assert.assertEquals;

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
        List<QoS> qos = null;

        double actualAverage = calculator.calcPowerAverage(qos);

        assertEquals(actualAverage, 0.0, EPSILON);
    }

}
