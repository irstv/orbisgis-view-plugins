package org.orbisgis.mapcomposer.controller;

import static org.orbisgis.mapcomposer.controller.UIController.ZIndex.TO_FRONT;
import org.orbisgis.mapcomposer.model.configurationattribute.interfaces.ConfigurationAttribute;
import org.orbisgis.mapcomposer.model.configurationattribute.interfaces.RefreshCA;
import org.orbisgis.mapcomposer.model.configurationattribute.utils.CAManager;
import org.orbisgis.mapcomposer.model.graphicalelement.element.Document;
import org.orbisgis.mapcomposer.model.graphicalelement.interfaces.AlwaysOnBack;
import org.orbisgis.mapcomposer.model.graphicalelement.interfaces.AlwaysOnFront;
import org.orbisgis.mapcomposer.model.graphicalelement.interfaces.GERefresh;
import org.orbisgis.mapcomposer.model.graphicalelement.interfaces.GraphicalElement;
import org.orbisgis.mapcomposer.model.graphicalelement.interfaces.GraphicalElement.Property;
import org.orbisgis.mapcomposer.model.graphicalelement.utils.GEManager;
import org.orbisgis.mapcomposer.model.utils.SaveAndLoadHandler;
import org.orbisgis.mapcomposer.model.utils.LinkToOrbisGIS;
import org.orbisgis.mapcomposer.view.ui.MainWindow;
import org.orbisgis.mapcomposer.view.utils.CompositionJPanel;
import org.orbisgis.mapcomposer.view.utils.DialogProperties;
import java.awt.Dimension;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Stack;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.imageio.ImageIO;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.filechooser.FileFilter;
import javax.xml.parsers.ParserConfigurationException;

import org.slf4j.LoggerFactory;
import org.xml.sax.SAXException;

/**
 * This class manager the interaction between the user, the UI and the data model.
 */
public class UIController{

    /** CAManager */
    private CAManager caManager;

    /** GEManager */
    private GEManager geManager;
    
    /**Map doing the link between GraphicalElements and their CompositionJPanel*/
    private static LinkedHashMap<GraphicalElement, CompositionJPanel> map;
    
    /**Selected GraphicalElement */
    private List<GraphicalElement> selectedGE;
    
    /**GraphicalElement stack giving the Z-index information*/
    private static Stack<GraphicalElement> zIndexStack;
    
    private static SaveAndLoadHandler listGE;
    
    private MainWindow mainWindow;
    
    /**
     * Main constructor.
     */
    public UIController(){
        //Initialize the different attributes
        caManager = new CAManager();
        geManager = new GEManager();
        map = new LinkedHashMap<>();
        selectedGE = new ArrayList<>();
        zIndexStack = new Stack<>();
        listGE = new SaveAndLoadHandler(geManager, caManager);
        mainWindow = new MainWindow(this);
    }
    
    public MainWindow getMainWindow() { return mainWindow; }

    /**
     * Returns the CAManager.
     * @return The CAManager
     */
    public CAManager getCAManager() { return caManager; }
    
    /**
     * Change the Z-index of the displayed GraphicalElement.
     * @param z Change of the Z-index.
     */
    public void zindexChange(ZIndex z){
        Stack<GraphicalElement> temp = new Stack<>();
        Stack<GraphicalElement> tempBack = new Stack<>();
        Stack<GraphicalElement> tempFront = new Stack<>();
        //Get the GE implementing the AlwaysOnBack interface and put them at the back
        for(GraphicalElement ge : zIndexStack){
            if(ge instanceof AlwaysOnBack){
                tempBack.push(ge);
                temp.add(ge);
            }
        }
        //Get the GE implementing the AlwaysOnFront interface and put them at the front
        for(GraphicalElement ge : zIndexStack){
            if(ge instanceof AlwaysOnFront){
                tempFront.push(ge);
                temp.add(ge);
            }
        }
        //Remove the previous detected elements
        for(GraphicalElement ge : temp){
            zIndexStack.remove(ge);
        }
        temp = new Stack<>();
        //Move the others GE
        for(GraphicalElement ge : selectedGE){
            if(zIndexStack.contains(ge)){
                switch (z){
                    case TO_BACK:
                        temp.push(ge);
                        toBackFront(ge, temp);
                        break;
                    case TO_FRONT:
                        toBackFront(ge, temp);
                        temp.push(ge);
                        break;
                    case FRONT:
                        if(zIndexStack.indexOf(ge)>0)
                            backFront(1, ge, temp);
                        break;
                    case BACK:
                        if(zIndexStack.indexOf(ge)<zIndexStack.size()-1)
                            backFront(-1, ge, temp);
                        break;
                }
            }
        }
        //Add to the stack the GE of the front
        while(!tempFront.empty())
            zIndexStack.push(tempFront.pop());
        while(!temp.empty())
            zIndexStack.push(temp.pop());
        //Add to the stack the GE of the back
        while(!tempBack.empty())
            zIndexStack.push(tempBack.pop());
        //Set the z-index of the GE from their stack position
        for(GraphicalElement g : zIndexStack){
            mainWindow.getCompositionArea().setZIndex(map.get(g), zIndexStack.indexOf(g));
        }
        validateSelectedGE();
    }
    
