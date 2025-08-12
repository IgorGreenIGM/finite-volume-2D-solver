package solvers;

import storage.CDS;

public class ParallelGaussSeidelSolver {

    /**
     * Résout Ax=B avec Gauss-Seidel et une coloration Rouge-Noir.
     * L'algorithme met à jour tous les nœuds "rouges" puis tous les "noirs".
     * Cela permet une parallélisation car tous les nœuds d'une même couleur sont indépendants.
     *
     * @param A La matrice du système (CDS).
     * @param B Le vecteur second membre.
     * @param n La largeur de la grille (nb de points intérieurs en x).
     * @param x0 La solution initiale.
     * @param maxIter Le nombre max d'itérations.
     * @param tol La tolérance.
     * @return La solution x.
     */
    public double[] solve(CDS A, double[] B, int n, double[] x0, int maxIter, double tol) {
        int size = A.getSize();
        int m = size / n; // Hauteur de la grille
        double[] x = x0.clone();

        for (int k = 0; k < maxIter; k++) {
            double maxDiff = 0;

            // --- PAS ROUGE ---
            // On met à jour tous les nœuds (i,j) où (i+j) est pair.
            // Une vraie implémentation parallèle utiliserait ici un pool de threads.
            for (int idx = 0; idx < size; idx++) {
                int i = idx % n; // coordonnée x de la grille (0-indexed)
                int j = idx / n; // coordonnée y de la grille (0-indexed)

                if ((i + j) % 2 == 0) { // C'est un nœud ROUGE
                    double sum = calculateSum(A, x, idx, n, size);
                    double old_xi = x[idx];
                    x[idx] = (B[idx] - sum) / A.get(idx, idx);

                    // On ne peut calculer la convergence qu'à la fin de l'itération complète
                }
            }

            // --- PAS NOIR ---
            // On met à jour tous les nœuds (i,j) où (i+j) est impair.
            for (int idx = 0; idx < size; idx++) {
                int i = idx % n;
                int j = idx / n;

                if ((i + j) % 2 != 0) { // C'est un nœud NOIR
                    double sum = calculateSum(A, x, idx, n, size);
                    double old_xi = x[idx];
                    x[idx] = (B[idx] - sum) / A.get(idx, idx);
                }
            }

            // Recalculer la convergence après une itération complète (Rouge + Noir)
            // C'est moins précis que le calcul de la diff à chaque update mais nécessaire ici.
            // Pour être rigoureux, il faudrait stocker x avant l'itération.
            // On se contente d'une vérification sur la norme du résidu pour simplifier.
            double residualNorm = calculateResidualNorm(A, B, x);
            if (residualNorm < tol) {
                System.out.println("Gauss-Seidel Parallèle (CDS) a convergé en " + (k + 1) + " itérations.");
                return x;
            }
        }

        System.err.println("Gauss-Seidel Parallèle (CDS) n'a pas convergé après " + maxIter + " itérations.");
        return x;
    }

    private double calculateSum(CDS A, double[] x, int i, int gridWidth, int size) {
        double sum = 0.0;
        // Voisin de gauche
        if (i > 0 && A.get(i, i - 1) != 0) sum += A.get(i, i - 1) * x[i - 1];
        // Voisin de droite
        if (i < size - 1 && A.get(i, i + 1) != 0) sum += A.get(i, i + 1) * x[i + 1];
        // Voisin du bas
        if (i >= gridWidth && A.get(i, i - gridWidth) != 0) sum += A.get(i, i - gridWidth) * x[i - gridWidth];
        // Voisin du haut
        if (i < size - gridWidth && A.get(i, i + gridWidth) != 0) sum += A.get(i, i + gridWidth) * x[i + gridWidth];
        return sum;
    }

    // Calcule la norme L2 du résidu r = B - Ax
    private double calculateResidualNorm(CDS A, double[] B, double[] x) {
        int size = A.getSize();
        double norm = 0.0;
        for (int i = 0; i < size; i++) {
            double ax_i = A.get(i, i) * x[i] + calculateSum(A, x, i, (int)Math.sqrt(size), size);
            double residual_i = B[i] - ax_i;
            norm += residual_i * residual_i;
        }
        return Math.sqrt(norm);
    }
}