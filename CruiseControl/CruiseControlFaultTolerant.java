package CruiseControl;

import java.util.LinkedList;
import java.util.zip.CRC32;
public class CruiseControlFaultTolerant {

    private int currentSpeed;
    private int currentSpeedReplica, errorReplica, maxAccelReplica, setSpeedReplica, topSpeedReplica;
    private int[] prevSpeedReplica;
    private long curentSpeedCRC, errorCRC, maxAccelCRC, setSpeedCRC, topSpeedCRC, prevSpeedCRC;

    private long calculateCRC(int value) {
        byte[] bytes = new byte[4];
        CRC32 crc = new CRC32();

        bytes[0] = (byte)(value >> 24); // most significant byte
        bytes[1] = (byte)(value >> 16);
        bytes[2] = (byte)(value >> 8);
        bytes[3] = (byte)(value); // least significant byte
        
        crc.update(bytes);
        return crc.getValue();
    }

    private long calculateCRC(int[] values) {
        CRC32 crc = new CRC32();
        byte[] bytes = new byte[values.length * 4]; // Continuous byte array

        for (int i = 0; i < values.length; i++) {
            int value = values[i];
            int baseIndex = i * 4;
            bytes[baseIndex] = (byte)(value >> 24); // MSB
            bytes[baseIndex + 1] = (byte)(value >> 16);
            bytes[baseIndex + 2] = (byte)(value >> 8);
            bytes[baseIndex + 3] = (byte)(value);   // LSB
        }

        crc.update(bytes); // Process all bytes at once
        return crc.getValue();
    }

    private int checkValues(int val, int valReplica, long valCRC){

        // Check if the value is equal to its replica
        if(val == valReplica)return val;
        else{
            if(calculateCRC(val) == valCRC){
                valReplica = val;
                return val;
            }else{
                if(calculateCRC(valReplica) == valCRC){
                    val = valReplica;
                    return valReplica;
                }else return -1; // Error: both values are incorrect
            }
                
        }
    }

    private int[] checkValues(int[] val, int[] valReplica, long valCRC){

        // Check if the value is equal to its replica
        if(val == valReplica)return val;
        else{
            if(calculateCRC(val) == valCRC){
                valReplica = val;
                return val;
            }else{
                if(calculateCRC(valReplica) == valCRC){
                    val = valReplica;
                    return valReplica;
                }else return null; // Error: both values are incorrect
            }
                
        }
    }

