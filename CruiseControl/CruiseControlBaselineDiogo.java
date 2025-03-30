package CruiseControl;

public class CruiseControlBaselineDiogo {
    public static double adjustAcceleration(int currentSpeed, int[] prevSpeed, int setSpeed, int maxAccel, int error) {
        int lowerBound = setSpeed - error;
        int upperBound = setSpeed + error;
        
        // Se a velocidade atual estiver dentro da faixa aceitável, não há necessidade de ajuste.
        if (currentSpeed >= lowerBound && currentSpeed <= upperBound) {
            return 0.0;
        }
        
        // Se a velocidade atual estiver abaixo do limite inferior, é suposto acelarar.
        if (currentSpeed < lowerBound) {
            int diff = lowerBound - currentSpeed;
            int accelMagnitude = diff < maxAccel ? diff : maxAccel;
            return accelMagnitude;
        }
        
        // Se a velocidade atual estiver acima do limite superior, é suposto desacelerar.
        int diff = currentSpeed - upperBound;
        int decelMagnitude = diff < maxAccel ? diff : maxAccel;
        // A desaceleração é negativa, então retornamos o valor negativo.
        return -decelMagnitude;
    }

    public static void main(String[] args) {
        // Test Case 1
        int currentSpeed = 100;
        int[] prevSpeed = {100, 100};
        int setSpeed1 = 100;
        int maxAccel1 = 10;
        int error1 = 10;
        System.out.println("TC1: " + adjustAcceleration(currentSpeed, prevSpeed, setSpeed1, maxAccel1, error1));

        // Test Case 2
        int setSpeed2 = 100;
        int maxAccel2 = 10;
        int error2 = 0;
        System.out.println("TC2: " + adjustAcceleration(currentSpeed, prevSpeed, setSpeed2, maxAccel2, error2));

        // Test Case 3
        int setSpeed3 = 120;
        int maxAccel3 = 10;
        int error3 = 0;
        System.out.println("TC3: " + adjustAcceleration(currentSpeed, prevSpeed, setSpeed3, maxAccel3, error3));

        // Test Case 4
        int setSpeed4 = 80;
        int maxAccel4 = 30;
        int error4 = 0;
        System.out.println("TC4: " + adjustAcceleration(currentSpeed, prevSpeed, setSpeed4, maxAccel4, error4));

        // Test Case 5

        int setSpeed5 = 80;
        int maxAccel5 = 10;
        int error5 = 21;
        System.out.println("TC5: " + adjustAcceleration(currentSpeed, prevSpeed, setSpeed5, maxAccel5, error5));
    }
}

