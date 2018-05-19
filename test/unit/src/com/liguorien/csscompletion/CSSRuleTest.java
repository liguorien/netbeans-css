/*
 * CSSRuleTest.java
 * JUnit based test
 *
 * Created on March 5, 2006, 12:23 AM
 */

package com.liguorien.csscompletion;

import junit.framework.*;
import java.util.Iterator;

/**
 *
 * @author Nico
 */
public class CSSRuleTest extends TestCase {
    
    public CSSRuleTest(String testName) {
        super(testName);
    }
    
    public static Test suite() {
        TestSuite suite = new TestSuite(CSSRuleTest.class);
        
        return suite;
    }
    
    private void assertPart(CSSRulePart part, String name, int type, boolean finalDeclaration){
        assertTrue("'"+part+"' is invalid", part.getType() == type &&
                name.equals(part.getName()) &&
                part.isFinalDeclaration() == finalDeclaration);
    }
    
    private void assertPart(Iterator parts, String name, int type, boolean finalDeclaration){
        final CSSRulePart part = (CSSRulePart)parts.next();       
        assertTrue("'"+part+"' is invalid", part.getType() == type &&
                name.equals(part.getName()) &&
                part.isFinalDeclaration() == finalDeclaration);
    }
    
    
    public void testBuildSingleTree(){
        
        CSSRule rule = new CSSRule();
        rule.setLabel("#foo a:hover");
        CSSRulePart part = rule.buildSingleTree();
        
        assertPart(part, "foo", CSSRule.ID, false);
        assertPart(part = part.getFirstChild(), "a", CSSRule.ELEMENT, false);
        assertPart(part.getFirstChild(), "hover", CSSRule.PSEUDO_CLASS, true);
        
        
        rule.setLabel("div#foo div#main table tr td.selected table tr td:hover");
        part = rule.buildSingleTree();
        assertPart(part, "div", CSSRule.ELEMENT, false);
        assertFalse(part.isSpaceBefore());
        assertPart(part = part.getFirstChild(), "foo", CSSRule.ID, false);
        assertFalse(part.isSpaceBefore());
        assertPart(part = part.getFirstChild(), "div", CSSRule.ELEMENT, false);
        assertTrue(part.isSpaceBefore());
        assertPart(part = part.getFirstChild(), "main", CSSRule.ID, false);
        assertFalse(part.isSpaceBefore());
        assertPart(part = part.getFirstChild(), "table", CSSRule.ELEMENT, false);
        assertTrue(part.isSpaceBefore());
        assertPart(part = part.getFirstChild(), "tr", CSSRule.ELEMENT, false);
        assertTrue(part.isSpaceBefore());
        assertPart(part = part.getFirstChild(), "td", CSSRule.ELEMENT, false);
        assertTrue(part.isSpaceBefore());
        assertPart(part = part.getFirstChild(), "selected", CSSRule.CLASS, false);
        assertFalse(part.isSpaceBefore());
        assertPart(part = part.getFirstChild(), "table", CSSRule.ELEMENT, false);
        assertTrue(part.isSpaceBefore());
        assertPart(part = part.getFirstChild(), "tr", CSSRule.ELEMENT, false);
        assertTrue(part.isSpaceBefore());
        assertPart(part = part.getFirstChild(), "td", CSSRule.ELEMENT, false);
        assertTrue(part.isSpaceBefore());
        assertPart(part = part.getFirstChild(), "hover", CSSRule.PSEUDO_CLASS, true);
        assertFalse(part.isSpaceBefore());
    }
    
