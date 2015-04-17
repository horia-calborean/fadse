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
package ro.ulbsibiu.fadse.extended.problems.simulators.unimap;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ro.ulbsibiu.acaps.ctg.xml.apcg.ApcgType;
import ro.ulbsibiu.acaps.ctg.xml.apcg.CoreType;
import ro.ulbsibiu.acaps.ctg.xml.apcg.ObjectFactory;
import ro.ulbsibiu.acaps.ctg.xml.apcg.TaskType;
import ro.ulbsibiu.acaps.ctg.xml.mapping.MapType;
import ro.ulbsibiu.acaps.ctg.xml.mapping.MappingType;
import ro.ulbsibiu.fadse.extended.problems.simulators.SimulatorBase;
import ro.ulbsibiu.fadse.extended.problems.simulators.SimulatorRunner;

/**
 * Simulator runner for the ns-3 NoC simulator of UniMap.
 * 
 * @author Ciprian Radu
 * @author Horia Calborean
 */
public class UniMapRunner extends SimulatorRunner {

	/**
	 * Logger for this class
	 */
	private static final Logger logger = LoggerFactory.getLogger(UniMapRunner.class);
	
	private static final String PARAMETER_PREFIX = "--";
	
	private static final String PARAMETER_APCG_CORE_PREFIX = "core-";
	
	private FileRemover fileRemover;
	
    public UniMapRunner(SimulatorBase simulator) {
        super(simulator);
        fileRemover = new FileRemover();
    }

    @Override
	protected String getParameterPrefix(String parameterName) {
		return PARAMETER_PREFIX;
	}

