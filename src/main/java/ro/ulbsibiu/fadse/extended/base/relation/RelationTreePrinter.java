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
package ro.ulbsibiu.fadse.extended.base.relation;

import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JFrame;

import com.mxgraph.swing.mxGraphComponent;
import com.mxgraph.view.mxGraph;
import java.util.List;
import java.util.Random;
import jMetal.util.JMException;

/**
 *
 * @author Horia Calborean
 */
public class RelationTreePrinter extends JFrame {

    /**
     *
     */
    private static final long serialVersionUID = -2707712944901661771L;

    RelationTreePrinter(List<RelationNode> roots) {

        super("Hello, World!");

        mxGraph graph = new mxGraph();
        Object parent = graph.getDefaultParent();
        graph.setDisconnectOnMove(false);
        graph.setResetEdgesOnMove(true);
        graph.getModel().beginUpdate();
        try {
            for (RelationNode r : roots) {
                String label = "";
                try {
                    label = r.getPosition() + " " + (r.getVariable() != null ? "_" + r.getVariable().getValue() : "");
                } catch (JMException ex) {
                    Logger.getLogger(RelationTreePrinter.class.getName()).log(Level.SEVERE, null, ex);
                }
                Object v1 = graph.insertVertex(parent, null, label, r.getPosition() * 40, 0, 30, 30);
                addChildren(r, v1, graph, parent);
            }

        } finally {

            graph.getModel().endUpdate();
        }

        mxGraphComponent graphComponent = new mxGraphComponent(graph);
        getContentPane().add(graphComponent);
    }

    public static void print(List<RelationNode> roots) {
        RelationTreePrinter frame = new RelationTreePrinter(roots);
        frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        frame.setSize(400, 320);
        frame.setVisible(true);
    }

    private void addChildren(RelationNode r, Object v1, mxGraph graph, Object parent) {
        Random rand = new Random();
        for (Child c : r.getChildren()) {
            String label = "";
            try {
                label = c.getChild().getPosition() + " " + (c.getChild().getVariable() != null ? "_" + c.getChild().getVariable().getValue() : "");
            } catch (JMException ex) {
                Logger.getLogger(RelationTreePrinter.class.getName()).log(Level.SEVERE, null, ex);
            }
            Object v2 = graph.insertVertex(parent, null, label, rand.nextInt(500), rand.nextInt(500), 30, 30);
            for (double deact : c.getDeactivationValues()) {
                graph.insertEdge(parent, null, deact, v1, v2);
            }
            addChildren(c.getChild(), v2, graph, parent);
        }

    }
}
