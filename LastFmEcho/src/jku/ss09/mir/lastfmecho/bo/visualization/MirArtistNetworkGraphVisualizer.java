/*
 * Copyright (c) 2003, the JUNG Project and the Regents of the University of
 * California All rights reserved.
 * 
 * This software is open-source under the BSD license; see either "license.txt"
 * or http://jung.sourceforge.net/license.txt for a description.
 * 
 */
package jku.ss09.mir.lastfmecho.bo.visualization;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Graphics;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;

import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import jku.ss09.mir.lastfmecho.bo.MirArtist;

import org.apache.commons.collections15.Transformer;

import edu.uci.ics.jung.algorithms.layout.FRLayout;
import edu.uci.ics.jung.graph.DirectedSparseGraph;
import edu.uci.ics.jung.graph.Graph;
import edu.uci.ics.jung.graph.UndirectedSparseGraph;
import edu.uci.ics.jung.graph.UndirectedSparseMultigraph;
import edu.uci.ics.jung.graph.event.GraphEvent.Edge;
import edu.uci.ics.jung.graph.util.EdgeType;
import edu.uci.ics.jung.visualization.GraphZoomScrollPane;
import edu.uci.ics.jung.visualization.VisualizationViewer;
import edu.uci.ics.jung.visualization.control.CrossoverScalingControl;
import edu.uci.ics.jung.visualization.control.DefaultModalGraphMouse;
import edu.uci.ics.jung.visualization.control.ModalGraphMouse;
import edu.uci.ics.jung.visualization.control.ScalingControl;
import edu.uci.ics.jung.visualization.decorators.PickableEdgePaintTransformer;
import edu.uci.ics.jung.visualization.decorators.PickableVertexPaintTransformer;
import edu.uci.ics.jung.visualization.decorators.ToStringLabeller;
import edu.uci.ics.jung.visualization.renderers.DefaultEdgeLabelRenderer;
import edu.uci.ics.jung.visualization.renderers.DefaultVertexLabelRenderer;

/**
 * A demo that shows drawn Icons as vertices
 * 
 * @author Tom Nelson 
 * 
 */
public class MirArtistNetworkGraphVisualizer {

    static final int SIMILARITY_MIN = 0;
    static final int SIMILARITY_MAX = 100;
    static final int SIMILARITY_DEFAULT  = 90;    //initial frames per second
	
	
	/**
     * the graph
     */
    Graph<Integer,Number> graph;

    /**
     * the visual component and renderer for the graph
     */
    VisualizationViewer<Integer,Number> vv;

	private List<MirArtist> artistList;

	private double[][] similarityMatrix;
    
    public MirArtistNetworkGraphVisualizer(List<MirArtist> artistList, double[][] similarityMatrix) {
    	this.artistList = artistList;
    	this.similarityMatrix = similarityMatrix;
    }
    

