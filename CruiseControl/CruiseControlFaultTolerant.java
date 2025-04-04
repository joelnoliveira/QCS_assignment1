package CruiseControl;

import java.util.zip.CRC32;
public class CruiseControlFaultTolerant {

    private int currentSpeed;
    private int currentSpeedReplica, errorReplica, maxAccelReplica, setSpeedReplica, topSpeedReplica;
    private int[] prevSpeedReplica;
    private long curentSpeedCRC, errorCRC, maxAccelCRC, setSpeedCRC, topSpeedCRC;
    private long [] prevSpeedCRC;

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

    private void calculateArrayCRCs(int[] values, long[] valuesCRCs) {
        
        for(int i = 0; i < values.length; i++) {
            valuesCRCs[i] = calculateCRC(values[i]);
        }
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

    private int checkValues(int[] val, int[] valReplica, long[] valCRC, int index){

        // Check if the value is equal to its replica
        if(val[index] == valReplica[index])return val[index];
        else{
            if(calculateCRC(val[index]) == valCRC[index]){
                valReplica[index] = val[index];
                return val[index];
            }else{
                if(calculateCRC(valReplica[index]) == valCRC[index]){
                    val[index] = valReplica[index];
                    return valReplica[index];
                }else return -1; // Error: both values are incorrect or crc is corrupted
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
            System.out.println("Error: maxSpeed must be lower or equal than topSpeed");
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
            if(speedSensors[i] < 0 || speedSensors[i] > topSpeed){
                badSensorsCount++;
                System.out.println("Warning: sensor " + i + " provided a bad value");
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
    private int voter(final int[] speedSensors, String type){
    
        if (speedSensors.length == 3) { // Perform Pair-and-a-spare approach
            
            if(speedSensors[0] == speedSensors[1]){
             return speedSensors[0];
            }else {
                if(speedSensors[0] == speedSensors[2]){
                    System.out.printf("Warning: %s 1 gave a faulty value\n",type);
                    return speedSensors[0];
                }else if(speedSensors[1] == speedSensors[2]){
                    System.out.printf("Warning: %s 0 gave a faulty value\n",type);
                    return speedSensors[1];
                }else{
                    System.out.printf("Error: all 3 %s gave different values\n",type);
                    return -1;
                }
            }
            
        }else { // Perform Dual Modular Redundancy
            if(speedSensors[0] == speedSensors[1]){
                return speedSensors[0];
            }else{
                System.out.printf("Error: All 2 %s gave different values\n", type);
                return -1;
            }
        }
    }

    private float calculateAcceleration(final int topSpeed, final int error, final int maxAccel, final int setSpeed, final int[] prevSpeed, final int currentSpeed){
        float acceleration = 0;
        float speedAvg;
        float speedDiff;
        int counter = 1;

        speedAvg = checkValues(currentSpeed, currentSpeedReplica, curentSpeedCRC);
        if(speedAvg == -1){
            System.out.println("Error: currentSpeed is corrupted");
            return 0;
        }

        for(int i=0; i< prevSpeed.length; i++){ // sum all values

            if(checkValues(topSpeed, topSpeedReplica, topSpeedCRC) == -1){
                System.out.println("Error: topSpeed is corrupted");
                return 0;
            }
            prevSpeed[i] = checkValues(prevSpeed, prevSpeedReplica, prevSpeedCRC, i); // check the validity of the value
            // check if the value is positive and lower than topSpeed
            if((prevSpeed[i] >= 0 && prevSpeed[i] <= topSpeed)){
                speedAvg+=prevSpeed[i];
                counter++;
            }
            //otherwise, print a warning
            else{
                // System.out.println("Warning: prevSpeed had a bad value and was discarded");
            }
        }

        speedAvg = speedAvg/counter; // calculate the average
        
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
        if(checkValues(error, errorReplica, errorCRC) == -1){
            System.out.println("Error: error is corrupted");
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
        // System.out.println("Returned acceleration: " + acceleration);

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
        prevSpeedCRC = new long[prevSpeed.length];

        // create CRC for the input values, no need to create CRC for minSpeed and maxSpeed as they are only needed to check setSpeed
        topSpeedCRC = calculateCRC(topSpeed);
        errorCRC = calculateCRC(error);
        maxAccelCRC = calculateCRC(maxAccel);
        setSpeedCRC = calculateCRC(setSpeed);
        calculateArrayCRCs(prevSpeed, prevSpeedCRC);

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

        currentSpeed = voter(speedSensors, "speedSensor");
        if(currentSpeed == -1){
            return (float)0.0; // No change in speed
        }

        // create CRC and replica for the current speed
        curentSpeedCRC = calculateCRC(currentSpeed);
        currentSpeedReplica = currentSpeed;

        float result0, result1, result2;
        result0 = calculateAcceleration(topSpeed, error, maxAccel, setSpeed, prevSpeed, currentSpeed);
        System.out.println("Fault Tolerant Version result0: " + result0);
        result1 = calculateAcceleration(topSpeed, error, maxAccel, setSpeed, prevSpeed, currentSpeed);
        System.out.println("Fault Tolerant Version result1: " + result1);


        if(result0 == result1){
            return result0;
        }else{ // else check the third result
            result2 = calculateAcceleration(topSpeed, error, maxAccel, setSpeed, prevSpeed, currentSpeed);
            System.out.println("Fault Tolerant Version result2: " + result1);
            if(result0 == result2){
                return result0;
            }else if(result1 == result2){
                return result1;
            }else{
                System.out.println("Error: all 3 results are different");
                return (float)0.0; // No change in speed
            }
        } 
    }
}
