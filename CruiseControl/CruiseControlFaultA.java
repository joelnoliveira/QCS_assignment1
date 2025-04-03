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

    // Novo método: obtém a velocidade a partir de 3 sensores (hardware redundancy)
    public static int getVotedSpeed(int[] sensors) {
        if (sensors == null) {
            throw new IllegalArgumentException("As leituras dos sensores não podem ser nulas.");
        }
        if (sensors.length != 3) {
            throw new IllegalArgumentException("São necessários exatamente 3 sensores de velocidade.");
        }
        // Votação: se pelo menos dois sensores concordarem, esse é o valor adotado.
        if (sensors[0] == sensors[1]) return sensors[0];
        else if (sensors[0] == sensors[2]) return sensors[0];
        else if (sensors[1] == sensors[2]) return sensors[1];
        else {
            System.out.println("Warning: Divergência entre os 3 sensores. Utilizando o valor do primeiro sensor.");
            return sensors[0];
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
     * Agora, a função recebe um array de 3 sensores para determinar a velocidade atual.
     */
    public static float computeAcceleration(int[] sensorReadings, int[] prevSpeed, int setSpeed, int maxAccel, int error) {
        // Validação de inputs essenciais
        if (sensorReadings == null) {
            throw new IllegalArgumentException("Array de leituras dos sensores não pode ser nulo.");
        }
        if (sensorReadings.length != 3) {
            throw new IllegalArgumentException("São necessários exatamente 3 sensores de velocidade.");
        }
        if (prevSpeed == null) {
            throw new IllegalArgumentException("Array de velocidades anteriores não pode ser nulo.");
        }
        if (maxAccel < 0) {
            throw new IllegalArgumentException("Aceleração máxima não pode ser negativa: " + maxAccel);
        }
        if (error < 0) {
            throw new IllegalArgumentException("Margem de erro não pode ser negativa: " + error);
        }
        
        // Obtém a velocidade atual através da votação dos 3 sensores
        int currentSpeed = getVotedSpeed(sensorReadings);
        
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
        int[] sensorReadings = {100, 100, 100};  // valores consistentes
        int[] prevSpeed = {100, 100};
        int setSpeed = 100;
        int maxAccel = 10;
        int error = 10;

        // Test Cases com a versão otimizada com hot spare e verificação de inputs
        System.out.println("TC1: " + computeAcceleration(sensorReadings, prevSpeed, 100, 10, 10));
        System.out.println("TC2: " + computeAcceleration(sensorReadings, prevSpeed, 100, 10, 0));
        System.out.println("TC3: " + computeAcceleration(sensorReadings, prevSpeed, 120, 10, 0));
        System.out.println("TC4: " + computeAcceleration(sensorReadings, prevSpeed, 80, 30, 0));
        System.out.println("TC5: " + computeAcceleration(sensorReadings, prevSpeed, 80, 10, 21));

        // Exemplos de testes com inputs inválidos
        try {
            System.out.println("TC6: " + computeAcceleration(sensorReadings, null, 100, 10, 10));
        } catch (IllegalArgumentException e) {
            System.out.println("TC6 com erro: " + e.getMessage());
        }
        try {
            System.out.println("TC7: " + computeAcceleration(sensorReadings, new int[]{100, 100}, 100, -10, 10));
        } catch (IllegalArgumentException e) {
            System.out.println("TC7 com erro: " + e.getMessage());
        }
        try {
            System.out.println("TC8: " + computeAcceleration(sensorReadings, new int[]{100, 100}, 100, 10, -10));
        } catch (IllegalArgumentException e) {
            System.out.println("TC8 com erro: " + e.getMessage());
        }
        System.out.println("TC9: " + computeAcceleration(sensorReadings, prevSpeed, 10, 10, 5));
        System.out.println("TC10: " + computeAcceleration(sensorReadings, prevSpeed, 150, 10, 5));
        try {
            int[] sensorReadingsInvalid = {100, 100}; // menos que 3 sensores
            System.out.println("TC11: " + computeAcceleration(sensorReadingsInvalid, prevSpeed, 100, 10, 10));
        } catch (RuntimeException e) {
            System.out.println("TC11 com erro: " + e.getMessage());
        }
    }
}
