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


/*
 * CSSExplorer.java
 *
 * Created on February 22, 2006, 9:48 PM
 */

package com.liguorien.csscompletion;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.Collections;
import java.util.Comparator;
import java.util.Enumeration;
import java.util.List;
import javax.swing.AbstractListModel;
import javax.swing.Box;
import javax.swing.DefaultListCellRenderer;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JToggleButton;
import javax.swing.JToolBar;
import javax.swing.SwingConstants;
import org.openide.ErrorManager;
import org.openide.cookies.LineCookie;
import org.openide.explorer.ExplorerManager;
import org.openide.explorer.ExplorerUtils;
import org.openide.explorer.view.BeanTreeView;
import org.openide.loaders.DataObject;
import org.openide.loaders.DataObjectNotFoundException;
import org.openide.nodes.Node;
import org.openide.text.Line;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;
import org.openide.util.Utilities;

/**
 * @author Nicolas Désy
 */

public final class CSSExplorer extends JPanel
        implements ExplorerManager.Provider, Lookup.Provider {
    
    private ExplorerManager manager;
    private Lookup lookup;
    
    public final static int LIST_MODE = 0;
    public final static int TREE_MODE = 1;
    private int _viewMode = LIST_MODE;
    
    private final static Icon waitIcon = new ImageIcon( Utilities.loadImage(
            "com/liguorien/csscompletion/resources/wait.gif" ) ); //NOI18N
    private final static ImageIcon idIcon = new ImageIcon(Utilities.loadImage(
            "com/liguorien/csscompletion/resources/id.png")); //NOI18N
    private final static ImageIcon classIcon = new ImageIcon(Utilities.loadImage(
            "com/liguorien/csscompletion/resources/class.png")); //NOI18N
    private final static ImageIcon elementIcon = new ImageIcon(Utilities.loadImage(
            "com/liguorien/csscompletion/resources/element.png")); //NOI18N
    private final static ImageIcon pseudoClassIcon = new ImageIcon(Utilities.loadImage(
            "com/liguorien/csscompletion/resources/pseudo-class.png")); //NOI18N
    
    private CSSNavigatorPanel _navigatorPanel;
    private CSSTree _treeView;
    private CSSList _listView;
    private JPanel _contentPanel;
    private JPanel _showScanningPanel;
    private JLabel _showScanningLabel;
    private JComboBox _viewCombo;
    private JToolBar _toolBar;
    private JButton _expandBtn;
    private JButton _collapseBtn;
    private JLabel _sortLabel;
    private JToggleButton _sortNameBtn;
    private JToggleButton _sortTypeBtn;
    private JToggleButton _sortDeclarationBtn;
    private JToggleButton _currentSortBtn;
    
    private Comparator _currentComparator = CSSRule.NAME_COMPARATOR;
    private final static Object COMPARATOR_LOCK = new Object();    
    private final static CSSExplorer _instance = new CSSExplorer();
    
    public static CSSExplorer getInstance(){
        return _instance;
    }
    
    public static CSSExplorer getInstanceAndSetPanel(CSSNavigatorPanel panel){
        _instance.setNavigatorPanel(panel);
        return _instance;
    }
    
    private CSSExplorer() {
        super();
        
        manager = new ExplorerManager();
        
        setLayout(new BorderLayout());
        
        _viewCombo = new JComboBox(new ViewMode[]{
            new ViewMode(LIST_MODE, NbBundle.getMessage(CSSExplorer.class, "LBL_ListView")),
            new ViewMode(TREE_MODE, NbBundle.getMessage(CSSExplorer.class, "LBL_TreeView"))
        });
        
        _viewCombo.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                ViewMode mode = (ViewMode)_viewCombo.getSelectedItem();
                if(mode.value == TREE_MODE){
                    if(_currentComparator == CSSRule.NAME_COMPARATOR){
                        _currentComparator = CSSRulePart.NAME_COMPARATOR;
                    }else if(_currentComparator == CSSRule.TYPE_COMPARATOR){
                        _currentComparator = CSSRulePart.TYPE_COMPARATOR;
                    }else if(_currentComparator == CSSRule.DECLARATION_COMPARATOR){
                        _currentComparator = CSSRulePart.DECLARATION_COMPARATOR;
                    }
                    showTreeToolBar();
                }else{
                    if(_currentComparator == CSSRulePart.NAME_COMPARATOR){
                        _currentComparator = CSSRule.NAME_COMPARATOR;
                    }else if(_currentComparator == CSSRulePart.TYPE_COMPARATOR){
                        _currentComparator = CSSRule.TYPE_COMPARATOR;
                    }else if(_currentComparator == CSSRulePart.DECLARATION_COMPARATOR){
                        _currentComparator = CSSRule.DECLARATION_COMPARATOR;
                    }
                    showListToolBar();
                }
                _viewMode = mode.value;
                _navigatorPanel.revalidate();
            }
        });
        
        _sortLabel = new JLabel(NbBundle.getMessage(CSSExplorer.class, "LBL_SortBy"));
        _sortLabel.setVerticalAlignment(JLabel.CENTER);
        
        _sortNameBtn = _currentSortBtn = createToggle(new ImageIcon(
                Utilities.loadImage("com/liguorien/csscompletion/resources/sort-name.png")), true); //NOI18N
        _sortNameBtn.setToolTipText(NbBundle.getMessage(CSSExplorer.class, "LBL_SortByName"));
        _sortNameBtn.addActionListener(new SortListener(_sortNameBtn, new Comparator[]{
            CSSRule.NAME_COMPARATOR, CSSRulePart.NAME_COMPARATOR
        }));
        
        
        _sortTypeBtn = createToggle(new ImageIcon(
                Utilities.loadImage("com/liguorien/csscompletion/resources/sort-type.png")), false); //NOI18N
        _sortTypeBtn.setToolTipText(
                NbBundle.getMessage(CSSExplorer.class, "LBL_SortByType"));
        _sortTypeBtn.addActionListener(new SortListener(_sortTypeBtn, new Comparator[]{
            CSSRule.TYPE_COMPARATOR, CSSRulePart.TYPE_COMPARATOR
        }));
        
        
        _sortDeclarationBtn = createToggle(new ImageIcon(
                Utilities.loadImage("com/liguorien/csscompletion/resources/sort-declaration.png")), false); //NOI18N
        _sortDeclarationBtn.setToolTipText(
                NbBundle.getMessage(CSSExplorer.class, "LBL_SortByDeclaration"));
        _sortDeclarationBtn.addActionListener(new SortListener(_sortDeclarationBtn, new Comparator[]{
            CSSRule.DECLARATION_COMPARATOR, CSSRulePart.DECLARATION_COMPARATOR
        }));
        
        _contentPanel = new JPanel();
        _contentPanel.setLayout(new BorderLayout());
        
        setBackground(Color.WHITE);
        _showScanningPanel = new JPanel();
        _showScanningPanel.setBackground(Color.WHITE);
        _showScanningPanel.setLayout(new BorderLayout());
        _showScanningLabel = new JLabel();
        _showScanningLabel.setIcon(waitIcon);
        _showScanningLabel.setHorizontalAlignment(SwingConstants.LEFT);
        _showScanningLabel.setForeground(Color.BLACK);
        _showScanningLabel.setText(NbBundle.getMessage(CSSExplorer.class, "LBL_Scan"));
        _showScanningPanel.add(_showScanningLabel, BorderLayout.NORTH);
        
        
        
        _treeView = new CSSTree();
        _listView = new CSSList();
        
        _contentPanel.add(_treeView, BorderLayout.CENTER);
        
        _toolBar = new JToolBar(JToolBar.HORIZONTAL);
        _toolBar.setFloatable(false);
        
        _expandBtn = createButton(new ImageIcon(
                Utilities.loadImage("com/liguorien/csscompletion/resources/expand-all.png"))); //NOI18N
        _expandBtn.setToolTipText(NbBundle.getMessage(CSSExplorer.class, "LBL_ExpandAll"));
        _expandBtn.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                expandAll();
            }
        });
        
        _collapseBtn = createButton(new ImageIcon(
                Utilities.loadImage("com/liguorien/csscompletion/resources/collapse-all.png"))); //NOI18N
        _collapseBtn.setToolTipText(NbBundle.getMessage(CSSExplorer.class, "LBL_CollapseAll"));
        _collapseBtn.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                collapseAll();
            }
        });
        
        
        _toolBar.add(Box.createHorizontalStrut(5));
        _toolBar.add(_sortNameBtn);
        _toolBar.add(_sortTypeBtn);
        _toolBar.add(_sortDeclarationBtn);
        
        init();
    }
    
    public Comparator getComparator(){
        synchronized(COMPARATOR_LOCK){
            return _currentComparator;
        }
    }
    
    public void setComparator(Comparator c){
        synchronized(COMPARATOR_LOCK){
            _currentComparator = c;
        }
    }
    
    
    private JToggleButton createToggle(Icon icon, boolean isSelected) {
        // ensure small size, just for the icon
        JToggleButton result = new JToggleButton(icon, isSelected);
        Dimension size = new Dimension(icon.getIconWidth() + 6, icon.getIconHeight() + 4);
        result.setPreferredSize(size);
        result.setMargin(new Insets(2,3,2,3));
        return result;
    }
    
    private JButton createButton(Icon icon) {
        // ensure small size, just for the icon
        JButton result = new JButton(icon);
        Dimension size = new Dimension(icon.getIconWidth() + 6, icon.getIconHeight() + 4);
        result.setPreferredSize(size);
        result.setMargin(new Insets(2,3,2,3));
        return result;
    }
    
    public CSSNavigatorPanel getNavigatorPanel(){
        return _navigatorPanel;
    }
    
    private void showListToolBar(){
        _toolBar.removeAll();
        _toolBar.add(_sortNameBtn);
        _toolBar.add(_sortTypeBtn);
        _toolBar.add(_sortDeclarationBtn);
        _toolBar.revalidate();
        _toolBar.repaint();
    }
    
    private void showTreeToolBar(){
        _toolBar.removeAll();
        _toolBar.add(_sortNameBtn);
        _toolBar.add(_sortTypeBtn);
        _toolBar.add(_sortDeclarationBtn);
        _toolBar.addSeparator();
        _toolBar.add(_expandBtn);
        _toolBar.add(_collapseBtn);
        _toolBar.revalidate();
        _toolBar.repaint();
    }
    
    public void showScanningPanel() {
        _contentPanel.removeAll();
        _contentPanel.add(_showScanningPanel, BorderLayout.CENTER);
        _contentPanel.revalidate();
        _contentPanel.repaint();
    }
    
    public void showNothing() {
        removeAll();
        repaint();
    }
    
    
    public void init() {
        removeAll();
        add(_viewCombo, BorderLayout.NORTH);
        add(_contentPanel, BorderLayout.CENTER);
        add(_toolBar, BorderLayout.SOUTH);
        revalidate();
        repaint();
    }
    
    public int getViewMode(){
        return _viewMode;
    }
    
    public void setNavigatorPanel(CSSNavigatorPanel navigatorPanel){
        _navigatorPanel = navigatorPanel;
    }
    
    public void showTreetPanel() {        
        _contentPanel.removeAll();
        _contentPanel.add(_treeView, BorderLayout.CENTER);
        _contentPanel.revalidate();
        _contentPanel.repaint();
    }
    
    public void showListPanel(List/*<CSSRule>*/ rules) {       
        final CSSListModel model = (CSSListModel)_listView.getModel();
        Collections.sort(rules, _currentComparator);
        model.setRules(rules);
        
        _contentPanel.removeAll();
        _contentPanel.add(_listView, BorderLayout.CENTER);
        _contentPanel.revalidate();
        _contentPanel.repaint();
    }
    
    public void collapseAll(){
        final CSSRuleNode root = (CSSRuleNode)manager.getRootContext();
        final Enumeration e = root.getChildren().nodes();
        while(e.hasMoreElements()){
            _treeView.collapseNode((Node)e.nextElement());
        }
    }
    
    public void expandAll() {
        _treeView.expandAll();
    }
    
    public void expandNode(Node node, boolean recursive){
        _treeView.expandNode(node);
        if(recursive){
            final Node[] nodes = node.getChildren().getNodes();
            for (int i = 0; i < nodes.length; i++) {
                expandNode(nodes[i], true);
            }
        }
    }
    
    // ...method as before and getLookup
    public ExplorerManager getExplorerManager() {
        return manager;
    }
    public Lookup getLookup() {
        return lookup;
    }
    // ...methods as before, but replace componentActivated and
    // componentDeactivated with e.g.:
    public void addNotify() {
        super.addNotify();
        ExplorerUtils.activateActions(manager, true);
    }
    public void removeNotify() {
        ExplorerUtils.activateActions(manager, false);
        super.removeNotify();
    }
    
    /**
     * used by the combobox
     */
    private static class ViewMode {
        int value;
        String label;
        ViewMode(int value, String label){
            this.value = value;
            this.label = label;
        }
        public String toString(){
            return label;
        }
    }
    
    
    private static class CSSTree extends BeanTreeView{
        public CSSTree(){
            super();
            tree.setScrollsOnExpand(false);
            setRootVisible(false);
        }
    }
    
    
    private static class CSSList extends JScrollPane {
        private JList _list;
        public CSSList(){
            super();
            _list = new JList();
            _list.setModel(new CSSListModel());
            _list.setCellRenderer(new CSSListRenderer());
            _list.addMouseListener(new CSSListClickListener());
            setViewportView(_list);
        }
        public CSSListModel getModel(){
            return (CSSListModel)_list.getModel();
        }
    }
    
    private static class CSSListModel extends AbstractListModel {
        
        private List/*<CSSRule>*/ _rules = Collections.EMPTY_LIST;
        
        public CSSListModel(){
            super();
        }
        
        public int getSize() {
            return _rules.size();
        }
        
        public Object getElementAt(int index) {
            return _rules.get(index);
        }
        
        public void sort(Comparator c){
            Collections.sort(_rules, c);
            fireContentsChanged(this, 0, _rules.size());
        }
        
        public void setRules(List/*<CSSRule>*/ rules){
            _rules = rules;
            fireContentsChanged(this, 0, _rules.size());
        }
    }
    
    
    private static class CSSListRenderer extends DefaultListCellRenderer {
        
        public Component getListCellRendererComponent(JList list,
                Object value,
                int index,
                boolean isSelected,
                boolean hasFocus) {
            
            final JLabel label = (JLabel)super.getListCellRendererComponent(list, value, index, isSelected, hasFocus);
            final CSSRule item = (CSSRule) value;
            
            switch(item.getType()){
                case CSSRule.ID : label.setIcon(idIcon); break;
                case CSSRule.CLASS : label.setIcon(classIcon); break;
                case CSSRule.ELEMENT : label.setIcon(elementIcon); break;
                case CSSRule.PSEUDO_CLASS : label.setIcon(pseudoClassIcon); break;
                default : label.setIcon(null);
            }
            
            return label;
        }
    }
    
    
    private class SortListener implements ActionListener {
        
        private JToggleButton _btn;
        private Comparator[] _comparators; // { LIST_COMPARATOR , TREE_COMPARATOR }
        
        SortListener(JToggleButton btn, Comparator[] cs){
            _btn = btn;
            _comparators = cs;
        }
        
        public void actionPerformed(ActionEvent e) {
            if(_currentSortBtn != _btn){
                _currentSortBtn.getModel().setSelected(false);
                _btn.getModel().setSelected(true);
                if(_viewMode == TREE_MODE) {
                    _currentComparator = _comparators[TREE_MODE];
                    ((CSSRuleNode)manager.getRootContext()).refresh(_currentComparator);
                }else{
                    _currentComparator = _comparators[LIST_MODE];
                    _listView.getModel().sort(_currentComparator);
                }
                _currentSortBtn = _btn;
            }
        }
    }
    
    private static class CSSListClickListener extends MouseAdapter{
        public void mouseClicked(MouseEvent e){
            if(e.getClickCount() == 2){
                final JList list = (JList)e.getSource();
                final int index = list.locationToIndex(e.getPoint());
                final CSSRule item =
                        (CSSRule)list.getModel().getElementAt(index);
                list.ensureIndexIsVisible(index);
                
                try {
                    
                    final LineCookie lc = (LineCookie)DataObject.find(
                            item.getFile()).getCookie(LineCookie.class);
                    
                    if (lc != null){
                        lc.getLineSet().getCurrent(item.getLineNumber()).show(Line.SHOW_GOTO);
                    }
                    
                } catch (Exception ex) {
                    ErrorManager.getDefault().notify(ex);
                }
            }
        }
    }
}