    /**
     * Move the GraphicalElement one step to the front or back and set its index.
     * @param deltaZ Variation of the Z-index of the GE.
     * @param ge GraphicalElement to move.
     * @param temp Temporary stack.
     */
    private void backFront(int deltaZ, GraphicalElement ge, Stack<GraphicalElement> temp){
        //Get the target index in the temporary stack (reverse order of the class stack)
        int target = zIndexStack.size()-(zIndexStack.indexOf(ge)-deltaZ);
        zIndexStack.remove(ge);
        int i=1;

        //While the object stack isn't empty, push element in the temporary stack.
        while(!zIndexStack.empty()){
            //Push the GE at the write index.
            if(i==target)
                temp.push(ge);
            else
                temp.push(zIndexStack.pop());
            i++;
        }
        //If the GE has the higher index, it isn't already pushed.
        if(!temp.contains(ge))
            temp.push(ge);
    }   
    
    /**
     * Move the GraphicalElement to the front or back and set its index.
     * @param ge GraphicalElement to move.
     * @param temp Temporary stack.
     */
    private void toBackFront(GraphicalElement ge, Stack<GraphicalElement> temp){
        zIndexStack.remove(ge);
        while(!zIndexStack.empty())
            temp.push(zIndexStack.pop());
    }
    
    /**
     * Selects a GraphicalElement and redisplays the ConfigurationAttributes.
     * @param ge GraphicalElement to select.
     */
    public void selectGE(GraphicalElement ge){
        selectedGE.add(ge);
        map.get(ge).select();
        refreshSpin();
    }
    
    public void refreshSpin(){
        boolean boolX=false, boolY=false, boolW=false, boolH=false, boolR=false;
        int x=0, y=0, w=0, h=0, r=0; 
        if(!selectedGE.isEmpty()){
            x=selectedGE.get(0).getX();
            y=selectedGE.get(0).getY();
            w=selectedGE.get(0).getWidth();
            h=selectedGE.get(0).getHeight();
            r=selectedGE.get(0).getRotation();
            for(GraphicalElement graph : selectedGE){
                if(x!=graph.getX()){ boolX=true;x=selectedGE.get(0).getX();}
                if(y!=graph.getY()){ boolY=true;y=selectedGE.get(0).getY();}
                if(w!=graph.getWidth()){ boolW=true;w=selectedGE.get(0).getWidth();}
                if(h!=graph.getHeight()){ boolH=true;h=selectedGE.get(0).getHeight();}
                if(r!=graph.getRotation()){ boolR=true;r=selectedGE.get(0).getRotation();}
            }
        }
        mainWindow.setSpinner(boolX, x, Property.X);
        mainWindow.setSpinner(boolY, y, Property.Y);
        mainWindow.setSpinner(boolW, w, Property.WIDTH);
        mainWindow.setSpinner(boolH, h, Property.HEIGHT);
        mainWindow.setSpinner(boolR, r, Property.ROTATION);
    }
    
    /**
     * Unselects a GraphicalElement and redisplays the ConfigurationAttributes.
     * @param ge GraphicalElement to select.
     */
    public void unselectGE(GraphicalElement ge){
        selectedGE.remove(ge);
        map.get(ge).unselect();
        refreshSpin();
    }
    
    /**
     * Unselect all the GraphicalElement.
     * Reset the selectedGE list and unselect all the CompositionJPanel in the compositionArea.
     */
    public void unselectAllGE(){
        //Unselect all the GraphicalElements
        for(GraphicalElement ge : selectedGE)
            map.get(ge).unselect();
        selectedGE= new ArrayList<>();
        refreshSpin();
    }
    
