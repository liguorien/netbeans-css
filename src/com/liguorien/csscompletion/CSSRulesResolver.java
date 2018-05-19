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
 * CSSRulesResolver.java
 *
 * Created on February 20, 2006, 7:47 PM
 */

package com.liguorien.csscompletion;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import javax.swing.JEditorPane;
import javax.swing.text.StyledDocument;
import org.netbeans.editor.BaseDocument;
import org.netbeans.editor.Utilities;
import org.openide.ErrorManager;
import org.openide.cookies.EditorCookie;
import org.openide.filesystems.FileObject;
import org.openide.loaders.DataObject;
import org.openide.text.Line;
import org.openide.text.NbDocument;

/**
 * Class used to resolve the rules that contains a CSS document
 * @author Nicolas Désy
 */
public final class CSSRulesResolver {
    
    private final static int INSIDE_RULE = 0;
    private final static int OUTSIDE_RULE = 1;
    private final static int INSIDE_COMMENT = 2;
    
    private List _results = new ArrayList();
    private int _state = OUTSIDE_RULE;
    private int _oldState = _state;
    private int _currentLine = 0;
    private FileObject _file = null;
    private StringBuffer _currentRule = new StringBuffer();
    
    /**
     * Create an instance of CSSRuleResolver
     * @return an instance of CSSRuleResolver
     */
    public static CSSRulesResolver newInstance(){
        return new CSSRulesResolver();
    }
    
    /** Creates a new instance of CSSRulesResolver */
    private CSSRulesResolver() {
        
    }
    
    
    /**
     * Parse the file and return a list of CSSRule
     * @param data A DataObject that represent a CSS document
     * @return A List of CSSRule
     */
    public List/*<CSSRule>*/ resolve(DataObject data){
        
        _file = data.getPrimaryFile();
      
        final EditorCookie ec = (EditorCookie)data.getCookie(EditorCookie.class);
        if(ec != null){
            final JEditorPane[] panes = ec.getOpenedPanes();
            if(panes != null && panes.length > 0){
                return resolveBaseDocument(Utilities.getDocument(panes[0]));
            }
        }
        
        return resolveFile(_file);
    }
    
    
    /**
     * Resolve a DataObject by using a LineCookie
     * @param cookie A LineCookie
     * @return A List of CSSRule
     */
    public List/*<CSSRule>*/ resolveLineSet(Line.Set lines){
        final Iterator it = lines.getLines().iterator();
        while(it.hasNext()){
            Line line = (Line)it.next();
            _parseLine(line.getText());
            _currentLine++;
        }
        return _results;
    }
    
    
    /**
     * Resolve a DataObject by using a BaseDocument
     * @param doc An instance of BaseDocument
     * @return A List of CSSRule
     */
    public List/*<CSSRule>*/ resolveBaseDocument(BaseDocument doc){
        
        doc.readLock();
        
        _currentLine = 0;
        
        try {
            
            final int length = doc.getLength();
            int startOffset = 0;
            while(startOffset < length){
                final int endOffset = Utilities.getRowEnd(doc, startOffset);               
                _parseLine(new String(doc.getChars(startOffset, endOffset-startOffset)));
                startOffset = endOffset + 1;
                _currentLine++;
            }
            
        } catch (Exception ex) {
            ErrorManager.getDefault().notify(ex);
        } finally {
            doc.readUnlock();
        }
        
        return _results;
    }
    