	public boolean init(){
        
		
		if (artistList.size() > similarityMatrix.length)
		{
			System.out.println("Error in MirArtistNetworkGraphVisualizer - The similaritymatrix is smaller than number of artists ");
			return false;
		}
		
		graph = getGraph(SIMILARITY_DEFAULT / 100.0);
		setVisualizationRenderer();
        
        // create a frome to hold the graph
        final JFrame frame = new JFrame();
        Container content = frame.getContentPane();
        final GraphZoomScrollPane panel = new GraphZoomScrollPane(vv);
        content.add(panel);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        
        final ModalGraphMouse gm = new DefaultModalGraphMouse<Integer,Number>();
        vv.setGraphMouse(gm);
        
        final ScalingControl scaler = new CrossoverScalingControl();

        JButton plus = new JButton("+");
        plus.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                scaler.scale(vv, 1.1f, vv.getCenter());
            }
        });
        JButton minus = new JButton("-");
        minus.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                scaler.scale(vv, 1/1.1f, vv.getCenter());
            }
        });
        


        JSlider slider = new JSlider(JSlider.HORIZONTAL,SIMILARITY_MIN,SIMILARITY_MAX,SIMILARITY_DEFAULT);
        slider.setMajorTickSpacing(10);
        slider.setPaintTicks(true);

        
        final JLabel sliderLabel = new JLabel("0.87");
        
        final MirArtistNetworkGraphVisualizer thisPointer = this;
        
        slider.addChangeListener(new ChangeListener()
        {
			@Override
			public void stateChanged(ChangeEvent arg0) {
				JSlider source = (JSlider)arg0.getSource();
                if (!source.getValueIsAdjusting()) {
                    System.out.println(source.getValue()/100.0);
                    
                    Graph testG = getGraph(source.getValue() /100.0);
                    sliderLabel.setText(Double.toString(source.getValue() /100.0));
                    thisPointer.vv.setGraphLayout(new FRLayout<Integer,Number>(testG));
                    //thisPointer.setVisualizationRenderer();
                    thisPointer.vv.validate();
                    thisPointer.vv.repaint();
                    
                }    
			}
        	
        });

        JPanel controls = new JPanel();
        controls.add(plus);
        controls.add(minus);
        controls.add(((DefaultModalGraphMouse<Integer,Number>) gm).getModeComboBox());
        controls.add(new JLabel("Similarity Limit: "));
        controls.add(sliderLabel);
        controls.add(slider);
        content.add(controls, BorderLayout.SOUTH);

        frame.pack();
        frame.setVisible(true);
        
        return true;
    }

	private void setVisualizationRenderer() {
		if (graph == null) {
			return ;
		}

			
        vv =  new VisualizationViewer<Integer,Number>(new FRLayout<Integer,Number>(graph));
        vv.getRenderContext().setVertexLabelTransformer(new Transformer<Integer,String>(){

			public String transform(Integer v) {
				return artistList.get(v).getName();
				//return "Vertex "+v;
			}});
        vv.getRenderContext().setVertexLabelRenderer(new DefaultVertexLabelRenderer(Color.cyan));
        vv.getRenderContext().setEdgeLabelRenderer(new DefaultEdgeLabelRenderer(Color.cyan));

//        vv.getRenderContext().setVertexIconTransformer(new Transformer<Integer,Icon>() {
//
//        	/*
//        	 * Implements the Icon interface to draw an Icon with background color and
//        	 * a text label
//        	 */
//			public Icon transform(final Integer v) {
//				return new Icon() {
//
//					public int getIconHeight() {
//						return 20;
//					}
//
//					public int getIconWidth() {
//						return 20;
//					}
//
//					public void paintIcon(Component c, Graphics g,
//							int x, int y) {
//						if(vv.getPickedVertexState().isPicked(v)) {
//							g.setColor(Color.yellow);
//						} else {
//							g.setColor(Color.red);
//						}
//						g.fillOval(x, y, 20, 20);
//						if(vv.getPickedVertexState().isPicked(v)) {
//							g.setColor(Color.black);
//						} else {
//							g.setColor(Color.white);
//						}
//						g.drawString(""+v, x+6, y+15);
//						
//					}};
//			}});
//
//        vv.getRenderContext().setVertexFillPaintTransformer(new PickableVertexPaintTransformer<Integer>(vv.getPickedVertexState(), Color.white,  Color.yellow));
//        vv.getRenderContext().setEdgeDrawPaintTransformer(new PickableEdgePaintTransformer<Number>(vv.getPickedEdgeState(), Color.black, Color.lightGray));

        //for displaying edge labels - care this is index displayed not weight - i dont know how to set weights...
//        vv.getRenderContext().setEdgeLabelTransformer(new ToStringLabeller<Number>());
        vv.setBackground(Color.white);

        // add my listener for ToolTips
        vv.setVertexToolTipTransformer(new ToStringLabeller<Integer>());
	}

	private Graph getGraph(double limit) {
		Graph graph = new UndirectedSparseGraph<Integer, Number>();
		
		int idx = 0;
		for (MirArtist artist : artistList) {
			
			boolean findSimilaritiesAboveLimit = findSimilarityAboveLimit(limit, idx);
			
			if (findSimilaritiesAboveLimit)  {
				graph.addVertex((Integer) idx);	
			}

			
			idx++;
		}
		
		
		double edgeIndex = 0;
		for (int i = 0; i < artistList.size(); i++) {
			for (int j = 0; j < artistList.size(); j++) {
				
				if (j > i) {
					if (similarityMatrix[i][j] > limit) {
						graph.addEdge(edgeIndex, i,j,EdgeType.UNDIRECTED);
						edgeIndex++;
					}
				}
			}
		}
		return graph;
	}


	private boolean findSimilarityAboveLimit(double limit, int idx) {
		boolean findSimilaritiesAboveLimit = false;
		for (int i = 0; i < artistList.size(); i++) {
			if (idx != i)
			{
				if (similarityMatrix[idx][i] > limit)
				{
					findSimilaritiesAboveLimit = true;
					break;
				}
			}
		}
		return findSimilaritiesAboveLimit;
	}
    
    
    /**
     * create some vertices
     * @param count how many to create
     * @return the Vertices in an array
     */
    private Integer[] createVertices(int count) {
        Integer[] v = new Integer[count];
        for (int i = 0; i < count; i++) {
            v[i] = new Integer(i);
            graph.addVertex(v[i]);
        }
        return v;
    }

    /**
     * create edges for this demo graph
     * @param v an array of Vertices to connect
     */
    void createEdges(Integer[] v) {
        graph.addEdge(new Double(Math.random()), v[0], v[1], EdgeType.DIRECTED);
        graph.addEdge(new Double(Math.random()), v[0], v[3], EdgeType.DIRECTED);
        graph.addEdge(new Double(Math.random()), v[0], v[4], EdgeType.DIRECTED);
        graph.addEdge(new Double(Math.random()), v[4], v[5], EdgeType.DIRECTED);
        graph.addEdge(new Double(Math.random()), v[3], v[5], EdgeType.DIRECTED);
        graph.addEdge(new Double(Math.random()), v[1], v[2], EdgeType.DIRECTED);
        graph.addEdge(new Double(Math.random()), v[1], v[4], EdgeType.DIRECTED);
        graph.addEdge(new Double(Math.random()), v[8], v[2], EdgeType.DIRECTED);
        graph.addEdge(new Double(Math.random()), v[3], v[8], EdgeType.DIRECTED);
        graph.addEdge(new Double(Math.random()), v[6], v[7], EdgeType.DIRECTED);
        graph.addEdge(new Double(Math.random()), v[7], v[5], EdgeType.DIRECTED);
        graph.addEdge(new Double(Math.random()), v[0], v[9], EdgeType.DIRECTED);
        graph.addEdge(new Double(Math.random()), v[9], v[8], EdgeType.DIRECTED);
        graph.addEdge(new Double(Math.random()), v[7], v[6], EdgeType.DIRECTED);
        graph.addEdge(new Double(Math.random()), v[6], v[5], EdgeType.DIRECTED);
        graph.addEdge(new Double(Math.random()), v[4], v[2], EdgeType.DIRECTED);
        graph.addEdge(new Double(Math.random()), v[5], v[4], EdgeType.DIRECTED);
    }


}
