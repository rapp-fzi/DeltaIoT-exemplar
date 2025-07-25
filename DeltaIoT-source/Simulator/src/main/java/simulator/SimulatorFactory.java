package simulator;

import domain.DoubleRange;
import domain.Gateway;
import domain.Mote;
import domain.Position;
import domain.SNREquation;

public class SimulatorFactory {
    public static final int DEFAULT_GATEWAY_ID = 1;

    public static Simulator createExperimentSimulator(int numOfRuns) {
        return createExperimentSimulator(numOfRuns, DEFAULT_GATEWAY_ID);
    }

    public static Simulator createExperimentSimulator(int numOfRuns, int gatewayId) {
        Simulator simul = new Simulator(numOfRuns);

        // Motes
        int load = 10;
        double battery = 11880.0;
        double posScale = 2;
        Mote mote2 = new Mote(2, battery, load, new Position(378 * posScale, 193 * posScale));
        Mote mote3 = new Mote(3, battery, load, new Position(365 * posScale, 343 * posScale));
        Mote mote4 = new Mote(4, battery, load, new Position(508 * posScale, 296 * posScale));
        Mote mote5 = new Mote(5, battery, load, new Position(603 * posScale, 440 * posScale));
        Mote mote6 = new Mote(6, battery, load, new Position(628 * posScale, 309 * posScale));
        Mote mote7 = new Mote(7, battery, load, new Position(324 * posScale, 273 * posScale));
        Mote mote8 = new Mote(8, battery, load, new Position(392 * posScale, 478 * posScale));
        Mote mote9 = new Mote(9, battery, load, new Position(540 * posScale, 479 * posScale));
        Mote mote10 = new Mote(10, battery, load, new Position(694 * posScale, 356 * posScale));
        Mote mote11 = new Mote(11, battery, load, new Position(234 * posScale, 232 * posScale));
        Mote mote12 = new Mote(12, battery, load, new Position(221 * posScale, 322 * posScale));
        Mote mote13 = new Mote(13, battery, load, new Position(142 * posScale, 170 * posScale));
        Mote mote14 = new Mote(14, battery, load, new Position(139 * posScale, 293 * posScale));
        Mote mote15 = new Mote(15, battery, load, new Position(128 * posScale, 344 * posScale));

        Mote[] allMotes = new Mote[] { mote2, mote3, mote4, mote5, mote6, mote7, mote8, mote9, mote10, mote11, mote12,
                mote13, mote14, mote15 };
        simul.addMotes(allMotes); // ignore the first null elements

        // Gateway
        Gateway gateway = new Gateway(gatewayId, new Position(482 * posScale, 360 * posScale));
        gateway.setView(allMotes);
        simul.addGateways(gateway);

        // Links
        int power = 15;
        int distribution = 100;
        mote2.addLinkTo(mote4, gateway, power, distribution);
        mote3.addLinkTo(gateway, gateway, power, distribution);
        mote4.addLinkTo(gateway, gateway, power, distribution);
        mote5.addLinkTo(mote9, gateway, power, distribution);
        mote6.addLinkTo(mote4, gateway, power, distribution);
        mote7.addLinkTo(mote2, gateway, power, 0);
        mote7.addLinkTo(mote3, gateway, power, distribution);
        mote8.addLinkTo(gateway, gateway, power, distribution);
        mote9.addLinkTo(gateway, gateway, power, distribution);
        mote10.addLinkTo(mote6, gateway, power, 50);
        mote10.addLinkTo(mote5, gateway, power, 50);
        mote11.addLinkTo(mote7, gateway, power, distribution);
        mote12.addLinkTo(mote7, gateway, power, 0);
        mote12.addLinkTo(mote3, gateway, power, distribution);
        mote13.addLinkTo(mote11, gateway, power, distribution);
        mote14.addLinkTo(mote12, gateway, power, distribution);
        mote15.addLinkTo(mote12, gateway, power, distribution);

        // Set order
        simul.setTurnOrder(8, 10, 13, 14, 15, 5, 6, 11, 12, 9, 7, 2, 3, 4);

        // Mote activations
        mote5.setActivationProbability(new DoubleRange(0.7, 0.9));
        mote7.setActivationProbability(new DoubleRange(0.6, 1.0));
        mote11.setActivationProbability(new DoubleRange(0.7, 0.9));
        mote12.setActivationProbability(new DoubleRange(0.85, 0.95));

        // Global random interference (mimicking Usman's random interference)
        simul.getRunInfo()
            .setGlobalInterference(new DoubleRange(0.0, 0.0));

        DoubleRange highWirelessInterference = new DoubleRange(-5.0, 5.0);
        DoubleRange smallWirelessInterference = new DoubleRange(-2.0, 2.0);
        mote2.getLinkTo(mote4)
            .setInterference(highWirelessInterference);
        mote3.getLinkTo(gateway)
            .setInterference(smallWirelessInterference);
        mote4.getLinkTo(gateway)
            .setInterference(smallWirelessInterference);
        mote5.getLinkTo(mote9)
            .setInterference(smallWirelessInterference);
        mote6.getLinkTo(mote4)
            .setInterference(smallWirelessInterference);
        mote7.getLinkTo(mote3)
            .setInterference(highWirelessInterference);
        mote7.getLinkTo(mote2)
            .setInterference(highWirelessInterference);
        mote8.getLinkTo(gateway)
            .setInterference(highWirelessInterference);
        mote9.getLinkTo(gateway)
            .setInterference(smallWirelessInterference);
        mote10.getLinkTo(mote6)
            .setInterference(highWirelessInterference);
        mote10.getLinkTo(mote5)
            .setInterference(highWirelessInterference);
        mote11.getLinkTo(mote7)
            .setInterference(smallWirelessInterference);
        mote12.getLinkTo(mote7)
            .setInterference(smallWirelessInterference);
        mote12.getLinkTo(mote3)
            .setInterference(smallWirelessInterference);
        mote13.getLinkTo(mote11)
            .setInterference(highWirelessInterference);
        mote14.getLinkTo(mote12)
            .setInterference(highWirelessInterference);
        mote15.getLinkTo(mote12)
            .setInterference(smallWirelessInterference);

        // Add SNR equations (from Usman's settings class)
        mote2.getLinkTo(mote4)
            .setSnrEquation(new SNREquation(0.0473684210526, -5.29473684211));
        mote3.getLinkTo(gateway)
            .setSnrEquation(new SNREquation(0.0280701754386, 4.25263157895));
        mote4.getLinkTo(gateway)
            .setSnrEquation(new SNREquation(0.119298245614, -1.49473684211));
        mote5.getLinkTo(mote9)
            .setSnrEquation(new SNREquation(-0.019298245614, 4.8));
        mote6.getLinkTo(mote4)
            .setSnrEquation(new SNREquation(0.0175438596491, -3.84210526316));
        mote7.getLinkTo(mote3)
            .setSnrEquation(new SNREquation(0.168421052632, 2.30526315789));
        mote7.getLinkTo(mote2)
            .setSnrEquation(new SNREquation(-0.0157894736842, 3.77894736842));
        mote8.getLinkTo(gateway)
            .setSnrEquation(new SNREquation(0.00350877192982, 0.45263157895));
        mote9.getLinkTo(gateway)
            .setSnrEquation(new SNREquation(0.0701754385965, 2.89473684211));
        mote10.getLinkTo(mote6)
            .setSnrEquation(new SNREquation(3.51139336547e-16, -2.21052631579));
        mote10.getLinkTo(mote5)
            .setSnrEquation(new SNREquation(0.250877192982, -1.75789473684));
        mote11.getLinkTo(mote7)
            .setSnrEquation(new SNREquation(0.380701754386, -2.12631578947));
        mote12.getLinkTo(mote7)
            .setSnrEquation(new SNREquation(0.317543859649, 2.95789473684));
        mote12.getLinkTo(mote3)
            .setSnrEquation(new SNREquation(-0.0157894736842, -3.77894736842));
        mote13.getLinkTo(mote11)
            .setSnrEquation(new SNREquation(-0.0210526315789, -2.81052631579));
        mote14.getLinkTo(mote12)
            .setSnrEquation(new SNREquation(0.0333333333333, 2.58947368421));
        mote15.getLinkTo(mote12)
            .setSnrEquation(new SNREquation(0.0438596491228, 1.31578947368));

        return simul;
    }

//  public static Simulator createSimulatorForDeltaIoT() {
//  Simulator simul = new Simulator();
//  
//  // Motes
//  int load = 10;
//  double battery = 11880.0;
//  double posScale = 2;
//  Mote mote2  = new Mote(2 , battery, load, new Position(378 * posScale, 193 * posScale));
//  Mote mote3  = new Mote(3 , battery, load, new Position(365 * posScale, 343 * posScale));
//  Mote mote4  = new Mote(4 , battery, load, new Position(508 * posScale, 296 * posScale));
//  Mote mote5  = new Mote(5 , battery, load, new Position(603 * posScale, 440 * posScale));
//  Mote mote6  = new Mote(6 , battery, load, new Position(628 * posScale, 309 * posScale));
//  Mote mote7  = new Mote(7 , battery, load, new Position(324 * posScale, 273 * posScale));
//  Mote mote8  = new Mote(8 , battery, load, new Position(392 * posScale, 478 * posScale));
//  Mote mote9  = new Mote(9 , battery, load, new Position(540 * posScale, 479 * posScale));
//  Mote mote10 = new Mote(10, battery, load, new Position(694 * posScale, 356 * posScale));
//  Mote mote11 = new Mote(11, battery, load, new Position(234 * posScale, 232 * posScale));
//  Mote mote12 = new Mote(12, battery, load, new Position(221 * posScale, 322 * posScale));
//  Mote mote13 = new Mote(13, battery, load, new Position(142 * posScale, 170 * posScale));
//  Mote mote14 = new Mote(14, battery, load, new Position(139 * posScale, 293 * posScale));
//  Mote mote15 = new Mote(15, battery, load, new Position(128 * posScale, 344 * posScale));    
//  
//  Mote[] allMotes = new Mote[]{mote2, mote3, mote4, mote5, mote6, mote7, mote8, mote9, mote10, mote11, mote12, mote13, mote14, mote15};
//  simul.addMotes(allMotes); //ignore the first null elements
//  
//  // Gateway
//  Gateway gateway = new Gateway(GATEWAY_ID, new Position(482 * posScale, 360 * posScale));
//  gateway.setView(allMotes);
//  simul.addGateways(gateway);
//  
//  // Links
//  int power = 15;
//  int distribution = 100;
//  mote2. addLinkTo(mote4,   gateway, power, distribution);
//  mote3. addLinkTo(gateway, gateway, power, distribution);
//  mote4. addLinkTo(gateway, gateway, power, distribution);
//  mote5. addLinkTo(mote9,   gateway, power, distribution);
//  mote6. addLinkTo(mote4,   gateway, power, distribution);
//  mote7. addLinkTo(mote2,   gateway, power, distribution);
//  mote7. addLinkTo(mote3,   gateway, power, distribution);
//  mote8. addLinkTo(gateway, gateway, power, distribution);
//  mote9. addLinkTo(gateway, gateway, power, distribution);
//  mote10.addLinkTo(mote6,   gateway, power, distribution);
//  mote10.addLinkTo(mote5,   gateway, power, distribution);
//  mote11.addLinkTo(mote7,   gateway, power, distribution);
//  mote12.addLinkTo(mote7,   gateway, power, distribution);
//  mote12.addLinkTo(mote3,   gateway, power, distribution);
//  mote13.addLinkTo(mote11,  gateway, power, distribution);
//  mote14.addLinkTo(mote12,  gateway, power, distribution);
//  mote15.addLinkTo(mote12,  gateway, power, distribution);
//  
//  // Set order
//  simul.setTurnOrder(8, 10, 13, 14, 15, 5, 6, 11, 12, 9, 7, 2, 3, 4);
//  
//  // Set profiles for some links and motes
//  mote2 .setActivationProbability(new Constant<>(0.85));
//  mote8 .setActivationProbability(new Constant<>(0.85));
//  mote10.setActivationProbability(new FileProfile("deltaiot/scenario_data/PIR1.txt", 1.0));
//  mote13.setActivationProbability(new FileProfile("deltaiot/scenario_data/PIR2.txt", 1.0));
//  mote14.setActivationProbability(new Constant<>(0.85));
//  
//  mote12.getLinkTo(mote3).setInterference(new FileProfile("deltaiot/scenario_data/SNR2.txt", 0.0));
//  
//  // Add SNR equations (from Usman's settings class)
//  mote2 .getLinkTo(mote4  ).setSnrEquation(new SNREquation( 0.0473684210526,      -5.29473684211));
//  mote3 .getLinkTo(gateway).setSnrEquation(new SNREquation( 0.0280701754386,       4.25263157895));
//  mote4 .getLinkTo(gateway).setSnrEquation(new SNREquation( 0.119298245614,       -1.49473684211));
//  mote5 .getLinkTo(mote9  ).setSnrEquation(new SNREquation(-0.019298245614,        4.8));
//  mote6 .getLinkTo(mote4  ).setSnrEquation(new SNREquation( 0.0175438596491,      -3.84210526316));
//  mote7 .getLinkTo(mote3  ).setSnrEquation(new SNREquation( 0.168421052632,        2.30526315789));
//  mote7 .getLinkTo(mote2  ).setSnrEquation(new SNREquation(-0.0157894736842,       3.77894736842));
//  mote8 .getLinkTo(gateway).setSnrEquation(new SNREquation( 0.00350877192982,      0.45263157895));
//  mote9 .getLinkTo(gateway).setSnrEquation(new SNREquation( 0.0701754385965,       2.89473684211));
//  mote10.getLinkTo(mote6  ).setSnrEquation(new SNREquation( 3.51139336547e-16,        -2.21052631579));
//  mote10.getLinkTo(mote5  ).setSnrEquation(new SNREquation( 0.250877192982,       -1.75789473684));
//  mote11.getLinkTo(mote7  ).setSnrEquation(new SNREquation( 0.380701754386,       -2.12631578947));
//  mote12.getLinkTo(mote7  ).setSnrEquation(new SNREquation( 0.317543859649,        2.95789473684));
//  mote12.getLinkTo(mote3  ).setSnrEquation(new SNREquation(-0.0157894736842,      -3.77894736842));
//  mote13.getLinkTo(mote11 ).setSnrEquation(new SNREquation(-0.0210526315789,      -2.81052631579));
//  mote14.getLinkTo(mote12 ).setSnrEquation(new SNREquation( 0.0333333333333,       2.58947368421));
//  mote15.getLinkTo(mote12 ).setSnrEquation(new SNREquation( 0.0438596491228,       1.31578947368));
//  
//  // Global random interference (mimicking Usman's random interference)
//  simul.getRunInfo().setGlobalInterference(new DoubleRange(-5.0, 5.0));
//  
//  return simul;
//}

