package CruiseControl;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

public class MainCruiseControl {
    
    private static CruiseControlBaseline baseline = new CruiseControlBaseline();
    private static CruiseControlFaultTolerant faultTolerant = new CruiseControlFaultTolerant();

    private static float nVersionProgramming(final int minSpeed,final int maxSpeed, final int topSpeed,final int error, final int maxAccel, final int setSpeed,final int[] prevSpeed, final int[] speedSensors){
        float result0, result1;
        // run the 2 versions of the cruise control
        result0 = baseline.calculateAcceleration(speedSensors[0], prevSpeed, setSpeed, maxAccel, error);
        result1 = faultTolerant.cruiseControl(minSpeed, maxSpeed, topSpeed, error, maxAccel, setSpeed, prevSpeed, speedSensors);

        if (result0==result1)return result0;
        else {
            System.out.println("Error: The two versions of the cruise control returned different results.");
            return (float) 0.0; // No acceleration
        }
    }

    private static float watchDog(final int minSpeed, final int maxSpeed, final int topSpeed, final int error, final int maxAccel, final int setSpeed, final int[] prevSpeed, final int[] speedSensors, long timeoutMillis) {
        ExecutorService executor = Executors.newSingleThreadExecutor();
        Future<Float> future = executor.submit(() -> nVersionProgramming(minSpeed, maxSpeed, topSpeed, error, maxAccel, setSpeed, prevSpeed, speedSensors));
    
        try {
            return future.get(timeoutMillis, TimeUnit.MILLISECONDS); // Wait for the specified timeout
        } catch (TimeoutException e) {
            System.out.println("Watchdog: Task exceeded time limit! Killing it...");
            future.cancel(true); // Interrupt task
            return 0.0f; // Default return value if killed
        } catch (Exception e) {
            e.printStackTrace();
            future.cancel(true); // Interrupt task
            return 0.0f;
        } finally {
            executor.shutdownNow(); // Ensure the executor is always shut down
        }
    }

    public static void main(String[] args) {

        int minSpeed = 30;
        int maxSpeed = 130;
        int topSpeed = 240;
        int setSpeed, maxAccel, error;
        long timeoutMillis = 1000; // 1 second timeout
        int[] prevSpeed;
        int[] sensors = new int[3];

        
        // Test Case 1
        sensors[0] = 100;
        sensors[1] = 100;
        sensors[2] = 100;
        prevSpeed = new int[]{100,100};
        setSpeed = 100;
        maxAccel = 10;
        error = 10;

        System.out.println("Test 1 final result: " + watchDog(minSpeed, maxSpeed, topSpeed, error, maxAccel, setSpeed,prevSpeed,sensors, timeoutMillis));

        // Test Case 2
        sensors[0] = 100;
        sensors[1] = 100;
        sensors[2] = 100;
        prevSpeed = new int[]{100,100};
        setSpeed = 100;
        maxAccel = 10;
        error = 5;

        System.out.println("Test 2 final result: " + watchDog(minSpeed, maxSpeed, topSpeed, error, maxAccel, setSpeed,prevSpeed,sensors, timeoutMillis));
        

        /// Test Case 3
        sensors[0] = 100;
        sensors[1] = 100;
        sensors[2] = 100;
        prevSpeed = new int[]{100, 100};
        setSpeed = 120;
        maxAccel = 10;
        error = 0;

        System.out.println("Test 3 final result: " + watchDog(minSpeed, maxSpeed, topSpeed, error, maxAccel, setSpeed,prevSpeed,sensors, timeoutMillis));
        

        // Test Case 4
        sensors[0] = 100;
        sensors[1] = 100;
        sensors[2] = 100;
        prevSpeed = new int[]{100, 100};
        setSpeed = 80;
        maxAccel = 30;
        error = 0;

        System.out.println("Test 4 final result: " + watchDog(minSpeed, maxSpeed, topSpeed, error, maxAccel, setSpeed,prevSpeed,sensors, timeoutMillis));
        

        // Test Case 5
        sensors[0] = 100;
        sensors[1] = 100;
        sensors[2] = 100;
        prevSpeed = new int[]{100, 100};
        setSpeed = 80;
        maxAccel = 10;
        error = 21;

        System.out.println("Test 5 final result: " + watchDog(minSpeed, maxSpeed, topSpeed, error, maxAccel, setSpeed,prevSpeed,sensors, timeoutMillis));

        // Test Case 6 one bad sensor
        sensors[0] = 100;
        sensors[1] = 1;
        sensors[2] = 100;
        prevSpeed = new int[]{100, 100};
        setSpeed = 80;
        maxAccel = 10;
        error = 21;
        System.out.println("Test 6 final result: " + watchDog(minSpeed, maxSpeed, topSpeed, error, maxAccel, setSpeed,prevSpeed,sensors, timeoutMillis));

        // Test Case 7 two bad sensors (all values different)
        sensors[0] = 1;
        sensors[1] = 100;
        sensors[2] = 2;

        prevSpeed = new int[]{100, 100};
        setSpeed = 80;
        maxAccel = 10;
        error = 21;
        System.out.println("Test 7 final result: " + watchDog(minSpeed, maxSpeed, topSpeed, error, maxAccel, setSpeed,prevSpeed,sensors, timeoutMillis));

        // Test Case 8 bad previous values
        sensors[0] = 100;
        sensors[1] = 100;
        sensors[2] = 100;

        prevSpeed = new int[]{-1,100};
        setSpeed = 80;
        maxAccel = 10;
        error = 21;
        System.out.println("Test 8 final result: " + watchDog(minSpeed, maxSpeed, topSpeed, error, maxAccel, setSpeed,prevSpeed,sensors, timeoutMillis));


        // Test Case 9 all bad previous values
        sensors[0] = 100;
        sensors[1] = 100;
        sensors[2] = 100;

        prevSpeed = new int[]{-1,-1};
        setSpeed = 80;
        maxAccel = 10;
        error = 21;
        System.out.println("Test 9 final result: " + watchDog(minSpeed, maxSpeed, topSpeed, error, maxAccel, setSpeed,prevSpeed,sensors, timeoutMillis));

    }
}
