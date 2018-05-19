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
 * CSSCompletionItem.java
 *
 * Created on February 13, 2006, 8:31 PM
 */
package com.liguorien.csscompletion;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.event.KeyEvent;
import javax.swing.ImageIcon;
import javax.swing.text.BadLocationException;
import javax.swing.text.JTextComponent;
import org.netbeans.api.editor.completion.Completion;
import org.netbeans.editor.BaseDocument;
import org.netbeans.spi.editor.completion.CompletionItem;
import org.netbeans.spi.editor.completion.CompletionTask;
import org.netbeans.spi.editor.completion.support.CompletionUtilities;
import org.openide.ErrorManager;
import org.openide.util.Utilities;

/**
 * @author Nicolas Désy
 */
public class CSSCompletionItem implements CompletionItem {

    private static Color fieldColor = Color.decode("0x0000B2");
    private static ImageIcon fieldIcon = null;

    private ImageIcon  _icon;
    private int _type;
    private int _carretOffset;
    private int _dotOffset;
    private String _text;
    
    /** Creates a new instance of CSSCompletionItem */
    public CSSCompletionItem(String text, int dotOffset, int carretOffset) {
        _text = text;
        _dotOffset = dotOffset;
        _carretOffset = carretOffset;
        
        if(fieldIcon == null){           
            fieldIcon = new ImageIcon(Utilities.loadImage("com/liguorien/csscompletion/resources/field-icon.png"));            
        }
        
        _icon = fieldIcon;        
    }
    
    private void doSubstitute(JTextComponent component, String toAdd, int backOffset) {
        final BaseDocument doc = (BaseDocument) component.getDocument();
        final int caretOffset = component.getCaretPosition();
        String value = getText();
        
        if (toAdd != null) {
            value += toAdd;
        }
        
        // Update the text
        doc.atomicLock();
        
        try {
            
            doc.remove(_dotOffset+1, _carretOffset-_dotOffset-1);
            doc.insertString(_dotOffset+1, value + ": ", null);
            component.setCaretPosition(component.getCaretPosition() - backOffset);           
            
        } catch (BadLocationException e) {
            ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, e);
        } finally {
            doc.atomicUnlock();
        }
    }
    
    public void defaultAction(JTextComponent component) {
        doSubstitute(component, null, 0);
        Completion.get().hideAll();
    }
    
    public void processKeyEvent(KeyEvent evt) {
        if (evt.getID() == KeyEvent.KEY_TYPED && evt.getKeyCode() == KeyEvent.VK_ENTER) {
            doSubstitute((JTextComponent) evt.getSource(), _text, _text.length() - 1);
            evt.consume();
        }
    }
    
    public int getPreferredWidth(Graphics g, Font defaultFont) {        
        return CompletionUtilities.getPreferredWidth(_text, null, g, defaultFont);
    }
    
    public void render(Graphics g, Font defaultFont, Color defaultColor, Color backgroundColor, int width, int height, boolean selected) {
        CompletionUtilities.renderHtml(_icon, _text, null, g, defaultFont,
                        (selected ? Color.white : fieldColor), width, height, selected);
    }
    
    public CompletionTask createDocumentationTask() {
        return null;
    }
    
    public CompletionTask createToolTipTask() {
        return null;
    }
    
    public boolean instantSubstitution(JTextComponent component) {
        return true; //????
    }
    
    public int getSortPriority() {
        return 0;
    }
    
    public CharSequence getSortText() {
        return getText();
    }
    
    public CharSequence getInsertPrefix() {
        return getText();
    }
    
    public String getText() {
        return _text;
    }
    
    public int hashCode() {
        return getText().hashCode();
    }
    
    public boolean equals(Object o) {
        if (!(o instanceof CSSCompletionItem))
            return false;
        
        CSSCompletionItem remote = (CSSCompletionItem) o;
        
        return getText().equals(remote.getText());
    }
}