    public void testGetParts() {
        CSSRule rule = new CSSRule();
        CSSRulePart part = null;
        Iterator parts = null;
        
        rule.setLabel("#foo .geez:hover");
        parts = rule.getParts();
        
        try{
            assertPart(parts, "foo", CSSRule.ID, false);
            assertPart(parts, "geez", CSSRule.CLASS, false);
            assertPart(parts, "hover", CSSRule.PSEUDO_CLASS, true);
            assertTrue("Rule parts for rule '"+rule.getLabel()+
                    "' is not suposed to have more then 3 elements", !parts.hasNext());
        }catch(Exception ex){
            assertTrue("'"+rule.getLabel()+"' is invalid", false);
        }
        
        rule.setLabel("table tr td");
        parts = rule.getParts();
        
        try{
            assertPart(parts, "table", CSSRule.ELEMENT, false);
            assertPart(parts, "tr", CSSRule.ELEMENT, false);
            assertPart(parts, "td", CSSRule.ELEMENT, true);
            assertTrue("Rule parts for rule '"+rule.getLabel()+
                    "' is not suposed to have more then 3 elements", !parts.hasNext());
        }catch(Exception ex){
            assertTrue("'"+rule.getLabel()+"' is invalid", false);
        }
        
        rule.setLabel("#main.selected table tr td a:hover");
        parts = rule.getParts();
        
        try{
            
            assertPart(parts, "main", CSSRule.ID, false);
            assertPart(parts, "selected", CSSRule.CLASS, false);
            assertPart(parts, "table", CSSRule.ELEMENT, false);
            assertPart(parts, "tr", CSSRule.ELEMENT, false);
            assertPart(parts, "td", CSSRule.ELEMENT, false);
            assertPart(parts, "a", CSSRule.ELEMENT, false);
            assertPart(parts, "hover", CSSRule.PSEUDO_CLASS, true);
            assertTrue("Rule parts for rule '"+rule.getLabel()+
                    "' is not suposed to have more then 7 elements", !parts.hasNext());
            
        }catch(Exception ex){
            assertTrue("'"+rule.getLabel()+"' is invalid", false);
        }
        
        
        
        rule.setLabel("table.contact td.icons");
        parts = rule.getParts();
        
        try{
            assertPart(parts, "table", CSSRule.ELEMENT, false);
            assertPart(parts, "contact", CSSRule.CLASS, false);
            assertPart(parts, "td", CSSRule.ELEMENT, false);
            assertPart(parts, "icons", CSSRule.CLASS, true);
            assertTrue("Rule parts for rule '"+rule.getLabel()+
                    "' is not suposed to have more then 4 elements", !parts.hasNext());
        }catch(Exception ex){
            assertTrue("'"+rule.getLabel()+"' is invalid", false);
        }
        
        
        rule.setLabel("#global.toto #menu table.contact td.icons");
        parts = rule.getParts();
        
        try{
            assertPart(parts, "menu", CSSRule.ID, false);
            assertPart(parts, "table", CSSRule.ELEMENT, false);
            assertPart(parts, "contact", CSSRule.CLASS, false);
            assertPart(parts, "td", CSSRule.ELEMENT, false);
            assertPart(parts, "icons", CSSRule.CLASS, true);
            assertTrue("Rule parts for rule '"+rule.getLabel()+
                    "' is not suposed to have more then 5 elements", !parts.hasNext());
        }catch(Exception ex){
            assertTrue("'"+rule.getLabel()+"' is invalid", false);
        }
        
    }
    
    
    
