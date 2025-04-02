package CruiseControl;

import java.util.LinkedList;
public class CruiseControlFaultTolerant {

    
    private boolean inputChecker(final int minSpeed,final int maxSpeed, final int error, final int maxAccel, final int setSpeed, LinkedList<Integer> preSpeed, final int[] speedSensors){

        short badSensorsCount = 0;
        // check for bad inputs
        if(minSpeed < 0){
            System.out.println("Error: minSpeed must be non negative");
            return false;
        }
        if(maxSpeed <= 0){
            System.out.println("Error: maxSpeed must be higher than zero");
            return false;
        }
        if(error < 0){
            System.out.println("Error: error must be non-negative");
            return false;
        }
        if(setSpeed < minSpeed){
            System.out.println("Error: setSpeed must be higher or equal than minSpeed");
            return false;
        }
        if(setSpeed > maxSpeed){
            System.out.println("Error: setSpeed must be lower or equal than maxSpeed");
            return false;
        }
        if(maxAccel <= 0){
            System.out.println("Error: maxAccel must be higher than zero");
            return false;
        }
        if(speedSensors == null){
            System.out.println("Error: speedSensors must not be null");
            return false;
        }
        if(speedSensors.length == 0){
            System.out.println("Error: speedSensors must not be empty");
            return false;
        }
        if(preSpeed == null){
            System.out.println("Error: preSpeed must not be null");
            return false;
        }
        if(preSpeed.size() == 0){
            System.out.println("Error: preSpeed must not be empty");
            return false;
        }

        // check if number of sensors >= 2
        if(speedSensors.length < 2 || speedSensors.length > 3){
            System.out.println("Error: number of sensors must be 2 or 3");
            return false;
        }

        // check valid conditions
        if(minSpeed >= maxSpeed){
            System.out.println("Error: minSpeed must be lower than maxSpeed");
            return false;
        }

        // check preSpeed values and delete bad ones
        for(int i = 0; i < preSpeed.size(); i++){
            if(preSpeed.get(i) < 0){
                preSpeed.remove(i);
                i--;
            }
        }

        // check how many bad sensors are there
        for(short i = 0; i < speedSensors.length; i++){
            if(speedSensors[i] < 0){
                badSensorsCount++;
                System.out.println("Warning: sensor " + i + " provided a negative value");
            }
        }
        // There must be at least 2 non-negative vaule sensors
        if(speedSensors.length - badSensorsCount < 2){
            System.out.println("Error: there are too many bad sensors");
            return false;
        }
        
        // check if target speed is in the range of min and max speed
        if(setSpeed < minSpeed || setSpeed > maxSpeed){
            System.out.println("Error: setSpeed must be in the range of min and max speed");
            return false;
        }

        return true;
    }

    /**
     * @param speedSensors current speed value from each sensor, must be at least 2 and max 3
     * @return correct current speed value or -1 if the correct value cannot be determined
     */
    private int getCurrentSpeed(final int[] speedSensors){
    
        if (speedSensors.length == 3) { // Perform Pair-and-a-spare approach
            
            if(speedSensors[0] == speedSensors[1]){
             return speedSensors[0];
            }else {
                if(speedSensors[0] == speedSensors[2]){
                    System.out.println("Warning: sensor 1 gave a faulty value");
                    return speedSensors[0];
                }else if(speedSensors[1] == speedSensors[2]){
                    System.out.println("Warning: sensor 0 gave a faulty value");
                    return speedSensors[1];
                }else{
                    System.out.println("Error: all 3 sensors gave different values");
                    return -1;
                }
            }
            
        }else { // Perform Dual Modular Redundancy
            if(speedSensors[0] == speedSensors[1]){
                return speedSensors[0];
            }else{
                System.out.println("Error: All 2 sensors gave different values");
                return -1;
            }
        }
    }

    /**
     * @param minSpeed mininum possible speed for cruise control, must be non-negative
     * @param maxSpeed maximum possible speed for cruise control, must be higher than zero
     * @param error value to be used as a tolerance, must be non-negative
     * @param maxAccel maximum allowed acceleration, must be higher than zero
     * @param setSpeed target speed, must be in the range of min and max speed
     * @param prevSpeed array holdig previous speed values
     * @param speedSensors current speed value from each sensor, must be at least 2 and max 3
     * @return acceleration value
     */
    public float calculateAccelaration(final int minSpeed,final int maxSpeed, final int error, final int maxAccel, final int setSpeed,final int[] preSpeed, final int[] speedSensors){

        boolean good;
        final int currentSpeed;
        LinkedList<Integer> preSpeedList = new LinkedList<>();

        good = inputChecker(minSpeed, maxSpeed, error, maxAccel, setSpeed, preSpeedList, speedSensors);
        if(!good) {
            return (float)0.0; // No change in speed
        }

        currentSpeed = getCurrentSpeed(speedSensors);
        if(currentSpeed == -1){
            return (float)0.0; // No change in speed
        }

        return (float)1.0;
    }
    
}
