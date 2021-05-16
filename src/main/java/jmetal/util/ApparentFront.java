package jmetal.util;

import jmetal.base.Problem;
import jmetal.base.SolutionSet;
import org.apache.commons.math3.linear.EigenDecomposition;
import org.apache.commons.math3.linear.MatrixUtils;
import org.apache.commons.math3.linear.RealMatrix;
import org.apache.commons.math3.linear.RealVector;
import ro.ulbsibiu.fadse.environment.Environment;
import ro.ulbsibiu.fadse.extended.problems.simulators.ServerSimulator;
import ro.ulbsibiu.fadse.utils.Utils;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

public class ApparentFront {
    private double[] coefficients;
    private double power;

    /**
     * Instantiates an {@link ApparentFront} representing an ellipses described by an array of coefficients and a power
     *
     * @param power the power considered for the ellipse's equation
     */
    public ApparentFront(double power) {
        this.power = power;
    }

    public double[] getCoefficients() {
        return coefficients;
    }

    public double getPower() {
        return power;
    }

    /**
     * Performs ellipse fitting finding the coefficients of the {@link ApparentFront}
     *
     * @param supportVectors the {@link SolutionSet} considered for finding the Apparent Front
     */
    public void fit(SolutionSet supportVectors) {
        int n = supportVectors.size();
        int dims = supportVectors.get(0).numberOfObjectives();
        
        System.out.println("Dimensions number:" + dims + "\n" + "support vectors size: " + n);
        
        dumpCurrentFrontSynthetic(supportVectors, "dumped"+System.currentTimeMillis());
        
        double[][] d = new double[n][dims + 1];
        for (int i = 0; i < n; i++) {
            double[] row = new double[dims + 1];
            for (int j = 0; j < dims; j++) {
                row[j] = Math.pow(supportVectors.get(i).getObjective(j), power);
            }
            row[dims] = 1;
            d[i] = row;
        }

        RealMatrix designMatrix = MatrixUtils.createRealMatrix(d);

        RealMatrix scatterMatrix = designMatrix.preMultiply(designMatrix.transpose());

        RealMatrix invScatterMatrix = MatrixUtils.inverse(scatterMatrix);
        EigenDecomposition eigenDecomposition = new EigenDecomposition(invScatterMatrix);

        double[] eigenValues = eigenDecomposition.getRealEigenvalues();
        int pos = indexOfMax(eigenValues);

        RealVector eigenVector = eigenDecomposition.getEigenvector(pos);

        coefficients = new double[eigenVector.getDimension()];
        double[] eigenVectorArray = eigenVector.toArray();

        for (int i = 0; i < eigenVectorArray.length; i++) {
            coefficients[i] = -eigenVectorArray[i] / eigenVectorArray[dims];
            if (i < 2 && coefficients[i] < 0) {
                System.out.println("Negative AF coefficients!!!");
                System.exit(1);
            }
        }
    }

    public void dumpCurrentFront(Problem problem, String filename) {
        if (problem instanceof ServerSimulator) {
            Environment environment = ((ServerSimulator) problem).getEnvironment();

            String result = (new Utils()).generateCSVHeaderForApparentFront(this);
            result += (new Utils()).generateCSVForApparentFront(this);

            try {
                (new File(environment.getResultsFolder())).mkdirs();
                BufferedWriter out = new BufferedWriter(new FileWriter(environment.getResultsFolder() + System.getProperty("file.separator") + filename + ".csv"));
                out.write(result);
                out.close();
            } catch (IOException e) {
                System.err.println(e.getMessage());
                e.printStackTrace();
            }
        }
    }
    
    public void dumpCurrentFrontSynthetic(SolutionSet population, String filename) {
        //if (problem instanceof ServerSimulator) {
            //Environment environment = ((ServerSimulator) problem).getEnvironment();

            String resultFolder = "d:/work/outputs/";
            
            String result = "objectives\n";
            result += (new Utils()).generateCSV(population);

            try {
                (new File(resultFolder)).mkdirs();
                BufferedWriter out = new BufferedWriter(new FileWriter(resultFolder + System.getProperty("file.separator") + filename + ".csv"));
                out.write(result);
                out.close();
            } catch (IOException e) {
                System.err.println(e.getMessage());
                e.printStackTrace();
            }
        //}
    }

    /**
     * @param array an array of double
     * @return the position of the element with the maximum value
     */
    private int indexOfMax(double[] array) {
        int index = 0;

        for (int i = 0; i < array.length; i++) {
            index = array[i] > array[index] ? i : index;
        }

        return index;
    }
}
