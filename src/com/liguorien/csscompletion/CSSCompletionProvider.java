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
 * CSSCompletionProvider.java
 *
 * Created on February 13, 2006, 7:12 PM
 */
package com.liguorien.csscompletion;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import javax.swing.text.JTextComponent;
import org.netbeans.editor.Utilities;
import org.netbeans.editor.BaseDocument;
import org.netbeans.spi.editor.completion.CompletionProvider;
import org.netbeans.spi.editor.completion.CompletionResultSet;
import org.netbeans.spi.editor.completion.CompletionTask;
import org.netbeans.spi.editor.completion.support.AsyncCompletionQuery;
import org.netbeans.spi.editor.completion.support.AsyncCompletionTask;
import org.openide.ErrorManager;

/**
 * @author Nicolas Désy
 */
public class CSSCompletionProvider implements CompletionProvider {
    
    public CompletionTask createTask(int queryType, final JTextComponent component) {
        return new AsyncCompletionTask(new AsyncCompletionQuery() {
            protected void query(CompletionResultSet resultSet, Document doc, int caretOffset) {
                
                final BaseDocument bDoc = (BaseDocument)doc;
                boolean showCompletion = false;
                String filter = null;
                int startOffset = caretOffset-1;
                
                try {
                    bDoc.readLock();
                    
                    final int lineStartOffset = Utilities.getRowFirstNonWhite(bDoc, caretOffset);
                    
                    if(lineStartOffset > -1 && caretOffset > lineStartOffset){
                        
                        final char[] line = bDoc.getChars(lineStartOffset, caretOffset-lineStartOffset);
                        final int colonOffset = indexOf(line, ':');
                        final int semicolonOffset = indexOf(line, ';');
                        
                        if(colonOffset == -1 || (semicolonOffset > -1 && semicolonOffset > colonOffset)){
                            showCompletion = true;
                            final int whiteOrSemiOffset = indexOfWhiteOrSemicolon(line);
                            filter = new String(line, whiteOrSemiOffset+1, line.length-whiteOrSemiOffset-1);
                            if(whiteOrSemiOffset > 0){
                                startOffset = lineStartOffset + whiteOrSemiOffset;
                            }else{
                                startOffset = lineStartOffset - 1;
                            }
                        }
                    }else{
                        showCompletion = true;
                    }
                    
                    if(showCompletion){
                        //check if the caret is inside a valid CSS block
                        int i = caretOffset;
                        int lbraceOffset = -1;
                        int rbraceOffset = -1;
                        final char[] buffer = new char[1];
                        
                        while(--i > -1){
                            bDoc.getChars(i, buffer, 0, 1);
                            if(buffer[0]=='{'){lbraceOffset=i; break;}
                            if(buffer[0]=='}'){rbraceOffset=i; break;}
                        }
                        
                        showCompletion = (lbraceOffset > -1 && lbraceOffset > rbraceOffset);
                    }
                } catch (BadLocationException ex) {
                    ErrorManager.getDefault().notify(ex);
                }finally{
                    bDoc.readUnlock();
                }
                
                if(showCompletion){
                    if(filter != null){
                        final Iterator it = keywords.iterator();
                        while(it.hasNext()){
                            final String entry = (String)it.next();
                            if(entry.startsWith(filter)){
                                resultSet.addItem(new CSSCompletionItem(entry, startOffset, caretOffset));
                            }
                        }
                    }else{
                        final Iterator it = keywords.iterator();
                        while(it.hasNext()){
                            resultSet.addItem(new CSSCompletionItem((String)it.next(), startOffset, caretOffset));
                        }
                    }
                }
                
                resultSet.finish();
            }
        }, component);
    }
    
    
    public int getAutoQueryTypes(JTextComponent component, String typedText) {
        return 0;
    }
    
    static int indexOf(char[] line, char c){
        for(int i=0; i<line.length; i++){
            if(line[i] == c) return i;
        }
        return -1;
    }
    
