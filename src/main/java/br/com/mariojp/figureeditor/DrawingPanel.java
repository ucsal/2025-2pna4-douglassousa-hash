
package br.com.mariojp.figureeditor;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.RenderingHints;
import java.awt.Shape;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;

import javax.swing.JPanel;

class DrawingPanel extends JPanel {

    private static final long serialVersionUID = 1L;
    private static final int DEFAULT_SIZE = 60;
    private final List<Shape> shapes = new ArrayList<>();
    private Point startDrag = null;
    private Shape previewShape;
    private Color currentColor = Color.BLUE;
    private Shape selectedShape = null;
    private Point lastDragPoint = null;
    private final Deque<List<Shape>> undoStack = new ArrayDeque<>();
    private final Deque<List<Shape>> redoStack = new ArrayDeque<>();
    
    
    public Color getCurrentColor() {
        return currentColor;
    }

    public void setCurrentColor(Color c) {
        currentColor = c;
    }

    DrawingPanel() {
        
        setBackground(Color.WHITE);
        setOpaque(true);
        setDoubleBuffered(true);

        var mouse = new MouseAdapter() {
        	
        	@Override public void mouseReleased(MouseEvent e) {
        		 if (selectedShape != null) {
        		        selectedShape = null;
        		        lastDragPoint = null;
        		    } else if (previewShape != null) {
        		        shapes.add(previewShape);
        		        previewShape = null;
        		        repaint();
        		    }
        		    
        		 	startDrag = null;
            }
        	
        	@Override public void mouseDragged(MouseEvent e) {
        		
        		if (selectedShape != null && lastDragPoint != null) {
        	        double dx = e.getX() - lastDragPoint.x;
        	        double dy = e.getY() - lastDragPoint.y;
        	        
        	        int grid = 10;

        	        if (selectedShape instanceof Rectangle2D r) {
        	            double newX = r.getX() + dx;
        	            double newY = r.getY() + dy;

        	            
        	            newX = Math.round(newX / grid) * grid;
        	            newY = Math.round(newY / grid) * grid;

        	            r.setFrame(newX, newY, r.getWidth(), r.getHeight());

        	        } else if (selectedShape instanceof Ellipse2D c) {
        	            double newX = c.getX() + dx;
        	            double newY = c.getY() + dy;

        	            newX = Math.round(newX / grid) * grid;
        	            newY = Math.round(newY / grid) * grid;

        	            c.setFrame(newX, newY, c.getWidth(), c.getHeight());
        	        }

        	        lastDragPoint = e.getPoint();
        	        repaint();
        	        return;
        	    }
        		
        		if (startDrag!= null && selectedShape == null) {
        	        int x = Math.min(startDrag.x, e.getX());
        	        int y = Math.min(startDrag.y, e.getY());
        	        int w = Math.abs(e.getX() - startDrag.x);
        	        int h = Math.abs(e.getY() - startDrag.y);
        	        previewShape = new Rectangle2D.Double(x, y, w, h);
        	        repaint();
        	    }
            }
        	
        	
        	 @Override public void mousePressed(MouseEvent e) {
                 startDrag = e.getPoint();
                 selectedShape = null;
                 for (int i = shapes.size() - 1; i >= 0; i--) {
                     Shape s = shapes.get(i);
                     if (s.contains(e.getPoint())) {
                         selectedShape = s;
                         lastDragPoint = e.getPoint();
                         break;
                     }
                 }
             }
        	 
        	 
            @Override public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 1 && startDrag == null) {
                    int size = Math.max(Math.min(DEFAULT_SIZE, DEFAULT_SIZE), 10);
                    Shape s =  new Ellipse2D.Double(e.getPoint().x, e.getPoint().y, size, size);
                    shapes.add(s);
                    repaint();
                }
            }
           

        };
        addMouseListener(mouse);        
        addMouseMotionListener(mouse);

    }

    void clear() {
        shapes.clear();
        repaint();
    }
    
    
    
    void exportPNG(File file) throws IOException {
        BufferedImage img = new BufferedImage(getWidth(), getHeight(), BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2 = img.createGraphics();
        paint(g2); 
        g2.dispose();
        javax.imageio.ImageIO.write(img, "png", file);
    }
    
    void bringToFront() {
        if (selectedShape != null) {
            shapes.remove(selectedShape);
            shapes.add(selectedShape); 
            repaint();
        }
    }

    void sendToBack() {
        if (selectedShape != null) {
            shapes.remove(selectedShape);
            shapes.add(0, selectedShape); 
            repaint();
        }
    }
    
    private void saveState() {
        List<Shape> snapshot = new ArrayList<>();
        for (Shape s : shapes) {
            if (s instanceof Rectangle2D r) {
                snapshot.add((Rectangle2D) r.clone());
            } else if (s instanceof Ellipse2D c) {
                snapshot.add((Ellipse2D) c.clone());
            }
        }
        undoStack.push(snapshot);
        redoStack.clear();
    }
    
    
    void undo() {
        if (!undoStack.isEmpty()) {
            redoStack.push(copyShapes(shapes));
            shapes.clear();
            shapes.addAll(undoStack.pop());
            repaint();
        }
    }

    void redo() {
        if (!redoStack.isEmpty()) {
            undoStack.push(copyShapes(shapes));
            shapes.clear();
            shapes.addAll(redoStack.pop());
            repaint();
        }
    }

    private List<Shape> copyShapes(List<Shape> src) {
        List<Shape> copy = new ArrayList<>();
        for (Shape s : src) {
            if (s instanceof Rectangle2D r) {
                copy.add((Rectangle2D) r.clone());
            } else if (s instanceof Ellipse2D c) {
                copy.add((Ellipse2D) c.clone());
            }
        }
        return copy;
    }
    

    @Override protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        for (Shape s : shapes) {
        	g2.setColor(currentColor);
            g2.fill(s);
            g2.setColor(new Color(0,0,0,70));
            g2.setStroke(new BasicStroke(1.2f));
            g2.draw(s);
        }
        
        if (previewShape != null) {
            float[] dash = {5f, 5f};
            g2.setStroke(new BasicStroke(1, BasicStroke.CAP_BUTT,
                    BasicStroke.JOIN_BEVEL, 0, dash, 0));
            g2.setColor(Color.GRAY);
            g2.draw(previewShape);
        }

        g2.dispose();
    }

}
