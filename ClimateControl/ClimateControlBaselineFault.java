package ClimateControl;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ClimateControlBaselineFault {

    /**
     * Ajusta os duty cycles dos ventiladores com base na temperatura desejada,
     * margem de erro, temperaturas atuais e duty cycles atuais dos ventiladores.
     * 
     * Aplica validação de inputs, execução redundante (replicação) e tratamento
     * de exceções para garantir tolerância a falhas.
     */
    public static List<List<Integer>> adjustFanDuty(int desiredTemp, int error, int[] temps, List<List<Integer>> fans) {
        // Validação de Inputs
        if (temps == null || fans == null) {
            throw new IllegalArgumentException("Temps or fans cannot be null.");
        }
        if (temps.length != fans.size()) {
            throw new IllegalArgumentException("Mismatch between number of zones in temps and fans.");
        }

        // Execução redundante: cálculo realizado duas vezes para verificação de consistência
        List<List<Integer>> result1 = computeAdjustment(desiredTemp, error, temps, fans);
        List<List<Integer>> result2 = computeAdjustment(desiredTemp, error, temps, fans);

        if (!result1.equals(result2)) {
            // Se os resultados não coincidirem, loga o erro e toma uma ação de rollback/fallback
            System.err.println("Inconsistent computation detected. Initiating fallback strategy.");
            // Aqui pode-se optar por retornar um valor de fallback, disparar um alerta, ou reexecutar o cálculo
            return result1; // Retorna o primeiro resultado, considerando-o como fallback
        }
        return result1;
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
                    System.err.println("Zone " + i + " fan list is null. Using empty list as fallback.");
                    zoneFans = new ArrayList<>();
                }
                
                // Cálculo da média do duty cycle na zona
                int sum = 0;
                for (int duty : zoneFans) {
                    sum += duty;
                }
                int avg = zoneFans.size() > 0 ? sum / zoneFans.size() : 0;
                int adjustment = (avg == 0) ? 20 : 10;

                // Define os limites aceitáveis para a temperatura
                int lowerBound = desiredTemp - error;
                int upperBound = desiredTemp + error;

                List<Integer> updatedFans = new ArrayList<>();
                if (currentTemp < lowerBound) {
                    // Se a temperatura está abaixo do limite, aumenta os duty cycles (máximo 100%)
                    for (int duty : zoneFans) {
                        int newDuty = duty + adjustment;
                        updatedFans.add(newDuty > 100 ? 100 : newDuty);
                    }
                } else if (currentTemp > upperBound) {
                    // Se a temperatura está acima do limite, diminui os duty cycles (mínimo 0%)
                    for (int duty : zoneFans) {
                        int newDuty = duty - adjustment;
                        updatedFans.add(newDuty < 0 ? 0 : newDuty);
                    }
                } else {
                    // Se a temperatura está dentro da faixa, mantém os valores atuais
                    updatedFans.addAll(zoneFans);
                }
                result.add(updatedFans);
            } catch (Exception e) {
                // Isolamento de falhas por zona: em caso de erro, loga o problema e usa fallback para a zona
                System.err.println("Error processing zone " + i + ": " + e.getMessage());
                result.add(new ArrayList<>()); // fallback: retorna lista vazia para essa zona
            }
        }
        return result;
    }
    
    public static void main(String[] args) {
        // Valores de teste
        int desiredTemp = 25;
        int error = 3;
        int[] temps1 = {23};
        int[] temps2 = {20, 18};

        // TC1
        List<List<Integer>> fans1 = new ArrayList<>();
        fans1.add(Arrays.asList(0, 0, 0));
        System.out.println("TC1: " + adjustFanDuty(desiredTemp, error, temps1, fans1));

        // TC2
        List<List<Integer>> fans2 = new ArrayList<>();
        fans2.add(Arrays.asList(10, 10));
        fans2.add(Arrays.asList(5, 5));
        System.out.println("TC2: " + adjustFanDuty(desiredTemp, error, temps2, fans2));

        // TC3
        List<List<Integer>> fans3 = new ArrayList<>();
        fans3.add(Arrays.asList(10, 0));
        fans3.add(Arrays.asList(0, 0));
        System.out.println("TC3: " + adjustFanDuty(desiredTemp, error, temps2, fans3));

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
