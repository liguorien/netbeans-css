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
 * CSSRulePart.java
 *
 * Created on March 1, 2006, 10:11 PM
 */

package com.liguorien.csscompletion;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import org.openide.filesystems.FileObject;

/**
 * @author Nicolas Désy
 */
public final class CSSRulePart {
    
    private FileObject _file;
    private int _lineNumber;
    private CSSRule _rule;
    private CSSRuleNode _node;
    private String _name;
    private boolean _finalDeclaration;
    private int _type = 666;
    private List/*<CSSRulePart>*/ _children = null;
    private Set/*<CSSRule>*/ _ruleDependencies = new HashSet/*<CSSRule>*/();
    private boolean _presenceConfirmed = true;
    private CSSRulePart _parent = null;
    
    /** Creates a new instance of CSSRulePart */
    public CSSRulePart() {
        
    }
    
    /**
     * Getter for property file.
     * @return Value of property file.
     */
    public FileObject getFile() {
        return _file;
    }
    
    public CSSRulePart getFirstChild(){
        if(_children != null && _children.size() > 0){
            return (CSSRulePart)_children.get(0);
        }
        return null;
    }
    
    /**
     * Setter for property file.
     * @param file New value of property file.
     */
    public void setFile(FileObject file) {
        _file = file;
    }
    
    /**
     * Getter for property lineNumber.
     * @return Value of property lineNumber.
     */
    public int getLineNumber() {
        return _lineNumber;
    }
    
    /**
     * Setter for property lineNumber.
     * @param lineNumber New value of property lineNumber.
     */
    public void setLineNumber(int lineNumber) {
        _lineNumber = lineNumber;
    }
    
    /**
     * Getter for property rule.
     * @return Value of property rule.
     */
    public CSSRule getRule() {
        return _rule;
    }
    
    /**
     * Setter for property rule.
     * @param rule New value of property rule.
     */
    public void setRule(CSSRule rule) {
        _rule = rule;
    }
    
    public CSSRuleNode getNode() {
        return _node;
    }
    
    public void setNode(CSSRuleNode node) {
        _node = node;
    }
    
    public String getDeclaration(){
        
        final StringBuffer buffer = new StringBuffer();
        
        if(_spaceBefore){
            buffer.append(' ');
        }
        
        switch(_type){
            case CSSRule.ID : buffer.append('#'); break;
            case CSSRule.CLASS : buffer.append('.'); break;
            case CSSRule.PSEUDO_CLASS : buffer.append(':'); break;
        }
        
        return buffer.append(_name).toString();
    }
    
    /**
     * Getter for property type.
     * @return Value of property type.
     */
    public int getType() {
        return _type;
    }
    
    /**
     * Setter for property type.
     * @param type New value of property type.
     */
    public void setType(int type) {
        _type = type;
    }
    
    /**
     * Getter for property name.
     * @return Value of property name.
     */
    public String getName() {
        return _name;
    }
    
    /**
     * Setter for property name.
     * @param name New value of property name.
     */
    public void setName(String name) {
        _name = name;
    }
    
    /**
     * Getter for property finalDeclaration.
     * @return Value of property finalDeclaration.
     */
    public boolean isFinalDeclaration() {
        return _finalDeclaration;
    }
    
    /**
     * Setter for property finalDeclaration.
     * @param finalDeclaration New value of property finalDeclaration.
     */
    public void setFinalDeclaration(boolean finalDeclaration) {
        _finalDeclaration = finalDeclaration;        
    }
    
    public void initNode(){
        if(_node != null){
            _node.init();
        }
    }
    
    public String toString(){
        return _name;
    }
    
    public boolean equals(Object obj){
        if(obj instanceof CSSRulePart){
            final CSSRulePart part = (CSSRulePart)obj;
            return (_type == part.getType() && _name.equals(part.getName()));
        }
        return false;
    }
    
    
    public boolean isSamePath(CSSRulePart part){
        
        CSSRulePart tParent = this;
        CSSRulePart pParent = part;
        
        while(tParent != null && pParent != null){
            if(!tParent.equals(pParent)){
                return false;
            }
            
            if(pParent.getType() == CSSRule.ID){
                return true;
            }
            
            tParent = tParent._parent;
            pParent = pParent._parent;
        }
        
        if(tParent == null && pParent != null) {          
            return (pParent._parent == null && "".equals(pParent._name));
        }
        if(pParent == null && tParent != null){
            return (tParent._parent == null && "".equals(tParent._name));
        }
        
        return true;
    }
    
    /**
     * Getter for property children.
     * @return Value of property children.
     */
    public List/*<CSSRulePart>*/ getChildren() {
        return _children;
    }
    
