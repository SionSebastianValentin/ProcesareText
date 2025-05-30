import java.io.*;
import java.util.HashMap;
import java.util.Map;

public class ProcesareTextSecvential {
    public static void main(String[] args) {
        String fileName = "DataIN.txt";
        long startTime = System.nanoTime();
        Map<String, Integer> wordFrequencies = new HashMap<>();
        File file = new File(fileName);
        long fileSize = file.length();
        double fileSizeMB = fileSize / Math.pow(1024,2);

        try (BufferedReader reader = new BufferedReader(new FileReader(fileName))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] words = line.trim().split("\\s+");
                for (String word : words) {
                    wordFrequencies.put(word, wordFrequencies.getOrDefault(word, 0) + 1);
                }
            }
            System.out.println("Procesare finalizata cu succes!");
        } catch (IOException e) {
            System.err.println("Eroare la citirea fisierului: " + e.getMessage());
        }

        for (Map.Entry<String, Integer> entry : wordFrequencies.entrySet()) {
            System.out.println(entry.getKey() + " - " + entry.getValue());
        }
        long endTime   = System.nanoTime();
        long totalTime = endTime - startTime;
        System.out.println(totalTime * Math.pow(10, -9));
        System.out.printf("Dimensiunea fisierului este: %.2f MB%n", fileSizeMB);
    }
}