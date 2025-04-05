package ClimateControl;

import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

public class ClimateControlFault {

    // Constantes para limites de temperatura e duty cycle
    private static final int MIN_TEMP = -20;
    private static final int MAX_TEMP = 50;
    private static final int MIN_DUTY = 0;
    private static final int MAX_DUTY = 100;
    
    // Configuração do logger
    private static final String LOG_FILE = "climate_control.log";
    private static PrintWriter logWriter;
    
    static {
        try {
            // Inicia o logger - append=true para manter os logs anteriores
            logWriter = new PrintWriter(new FileWriter(LOG_FILE, true), true);
            logInfo("ClimateControl system initialized at " + getCurrentTimestamp());
        } catch (IOException e) {
            System.err.println("Failed to initialize log file: " + e.getMessage());
            // Fallback para saída padrão se não conseguir criar arquivo
            logWriter = new PrintWriter(System.out, true);
        }
    }
    
    private static String getCurrentTimestamp() {
        return new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS").format(new Date());
    }
    
    private static void logInfo(String message) {
        logWriter.println("[INFO] " + getCurrentTimestamp() + " - " + message);
    }
    
    private static void logWarning(String message) {
        logWriter.println("[WARNING] " + getCurrentTimestamp() + " - " + message);
    }
    
    private static void logError(String message) {
        logWriter.println("[ERROR] " + getCurrentTimestamp() + " - " + message);
    }

    /**
     * Classe auxiliar para encapsular o resultado do ajuste dos duty cycles, incluindo
     * um flag que indica se ocorreram erros durante a validação.
     */
    public static class AdjustmentResult {
        public List<List<Integer>> result;
        public boolean errorOccurred;
        
        public AdjustmentResult(List<List<Integer>> result, boolean errorOccurred) {
            this.result = result;
            this.errorOccurred = errorOccurred;
        }
        
        @Override
        public String toString() {
            return "AdjustmentResult { result = " + result + ", errorOccurred = " + errorOccurred + " }";
        }
    }
    
