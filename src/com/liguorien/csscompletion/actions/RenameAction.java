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
 * RenameAction.java
 *
 * Created on February 26, 2006, 2:27 PM
 */

package com.liguorien.csscompletion.actions;

import com.liguorien.csscompletion.CSSExplorer;
import com.liguorien.csscompletion.CSSRule;
import com.liguorien.csscompletion.CSSRuleNode;
import com.liguorien.csscompletion.CSSRulePart;
import java.awt.Dialog;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Iterator;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JEditorPane;
import javax.swing.text.BadLocationException;
import org.netbeans.editor.BaseDocument;
import org.netbeans.editor.Utilities;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.ErrorManager;
import org.openide.cookies.EditorCookie;
import org.openide.loaders.DataObject;
import org.openide.text.NbDocument;
import org.openide.util.NbBundle;


/**
 * @author Nicolas Désy
 */
public class RenameAction extends AbstractAction {
    
    private CSSRuleNode _node;
    private RuleMatchResult _result = new RuleMatchResult();
    
    public RenameAction(CSSRuleNode node) {
        _node = node;
        putValue(Action.NAME, NbBundle.getMessage(RenameAction.class, "LBL_Rename"));
    }
    
    public void actionPerformed(ActionEvent ae) {
        final RenameDialog view = new RenameDialog(_node.getName());
        final DialogDescriptor descriptor =
                new DialogDescriptor(view, NbBundle.getMessage(RenameDialog.class, "LBL_Rename"));
        final Dialog dialog = DialogDisplayer.getDefault().createDialog(descriptor);
        
        descriptor.setButtonListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                if("OK".equals(e.getActionCommand())){
                    rename(view.getNewName());
                }
            }
        });
        
        dialog.setVisible(true);
    }
    
    private String getDeclaration(String str){
        switch(_node.getType()){
            case CSSRule.ID : return '#' + str;
            case CSSRule.CLASS : return '.' + str;
            case CSSRule.PSEUDO_CLASS : return ':' + str;
        }
        return str;
    }
    
    private String replace(CSSRule dependency, String oldName, String newName){
        
        final CSSRulePart replacePart = _node.getRulePart();
        final StringBuffer buffer = new StringBuffer();
        final Iterator/*<CSSRulePart>*/ it = dependency.getAllParts();
        
        while(it.hasNext()){
            final CSSRulePart part = (CSSRulePart)it.next();
            
            if(part.isSamePath(replacePart)){
                part.setName(newName);
            }
            
            if(part.getType() != CSSRule.PSEUDO_CLASS){
                buffer.append(' ');
            }
            
            buffer.append(part.getDeclaration());
        }
        
        return buffer.toString().trim();
    }
    
    
    private void rename(final String newName){
        try {
            
            int i = -1;
            final CSSRulePart replacePart = _node.getRulePart();
            final int nbDependencies = replacePart.getDependenciesCount();
            final CSSRule[] dependencies = new CSSRule[nbDependencies];
            final String[] newDependencies = new String[nbDependencies];
            final Iterator it = replacePart.getDependencies();
            
            
            while(it.hasNext()){
                final CSSRule dependency = (CSSRule)it.next();
                dependencies[++i] = dependency;
                newDependencies[i] = dependency.rename(replacePart, newName);
            }
            
            final DataObject data = DataObject.find(_node.getFile());
            final EditorCookie ec = (EditorCookie)data.getCookie(EditorCookie.class);
            if(ec != null){
                
                final JEditorPane[] panes = ec.getOpenedPanes();
                
                if(panes != null && panes.length > 0){
                    
                    final BaseDocument doc = Utilities.getDocument(panes[0]);
                    
                    doc.runAtomic(new Runnable() {
                        public void run() {
                            try {
                                
                                final int length = doc.getLength();
                                int startOffset = 0;
                                String line = null;
                                
                                while(startOffset < length){
                                    
                                    int endOffset = Utilities.getRowEnd(doc, startOffset);                                    
                                    line = new String(doc.getChars(startOffset, endOffset-startOffset));
                                    
                                    for(int i=0; i<nbDependencies; i++){                                        
                                        final int matchOffset = line.indexOf(dependencies[i].getLabel());
                                       
                                        if(matchOffset > -1){                                            
                                            checkIfRuleMatch(matchOffset, line, dependencies[i]);
                                            
                                            if(_result.match){
                                                doc.remove(startOffset + _result.start, dependencies[i].getLabel().length());
                                                doc.insertString(startOffset + _result.start, newDependencies[i], null);
                                                endOffset = Utilities.getRowEnd(doc, startOffset);
                                                line = doc.getText(startOffset, endOffset-startOffset-1);
                                            }
                                        }
                                    }
                                    
                                    startOffset = endOffset + 1;                                    
                                }                                
                                
                                for(int i=0; i<nbDependencies; i++){
                                    dependencies[i].setLabel(newDependencies[i]);
                                }
                                
                                _node.changeName(newName);
                                
                                CSSExplorer.getInstance().getNavigatorPanel().dontRefresh();
                                
                            } catch (BadLocationException ex) {
                                ErrorManager.getDefault().notify(ex);
                            }
                        }
                    });                    
                }
            }
        } catch (Exception ex) {
            ErrorManager.getDefault().notify(ex);
        }
    }
    
    