    public void testGetAllParts() {
        CSSRule rule = new CSSRule();
        CSSRulePart part = null;
        Iterator parts = null;
        
        rule.setLabel("#foo .geez:hover");
        parts = rule.getAllParts();
        
        try{
            assertPart(parts, "foo", CSSRule.ID, false);
            assertPart(parts, "geez", CSSRule.CLASS, false);
            assertPart(parts, "hover", CSSRule.PSEUDO_CLASS, true);
            assertTrue("Rule parts for rule '"+rule.getLabel()+
                    "' is not suposed to have more then 3 elements", !parts.hasNext());
        }catch(Exception ex){
            assertTrue("'"+rule.getLabel()+"' is invalid", false);
        }
        
        rule.setLabel("table tr td");
        parts = rule.getAllParts();
        
        try{
            assertPart(parts, "table", CSSRule.ELEMENT, false);
            assertPart(parts, "tr", CSSRule.ELEMENT, false);
            assertPart(parts, "td", CSSRule.ELEMENT, true);
            assertTrue("Rule parts for rule '"+rule.getLabel()+
                    "' is not suposed to have more then 3 elements", !parts.hasNext());
        }catch(Exception ex){
            assertTrue("'"+rule.getLabel()+"' is invalid", false);
        }
        
        rule.setLabel("div#foo div#main.selected table tr td a:hover");
        parts = rule.getAllParts();
        
        try{
            assertPart(parts, "div", CSSRule.ELEMENT, false);
            assertPart(parts, "foo", CSSRule.ID, false);
            assertPart(parts, "div", CSSRule.ELEMENT, false);
            assertPart(parts, "main", CSSRule.ID, false);
            assertPart(parts, "selected", CSSRule.CLASS, false);
            assertPart(parts, "table", CSSRule.ELEMENT, false);
            assertPart(parts, "tr", CSSRule.ELEMENT, false);
            assertPart(parts, "td", CSSRule.ELEMENT, false);
            assertPart(parts, "a", CSSRule.ELEMENT, false);
            assertPart(parts, "hover", CSSRule.PSEUDO_CLASS, true);
            assertTrue("Rule parts for rule '"+rule.getLabel()+
                    "' is not suposed to have more then 7 elements", !parts.hasNext());
            
        }catch(Exception ex){
            assertTrue("'"+rule.getLabel()+"' is invalid", false);
        }
        
        
        
        rule.setLabel("table.contact td.icons");
        parts = rule.getAllParts();
        
        try{
            assertPart(parts, "table", CSSRule.ELEMENT, false);
            assertPart(parts, "contact", CSSRule.CLASS, false);
            assertPart(parts, "td", CSSRule.ELEMENT, false);
            assertPart(parts, "icons", CSSRule.CLASS, true);
            assertTrue("Rule parts for rule '"+rule.getLabel()+
                    "' is not suposed to have more then 4 elements", !parts.hasNext());
        }catch(Exception ex){
            assertTrue("'"+rule.getLabel()+"' is invalid", false);
        }
        
        
        rule.setLabel("#global .toto #menu table .contact td .icons");
        parts = rule.getAllParts();
        
        try{
            assertPart(parts, "global", CSSRule.ID, false);
            assertPart(parts, "toto", CSSRule.CLASS, false);
            assertPart(parts, "menu", CSSRule.ID, false);
            assertPart(parts, "table", CSSRule.ELEMENT, false);
            assertPart(parts, "contact", CSSRule.CLASS, false);
            assertPart(parts, "td", CSSRule.ELEMENT, false);
            assertPart(parts, "icons", CSSRule.CLASS, true);
            assertTrue("Rule parts for rule '"+rule.getLabel()+
                    "' is not suposed to have more then 7 elements", !parts.hasNext());
        }catch(Exception ex){
            assertTrue("'"+rule.getLabel()+"' is invalid", false);
        }
        
        
        rule.setLabel("#global .toto #menu table.contact td .icons");
        parts = rule.getAllParts();
        
        try{
            assertPart(parts, "global", CSSRule.ID, false);
            assertPart(parts, "toto", CSSRule.CLASS, false);
            assertPart(parts, "menu", CSSRule.ID, false);
            assertPart(parts, "table", CSSRule.ELEMENT, false);
            assertPart(parts, "contact", CSSRule.CLASS, false);
            assertPart(parts, "td", CSSRule.ELEMENT, false);
            assertPart(parts, "icons", CSSRule.CLASS, true);
            assertTrue("Rule parts for rule '"+rule.getLabel()+
                    "' is not suposed to have more then 7 elements", !parts.hasNext());
        }catch(Exception ex){
            assertTrue("'"+rule.getLabel()+"' is invalid", false);
        }
        
        
        rule.setLabel("div#global div #menu a:hover img");
        parts = rule.getAllParts();
        
        try{
            assertPart(parts, "div", CSSRule.ELEMENT, false);
            assertPart(parts, "global", CSSRule.ID, false);
            assertPart(parts, "div", CSSRule.ELEMENT, false);
            assertPart(parts, "menu", CSSRule.ID, false);
            assertPart(parts, "a", CSSRule.ELEMENT, false);
            assertPart(parts, "hover", CSSRule.PSEUDO_CLASS, false);
            assertPart(parts, "img", CSSRule.ELEMENT, true);
            assertTrue("Rule parts for rule '"+rule.getLabel()+
                    "' is not suposed to have more then 6 elements", !parts.hasNext());
        }catch(Exception ex){
            assertTrue("'"+rule.getLabel()+"' is invalid", false);
        }
        
        rule.setLabel("div#global div#main table tr td table tr td");
        parts = rule.getAllParts();
        
        try{
            assertPart(parts, "div", CSSRule.ELEMENT, false);
            assertPart(parts, "global", CSSRule.ID, false);
            assertPart(parts, "div", CSSRule.ELEMENT, false);
            assertPart(parts, "main", CSSRule.ID, false);
            assertPart(parts, "table", CSSRule.ELEMENT, false);
            assertPart(parts, "tr", CSSRule.ELEMENT, false);
            assertPart(parts, "td", CSSRule.ELEMENT, false);
            assertPart(parts, "table", CSSRule.ELEMENT, false);
            assertPart(parts, "tr", CSSRule.ELEMENT, false);
            assertPart(parts, "td", CSSRule.ELEMENT, true);
            assertTrue("Rule parts for rule '"+rule.getLabel()+
                    "' is not suposed to have more then 10 elements", !parts.hasNext());
        }catch(Exception ex){
            assertTrue("'"+rule.getLabel()+"' is invalid", false);
        }
        
    }
    
    
    /**
     * Test of resolveType method, of class com.liguorien.csscompletion.CSSRule.
     */
    public void testResolveType() {
        CSSRule rule = new CSSRule();
        
        String[] testClass = new String[]{
            ".foo", "table tr td.selected", "#main tr td.selected",
            "#main tr td .selected", "#main tr td .selected:hover.foo"
        };
        
        for (int i = 0; i < testClass.length; i++) {
            rule.setLabel(testClass[i]);
            rule.resolveType();
            assertTrue("'"+testClass[i]+"' is not a CLASS",rule.getType() == CSSRule.CLASS);
        }
        
        
        String[] testId = new String[]{
            "#foo", "div#foo", "table tr td#main", "table tr td #main"
        };
        
        for (int i = 0; i < testId.length; i++) {
            rule.setLabel(testId[i]);
            rule.resolveType();
            assertTrue(rule.getType() == CSSRule.ID);
        }
        
        
        String[] testElement = new String[]{
            "div", ".foo div", "table tr td", "#foo div",
            "table tr td a:hover div", "div.foo a", "a"
        };
        
        for (int i = 0; i < testElement.length; i++) {
            rule.setLabel(testElement[i]);
            rule.resolveType();
            assertTrue(rule.getType() == CSSRule.ELEMENT);
        }
        
        
        String[] testPseudoClass = new String[]{
            "a:hover", ".foo a:hover", "#foo:hover", ".foo:hover",
            "table tr td:hover", "#foo div.selected:hover"
        };
        
        for (int i = 0; i < testPseudoClass.length; i++) {
            rule.setLabel(testPseudoClass[i]);
            rule.resolveType();
            assertTrue(rule.getType() == CSSRule.PSEUDO_CLASS);
        }
    }
    
    
    public void testRename(){
        
        CSSRule rule1 = new CSSRule();
        rule1.setLabel("#main table tr td.selected a:hover");
        
        CSSRule rule2 = new CSSRule();
        rule2.setLabel("#main table tr td a:hover");
        
        CSSRule rule3 = new CSSRule();
        rule3.setLabel("#main table.foo tr td a:hover");
        
        CSSRule rule4 = new CSSRule();
        rule4.setLabel("div#main table tr td table tr td");
        
        CSSRule rule5 = new CSSRule();
        rule5.setLabel("div#foo div#main table tr td table tr td");
                
        CSSRulePart replacePart = rule1.buildSingleTree().
                getFirstChild().getFirstChild().getFirstChild();
        
        assertEquals("#main table tr td2.selected a:hover",
                rule1.rename(replacePart, "td2"));
        assertEquals("#main table tr td2 a:hover",
                rule2.rename(replacePart, "td2"));
        assertEquals("#main table.foo tr td a:hover",
                rule3.rename(replacePart, "td2"));       
        assertEquals("div#main table tr td2 table tr td",
                rule4.rename(replacePart, "td2"));
        assertEquals("div#foo div#main table tr td2 table tr td",
                rule5.rename(replacePart, "td2"));
    }
    
    /**
     * Test of getSortLabel method, of class com.liguorien.csscompletion.CSSRule.
     */
    public void testGetSortLabel() {
        CSSRule rule = new CSSRule();
        rule.setLabel(".foo");
        assertEquals("foo", rule.getSortLabel());
        rule = new CSSRule();
        rule.setLabel("foo");
        assertEquals("foo", rule.getSortLabel());
        rule = new CSSRule();
        rule.setLabel("#foo");
        assertEquals("foo", rule.getSortLabel());
        rule = new CSSRule();
        rule.setLabel(".foo:hover");
        assertEquals("foo:hover", rule.getSortLabel());
        rule = new CSSRule();
        rule.setLabel("#foo .foo:hover");
        assertEquals("foo .foo:hover", rule.getSortLabel());
        rule = new CSSRule();
        rule.setLabel("a");
        assertEquals("a", rule.getSortLabel());
    }
    
}
