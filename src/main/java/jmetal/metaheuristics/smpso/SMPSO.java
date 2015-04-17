/**
 * SMPSO.java
 * 
 * @author Juan J. Durillo
 * @author Antonio J. Nebro
 * @version 1.0
 */
package jmetal.metaheuristics.smpso;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.Comparator;
import java.util.StringTokenizer;
import java.util.Vector;
import java.util.logging.Level;
import java.util.logging.Logger;

import ro.ulbsibiu.fadse.environment.parameters.CheckpointFileParameter;
import ro.ulbsibiu.fadse.extended.problems.simulators.ServerSimulator;
import jmetal.base.Algorithm;
import jmetal.base.Operator;
import jmetal.base.Problem;
import jmetal.base.Solution;
import jmetal.base.SolutionSet;
import jmetal.base.operator.comparator.CrowdingDistanceComparator;
import jmetal.base.operator.comparator.DominanceComparator;
import jmetal.qualityIndicator.Hypervolume;
import jmetal.qualityIndicator.QualityIndicator;
import jmetal.util.Distance;
import jmetal.util.JMException;
import jmetal.util.PseudoRandom;
import jmetal.util.archive.CrowdingArchive;
import jmetal.util.wrapper.XInt;

public class SMPSO extends Algorithm {

    /**
     * Stores the problem to solve
     */
    private Problem problem_;
    /**
     * Stores the number of particles_ used
     */
    private int particlesSize_;
    /**
     * Stores the maximum size for the archive
     */
    private int archiveSize_;
    /**
     * Stores the maximum number of iteration_
     */
    private int maxIterations_;
    /**
     * Stores the current number of iteration_
     */
    private int iteration_;
    /**
     * Stores the particles
     */
    private SolutionSet particles_;
    /**
     * Stores the best_ solutions founds so far for each particles
     */
    private Solution[] best_;
    /**
     * Stores the leaders_
     */
    private CrowdingArchive leaders_;
    /**
     * Stores the speed_ of each particle
     */
    private double[][] speed_;
    /**
     * Stores a comparator for checking dominance
     */
    private Comparator dominance_;
    /**
     * Stores a comparator for crowding checking
     */
    private Comparator crowdingDistanceComparator_;
    /**
     * Stores a <code>Distance</code> object
     */
    private Distance distance_;
    /**
     * Stores a operator for non uniform mutations
     */
    private Operator polynomialMutation_;
    QualityIndicator indicators_; // QualityIndicator object
    double r1Max_;
    double r1Min_;
    double r2Max_;
    double r2Min_;
    double C1Max_;
    double C1Min_;
    double C2Max_;
    double C2Min_;
    double WMax_;
    double WMin_;
    double ChVel1_;
    double ChVel2_;

    /**
     * Constructor
     * @param problem Problem to solve
     */
    public SMPSO(Problem problem) {
        problem_ = problem;

        r1Max_ = 1.0;
        r1Min_ = 0.0;
        r2Max_ = 1.0;
        r2Min_ = 0.0;
        C1Max_ = 2.5;
        C1Min_ = 1.5;
        C2Max_ = 2.5;
        C2Min_ = 1.5;
        WMax_ = 0.1;
        WMin_ = 0.1;
        ChVel1_ = -1;
        ChVel2_ = -1;
    } // Constructor

    public SMPSO(Problem problem,
            Vector<Double> variables,
            String trueParetoFront) throws FileNotFoundException {
        problem_ = problem;

        r1Max_ = variables.get(0);
        r1Min_ = variables.get(1);
        r2Max_ = variables.get(2);
        r2Min_ = variables.get(3);
        C1Max_ = variables.get(4);
        C1Min_ = variables.get(5);
        C2Max_ = variables.get(6);
        C2Min_ = variables.get(7);
        WMax_ = variables.get(8);
        WMin_ = variables.get(9);
        ChVel1_ = variables.get(10);
        ChVel2_ = variables.get(11);

        hy_ = new Hypervolume();
        jmetal.qualityIndicator.util.MetricsUtil mu = new jmetal.qualityIndicator.util.MetricsUtil();
        trueFront_ = mu.readNonDominatedSolutionSet(trueParetoFront);
        trueHypervolume_ = hy_.hypervolume(trueFront_.writeObjectivesToMatrix(),
                trueFront_.writeObjectivesToMatrix(),
                problem_.getNumberOfObjectives());

    } // SMPSO
    private double trueHypervolume_;
    private Hypervolume hy_;
    private SolutionSet trueFront_;
    private double deltaMax_[];
    private double deltaMin_[];
    boolean success_;

