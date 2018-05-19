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
 * CreateDeclarationAction.java
 *
 * Created on February 28, 2006, 9:56 PM
 */

package com.liguorien.csscompletion.actions;

import com.liguorien.csscompletion.CSSRuleNode;
import java.awt.event.ActionEvent;
import java.io.IOException;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.text.BadLocationException;
import javax.swing.text.StyledDocument;
import org.openide.ErrorManager;
import org.openide.cookies.EditorCookie;
import org.openide.loaders.DataObject;
import org.openide.loaders.DataObjectNotFoundException;
import org.openide.nodes.Node;
import org.openide.text.NbDocument;
import org.openide.util.NbBundle;

/**
 * @author Nicolas Désy
 */
public class CreateDeclarationAction extends AbstractAction {
    
    private CSSRuleNode _node;
    
    public CreateDeclarationAction(CSSRuleNode node) {
        _node = node;
        putValue(Action.NAME, NbBundle.getMessage(RenameAction.class, "LBL_CreateDeclaration"));
    }
    
    public void actionPerformed(ActionEvent ae) {
       
        try {
            
            _node.setRuleLeaf(true);
            
            final DataObject data = DataObject.find(_node.getFile());
            final EditorCookie ec = (EditorCookie)data.getCookie(EditorCookie.class);
            
            final StringBuffer declaration = new StringBuffer();
            
            declaration
                    .append("\n\n")
                    .append(_node.getDeclaration().trim())
                    .append(" {\n    \n}\n");
            
            if(ec != null){
                ec.open();
                final StyledDocument doc = ec.openDocument();
                          
                NbDocument.runAtomic(doc, new Runnable() {
                    public void run() {
                        try {
                            doc.insertString(doc.getLength(), declaration.toString(),null);
                            if(ec.getOpenedPanes() != null && ec.getOpenedPanes().length > 0){
                                ec.getOpenedPanes()[0].setCaretPosition(doc.getLength()-3);
                            }
                        } catch (BadLocationException ex) {
                            ErrorManager.getDefault().notify(ex);
                        }                        
                    }
                });                
            }
        } catch (Exception ex) {
            ErrorManager.getDefault().notify(ex);
        }
    }
}
