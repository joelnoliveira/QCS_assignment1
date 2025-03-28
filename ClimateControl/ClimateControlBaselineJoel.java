package ClimateControl;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ClimateControlBaselineJoel {
    
    public static List<List<Integer>> adjustFanDuty(int desired_temp, int error, int[] temps, List<List<Integer>> fans) {
        // Validação dos inputs
        if (temps == null || fans == null || temps.length != fans.size()) {
            throw new IllegalArgumentException("Arrays de temperaturas e grupos de ventiladores devem ser não-nulos e ter o mesmo tamanho.");
        }
        
        List<List<Integer>> result = new ArrayList<>();
        
        // Processamento para cada zona
        for (int i = 0; i < temps.length; i++) {
            int currentTemp = temps[i];
            List<Integer> zoneFans = fans.get(i);
            
            // Cálculo da média dos duty cycles na zona
            int sum = 0;
            for (int duty : zoneFans) {
                sum += duty;
            }
            int avg = zoneFans.size() > 0 ? sum / zoneFans.size() : 0;
            int adjustment = (avg == 0) ? 20 : 10;
            
            // Definindo os limites aceitáveis para a temperatura
            int lowerBound = desired_temp - error;
            int upperBound = desired_temp + error;
            
            List<Integer> updatedFans = new ArrayList<>();
            if (currentTemp < lowerBound) {
                // Aumentar os duty cycles (máximo 100%)
                for (int duty : zoneFans) {
                    int newDuty = duty + adjustment;
                    updatedFans.add(newDuty > 100 ? 100 : newDuty);
                }
            } else if (currentTemp > upperBound) {
                // Diminuir os duty cycles (mínimo 0%)
                for (int duty : zoneFans) {
                    int newDuty = duty - adjustment;
                    updatedFans.add(newDuty < 0 ? 0 : newDuty);
                }
            } else {
                // Temperatura dentro da faixa: sem alteração
                updatedFans.addAll(zoneFans);
            }
            result.add(updatedFans);
        }
        return result;
    }
    
    // Método que executa a função de forma redundante e compara os resultados para detectar possíveis erros
    public static List<List<Integer>> adjustFanDutyRedundant(int desired_temp, int error, int[] temps, List<List<Integer>> fans) {
        List<List<Integer>> result1 = adjustFanDuty(desired_temp, error, temps, fans);
        List<List<Integer>> result2 = adjustFanDuty(desired_temp, error, temps, fans);
        
        // Usamos hashCode e equals para comparar os resultados
        if (result1.hashCode() == result2.hashCode() && result1.equals(result2)) {
            return result1;
        } else {
            // Se houver discrepância, pode-se optar por uma nova reexecução ou notificar um erro
            throw new RuntimeException("Falha na redundância: resultados inconsistentes detectados.");
        }
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
        System.out.println("TC1: " + adjustFanDutyRedundant(desired_temp, error, temps1, fans1));
    
        // TC2
        List<List<Integer>> fans2 = new ArrayList<>();
        fans2.add(Arrays.asList(10, 10));
        fans2.add(Arrays.asList(5, 5));
        System.out.println("TC2: " + adjustFanDutyRedundant(desired_temp, error, temps2, fans2));
    
        // TC3
        List<List<Integer>> fans3 = new ArrayList<>();
        fans3.add(Arrays.asList(10, 0));
        fans3.add(Arrays.asList(0, 0));
        System.out.println("TC3: " + adjustFanDutyRedundant(desired_temp, error, temps2, fans3));
    
        // TC4
        List<List<Integer>> fans4 = new ArrayList<>();
        fans4.add(Arrays.asList(10, 0));
        fans4.add(Arrays.asList(0, 0));
        System.out.println("TC4: " + adjustFanDutyRedundant(15, error, temps2, fans4));
    
        // TC5
        List<List<Integer>> fans5 = new ArrayList<>();
        fans5.add(Arrays.asList(95, 70));
        fans5.add(Arrays.asList(100, 0));
        System.out.println("TC5: " + adjustFanDutyRedundant(30, error, temps2, fans5));
    }
    
}