    /**
     * Constructor
     * @param problem Problem to solve
     */
    public SMPSO(Problem problem, String trueParetoFront) throws FileNotFoundException {
        problem_ = problem;
        hy_ = new Hypervolume();
        jmetal.qualityIndicator.util.MetricsUtil mu = new jmetal.qualityIndicator.util.MetricsUtil();
        trueFront_ = mu.readNonDominatedSolutionSet(trueParetoFront);
        trueHypervolume_ = hy_.hypervolume(trueFront_.writeObjectivesToMatrix(),
                trueFront_.writeObjectivesToMatrix(),
                problem_.getNumberOfObjectives());

        // Default configuration
        r1Max_ = 1.0;
        r1Min_ = 0.0;
        r2Max_ = 1.0;
        r2Min_ = 0.0;
        C1Max_ = 2.5;
        C1Min_ = 1.5;
        C2Max_ = 2.5;
        C2Min_ = 1.5;
        WMax_ = 0.1;
        WMin_ = 0.1;
        ChVel1_ = -1;
        ChVel2_ = -1;
    } // Constructor

    /**
     * Initialize all parameter of the algorithm
     */
    public void initParams() {
        particlesSize_ = ((Integer) getInputParameter("swarmSize")).intValue();
        archiveSize_ = ((Integer) getInputParameter("archiveSize")).intValue();
        maxIterations_ = ((Integer) getInputParameter("maxIterations")).intValue();

        indicators_ = (QualityIndicator) getInputParameter("indicators");

        polynomialMutation_ = operators_.get("mutation");

        iteration_ = 0;

        success_ = false;

        particles_ = new SolutionSet(particlesSize_);
        best_ = new Solution[particlesSize_];
        leaders_ = new CrowdingArchive(archiveSize_, problem_.getNumberOfObjectives());

        // Create comparators for dominance and crowding distance
        dominance_ = new DominanceComparator();
        crowdingDistanceComparator_ = new CrowdingDistanceComparator();
        distance_ = new Distance();

        // Create the speed_ vector
        speed_ = new double[particlesSize_][problem_.getNumberOfVariables()];


        deltaMax_ = new double[problem_.getNumberOfVariables()];
        deltaMin_ = new double[problem_.getNumberOfVariables()];
        for (int i = 0; i < problem_.getNumberOfVariables(); i++) {
            deltaMax_[i] = (problem_.getUpperLimit(i)
                    - problem_.getLowerLimit(i)) / 2.0;
            deltaMin_[i] = -deltaMax_[i];
        } // for
    } // initParams

    // Adaptive inertia
    private double inertiaWeight(int iter, int miter, double wma, double wmin) {
        return wma; // - (((wma-wmin)*(double)iter)/(double)miter);
    } // inertiaWeight

    // constriction coefficient (M. Clerc)
    private double constrictionCoefficient(double c1, double c2) {
        double rho = c1 + c2;
        //rho = 1.0 ;
        if (rho <= 4) {
            return 1.0;
        } else {
            return 2 / (2 - rho - Math.sqrt(Math.pow(rho, 2.0) - 4.0 * rho));
        }
    } // constrictionCoefficient

    // velocity bounds
    private double velocityConstriction(double v, double[] deltaMax,
            double[] deltaMin, int variableIndex,
            int particleIndex) throws IOException {


        double result;

        double dmax = deltaMax[variableIndex];
        double dmin = deltaMin[variableIndex];

        result = v;

        if (v > dmax) {
            result = dmax;
        }

        if (v < dmin) {
            result = dmin;
        }

        return result;
    } // velocityConstriction