    /**
     * Remove all the selected GraphicalElement.
     */
    public void remove(){
        for(GraphicalElement ge : selectedGE){
            mainWindow.getCompositionArea().removeGE(map.get(ge));
            map.remove(ge);
            zIndexStack.remove(ge);
        }
        for(GraphicalElement ge : zIndexStack)
            mainWindow.getCompositionArea().setZIndex(map.get(ge), zIndexStack.indexOf(ge));
        selectedGE=new ArrayList<>();
        mainWindow.getCompositionArea().refresh();
    }
    
    
    public void validateSelectedGE(){
        for(GraphicalElement ge : selectedGE)
            validateGE(ge);
    }
    
    public void validateGE(GraphicalElement ge){
        if(ge instanceof GERefresh)
            ((GERefresh)ge).refresh();
        map.get(ge).setPanel(geManager.getRenderer(ge.getClass()).render(ge, map.get(ge)));
        if(ge instanceof Document)
            mainWindow.getCompositionArea().setDocumentDimension(new Dimension(ge.getWidth(), ge.getHeight()));
        refreshSpin();
    }

    /**
     * Read a List of ConfigurationAttribute to set the GraphicalElement.
     * This action is done when the button validate of the ConfigurationShutter is clicked. 
     * @param listCA List of ConfigurationAttributes to read.
     */
    public void validate(List<ConfigurationAttribute> listCA) {
        //Apply the function to all the selected GraphicalElements
        for(GraphicalElement ge : selectedGE){
            //Takes each ConfigurationAttribute from the GraphicalElement
            for(ConfigurationAttribute ca : ge.getAllAttributes())
                //Takes each CA from the list of CA to validate
                for(ConfigurationAttribute confShutterCA : listCA)
                    //If the two CA are the same property and are unlocked, set the new CA value
                    if(ca.isSameName(confShutterCA)){
                        if(!confShutterCA.getReadOnly()){
                            ca.setValue(confShutterCA.getValue());
                            if(ca instanceof RefreshCA)
                                ((RefreshCA)ca).refresh(this);
                        }
                        break;
                    }
            if(ge instanceof GERefresh)
                ((GERefresh)ge).refresh();
            map.get(ge).setPanel(geManager.getRenderer(ge.getClass()).render(ge, map.get(ge)));
            if(ge instanceof Document)
                mainWindow.getCompositionArea().setDocumentDimension(((Document)zIndexStack.peek()).getDimension());
        }
        unselectAllGE();
    }
    
    /**
     * Returns the list of ConfigurationAttributes that all the selected GraphicalElements have in common.
     * @return List of common ConfigurationAttributes (common about the names and not values).
     */
    private List<ConfigurationAttribute> getCommonAttributes(){
        List<ConfigurationAttribute> list = selectedGE.get(0).getAllAttributes();
        List<ConfigurationAttribute> listRemove = new ArrayList<>();
        //Compare each the CA of the list to those of the GE from selectedGE
        boolean flag=false;
        for(ConfigurationAttribute caList : list){
            for(GraphicalElement ge : selectedGE){
                flag=false;
                for(ConfigurationAttribute caGE : ge.getAllAttributes()){
                    //refresh the attributes
                    if(caGE instanceof RefreshCA) ((RefreshCA)caGE).refresh(this);
                    
                    if(caList.isSameName(caGE)){
                        flag=true;
                        caList.setReadOnly(!caList.isSameValue(caGE));
                    }
                }
            }
            //If the CA isn't in common, it's added to a list to be removed after
            if(!flag){
                listRemove.add(caList);
            }
        }
        for(ConfigurationAttribute ca : listRemove){
            list.remove(ca);
        }
        
        return list;
    }

