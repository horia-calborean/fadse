package problems.ports;

import org.uma.jmetal.problem.Problem;

public abstract class WrappedProblem<S> implements Problem<S> {
    protected Problem<S> problem;

    public WrappedProblem(Problem<S> problem) {
        this.problem = problem;
    }
}