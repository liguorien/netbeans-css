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
 * CSSRuleNode.java
 *
 * Created on February 21, 2006, 11:02 PM
 */

package com.liguorien.csscompletion;

import com.liguorien.csscompletion.actions.CreateDeclarationAction;
import com.liguorien.csscompletion.actions.OpenAction;
import com.liguorien.csscompletion.actions.RenameAction;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import javax.swing.Action;
import javax.swing.JEditorPane;
import org.openide.ErrorManager;
import org.openide.cookies.EditorCookie;
import org.openide.filesystems.FileObject;
import org.openide.loaders.DataObject;
import org.openide.loaders.DataObjectNotFoundException;
import org.openide.nodes.AbstractNode;
import org.openide.nodes.Children;
import org.openide.nodes.Node;

/**
 * Represent a CSSRulePart in the TreeView
 * @author Nicolas Désy
 */
public final class CSSRuleNode extends AbstractNode {
    
    private final static String ICON_BASE = "com/liguorien/csscompletion/resources/";
    private final static String ICON_ID = ICON_BASE + "id.png";
    private final static String ICON_ID_OFF = ICON_BASE + "id-off.png";
    private final static String ICON_CLASS = ICON_BASE + "class.png";
    private final static String ICON_CLASS_OFF = ICON_BASE + "class-off.png";
    private final static String ICON_ELEMENT = ICON_BASE + "element.png";
    private final static String ICON_ELEMENT_OFF = ICON_BASE + "element-off.png";
    private final static String ICON_PSEUDO_CLASS = ICON_BASE + "pseudo-class.png";
    private final static String ICON_PSEUDO_CLASS_OFF = ICON_BASE + "pseudo-class-off.png";
    
    private CSSRulePart _part;
    
    /**
     * Constructor
     * @param part The rule part that this node represents
     * @param c The node's children
     */
    public CSSRuleNode(CSSRulePart part, Children c){
        super(c);
        _part = part;
        _part.setNode(this);
        setName(_part.getName());
        init();
        
        if(c != Children.LEAF){
            final RuleChildren children = (RuleChildren)getChildren();
            children.setChildren(part.getChildren());
            children.refreshChildren(CSSExplorer.getInstance().getComparator());
        }
    }
    
    /**
     * Return the rule part attached to this node
     * @return the rule part
     */
    public CSSRulePart getRulePart(){
        return _part;
    }
    
    
    /**
     * Refresh node's children
     * @param comp The comparator used to sort the children
     */
    public void refresh(Comparator comp){
        
        init();
        
        if(getChildren() == Children.LEAF && _part.getChildren().size() > 0){
            final RuleChildren children = new RuleChildren();
            setChildren(children);
            children.setChildren(_part.getChildren());
            children.refreshChildren(comp);
        }
        
        else if(_part.getChildren().size() == 0){
            setChildren(Children.LEAF);
        }
        
        else{
            final RuleChildren children = (RuleChildren)getChildren();
            children.setChildren(_part.getChildren());
            children.refreshChildren(comp);
        }
    }
    
    
    /**
     * Change the name of the node
     * @param newName The new name
     */
    public void changeName(String newName){
        setName(newName);
        _part.setName(newName);
    }
    
    
    /**
     * Return a String representation of the node
     * @return a String representation of the node
     */
    public String toString(){
        return getName();
    }
    
    
    /**
     * Initialize the node
     */
    public void init(){
        
        switch(_part.getType()){
            case CSSRule.CLASS :
                setIconBaseWithExtension(
                        _part.isFinalDeclaration() ? ICON_CLASS : ICON_CLASS_OFF);
                break;
            case CSSRule.ID :
                setIconBaseWithExtension(
                        _part.isFinalDeclaration() ? ICON_ID : ICON_ID_OFF);
                break;
            case CSSRule.ELEMENT :
                setIconBaseWithExtension(
                        _part.isFinalDeclaration() ? ICON_ELEMENT : ICON_ELEMENT_OFF);
                break;
            case CSSRule.PSEUDO_CLASS :
                setIconBaseWithExtension(
                        _part.isFinalDeclaration() ? ICON_PSEUDO_CLASS : ICON_PSEUDO_CLASS_OFF);
                break;
        }
    }
    
    
    /**
     * Return a short description of the node
     * @return a short description of the node
     */
    public String getShortDescription(){
        return _part.getRule().getDeclaration().toString();
    }
    
    
    /**
     * Create a CSS declaration that fit with this node
     * @return A CSS declaration
     */
    public String getDeclaration(){
        
        final StringBuffer buffer = new StringBuffer();
        final List ruleParts = new ArrayList();
        
        ruleParts.add(this);
        
        Node node = getParentNode();
        while(node != null){
            ruleParts.add(node);
            node = node.getParentNode();
        }
        
        int i = ruleParts.size()-1;
        while(--i > -1) {
            final CSSRuleNode part = (CSSRuleNode)ruleParts.get(i);
            switch(part.getType()){
                case CSSRule.ID :
                    buffer.append(" #");
                    break;
                case CSSRule.CLASS :
                    buffer.append(" .");
                    break;
                case CSSRule.PSEUDO_CLASS :
                    buffer.append(':');
                    break;
                case CSSRule.ELEMENT :
                    buffer.append(' ');
                    break;
            }
            buffer.append(part.getName());
        }
        return buffer.toString();
    }
    
    
    /**
     * Setter for property name.
     * @param name New value of property name.
     */
    public void setName(String name) {
        super.setName(name);
        setDisplayName(name);
    }
    
    
    /**
     * Getter for property lineNumber.
     * @return Value of property lineNumber.
     */
    public int getLineNumber() {
        return _part.getLineNumber();
    }
    
