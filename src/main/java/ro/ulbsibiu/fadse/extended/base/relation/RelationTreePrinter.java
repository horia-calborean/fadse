package ro.ulbsibiu.fadse.extended.base.relation;

import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JFrame;

import com.mxgraph.swing.mxGraphComponent;
import com.mxgraph.view.mxGraph;
import org.uma.jmetal.util.errorchecking.JMetalException;

import java.util.List;
import java.util.Random;

public class RelationTreePrinter extends JFrame {

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
                    label = r.getPosition() + " " + (r.getVariable() != null ? "_" + r.getVariable() : "");
                } catch (JMetalException ex) {
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
                label = c.getChild().getPosition() + " " + (c.getChild().getVariable() != null ? "_" + c.getChild().getVariable() : "");
            } catch (JMetalException ex) {
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
