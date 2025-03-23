package ClimateControl;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ClimateControlBaseline {
    public static List<List<Integer>> adjustFanDuty(int desired_temp, int error, int[] temps, List<List<Integer>> fans) {
        List<List<Integer>> result = new ArrayList<>();
        
        // Assumindo que a quantidade de zonas em "temps" é igual à de grupos de "fans"
        for (int i = 0; i < temps.length; i++) {
            int currentTemp = temps[i];
            List<Integer> zoneFans = fans.get(i);
            
            // Média do duty cycle na zona
            int sum = 0;
            for (int duty : zoneFans) {
                sum += duty;
            }
            int avg = zoneFans.size() > 0 ? sum / zoneFans.size() : 0;
            int adjustment = (avg == 0) ? 20 : 10;
            
            // Limites aceitáveis para a temperatura
            int lowerBound = desired_temp - error;
            int upperBound = desired_temp + error;
            
            List<Integer> updatedFans = new ArrayList<>();
            if (currentTemp < lowerBound) {
                // Aumentar os duty cycles (até 100%)
                for (int duty : zoneFans) {
                    int newDuty = duty + adjustment;
                    updatedFans.add(newDuty > 100 ? 100 : newDuty);
                }
            } else if (currentTemp > upperBound) {
                // Diminuir os duty cycles (até 0%)
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

       public static void main(String[] args) {

        //Valores de teste
        int desired_temp1___3 = 25;
        int error1__5 = 3;
        int[] temps1 = {23};
        int[] temps2__5 = {20, 18};
        
        //TC1
        List<List<Integer>> fans1 = new ArrayList<>();
        fans1.add(Arrays.asList(0, 0, 0));
        System.out.println("TC1: " + adjustFanDuty(desired_temp1___3, error1__5, temps1, fans1));
        
        //TC2
        List<List<Integer>> fans2 = new ArrayList<>();
        fans2.add(Arrays.asList(10, 10));
        fans2.add(Arrays.asList(5, 5));
        System.out.println("TC2: " + adjustFanDuty(desired_temp1___3, error1__5, temps2__5, fans2));
        
        //TC3
        List<List<Integer>> fans3 = new ArrayList<>();
        fans3.add(Arrays.asList(10, 0));
        fans3.add(Arrays.asList(5, 5));
        System.out.println("TC3: " + adjustFanDuty(desired_temp1___3, error1__5, temps2__5, fans3));
        
        //TC4
        List<List<Integer>> fans4 = new ArrayList<>();
        fans4.add(Arrays.asList(10, 0));
        fans4.add(Arrays.asList(0, 0));
        System.out.println("TC4: " + adjustFanDuty(15, error1__5, temps2__5, fans4));
        
        //TC5
        List<List<Integer>> fans5 = new ArrayList<>();
        fans5.add(Arrays.asList(95, 70));
        fans5.add(Arrays.asList(100, 0));
        System.out.println("TC5: " + adjustFanDuty(30, error1__5, temps2__5, fans5));
    }
}