	@Override
    protected String[] getCommandLine() {
        LinkedList<String> params = new LinkedList<String>();
        params.add(this.simulator.getInputDocument().getSimulatorParameter("simulator_executable"));
        Map<String, Map<String,String>> coreUidIdMaps = new HashMap<String, Map<String,String>>();
        logger.info("Identifying core types");
        for (Map.Entry<String, String> param:  this.simpleParameters.entrySet()){
        	if (param.getKey().startsWith(PARAMETER_APCG_CORE_PREFIX)) {
        		String ctg = param.getKey().substring(PARAMETER_APCG_CORE_PREFIX.length(), param.getKey().lastIndexOf("_"));
        		String coreUid = param.getKey().substring(param.getKey().lastIndexOf("_") + 1);
        		if (coreUidIdMaps.get(ctg) == null) {
        			Map<String,String> coreUidIdMap = new HashMap<String, String>();
        			coreUidIdMap.put(coreUid, param.getValue());
        			coreUidIdMaps.put(ctg, coreUidIdMap);
        		} else {
        			Map<String, String> coreUidIdMap = coreUidIdMaps.get(ctg);
        			coreUidIdMap.put(coreUid, param.getValue());
        			coreUidIdMaps.put(ctg, coreUidIdMap);
        		}
        		logger.info("Core with UID " + coreUid + " (CTG " + ctg + ")" + " has type " + param.getValue());
        	} else {
	            String str = getParameterPrefix(param.getKey()) + param.getKey();
	            if (!param.getValue().isEmpty())
	                str += "=" + param.getValue();
	            params.add(str);
        	}
        }
        String mappingDir = getIndividual().getBenchmark().substring(0, getIndividual().getBenchmark().lastIndexOf(File.separator));
        String mappingFilePath = null;
        try {
			JAXBContext jaxbContext = JAXBContext.newInstance("ro.ulbsibiu.acaps.ctg.xml.mapping");
			Unmarshaller unmarshaller = jaxbContext.createUnmarshaller();
			@SuppressWarnings("unchecked")
			MappingType mapping = ((JAXBElement<MappingType>) unmarshaller.unmarshal(new File(getIndividual().getBenchmark()))).getValue();
			String apcgId = Long.toString(System.currentTimeMillis());
			logger.info("Creating APCG XML files with ID " + apcgId + " (same ID for all CTGs)");
	        for (String ctg: coreUidIdMaps.keySet()) {
	        	String apcgFileName = mappingDir.substring(0, mappingDir.lastIndexOf("ctg-"));
	        	String templateApcgId = getApcgId(mapping, ctg);
	        	apcgFileName = apcgFileName + "ctg-" + ctg + File.separator + "apcg-" + templateApcgId + ".xml";
	        	File apcgFile = new File(apcgFileName);
	        	if (apcgFile.exists()) {
	        		try {
						String modifiedApcg = modifyApcg(apcgFile, coreUidIdMaps.get(ctg), apcgId, ctg);
						String newApcgFileName = mappingDir.substring(0, mappingDir.lastIndexOf("ctg-")) + "ctg-" + ctg + File.separator + "apcg-" + ctg + "_" + apcgId + ".xml";
						PrintWriter pw = new PrintWriter(new File(newApcgFileName));
						pw.write(modifiedApcg);
						pw.close();
						logger.info("Created file " + newApcgFileName);
						fileRemover.addFileToBeCleaned(newApcgFileName);
					} catch (JAXBException e) {
						logger.error(e.getMessage());
					} catch (FileNotFoundException e) {
						logger.error(e.getMessage());
					}
	        	} else {
	        		logger.error("APCG file " + apcgFile.getName() + " does not exist! Exiting...");
	        		System.exit(0);
	        	}
	        }
	        for (MapType mapType : mapping.getMap()) {
	        	String oldApcg = mapType.getApcg();
	        	String prefix = oldApcg.substring(0, oldApcg.lastIndexOf("_"));
				mapType.setApcg(prefix + "_" + apcgId);
			}
	        Marshaller marshaller = jaxbContext.createMarshaller();
	        marshaller.setProperty("jaxb.formatted.output", Boolean.TRUE);
	        String newMapping = getIndividual().getBenchmark().substring(0, getIndividual().getBenchmark().lastIndexOf("_")) + "_" + apcgId + ".xml";
	        logger.info("Creating the mapping XML file " + newMapping + " (with the new APCG ID)");
			ro.ulbsibiu.acaps.ctg.xml.mapping.ObjectFactory mappingFactory = new ro.ulbsibiu.acaps.ctg.xml.mapping.ObjectFactory();
			JAXBElement<MappingType> jaxbElement = mappingFactory.createMapping(mapping);
	        marshaller.marshal(jaxbElement, new File(newMapping));
	        mappingFilePath = newMapping;
	        fileRemover.addFileToBeCleaned(newMapping);
        } catch (JAXBException e) {
        	logger.error(e.getMessage());
        }
        params.add(PARAMETER_PREFIX + "mapping-file-path=" + mappingFilePath);
        params.add(PARAMETER_PREFIX + "output-file=" + this.simulator.getInputDocument().getSimulatorParameter("simulator_output_file"));
        String[] result = new String[params.size()];
        params.toArray(result);

        return result;
    }

    @Override
    protected void prepareParameters() {
        super.prepareParameters();
    }
    
