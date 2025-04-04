package CruiseControl;

public class CruiseControlBaseline {

    public float calculateAcceleration(int currentSpeed, int[] prevSpeed, int setSpeed, int maxAccel, int error){

        float acceleration = 0;
        float speed_avg;
        float speed_diff;

        speed_avg = currentSpeed;

        for(int i=0; i< prevSpeed.length; i++){ // sum all values
            speed_avg+=prevSpeed[i];
        }

        speed_avg = speed_avg/(prevSpeed.length+1); // count of previous values plus current speed
        
        // print received values
        // System.out.println("currentSpeed: " + currentSpeed);
        // System.out.println("prevSpeed: [" + prevSpeed[0] + ", " + prevSpeed[1] + "]");
        // System.out.println("setSpeed: " + setSpeed);
        // System.out.println("maxAccel: " + maxAccel);
        // System.out.println("error: " + error);

        //if abs difference between the average and setSpeed is bigger than the error
        if (Math.abs(speed_avg - setSpeed) > error){

            speed_diff = setSpeed - currentSpeed;
            
            //if the absolute of the difference is bigger than the maxAccel
            if (Math.abs(speed_diff) > maxAccel)acceleration = maxAccel;
            else acceleration = speed_diff;

        }

        System.out.println("Baseline result: " + acceleration);

        return acceleration;
    }
}
