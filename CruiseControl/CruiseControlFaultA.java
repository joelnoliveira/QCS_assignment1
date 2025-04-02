package CruiseControl;

import java.io.PrintWriter;
import java.util.zip.CRC32;


public class CruiseControlFaultA {

    private static long calculateCRC(float value) {
        CRC32 crc = new CRC32();
        int bits = Float.floatToIntBits(value);
        crc.update((bits >> 24) & 0xFF);
        crc.update((bits >> 16) & 0xFF);
        crc.update((bits >> 8) & 0xFF);
        crc.update(bits & 0xFF);
        return crc.getValue();
    }

    private static final int MIN_SPEED = 30;
    private static final int MAX_SPEED = 130;

    public static float adjustAcceleration(int currentSpeed, int[] prevSpeed, int setSpeed, int maxAccel, int error) {
        // Validação dos inputs
        if (prevSpeed == null) {
            throw new IllegalArgumentException("Array de velocidades anteriores não pode ser nulo.");
        }
        if (maxAccel < 0) {
            throw new IllegalArgumentException("Aceleração máxima não pode ser negativa: " + maxAccel);
        }
        if (error < 0) {
            throw new IllegalArgumentException("Margem de erro não pode ser negativa: " + error);
        }
        if (setSpeed < MIN_SPEED) {
            setSpeed = MIN_SPEED;
        } else if (setSpeed > MAX_SPEED) {
            setSpeed = MAX_SPEED;
        }

        // Execução redundante: cálculo feito duas vezes para verificação de consistência
        float result1 = computeAcceleration(currentSpeed, prevSpeed, setSpeed, maxAccel, error);
        float result1Copy = result1; // Variável redundante
       
        float result2 = computeAcceleration(currentSpeed, prevSpeed, setSpeed, maxAccel, error);
        float result2Copy = result2; // Variável redundante
        // Verificação de consistência da variável redundante
        if (result1 != result1Copy) {
            throw new RuntimeException("Inconsistência detetada entre variável original e redundante.");
        }

        // Verificação de consistência da variável redundante
        if (result2 != result2Copy) {
            throw new RuntimeException("Inconsistência detetada entre variável original e redundante.");
        }

        // Verificação de consistência usando CRC
        long crc1 = calculateCRC(result1);
        long crc2 = calculateCRC(result2);

        if (crc1 != crc2) {
            System.out.println("⚠️ Inconsistência detectada via CRC! crc1=" + crc1 + ", crc2=" + crc2);
            // Temos de decidir o que fazer em caso de inconsistência.
            // Aqui, apenas lançamos uma exceção, mas isso pode ser ajustado conforme necessário.
        }

        // Ajusta para garantir que a velocidade alvo (currentSpeed + aceleração) permaneça entre MIN_SPEED e MAX_SPEED
        float targetSpeed = currentSpeed + result1;
        if (targetSpeed < MIN_SPEED) {
            result1 = MIN_SPEED - currentSpeed;
        } else if (targetSpeed > MAX_SPEED) {
            result1 = MAX_SPEED - currentSpeed;
        }

        return result1;
    }

    public static float computeAcceleration(int currentSpeed, int[] prevSpeed, int setSpeed, int maxAccel, int error) {
        try {
            int lowerBound = setSpeed - error;
            int upperBound = setSpeed + error;

            // Se a velocidade atual está dentro da faixa aceitável, retorna 0
            if (currentSpeed >= lowerBound && currentSpeed <= upperBound) {
                return 0;
            }

            // Se a velocidade atual está abaixo do limite inferior, acelera
            if (currentSpeed < lowerBound) {
                int diff = lowerBound - currentSpeed;
                int accelMagnitude = (diff < maxAccel) ? diff : maxAccel;
                return accelMagnitude;
            }

            // Se a velocidade atual está acima do limite superior, desacelera (valor negativo)
            int diff = currentSpeed - upperBound;
            int decelMagnitude = (diff < maxAccel) ? diff : maxAccel;
            return -decelMagnitude;
        } catch (Exception e) {
            throw new RuntimeException("Erro no cálculo da aceleração: " + e.getMessage());
        }
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

        // Test Case 6 prevSpeed nulo
        try {
            System.out.println("TC6: " + adjustAcceleration(100, null, 100, 10, 10));
        } catch (IllegalArgumentException e) {
            System.out.println("TC6 com erro: " + e.getMessage());
        }

        // Test Case 7 maxAccel negativo
        try {
            System.out.println("TC7: " + adjustAcceleration(100, new int[]{100, 100}, 100, -10, 10));
        } catch (IllegalArgumentException e) {
            System.out.println("TC7 com erro: " + e.getMessage());
        }

        // Test Case 8 error negativo
        try {
            System.out.println("TC8: " + adjustAcceleration(100, new int[]{100, 100}, 100, 10, -10));
        } catch (IllegalArgumentException e) {
            System.out.println("TC8 com erro: " + e.getMessage());
        }

        // Test Case 9 setSpeed inferior ao mínimo
        int setSpeedLow = 10;
        System.out.println("TC9: " + adjustAcceleration(currentSpeed, prevSpeed, setSpeedLow, 10, 5));

        // Test Case 10 setSpeed superior ao máximo
        int setSpeedHigh = 150;
        System.out.println("TC10: " + adjustAcceleration(currentSpeed, prevSpeed, setSpeedHigh, 10, 5));

    }
}

