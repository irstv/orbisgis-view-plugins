/*
 * OrbisGIS is a GIS application dedicated to scientific spatial simulation.
 * This cross-platform GIS is developed at French IRSTV institute and is able to
 * manipulate and create vector and raster spatial information. 
 * 
 * OrbisGIS is distributed under GPL 3 license. It is produced by the "Atelier SIG"
 * team of the IRSTV Institute <http://www.irstv.fr/> CNRS FR 2488.
 * 
 * Copyright (C) 2007-2012 IRSTV (FR CNRS 2488)
 * 
 * This file is part of OrbisGIS.
 * 
 * OrbisGIS is free software: you can redistribute it and/or modify it under the
 * terms of the GNU General Public License as published by the Free Software
 * Foundation, either version 3 of the License, or (at your option) any later
 * version.
 * 
 * OrbisGIS is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR
 * A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License along with
 * OrbisGIS. If not, see <http://www.gnu.org/licenses/>.
 * 
 * For more information, please consult: <http://www.orbisgis.org/>
 * or contact directly:
 * info_at_ orbisgis.org
 */
package org.orbisgis.groovy;

import groovy.lang.GroovyShell;
import org.apache.commons.io.FileUtils;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.fife.rsta.ac.LanguageSupportFactory;
import org.fife.rsta.ac.groovy.GroovyLanguageSupport;
import org.fife.ui.rsyntaxtextarea.RSyntaxTextArea;
import org.fife.ui.rsyntaxtextarea.SyntaxConstants;
import org.fife.ui.rtextarea.RTextScrollPane;
import org.orbisgis.core.Services;
import org.orbisgis.coremap.layerModel.MapContext;
import org.orbisgis.mapeditorapi.MapElement;
import org.orbisgis.progress.ProgressMonitor;
import org.orbisgis.sif.UIFactory;
import org.orbisgis.sif.components.OpenFilePanel;
import org.orbisgis.sif.components.SaveFilePanel;
import org.orbisgis.view.background.BackgroundJob;
import org.orbisgis.view.background.BackgroundManager;
import org.orbisgis.view.components.Log4JOutputStream;
import org.orbisgis.view.components.actions.ActionCommands;
import org.orbisgis.view.components.findReplace.FindReplaceDialog;
import org.orbisgis.view.icons.OrbisGISIcon;
import org.orbisgis.view.util.CommentUtil;
import org.orbisgis.viewapi.components.actions.DefaultAction;
import org.orbisgis.viewapi.docking.DockingPanelParameters;
import org.orbisgis.viewapi.edition.EditableElement;
import org.orbisgis.viewapi.edition.EditorDockable;
import org.orbisgis.viewapi.edition.EditorManager;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;
import org.xnap.commons.i18n.I18n;
import org.xnap.commons.i18n.I18nFactory;

import javax.sql.DataSource;
import javax.swing.*;
import javax.swing.event.CaretListener;
import javax.swing.event.DocumentListener;
import java.awt.*;
import java.awt.event.ActionListener;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.beans.EventHandler;
import java.beans.PropertyChangeListener;
import java.io.IOException;
import java.io.PrintStream;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

/**
 * Create the groovy console panel
 *
 * @author Erwan Bocher
 */
@Component(service = EditorDockable.class)
public class GroovyConsolePanel extends JPanel implements EditorDockable {

