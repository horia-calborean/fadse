/*
 * This file is part of the FADSE tool.
 * 
 *   Authors: Horia Andrei Calborean {horia.calborean at ulbsibiu.ro}
 *   Copyright (c) 2009-2011
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
package net.sourceforge.jFuzzyLogic.defuzzifier;

import java.util.LinkedList;
import java.util.Random;
import net.sourceforge.jFuzzyLogic.rule.Variable;

/**
 *
 * @author Horia Calborean
 */
public class DefuzzifierRandom extends DefuzzifierContinuous {
private double treshold;
    public DefuzzifierRandom(Variable variable, double treshold) {
        super(variable);
        this.treshold = treshold;
    }

    @Override
    public double defuzzify() {
        double x = min, sum = 0, weightedSum = 0;
        // Calculate integrals (approximated as sums)
        LinkedList<Double> possibleValues = new LinkedList<Double>();
        for (int i = 0; i < values.length; i++, x += stepSize) {
            if(values[i]>treshold){//membership
                possibleValues.add(x);
            }
        }
        if (possibleValues.size()==0) {
            return Double.NaN;
        } else {
            Random r = new Random(System.currentTimeMillis());

            return possibleValues.get(r.nextInt(possibleValues.size()));
        }

    }

    @Override
    public String toStringFcl() {
        return "METHOD : RANDOM;";
    }
}
