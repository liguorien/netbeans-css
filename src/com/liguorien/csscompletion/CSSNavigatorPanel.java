/*
 *                 Sun Public License Notice
 *
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 *
 * The Original Code is the CSSCompletion module. 
 * The Initial Developer of the Original Code is Nicolas Désy. 
 * Portions created by Nicolas Désy are Copyright (C) 2006.
 * All Rights Reserved.
 */


package com.liguorien.csscompletion;

import java.awt.EventQueue;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import javax.swing.ImageIcon;
import javax.swing.JComponent;
import javax.swing.JEditorPane;
import javax.swing.SwingUtilities;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.JTextComponent;
import javax.swing.text.StyledDocument;
import org.netbeans.editor.BaseDocument;
import org.netbeans.editor.Registry;
import org.netbeans.spi.navigator.NavigatorLookupHint;
import org.netbeans.spi.navigator.NavigatorPanel;
import org.openide.cookies.EditorCookie;
import org.openide.filesystems.FileObject;
import org.openide.loaders.DataObject;
import org.openide.nodes.Children;
import org.openide.util.Lookup;
import org.openide.util.LookupEvent;
import org.openide.util.LookupListener;
import org.openide.util.NbBundle;
import org.openide.util.RequestProcessor;
import org.openide.util.Utilities;
import org.openide.util.WeakListeners;

/**
 * @author Nicolas Désy
 */
