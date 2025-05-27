import java.io.*;
import java.util.*;
import java.util.concurrent.*;

public class ProcesareTextThreads {

    public static void main(String[] args) throws IOException, InterruptedException, ExecutionException {
        String fileName = "DataIN3.txt";
        int numThreads = 8;

        long startTime = System.nanoTime();

        List<String> allWords = new ArrayList<>();
        try (BufferedReader reader = new BufferedReader(new FileReader(fileName))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] words = line.trim().split("\\s+");
                allWords.addAll(Arrays.asList(words));
            }
        }

        int totalWords = allWords.size();
        int chunkSize = (int) Math.ceil((double) totalWords / numThreads);

        ExecutorService executor = Executors.newFixedThreadPool(numThreads);
        List<Future<Map<String, Integer>>> futures = new ArrayList<>();

        for (int i = 0; i < totalWords; i += chunkSize) {
            int start = i;
            int end = Math.min(i + chunkSize, totalWords);

            List<String> sublist = allWords.subList(start, end);
            Callable<Map<String, Integer>> task = () -> {
                Map<String, Integer> localFreq = new HashMap<>();
                for (String word : sublist) {
                    localFreq.put(word, localFreq.getOrDefault(word, 0) + 1);
                }
                return localFreq;
            };
            futures.add(executor.submit(task));
        }

        Map<String, Integer> finalFrequencies = new HashMap<>();
        for (Future<Map<String, Integer>> future : futures) {
            try {
                Map<String, Integer> local = future.get();
                for (Map.Entry<String, Integer> entry : local.entrySet()) {
                    finalFrequencies.merge(entry.getKey(), entry.getValue(), Integer::sum);
                }
            } catch (ExecutionException e) {
                System.err.println("Eroare într-un thread: " + e.getCause());
                e.getCause().printStackTrace();
            }
        }

        executor.shutdown();

        long endTime = System.nanoTime();
        double totalTime = (endTime - startTime) * 1e-9;
        
        System.out.println("Procesare finalizată cu succes!");
        for (Map.Entry<String, Integer> entry : finalFrequencies.entrySet()) {
            System.out.println(entry.getKey() + " - " + entry.getValue());
        }

        File file = new File(fileName);
        double fileSizeMB = file.length() / Math.pow(1024, 2);
        System.out.printf("Timp total: %.5f secunde%n", totalTime);
        System.out.printf("Dimensiunea fișierului: %.2f MB%n", fileSizeMB);
    }
}
