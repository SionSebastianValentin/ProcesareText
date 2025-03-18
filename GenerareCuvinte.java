import java.io.BufferedReader;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class GenerareCuvinte {
    public static void main(String[] args) {
        List<String> words = new ArrayList<>();
        String inputFileName = "cuvinte.txt";
        String outputFileName = "DataIN.txt";
        int multiplier = 5000;

        try (BufferedReader reader = new BufferedReader(new FileReader(inputFileName))) {
            String line;
            while ((line = reader.readLine()) != null) {
                words.add(line);
            }
        } catch (IOException e) {
            System.err.println("Eroare la citirea fișierului: " + e.getMessage());
        }

        try (FileWriter writer = new FileWriter(outputFileName)) {
            for (int i = 0; i < multiplier; i++) {
                for (String word : words) {
                    writer.write(word + " ");
                }
            }
            System.out.println("Fișierul " + outputFileName + " cu cuvintele multiplicate a fost generat cu succes!");
        } catch (IOException e) {
            System.err.println("Eroare la scrierea fișierului: " + e.getMessage());
        }
    }
}