    // Pre-build simulators

    public static Simulator createBaseCase(int numOfRuns) {
        Simulator simul = new Simulator(numOfRuns);

        // Motes
        double battery = 11880;
        int load = 10;
        Mote mote1 = new Mote(1, battery, load);
        Mote mote12 = new Mote(12, battery, load);
        Mote mote2 = new Mote(2, battery, load);
        simul.addMotes(mote1, mote12, mote2);

        // Gateways
        // I use the convention to give gateways negative ids
        // Nothing enforces this, but all ids have to be unique between all nodes (= motes &
        // gateways)
        Gateway gateway1 = new Gateway(-1);
        gateway1.setView(mote1, mote12);
        Gateway gateway2 = new Gateway(-2);
        gateway2.setView(mote2, mote12);
        simul.addGateways(gateway1, gateway2);

        // Links
        int power = 15;
        int distribution = 100;
        mote1.addLinkTo(gateway1, gateway1, power, distribution);
        mote2.addLinkTo(gateway2, gateway2, power, distribution);
        mote12.addLinkTo(mote1, gateway1, power, distribution);
        mote12.addLinkTo(mote2, gateway2, power, distribution);

        simul.setTurnOrder(mote12, mote1, mote2);

        return simul;
    }

    public static Simulator createBaseCase2(int numOfRuns) {
        Simulator simul = new Simulator(numOfRuns);

        // Motes
        double battery = 11880;
        int load = 10;
        Mote mote0 = new Mote(0, battery, load);
        Mote mote11 = new Mote(11, battery, load);
        Mote mote12 = new Mote(12, battery, load);
        Mote mote21 = new Mote(21, battery, load);
        Mote mote22 = new Mote(22, battery, load);
        simul.addMotes(mote0, mote11, mote12, mote21, mote22);

        // Gateways
        // I use the convention to give gateways negative ids
        // Nothing enforces this, but all ids have to be unique between all nodes (= motes &
        // gateways)
        Gateway gateway1 = new Gateway(-1);
        gateway1.setView(mote11, mote12, mote0);
        Gateway gateway2 = new Gateway(-2);
        gateway2.setView(mote21, mote22, mote0);
        simul.addGateways(gateway1, gateway2);

        // Links
        int power = 15;
        int distribution = 100;
        mote0.addLinkTo(mote11, gateway1, power, distribution);
        mote0.addLinkTo(mote12, gateway1, power, distribution);
        mote0.addLinkTo(mote21, gateway2, power, distribution);
        mote0.addLinkTo(mote22, gateway2, power, distribution);

        mote11.addLinkTo(gateway1, gateway1, power, distribution);
        mote12.addLinkTo(gateway1, gateway1, power, distribution);

        mote21.addLinkTo(gateway2, gateway2, power, distribution);
        mote22.addLinkTo(gateway2, gateway2, power, distribution);

        simul.setTurnOrder(mote0, mote11, mote12, mote21, mote22);

        return simul;
    }
}