    /**
     * Ajusta os duty cycles dos ventiladores com base na temperatura desejada,
     * margem de erro, temperaturas atuais e duty cycles atuais dos ventiladores.
     * 
     * Aplica validação de inputs, execução redundante (replicação) e tratamento
     * de exceções para garantir tolerância a falhas.
     * 
     * @param desiredTemp Temperatura desejada (em graus Celsius).
     * @param error Margem de erro permitida (em graus Celsius).
     * @param temps Array de temperaturas atuais (em graus Celsius) para cada zona.
     * @param fans Lista de listas de duty cycles atuais (em porcentagem) para cada zona.
     * 
     * Retorna um AdjustmentResult que contém o output ajustado e um flag indicando se houve
     * algum erro (ou correção) na validação.
     */
    public static AdjustmentResult adjustFanDuty(int desiredTemp, int error, int[] temps, List<List<Integer>> fans) {
        // Validação de Inputs
        if (temps == null || fans == null) {
            logError("Temps or fans cannot be null.");
            throw new IllegalArgumentException("Temps or fans cannot be null.");
        }
        if (temps.length != fans.size()) {
            logError("Mismatch between number of zones in temps and fans.");
            throw new IllegalArgumentException("Mismatch between number of zones in temps and fans.");
        }
        if (error < 0) {
            logError("Error margin cannot be negative: " + error);
            throw new IllegalArgumentException("Error margin cannot be negative: " + error);
        }
        if (desiredTemp < MIN_TEMP || desiredTemp > MAX_TEMP) {
            logError("Desired temperature out of realistic range (" + MIN_TEMP + ";" + MAX_TEMP + "): " + desiredTemp);
            throw new IllegalArgumentException("Desired temperature out of realistic range (" + 
                MIN_TEMP + ";" + MAX_TEMP + "): " + desiredTemp);
        }
        
        // Validar temperaturas
        for (int i = 0; i < temps.length; i++) {
            if (temps[i] < MIN_TEMP || temps[i] > MAX_TEMP) {
                logError("Temperature value out of realistic range (" + MIN_TEMP + ";" + MAX_TEMP + ") in zone " + i + ": " + temps[i]);
                throw new IllegalArgumentException("Temperature value out of realistic range (" + 
                    MIN_TEMP + ";" + MAX_TEMP + ") in zone " + i + ": " + temps[i]);
            }
        }
        
        // Validar duty cycles
        for (int i = 0; i < fans.size(); i++) {
            List<Integer> zoneFans = fans.get(i);
            if (zoneFans != null) {
                for (Integer duty : zoneFans) {
                    if (duty == null) {
                        logError("Duty cycle cannot be null in zone " + i);
                        throw new IllegalArgumentException("Duty cycle cannot be null in zone " + i);
                    }
                    if (duty < MIN_DUTY || duty > MAX_DUTY) {
                        logError("Duty cycle out of range (" + MIN_DUTY + "-" + MAX_DUTY + ") in zone " + i + ": " + duty);
                        throw new IllegalArgumentException("Duty cycle out of range (" + 
                            MIN_DUTY + "-" + MAX_DUTY + ") in zone " + i + ": " + duty);
                    }
                }
            }
        }

        // Execução redundante: cálculo realizado duas vezes para verificação de consistência
        List<List<Integer>> result1 = computeAdjustment(desiredTemp, error, temps, fans);
        List<List<Integer>> result2 = computeAdjustment(desiredTemp, error, temps, fans);
    
        if (!result1.equals(result2)) {
            logError("Inconsistent computation detected. Initiating fallback strategy.");
            // Se as duas execuções forem inconsistentes, executa a validação e marca que houve erro
            boolean valid = validateOutput(result1, fans, temps);
            return new AdjustmentResult(result1, !valid);
        }
        
        logInfo("Successfully adjusted fan duty cycles for " + temps.length + " zones.");
        boolean valid = validateOutput(result1, fans, temps);
        return new AdjustmentResult(result1, !valid);
    }

    /**
     * Executa o cálculo do ajuste dos duty cycles para cada zona.
     * Cada zona é processada de forma isolada (try-catch) para evitar que falhas
     * em uma zona comprometam o processamento das demais.
     */
    private static List<List<Integer>> computeAdjustment(int desiredTemp, int error, int[] temps, List<List<Integer>> fans) {
        List<List<Integer>> result = new ArrayList<>();
        
        // Processamento para cada zona
        for (int i = 0; i < temps.length; i++) {
            try {
                int currentTemp = temps[i];
                List<Integer> zoneFans = fans.get(i);

                // Caso a lista de ventiladores esteja nula, registra um erro e usa lista vazia
                if (zoneFans == null) {
                    logWarning("Zone " + i + " fan list is null. Using empty list as fallback.");
                    zoneFans = new ArrayList<>();
                }
                
                // Cálculo da média do duty cycle na zona usando método dedicado
                double avgDouble = calculateAverage(zoneFans, i);
                int avg = (int) avgDouble;
                int adjustment = (avg == 0) ? 20 : 10;

                // Define os limites aceitáveis para a temperatura
                int lowerBound = desiredTemp - error;
                int upperBound = desiredTemp + error;

                List<Integer> updatedFans = new ArrayList<>();
                if (currentTemp < lowerBound) {
                    // Se a temperatura está abaixo do limite, aumenta os duty cycles (máximo 100%)
                    for (int duty : zoneFans) {
                        int newDuty = duty + adjustment;
                        updatedFans.add(newDuty > MAX_DUTY ? MAX_DUTY : newDuty);
                    }
                    logInfo("Zone " + i + ": Temperature below desired range. Increasing fan duty.");
                } else if (currentTemp > upperBound) {
                    // Se a temperatura está acima do limite, diminui os duty cycles (mínimo 0%)
                    for (int duty : zoneFans) {
                        int newDuty = duty - adjustment;
                        updatedFans.add(newDuty < MIN_DUTY ? MIN_DUTY : newDuty);
                    }
                    logInfo("Zone " + i + ": Temperature above desired range. Decreasing fan duty.");
                } else {
                    // Se a temperatura está dentro da faixa, mantém os valores atuais
                    updatedFans.addAll(zoneFans);
                    logInfo("Zone " + i + ": Temperature within desired range. Maintaining fan duty.");
                }
                result.add(updatedFans);
            } catch (Exception e) {
                // Isolamento de falhas por zona: em caso de erro, loga o problema e usa fallback para a zona
                logError("Error processing zone " + i + ": " + e.getMessage());
                result.add(new ArrayList<>());
            }
        }
        return result;
    }
    