    /**
     * Update the speed of each particle
     * @throws JMException
     */
    private void computeSpeed(int iter, int miter) throws JMException, IOException {
        double r1, r2, W, C1, C2;
        double wmax, wmin, deltaMax, deltaMin;
        XInt bestGlobal;

        for (int i = 0; i < particlesSize_; i++) {
            XInt particle = new XInt(particles_.get(i));
            XInt bestParticle = new XInt(best_[i]);

            //Select a global best_ for calculate the speed of particle i, bestGlobal
            Solution one, two;
            int pos1 = PseudoRandom.randInt(0, leaders_.size() - 1);
            int pos2 = PseudoRandom.randInt(0, leaders_.size() - 1);
            one = leaders_.get(pos1);
            two = leaders_.get(pos2);

            if (crowdingDistanceComparator_.compare(one, two) < 1) {
                bestGlobal = new XInt(one);
            } else {
                bestGlobal = new XInt(two);
                //Params for velocity equation
            }
            r1 = PseudoRandom.randDouble(r1Min_, r1Max_);
            r2 = PseudoRandom.randDouble(r2Min_, r2Max_);
            C1 = PseudoRandom.randDouble(C1Min_, C1Max_);
            C2 = PseudoRandom.randDouble(C2Min_, C2Max_);
            W = PseudoRandom.randDouble(WMin_, WMax_);
            //
            wmax = WMax_;
            wmin = WMin_;

            for (int var = 0; var < particle.getNumberOfDecisionVariables(); var++) {
                //Computing the velocity of this particle
                speed_[i][var] = velocityConstriction(constrictionCoefficient(C1, C2)
                        * (inertiaWeight(iter, miter, wmax, wmin)
                        * speed_[i][var]
                        + C1 * r1 * (bestParticle.getValue(var)
                        - particle.getValue(var))
                        + C2 * r2 * (bestGlobal.getValue(var)
                        - particle.getValue(var))), deltaMax_, //[var],
                        deltaMin_, //[var],
                        var,
                        i);
            }
        }
    } // computeSpeed

    /**
     * Update the position of each particle
     * @throws JMException
     */
    private void computeNewPositions() throws JMException {
        for (int i = 0; i < particlesSize_; i++) {
            //Variable[] particle = particles_.get(i).getDecisionVariables();
            XInt particle = new XInt(particles_.get(i));
            //particle.move(speed_[i]);
            for (int var = 0; var < particle.getNumberOfDecisionVariables(); var++) {
                particle.setValue(var, (int) Math.round(particle.getValue(var) + speed_[i][var]));

                if (particle.getValue(var) < problem_.getLowerLimit(var)) {
                    particle.setValue(var, (int) problem_.getLowerLimit(var));
                    speed_[i][var] = speed_[i][var] * ChVel1_; //
                }
                if (particle.getValue(var) > problem_.getUpperLimit(var)) {
                    particle.setValue(var, (int) problem_.getUpperLimit(var));
                    speed_[i][var] = speed_[i][var] * ChVel2_; //
                }
            }
        }
    } // computeNewPositions

    /**
     * Apply a mutation operator to some particles in the swarm
     * @throws JMException
     */
    private void mopsoMutation(int actualIteration, int totalIterations) throws JMException {
        for (int i = 0; i < particles_.size(); i++) {
            if ((i % 6) == 0) {
                polynomialMutation_.execute(particles_.get(i));
            }
            //if (i % 3 == 0) { //particles_ mutated with a non-uniform mutation %3
            //  nonUniformMutation_.execute(particles_.get(i));
            //} else if (i % 3 == 1) { //particles_ mutated with a uniform mutation operator
            //  uniformMutation_.execute(particles_.get(i));
            //} else //particles_ without mutation
            //;
        }
    } // mopsoMutation

