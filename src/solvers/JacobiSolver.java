package solvers;

import storage.CDS;
import java.util.Arrays;

public class JacobiSolver {

    /**
     * Solve the system Ax = B By JAcobi method.
     *
     * @param A       La matrice du système (stockage dense).
     * @param B       Le vecteur second membre.
     * @param x0      La solution initiale.
     * @param maxIter Le nombre maximum d'itérations.
     * @param tol     La tolérance pour la convergence.
     * @return Le vecteur solution x.
     */
    public double[] solve(double[][] A, double[] B, double[] x0, int maxIter, double tol) {
        int n = A.length;
        double[] x = new double[n];
        double[] x_old = x0.clone();

        for (int k = 0; k < maxIter; k++) {
            for (int i = 0; i < n; i++) {
                double sum = 0.0;
                for (int j = 0; j < n; j++) {
                    if (i != j) {
                        sum += A[i][j] * x_old[j];
                    }
                }
                x[i] = (B[i] - sum) / A[i][i];
            }

            // Vérification de la convergence
            double maxDiff = 0;
            for (int i = 0; i < n; i++) {
                double diff = Math.abs(x[i] - x_old[i]);
                if (diff > maxDiff) {
                    maxDiff = diff;
                }
            }

            if (maxDiff < tol) {
                System.out.println("Jacobi (Dense) a convergé en " + (k + 1) + " itérations.");
                return x;
            }

            System.arraycopy(x, 0, x_old, 0, n);
        }

        System.err.println("Jacobi (Dense) n'a pas convergé après " + maxIter + " itérations.");
        return x;
    }

    /**
     * Résout le système Ax = B par la méthode de Jacobi pour une matrice creuse (CDS).
     *
     * @param A       La matrice du système (stockage CDS).
     * @param B       Le vecteur second membre.
     * @param x0      La solution initiale.
     * @param maxIter Le nombre maximum d'itérations.
     * @param tol     La tolérance pour la convergence.
     * @return Le vecteur solution x.
     */
    public double[] solve(CDS A, double[] B, double[] x0, int maxIter, double tol) {
        int size = A.getSize();
        int gridWidth = (int) Math.sqrt(size); // Approximation de n, plus précis si n=m
        double[] x = new double[size];
        double[] x_old = x0.clone();


        for (int k = 0; k < maxIter; k++) {
            for (int i = 0; i < size; i++) {
                double sum = 0.0;

                // Somme sur les 4 voisins en utilisant les valeurs de l'itération précédente
                if (i > 0 && A.get(i, i - 1) != 0) sum += A.get(i, i - 1) * x_old[i - 1];
                if (i < size - 1 && A.get(i, i + 1) != 0) sum += A.get(i, i + 1) * x_old[i + 1];
                if (i >= gridWidth && A.get(i, i - gridWidth) != 0) sum += A.get(i, i - gridWidth) * x_old[i - gridWidth];
                if (i < size - gridWidth && A.get(i, i + gridWidth) != 0) sum += A.get(i, i + gridWidth) * x_old[i + gridWidth];

                x[i] = (B[i] - sum) / A.get(i, i);
            }

            // Vérification de la convergence
            double maxDiff = 0;
            for (int i = 0; i < size; i++) {
                double diff = Math.abs(x[i] - x_old[i]);
                if (diff > maxDiff) {
                    maxDiff = diff;
                }
            }

            if (maxDiff < tol) {
                System.out.println("Jacobi (CDS) a convergé en " + (k + 1) + " itérations.");
                return x;
            }

            // Préparer l'itération suivante
            System.arraycopy(x, 0, x_old, 0, size);
        }

        System.err.println("Jacobi (CDS) n'a pas convergé après " + maxIter + " itérations.");
        return x;
    }
}