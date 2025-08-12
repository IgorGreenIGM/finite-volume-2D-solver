// --- FILE: solvers/SimpleGaussSolver.java ---
package solvers;

public class SimpleGaussSolver {

    /**
     * Solve the system Ax = B by Gauss elimination.
     *
     * @param A La matrice du système (dense).
     * @param B Le vecteur second membre.
     * @return Le vecteur solution x.
     */
    public double[] solve(double[][] A, double[] B) {
        int n = A.length;

        double[][] A_copy = new double[n][n];
        for(int i=0; i<n; i++) A_copy[i] = A[i].clone();
        double[] B_copy = B.clone();

        for (int k = 0; k < n; k++) {
            if (Math.abs(A_copy[k][k]) < 1e-10) {
                throw new ArithmeticException("Pivot nul détecté, la matrice est peut-être singulière.");
            }

            for (int i = k + 1; i < n; i++) {
                double factor = A_copy[i][k] / A_copy[k][k];
                B_copy[i] -= factor * B_copy[k];
                for (int j = k; j < n; j++) {
                    A_copy[i][j] -= factor * A_copy[k][j];
                }
            }
        }

        double[] x = new double[n];
        for (int i = n - 1; i >= 0; i--) {
            double sum = 0.0;
            for (int j = i + 1; j < n; j++) {
                sum += A_copy[i][j] * x[j];
            }
            x[i] = (B_copy[i] - sum) / A_copy[i][i];
        }

        System.out.println("Résolution par élimination de Gauss (Dense) terminée.");
        return x;
    }
}