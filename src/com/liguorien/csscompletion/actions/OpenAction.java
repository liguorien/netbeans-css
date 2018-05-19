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
 * OpenAction.java
 *
 * Created on February 28, 2006, 9:56 PM
 */

package com.liguorien.csscompletion.actions;

import com.liguorien.csscompletion.CSSRuleNode;
import java.awt.event.ActionEvent;
import javax.swing.AbstractAction;
import javax.swing.Action;
import org.openide.ErrorManager;
import org.openide.cookies.LineCookie;
import org.openide.loaders.DataObject;
import org.openide.text.Line;
import org.openide.util.NbBundle;

/**
 * @author Nicolas Désy
 */
public class OpenAction extends AbstractAction {
   
    private CSSRuleNode _node;
   
    public OpenAction(CSSRuleNode node) {
        _node = node;
        putValue(Action.NAME, NbBundle.getMessage(RenameAction.class, "LBL_Open"));
    }
   
    public void actionPerformed(ActionEvent ae) {
        try {           
            final LineCookie lc = 
                    (LineCookie) DataObject.find(_node.getFile()).getCookie(LineCookie.class);
            
            if (lc != null)
                lc.getLineSet().getCurrent(_node.getLineNumber()).show(Line.SHOW_GOTO);
       
        } catch (Exception ex) {         
            ErrorManager.getDefault().notify(ex);
        }
    }
}