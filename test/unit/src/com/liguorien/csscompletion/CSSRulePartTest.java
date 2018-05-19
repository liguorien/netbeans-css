/*
 * CSSRulePartTest.java
 * JUnit based test
 *
 * Created on March 5, 2006, 12:37 PM
 */

package com.liguorien.csscompletion;

import junit.framework.*;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import org.openide.filesystems.FileObject;

/**
 *
 * @author Nico
 */
public class CSSRulePartTest extends TestCase {
    
    public CSSRulePartTest(String testName) {
        super(testName);
    }
    
    public static Test suite() {
        TestSuite suite = new TestSuite(CSSRulePartTest.class);
        
        return suite;
    }
    

    /**
     * Test of getDeclaration method, of class com.liguorien.csscompletion.CSSRulePart.
     */
    public void testGetDeclaration() {
        CSSRulePart part = new CSSRulePart();
        part.setName("foo");
        part.setType(CSSRule.ID);
        assertEquals("#foo", part.getDeclaration());
        part.setType(CSSRule.CLASS);
        assertEquals(".foo", part.getDeclaration());
        part.setType(CSSRule.PSEUDO_CLASS);
        assertEquals(":foo", part.getDeclaration());
        part.setType(CSSRule.ELEMENT);
        assertEquals("foo", part.getDeclaration());        
    }
    


    /**
     * Test of toString method, of class com.liguorien.csscompletion.CSSRulePart.
     */
    public void testToString() {
        CSSRulePart part = new CSSRulePart();
        part.setName("foo");        
        assertEquals(part.toString(), "foo");
    }
       
    /**
     * Test of equals method, of class com.liguorien.csscompletion.CSSRulePart.
     */
    public void testEquals() {
        
        CSSRulePart part = new CSSRulePart();
        part.setName("foo");
        part.setType(CSSRule.CLASS);        
        
        CSSRulePart part2 = new CSSRulePart();
        part2.setName("foo");
        part2.setType(CSSRule.CLASS);
        
        CSSRulePart part3 = new CSSRulePart();
        part3.setName("a");
        part3.setType(CSSRule.ELEMENT);
        
        CSSRulePart part4 = new CSSRulePart();
        part4.setName("foo");
        part4.setType(CSSRule.ID);
    
        assertTrue(part.equals(part2));
        assertFalse(part.equals(part3));
        assertFalse(part.equals(part4));
    }
    
    
    public void testGetFirstChild(){
        CSSRulePart part = new CSSRulePart();
        part.setName("foo");
        part.setType(CSSRule.CLASS);        
        
        CSSRulePart part2 = new CSSRulePart();
        part2.setName("foo");
        part2.setType(CSSRule.CLASS);
        
        CSSRulePart part3 = new CSSRulePart();
        part3.setName("a");
        part3.setType(CSSRule.ELEMENT);
             
        CSSRulePart part4 = new CSSRulePart();
        part4.setName("div");
        part4.setType(CSSRule.ELEMENT);
        
        part.addChild(part2);
        part.addChild(part3);
        part2.addChild(part4);
        
        assertEquals(part.getFirstChild(), part2);
        assertEquals(part2.getFirstChild(), part4);
        assertNull(part4.getFirstChild());      
    }
    