    /**
     * Setter for property children.
     * @param children New value of property children.
     */
    public void setChildren(List/*<CSSRulePart>*/ children) {
        _children = children;
    }
    
    public CSSRulePart getParent(){
        return _parent;
    }
    
    public void addChild(CSSRulePart part){
        part._parent = this;
        if(_children == null){
            _children = new ArrayList/*<CSSRulePart>*/();
        }        
        _children.add(part);
        if(_node != null){
            part.setCreateAtRuntime(true);
            CSSExplorer.getInstance().expandNode(_node, false);            
        }
    }
    
    public void refresh(){
        if(_node != null){
            _node.refresh(CSSExplorer.getInstance().getComparator());
        }
    }
    
    public void removeChild(CSSRulePart part){
        part._parent = null;
        if(_children != null){
            _children.remove(part);
        }
    }
    
    public CSSRulePart findChild(CSSRulePart part){
        if(_children == null) return null;
        final int size = _children.size();
        for (int i = 0; i < size; i++) {
            final CSSRulePart c = (CSSRulePart)_children.get(i);
            if(c.equals(part)){
                return c;
            }
        }
        return null;
    }
    
    
    public void addDependency(CSSRule rule){
        _ruleDependencies.add(rule);
    }
    
    public Iterator/*<CSSRule>*/ getDependencies(){
        return _ruleDependencies.iterator();
    }
    
    public int getDependenciesCount(){
        return _ruleDependencies.size();
    }
    
    
    public boolean hasConfirmed(){
        return _presenceConfirmed;
    }
    
    public void confirmPresence(){
        _presenceConfirmed = true;
    }
    
    public void resetPresenceConfirmation(){
        _ruleDependencies.clear();
        _finalDeclaration = false;
        _presenceConfirmed = false;
        if(_children != null){
            final Iterator it = _children.iterator();
            while(it.hasNext()){
                ((CSSRulePart)it.next()).resetPresenceConfirmation();
            }
        }
    }
    
    public void removeChildWithoutConfirmation(){
        
        boolean childRemoved = false;
        
        if(_children != null){
            int i = _children.size();
            while(--i > -1){
                final CSSRulePart child = (CSSRulePart) _children.get(i);
                if(child.hasConfirmed()){
                    child.removeChildWithoutConfirmation();
                }else{
                    removeChild(child);
                    childRemoved = true;
                }
            }
        }
        
        if(childRemoved){
            refresh();
        }
    }
    
    
    public final static Comparator NAME_COMPARATOR = new Comparator(){
        public int compare(Object obj1, Object obj2) {
            return obj1.toString().compareTo(obj2.toString());
        }
    };
    
    
    
    public final static Comparator TYPE_COMPARATOR = new Comparator(){
        public int compare(Object obj1, Object obj2) {
            final CSSRulePart item1 = (CSSRulePart)obj1;
            final CSSRulePart item2 = (CSSRulePart)obj2;
            if(item1.getType() > item2.getType()) return -1;
            if(item1.getType() == item2.getType()) return 0;
            return 1;
        }
    };
    
    public final static Comparator DECLARATION_COMPARATOR = new Comparator(){
        public int compare(Object obj1, Object obj2) {
            final CSSRulePart item1 = (CSSRulePart)obj1;
            final CSSRulePart item2 = (CSSRulePart)obj2;
            if(item1.getLineNumber() < item2.getLineNumber()) return -1;
            if(item1.getLineNumber() == item2.getLineNumber()) return NAME_COMPARATOR.compare(obj1, obj2);
            return 1;
        }
    };
    
    /**
     * Holds value of property _spaceBefore.
     */
    private boolean _spaceBefore;
    
    /**
     * Getter for property spaceBefore.
     * @return Value of property spaceBefore.
     */
    public boolean isSpaceBefore() {
        return _spaceBefore;
    }
    
    /**
     * Setter for property spaceBefore.
     * @param spaceBefore New value of property spaceBefore.
     */
    public void setSpaceBefore(boolean spaceBefore) {
        _spaceBefore = spaceBefore;
    }

    /**
     * Holds value of property _createAtRuntime.
     */
    private boolean _createAtRuntime;

    /**
     * Getter for property createAtRuntime.
     * @return Value of property createAtRuntime.
     */
    public boolean isCreateAtRuntime() {
        return _createAtRuntime;
    }

    /**
     * Setter for property createAtRuntime.
     * @param createAtRuntime New value of property createAtRuntime.
     */
    public void setCreateAtRuntime(boolean createAtRuntime) {
        _createAtRuntime = createAtRuntime;
    }
}
