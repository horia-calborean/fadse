/*
 * This file is part of the FADSE tool.
 * 
 *   Authors: 	Ciprian Radu {ciprian.radu at ulbsibiu.ro} 
 *   			Horia Andrei Calborean {horia.calborean at ulbsibiu.ro}
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
package ro.ulbsibiu.fadse.extended.problems.simulators;

import ro.ulbsibiu.fadse.environment.Environment;
import ro.ulbsibiu.fadse.extended.problems.simulators.unimap.FileRemover;
import ro.ulbsibiu.fadse.extended.problems.simulators.unimap.UniMapOutputParser;
import ro.ulbsibiu.fadse.extended.problems.simulators.unimap.UniMapRunner;

/**
 * Simulator configuration for UniMap
 * 
 * @author Ciprian Radu
 */
public class UniMapSimulator extends SimulatorBase {

	/**
	 * Constructor
	 * 
	 * @param inputDocument
	 */
	public UniMapSimulator(Environment environment) throws ClassNotFoundException {
		super(environment);
		this.simulatorOutputFile = environment.getInputDocument().getSimulatorParameter("simulator_output_file");
		this.simulatorRunner = new UniMapRunner(this);
		FileRemover fileRemover = ((UniMapRunner) simulatorRunner).getXmlFileCleaner();
		this.simulatorOutputParser = new UniMapOutputParser(this, fileRemover);
	}
}
