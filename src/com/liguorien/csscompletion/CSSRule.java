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
 * CSSRule.java
 *
 * Created on February 20, 2006, 7:59 PM
 */

package com.liguorien.csscompletion;

import java.util.Comparator;
import java.util.Iterator;
import java.util.regex.Pattern;
import org.openide.filesystems.FileObject;

/**
 * @author Nicolas Désy
 */
public final class CSSRule {
    
    public final static int ID = 1;
    public final static int CLASS = 2;
    public final static int ELEMENT = 3;
    public final static int PSEUDO_CLASS = 4;
    
    private final static Pattern _whitePattern = Pattern.compile("\\s+");
    
    private int _type;
    private String _label;
    private int _lineNumber;
    private FileObject _file;
    private boolean _leaf;
    private String _declaration;
    private String _sortLabel;
    
    /**
     * Creates a new instance of CSSRule
     */
    public CSSRule() {
    }
    
    public Iterator/*<CSSRulePart>*/ getParts(){
        return new CSSRulePartIterator(_whitePattern.split(_label));
    }
    
    public Iterator/*<CSSRulePart>*/ getAllParts(){
        return new CSSAllRulePartIterator(_whitePattern.split(_label));
    }
    
    public int getType(){
        return _type;
    }
    
    public void setType(int type){
        _type = type;
    }
    
    public String getTypeString(){
        switch(_type){
            case ID : return "ID";
            case ELEMENT : return "ELEMENT";
            case CLASS : return "CLASS";
            case PSEUDO_CLASS : return "PSEUDO_CLASS";
        }
        return "";
    }
    
    /**
     * Getter for property label.
     * @return Value of property label.
     */
    public String getLabel() {
        return _label;
    }
    
    /**
     * Setter for property label.
     * @param label New value of property label.
     */
    public void setLabel(String label) {
        _declaration = label;
        _label = label.trim();
        _sortLabel = null;
    }
    
    public void resolveType(){
        
        final String[] tokens = _whitePattern.split(_label);
        final String token = tokens[tokens.length-1];
        
        switch(token.charAt(0)){
            case '#' : _type = _getType(token, ID); break;
            case '.' : _type = _getType(token, CLASS); break;
            default : _type = _getType(token, ELEMENT); break;
        }
    }
    
    private int _getType(String part, int initType){
        final int colonOffset = part.indexOf(":", 1);
        final int dotOffset = part.indexOf(".", 1);
        final int idOffset = part.indexOf("#", 1);
        
        if(colonOffset > 0 && colonOffset > dotOffset && colonOffset > idOffset){
            return PSEUDO_CLASS;
        }
        
        if(dotOffset > 0 && dotOffset > colonOffset && dotOffset > idOffset){
            return CLASS;
        }
        
        if(idOffset > 0 && idOffset > colonOffset && idOffset > dotOffset){
            return ID;
        }
        
        return initType;
    }
    
