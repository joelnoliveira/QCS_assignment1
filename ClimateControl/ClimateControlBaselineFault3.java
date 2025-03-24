package ClimateControl;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ClimateControlBaselineFault3 {private static final Logger logger = Logger.getLogger(ClimateControlBaselineFault.class.getName());

// Voter System: usa múltiplas leituras para chegar a um valor consensual.
// Se a dispersão (spread) entre as leituras for muito alta, usa o valor desejado como fallback.
private static int voteTemperature(List<Integer> sensorReadings, int desiredTemp) {
    if (sensorReadings == null || sensorReadings.isEmpty()) {
        logger.warning("Leituras de sensores nulas ou vazias. Utilizando fallback: " + desiredTemp);
        return desiredTemp;
    }
    int min = Collections.min(sensorReadings);
    int max = Collections.max(sensorReadings);
    int spread = max - min;
    int threshold = 10; // Threshold para dispersão aceitável
    if (spread > threshold) {
        logger.warning("Leituras inconsistentes (spread = " + spread + " > " + threshold + "). Utilizando fallback: " + desiredTemp);
        return desiredTemp;
    }
    // Calcula a mediana como valor consensual
    List<Integer> sorted = new ArrayList<>(sensorReadings);
    Collections.sort(sorted);
    int median = sorted.get(sorted.size() / 2);
    logger.info("Temperatura votada (mediana): " + median);
    return median;
}

// Adaptive Fault Tolerance: ajusta o erro com base na variabilidade das leituras.
private static int adjustErrorMargin(List<Integer> sensorReadings, int baseError) {
    int min = Collections.min(sensorReadings);
    int max = Collections.max(sensorReadings);
    int spread = max - min;
    // Se a dispersão for alta, aumenta a margem de erro para evitar ajustes agressivos.
    if (spread > 10) {
        int adjustedError = baseError + spread / 2;
        logger.warning("Aumentando a margem de erro de " + baseError + " para " + adjustedError + " devido à inconsistência das leituras.");
        return adjustedError;
    }
    return baseError;
}

// Formal Verification: garante que os duty cycles estejam sempre entre 0 e 100.
private static int verifyDutyCycle(int duty) {
    assert duty >= 0 && duty <= 100 : "Duty cycle " + duty + " fora do intervalo (0-100)";
    return duty;
}

// Método avançado: cada zona possui múltiplas leituras de sensores.
// sensorTemps: lista com, para cada zona, várias leituras de sensores.
// fans: lista de listas contendo os duty cycles atuais dos ventiladores de cada zona.
public static List<List<Integer>> adjustFanDutyAdvanced(int desired_temp, int baseError, List<List<Integer>> sensorTemps, List<List<Integer>> fans) {
    // Verificação formal das pré-condições
    assert sensorTemps != null : "Lista de temperaturas dos sensores é nula";
    assert fans != null : "Lista de ventiladores é nula";
    
    if (sensorTemps.size() != fans.size()) {
        logger.severe("Número de zonas de sensores (" + sensorTemps.size() + ") não corresponde ao número de zonas de ventiladores (" + fans.size() + ").");
        return new ArrayList<>();
    }
    
    List<List<Integer>> result = new ArrayList<>();
    
    for (int i = 0; i < sensorTemps.size(); i++) {
        try {
            List<Integer> zoneSensors = sensorTemps.get(i);
            // Vota pela temperatura correta usando leituras redundantes
            int consensusTemp = voteTemperature(zoneSensors, desired_temp);
            // Ajusta a margem de erro dinamicamente com base na consistência dos sensores
            int error = adjustErrorMargin(zoneSensors, baseError);
            
            List<Integer> zoneFans = fans.get(i);
            if (zoneFans == null || zoneFans.isEmpty()) {
                logger.warning("Lista de ventiladores para a zona " + i + " nula ou vazia. Pulando zona.");
                result.add(new ArrayList<>());
                continue;
            }
            
            // Calcula a média dos duty cycles para a zona
            int sum = 0;
            for (int duty : zoneFans) {
                sum += duty;
            }
            int avg = zoneFans.size() > 0 ? sum / zoneFans.size() : 0;
            int adjustment = (avg == 0) ? 20 : 10;
            
            // Define os limites aceitáveis para a temperatura
            int lowerBound = desired_temp - error;
            int upperBound = desired_temp + error;
            
            List<Integer> updatedFans = new ArrayList<>();
            if (consensusTemp < lowerBound) {
                // Aumenta o duty cycle, garantindo que não ultrapasse 100%
                for (int duty : zoneFans) {
                    int newDuty = duty + adjustment;
                    if (newDuty > 100) {
                        newDuty = 100;
                    }
                    updatedFans.add(verifyDutyCycle(newDuty));
                }
            } else if (consensusTemp > upperBound) {
                // Diminui o duty cycle, garantindo que não seja menor que 0%
                for (int duty : zoneFans) {
                    int newDuty = duty - adjustment;
                    if (newDuty < 0) {
                        newDuty = 0;
                    }
                    updatedFans.add(verifyDutyCycle(newDuty));
                }
            } else {
                // Temperatura dentro da faixa, sem alteração
                for (int duty : zoneFans) {
                    updatedFans.add(verifyDutyCycle(duty));
                }
            }
            result.add(updatedFans);
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Exceção ao processar a zona " + i + ": " + e.getMessage(), e);
            result.add(new ArrayList<>());
        }
    }
    return result;
}

public static void main(String[] args) {
    // Valores de teste
    int desired_temp = 25;
    int error = 3;
    
    // Para cada temperatura única no array original, criaremos uma lista com 3 valores próximos
    // para simular múltiplas leituras de sensores
    
    // TC1
    List<List<Integer>> sensorTemps1 = new ArrayList<>();
    sensorTemps1.add(Arrays.asList(22, 23, 24)); // Baseado em temps1 = {23}
    
    List<List<Integer>> fans1 = new ArrayList<>();
    fans1.add(Arrays.asList(0, 0, 0));
    System.out.println("TC1: " + adjustFanDutyAdvanced(desired_temp, error, sensorTemps1, fans1));
    
    // TC2
    List<List<Integer>> sensorTemps2 = new ArrayList<>();
    sensorTemps2.add(Arrays.asList(19, 20, 21)); // Baseado em temps2[0] = 20
    sensorTemps2.add(Arrays.asList(17, 18, 19)); // Baseado em temps2[1] = 18
    
    List<List<Integer>> fans2 = new ArrayList<>();
    fans2.add(Arrays.asList(10, 10));
    fans2.add(Arrays.asList(5, 5));
    System.out.println("TC2: " + adjustFanDutyAdvanced(desired_temp, error, sensorTemps2, fans2));
    
    // TC3
    List<List<Integer>> sensorTemps3 = new ArrayList<>();
    sensorTemps3.add(Arrays.asList(19, 20, 21)); // Baseado em temps2[0] = 20
    sensorTemps3.add(Arrays.asList(17, 18, 19)); // Baseado em temps2[1] = 18
    
    List<List<Integer>> fans3 = new ArrayList<>();
    fans3.add(Arrays.asList(10, 0));
    fans3.add(Arrays.asList(0, 0));
    System.out.println("TC3: " + adjustFanDutyAdvanced(desired_temp, error, sensorTemps3, fans3));
    
    // TC4
    List<List<Integer>> sensorTemps4 = new ArrayList<>();
    sensorTemps4.add(Arrays.asList(19, 20, 21)); // Baseado em temps2[0] = 20
    sensorTemps4.add(Arrays.asList(17, 18, 19)); // Baseado em temps2[1] = 18
    
    List<List<Integer>> fans4 = new ArrayList<>();
    fans4.add(Arrays.asList(10, 0));
    fans4.add(Arrays.asList(0, 0));
    System.out.println("TC4: " + adjustFanDutyAdvanced(15, error, sensorTemps4, fans4));
    
    // TC5
    List<List<Integer>> sensorTemps5 = new ArrayList<>();
    sensorTemps5.add(Arrays.asList(19, 20, 21)); // Baseado em temps2[0] = 20
    sensorTemps5.add(Arrays.asList(17, 18, 19)); // Baseado em temps2[1] = 18
    
    List<List<Integer>> fans5 = new ArrayList<>();
    fans5.add(Arrays.asList(95, 70));
    fans5.add(Arrays.asList(100, 0));
    System.out.println("TC5: " + adjustFanDutyAdvanced(30, error, sensorTemps5, fans5));
    
    // Test Case com outlier para testar o sistema de votação
    List<List<Integer>> sensorTemps6 = new ArrayList<>();
    sensorTemps6.add(Arrays.asList(19, 20, 50)); // Inclui um outlier (50)
    
    List<List<Integer>> fans6 = new ArrayList<>();
    fans6.add(Arrays.asList(50, 50));
    System.out.println("TC6 (com outlier): " + adjustFanDutyAdvanced(desired_temp, error, sensorTemps6, fans6));
}

}