public final class CSSNavigatorPanel implements
        NavigatorPanel, NavigatorLookupHint, ChangeListener {
    
    private CSSExplorer _explorer = CSSExplorer.getInstanceAndSetPanel(this);
    
    private boolean _dontRefresh = false;
    
    /** template for finding data in given context. */
    private static final Lookup.Template DATA_LOOKUP = new Lookup.Template(DataObject.class);
    /** current context to work on */
    private Lookup.Result curContext;
    /** listener to context changes */
    private LookupListener contextL;
    
    private CSSDocumentListener _listener = null;
    private DataObject _currentDataObject = null;
    /** public no arg constructor needed for system to instantiate provider well */
    public CSSNavigatorPanel() {
        Registry.addChangeListener(this);
    }
    
    public String getContentType() {
        return "text/css";
    }
    
    public String getDisplayHint() {
        return NbBundle.getMessage(CSSNavigatorPanel.class, "CNP_hint");
    }
    
    public String getDisplayName() {
        return NbBundle.getMessage(CSSNavigatorPanel.class, "CNP_label");
    }
    
    public void showNothing(){
        _currentDataObject = null;
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                _explorer.showNothing();
            }
        });
    }
    
    public void stateChanged(ChangeEvent e) {
        if(_listener == null && _currentDataObject != null){
            final EditorCookie ec =
                    (EditorCookie)_currentDataObject.getCookie(EditorCookie.class);
            
            if(ec != null){
                final BaseDocument active = Registry.getMostActiveDocument();
                final StyledDocument doc = ec.getDocument();
                if(active != null && doc != null && active.equals(doc)){
                    _listener = new CSSDocumentListener(doc);
                    RequestProcessor.getDefault().post(_listener);
                }
            }
        }
    }
    
    public JComponent getComponent() {
        return _explorer;
    }
    
    public void panelActivated(Lookup context) {
        
        if(_explorer != null){
            _explorer.init();
        }
        
        curContext = context.lookup(DATA_LOOKUP);
        curContext.addLookupListener(getContextListener());
        // get actual data and recompute content
        Collection data = curContext.allInstances();
        setNewContent(data);
    }
    
    public void documentChanged(){
        if(_listener != null){
            _listener.documentChanged();
        }
    }
    
    public boolean refresh(){       
        if(!_isParsing){
            refreshData(_currentDataObject);
            return true;
        }
        return false;
    }
    
    private boolean _needToShowTree;
    
    private boolean _isParsing = false;
    
    public boolean isParsing(){
        return _isParsing;
    }
    
    public void revalidate(){
        _isParsing = true;
        _needToShowTree = (_explorer.getViewMode() == CSSExplorer.TREE_MODE);
        setNewContent(Arrays.asList(new Object[]{_currentDataObject}));
    }
    
    public void panelDeactivated() {       
        if(_explorer != null){
            _explorer.showNothing();
        }
        
        curContext.removeLookupListener(getContextListener());
        curContext = null;
        desactiveListener();
    }
    
   
    
    public Lookup getLookup() {
        // go with default activated Node strategy
        return null;
    }
    
     public void dontRefresh(){
        _dontRefresh = true;
    }
    
    /************* non - public part ************/
    
     
      private void desactiveListener(){
        if(_listener != null){
            _listener.desactivate();
            _listener = null;
        }
    }
     
    private EditorCookie _editCookie = null;
    
    private void setNewContent(Collection newData) {
        setNewContent(newData, true);
    }
    
    
    private void refreshData(final DataObject data) {
        
        if(!_dontRefresh){
            _isParsing = true;
            RequestProcessor.getDefault().post(new Runnable() {
                public void run() {
                    
                    if(_explorer.getViewMode() == CSSExplorer.LIST_MODE){
                        
                        final List/*<CSSRule>*/ rules = CSSRulesResolver.newInstance().resolve(data);
                        
                        _isParsing = false;
                        
                        EventQueue.invokeLater(new Runnable() {
                            public void run() {
                                _explorer.showListPanel(rules);
                            }
                        });
                        
                    } else if(_explorer.getViewMode() == CSSExplorer.TREE_MODE){
                        
                        final CSSRuleNode rootNode = (CSSRuleNode)_explorer.getExplorerManager().getRootContext();
                        final CSSRulePart rootPart = rootNode.getRulePart();
                        rootPart.resetPresenceConfirmation();
                        CSSRulesResolver.buildRulePartTree(data, rootPart);
                        rootPart.removeChildWithoutConfirmation();
                        _isParsing = false;
                    }                   
                }
            });
        }else{
            _dontRefresh = false;
        }
    }
    
   
    
    private void setNewContent(Collection newData, final boolean showScanning) {
        
        _isParsing = true;
        
        desactiveListener();
        
        // wait some time before showing the "Scanning in progress..." message (if necessary)
        final RequestProcessor.Task showScanningTask = RequestProcessor.getDefault().post(new Runnable() {
            public void run() {
                if(_explorer != null){
                    EventQueue.invokeLater(new Runnable() {
                        public void run() {
                            _explorer.showScanningPanel();
                        }
                    });
                }
            }
        }, 50);
        
        final DataObject data = _currentDataObject = (DataObject) newData.iterator().next();
        final FileObject file = data.getPrimaryFile();
        final EditorCookie ec = (EditorCookie)data.getCookie(EditorCookie.class);
        
        if(ec != null){
            final JEditorPane[] openedPanes = ec.getOpenedPanes();
            if(openedPanes != null && openedPanes.length > 0){
                _listener = new CSSDocumentListener(ec.getDocument());
                RequestProcessor.getDefault().post(_listener);
            }
        }
        
        RequestProcessor.getDefault().post(new Runnable() {
            public void run() {
                
                if(_explorer.getViewMode() == CSSExplorer.LIST_MODE){
                    
                    final List/*<CSSRule>*/ rules = CSSRulesResolver.newInstance().resolve(data);
                    
                    if(!showScanningTask.isFinished()){
                        showScanningTask.cancel();
                    }
                    
                    EventQueue.invokeLater(new Runnable() {
                        public void run() {
                            _explorer.showListPanel(rules);
                            _isParsing = false;
                        }
                    });
                    
                } else if(_explorer.getViewMode() == CSSExplorer.TREE_MODE){
                    
                    // compute tree content
                    
                    final CSSRulePart rootPart = new CSSRulePart();
                    rootPart.setName("");
                    
                    CSSRulesResolver.buildRulePartTree(data, rootPart);
                    
                    final CSSRuleNode root = new CSSRuleNode(rootPart, (rootPart.getChildren().size() == 0
                            ? Children.LEAF : new CSSRuleNode.RuleChildren()));
                    
                    final boolean scanningShowed = showScanningTask.isFinished();
                    if(!scanningShowed){
                        showScanningTask.cancel();
                    }
                    
                    _explorer.getExplorerManager().setRootContext(root);
                    
                    if(scanningShowed || _needToShowTree){
                        _needToShowTree = false;
                        EventQueue.invokeLater(new Runnable() {
                            public void run() {
                                _explorer.showTreetPanel();
                            }
                        });
                    }
                    
                    _isParsing = false;
                }
            }
        });
    }
    
    /** Accessor for listener to context */
    private LookupListener getContextListener() {
        if (contextL == null) {
            contextL = new ContextListener();
        }
        return contextL;
    }
    
    /** Listens to changes of context and triggers proper action */
    private class ContextListener implements LookupListener {
        
        public void resultChanged(LookupEvent ev) {
            Collection data = ((Lookup.Result)ev.getSource()).allInstances();
            setNewContent(data);
        }
        
    } // end of ContextListener
    
    
    
    private final class CSSDocumentListener
            implements DocumentListener, ChangeListener, Runnable {
        
        private long _lastTimeChanged = System.currentTimeMillis();
        private long _lastTimeParsed = System.currentTimeMillis();
        private boolean _docChanged = false;
        private boolean _isActivated = true;
        private JTextComponent _active = null;
        private StyledDocument _doc;
        
        public CSSDocumentListener(StyledDocument doc){
            _doc = doc;         
            _doc.addDocumentListener(WeakListeners.document(this, _doc));
            _active = Registry.getMostActiveComponent();
            Registry.addChangeListener(this);
        }
        
        public synchronized void desactivate(){
            _isActivated = false;
            Registry.removeChangeListener(this);
            _doc.removeDocumentListener(this);
        }
        
        public synchronized boolean isActivated(){
            return _isActivated;
        }
        
        public void insertUpdate(DocumentEvent e) {
            documentChanged();
        }
        
        public void removeUpdate(DocumentEvent e) {            
            documentChanged();
        }
        
        public void changedUpdate(DocumentEvent e) {
            documentChanged();
        }
        
        public synchronized void documentChanged(){
            _docChanged = true;
            _lastTimeChanged = System.currentTimeMillis();
        }
        
        public synchronized boolean isDocumentChanged(){
            return _docChanged;
        }
        
        public synchronized void documentUnchanged(){
            _docChanged = false;
        }
        
        public void run() {
            
            while(isActivated()){
              
                if(isDocumentChanged() &&
                        System.currentTimeMillis()-_lastTimeParsed > 3000 &&
                        System.currentTimeMillis()-_lastTimeChanged > 3000){                  
                    _lastTimeParsed = System.currentTimeMillis();
                    if(refresh()){
                        documentUnchanged();
                    }                    
                }
                try {
                    Thread.sleep(500);
                } catch (InterruptedException ex) {
                    desactivate();
                }
            }
        }
        
        public void stateChanged(ChangeEvent e) {
            if(Registry.getMostActiveComponent() != _active){
                desactivate();
            }
        }
    }
}