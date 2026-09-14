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

package ro.ulbsibiu.fadse.environment.rule;

import ro.ulbsibiu.fadse.environment.parameters.Parameter;

/**
 *
 * @author Horia
 */
public class IfRule implements Rule {

    String type;
    String description;
    Rule ifExpresion;
    Rule thenExpresion;

    public IfRule(String type, String description, Rule ifExpresion, Rule thenExpresion) {
        this.type = type;
        this.description = description;
        this.ifExpresion = ifExpresion;
        this.thenExpresion = thenExpresion;
    }

    public boolean validate(Parameter[] parameters) {
        boolean result = true;
        if (ifExpresion.validate(parameters)) {
            result = thenExpresion.validate(parameters);
        }
        return result;
    }

    @Override
    public String toString() {
        String result = "\nIfRule: " + type + " " + description + "if(" + ifExpresion.toString() + ") then {" + thenExpresion.toString() + "}";
        return result;
    }

    public Rule getIfExpresion() {
        return ifExpresion;
    }

    public void setIfExpresion(Rule ifExpresion) {
        this.ifExpresion = ifExpresion;
    }

    public Rule getThenExpresion() {
        return thenExpresion;
    }

    public void setThenExpresion(Rule thenExpresion) {
        this.thenExpresion = thenExpresion;
    }
    
}
