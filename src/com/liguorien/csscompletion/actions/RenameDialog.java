/*
 * RenameDialog.java
 *
 * Created on February 26, 2006, 2:30 PM
 */

package com.liguorien.csscompletion.actions;

import org.openide.util.NbBundle;

/**
 *
 * @author  Nicolas D�sy
 */
public class RenameDialog extends javax.swing.JPanel {
    
    /** Creates new form RenameDialog */
    public RenameDialog(String oldName) {
        initComponents();
        nameTxt.setText(oldName);
        nameTxt.setSelectionStart(0);
        nameTxt.setSelectionEnd(oldName.length());
    }
    
    public String getNewName(){
        return nameTxt.getText();
    }
    
    
    
    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc=" Generated Code ">//GEN-BEGIN:initComponents
    private void initComponents() {
        label = new javax.swing.JLabel();
        nameTxt = new javax.swing.JTextField();

        label.setText(NbBundle.getMessage(RenameDialog.class, "LBL_NewName"));

        org.jdesktop.layout.GroupLayout layout = new org.jdesktop.layout.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(org.jdesktop.layout.GroupLayout.TRAILING, layout.createSequentialGroup()
                .addContainerGap()
                .add(label)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(nameTxt, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 164, Short.MAX_VALUE)
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .addContainerGap()
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(label)
                    .add(nameTxt, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
    }// </editor-fold>//GEN-END:initComponents
    
    
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JLabel label;
    private javax.swing.JTextField nameTxt;
    // End of variables declaration//GEN-END:variables
    
}