    /**
     * Runs of the SMPSO algorithm.
     * @return a <code>SolutionSet</code> that is a set of non dominated solutions
     * as a result of the algorithm execution
     * @throws JMException
     */
    public SolutionSet execute() throws JMException, ClassNotFoundException {
        initParams();

        boolean outputEveryPopulation = false;
        Object output = getInputParameter("outputEveryPopulation");
        if(output != null){
        	outputEveryPopulation = (Boolean)output;
        }
        String outputPath = (String) getInputParameter("outputPath");
        
        success_ = false;
        //->Step 1 (and 3) Create the initial population and evaluate
        CheckpointFileParameter fileParam = (CheckpointFileParameter) getInputParameter("checkpointFile");
        String file = "";
        String speedFile = "";

        if (fileParam != null) {
            file = fileParam.GetCheckpointFile();
            speedFile = fileParam.GetSecondFile();
        }

        if (file != null && !file.equals("")) {
            Logger.getLogger(SMPSO.class.getName()).log(Level.WARNING, "Using a checkpoint file: " + file);
            int i = 0;
            try {
                //read population
                BufferedReader input = new BufferedReader(new FileReader(file));

                String line = null; //not declared within while loop
                line = input.readLine();//skip the headder
                while ((line = input.readLine()) != null && i < particlesSize_) {
                    Solution particle = new Solution(problem_);

                    StringTokenizer tokenizer = new StringTokenizer(line, ",");
                    for (int j = 0; j < problem_.getNumberOfVariables(); j++) {
                        particle.getDecisionVariables()[j].setValue(Double.valueOf(tokenizer.nextToken()));
                    }
                    problem_.evaluate(particle);
                    problem_.evaluateConstraints(particle);

                    particles_.add(particle);
                    i++;
                } //while

                //read speed
                if (speedFile != null && speedFile!="") {
                    speed_ = readCurrentSpeed(speedFile);
                }

            } catch (IOException ex) {
                Logger.getLogger(SMPSO.class.getName()).log(Level.SEVERE, "Checkpoint file does not have enough elements to fill the entire population [" + i + "<" + particlesSize_ + "]. Filling it with random individuals");
                while (i < particlesSize_) {
                    Solution particle = new Solution(problem_);
                    problem_.evaluate(particle);
                    problem_.evaluateConstraints(particle);
                    particles_.add(particle);
                    i++;
                }
            }
        } else {
            String force_feasible = (String) getInputParameter("forceFeasibleFirstGeneration");
            
            // Operator mutationOperator = operators_.get("mutation");
            
            /* for (int i = 0; i < particlesSize_; i++) {
                Solution particle = new Solution(problem_);
                problem_.evaluate(particle);
                problem_.evaluateConstraints(particle);
                particles_.add(particle);
            } */
            
            int i = 0;
            int infeasible_counter = 0;
            int last_infeasible_counter = 0;
            
            while (i < particlesSize_) {
                Solution particle = new Solution(problem_);
                problem_.evaluate(particle);
                problem_.evaluateConstraints(particle);
                
                if (force_feasible != null && force_feasible.equals("true")) {//this will skip ind only if they are infeasible because of constrains
                    if (particle.getNumberOfViolatedConstraint() > 0) {
                        infeasible_counter++;
                        
                        if((infeasible_counter - last_infeasible_counter) % 200 == 0) {
                            Logger.getLogger(SMPSO.class.getName()).log(Level.INFO, "Last " + (infeasible_counter - last_infeasible_counter) + " solutions infeasible or constraints infeasible - trying... " + infeasible_counter);
                        }
                        
                        if(infeasible_counter < 100000)  {
                            continue;
                        }
                    }
                }
                
                particles_.add(particle);
                last_infeasible_counter = infeasible_counter;
                i++;
            }
            
            
        }
        if (problem_ instanceof ServerSimulator) {
            ((ServerSimulator) problem_).join();//blocks until all  the offsprings are evaluated
            String currentMiliseconds = "" + System.currentTimeMillis();
            ((ServerSimulator) problem_).dumpCurrentPopulation("filled" + currentMiliseconds, particles_);
            dumpCurrentSpeed("speed" + currentMiliseconds);
        }

        //-> Step2. Initialize the speed_ of each particle to 0
        for (int i = 0; i < particlesSize_; i++) {
            for (int j = 0; j < problem_.getNumberOfVariables(); j++) {
                speed_[i][j] = 0.0;
            }
        }


        // Step4 and 5
        for (int i = 0; i < particles_.size(); i++) {
            Solution particle = new Solution(particles_.get(i));
            leaders_.add(particle);
        }

        //-> Step 6. Initialize the memory of each particle
        for (int i = 0; i < particles_.size(); i++) {
            Solution particle = new Solution(particles_.get(i));
            best_[i] = particle;
        }

        //Crowding the leaders_
        distance_.crowdingDistanceAssignment(leaders_, problem_.getNumberOfObjectives());

        //-> Step 7. Iterations ..
        while (iteration_ < maxIterations_) {
            try {
                //Compute the speed_
                computeSpeed(iteration_, maxIterations_);
            } catch (IOException ex) {
                Logger.getLogger(SMPSO.class.getName()).log(Level.SEVERE, null, ex);
            }

            //Compute the new positions for the particles_
            computeNewPositions();

            //Mutate the particles_
            mopsoMutation(iteration_, maxIterations_);

            //Evaluate the new particles_ in new positions
            for (int i = 0; i < particles_.size(); i++) {
                Solution particle = particles_.get(i);
                //This is a wokaround. problems might arise if you reuse the same solution
                Solution newParticle = new Solution(particle);
                for (int k = 0; k < particle.numberOfObjectives(); k++) {
                    particle.setObjective(k, 0);
                }
                problem_.evaluate(newParticle);
                particles_.replace(i, newParticle);
//                problem_.evaluate(particle);
            }
            if (problem_ instanceof ServerSimulator) {
                ((ServerSimulator) problem_).join();//blocks until all  the offsprings are evaluated             
                String currentMiliseconds = "" + System.currentTimeMillis();
                ((ServerSimulator) problem_).dumpCurrentPopulation("offspring" + currentMiliseconds, particles_);
                dumpCurrentSpeed("speed" + currentMiliseconds);
            }
            //Actualize the archive
            for (int i = 0; i < particles_.size(); i++) {
                Solution particle = new Solution(particles_.get(i));
                leaders_.add(particle);
            }

            //Actualize the memory of this particle
            for (int i = 0; i < particles_.size(); i++) {
                int flag = dominance_.compare(particles_.get(i), best_[i]);
                if (flag != 1) { // the new particle is best_ than the older remeber
                    Solution particle = new Solution(particles_.get(i));
                    //this.best_.reemplace(i,particle);
                    best_[i] = particle;
                }
            }

            //Crowding the leaders_
            distance_.crowdingDistanceAssignment(leaders_,
                    problem_.getNumberOfObjectives());
            iteration_++;
            if (problem_ instanceof ServerSimulator) {
                ((ServerSimulator) problem_).join();//blocks until all  the offsprings are evaluated
                String currentMiliseconds = "" + System.currentTimeMillis();
                ((ServerSimulator) problem_).dumpCurrentPopulation("filled" + currentMiliseconds, leaders_);
                dumpCurrentSpeed("speed" + currentMiliseconds);
            }
            else{
            	if(outputEveryPopulation){
            		particles_.printObjectivesToFile(outputPath+System.currentTimeMillis()+".csv");            	
            	}
            }
        }
        return this.leaders_;
    } // execute