    public static final String EDITOR_NAME = "Groovy";
    private static final I18n I18N = I18nFactory.getI18n(GroovyConsolePanel.class);
    private static final Logger LOGGER = Logger.getLogger("gui." + GroovyConsolePanel.class);
    private static final Logger LOGGER_POPUP = Logger.getLogger("gui.popup" + GroovyConsolePanel.class);
    private final Log4JOutputStream infoLogger = new Log4JOutputStream(LOGGER, Level.INFO);
    private final Log4JOutputStream errorLogger = new Log4JOutputStream(LOGGER, Level.ERROR);
    private DockingPanelParameters parameters = new DockingPanelParameters();
    private ActionCommands actions = new ActionCommands();
    private GroovyLanguageSupport gls;
    private RTextScrollPane centerPanel;
    private RSyntaxTextArea scriptPanel;
    private DefaultAction executeAction;
    private DefaultAction clearAction;
    private DefaultAction saveAction;
    private DefaultAction findAction;
    private DefaultAction commentAction;
    private DefaultAction blockCommentAction;
    private FindReplaceDialog findReplaceDialog;
    private int line = 0;
    private int character = 0;
    private MapElement mapElement;
    private static final String MESSAGEBASE = "%d | %d";
    private JLabel statusMessage = new JLabel();
    private Map<String, Object> variables = new HashMap<String, Object>();
    private Map<String, Object> properties = new HashMap<String, Object>();

    /**
     * Create the groovy console panel
     */
    public GroovyConsolePanel() {
        setLayout(new BorderLayout());
        add(getCenterPanel(), BorderLayout.CENTER);
        add(statusMessage, BorderLayout.SOUTH);
        init();
    }

    @Reference(cardinality = ReferenceCardinality.OPTIONAL)
    public void setDataSource(DataSource ds) {
        variables.put("ds", ds);
    }

    public void unsetDataSource(DataSource ds) {
        variables.remove("ds");
    }

    @Reference(cardinality = ReferenceCardinality.OPTIONAL)
    public void setEditorManager(EditorManager editorManager) {
        // If a map is already loaded fetch it in the EditorManager
        if(editorManager != null) {
            try {
                mapElement = MapElement.fetchFirstMapElement(editorManager);
            } catch (Exception ex) {
                LOGGER.error(ex.getLocalizedMessage(), ex);
            }
        }
        if (mapElement != null) {
            setMapContext(mapElement.getMapContext());
        }
    }

    public void unsetEditorManager(EditorManager editorManager) {
    }
    /**
     * Init the groovy panel with all docking parameters and set the necessary
     * properties to the console shell
     */
    private void init() {
        properties.put("out", new PrintStream(infoLogger));
        properties.put("err", new PrintStream(errorLogger));
        parameters.setName(EDITOR_NAME);
        parameters.setTitle(I18N.tr("Groovy"));
        parameters.setTitleIcon(new ImageIcon(GroovyConsolePanel.class.getResource("icon.png")));
        parameters.setDockActions(getActions().getActions());
    }

    /**
     * Get the action manager.
     *
     * @return ActionCommands instance.
     */
    public ActionCommands getActions() {
        return actions;
    }

    /**
     * The main panel to write and execute a groovy script.
     *
     * @return
     */
    private RTextScrollPane getCenterPanel() {
        if (centerPanel == null) {
            initActions();
            LanguageSupportFactory lsf = LanguageSupportFactory.get();
            gls = (GroovyLanguageSupport) lsf.getSupportFor(SyntaxConstants.SYNTAX_STYLE_GROOVY);
            scriptPanel = new RSyntaxTextArea();
            scriptPanel.setLineWrap(true);
            lsf.register(scriptPanel);
            scriptPanel.setSyntaxEditingStyle(RSyntaxTextArea.SYNTAX_STYLE_GROOVY);
            scriptPanel.addCaretListener(EventHandler.create(CaretListener.class, this, "onScriptPanelCaretUpdate"));
            scriptPanel.getDocument().addDocumentListener(EventHandler.create(DocumentListener.class, this, "onUserSelectionChange"));
            scriptPanel.clearParsers();
            scriptPanel.setTabsEmulated(true);
            scriptPanel.setTabSize(4);
            actions.setAccelerators(scriptPanel);
            // Actions will be set on the scriptPanel PopupMenu
            scriptPanel.getPopupMenu().addSeparator();
            actions.registerContainer(scriptPanel.getPopupMenu());
            centerPanel = new RTextScrollPane(scriptPanel);
            onUserSelectionChange();
        }
        return centerPanel;
    }