    /**
     * Resolve a DataObject by using a StyledDocument
     * @param doc A StyledDocument
     * @return A List of CSSRule
     */
    public List/*<CSSRule>*/ resolveDocument(StyledDocument doc){
        
        try {
            
            final int lineNumber = NbDocument.findLineNumber(doc, doc.getLength() - 1);
            _currentLine = 0;
            int start = 0;
            int end = 0;
            String line = null;
            
            while(_currentLine < lineNumber){
                start = end;
                end = NbDocument.findLineOffset(doc, _currentLine + 1);
                line = doc.getText(start, end-start);
                _parseLine(line);
                _currentLine++;
            }
            
            
        } catch (Exception ex) {
            ErrorManager.getDefault().notify(ex);
        }
        
        return _results;
    }
    
    
    /**
     * Resolve a DataObject by using a FileObject
     * @param file A FileObject
     * @return A List of CSSRule
     */
    public List/*<CSSRule>*/ resolveFile(FileObject file){
        
        _file = file;
        
        BufferedReader in = null;
        
        try {
            in = new BufferedReader(new InputStreamReader(file.getInputStream()));
            String line = null;
            
            while((line = in.readLine()) != null){
                _parseLine(line);
                _currentLine++;
            }
            
        } catch (Exception ex) {
            ErrorManager.getDefault().notify(ex);
        } finally{
            try {
                if(in != null) in.close();
            } catch (IOException ex) {
                ErrorManager.getDefault().notify(ex);
            }
        }
        
        return _results;
    }
    
    private void _flushBuffer(){
        final String[] rules = _currentRule.toString().trim().split(",");
        for(int i=0; i<rules.length; i++){
            final CSSRule item = new CSSRule();
            item.setLabel(rules[i].trim());
            item.setLineNumber(_currentLine);
            item.setFile(_file);
            item.resolveType();
            _results.add(item);
        }
        _currentRule = new StringBuffer(64);
    }
    
    private void _parseLine(String line){
        
        final int length = line.length();
        int offset = -1;
        
        while(++offset < length){
            
            final char currentChar = line.charAt(offset);
            
            switch(currentChar){                
                
                case '/' :
                    
                    if(_state == INSIDE_COMMENT){
                        if(line.charAt(offset-1) == '*'){
                            _state = _oldState;
                        }
                    }else if(offset+1 < length && line.charAt(offset+1) == '*'){
                        _oldState = _state;
                        _state = INSIDE_COMMENT;
                    }
                    
                    break;
                    
                    
                case '{' :
                    
                    if(_state == OUTSIDE_RULE){
                        _flushBuffer();
                        _state = INSIDE_RULE;
                    }
                    
                    break;
                    
                case '}' :
                    if(_state == INSIDE_RULE){
                        _state = OUTSIDE_RULE;
                    }
                    break;
                    
                default :
                    
                    if(_state == OUTSIDE_RULE){
                        _currentRule.append(currentChar);
                    }
                    break;
            }
        }
    }
    
    
    /**
     * Resolve a DataObject and build a tree of CSSRulePart
     * @param data A DataObject that represent a CSS document
     * @param root The root of the tree
     */
    public static void buildRulePartTree(DataObject data, CSSRulePart root){
        
        final List results = newInstance().resolve(data);
        final Iterator rules = results.iterator();
        
        while(rules.hasNext()){
            
            final CSSRule rule = (CSSRule)rules.next();
            final Iterator ruleParts = rule.getParts();
            
            CSSRulePart current = root;
            
            while(ruleParts.hasNext()){
                
                final CSSRulePart newPart = (CSSRulePart)ruleParts.next();
                final CSSRulePart existingPart = current.findChild(newPart);
                
                // if there is already a part with the same declaration on this branch
                if(existingPart != null){
                    existingPart.confirmPresence();
                    existingPart.addDependency(newPart.getRule());
                    current = existingPart;
                    
                    // check if the new part is a final declaration
                    // if yes, transfer some infos to the existing part
                    if(newPart.isFinalDeclaration()){
                        current.setFinalDeclaration(true);
                        current.setRule(newPart.getRule());
                        current.setLineNumber(newPart.getLineNumber());
                        current.initNode();
                        current.getParent().refresh();
                    }
                    
                }else{
                    // add the new part to the tree
                    newPart.addDependency(newPart.getRule());
                    current.addChild(newPart);
                    current.refresh();
                    current = newPart;
                    current.confirmPresence();
                }
            }
        }
    }
}