    /**
     * Setter for property lineNumber.
     * @param lineNumber New value of property lineNumber.
     */
    public void setLineNumber(int lineNumber) {
        _part.setLineNumber(lineNumber);
    }
    
    
    /**
     * Getter for property file.
     * @return Value of property file.
     */
    public FileObject getFile() {
        return _part.getFile();
    }
    
    /**
     * Setter for property file.
     * @param file New value of property file.
     */
    public void setFile(FileObject file) {
        _part.setFile(file);
    }
    
    /**
     * Getter for property ruleLeaf.
     * @return Value of property ruleLeaf.
     */
    public boolean isRuleLeaf() {
        return _part.isFinalDeclaration();
    }
    
    /**
     * Setter for property ruleLeaf.
     * @param ruleLeaf New value of property ruleLeaf.
     */
    public void setRuleLeaf(boolean ruleLeaf) {
        _part.setFinalDeclaration(ruleLeaf);
    }
    
    /**
     * Getter for property type.
     * @return Value of property type.
     */
    public int getType() {
        return _part.getType();
    }
    
    /**
     * Setter for property type.
     * @param type New value of property type.
     */
    public void setType(int type) {
        _part.setType(type);
    }
    
    
    /**
     * Return the actions for this node
     * @param context whether to find actions for context meaning or for the node itself
     * @return a list of actions
     */
    public Action[] getActions(boolean context) {
        
        try {            
            final DataObject data = DataObject.find(_part.getFile());
            final EditorCookie ec = (EditorCookie)data.getCookie(EditorCookie.class);
            if(ec != null){            
                final JEditorPane[] panes = ec.getOpenedPanes();            
                if(panes != null && panes.length > 0){
                    if(_part.isFinalDeclaration()){
                        return new Action[]{new OpenAction(this), null, new RenameAction(this)};
                    }
                    return new Action[]{new CreateDeclarationAction(this), null, new RenameAction(this)};
                }
            }
        } catch (DataObjectNotFoundException ex) {
            ErrorManager.getDefault().notify(ex);
        }
        
        if(_part.isFinalDeclaration()){
            return new Action[]{new OpenAction(this)};
        }
        
        return new Action[]{new CreateDeclarationAction(this)};
    }
    
    /**
     * Class used to represent node's children
     */
    public final static class RuleChildren extends Children.Keys {
        
        private List/*<CSSRulePart>*/ _parts = new ArrayList/*<CSSRulePart>*/();
        
        public RuleChildren(){
            super();
        }
        
        /**
         * Add a child
         * @param part An instance of CSSRulePart
         */
        public void addChild(CSSRulePart part){
            _parts.add(part);
        }
        
        /**
         * Remove a child
         * @param part An instance of CSSRulePart that was previously added
         */
        public void removeChild(CSSRulePart part){
            _parts.remove(part);
        }
        
        /**
         * Replace the whole children list
         * @param children A List of children
         */
        public void setChildren(List children){
            _parts = children;
        }
        
        /**
         * Refresh and resort the children
         * @param c The comparator used to sort the children
         */
        public void refreshChildren(Comparator c){
            Collections.sort(_parts, c);
            setKeys(_parts);
        }
        
        protected void addNotify() {
            super.addNotify();
            refreshChildren(CSSExplorer.getInstance().getComparator());
        }
        
        protected void removeNotify() {
            setKeys(Collections.EMPTY_SET);
            super.removeNotify();
        }
        
        /**
         *
         * @param key
         * @return
         */
        protected Node[] createNodes(Object key) {
            final CSSRulePart part = (CSSRulePart)key;
            if(part.getChildren() == null){
                return new Node[]{new CSSRuleNode(part, Children.LEAF)};
            }
            return new Node[]{new CSSRuleNode(part, new RuleChildren())};
        }
        
        /**
         *
         * @return
         */
        public String toString(){
            return "RuleChildren : " + _parts;
        }
    }
    
    
    /**
     * Return the preferred action of the node
     * @return The preferred action of the node
     */
    public Action getPreferredAction() {
        if(isRuleLeaf()){
            return new OpenAction(this);
        }
        return null;
    }
}
