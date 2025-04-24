import mpi.MPI;

import java.io.File;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

public class ProcesareTextMPJ {
    public static void main(String[] args) throws Exception {
        MPI.Init(args);
        int rank = MPI.COMM_WORLD.Rank();
        int size = MPI.COMM_WORLD.Size();

        String fileName = "DataIN.txt";
        long fileSize;

        if (rank == 0) {
            fileSize = new File(fileName).length();
        } else {
            fileSize = 0;
        }

        long[] fileSizeArray = new long[1];
        fileSizeArray[0] = fileSize;
        MPI.COMM_WORLD.Bcast(fileSizeArray, 0, 1, MPI.LONG, 0);
        fileSize = fileSizeArray[0];

        long chunkSize = fileSize / size;
        long start = rank * chunkSize;
        long end = (rank == size - 1) ? fileSize : (rank + 1) * chunkSize;
        long length = end - start;

        System.out.printf("Rank %d citeste de la %d la %d (dim %d)%n", rank, start, end, length);

        Map<String, Integer> wordFreq = new HashMap<>();

        try (RandomAccessFile raf = new RandomAccessFile(fileName, "r");
             FileChannel channel = raf.getChannel()) {

            ByteBuffer buffer = ByteBuffer.allocate((int) length);
            channel.position(start);
            channel.read(buffer);
            buffer.flip();

            String text = new String(buffer.array(), StandardCharsets.UTF_8);

            if (rank != 0) {
                int firstSpace = text.indexOf(' ');
                if (firstSpace != -1) {
                    text = text.substring(firstSpace + 1);
                }
            }
            
            if (rank != size - 1) {
                int lastSpace = text.lastIndexOf(' ');
                if (lastSpace != -1) {
                    text = text.substring(0, lastSpace);
                }
            }

            String[] words = text.trim().split("\\s+");
            for (String word : words) {
                if (!word.isEmpty()) {
                    wordFreq.put(word, wordFreq.getOrDefault(word, 0) + 1);
                }
            }

        } catch (Exception e) {
            System.err.println("Eroare la rank " + rank + ": " + e.getMessage());
        }

        if (rank != 0) {
            Object[] mapToSend = new Object[]{wordFreq};
            MPI.COMM_WORLD.Send(mapToSend, 0, 1, MPI.OBJECT, 0, 0);
        } else {
            for (int i = 1; i < size; i++) {
                Object[] recvMap = new Object[1];
                MPI.COMM_WORLD.Recv(recvMap, 0, 1, MPI.OBJECT, i, 0);
                @SuppressWarnings("unchecked")
                Map<String, Integer> received = (Map<String, Integer>) recvMap[0];
                for (Map.Entry<String, Integer> entry : received.entrySet()) {
                    wordFreq.put(entry.getKey(), wordFreq.getOrDefault(entry.getKey(), 0) + entry.getValue());
                }
            }

            System.out.println("Procesare finalizata cu succes!");
            for (Map.Entry<String, Integer> entry : wordFreq.entrySet()) {
                System.out.println(entry.getKey() + " - " + entry.getValue());
            }

            double time = (System.nanoTime() - startTime) / 1e9;
            System.out.printf("Timp total: %.2f secunde%n", time);
            System.out.printf("Dimensiune fisier: %.2f MB%n", fileSize / Math.pow(1024, 2));
        }

        MPI.Finalize();
    }

    private static long startTime = System.nanoTime();
}
