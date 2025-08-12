// --- FILE: solvers/ThomasSolver.java ---
package solvers;

public class ThomasSolver {

    /**
     * Résout un système tridiagonal Ax = d.
     * @param a Vecteur de la diagonale inférieure (taille n-1).
     * @param b Vecteur de la diagonale principale (taille n).
     * @param c Vecteur de la diagonale supérieure (taille n-1).
     * @param d Vecteur second membre (taille n).
     * @return La solution x.
     */
    public double[] solve(double[] a, double[] b, double[] c, double[] d) {
        int n = d.length;
        double[] c_prime = new double[n-1];
        double[] d_prime = new double[n];

        // Travail sur des copies pour ne pas modifier les entrées
        b = b.clone();
        d = d.clone();

        // Phase de balayage avant (forward sweep)
        c_prime[0] = c[0] / b[0];
        d_prime[0] = d[0] / b[0];

        for (int i = 1; i < n - 1; i++) {
            double m = 1.0 / (b[i] - a[i-1] * c_prime[i-1]);
            c_prime[i] = c[i] * m;
            d_prime[i] = (d[i] - a[i-1] * d_prime[i-1]) * m;
        }
        d_prime[n-1] = (d[n-1] - a[n-2] * d_prime[n-2]) / (b[n-1] - a[n-2] * c_prime[n-2]);


        // Phase de substitution arrière (backward substitution)
        double[] x = new double[n];
        x[n-1] = d_prime[n-1];
        for (int i = n - 2; i >= 0; i--) {
            x[i] = d_prime[i] - c_prime[i] * x[i+1];
        }

        System.out.println("Solveur de Thomas exécuté (pour système tridiagonal uniquement).");
        return x;
    }
}