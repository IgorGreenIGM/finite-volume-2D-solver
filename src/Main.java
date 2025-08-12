// --- FILE: Main.java ---
import solvers.*;
import storage.CDS;
import utils.ErrorAnalysis;
import utils.MatrixBuilder;
import utils.PerformanceAnalyser;
import utils.ResultsSaver;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.BiFunction;

public class Main {

    // Interface fonctionnelle pour encapsuler une tâche de résolution complète.
    // Elle prend tout ce dont elle a besoin pour construire et résoudre le système.
    @FunctionalInterface
    interface SolveTask {
        double[] run(int n, int m, double l, double h,
                     BiFunction<Double, Double, Double> f,
                     BiFunction<Double, Double, Double> g,
                     double[] x0, int maxIter, double tol);
    }

    public static void main(String[] args) {
        // --- 1. Définition du Cas Test ---
        // On choisit une solution analytique pour laquelle on peut calculer f et g.
        // Solution exacte : u(x,y) = sin(πx) * sin(πy)
        BiFunction<Double, Double, Double> exactSolution = (x, y) -> Math.sin(Math.PI * x) * Math.sin(Math.PI * y);

        // La fonction source f(x,y) est -Δu
        BiFunction<Double, Double, Double> f = (x, y) -> 2 * Math.pow(Math.PI, 2) * Math.sin(Math.PI * x) * Math.sin(Math.PI * y);

        // La fonction g(x,y) donne les valeurs de u(x,y) sur les frontières du domaine [0,1]x[0,1]
        BiFunction<Double, Double, Double> dirichletBoundaryCond = (x, y) -> exactSolution.apply(x,y); // g(x,y) est simplement u(x,y) sur le bord
        // Dans ce cas, cela donne 0.

        // --- 2. Configuration du Banc d'Essai ---
        // On utilise des tailles de grille différentes pour les solveurs lents (Dense) et rapides (Sparse/CDS)
        int[] gridSizesForDense = {10, 20, 30, 40, 80}; // Limité car O(N^3) est très lent
        int[] gridSizesForSparse = {10, 20, 40, 80, 100};
        String benchmarkFile = "src/plot/benchmark_full_results.csv";

        // Nettoyer les anciens résultats pour une nouvelle exécution propre
        new File(benchmarkFile).delete();
        initCsvFile(benchmarkFile);
        System.out.println("Fichier de résultats '" + benchmarkFile + "' initialisé.");

        // --- 3. Définition des Solveurs à Tester ---
        Map<String, SolveTask> solversToTest = new LinkedHashMap<>();

        // Itératifs sur Matrice Creuse (CDS) - Les plus efficaces
        solversToTest.put("Jacobi (CDS)", (n, m, l, h, func_f, func_g, x0, maxIter, tol) -> {
            CDS A = MatrixBuilder.buildCdsA(n, m, l, h);
            double[] B = MatrixBuilder.buildVectorB(n, m, l, h, func_f, func_g);
            return new JacobiSolver().solve(A, B, x0, maxIter, tol);
        });
        solversToTest.put("Gauss-Seidel (CDS)", (n, m, l, h, func_f, func_g, x0, maxIter, tol) -> {
            CDS A = MatrixBuilder.buildCdsA(n, m, l, h);
            double[] B = MatrixBuilder.buildVectorB(n, m, l, h, func_f, func_g);
            return new GaussSeidelSolver().solve(A, B, x0, maxIter, tol);
        });
        solversToTest.put("Parallel GS (CDS)", (n, m, l, h, func_f, func_g, x0, maxIter, tol) -> {
            CDS A = MatrixBuilder.buildCdsA(n, m, l, h);
            double[] B = MatrixBuilder.buildVectorB(n, m, l, h, func_f, func_g);
            return new ParallelGaussSeidelSolver().solve(A, B, n, x0, maxIter, tol);
        });

        // Solveur Direct sur Matrice Dense - Pour la comparaison de performance
        solversToTest.put("Gauss (Dense)", (n, m, l, h, func_f, func_g, x0, maxIter, tol) -> {
            double[][] A = MatrixBuilder.buildDenseA(n, m, l, h);
            double[] B = MatrixBuilder.buildVectorB(n, m, l, h, func_f, func_g);
            return new SimpleGaussSolver().solve(A, B);
        });

        // --- 4. Exécution du Banc d'Essai ---
        System.out.println("\nDébut du banc d'essai comparatif des solveurs.");
        System.out.println("=============================================");

        for (Map.Entry<String, SolveTask> entry : solversToTest.entrySet()) {
            String solverName = entry.getKey();
            SolveTask task = entry.getValue();

            int[] gridSizes = solverName.contains("(Dense)") ? gridSizesForDense : gridSizesForSparse;
            int largestGridSize = gridSizes[gridSizes.length - 1];

            System.out.printf("\n---> Test du solveur : %s\n", solverName);

            for (int N : gridSizes) {
                runSingleTest(solverName, task, N, N, f, dirichletBoundaryCond, exactSolution, benchmarkFile, (N == largestGridSize));
            }
        }
        System.out.println("\n=============================================");
        System.out.println("Banc d'essai terminé. Résultats dans '" + benchmarkFile + "'.");
        System.out.println("Lancez le script 'analyse_benchmark.py' pour visualiser les résultats.");
    }