    /**
     * Adds a new GraphicalElement to the UIController and its JPanel to the COmpositionArea.
     * @param aClass Class of the new GraphicalElement.
     */
    public void addGE(Class<? extends GraphicalElement> aClass) {
        //Creates the GraphicalElement.
        GraphicalElement ge = null;
        try {
            ge = aClass.newInstance();
        } catch (InstantiationException|IllegalAccessException ex) {
            LoggerFactory.getLogger(UIController.class).error(ex.getMessage());
        }
        if(ge!=null) {
            //Registers the GE and its CompositionJPanel.
            map.put(ge, new CompositionJPanel(ge, this));
            mainWindow.getCompositionArea().addGE(map.get(ge));
            map.get(ge).setPanel(geManager.getRenderer(ge.getClass()).render(ge, map.get(ge)));
            zIndexStack.push(ge);

            //Refreshes the GE.
            if (ge instanceof GERefresh)
                ((GERefresh) ge).refresh();

            //Selects only the GE
            selectedGE = new ArrayList<>();
            selectedGE.add(ge);

            //Bring it to the front an validate its default properties.
            zindexChange(TO_FRONT);
            validateGE(ge);

            //Show the configuration dialog
            showProperties();
        }
    }
    
    /**
     * Breaks the encapsulation and should be removed.
     */
    public LinkedHashMap<GraphicalElement, CompositionJPanel> getGEMap(){
        return map;
    }

    /**
     * Remove all the displayed GE from the panel.
     */
    public void removeAllGE() {
        mainWindow.getCompositionArea().removeAllGE();
        map = new LinkedHashMap<>();
        selectedGE = new ArrayList<>();
        zIndexStack = new Stack<>();
    }

    /**
     * Run saveProject function of the SaveHandler.
     */
    public void save(){
        try {
            listGE.saveProject(zIndexStack.subList(0, zIndexStack.size()));
        } catch (NoSuchMethodException|IOException ex) {
            LoggerFactory.getLogger(UIController.class).error(ex.getMessage());
        }
    }

    /**
     * Run loadProject function from the SaveHandler and draw loaded GE.
     */
    public void load(){
        try {
            removeAllGE();
            List<GraphicalElement> list = listGE.loadProject();
            for(GraphicalElement ge : list){
                addLoadedGE(ge);
            }
        } catch (ParserConfigurationException|SAXException|IOException ex) {
            LoggerFactory.getLogger(UIController.class).error(ex.getMessage());
        }
    }

    /**
     * Add to the project the given GE (that is just loaded)..
     * @param ge GE to add to the project.
     */
    private void addLoadedGE(GraphicalElement ge) {
        map.put(ge, new CompositionJPanel(ge, this));
        mainWindow.getCompositionArea().addGE(map.get(ge));
        if(ge instanceof GERefresh){
            ((GERefresh)ge).refresh();
        }
        map.get(ge).setPanel(geManager.getRenderer(ge.getClass()).render(ge, map.get(ge)));
        zIndexStack.push(ge);
        selectedGE = new ArrayList<>();
        zindexChange(TO_FRONT);
        validateGE(ge);
        mainWindow.getCompositionArea().refresh();
    }
    
    public void setAlign( Align a) {
        if(selectedGE.size()>0){
            int xMin;
            int xMax;
            int yMin;
            int yMax;
            switch(a){
                case LEFT:
                    xMin=selectedGE.get(0).getX();
                    for (GraphicalElement ge : selectedGE){
                        if (ge.getX() < xMin)
                            xMin = ge.getX();
                    }
                    for(GraphicalElement ge : selectedGE){
                        ge.setX(xMin);
                    }
                    break;
                case CENTER:
                    xMin=selectedGE.get(0).getX();
                    xMax=selectedGE.get(0).getX()+selectedGE.get(0).getWidth();
                    for (GraphicalElement ge : selectedGE) {
                        if (ge.getX() < xMin)
                            xMin = ge.getX();
                        if (ge.getX()+ge.getWidth() > xMax)
                            xMax = ge.getX()+ge.getWidth();
                    }
                    int xMid = (xMax+xMin)/2;
                    for(GraphicalElement ge : selectedGE)
                        ge.setX(xMid-ge.getWidth()/2);
                    break;
                case RIGHT:
                    xMax=selectedGE.get(0).getX()+selectedGE.get(0).getWidth();
                    for (GraphicalElement ge : selectedGE)
                        if (ge.getX()+ge.getWidth() > xMax)
                            xMax = ge.getX()+ge.getWidth();
                    for(GraphicalElement ge : selectedGE)
                        ge.setX(xMax-ge.getWidth());
                    break;
                case TOP:
                    yMin=selectedGE.get(0).getY();
                    for (GraphicalElement ge : selectedGE)
                        if (ge.getY() < yMin)
                            yMin = ge.getY();
                    for(GraphicalElement ge : selectedGE)
                        ge.setY(yMin);
                    break;
                case MIDDLE:
                    yMin=selectedGE.get(0).getY();
                    yMax=selectedGE.get(0).getY()+selectedGE.get(0).getHeight();
                    for (GraphicalElement ge : selectedGE) {
                        if (ge.getY() < yMin)
                            yMin = ge.getY();
                        if (ge.getY()+ge.getHeight() > yMax)
                            yMax = ge.getY()+ge.getHeight();
                    }
                    int yMid = (yMax+yMin)/2;
                    for(GraphicalElement ge : selectedGE)
                        ge.setY(yMid-ge.getHeight()/2);
                    break;
                case BOTTOM:
                    yMax=selectedGE.get(0).getY()+selectedGE.get(0).getHeight();
                    for (GraphicalElement ge : selectedGE)
                        if (ge.getY()+ge.getHeight() > yMax)
                            yMax = ge.getY()+ge.getHeight();
                    for(GraphicalElement ge : selectedGE)
                        ge.setY(yMax-ge.getHeight());
                    break;
            }
            validateSelectedGE();
        }
    }
    