//    private void rename(final String newName){
//        try {
//
//            int i = -1;
//            final CSSRulePart replacePart = _node.getRulePart();
//            final int nbDependencies = replacePart.getDependenciesCount();
//            final CSSRule[] dependencies = new CSSRule[nbDependencies];
//            final String[] newDependencies = new String[nbDependencies];
//            final Iterator it = replacePart.getDependencies();
//
//
//            while(it.hasNext()){
//                final CSSRule dependency = (CSSRule)it.next();
//                dependencies[++i] = dependency;
//                newDependencies[i] = dependency.rename(replacePart, newName);
//            }
//
//            final DataObject data = DataObject.find(_node.getFile());
//            final EditorCookie ec = (EditorCookie)data.getCookie(EditorCookie.class);
//
//            if(ec != null){
//                ec.open();
//                final StyledDocument doc = ec.openDocument();
//
//                NbDocument.runAtomic(doc, new Runnable() {
//                    public void run() {
//                        try {
//                            int lineNumber = NbDocument.findLineNumber(doc, doc.getLength() - 1);
//                            int currentLine = -1;
//                            int lineStart = 0;
//                            int lineEnd = 0;
//                            String line = null;
//
//                            while(++currentLine < lineNumber){
//
//                                lineStart = lineEnd;
//                                lineEnd = NbDocument.findLineOffset(doc, currentLine + 1);
//                                line = doc.getText(lineStart, lineEnd-lineStart);
//
//                                for(int i=0; i<nbDependencies; i++){
//
//                                    final int matchOffset = line.indexOf(dependencies[i].getLabel());
//                                    if(matchOffset > -1){
//
//                                        checkIfRuleMatch(matchOffset, line, dependencies[i]);
//
//
//                                        if(_result.match){
//                                            doc.remove(lineStart + _result.start, dependencies[i].getLabel().length());
//                                            doc.insertString(lineStart + _result.start, newDependencies[i], null);
//                                            lineEnd = NbDocument.findLineOffset(doc, currentLine + 1);
//                                            line = doc.getText(lineStart, lineEnd-lineStart-1);
//                                        }
//                                    }
//                                }
//                            }
//
//                            for(int i=0; i<nbDependencies; i++){
//                                dependencies[i].setLabel(newDependencies[i]);
//                            }
//
//                            _node.changeName(newName);
//
//                            CSSExplorer.getInstance().getNavigatorPanel().dontRefresh();
//
//                        } catch (BadLocationException ex) {
//                            ErrorManager.getDefault().notify(ex);
//                        }
//                    }
//                });
//            }
//        } catch (Exception ex) {
//            ErrorManager.getDefault().notify(ex);
//        }
//    }
    
    private static class RuleMatchResult {
        int start;
        int end;
        boolean match;
    }
    
    private void checkIfRuleMatch(int matchOffset, String line, CSSRule rule){
        
        _result.start = matchOffset;
        _result.end = line.length()-1;
        
        int i = matchOffset;
        
        while(--i > -1){
            char ch = line.charAt(i);
            if(ch == ',' || ch == '}'){
                _result.start = i;
                break;
            }
        }
        
        while(++i <= matchOffset){
            if(line.charAt(i) != ' '){
                _result.start = i;
                break;
            }
        }
        
        final int length = line.length();
        i = matchOffset;
        
        while(++i < length){
            char ch = line.charAt(i);
            if(ch == ',' || ch == '{'){
                _result.end = i;
                break;
            }
        }
        
        while(--i >= matchOffset){
            if(line.charAt(i) != ' '){
                _result.end = i+1;
                break;
            }
        }
        
        _result.match = rule.getLabel().equals(line.substring(_result.start, _result.end).trim());
    }
}