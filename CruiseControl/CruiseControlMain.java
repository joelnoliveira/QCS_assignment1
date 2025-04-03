package CruiseControl;

import java.util.concurrent.*;
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
    
    /**
     * Ajusta a aceleração do Cruise Control utilizando redundância de hardware.
     * Recebe um array de 3 sensores e os demais parâmetros necessários.
     */
    public static float adjustAcceleration(int[] sensorReadings, int[] prevSpeed, int setSpeed, int maxAccel, int error) {
        if (sensorReadings == null) throw new IllegalArgumentException("sensorReadings não pode ser nulo.");
        if (sensorReadings.length != 3) throw new IllegalArgumentException("São necessários exatamente 3 sensores de velocidade.");
        
        // Chama a função computeAcceleration que usa os 3 sensores para determinar currentSpeed via votação.
        float resultA = CruiseControlFaultA.computeAcceleration(sensorReadings, prevSpeed, setSpeed, maxAccel, error);
        float resultB = CruiseControlFaultA.computeAcceleration(sensorReadings, prevSpeed, setSpeed, maxAccel, error);
        
        long crcA = calculateCRC(resultA);
        long crcB = calculateCRC(resultB);
        
        if (crcA == crcB) {
            // Para o clamping, usamos o currentSpeed obtido via votação dos sensores.
            int currentSpeed = CruiseControlFaultA.getVotedSpeed(sensorReadings);
            return clampSpeed(currentSpeed, resultA);
        } else {
            System.out.println("⚠️ Divergência entre versões detectada.");
            throw new RuntimeException("Resultados inconsistentes entre versões N-Version.");
        }
    }
    
    /**
     * Executa a função de ajuste de aceleração com watchdog timer.
     * Se a execução exceder o tempo limite, dispara um timeout.
     */
    public static float runWithWatchdog(final int[] sensorReadings, final int[] prevSpeed, 
                                          final int setSpeed, final int maxAccel, final int error, 
                                          long timeoutMillis) {
        ExecutorService executor = Executors.newSingleThreadExecutor();
        try {
            Future<Float> future = executor.submit(new Callable<Float>() {
                public Float call() {
                    return adjustAcceleration(sensorReadings, prevSpeed, setSpeed, maxAccel, error);
                }
            });
            return future.get(timeoutMillis, TimeUnit.MILLISECONDS);
        } catch (TimeoutException te) {
            throw new RuntimeException("Timeout: a execução ultrapassou " + timeoutMillis + " ms.");
        } catch (Exception e) {
            throw new RuntimeException("Erro durante a execução: " + e.getMessage());
        } finally {
            executor.shutdownNow();
        }
    }
    
    public static void main(String[] args) {
        // Exemplo de leituras de 3 sensores (redundância de hardware)
        int[] sensorReadings = {100, 100, 100};  // leituras consistentes
        int[] prevSpeed = {100, 100};
        int setSpeed = 100;
        int maxAccel = 10;
        int error = 10;
        long timeoutMillis = 1000;
        
        try {
            System.out.println("TC1: " + runWithWatchdog(sensorReadings, prevSpeed, setSpeed, maxAccel, error, timeoutMillis));
            
            // Teste com divergência: um sensor apresenta valor diferente
            int[] sensorReadings2 = {100, 95, 100};
            System.out.println("TC2: " + runWithWatchdog(sensorReadings2, prevSpeed, setSpeed, maxAccel, error, timeoutMillis));
            
            // Teste com array de sensores inválido (menos de 3 elementos)
            try {
                int[] sensorReadingsInvalid = {100, 100};
                System.out.println("TC3: " + runWithWatchdog(sensorReadingsInvalid, prevSpeed, setSpeed, maxAccel, error, timeoutMillis));
            } catch (IllegalArgumentException e) {
                System.out.println("TC3 passou: " + e.getMessage());
            }
        } catch (Exception e) {
            System.out.println("Erro nos testes: " + e.getMessage());
        }
    }
}