    public void showProperties(){
        if(selectedGE.size()>0){
            //If the only one GraphicalElement is selected, the locking checkboxes are hidden
            DialogProperties dp = new DialogProperties(getCommonAttributes(), this, selectedGE.size()>1);
            dp.setVisible(true);
        }
    }
    
    /**
     * Open a dialog window with all the ducument properties.
     */
    public void showDocProperties(){
        Document doc=null;
        for(GraphicalElement ge : zIndexStack)
            if(ge instanceof Document){
                doc=(Document)ge;
                break;
            }
        
        if(doc!=null){
            for(GraphicalElement graph : selectedGE)
                unselectGE(graph);
            selectGE(doc);
            DialogProperties dp = new DialogProperties(getCommonAttributes(), this, false);
            dp.setVisible(true);
        }
    }
    
    public enum ZIndex{
        TO_FRONT, FRONT, BACK, TO_BACK;
    }
    
    public enum Align{
        LEFT, CENTER, RIGHT, TOP, MIDDLE, BOTTOM;
    }
    
    public void changeProperty(GraphicalElement.Property prop, int i){
        for(GraphicalElement ge : selectedGE){
            switch(prop){
                case X:
                    ge.setX(i);
                    break;
                case Y:
                    ge.setY(i);
                    break;
                case WIDTH:
                    ge.setWidth(i);
                    break;
                case HEIGHT:
                    ge.setHeight(i);
                    break;
                case ROTATION:
                    ge.setRotation(i);
                    break;
            }
            validateGE(ge);
        mainWindow.getCompositionArea().refresh();
        }
    }
    
    /**
     * Export to document into png file.
     */
    public void export(){
        //Creates and sets the file chooser
        JFileChooser fc = new JFileChooser();
        fc.setCurrentDirectory(new File(LinkToOrbisGIS.getInstance().getViewWorkspace().getCoreWorkspace().getWorkspaceFolder()));
        fc.setDialogType(JFileChooser.CUSTOM_DIALOG);
        fc.setDialogTitle("Export document into PNG");
        fc.setFileSelectionMode(JFileChooser.FILES_ONLY);
        fc.setFileFilter(new FileFilter() {

            @Override public boolean accept(File file) {
                if(file.isDirectory()) return true;
                return file.getAbsolutePath().toLowerCase().contains(".png");
            }

            @Override public String getDescription() {return "PNG Files (.png)";}
        });
        //If the saveProject is validated, do the export
        if(fc.showDialog(new JFrame(), "Export")==JFileChooser.APPROVE_OPTION){
            //Sets the path of the new file
            String path;
            if(fc.getSelectedFile().exists() && fc.getSelectedFile().isFile())
                path=fc.getSelectedFile().getAbsolutePath();
            else 
                path=fc.getSelectedFile().getAbsolutePath()+".png";

            try{
                //Remove the border, do the export, readd them
                for(GraphicalElement ge : zIndexStack){
                    map.get(ge).enableBorders(false);
                }
                ImageIO.write(mainWindow.getCompositionArea().getDocBufferedImage(),"png",new File(path));
                
                for(GraphicalElement ge : zIndexStack){
                    map.get(ge).enableBorders(true);
                }
            } catch (IOException ex) {
                Logger.getLogger(UIController.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }
}
