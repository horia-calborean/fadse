package ro.ulbsibiu.fadse;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.commons.cli.PosixParser;

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

/**
 *
 * @author Horia Calborean
 */
public class TestCLI {

    public static void main(String[] args) throws ParseException {
          // Create a Parser
        args = new String[]{"-h","-c","2132","-s"};
        CommandLineParser parser = new PosixParser();
        Option client = new Option("c", "client", true, "Specifies that a client needs to be run");
        client.setOptionalArg(true);
        client.setArgName("port");
        Option server = new Option("s", "server", false, "Specifies that a server needs to be run");

        Option xmlConfig = new Option("x", "xml", true, "Specifies the input xml file");
        xmlConfig.setArgName("xmlFile");

        Option checkpointFile = new Option("ch", "checkpoint", true, "Specifies the checkpoint file");
        checkpointFile.setArgName("checkpointFile");


        Options options = new Options();
        options.addOption("h", "help", false, "Print this usage information");
        options.addOption(client);
        options.addOption(server);
        // Parse the program arguments
        CommandLine commandLine = parser.parse(options, args);

        System.out.println(commandLine.hasOption("c"));
        System.out.println(commandLine.getOptionValue("c"));
















    }
}