    /**
     * Exécute un seul test pour une configuration donnée et sauvegarde les résultats.
     */
    private static void runSingleTest(String solverName, SolveTask task, int n, int m,
                                      BiFunction<Double, Double, Double> f,
                                      BiFunction<Double, Double, Double> g,
                                      BiFunction<Double, Double, Double> exactSolution,
                                      String filename, boolean saveGrid) {

        System.out.printf("  - Grille %dx%d... ", n, m);

        double l = 1.0 / (n + 1);
        double h = 1.0 / (m + 1);
        int size = n * m;

        // Paramètres de résolution
        double[] initialGuess = new double[size];
        int maxIterations = 20000;
        double tolerance = 1e-9;

        // Mesure du temps
        PerformanceAnalyser timer = new PerformanceAnalyser();
        timer.start();
        double[] solution = task.run(n, m, l, h, f, g, initialGuess, maxIterations, tolerance);
        timer.stop();

        double timeMs = timer.getDurationMillis();

        // Calcul de l'erreur
        double error = ErrorAnalysis.calculateLInfinityError(solution, n, m, l, h, exactSolution);

        // Estimation de la mémoire
        double memoryMb = solverName.contains("(Dense)") ?
                PerformanceAnalyser.estimateDenseMatrixMemoryMb(n, m) :
                PerformanceAnalyser.estimateCdsMatrixMemoryMb(n, m);

        // Sauvegarde des données de benchmark
        saveBenchmarkData(filename, solverName, n, m, size, h, error, timeMs, memoryMb);

        // Sauvegarde de la grille de solution si demandé (pour la plus grande grille)
        if (saveGrid) {
            String gridFile = String.format("src/plot/solution_grid_%s_%dx%d.csv", solverName.replaceAll("[^a-zA-Z0-9]", "_"), n, m);
            ResultsSaver.saveGridToFile(gridFile, solution, n, m);
        }

        System.out.printf("terminé en %.2f ms, erreur = %.2e\n", timeMs, error);
    }

    private static void initCsvFile(String filename) {
        try (PrintWriter pw = new PrintWriter(new FileWriter(filename))) {
            pw.println("solver_name,n,m,size,h,error_L_inf,time_ms,memory_mb");
        } catch (IOException e) {
            System.err.println("Erreur lors de l'initialisation du fichier CSV : " + e.getMessage());
            e.printStackTrace();
        }
    }

    private static void saveBenchmarkData(String filename, String solverName, int n, int m, int size, double h, double error, double time, double memory) {
        try (FileWriter fw = new FileWriter(filename, true);
             PrintWriter pw = new PrintWriter(fw)) {
            pw.printf("%s,%d,%d,%d,%.6f,%.10e,%.4f,%.4f\n", solverName, n, m, size, h, error, time, memory);
        } catch (IOException e) {
            System.err.println("Erreur lors de l'écriture des données de benchmark : " + e.getMessage());
        }
    }
}