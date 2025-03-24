package ClimateControl;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ClimateControlBaselineFault {
   // Método auxiliar para calcular a média com validação dos valores
private static double calculateAverage(List<Integer> values) {
    if (values == null || values.isEmpty()) {
        System.out.println("Aviso: Lista de ventiladores nula ou vazia. Média definida como 0.");
        return 0;
    }
    int sum = 0;
    for (int duty : values) {
        // Verifica se o duty cycle está no intervalo esperado (0 a 100)
        if (duty < 0 || duty > 100) {
            System.out.println("Aviso: Valor de duty cycle " + duty + " fora do intervalo esperado (0-100).");
        }
        sum += duty;
    }
    return (double) sum / values.size();
}

public static List<List<Integer>> adjustFanDuty(int desired_temp, int error, int[] temps, List<List<Integer>> fans) {
    // Assertions para garantir que os dados de entrada não são nulos
    assert temps != null : "Array de temperaturas é nulo";
    assert fans != null : "Lista de ventiladores é nula";

    // Validação: número de zonas deve coincidir
    if (temps.length != fans.size()) {
        System.out.println("Erro: Número de leituras de temperatura (" + temps.length + ") não corresponde ao número de grupos de ventiladores (" + fans.size() + ").");
        return new ArrayList<>();
    }

    List<List<Integer>> result = new ArrayList<>();

    for (int i = 0; i < temps.length; i++) {
        try {
            int currentTemp = temps[i];
            List<Integer> zoneFans = fans.get(i);
            
            // Se a lista de ventiladores da zona estiver vazia ou for nula, avisa e pula para a próxima zona
            if (zoneFans == null || zoneFans.isEmpty()) {
                System.out.println("Aviso: Lista de ventiladores para a zona " + i + " está nula ou vazia. Pulando esta zona.");
                result.add(new ArrayList<>());
                continue;
            }

            // Validação: temperatura plausível para um ambiente automóvel (exemplo: -30°C a 60°C)
            if (currentTemp < -30 || currentTemp > 60) {
                System.out.println("Aviso: Leitura de temperatura " + currentTemp + " na zona " + i + " fora do intervalo plausível (-30°C a 60°C).");
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
            System.out.println("Exceção ao processar a zona " + i + ": " + e.getMessage());
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