    /**
     * Create actions instances
     *
     * Each action is put in the Popup menu and the tool bar Their shortcuts are
     * registered also in the editor
     */
    private void initActions() {
        //Execute action
        executeAction = new DefaultAction(GroovyConsoleActions.A_EXECUTE, I18N.tr("Execute"),
                I18N.tr("Execute the groovy script"),
                OrbisGISIcon.getIcon("execute"),
                EventHandler.create(ActionListener.class, this, "onExecute"),
                KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, InputEvent.CTRL_DOWN_MASK));
        actions.addAction(executeAction);

        //Clear action
        clearAction = new DefaultAction(GroovyConsoleActions.A_CLEAR,
                I18N.tr("Clear"),
                I18N.tr("Erase the content of the editor"),
                OrbisGISIcon.getIcon("erase"),
                EventHandler.create(ActionListener.class, this, "onClear"),
                null);
        actions.addAction(clearAction);

        //Open action
        actions.addAction(new DefaultAction(GroovyConsoleActions.A_OPEN,
                I18N.tr("Open"),
                I18N.tr("Load a file in this editor"),
                OrbisGISIcon.getIcon("open"),
                EventHandler.create(ActionListener.class, this, "onOpenFile"),
                KeyStroke.getKeyStroke(KeyEvent.VK_O, InputEvent.CTRL_DOWN_MASK)));
        //Save
        saveAction = new DefaultAction(GroovyConsoleActions.A_SAVE,
                I18N.tr("Save"),
                I18N.tr("Save the editor content into a file"),
                OrbisGISIcon.getIcon("save"),
                EventHandler.create(ActionListener.class, this, "onSaveFile"),
                KeyStroke.getKeyStroke(KeyEvent.VK_S, InputEvent.CTRL_DOWN_MASK));
        actions.addAction(saveAction);
        //Find action
        findAction = new DefaultAction(GroovyConsoleActions.A_SEARCH,
                I18N.tr("Search.."),
                I18N.tr("Search text in the document"),
                OrbisGISIcon.getIcon("find"),
                EventHandler.create(ActionListener.class, this, "openFindReplaceDialog"),
                KeyStroke.getKeyStroke(KeyEvent.VK_F, InputEvent.CTRL_DOWN_MASK)).addStroke(KeyStroke.getKeyStroke(KeyEvent.VK_H, InputEvent.CTRL_DOWN_MASK));
        actions.addAction(findAction);

        // Comment/Uncomment
        commentAction = new DefaultAction(GroovyConsoleActions.A_COMMENT,
                I18N.tr("(Un)comment"),
                I18N.tr("(Un)comment the selected text"),
                null,
                EventHandler.create(ActionListener.class, this, "onComment"),
                KeyStroke.getKeyStroke("alt C")).setLogicalGroup("format");
        actions.addAction(commentAction);

