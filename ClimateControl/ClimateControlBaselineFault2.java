package ClimateControl;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ClimateControlBaselineFault2 {private static final Logger logger = Logger.getLogger(ClimateControlBaselineFault.class.getName());

    // Método auxiliar para calcular a média com validação dos valores
    private static double calculateAverage(List<Integer> values) {
        if (values == null || values.isEmpty()) {
            logger.warning("Lista de ventiladores nula ou vazia. Média definida como 0.");
            return 0;
        }
        int sum = 0;
        for (int duty : values) {
            // Verifica se o duty cycle está no intervalo esperado (0 a 100)
            if (duty < 0 || duty > 100) {
                logger.warning("Valor de duty cycle " + duty + " fora do intervalo esperado (0-100).");
            }
            sum += duty;
        }
        return (double) sum / values.size();
    }
    
    // Método auxiliar para validar a temperatura com fallback para um valor seguro (valor desejado)
    private static int validateTemperature(int temp, int desiredTemp) {
        int minTemp = -30;
        int maxTemp = 60;
        if (temp < minTemp || temp > maxTemp) {
            logger.warning("Leitura de temperatura " + temp + " fora do intervalo plausível (" + minTemp + " a " + maxTemp + "). Utilizando fallback para o valor desejado: " + desiredTemp);
            return desiredTemp;
        }
        return temp;
    }
    
    // Simulação de retry: tenta ler a temperatura até um número máximo de tentativas
    private static int readTemperatureWithRetry(int originalTemp, int desiredTemp) {
        int retries = 3;
        int temp = originalTemp;
        int minTemp = -30;
        int maxTemp = 60;
        int attempt = 0;
        while ((temp < minTemp || temp > maxTemp) && attempt < retries) {
            logger.info("Tentativa " + (attempt+1) + " para ler temperatura: " + temp);
            // Simulação: supõe-se que em uma nova tentativa o valor se corrige; para efeito de exemplo, usamos o valor desejado
            temp = desiredTemp; 
            attempt++;
        }
        if (temp < minTemp || temp > maxTemp) {
            logger.warning("Após " + retries + " tentativas, leitura de temperatura inválida. Usando fallback para " + desiredTemp);
            temp = desiredTemp;
        }
        return temp;
    }
    
    public static List<List<Integer>> adjustFanDuty(int desired_temp, int error, int[] temps, List<List<Integer>> fans) {
        // Design by Contract: Verifica se as entradas não são nulas
        assert temps != null : "Array de temperaturas é nulo";
        assert fans != null : "Lista de ventiladores é nula";
        
        // Validação: número de zonas deve coincidir
        if (temps.length != fans.size()) {
            logger.severe("Número de leituras de temperatura (" + temps.length + ") não corresponde ao número de grupos de ventiladores (" + fans.size() + ").");
            return new ArrayList<>();
        }
        
        List<List<Integer>> result = new ArrayList<>();
        
        for (int i = 0; i < temps.length; i++) {
            try {
                // Aplica retry e fallback para a leitura da temperatura
                int originalTemp = temps[i];
                int currentTemp = readTemperatureWithRetry(originalTemp, desired_temp);
                currentTemp = validateTemperature(currentTemp, desired_temp);
                
                List<Integer> zoneFans = fans.get(i);
                
                // Verifica se a lista de ventiladores está nula ou vazia
                if (zoneFans == null || zoneFans.isEmpty()) {
                    logger.warning("Lista de ventiladores para a zona " + i + " está nula ou vazia. Pulando esta zona.");
                    result.add(new ArrayList<>());
                    continue;
                }
                
                double avgDouble = calculateAverage(zoneFans);
                int avg = (int) Math.round(avgDouble);
                int adjustment = (avg == 0) ? 20 : 10;
                
                // Define os limites da faixa de temperatura
                int lowerBound = desired_temp - error;
                int upperBound = desired_temp + error;
                
                List<Integer> updatedFans = new ArrayList<>();
                if (currentTemp < lowerBound) {
                    // Aumenta os duty cycles (máximo de 100%)
                    for (int duty : zoneFans) {
                        int newDuty = duty + adjustment;
                        if (newDuty > 100) {
                            newDuty = 100;
                        }
                        updatedFans.add(newDuty);
                    }
                } else if (currentTemp > upperBound) {
                    // Diminui os duty cycles (mínimo de 0%)
                    for (int duty : zoneFans) {
                        int newDuty = duty - adjustment;
                        if (newDuty < 0) {
                            newDuty = 0;
                        }
                        updatedFans.add(newDuty);
                    }
                } else {
                    // Temperatura dentro da faixa: sem alteração
                    updatedFans.addAll(zoneFans);
                }
                result.add(updatedFans);
            } catch (Exception e) {
                logger.log(Level.SEVERE, "Exceção ao processar a zona " + i + ": " + e.getMessage(), e);
                // Em caso de exceção, adiciona uma lista vazia para a zona corrente
                result.add(new ArrayList<>());
            }
        }
        return result;
    }
    
    public static void main(String[] args) {
        // Valores de teste
        int desired_temp = 25;
        int error = 3;
        int[] temps1 = {23};
        int[] temps2 = {20, 18};
        
        // TC1
        List<List<Integer>> fans1 = new ArrayList<>();
        fans1.add(Arrays.asList(0, 0, 0));
        System.out.println("TC1: " + adjustFanDuty(desired_temp, error, temps1, fans1));
        
        // TC2
        List<List<Integer>> fans2 = new ArrayList<>();
        fans2.add(Arrays.asList(10, 10));
        fans2.add(Arrays.asList(5, 5));
        System.out.println("TC2: " + adjustFanDuty(desired_temp, error, temps2, fans2));
        
        // TC3
        List<List<Integer>> fans3 = new ArrayList<>();
        fans3.add(Arrays.asList(10, 0));
        fans3.add(Arrays.asList(0, 0));
        System.out.println("TC3: " + adjustFanDuty(desired_temp, error, temps2, fans3));
        
        // TC4
        List<List<Integer>> fans4 = new ArrayList<>();
        fans4.add(Arrays.asList(10, 0));
        fans4.add(Arrays.asList(0, 0));
        System.out.println("TC4: " + adjustFanDuty(15, error, temps2, fans4));
        
        // TC5
        List<List<Integer>> fans5 = new ArrayList<>();
        fans5.add(Arrays.asList(95, 70));
        fans5.add(Arrays.asList(100, 0));
        System.out.println("TC5: " + adjustFanDuty(30, error, temps2, fans5));
    }
    
}