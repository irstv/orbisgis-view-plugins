package org.orbisgis.mapcomposer.view.ui;

import org.orbisgis.mapcomposer.controller.UIController;
import org.orbisgis.mapcomposer.view.utils.CompositionAreaOverlay;
import org.orbisgis.mapcomposer.view.utils.CompositionJPanel;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.image.BufferedImage;
import javax.swing.*;
import javax.swing.plaf.LayerUI;

/**
 * Area for the map document composition.
 * All the GraphicalElement will be drawn inside.
 */
public class CompositionArea extends JPanel{
    
    /**JScrollPane of the CompositionArea. */
    private final JScrollPane scrollPane;
    
    /**Main JPanel of the CompositionArea. */
    private final JPanel panel = new JPanel(null);
    
    /**Dimension of the document into the CompositionArea. */
    private Dimension dimension = new Dimension(50, 50);

    /** LayerUI use (in this case it's a CompositionAreaOverlay) to display information in the CompositionArea. */
    LayerUI<JComponent> layerUI;

    /** JLayer used to link the LayerUI and the CompositionArea. */
    JLayer<JComponent> jLayer;
    
    /**
     * Main constructor.
     */
    public CompositionArea(UIController uiController){
        super(new BorderLayout());
        JPanel body = new JPanel(new BorderLayout());
        body.add(panel, BorderLayout.CENTER);
        scrollPane = new JScrollPane(body, JScrollPane.VERTICAL_SCROLLBAR_ALWAYS, JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS);
        this.add(scrollPane, BorderLayout.CENTER);

        layerUI = new CompositionAreaOverlay(uiController);
        jLayer = new JLayer<>(panel, layerUI);
        this.add(jLayer);
    }

    /**
     * Enable or disable the CompositionAreaOverlay.
     * @param bool If true enable the overlay, disable it otherwise
     */
    public void setOverlayEnable(boolean bool){
        ((CompositionAreaOverlay)layerUI).setEnable(bool);
    }
    
    /**
     * Adds a CompositionPanel to itself. Should be call only once for each GraphicalElement.
     * @param panel CompositionPanel to add.
     */
    public void addGE(CompositionJPanel panel){
        this.panel.add(panel);
    }
    
    /**
     * Removes the given panel representing a GE.
     * @param panel Panel to remove.
     */
    public void removeGE(CompositionJPanel panel){
        if(this.panel.isAncestorOf(panel))
            this.panel.remove(panel);
        refresh();
    }
    
    /**
     * Sets the dimension of the document in the compositionArea.
     * This method should be called on the document properties definition.
     * @param dimension Dimension of the document.
     */
    public void setDocumentDimension(Dimension dimension){
        this.dimension =dimension;
        this.panel.setPreferredSize(this.dimension);
        this.revalidate();
        this.repaint();
    }

    /**
     * Sets the z-index of an element in the compositionArea.
     * @param comp Component to set.
     * @param i New z-index.
     */
    public void setZIndex(CompositionJPanel comp, int i) {
        panel.setComponentZOrder(comp, i);
    }

    /**
     * Removes all the drawn elements on the CompositionArea.
     */
    public void removeAllGE() {
        panel.removeAll();
        refresh();
    }
    
    /**
     * Refreshes the CompositionArea.
     */
    public void refresh(){
        panel.revalidate();
        panel.repaint();
    }
    
    /**
     * Returns the buffered image corresponding to the whole CompositionArea.
     * @return The buffered image corresponding to the CompositionArea.
     */
    public BufferedImage getDocBufferedImage(){
        BufferedImage bi = new BufferedImage(dimension.width, dimension.height, BufferedImage.TYPE_INT_ARGB);
        Graphics g = bi.createGraphics();
        panel.paint(g);
        g.dispose();
        return bi;
    }
}
