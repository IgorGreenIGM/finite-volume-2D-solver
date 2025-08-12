// --- FILE: solvers/GaussSiedelSolver.java ---
package solvers;

import storage.CDS;

public class GaussSeidelSolver {

    /**
     * Résout le système Ax = B en utilisant la méthode de Gauss-Seidel pour une matrice dense.
     *
     * @param A       La matrice du système (stockage dense).
     * @param B       Le vecteur second membre.
     * @param x0      La solution initiale (sera modifiée).
     * @param maxIter Le nombre maximum d'itérations.
     * @param tol     La tolérance pour la convergence.
     * @return Le vecteur solution x.
     */
    public double[] solve(double[][] A, double[] B, double[] x0, int maxIter, double tol) {
        int n = A.length;
        double[] x = x0.clone(); // Utiliser une copie pour ne pas modifier l'original

        for (int k = 0; k < maxIter; k++) {
            double maxDiff = 0;

            for (int i = 0; i < n; i++) {
                double sum = 0.0;
                for (int j = 0; j < n; j++) {
                    if (i != j) {
                        sum += A[i][j] * x[j];
                    }
                }
                double old_xi = x[i];
                x[i] = (B[i] - sum) / A[i][i];

                double diff = Math.abs(x[i] - old_xi);
                if (diff > maxDiff) {
                    maxDiff = diff;
                }
            }

            if (maxDiff < tol) {
                System.out.println("Gauss-Seidel (Dense) a convergé en " + (k + 1) + " itérations.");
                return x;
            }
        }
        System.err.println("Gauss-Seidel (Dense) n'a pas convergé après " + maxIter + " itérations.");
        return x;
    }

    /**
     * Résout le système Ax = B en utilisant la méthode de Gauss-Seidel pour une matrice creuse (CDS).
     *
     * @param A       La matrice du système (stockage CDS).
     * @param B       Le vecteur second membre.
     * @param x0      La solution initiale (sera modifiée).
     * @param maxIter Le nombre maximum d'itérations.
     * @param tol     La tolérance pour la convergence.
     * @return Le vecteur solution x.
     */
    public double[] solve(CDS A, double[] B, double[] x0, int maxIter, double tol) {
        int size = A.getSize();
        double[] x = x0.clone();

        for (int k = 0; k < maxIter; k++) {
            double maxDiff = 0;

            for (int i = 0; i < size; i++) {
                double sum = 0.0;

                // On ne calcule la somme que pour les 4 voisins non-nuls
                // Voisin de gauche (i-1)
                if (i > 0 && A.get(i, i - 1) != 0) sum += A.get(i, i - 1) * x[i - 1];
                // Voisin de droite (i+1)
                if (i < size - 1 && A.get(i, i + 1) != 0) sum += A.get(i, i + 1) * x[i + 1];
                // Voisin du bas (i-n)
                if (i >= size/B.length && A.get(i, i - size/B.length) != 0) sum += A.get(i, i - size/B.length) * x[i - size/B.length]; // size/B.length = n
                // Voisin du haut (i+n)
                if (i < size - size/B.length && A.get(i, i + size/B.length) != 0) sum += A.get(i, i + size/B.length) * x[i + size/B.length];

                double old_xi = x[i];
                x[i] = (B[i] - sum) / A.get(i, i); // A.get(i,i) est l'élément diagonal

                double diff = Math.abs(x[i] - old_xi);
                if (diff > maxDiff) {
                    maxDiff = diff;
                }
            }

            if (maxDiff < tol) {
                System.out.println("Gauss-Seidel (CDS) a convergé en " + (k + 1) + " itérations.");
                return x;
            }
        }
        System.err.println("Gauss-Seidel (CDS) n'a pas convergé après " + maxIter + " itérations.");
        return x;
    }
}