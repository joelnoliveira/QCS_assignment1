package CruiseControl;

import java.util.zip.CRC32;


public class CruiseControlMain {

    private static final int MIN_SPEED = 30;
    private static final int MAX_SPEED = 130;

    private static long calculateCRC(float value) {
        CRC32 crc = new CRC32();
        int bits = Float.floatToIntBits(value);
        crc.update((bits >> 24) & 0xFF);
        crc.update((bits >> 16) & 0xFF);
        crc.update((bits >> 8) & 0xFF);
        crc.update(bits & 0xFF);
        return crc.getValue();
    }

    private static float clampSpeed(int currentSpeed, float adjustment) {
        float target = currentSpeed + adjustment;
        if (target < MIN_SPEED) return MIN_SPEED - currentSpeed;
        if (target > MAX_SPEED) return MAX_SPEED - currentSpeed;
        return adjustment;
    }

    public static float adjustAcceleration(int currentSpeed, int[] prevSpeed, int setSpeed, int maxAccel, int error) {
        if (prevSpeed == null) throw new IllegalArgumentException("prevSpeed não pode ser nulo");
        if (maxAccel < 0) throw new IllegalArgumentException("maxAccel não pode ser negativo");
        if (error < 0) throw new IllegalArgumentException("error não pode ser negativo");

        if (setSpeed < MIN_SPEED) setSpeed = MIN_SPEED;
        if (setSpeed > MAX_SPEED) setSpeed = MAX_SPEED;

        // Chamada às duas versões
        float resultA = CruiseControlFaultA.computeAcceleration(currentSpeed, prevSpeed, setSpeed, maxAccel, error);
       //meter a tua versao
        float resultB = CruiseControlFaultA.computeAcceleration(currentSpeed, prevSpeed, setSpeed, maxAccel, error);

        long crcA = calculateCRC(resultA);
        long crcB = calculateCRC(resultB);

        if (crcA == crcB) {
            return clampSpeed(currentSpeed, resultA);
        } else {
            System.out.println("⚠️ Divergência entre versões A e B detectada.");
            throw new RuntimeException("Resultados inconsistentes entre versões N-Version.");
        }
    }

    public static void main(String[] args) {
        int currentSpeed = 100;
        int[] prevSpeed = {100, 100};

        System.out.println("TC1: " + adjustAcceleration(currentSpeed, prevSpeed, 100, 10, 10));
        System.out.println("TC2: " + adjustAcceleration(currentSpeed, prevSpeed, 120, 10, 0));
        System.out.println("TC3: " + adjustAcceleration(currentSpeed, prevSpeed, 80, 20, 0));

        try {
            adjustAcceleration(100, null, 100, 10, 10);
        } catch (IllegalArgumentException e) {
            System.out.println("TC4 passou: " + e.getMessage());
        }
    }
}