    /**
     * Checks the input values for the cruise control system.
     * @param minSpeed minimum possible speed for cruise control, must be non-negative
     * @param maxSpeed maximum possible speed for cruise control, must be higher than zero
     * @param topSpeed maximum possible speed of the car, must be higher than zero
     * @param error value to be used as a tolerance, must be non-negative
     * @param maxAccel maximum allowed acceleration, must be higher than zero
     * @param setSpeed target speed, must be in the range of min and max speed
     * @param prevSpeed array holding previous speed values
     * @param speedSensors current speed value from each sensor, must be at least 2 and max 3
     * @return
     */
    private boolean inputChecker(final int minSpeed,final int maxSpeed, final int topSpeed, final int error, final int maxAccel, final int setSpeed, final int[] prevSpeed, final int[] speedSensors){

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
        if(maxSpeed > topSpeed){
            System.out.println("Error: maxSpeed must be lower than topSpeed");
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
        if(prevSpeed == null){
            System.out.println("Error: prevSpeed must not be null");
            return false;
        }
        if(prevSpeed.length == 0){
            System.out.println("Error: prevSpeed must not be empty");
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

    private float calculateAcceleration(final int topSpeed, final int error, final int maxAccel, final int setSpeed, final int[] prevSpeed, final int currentSpeed){
        float acceleration = 0;
        float speedAvg;
        float speedDiff;

        speedAvg = checkValues(currentSpeed, currentSpeedReplica, curentSpeedCRC);
        if(speedAvg == -1){
            System.out.println("Error: currentSpeed is corrupted");
            return 0;
        }

        if(checkValues(prevSpeed, prevSpeed, currentSpeed)==null){
            System.out.println("Error: prevSpeed is corrupted");
            return 0;
        }

        // TODO: reject values that are negative or bigger than topSpeed
        for(int i=0; i< prevSpeed.length; i++){ // sum all values
            speedAvg+=prevSpeed[i];
        }

        speedAvg = speedAvg/prevSpeed.length+1; // count of previous values plus current speed
        
        // print received values
        // System.out.println("currentSpeed: " + currentSpeed);
        // System.out.println("prevSpeed: [" + prevSpeed[0] + ", " + prevSpeed[1] + "]");
        // System.out.println("setSpeed: " + setSpeed);
        // System.out.println("maxAccel: " + maxAccel);
        // System.out.println("error: " + error);

        if(checkValues(setSpeed, setSpeedReplica, setSpeedCRC) == -1){
            System.out.println("Error: setSpeed is corrupted");
            return 0;
        }
        //if abs difference between the average and setSpeed is bigger than the error
        if (Math.abs(speedAvg - setSpeed) > error){

            speedDiff = setSpeed - currentSpeed;
            
            if(checkValues(maxAccel, maxAccelReplica, maxAccelCRC) == -1){
                System.out.println("Error: maxAccel is corrupted");
                return 0;
            }
            //if the absolute of the difference is bigger than the maxAccel
            if (Math.abs(speedDiff) > maxAccel)acceleration = maxAccel;
            else acceleration = speedDiff;

        }
        System.out.println("Returned acceleration: " + acceleration);

        return acceleration;
    }

    /**
     * @param minSpeed mininum possible speed for cruise control, must be non-negative
     * @param maxSpeed maximum possible speed for cruise control, must be higher than zero
     * @param topSpeed maximum possible speed of the car, must be higher than zero
     * @param error value to be used as a tolerance, must be non-negative
     * @param maxAccel maximum allowed acceleration, must be higher than zero
     * @param setSpeed target speed, must be in the range of min and max speed
     * @param prevSpeed array holdig previous speed values
     * @param speedSensors current speed value from each sensor, must be at least 2 and max 3
     * @return acceleration value
     */
    public float cruiseControl(final int minSpeed,final int maxSpeed, final int topSpeed,final int error, final int maxAccel, final int setSpeed,final int[] prevSpeed, final int[] speedSensors){

        boolean good;

        // create CRC for the input values, no need to create CRC for minSpeed and maxSpeed as they are only needed to check setSpeed
        topSpeedCRC = calculateCRC(topSpeed);
        errorCRC = calculateCRC(error);
        maxAccelCRC = calculateCRC(maxAccel);
        setSpeedCRC = calculateCRC(setSpeed);
        prevSpeedCRC = calculateCRC(prevSpeed);

        // create replicas for the input values
        topSpeedReplica = topSpeed;
        errorReplica = error;
        maxAccelReplica = maxAccel;
        setSpeedReplica = setSpeed;
        prevSpeedReplica = new int[prevSpeed.length];
        for(int i = 0; i < prevSpeed.length; i++){
            prevSpeedReplica[i] = prevSpeed[i];
        }

        // check the input
        good = inputChecker(minSpeed, maxSpeed, topSpeed, error, maxAccel, setSpeed, prevSpeed, speedSensors);
        if(!good) {
            return (float)0.0; // No change in speed
        }

        currentSpeed = getCurrentSpeed(speedSensors);
        if(currentSpeed == -1){
            return (float)0.0; // No change in speed
        }

        // create CRC and replica for the current speed
        curentSpeedCRC = calculateCRC(currentSpeed);
        currentSpeedReplica = currentSpeed;

        return calculateAcceleration(topSpeed, error, maxAccel, setSpeed, prevSpeed, currentSpeed);
    }

    public static void main(String[] args) {
        
    
}
