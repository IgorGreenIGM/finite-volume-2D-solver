package storage;

import java.util.Arrays;

/**
 * Implémente le stockage par diagonales compressées (Compressed Diagonal Storage)
 * pour une matrice à 5 diagonales.
 * Diagonales stockées :
 * - Diagonale principale (offset 0)
 * - Diagonales proches (offset +1, -1)
 * - Diagonales lointaines (offset +n, -n)
 */
public class CDS {
    private final double[] mainDiag;      // Offset 0
    private final double[] upperDiag1;    // Offset +1
    private final double[] lowerDiag1;    // Offset -1
    private final double[] upperDiagN;    // Offset +n
    private final double[] lowerDiagN;    // Offset -n
    private final int size;
    private final int n; // distance des diagonales lointaines (largeur de la grille)

    public CDS(int size, int n) {
        this.size = size;
        this.n = n;
        this.mainDiag = new double[size];
        this.upperDiag1 = new double[size - 1];
        this.lowerDiag1 = new double[size - 1];
        this.upperDiagN = new double[size - n];
        this.lowerDiagN = new double[size - n];
    }

    public int getSize() {
        return size;
    }

    public void set(int row, int col, double value) {
        int offset = col - row;
        if (offset == 0) {
            mainDiag[row] = value;
        } else if (offset == 1 && row < size - 1) {
            upperDiag1[row] = value;
        } else if (offset == -1 && row > 0) {
            lowerDiag1[row - 1] = value;
        } else if (offset == n && row < size - n) {
            upperDiagN[row] = value;
        } else if (offset == -n && row > 0) {
            lowerDiagN[row - n] = value;
        } else if (value != 0) {
            // Lève une exception si on essaie de mettre une valeur non-nulle en dehors des diagonales
            throw new IllegalArgumentException("Cannot set value outside of the 5 stored diagonals.");
        }
    }

    public double get(int row, int col) {
        int offset = col - row;
        if (offset == 0) {
            return mainDiag[row];
        } else if (offset == 1 && row < size - 1) {
            return upperDiag1[row];
        } else if (offset == -1 && row > 0) {
            return lowerDiag1[row - 1];
        } else if (offset == n && row < size - n) {
            return upperDiagN[row];
        } else if (offset == -n && row > 0) {
            return lowerDiagN[row - n];
        }
        return 0.0; // Par définition, tout ce qui n'est pas sur les diagonales est zéro.
    }

    // Affiche la matrice pour le débogage (peut être lent pour de grandes matrices)
    public void print() {
        for (int i = 0; i < size; i++) {
            for (int j = 0; j < size; j++) {
                System.out.printf("%6.2f ", get(i, j));
            }
            System.out.println();
        }
    }
}