        // Block Comment/Uncomment
        blockCommentAction = new DefaultAction(GroovyConsoleActions.A_BLOCKCOMMENT,
                I18N.tr("Block (un)comment"),
                I18N.tr("Block (un)comment the selected text."),
                null,
                EventHandler.create(ActionListener.class, this, "onBlockComment"),
                KeyStroke.getKeyStroke("alt shift C")).setLogicalGroup("format");
        actions.addAction(blockCommentAction);
    }

    /**
     * Clear the content of the console
     */
    public void onClear() {
        if (scriptPanel.getDocument().getLength() != 0) {
            int answer = JOptionPane.showConfirmDialog(this,
                    I18N.tr("Do you want to clear the contents of the console?"),
                    I18N.tr("Clear script"), JOptionPane.YES_NO_OPTION);
            if (answer == JOptionPane.YES_OPTION) {
                scriptPanel.setText("");
            }
        }
    }

    /**
     * Open a dialog that let the user select a file and save the content of the
     * sql editor into this file.
     */
    public void onSaveFile() {
        final SaveFilePanel outfilePanel = new SaveFilePanel(
                "groovyConsoleOutFile", I18N.tr("Save script"));
        outfilePanel.addFilter("groovy", I18N.tr("Groovy script (*.groovy)"));
        outfilePanel.loadState();
        if (UIFactory.showDialog(outfilePanel)) {
            try {
                FileUtils.writeLines(outfilePanel.getSelectedFile(), Arrays.asList(scriptPanel.getText()));
            } catch (IOException e1) {
                LOGGER.error(I18N.tr("Cannot write the script."), e1);
            }
            LOGGER_POPUP.info(I18N.tr("The file has been saved."));
        }
    }

    /**
     * Open a dialog that let the user select a file and add or replace the
     * content of the sql editor.
     */
    public void onOpenFile() {
        final OpenFilePanel inFilePanel = new OpenFilePanel("groovyConsoleInFile",
                I18N.tr("Open script"));
        inFilePanel.addFilter("groovy", I18N.tr("Groovy Script (*.groovy)"));
        inFilePanel.loadState();
        if (UIFactory.showDialog(inFilePanel)) {
            int answer = JOptionPane.NO_OPTION;
            if (scriptPanel.getDocument().getLength() > 0) {
                answer = JOptionPane.showConfirmDialog(
                        this,
                        I18N.tr("Do you want to clear all before loading the file ?"),
                        I18N.tr("Open file"),
                        JOptionPane.YES_NO_CANCEL_OPTION);
            }
            String text;
            try {
                text = FileUtils.readFileToString(inFilePanel.getSelectedFile());
            } catch (IOException e1) {
                LOGGER.error(I18N.tr("Cannot write the script."), e1);
                return;
            }

            if (answer == JOptionPane.YES_OPTION) {
                scriptPanel.setText(text);
            } else if (answer == JOptionPane.NO_OPTION) {
                scriptPanel.append(text);
            }
        }
    }

    /**
     * Update the row:column label
     */
    public void onScriptPanelCaretUpdate() {
        line = scriptPanel.getCaretLineNumber() + 1;
        character = scriptPanel.getCaretOffsetFromLineStart();
        statusMessage.setText(String.format(MESSAGEBASE, line, character));
    }

    /**
     * Change the status of the button when the console is empty or not.
     */
    public void onUserSelectionChange() {
        String text = scriptPanel.getText().trim();
        if (text.isEmpty()) {
            executeAction.setEnabled(false);
            clearAction.setEnabled(false);
            saveAction.setEnabled(false);
            findAction.setEnabled(false);
            commentAction.setEnabled(false);
            blockCommentAction.setEnabled(false);
        } else {
            executeAction.setEnabled(true);
            clearAction.setEnabled(true);
            saveAction.setEnabled(true);
            findAction.setEnabled(true);
            commentAction.setEnabled(true);
            blockCommentAction.setEnabled(true);
        }
    }

    /**
     * User click on execute script button
     */
    public void onExecute() {
        if(executeAction.isEnabled()) {
            String text = scriptPanel.getText().trim();
            GroovyJob groovyJob = new GroovyJob(text, properties, variables,
                    new  Log4JOutputStream[] {infoLogger, errorLogger}, executeAction);
            BackgroundManager backgroundManager = Services.getService(BackgroundManager.class);
            backgroundManager.nonBlockingBackgroundOperation(groovyJob);
        }
    }

    /**
     * Expose the map context in the groovy interpreter
     *
     * @param mc MapContext instance
     */
    private void setMapContext(MapContext mc) {
        try {
            if(mc != null) {
                variables.put("mc", mc);
            } else {
                variables.remove("mc");
            }
        } catch (Error ex) {
            LOGGER.error(ex.getLocalizedMessage(), ex);
        }
    }

    @Override
    public boolean match(EditableElement editableElement) {
        return editableElement instanceof MapElement;
    }

    @Override
    public EditableElement getEditableElement() {
        return null;
    }

    @Override
    public void setEditableElement(EditableElement editableElement) {
        if(editableElement instanceof MapElement) {
            setMapContext(((MapElement) editableElement).getMapContext());
        }
    }

    /**
     * Dispose
     */
    public void freeResources() {
        if (gls != null) {
            gls.uninstall(scriptPanel);
        }
    }

    @Override
    public DockingPanelParameters getDockingParameters() {
        return parameters;
    }

    @Override
    public JComponent getComponent() {
        return this;
    }

    /**
     * (Un)comment the selected text.
     */
    public void onComment() {
        CommentUtil.commentOrUncommentJava(scriptPanel);
    }

    /**
     * Block (un)comment the selected text.
     */
    public void onBlockComment() {
        CommentUtil.blockCommentOrUncomment(scriptPanel);
    }

    /**
     * Open one instanceof the find replace dialog
     */
    public void openFindReplaceDialog() {
        if (findReplaceDialog == null) {
            findReplaceDialog = new FindReplaceDialog(scriptPanel, UIFactory.getMainFrame());
        }
        findReplaceDialog.setAlwaysOnTop(true);
        findReplaceDialog.setVisible(true);
    }

    /**
     * Execute the provided script in groovy
     */
    private static class GroovyJob implements BackgroundJob {

        private String script;
        private Log4JOutputStream[] loggers;
        private Map<String, Object> variables;
        private Map<String, Object> properties;
        private Action executeAction;

        public GroovyJob(String script,Map<String, Object> properties, Map<String, Object> variables, Log4JOutputStream[] loggers, Action executeAction) {
            this.script = script;
            this.loggers = loggers;
            this.variables = variables;
            this.properties = properties;
            this.executeAction = executeAction;
        }

        @Override
        public void run(ProgressMonitor progressMonitor) {
            GroovyExecutor groovyExecutor = new GroovyExecutor(script, properties, variables, loggers, executeAction);
            progressMonitor.addPropertyChangeListener(ProgressMonitor.PROP_CANCEL,
                    EventHandler.create(PropertyChangeListener.class, groovyExecutor, "stop"));
            groovyExecutor.execute();
            while(!(groovyExecutor.isDone() || groovyExecutor.isCancelled() )) {
                try {
                    Thread.sleep(500);
                } catch (Exception ex) {
                    break;
                }
            }
        }

        @Override
        public String getTaskName() {
            return I18N.tr("Execute Groovy script");
        }
    }

    public static class GroovyExecutor extends SwingWorker<Object, Object> {

        private String script;
        private Log4JOutputStream[] loggers;
        private Map<String, Object> variables;
        private Map<String, Object> properties;
        private Action executeAction;
        private Thread runThread;

        public GroovyExecutor(String script,Map<String, Object> properties, Map<String, Object> variables, Log4JOutputStream[] loggers, Action executeAction) {
            this.script = script;
            this.loggers = loggers;
            this.variables = variables;
            this.properties = properties;
            this.executeAction = executeAction;
        }

        public void stop() {
            cancel(true);
        }

        @Override
        protected Object doInBackground() {
            runThread = Thread.currentThread();
            executeAction.setEnabled(false);
            try {
                GroovyShell groovyShell = new GroovyShell();
                for(Map.Entry<String,Object> variable : variables.entrySet()) {
                    groovyShell.setVariable("grv_"+variable.getKey(), variable.getValue());
                }
                for(Map.Entry<String,Object> property : properties.entrySet()) {
                    groovyShell.setProperty(property.getKey(), property.getValue());
                }
                groovyShell.evaluate(script);
            } catch (Exception e) {
                LOGGER.error(I18N.tr("Cannot execute the script"), e);
            } finally {
                executeAction.setEnabled(true);
                for(Log4JOutputStream logger : loggers) {
                    try {
                        logger.flush();
                    } catch (IOException e) {
                        LOGGER.error(I18N.tr("Cannot display the output of the console"), e);
                    }
                }
            }
            return null;
        }
    }
}
