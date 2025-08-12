package utils;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.function.BiFunction;

public class ErrorAnalysis {

    /**
     * Compute the L2 norm
     * @param numericalSol solution vector
     * @param n width of the grid
     * @param m height of the grid
     * @param l step in x
     * @param h step in y
     * @param exactSolFunc exact solution method
     * @return value of the L2 norm
     */
    public static double calculateLInfinityError(double[] numericalSol, int n, int m, double l, double h, BiFunction<Double, Double, Double> exactSolFunc) {
        double maxError = 0.0;
        for (int j = 0; j < m; j++) {
            for (int i = 0; i < n; i++) {
                int idx = j * n + i;
                double x_i = (i + 1) * l;
                double y_j = (j + 1) * h;
                double exactValue = exactSolFunc.apply(x_i, y_j);
                double error = Math.abs(exactValue - numericalSol[idx]);
                if (error > maxError) {
                    maxError = error;
                }
            }
        }
        return maxError;
    }

    private static boolean isFileEmpty(String filePath) {
        File file = new File(filePath);
        if (file.exists() && file.isFile()) {
            return file.length() == 0;
        }
        return false;
    }

    public static void saveConvergenceData(String filename, int n, int m, double h, double error, double time) {
        try (FileWriter fw = new FileWriter(filename, true);
             PrintWriter pw = new PrintWriter(fw)) {

            if (isFileEmpty(filename)) {
                pw.println("n,m,h,error_L_inf,time_ms");
            }
            pw.printf("%d,%d,%.6f,%.10e,%.4f\n", n, m, h, error, time);

        } catch (IOException e) {
            System.err.println("Erreur lors de l'écriture dans le fichier de convergence : " + e.getMessage());
        }
    }
}