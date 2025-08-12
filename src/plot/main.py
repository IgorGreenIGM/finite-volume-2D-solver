import pandas as pd
import matplotlib.pyplot as plt
import seaborn as sns
import numpy as np
import glob
import re

# --- FONCTION PRINCIPALE D'ANALYSE ---
def analyze_results():
    # Configurer un style de graphique agréable
    sns.set_theme(style="whitegrid")

    try:
        df = pd.read_csv('benchmark_full_results.csv')
    except FileNotFoundError:
        print("Fichier 'benchmark_full_results.csv' non trouvé. Veuillez lancer le programme Java 'Main' d'abord.")
        return

    solvers = df['solver_name'].unique()
    print("Solveurs détectés dans le benchmark:", solvers)

    # --- Génération des graphiques de performance ---
    plot_time_performance(df, solvers)
    plot_error_convergence(df, solvers)
    plot_memory_usage(df)

    # --- Génération des heatmaps des solutions ---
    plot_solution_grids(solvers)

    print("\nAnalyse terminée. Les graphiques ont été sauvegardés sous forme de fichiers PNG.")
    plt.show()

# --- GRAPHIQUE 1 : Temps d'exécution ---
def plot_time_performance(df, solvers):
    plt.figure(figsize=(10, 7))
    for solver in solvers:
        subset = df[df['solver_name'] == solver]
        plt.plot(subset['size'], subset['time_ms'], 'o-', label=solver)

    plt.xlabel('Taille du problème (N x M)')
    plt.ylabel('Temps d\'exécution (ms)')
    plt.title('Performance des Solveurs (Échelle Log-Log)')
    plt.legend()
    plt.yscale('log')
    plt.xscale('log')
    plt.grid(True, which="both", ls="--")
    plt.savefig('benchmark_plot_time.png')
    print("Graphique de performance (temps) sauvegardé.")

# --- GRAPHIQUE 2 : Erreur de convergence ---
def plot_error_convergence(df, solvers):
    plt.figure(figsize=(10, 7))
    for solver in solvers:
        subset = df[df['solver_name'] == solver]
        plt.loglog(subset['h'], subset['error_L_inf'], 'o-', label=solver)

    # Ligne de référence Ordre 2
    h_ref = df['h'].unique()
    h_ref.sort()
    first_error_point = df.sort_values(by='h').iloc[0]
    C = first_error_point['error_L_inf'] / (first_error_point['h']**2)
    plt.loglog(h_ref, C * h_ref**2, 'k--', label='Référence Ordre 2 ($O(h^2)$)')

    plt.xlabel('Pas de maillage (h)')
    plt.ylabel('Erreur L-infini')
    plt.title('Analyse de la Convergence des Solveurs')
    plt.legend()
    plt.gca().invert_xaxis()
    plt.grid(True, which="both", ls="--")
    plt.savefig('benchmark_plot_error.png')
    print("Graphique de convergence (erreur) sauvegardé.")

# --- GRAPHIQUE 3 : Empreinte mémoire ---
def plot_memory_usage(df):
    plt.figure(figsize=(10, 7))
    dense_data = df[df['solver_name'].str.contains("(Dense)")].drop_duplicates(subset=['size'])
    sparse_data = df[df['solver_name'].str.contains("(CDS)")].drop_duplicates(subset=['size'])

    if not dense_data.empty:
        plt.plot(dense_data['size'], dense_data['memory_mb'], 'o-', label='Stockage Dense')
    if not sparse_data.empty:
        plt.plot(sparse_data['size'], sparse_data['memory_mb'], 'o-', label='Stockage Creux (CDS)')

    plt.xlabel('Taille du problème (N x M)')
    plt.ylabel('Mémoire estimée (MB)')
    plt.title('Comparaison de l\'Empreinte Mémoire (Échelle Log-Log)')
    plt.legend()
    plt.yscale('log')
    plt.xscale('log')
    plt.grid(True, which="both", ls="--")
    plt.savefig('benchmark_plot_memory.png')
    print("Graphique d'empreinte mémoire sauvegardé.")

# --- GRAPHIQUE 4 : Grilles de solution (Heatmaps) ---
def plot_solution_grids(solvers):
    grid_files = glob.glob('solution_grid_*.csv')
    if not grid_files:
        print("Aucun fichier de grille de solution trouvé. Les heatmaps ne seront pas générées.")
        return

    num_solvers = len(solvers)
    # Arrange subplots in a grid
    cols = int(np.ceil(np.sqrt(num_solvers)))
    rows = int(np.ceil(num_solvers / cols))

    fig, axes = plt.subplots(rows, cols, figsize=(5 * cols, 4 * rows), squeeze=False)
    axes = axes.flatten()

    for i, solver in enumerate(solvers):
        # Find the corresponding file for the solver
        solver_file_pattern = f"solution_grid_{re.sub('[^a-zA-Z0-9]', '_', solver)}*.csv"
        matching_files = glob.glob(solver_file_pattern)

        if not matching_files:
            continue

        file_to_plot = matching_files[0]
        solution_grid = np.loadtxt(file_to_plot, delimiter=',')

        im = axes[i].imshow(solution_grid, cmap='viridis', extent=[0, 1, 1, 0])
        axes[i].set_title(solver)
        axes[i].set_xlabel('x')
        axes[i].set_ylabel('y')
        fig.colorbar(im, ax=axes[i], orientation='vertical', fraction=0.046, pad=0.04)

    # Hide any unused subplots
    for j in range(i + 1, len(axes)):
        axes[j].set_visible(False)

    fig.tight_layout()
    plt.savefig('benchmark_plot_solutions.png')
    print("Heatmaps des solutions sauvegardées.")

# --- POINT D'ENTRÉE DU SCRIPT ---
if __name__ == '__main__':
    analyze_results()