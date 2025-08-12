package utils;

import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;

public class ResultsSaver {

    /**
     * save the solution vector in a csv file as a 2D grid
     * @param filename path to the output file
     * @param solution solution vector
     * @param n width of the grid
     * @param m height od the grid.
     */
    public static void saveGridToFile(String filename, double[] solution, int n, int m) {
        try (PrintWriter pw = new PrintWriter(new FileWriter(filename))) {
            for (int j = 0; j < m; j++) {
                for (int i = 0; i < n; i++) {
                    int idx = j * n + i;
                    pw.print(solution[idx]);
                    if (i < n - 1) {
                        pw.print(",");
                    }
                }
                pw.println();
            }
            System.out.println("Grille de solution sauvegardée dans : " + filename);
        } catch (IOException e) {
            System.err.println("Erreur lors de la sauvegarde de la grille : " + e.getMessage());
        }
    }
}