    static int indexOfWhiteOrSemicolon(char[] line){
        int i = line.length;
        while(--i > -1){
            final char c = line[i];
            if(c=='{' || c==';' || Character.isWhitespace(c)){
                return i;
            }
        }
        return -1;
    }
    
    
    private final static List keywords = new ArrayList();
    static{
        keywords.add("font");
        keywords.add("font-family");
        keywords.add("font-size");
        keywords.add("font-style");
        keywords.add("font-variant");
        keywords.add("font-weight");
        keywords.add("background");
        keywords.add("background-attachment");
        keywords.add("background-color");
        keywords.add("background-image");
        keywords.add("background-position");
        keywords.add("background-repeat");
        keywords.add("color");
        keywords.add("letter-spacing");
        keywords.add("line-height");
        keywords.add("text-decoration");
        keywords.add("text-align");
        keywords.add("text-indent");
        keywords.add("text-transform");
        keywords.add("text-shadow");
        keywords.add("vertical-align");
        keywords.add("white-space");
        keywords.add("word-spacing");
        keywords.add("border");
        keywords.add("border-color");
        keywords.add("border-style");
        keywords.add("border-width");
        keywords.add("border-top");
        keywords.add("border-right");
        keywords.add("border-bottom");
        keywords.add("border-left");
        keywords.add("border-top-color");
        keywords.add("border-right-color");
        keywords.add("border-bottom-color");
        keywords.add("border-left-color");
        keywords.add("border-top-style");
        keywords.add("border-right-style");
        keywords.add("border-bottom-style");
        keywords.add("border-left-style");
        keywords.add("border-top-width");
        keywords.add("border-right-width");
        keywords.add("border-bottom-width");
        keywords.add("border-left-width");
        keywords.add("height");
        keywords.add("line-height");
        keywords.add("margin");
        keywords.add("margin-top");
        keywords.add("margin-right");
        keywords.add("margin-bottom");
        keywords.add("margin-left");
        keywords.add("max-height");
        keywords.add("max-width");
        keywords.add("min-height");
        keywords.add("min-width");
        keywords.add("padding");
        keywords.add("padding-top");
        keywords.add("padding-right");
        keywords.add("padding-bottom");
        keywords.add("padding-left");
        keywords.add("vertical-align");
        keywords.add("width");
        keywords.add("clip");
        keywords.add("direction");
        keywords.add("overflow");
        keywords.add("visibility");
        keywords.add("clear");
        keywords.add("display");
        keywords.add("float");
        keywords.add("position");
        keywords.add("top");
        keywords.add("right");
        keywords.add("bottom");
        keywords.add("left");
        keywords.add("z-index");
        keywords.add("list-style-type");
        keywords.add("list-style-image");
        keywords.add("list-style-position");
        keywords.add("list-style");
        keywords.add("marker-offset");
        keywords.add("content");
        keywords.add("counter-increment");
        keywords.add("counter-reset");
        keywords.add("size");
        keywords.add("marks");
        keywords.add("page-break-before");
        keywords.add("page-break-after");
        keywords.add("page-break-inside");
        keywords.add("page");
        keywords.add("orphans");
        keywords.add("widows");
        keywords.add("border-collapse");
        keywords.add("border-spacing");
        keywords.add("caption-side");
        keywords.add("empty-cells");
        keywords.add("table-layout");
        keywords.add("cursor");
        keywords.add("outline");
        keywords.add("outline-color");
        keywords.add("outline-style");
        keywords.add("outline-width");
        keywords.add("azimuth");
        keywords.add("cue");
        keywords.add("cue-after");
        keywords.add("cue-before");
        keywords.add("elevation");
        keywords.add("pause");
        keywords.add("pause-after");
        keywords.add("pause-before");
        keywords.add("play-during");
        keywords.add("pitch");
        keywords.add("pitch-range");
        keywords.add("richness");
        keywords.add("speak");
        keywords.add("speak-header");
        keywords.add("speak-numeral");
        keywords.add("speak-punctuation");
        keywords.add("speech-rate");
        keywords.add("stress");
        keywords.add("voice-family");
        keywords.add("volume");
    }
}