    /**
     * Test of isSamePath method, of class com.liguorien.csscompletion.CSSRulePart.
     */
    public void testIsSamePath() {
        
        CSSRule rule1 = new CSSRule();
        rule1.setLabel("#main table tr td.selected a:hover");
        
        CSSRule rule2 = new CSSRule();
        rule2.setLabel("#main table tr td a:hover");
            
        CSSRule rule3 = new CSSRule();
        rule3.setLabel("#main table.foo tr td a:hover");
        
        CSSRule rule4 = new CSSRule();
        rule4.setLabel("#main table tr td table tr td");
          
        CSSRulePart td1 = rule1.buildSingleTree().getFirstChild().
                getFirstChild().getFirstChild();   
        
        CSSRulePart td2 = rule2.buildSingleTree().getFirstChild().
                getFirstChild().getFirstChild();
        
        CSSRulePart td3 = rule3.buildSingleTree().getFirstChild().
                getFirstChild().getFirstChild().getFirstChild();
        
        CSSRulePart td4 = rule4.buildSingleTree().getFirstChild().
                getFirstChild().getFirstChild().getFirstChild().
                getFirstChild().getFirstChild();
        
        assertEquals("td", td1.getName());
        assertEquals(CSSRule.ELEMENT, td1.getType());
        assertEquals("td", td2.getName());
        assertEquals(CSSRule.ELEMENT, td2.getType());
        assertEquals("td", td3.getName());
        assertEquals(CSSRule.ELEMENT, td3.getType());
        assertEquals("td", td4.getName());
        assertEquals(CSSRule.ELEMENT, td4.getType());
        assertTrue(td1.isSamePath(td2));
        assertTrue(td2.isSamePath(td1));
        assertFalse(td1.isSamePath(td3));
        assertFalse(td2.isSamePath(td3));
        assertFalse(td3.isSamePath(td2));
        assertFalse(td3.isSamePath(td1));
        assertFalse(td1.isSamePath(td4));
        assertFalse(td2.isSamePath(td4));
        assertFalse(td3.isSamePath(td4));
        assertTrue(((CSSRulePart)rule1.getAllParts().next()).isSamePath(
                (CSSRulePart)rule2.getAllParts().next()));
        assertTrue(((CSSRulePart)rule2.getAllParts().next()).isSamePath(
                (CSSRulePart)rule3.getAllParts().next()));
        assertTrue(((CSSRulePart)rule1.getAllParts().next()).isSamePath(
                (CSSRulePart)rule3.getAllParts().next()));
    }
    
    
    
    /**
     * Test of addChild method, of class com.liguorien.csscompletion.CSSRulePart.
     */
    public void testAddChild() {
        CSSRulePart part = new CSSRulePart();
        part.setName("#main");
        part.setType(CSSRule.ID);
        
        CSSRulePart part2 = new CSSRulePart();
        part2.setName("foo");
        part2.setType(CSSRule.CLASS);
        
        CSSRulePart part3 = new CSSRulePart();
        part3.setName("a");
        part3.setType(CSSRule.ELEMENT);
        
        assertNull("Root part is not suposed to have any child", part.getChildren());
        
        part.addChild(part2);
        part.addChild(part3);
        
        assertEquals("Root part is suposed to have only 2 children", 2, part.getChildren().size());
    }
    
    
    /**
     * Test of removeChild method, of class com.liguorien.csscompletion.CSSRulePart.
     */
    public void testRemoveChild() {
        CSSRulePart part = new CSSRulePart();
        part.setName("#main");
        part.setType(CSSRule.ID);
        
        CSSRulePart part2 = new CSSRulePart();
        part2.setName("foo");
        part2.setType(CSSRule.CLASS);
        
        CSSRulePart part3 = new CSSRulePart();
        part3.setName("a");
        part3.setType(CSSRule.ELEMENT);
        
        part.addChild(part2);
        part.addChild(part3);
        
        
        assertEquals("Root part is suposed to have only 2 children", 2, part.getChildren().size());
        
        part.removeChild(part3);
        
        assertEquals("Root part is suposed to have only 2 child", 1, part.getChildren().size());
        assertEquals(part.getChildren().get(0), part2);
    }
    
    /**
     * Test of findChild method, of class com.liguorien.csscompletion.CSSRulePart.
     */
    public void testFindChild() {
        CSSRulePart part = new CSSRulePart();
        part.setName("#main");
        part.setType(CSSRule.ID);
        
        CSSRulePart part2 = new CSSRulePart();
        part2.setName("foo");
        part2.setType(CSSRule.CLASS);
        
        CSSRulePart part3 = new CSSRulePart();
        part3.setName("a");
        part3.setType(CSSRule.ELEMENT);
        
        CSSRulePart part4 = new CSSRulePart();
        part4.setName("div");
        part4.setType(CSSRule.ELEMENT);
        
        CSSRulePart part5 = new CSSRulePart();
        part5.setName("a");
        part5.setType(CSSRule.ELEMENT);
        
        part.addChild(part2);
        part.addChild(part3);
        
        assertNotNull(part.findChild(part2));
        assertNotNull(part.findChild(part3));
        assertNull(part.findChild(part4));
        assertNotNull(part.findChild(part5));
    }
    
    
    /**
     * Test of getDependenciesCount method, of class com.liguorien.csscompletion.CSSRulePart.
     */
    public void testGetDependenciesCount() {
        CSSRulePart part = new CSSRulePart();
        part.setName("foo");
        part.setType(CSSRule.ELEMENT);
        
        CSSRule rule = new CSSRule();
        rule.setLabel("#foo table tr td");
        rule.setLineNumber(1);
        rule.setLeaf(false);
        rule.setType(CSSRule.ELEMENT);
        
        CSSRule rule2 = new CSSRule();
        rule2.setLabel("#foo table tr td a");
        rule2.setLineNumber(12);
        rule2.setLeaf(true);
        rule2.setType(CSSRule.ELEMENT);
        
        CSSRule rule3 = new CSSRule();
        rule3.setLabel("#foo table tr td");
        rule3.setLineNumber(1);
        rule3.setLeaf(false);
        rule3.setType(CSSRule.ELEMENT);
        
        assertEquals("Rule part is not suposed to have any dependecies", 0, part.getDependenciesCount());
        part.addDependency(rule);
        assertEquals("Rule part is suposed to have only one dependecy", 1, part.getDependenciesCount());
        part.addDependency(rule3);
        assertEquals("Rule part is suposed to have only one dependecy", 1, part.getDependenciesCount());
        part.addDependency(rule3);
        assertEquals("Rule part is suposed to have only one dependecy", 1, part.getDependenciesCount());
        part.addDependency(rule2);
        assertEquals("Rule part is suposed to have only two dependecies", 2, part.getDependenciesCount());
        
    }
    
