package CruiseControl;

public class CruiseControl {

    private static float calculate_acceleration(int current_speed, int[] prev_speed, int set_speed, int max_accel, int error){

        float acceleration = 0;
        float speed_avg = (float)(current_speed + prev_speed[0] + prev_speed[1]) / 3;
        float speed_diff;
        
        // print received values
        System.out.println("current_speed: " + current_speed);
        System.out.println("prev_speed: [" + prev_speed[0] + ", " + prev_speed[1] + "]");
        System.out.println("set_speed: " + set_speed);
        System.out.println("max_accel: " + max_accel);
        System.out.println("error: " + error);

        //if abs difference between the average and set_speed is bigger than the error
        if (Math.abs(speed_avg - set_speed) > error){

            speed_diff = set_speed - current_speed;
            
            //if the absolute of the difference is bigger than the max_accel
            if (Math.abs(speed_diff) > max_accel)acceleration = max_accel;
            else acceleration = speed_diff;

        }

        System.out.println("Returned acceleration: " + acceleration);

        return acceleration;
    }
    
    // main function
    public static void main(String[] args) {
        
        int current_speed, set_speed, max_accel, error;
        int[] prev_speed;

        // Test Case 1
        current_speed = 100;
        prev_speed = new int[]{100,100};
        set_speed = 100;
        max_accel = 10;
        error = 10;

        calculate_acceleration(current_speed, prev_speed, set_speed, max_accel, error);

        // Test Case 2
        current_speed = 100;
        prev_speed = new int[]{100,100};
        set_speed = 100;
        max_accel = 10;
        error = 5;

        calculate_acceleration(current_speed, prev_speed, set_speed, max_accel, error);

        /// Test Case 3
        current_speed = 100;
        prev_speed = new int[]{100, 100};
        set_speed = 120;
        max_accel = 10;
        error = 0;

        calculate_acceleration(current_speed, prev_speed, set_speed, max_accel, error);

        // Test Case 4
        current_speed = 100;
        prev_speed = new int[]{100, 100};
        set_speed = 80;
        max_accel = 30;
        error = 0;

        calculate_acceleration(current_speed, prev_speed, set_speed, max_accel, error);

        // Test Case 5
        current_speed = 100;
        prev_speed = new int[]{100, 100};
        set_speed = 80;
        max_accel = 10;
        error = 21;

        calculate_acceleration(current_speed, prev_speed, set_speed, max_accel, error);
    }

}
