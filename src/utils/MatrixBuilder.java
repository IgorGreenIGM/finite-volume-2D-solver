package utils;

import storage.CDS;

import java.util.function.BiFunction;

public class MatrixBuilder {

    /**
     * Construit la matrice A (dense) pour le problème -laplacien(u) = f.
     * La matrice est de taille (n*m) x (n*m).
     *
     * @param n Nombre de points intérieurs en direction x.
     * @param m Nombre de points intérieurs en direction y.
     * @param l Pas de discrétisation en x.
     * @param h Pas de discrétisation en y.
     * @return La matrice A sous forme d'un tableau 2D (stockage dense).
     */
    public static double[][] buildDenseA(int n, int m, double l, double h) {
        int size = n * m;
        double[][] A = new double[size][size];

        double valX = 1.0 / (l * l);
        double valY = 1.0 / (h * h);
        double diagVal = 2 * (valX + valY);

        for (int j = 0; j < m; j++) { // Itération sur les lignes de la grille (y)
            for (int i = 0; i < n; i++) { // Itération sur les colonnes de la grille (x)
                int idx = j * n + i; // Index 1D correspondant à la coordonnée (i, j)

                // Diagonale principale
                A[idx][idx] = diagVal;

                // Voisin de gauche (i-1, j)
                if (i > 0) {
                    A[idx][idx - 1] = -valX;
                }
                // Voisin de droite (i+1, j)
                if (i < n - 1) {
                    A[idx][idx + 1] = -valX;
                }
                // Voisin du bas (i, j-1)
                if (j > 0) {
                    A[idx][idx - n] = -valY;
                }
                // Voisin du haut (i, j+1)
                if (j < m - 1) {
                    A[idx][idx + n] = -valY;
                }
            }
        }
        return A;
    }

    /**
     * Build A matrix with CDS Storage.
     *
     * @param n number of interior points in width
     * @param m number of interior points in height
     * @param l step in x
     * @param h step in y
     * @return CDS object
     */
    public static CDS buildCdsA(int n, int m, double l, double h) {
        int size = n * m;
        CDS A = new CDS(size, n); // n est la distance pour les diagonales lointaines

        double valX = 1.0 / (l * l);
        double valY = 1.0 / (h * h);
        double diagVal = 2 * (valX + valY);

        for (int i = 0; i < size; i++) {
            A.set(i, i, diagVal); // main diagonal

            // near diagonals (+1 et -1)
            // La condition (i+1) % n != 0 évite de lier le bord droit d'une ligne au bord gauche de la suivante.
            if (i + 1 < size && (i + 1) % n != 0) {
                A.set(i, i + 1, -valX);
            }
            if (i - 1 >= 0 && i % n != 0) {
                A.set(i, i - 1, -valX);
            }

            // Diagonales lointaines (+n et -n)
            if (i + n < size) {
                A.set(i, i + n, -valY);
            }
            if (i - n >= 0) {
                A.set(i, i - n, -valY);
            }
        }
        return A;
    }

    /**
     * Construit le vecteur B (second membre) en appliquant une fonction f(x,y)
     * et en incorporant les conditions aux limites de Dirichlet g(x,y).
     *
     * @param n Nombre de points intérieurs en x.
     * @param m Nombre de points intérieurs en y.
     * @param l Pas de discrétisation en x.
     * @param h Pas de discrétisation en y.
     * @param f La fonction source f(x,y).
     * @param g La fonction g(x,y) définissant les valeurs sur la frontière (Dirichlet).
     * @return Le vecteur B complet.
     */
    public static double[] buildVectorB(int n, int m, double l, double h,
                                        BiFunction<Double, Double, Double> f,
                                        BiFunction<Double, Double, Double> g) {
        int size = n * m;
        double[] B = new double[size];
        double l2_inv = 1.0 / (l * l);
        double h2_inv = 1.0 / (h * h);

        for (int j = 0; j < m; j++) { // Itération sur les lignes de la grille (y), 0-indexed
            for (int i = 0; i < n; i++) { // Itération sur les colonnes de la grille (x), 0-indexed
                int idx = j * n + i;

                // Coordonnées réelles du point intérieur (i,j)
                double x_i = (i + 1) * l;
                double y_j = (j + 1) * h;

                // 1. On commence avec la valeur de la fonction source f(x,y)
                double b_value = f.apply(x_i, y_j);

                // 2. On ajoute les contributions des frontières connues
                // Si le point est sur le bord GAUCHE de la grille interne (i=0)
                if (i == 0) {
                    b_value += l2_inv * g.apply(0.0, y_j);
                }
                // Si le point est sur le bord DROIT de la grille interne (i=n-1)
                if (i == n - 1) {
                    b_value += l2_inv * g.apply((n + 1) * l, y_j); // (n+1)*l = 1.0
                }
                // Si le point est sur le bord BAS de la grille interne (j=0)
                if (j == 0) {
                    b_value += h2_inv * g.apply(x_i, 0.0);
                }
                // Si le point est sur le bord HAUT de la grille interne (j=m-1)
                if (j == m - 1) {
                    b_value += h2_inv * g.apply(x_i, (m + 1) * h); // (m+1)*h = 1.0
                }

                B[idx] = b_value;
            }
        }
        return B;
    }
}