    /**
     * Test of hasConfirmed method, of class com.liguorien.csscompletion.CSSRulePart.
     */
    public void testHasConfirmed() {
        CSSRulePart part = new CSSRulePart();
        part.setName("foo");
        part.setType(CSSRule.ELEMENT);
        
        part.resetPresenceConfirmation();
        
        assertFalse("Rule part confirmation is suposed to be false", part.hasConfirmed());
        
        part.confirmPresence();
        
        assertTrue("Rule part confirmation is suposed to be true", part.hasConfirmed());
        
    }
    
    /**
     * Test of confirmPresence method, of class com.liguorien.csscompletion.CSSRulePart.
     */
    public void testConfirmPresence() {
        CSSRulePart part = new CSSRulePart();
        part.setName("foo");
        part.setType(CSSRule.ELEMENT);
        
        part.resetPresenceConfirmation();
        
        assertFalse("Rule part confirmation is suposed to be false", part.hasConfirmed());
        
        part.confirmPresence();
        
        assertTrue("Rule part confirmation is suposed to be true", part.hasConfirmed());
    }
    
    /**
     * Test of resetPresenceConfirmation method, of class com.liguorien.csscompletion.CSSRulePart.
     */
    public void testResetPresenceConfirmation() {
        CSSRulePart part = new CSSRulePart();
        part.setName("foo");
        part.setType(CSSRule.ELEMENT);
        
        CSSRulePart part2 = new CSSRulePart();
        part2.setName("foo2");
        part2.setType(CSSRule.ELEMENT);
        
        part.addChild(part2);
        
        part.confirmPresence();
        part2.confirmPresence();
        
        assertTrue("Presence confirmation of root part is suposed to be true",
                part.hasConfirmed());
        assertTrue("Presence confirmation of child part is suposed to be true",
                part2.hasConfirmed());
        
        part.resetPresenceConfirmation();
        
        assertFalse("Presence confirmation of root part is suposed to be false",
                part.hasConfirmed());
        assertFalse("Presence confirmation of child part is suposed to be false",
                part2.hasConfirmed());
        
    }
    
    /**
     * Test of removeChildWithoutConfirmation method, of class com.liguorien.csscompletion.CSSRulePart.
     */
    public void testRemoveChildWithoutConfirmation() {
        CSSRulePart part = new CSSRulePart();
        part.setName("foo");
        part.setType(CSSRule.ELEMENT);
        
        CSSRulePart part2 = new CSSRulePart();
        part2.setName("foo2");
        part2.setType(CSSRule.ELEMENT);
        
        CSSRulePart part3 = new CSSRulePart();
        part3.setName("foo3");
        part3.setType(CSSRule.ELEMENT);
        
        part.addChild(part2);
        part.addChild(part3);
        
        part.resetPresenceConfirmation();
        
        part.confirmPresence();
        part2.confirmPresence();
        
        part.removeChildWithoutConfirmation();
        
        assertEquals("The root part is suposed to contains only 1 child", 1, part.getChildren().size());
        assertEquals("The root part's child is suposed to be part2", part.getChildren().get(0), part2);
    }
}
