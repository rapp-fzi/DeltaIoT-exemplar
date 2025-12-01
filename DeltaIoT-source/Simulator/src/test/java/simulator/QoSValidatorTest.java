package simulator;

import java.util.Arrays;
import java.util.List;

import org.junit.Before;
import org.junit.Test;

public class QoSValidatorTest {
    private QoSValidator validator;

    @Before
    public void setUp() {
        validator = new QoSValidator();
    }

    @Test
    public void testValidateMinimum() {
        List<QoS> qos = Arrays
            .asList(new QoS(0, QoS.RANGE_PACKET_LOSS.getMinimum(), QoS.RANGE_ENERGY_CONSUMPTION.getMinimum()));

        validator.validate(qos);
    }

    @Test
    public void testValidateMaximum() {
        List<QoS> qos = Arrays
            .asList(new QoS(0, QoS.RANGE_PACKET_LOSS.getMaximum(), QoS.RANGE_ENERGY_CONSUMPTION.getMaximum()));

        validator.validate(qos);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testCalcPowerAverageLowerBoundsValidation() {
        List<QoS> qos = Arrays.asList(new QoS(0, 0, QoS.RANGE_ENERGY_CONSUMPTION.getMinimum() - 0.1));

        validator.validate(qos);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testCalcPowerAverageUpperBoundsValidation() {
        List<QoS> qos = Arrays.asList(new QoS(0, 0, QoS.RANGE_ENERGY_CONSUMPTION.getMaximum() + 0.1));

        validator.validate(qos);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testCalcPacketLossAverageLowerBoundsValidation() {
        List<QoS> qos = Arrays.asList(new QoS(0, QoS.RANGE_PACKET_LOSS.getMinimum() - 0.1, 0));

        validator.validate(qos);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testCalcPacketLossAverageUpperBoundsValidation() {
        List<QoS> qos = Arrays.asList(new QoS(0, QoS.RANGE_PACKET_LOSS.getMaximum() + 0.1, 0));

        validator.validate(qos);
    }
}
