package CruiseControl;

public class CruiseControl {

    private static float calculateAcceleration(int currentSpeed, int[] prevSpeed, int setSpeed, int maxAccel, int error){

        float acceleration = 0;
        float speed_avg;
        float speed_diff;

        speed_avg = currentSpeed;

        for(int i=0; i< prevSpeed.length; i++){ // sum all values
            speed_avg+=prevSpeed[i];
        }

        speed_avg = speed_avg/prevSpeed.length+1; // count of previous values plus current speed
        
        // print received values
        System.out.println("currentSpeed: " + currentSpeed);
        System.out.println("prevSpeed: [" + prevSpeed[0] + ", " + prevSpeed[1] + "]");
        System.out.println("setSpeed: " + setSpeed);
        System.out.println("maxAccel: " + maxAccel);
        System.out.println("error: " + error);

        //if abs difference between the average and setSpeed is bigger than the error
        if (Math.abs(speed_avg - setSpeed) > error){

            speed_diff = setSpeed - currentSpeed;
            
            //if the absolute of the difference is bigger than the maxAccel
            if (Math.abs(speed_diff) > maxAccel)acceleration = maxAccel;
            else acceleration = speed_diff;

        }

        System.out.println("Returned acceleration: " + acceleration);

        return acceleration;
    }
    
    // main function
    public static void main(String[] args) {
        
        int currentSpeed, setSpeed, maxAccel, error;
        int[] prevSpeed;

        // Test Case 1
        currentSpeed = 100;
        prevSpeed = new int[]{100,100};
        setSpeed = 100;
        maxAccel = 10;
        error = 10;

        calculateAcceleration(currentSpeed, prevSpeed, setSpeed, maxAccel, error);

        // Test Case 2
        currentSpeed = 100;
        prevSpeed = new int[]{100,100};
        setSpeed = 100;
        maxAccel = 10;
        error = 5;

        calculateAcceleration(currentSpeed, prevSpeed, setSpeed, maxAccel, error);

        /// Test Case 3
        currentSpeed = 100;
        prevSpeed = new int[]{100, 100};
        setSpeed = 120;
        maxAccel = 10;
        error = 0;

        calculateAcceleration(currentSpeed, prevSpeed, setSpeed, maxAccel, error);

        // Test Case 4
        currentSpeed = 100;
        prevSpeed = new int[]{100, 100};
        setSpeed = 80;
        maxAccel = 30;
        error = 0;

        calculateAcceleration(currentSpeed, prevSpeed, setSpeed, maxAccel, error);

        // Test Case 5
        currentSpeed = 100;
        prevSpeed = new int[]{100, 100};
        setSpeed = 80;
        maxAccel = 10;
        error = 21;

        calculateAcceleration(currentSpeed, prevSpeed, setSpeed, maxAccel, error);
    }

}