    public String getDeclaration(){
        return _declaration;
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
    
    public String toString() {
        return _label;
    }
    
    public String rename(CSSRulePart replacePart, String newName){
        final CSSRulePart tree = buildSingleTree();
        
        if(tree == null) return "";
        
        final StringBuffer buffer = new StringBuffer();
        CSSRulePart part = null;
        
        if(tree.isSamePath(replacePart)){
            tree.setName(newName);
        }
        
        buffer.append(tree.getDeclaration());
        part = tree;
        
        while((part = part.getFirstChild()) != null){
            if(part.isSamePath(replacePart)){
                part.setName(newName);
            }
            
            buffer.append(part.getDeclaration());
        }
        
        return buffer.toString();
    }
    
    
    public CSSRulePart buildSingleTree(){
        
        final Iterator/*<CSSRulePart>*/ parts = getAllParts();
        CSSRulePart rootPart = null;
        CSSRulePart currentPart = null;
        CSSRulePart tempPart = null;
        
        if(parts.hasNext()){
            currentPart = rootPart = (CSSRulePart)parts.next();
            while(parts.hasNext()){
                currentPart.addChild(tempPart = (CSSRulePart)parts.next());
                currentPart = tempPart;
            }
            return rootPart;
        }
        
        return null;
    }
    
    
    
    /**
     * Getter for property file.
     * @return Value of property file.
     */
    public FileObject getFile() {
        return _file;
    }
    
    /**
     * Setter for property file.
     * @param file New value of property file.
     */
    public void setFile(FileObject file) {
        _file = file;
    }
    
    
    /**
     * Getter for property leaf.
     * @return Value of property leaf.
     */
    public boolean isLeaf() {
        return _leaf;
    }
    
    /**
     * Setter for property leaf.
     * @param leaf New value of property leaf.
     */
    public void setLeaf(boolean leaf) {
        _leaf = leaf;
    }
    
    
    public String getSortLabel(){
        if(_sortLabel == null){
            if(_label.length() == 0) return "";
            switch(_label.charAt(0)){
                case '.' :
                case '#' :
                    _sortLabel = _label.substring(1);
                    break;
                default:
                    _sortLabel = _label;
                    break;
            }
        }
        return _sortLabel;
    }
    
    public int hashCode(){
        return _label.hashCode() + _type;
    }
    
    public boolean equals(Object obj){
        if(obj instanceof CSSRule){
            final CSSRule rule = (CSSRule)obj;
            return (_type == rule.getType() && _label.equals(rule.getLabel()));
        }
        return false;
    }
    
    /**
     * Iterator that iterates over the rule parts of the entire rule declaration
     */
    private final class CSSRulePartIterator implements Iterator {
        
        private final static int INIT = 0;
        private final static int WAS_EL = 1;
        private final static int WAS_ID = 2;
        private final static int WAS_CLASS = 3;
        
        private String[] _rulesNames;
        private int _offset = 0;
        private int _extra = 0;
        private int _state = INIT;
        private int _dotOffset = -1;
        private int _colonOffset = -1;
        private boolean _hasNext = false;
        private boolean _skipMultipleId;
        
        CSSRulePartIterator(String[] rulesNames){
            _rulesNames = rulesNames;
            int i = rulesNames.length;
            while(--i > -1){
                if(rulesNames[i].indexOf("#") > -1){
                    _offset = i;
                    break;
                }
            }
        }
        
        public boolean hasNext() {
            return (_offset < _rulesNames.length) || _hasNext;
        }
        
        public Object next() {
            
            _hasNext = false;
            
            final CSSRulePart part = new CSSRulePart();
            
            part.setFile(getFile());
            part.setLineNumber(getLineNumber());
            part.setRule(CSSRule.this);
            
            
            part.setSpaceBefore(_state == INIT && _offset > 0);
            
            switch(_state){
                
                case INIT :
                    
                    final String ruleName = _rulesNames[_offset++];
                    final int idOffset = ruleName.indexOf("#");
                    
                    // if the part is an ID
                    if(idOffset > -1){
                        
                        part.setType(ID);
                        
                        
                        // TODO: iterate the ruleName once to find both index (optimisation)
                        _dotOffset = ruleName.indexOf(".");
                        _colonOffset = ruleName.indexOf(":");
                        
                        // check if there is a class attached to the ID
                        if(_dotOffset > -1 && _dotOffset > _colonOffset){
                            //    _colonOffset = -1;
                            part.setName(ruleName.substring(idOffset + 1, _dotOffset));
                            _state = WAS_ID;
                            _offset--;
                        }
                        
                        else if(_colonOffset > -1){
                            //    _dotOffset = -1;
                            part.setName(ruleName.substring(idOffset + 1, _colonOffset));
                            _state = WAS_ID;
                            _offset--;
                        }
                        
                        else{
                            part.setName(ruleName.substring(idOffset + 1));
                            _state = INIT;
                            part.setFinalDeclaration(!hasNext());
                        }
                    }
                    // part is not an ID
                    else{
                        
                        _dotOffset = ruleName.indexOf(".");
                        _colonOffset = ruleName.indexOf(":");
                        
                        //if this is only a class
                        if(_dotOffset == 0){
                            
                            // class followed by a pseudo class
                            if(_colonOffset > -1){
                                _dotOffset = -1;
                                part.setName(ruleName.substring(1, _colonOffset));
                                part.setType(CLASS);
                                _state = WAS_CLASS;
                                _offset--;
                            }
                            // only a class
                            else{
                                part.setName(ruleName.substring(1));
                                part.setType(CLASS);
                                part.setFinalDeclaration(!hasNext());
                            }
                            
                        }
                        // if this is an element followed by a class
                        else if(_dotOffset > 0 && (_dotOffset < _colonOffset || _colonOffset == -1)){
                            _state = WAS_EL;
                            _offset--;
                            part.setName(ruleName.substring(0, _dotOffset));
                            part.setType(ELEMENT);
                        }
                        // if this is an element followed by a pseudo class
                        else if(_colonOffset > -1){
                            _state = WAS_EL;
                            _offset--;
                            part.setName(ruleName.substring(0, _colonOffset));
                            part.setType(ELEMENT);
                        }
                        // this is only an element
                        else{
                            part.setName(ruleName);
                            part.setType(ELEMENT);
                            part.setFinalDeclaration(!hasNext());
                        }
                    }
                    
                    break;
                    
                case WAS_EL :
                case WAS_ID :
                case WAS_CLASS :
                
                    // if this is a class
                    if(_dotOffset > -1 && (_dotOffset < _colonOffset || _colonOffset == -1)){
                        if(_colonOffset == -1){
                            part.setName(_rulesNames[_offset].substring(_dotOffset+1));
                            part.setType(CLASS);
                            _state = INIT;
                            _offset++;
                        }else{
                            _hasNext = true;
                            part.setName(_rulesNames[_offset].substring(_dotOffset+1, _colonOffset));
                            part.setType(CLASS);
                            _dotOffset = -1;
                        }
                    }
                    // if this is a pseudo class
                    else if(_colonOffset > -1){
                        if(_dotOffset == -1){                            
                            part.setName(_rulesNames[_offset].substring(_colonOffset+1));
                            part.setType(PSEUDO_CLASS);
                            _state = INIT;
                            _offset++;
                        }else{
                            _hasNext = true;
                            part.setName(_rulesNames[_offset].substring(_colonOffset+1, _dotOffset));
                            part.setType(PSEUDO_CLASS);
                            _colonOffset = -1;
                        }
                    }                    
                    
                    _extra++;
                    
                    part.setFinalDeclaration(!hasNext());
                    
                    break;
            }
            
            return part;
        }
        
        public void remove() {}
    }
    
    
    
    private final class CSSAllRulePartIterator implements Iterator {
        
        private final static int INIT = 0;
        private final static int WAS_EL = 1;
        private final static int WAS_ID = 2;
        private final static int WAS_CLASS = 3;
        
        private String[] _rulesNames;
        private int _offset = 0;
        private int _extra = 0;
        private int _idOffset = -1;
        private int _dotOffset = -1;
        private int _colonOffset = -1;
        private int _lastOffset = -1;
        private boolean _hasNext = false;
        
        CSSAllRulePartIterator(String[] rulesNames){
            _rulesNames = rulesNames;
        }
        
        public boolean hasNext() {
            return (_offset < _rulesNames.length) || _hasNext;
        }
        
        public Object next() {
            
            _hasNext = false;
            
            final CSSRulePart part = new CSSRulePart();
            
            part.setFile(getFile());
            part.setLineNumber(getLineNumber());
            part.setRule(CSSRule.this);
            part.setSpaceBefore(_lastOffset == -1 && _offset > 0);
            
            final String ruleName = _rulesNames[_offset++];
            
            if(_lastOffset == -1){
                
                int startOffset = 0;
                
                switch(ruleName.charAt(0)){
                    case '.' :
                        startOffset = 1;
                        part.setType(CLASS);
                        break;
                        
                    case '#' :
                        startOffset = 1;
                        part.setType(ID);
                        break;
                        
                    default :
                        part.setType(ELEMENT);
                }
                
                final int length = ruleName.length();
                for (int i = 1; i < length; i++) {
                    switch(ruleName.charAt(i)){
                        case '#' :
                        case '.' :
                        case ':' :
                            _offset--;
                            _lastOffset = i;
                            part.setName(ruleName.substring(startOffset, i));
                            return part;
                    }
                }
                
                part.setName(ruleName.substring(startOffset));
                part.setFinalDeclaration(!hasNext());
                _lastOffset = -1;
                return part;
                
            }else{
                
                int startOffset = _lastOffset;
                
                switch(ruleName.charAt(_lastOffset)){
                    case '.' :
                        startOffset = _lastOffset + 1;
                        part.setType(CLASS);
                        break;
                        
                    case '#' :
                        startOffset = _lastOffset + 1;
                        part.setType(ID);
                        break;
                        
                    case ':' :
                        startOffset = _lastOffset + 1;
                        part.setType(PSEUDO_CLASS);
                        break;
                }
                
                final int length = ruleName.length();
                for (int i = startOffset; i < length; i++) {
                    switch(ruleName.charAt(i)){
                        case '#' :
                        case '.' :
                        case ':' :
                            _offset--;
                            _lastOffset = i;
                            part.setName(ruleName.substring(startOffset, i));
                            return part;
                    }
                }
                
                part.setName(ruleName.substring(startOffset));
                part.setFinalDeclaration(!hasNext());
                _lastOffset = -1;
            }
            
            return part;
        }
        
        public void remove() {}
    }
    
    
    
    
    public final static Comparator NAME_COMPARATOR = new Comparator(){
        public int compare(Object obj1, Object obj2) {
            final CSSRule rule1 = (CSSRule)obj1;
            final CSSRule rule2 = (CSSRule)obj2;
            return rule1.getSortLabel().compareTo(rule2.getSortLabel());
        }
    };
    
    
    
    public final static Comparator TYPE_COMPARATOR = new Comparator(){
        public int compare(Object obj1, Object obj2) {
            final CSSRule rule1 = (CSSRule)obj1;
            final CSSRule rule2 = (CSSRule)obj2;
            if(rule1.getType() > rule2.getType()) return -1;
            if(rule1.getType() == rule2.getType()) return 0;
            return 1;
        }
    };
    
    public final static Comparator DECLARATION_COMPARATOR = new Comparator(){
        public int compare(Object obj1, Object obj2) {
            final CSSRule rule1 = (CSSRule)obj1;
            final CSSRule rule2 = (CSSRule)obj2;
            if(rule1.getLineNumber() < rule2.getLineNumber()) return -1;
            if(rule1.getLineNumber() == rule2.getLineNumber()) return NAME_COMPARATOR.compare(obj1, obj2);
            return 1;
        }
    };
    
}
