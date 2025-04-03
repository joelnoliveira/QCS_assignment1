package CruiseControl;

import java.util.zip.CRC32;

public class CruiseControlFaultA {

    private static final int MIN_SPEED = 30;
    private static final int MAX_SPEED = 130;

    // Calcula o CRC32 para um float (para outputs)
    private static long calculateCRC(float value) {
        CRC32 crc = new CRC32();
        int bits = Float.floatToIntBits(value);
        crc.update((bits >> 24) & 0xFF);
        crc.update((bits >> 16) & 0xFF);
        crc.update((bits >> 8) & 0xFF);
        crc.update(bits & 0xFF);
        return crc.getValue();
    }
    
    // Calcula o CRC32 para um int (para inputs)
    private static long calculateCRC(int value) {
        CRC32 crc = new CRC32();
        crc.update((value >> 24) & 0xFF);
        crc.update((value >> 16) & 0xFF);
        crc.update((value >> 8) & 0xFF);
        crc.update(value & 0xFF);
        return crc.getValue();
    }
    
    // Verifica a integridade de um valor inteiro, comparando-o com a sua réplica e o CRC armazenado.
    private static int checkValue(int value, int replica, long storedCRC) {
        if (value == replica) {
            return value;
        } else if (calculateCRC(value) == storedCRC) {
            return value;
        } else if (calculateCRC(replica) == storedCRC) {
            return replica;
        } else {
            throw new RuntimeException("Valor crítico corrompido.");
        }
    }

    // Função "core" que realiza o cálculo da aceleração sem redundância
    private static float computeAccelerationCore(int currentSpeed, int setSpeed, int maxAccel, int error) {
        int lowerBound = setSpeed - error;
        int upperBound = setSpeed + error;
        if (currentSpeed >= lowerBound && currentSpeed <= upperBound) {
            return 0.0f;
        } else if (currentSpeed < lowerBound) {
            int diff = lowerBound - currentSpeed;
            return (diff < maxAccel) ? diff : maxAccel;
        } else { // currentSpeed > upperBound
            int diff = currentSpeed - upperBound;
            return (diff < maxAccel) ? -diff : -maxAccel;
        }
    }

    /**
     * Função otimizada com hot spare (pair and spare "quente") e
     * verificação da integridade dos inputs críticos usando CRC.
     */
    public static float computeAcceleration(int currentSpeed, int[] prevSpeed, int setSpeed, int maxAccel, int error) {
        // Validação de inputs essenciais
        if (prevSpeed == null) {
            throw new IllegalArgumentException("Array de velocidades anteriores não pode ser nulo.");
        }
        if (maxAccel < 0) {
            throw new IllegalArgumentException("Aceleração máxima não pode ser negativa: " + maxAccel);
        }
        if (error < 0) {
            throw new IllegalArgumentException("Margem de erro não pode ser negativa: " + error);
        }
        if (currentSpeed < 0) {
            throw new IllegalArgumentException("Velocidade atual não pode ser negativa: " + currentSpeed);
        }
        
        // Ajusta setSpeed para estar dentro dos limites realistas
        if (setSpeed < MIN_SPEED) {
            setSpeed = MIN_SPEED;
        } else if (setSpeed > MAX_SPEED) {
            setSpeed = MAX_SPEED;
        }
        
        // Replicação e verificação dos inputs críticos
        int setSpeedReplica = setSpeed;
        long setSpeedCRC = calculateCRC(setSpeed);
        setSpeed = checkValue(setSpeed, setSpeedReplica, setSpeedCRC);
        
        int maxAccelReplica = maxAccel;
        long maxAccelCRC = calculateCRC(maxAccel);
        maxAccel = checkValue(maxAccel, maxAccelReplica, maxAccelCRC);
        
        int errorReplica = error;
        long errorCRC = calculateCRC(error);
        error = checkValue(error, errorReplica, errorCRC);

        // --- Hot Spare: Executa as três cópias "quentes" ---
        float primary = computeAccelerationCore(currentSpeed, setSpeed, maxAccel, error);
        float backup  = computeAccelerationCore(currentSpeed, setSpeed, maxAccel, error);
        float spare   = computeAccelerationCore(currentSpeed, setSpeed, maxAccel, error);

        long crcPrimary = calculateCRC(primary);
        long crcBackup  = calculateCRC(backup);
        long crcSpare   = calculateCRC(spare);

        float chosenResult;
        // Votação por maioria: se duas cópias tiverem o mesmo CRC, essa é a escolhida.
        if (crcPrimary == crcBackup || crcPrimary == crcSpare) {
            chosenResult = primary;
        } else if (crcBackup == crcSpare) {
            chosenResult = backup;
        } else {
            throw new RuntimeException("Erro: resultados inconsistentes entre hot spare e os módulos de pair.");
        }

        // Validação de output: a aceleração deve estar dentro de -maxAccel e +maxAccel.
        if (chosenResult < -maxAccel || chosenResult > maxAccel) {
            throw new RuntimeException("Erro: aceleração fora dos limites físicos permitidos. Valor: " + chosenResult);
        }

        // Ajuste final para garantir que a velocidade alvo (currentSpeed + aceleração) esteja entre MIN_SPEED e MAX_SPEED.
        float targetSpeed = currentSpeed + chosenResult;
        if (targetSpeed < MIN_SPEED) {
            chosenResult = MIN_SPEED - currentSpeed;
        } else if (targetSpeed > MAX_SPEED) {
            chosenResult = MAX_SPEED - currentSpeed;
        }

        return chosenResult;
    }

    public static void main(String[] args) {
        int currentSpeed = 100;
        int[] prevSpeed = {100, 100};

        // Test Cases com a versão otimizada com hot spare e verificação de inputs
        System.out.println("TC1: " + computeAcceleration(currentSpeed, prevSpeed, 100, 10, 10));
        System.out.println("TC2: " + computeAcceleration(currentSpeed, prevSpeed, 100, 10, 0));
        System.out.println("TC3: " + computeAcceleration(currentSpeed, prevSpeed, 120, 10, 0));
        System.out.println("TC4: " + computeAcceleration(currentSpeed, prevSpeed, 80, 30, 0));
        System.out.println("TC5: " + computeAcceleration(currentSpeed, prevSpeed, 80, 10, 21));

        // Exemplos de testes com inputs inválidos
        try {
            System.out.println("TC6: " + computeAcceleration(100, null, 100, 10, 10));
        } catch (IllegalArgumentException e) {
            System.out.println("TC6 com erro: " + e.getMessage());
        }
        try {
            System.out.println("TC7: " + computeAcceleration(100, new int[]{100, 100}, 100, -10, 10));
        } catch (IllegalArgumentException e) {
            System.out.println("TC7 com erro: " + e.getMessage());
        }
        try {
            System.out.println("TC8: " + computeAcceleration(100, new int[]{100, 100}, 100, 10, -10));
        } catch (IllegalArgumentException e) {
            System.out.println("TC8 com erro: " + e.getMessage());
        }
        System.out.println("TC9: " + computeAcceleration(currentSpeed, prevSpeed, 10, 10, 5));
        System.out.println("TC10: " + computeAcceleration(currentSpeed, prevSpeed, 150, 10, 5));
    }
}
