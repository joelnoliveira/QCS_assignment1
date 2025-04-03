package CruiseControl;

import CruiseControl.CruiseControlFaultTolerant;
public class MainCruiseControl {
    
    private static CruiseControlFaultTolerant cruiseControlFaultTolerant = new CruiseControlFaultTolerant();

    public static void main(String[] args) {

        int minSpeed = 30;
        int maxSpeed = 130;
        int topSpeed = 240;
        int setSpeed, maxAccel, error;
        int[] prevSpeed;
        int[] sensors = new int[2];
        
        // Test Case 1
        sensors[0] = 100;
        sensors[1] = 100;
        prevSpeed = new int[]{100,100};
        setSpeed = 100;
        maxAccel = 10;
        error = 10;

        cruiseControlFaultTolerant.cruiseControl(minSpeed, maxSpeed, topSpeed, error, maxAccel, setSpeed,prevSpeed,sensors);

        // Test Case 2
        sensors[0] = 100;
        sensors[1] = 100;
        prevSpeed = new int[]{100,100};
        setSpeed = 100;
        maxAccel = 10;
        error = 5;

        cruiseControlFaultTolerant.cruiseControl(minSpeed, maxSpeed, topSpeed, error, maxAccel, setSpeed,prevSpeed,sensors);

        /// Test Case 3
        sensors[0] = 100;
        sensors[1] = 100;
        prevSpeed = new int[]{100, 100};
        setSpeed = 120;
        maxAccel = 10;
        error = 0;

        cruiseControlFaultTolerant.cruiseControl(minSpeed, maxSpeed, topSpeed, error, maxAccel, setSpeed,prevSpeed,sensors);

        // Test Case 4
        sensors[0] = 100;
        sensors[1] = 100;
        prevSpeed = new int[]{100, 100};
        setSpeed = 80;
        maxAccel = 30;
        error = 0;

        cruiseControlFaultTolerant.cruiseControl(minSpeed, maxSpeed, topSpeed, error, maxAccel, setSpeed,prevSpeed,sensors);

        // Test Case 5
        sensors[0] = 100;
        sensors[1] = 100;
        prevSpeed = new int[]{100, 100};
        setSpeed = 80;
        maxAccel = 10;
        error = 21;

        cruiseControlFaultTolerant.cruiseControl(minSpeed, maxSpeed, topSpeed, error, maxAccel, setSpeed,prevSpeed,sensors);

    }
}
