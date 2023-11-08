/*
 * This file is part of the FADSE tool.
 * 
 *   Authors: Horia Andrei Calborean {horia.calborean at ulbsibiu.ro}
 *   Copyright (c) 2009-2010
 *   All rights reserved.
 * 
 *   Redistribution and use in source and binary forms, with or without modification,
 *   are permitted provided that the following conditions are met:
 * 
 *   * Redistributions of source code must retain the above copyright notice,
 *      this list of conditions and the following disclaimer.
 * 
 *   * Redistributions in binary form must reproduce the above copyright notice,
 *      this list of conditions and the following disclaimer in the documentation
 *      and/or other materials provided with the distribution.
 * 
 *   The names of its contributors NOT may be used to endorse or promote products
 *   derived from this software without specific prior written permission.
 * 
 *   THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 *   AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO,
 *   THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR
 *   PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR
 *   CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL,
 *   EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO,
 *   PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS;
 *   OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY,
 *   WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 *   ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED
 *   OF THE POSSIBILITY OF SUCH DAMAGE.

 */
package ro.ulbsibiu.fadse.utils;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.StringTokenizer;
import java.util.logging.Level;
import java.util.logging.Logger;

import ro.ulbsibiu.fadse.environment.Environment;
import jmetal.base.Problem;
import jmetal.base.Solution;
import jmetal.base.SolutionSet;
import jMetal.util.JMException;

/**
 *
 * @author Horia Calborean
 */
public class CheckpointHelper {

    StringBuilder content;
    String fileName;
    Environment environment;

    public CheckpointHelper(String fileName, Environment environment) {
        this.fileName = fileName;
        this.environment = environment;
        content = new StringBuilder();
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public void store(SolutionSet solutionSet) {
        content.append((new Utils()).generateCSV(solutionSet));
    }

    public void load(SolutionSet solutionSet, int size, Problem problem) throws ClassNotFoundException, JMException {
        int i = 0;
        try {
            BufferedReader input = new BufferedReader(new FileReader(fileName));

            String line = null; //not declared within while loop
            while ((line = input.readLine()) != null && i < size) {
                Solution solution = new Solution(problem);

                StringTokenizer tokenizer = new StringTokenizer(line, ",");
                for (int j = 0; j < problem.getNumberOfVariables(); j++) {
                    solution.getDecisionVariables()[j].setValue(Double.valueOf(tokenizer.nextToken()));
                }
                problem.evaluate(solution);
                problem.evaluateConstraints(solution);

                solutionSet.add(solution);
                i++;
            } //while
        } catch (IOException ex) {
            Logger.getLogger(CheckpointHelper.class.getName()).log(Level.SEVERE, "Checkpoint file does not have enough elements to fill the entire population [" + i + "<" + size + "]. Filling it with random individuals");
            while (i < size) {
                Solution particle = new Solution(problem);
                problem.evaluate(particle);
                problem.evaluateConstraints(particle);
                solutionSet.add(particle);
                i++;
            }
        }
    }

    public void store(Solution[] solutions) {
        SolutionSet solutionSet = new SolutionSet(solutions.length);
        for (int i = 0; i < solutions.length; i++) {
            solutionSet.add(solutions[i]);
        }
        store(solutionSet);
    }

    public void load(Solution[] solutions, int size, Problem problem) throws ClassNotFoundException, JMException {
        SolutionSet solutionSet = new SolutionSet(size);
        load(solutionSet, size, problem);
    }

    public void store(double[] items) {
        for (int i = 0; i < items.length; i++) {
            content.append(items[i]);
            if (i != items.length - 1) {
                content.append(",");
            }
        }
        content.append(System.getProperty("line.separator"));
    }

    public void load(double[] items, int size) throws FileNotFoundException {
        try {
            BufferedReader input = new BufferedReader(new FileReader(fileName));
            String line = null; //not declared within while loop
            line = input.readLine();
            StringTokenizer tokenizer = new StringTokenizer(line, ",");
            for (int j = 0; j < size; j++) {
                items[j] = Double.valueOf(tokenizer.nextToken());
            }
        } catch (IOException ex) {
            Logger.getLogger(CheckpointHelper.class.getName()).log(Level.SEVERE, "IO Exception ");
        }
    }

    public void store(double[][] itemsArray) {
        for (int i = 0; i < itemsArray.length; i++) {
            store(itemsArray[i]);
        }
    }

    public void load(double[][] itemsArray, int lines, int columns) throws FileNotFoundException{
        itemsArray = new double[lines][columns];
        for (int i = 0; i < lines; i++){
            double[] line = new double[columns];
            load(line , columns);
            itemsArray[i] = line;
        }
    }

    public boolean flush() {
        try {
            BufferedWriter out = new BufferedWriter(new FileWriter(environment.getResultsFolder() + System.getProperty("file.separator") + fileName + ".csv"));
            out.write(content.toString());
            out.close();
            return true;
        } catch (IOException e) {
        }
        return false;
    }
}