    /**
     * Gets the leaders of the SMPSO algorithm
     */
    public SolutionSet getLeader() {
        return leaders_;
    }  // getLeader

    public void dumpCurrentSpeed(String filename) {
        //commented because buggy - first call rendered null pointer exception, also the speed is set to ) after initializing from file ???
//        String result = "";
//        int particleSize = particlesSize_;
//        int nrObjectives = problem_.getNumberOfVariables();
//        result += particleSize + "," + nrObjectives + "\n";
//
//        for (int i = 0; i < particleSize; i++) {
//            for (int j = 0; j < nrObjectives; j++) {
//                result += speed_[i][j] + ",";
//            }
//            result += "\n";
//        }
//
//        try {
//            BufferedWriter out = new BufferedWriter(new FileWriter(((ServerSimulator) problem_).getEnvironment().getResultsFolder() + System.getProperty("file.separator") + filename + ".spd"));
//            out.write(result);
//            out.close();
//        } catch (IOException e) {
//        }
    }

    public double[][] readCurrentSpeed(String filename) {
        double[][] speed = null;
        try {
            BufferedReader in = new BufferedReader(new FileReader(filename));
            String str = in.readLine();
            String[] resultString = str.split(",");

            int particleSize = Integer.parseInt(resultString[0]);
            int nrObjectives = Integer.parseInt(resultString[1]);

            speed = new double[particleSize][nrObjectives];

            for (int i = 0; i < particleSize; i++) {
                str = in.readLine();
                resultString = str.split(",");
                for (int j = 0; j < nrObjectives; j++) {
                    speed[i][j] = Double.parseDouble(resultString[j]);
                }
            }
            in.close();
        } catch (IOException e) {
        }

        return speed;
    }
} // SMPSO