    /**
     * Calcula a média dos valores de duty cycle em uma lista.
     * Inclui validações para garantir a integridade do cálculo.
     */
    private static double calculateAverage(List<Integer> values, int zoneId) {
        if (values == null || values.isEmpty()) {
            logWarning("Zone " + zoneId + ": Empty fan list. Average defined as 0.");
            return 0;
        }
        
        long sum = 0; // Usa long para evitar overflow em somas grandes
        int validValues = 0;
        
        for (Integer duty : values) {
            if (duty == null) {
                logWarning("Zone " + zoneId + ": Null duty cycle value found. Skipping.");
                continue;
            }
            
            if (duty < MIN_DUTY || duty > MAX_DUTY) {
                logWarning("Zone " + zoneId + ": Duty cycle value " + duty + " is outside expected range (" + 
                    MIN_DUTY + "-" + MAX_DUTY + "). Using anyway.");
            }
            
            sum += duty;
            validValues++;
        }
        
        return validValues > 0 ? (double) sum / validValues : 0;
    }

    /**
     * Valida o output dos duty cycles, corrigindo erros in-loco.
     * Retorna true se nenhuma correção foi necessária; false caso contrário.
     */
    private static boolean validateOutput(List<List<Integer>> output, List<List<Integer>> originalFans, int[] temps) {
        boolean isValid = true;
        
        if (output == null) {
            logError("Output is null. Using empty list as fallback.");
            return false;
        }
        
        // Verifica se o output tem o mesmo número de zonas que o input
        if (output.size() != temps.length) {
            logError("Output size mismatch: expected " + temps.length + " zones, but got " + output.size());
            isValid = false;
            // Corrigir estrutura - adicionando listas vazias para zonas faltantes
            while (output.size() < temps.length) {
                output.add(new ArrayList<>());
            }
            // Removendo zonas extras
            while (output.size() > temps.length) {
                output.remove(output.size() - 1);
            }
        }
        
        // Verifica cada zona
        for (int i = 0; i < output.size(); i++) {
            List<Integer> zoneOutput = output.get(i);
            
            if (zoneOutput == null) {
                logError("Zone " + i + " output is null. Using empty list.");
                output.set(i, new ArrayList<>());
                isValid = false;
                continue;
            }
            
            // Verifica se os valores estão no intervalo correto
            List<Integer> correctedValues = new ArrayList<>();
            for (Integer duty : zoneOutput) {
                if (duty == null) {
                    logError("Zone " + i + " contains null duty cycle. Using 0.");
                    correctedValues.add(0);
                    isValid = false;
                } else if (duty < MIN_DUTY || duty > MAX_DUTY) {
                    logError("Zone " + i + " has duty cycle out of range: " + duty);
                    correctedValues.add(Math.max(MIN_DUTY, Math.min(MAX_DUTY, duty)));
                    isValid = false;
                } else {
                    correctedValues.add(duty);
                }
            }
            output.set(i, correctedValues);
            
            // Verifica se a estrutura corresponde ao input original
            if (i < originalFans.size() && originalFans.get(i) != null) {
                List<Integer> originalZone = originalFans.get(i);
                if (zoneOutput.size() != originalZone.size()) {
                    logWarning("Zone " + i + " output has different number of fans: expected " + 
                              originalZone.size() + ", got " + zoneOutput.size());
                    isValid = false;
                }
            }
        }
        
        return isValid;
    }
    
