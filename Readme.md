# 2D Finite Volume Solver for the Poisson Equation

This project provides a Java implementation of a 2D finite volume solver for the Poisson equation on a unit square. It features a benchmark comparing several numerical methods, focusing on performance, accuracy, and memory efficiency. The results are analyzed and visualized using a companion Python script.

---

### 1. The Mathematical Problem

The solver addresses the two-dimensional Poisson equation with Dirichlet boundary conditions:
$$
-\nabla^2 u = f(x, y) \quad \text{on } \Omega = \times
$$
$$
u(x, y) = g(x, y) \quad \text{on } \partial\Omega
$$
The domain is discretized using a finite volume method on a uniform Cartesian grid. Integrating the governing equation over a control volume $P$ and applying Gauss's divergence theorem leads to the standard five-point stencil formula:

$$
\left( \frac{2}{\Delta x^2} + \frac{2}{\Delta y^2} \right) u_P - \frac{1}{\Delta x^2} (u_E + u_W) - \frac{1}{\Delta y^2} (u_N + u_S) = f(x_P, y_P)
$$

Applying this to every internal node generates a large, sparse linear system of the form $\mathbf{Au = b}$.

---

### 2. Implementation Details

#### Matrix Storage Schemes

The benchmark compares two fundamental storage strategies for the matrix $\mathbf{A}$:
*   **Dense Storage**: A standard `double[][]` array. While simple, its memory usage grows quadratically ($O(N^2)$), making it impractical for large grids.
*   **Sparse Storage (CDS)**: A **Compressed Diagonal Storage** (`CDS.java`) format optimized for this problem. It only stores the 5 non-zero diagonals, reducing memory usage to a linear scale ($O(N)$).

#### Solver Approaches

The project evaluates a direct solver against several iterative methods:

*   **Direct Solver (Gauss Elimination)**:
    *   Solves the system exactly in a finite number of steps.
    *   Requires a **dense matrix** and has a high computational cost of $O(N^3)$. It serves as a performance baseline for small grids.

*   **Iterative Solvers**:
    *   **Jacobi**: A simple iterative method that updates each solution component based entirely on values from the *previous* iteration.
    *   **Gauss-Seidel**: An improvement over Jacobi that uses the most recently updated values *within the same* iteration, generally leading to faster convergence.
    *   **Parallel Gauss-Seidel (Red-Black)**: A variant where the grid nodes are colored like a checkerboard. All "red" nodes are updated in parallel, followed by all "black" nodes, making it suitable for parallel architectures.

All iterative solvers are implemented to leverage the efficiency of the **sparse CDS matrix format**.

---

### 3. How to Run

**Prerequisites**:
-   JDK 21 or later.
-   Python 3+ with `pandas`, `matplotlib`, and `seaborn`.
    ```bash
    pip install pandas matplotlib seaborn numpy
    ```

**Execution Steps:**

1.  **Run the Java Benchmark**:
    -   Compile and run the `Main.java` class from the `src` directory.
    -   This will execute the simulations and generate `.csv` data files in `src/plot/`.

2.  **Visualize Results**:
    -   Run the Python script `src/plot/main.py`.
    -   This script reads the generated `.csv` files and creates PNG plots summarizing the benchmark results.

---

### 4. Benchmark Results

The analysis script visualizes the following key comparisons:

-   **Performance**: Confirms the $O(N^3)$ complexity of the direct Gauss solver versus the much faster iterative methods.
-   **Accuracy**: Validates that the finite volume method is second-order accurate ($O(h^2)$) for all solvers.
-   **Memory Usage**: Clearly illustrates the linear ($O(N)$) memory scaling of **CDS** compared to the quadratic ($O(N^2)$) scaling of **dense storage**.
-   **Solution Visualization**: Produces heatmaps of the computed solutions for visual validation.