	private String modifyApcg(File apcgFile, Map<String, String> coreUidIdMap, String apcgId, String ctgId) throws JAXBException {
		if(apcgFile == null) { logger.warn("No APCG file specified!"); }
		if(coreUidIdMap == null) { logger.warn("No changes fot the APCG file!"); }
		if (logger.isDebugEnabled()) {
			logger.debug("Creating an APCG file from " + apcgFile.getName() + ", by modifying the core types");
		}
		
		JAXBContext jaxbContext = JAXBContext.newInstance("ro.ulbsibiu.acaps.ctg.xml.apcg");
		Unmarshaller unmarshaller = jaxbContext.createUnmarshaller();
		@SuppressWarnings("unchecked")
		ApcgType apcgType = ((JAXBElement<ApcgType>) unmarshaller.unmarshal(apcgFile)).getValue();
		logger.info("Replicating APCG file " + apcgFile.getPath() + ", with the following changes");
		apcgType.setId(ctgId + "_" + apcgId);
		logger.info("APCG ID set to " + ctgId + "_" + apcgId);
		
		List<CoreType> coreTypes = apcgType.getCore();
		for (CoreType coreType : coreTypes) {
			coreType.setId(coreUidIdMap.get(coreType.getUid()));
			logger.info("Core with UID " + coreType.getUid() + " has now type (ID) " + coreType.getId());
			for (TaskType apcgTaskType : coreType.getTask()) {
				String coreFilePath = apcgFile.getPath().substring(0, apcgFile.getPath().lastIndexOf(File.separator));
				coreFilePath = coreFilePath.substring(0, coreFilePath.lastIndexOf(File.separator));
				coreFilePath = coreFilePath + File.separator + "cores" + File.separator + "core-" + coreType.getId() + ".xml";
				JAXBContext jaxbContext1 = JAXBContext.newInstance("ro.ulbsibiu.acaps.ctg.xml.core");
				Unmarshaller unmarshaller1 = jaxbContext1.createUnmarshaller();
				@SuppressWarnings("unchecked")
				ro.ulbsibiu.acaps.ctg.xml.core.CoreType theCoreType = ((JAXBElement<ro.ulbsibiu.acaps.ctg.xml.core.CoreType>) unmarshaller1
						.unmarshal(new File(coreFilePath))).getValue();
				logger.info("Accessing core XML file " + coreFilePath + " to read task execution time and power");
				List<ro.ulbsibiu.acaps.ctg.xml.core.TaskType> taskList = theCoreType.getTask();
				for (ro.ulbsibiu.acaps.ctg.xml.core.TaskType coreTaskType : taskList) {
					String taskFilePath = apcgFile.getPath().substring(0, apcgFile.getPath().lastIndexOf(File.separator));
					taskFilePath = taskFilePath.substring(0, taskFilePath.lastIndexOf(File.separator));
					taskFilePath = taskFilePath + File.separator + "ctg-" + ctgId + File.separator + "tasks" + File.separator + "task-" + apcgTaskType.getId() + ".xml";
					JAXBContext jaxbContext2 = JAXBContext.newInstance("ro.ulbsibiu.acaps.ctg.xml.task");
					Unmarshaller unmarshaller2 = jaxbContext2.createUnmarshaller();
					@SuppressWarnings("unchecked")
					ro.ulbsibiu.acaps.ctg.xml.task.TaskType taskTaskType = ((JAXBElement<ro.ulbsibiu.acaps.ctg.xml.task.TaskType>) unmarshaller2
							.unmarshal(new File(taskFilePath))).getValue();
					if (coreTaskType.getType().equals(taskTaskType.getType())) {
						apcgTaskType.setExecTime(coreTaskType.getExecTime());
						apcgTaskType.setPower(coreTaskType.getPower());
						break;
					}
				}
				logger.info("Thus, its task with ID " + apcgTaskType.getId()
						+ " now has execution time " + apcgTaskType.getExecTime()
						+ " and power " + apcgTaskType.getPower());
				if(apcgTaskType.getExecTime() <= 0) { logger.error("Execution time must be positive!"); }
				if(apcgTaskType.getPower() <= 0) { logger.error("Power must be positive!"); }
			}
		}
		
		Marshaller marshaller = jaxbContext.createMarshaller();
		marshaller.setProperty("jaxb.formatted.output", Boolean.TRUE);
		StringWriter stringWriter = new StringWriter();
		ObjectFactory apcgFactory = new ObjectFactory();
		JAXBElement<ApcgType> apcg = apcgFactory.createApcg(apcgType);
		marshaller.marshal(apcg, stringWriter);
		
		return stringWriter.toString();
	}
	
	private String getApcgId(MappingType mapping, String ctgId) {
		String apcgId = null;
		List<MapType> map = mapping.getMap();
		for (MapType mapType : map) {
			if (mapType.getApcg().startsWith(ctgId + "_")) {
				apcgId = mapType.getApcg();
				break;
			}
		}
		return apcgId;
	}

	/**
	 * @return the {@link FileRemover}
	 */
	public FileRemover getXmlFileCleaner() {
		return fileRemover;
	}

}