    public static void main(String[] args) {
        logInfo("Started test execution with " + Runtime.getRuntime().availableProcessors() + " available processors");
        
        // Valores de teste
        int desiredTemp = 25;
        int error = 3;
        int[] temps1 = {23};
        int[] temps2 = {20, 18};

        // Test Case 1
        List<List<Integer>> fans1 = new ArrayList<>();
        fans1.add(Arrays.asList(0, 0, 0));
        logInfo("Running TC1 with desired temp: " + desiredTemp + ", error margin: " + error);
        AdjustmentResult result1 = adjustFanDuty(desiredTemp, error, temps1, fans1);
        logInfo("TC1 result: " + result1);
        logInfo("----------");

        // Test Case 2
        List<List<Integer>> fans2 = new ArrayList<>();
        fans2.add(Arrays.asList(10, 10));
        fans2.add(Arrays.asList(5, 5));
        logInfo("Running TC2 with desired temp: " + desiredTemp + ", error margin: " + error);
        AdjustmentResult result2 = adjustFanDuty(desiredTemp, error, temps2, fans2);
        logInfo("TC2 result: " + result2);
        logInfo("----------");

        // Test Case 3
        List<List<Integer>> fans3 = new ArrayList<>();
        fans3.add(Arrays.asList(10, 0));
        fans3.add(Arrays.asList(0, 0));
        logInfo("Running TC3 with desired temp: " + desiredTemp + ", error margin: " + error);
        AdjustmentResult result3 = adjustFanDuty(desiredTemp, error, temps2, fans3);
        logInfo("TC3 result: " + result3);
        logInfo("----------");

        // Test Case 4 
        List<List<Integer>> fans4 = new ArrayList<>();
        fans4.add(Arrays.asList(10, 0));
        fans4.add(Arrays.asList(0, 0));
        logInfo("Running TC4 with desired temp: 15, error margin: " + error);
        AdjustmentResult result4 = adjustFanDuty(15, error, temps2, fans4);
        logInfo("TC4 result: " + result4);
        logInfo("----------");

        // Test Case 5
        List<List<Integer>> fans5 = new ArrayList<>();
        fans5.add(Arrays.asList(95, 70));
        fans5.add(Arrays.asList(100, 0));
        logInfo("Running TC5 with desired temp: 30, error margin: " + error);
        AdjustmentResult result5 = adjustFanDuty(30, error, temps2, fans5);
        logInfo("TC5 result: " + result5);
        logInfo("----------");
        
        // Test Case 6 - Testar com temperatura inválida
        try {
            int[] invalidTemps = {-100, 200};
            List<List<Integer>> fans6 = new ArrayList<>();
            fans6.add(Arrays.asList(50, 50));
            fans6.add(Arrays.asList(50, 50));
            logInfo("Running TC6 with invalid temperature");
            AdjustmentResult result6 = adjustFanDuty(desiredTemp, error, invalidTemps, fans6);
            logInfo("TC6 result: " + result6);
            logInfo("----------");
        } catch (IllegalArgumentException e) {
            logInfo("TC6 correctly caught invalid temperature: " + e.getMessage());
            logInfo("----------");
        }
        
        // Test Case 7 - Testar com duty cycle inválido
        try {
            List<List<Integer>> fans7 = new ArrayList<>();
            fans7.add(Arrays.asList(150, 50));
            logInfo("Running TC7 with invalid duty cycle");
            AdjustmentResult result7 = adjustFanDuty(desiredTemp, error, temps1, fans7);
            logInfo("TC7 result: " + result7);
            logInfo("----------");
        } catch (IllegalArgumentException e) {
            logInfo("TC7 correctly caught invalid duty cycle: " + e.getMessage());
            logInfo("----------");
        }
        
        System.out.println("Tests completed. Check logs in " + LOG_FILE + " for details.");
    }
}
