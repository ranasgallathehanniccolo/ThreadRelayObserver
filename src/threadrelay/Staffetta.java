/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/GUIForms/JFrame.java to edit this template
 */
package threadrelay;
 
import javax.swing.*;
 
/**
 * ConcreteObserver del Pattern Observer.
 *
 * Staffetta ora implementa CorridoreObserver: si registra come observer
 * su ogni Corridore e riceve le notifiche direttamente tramite
 * onAggiornamento() e onFine(), senza più usare la classe anonima Ascoltatore.
 *
 * Questo riduce l'accoppiamento: Corridore non conosce Staffetta,
 * conosce solo l'interfaccia CorridoreObserver.
 */
public class Staffetta extends javax.swing.JFrame implements CorridoreObserver {
 
    private static final java.util.logging.Logger logger =
            java.util.logging.Logger.getLogger(Staffetta.class.getName());
 
    private javax.swing.JProgressBar[] barre;
    private javax.swing.JLabel[]       labelContatore;
 
    public Staffetta() {
        initComponents();
 
        barre = new javax.swing.JProgressBar[]{
            jProgressBar1, jProgressBar2, jProgressBar3, jProgressBar4
        };
        labelContatore = new javax.swing.JLabel[]{
            lblPercentuale1, lblPercentuale2, lblPercentuale3, lblPercentuale4
        };
 
        jLabel1.setText("Corridore 1");
        jLabel3.setText("Corridore 2");
        jLabel5.setText("Corridore 3");
        jLabel9.setText("Corridore 4");
 
        for (int i = 0; i < 4; i++) {
            barre[i].setMinimum(0);
            barre[i].setMaximum(99);
            barre[i].setValue(0);
            barre[i].setStringPainted(true);
            barre[i].setString("");
            labelContatore[i].setText("—");
        }
 
        cmbVelocita.setModel(new javax.swing.DefaultComboBoxModel<>(
                new String[]{"Veloce", "Normale", "Lento"}));
    }
 
    private Corridore c1, c2, c3, c4;
 
    // ════════════ Implementazione di CorridoreObserver ════════════
 
    /**
     * Chiamato dal thread del Corridore ad ogni passo.
     * IMPORTANTE: siamo su un thread secondario → usiamo invokeLater per Swing.
     */
    @Override
    public void onAggiornamento(int id, int count) {
        SwingUtilities.invokeLater(() -> {
            int idx = id - 1;
            barre[idx].setValue(count);
            barre[idx].setString(String.valueOf(count));
            labelContatore[idx].setText(String.valueOf(count));
        });
    }
 
    /**
     * Chiamato dal thread del Corridore quando termina la sua frazione.
     */
    @Override
    public void onFine(int id) {
        SwingUtilities.invokeLater(() -> {
            int idx = id - 1;
            barre[idx].setString("Fine");
            labelContatore[idx].setText("Fine");
        });
    }
 
    // ════════════ Logica avvia/pausa/riprendi/interrompi ════════════
 
    private void avvia() {
        btnPausa.setEnabled(true);
        btnRiprendi.setEnabled(false);
        btnInterrompi.setEnabled(false);
 
        int delay = switch ((String) cmbVelocita.getSelectedItem()) {
            case "Veloce"  -> 20;
            case "Normale" -> 60;
            default        -> 120;
        };
 
        // Reset UI
        for (int i = 0; i < 4; i++) {
            barre[i].setValue(0);
            barre[i].setString("");
            labelContatore[i].setText("—");
        }
 
        btnAvvia.setEnabled(false);
        btnInterrompi.setEnabled(false);
        cmbVelocita.setEnabled(false);
 
        // Crea i corridori in ordine inverso (catena di sblocco)
        c4 = new Corridore(4, delay, null);
        c3 = new Corridore(3, delay, c4);
        c2 = new Corridore(2, delay, c3);
        c1 = new Corridore(1, delay, c2);
 
        // Staffetta (this) si registra come Observer su ogni Corridore
        // Nessuna classe anonima: siamo noi stessi l'observer!
        c1.addObserver(this);
        c2.addObserver(this);
        c3.addObserver(this);
        c4.addObserver(this);
 
        // Avvia i thread
        new Thread(c1, "Corridore-1").start();
        new Thread(c2, "Corridore-2").start();
        new Thread(c3, "Corridore-3").start();
        new Thread(c4, "Corridore-4").start();
 
        c1.allowStart();
 
        // Thread sentinella: aspetta che c4 finisca, poi riabilita l'UI
        new Thread(() -> {
            synchronized (c4) {
                while (!c4.isFinished()) {
                    try {
                        c4.wait();
                    } catch (InterruptedException e) {
                        return;
                    }
                }
            }
            SwingUtilities.invokeLater(() -> {
                btnAvvia.setEnabled(true);
                btnPausa.setEnabled(false);
                btnRiprendi.setEnabled(false);
                cmbVelocita.setEnabled(true);
            });
        }).start();
    }
 
    private void pausa() {
        if (c1 != null) c1.pause();
        if (c2 != null) c2.pause();
        if (c3 != null) c3.pause();
        if (c4 != null) c4.pause();
        btnPausa.setEnabled(false);
        btnRiprendi.setEnabled(true);
        btnInterrompi.setEnabled(true);
    }
 
    private void riprendi() {
        if (c1 != null) c1.resume();
        if (c2 != null) c2.resume();
        if (c3 != null) c3.resume();
        if (c4 != null) c4.resume();
        btnPausa.setEnabled(true);
        btnRiprendi.setEnabled(false);
        btnInterrompi.setEnabled(false);
    }
 
    private void interrompi() {
        if (c1 != null) c1.stop();
        if (c2 != null) c2.stop();
        if (c3 != null) c3.stop();
        if (c4 != null) c4.stop();
        for (int i = 0; i < 4; i++) {
            barre[i].setValue(0);
            barre[i].setString("");
            labelContatore[i].setText("—");
        }
        btnAvvia.setEnabled(true);
        btnPausa.setEnabled(false);
        btnRiprendi.setEnabled(false);
        btnInterrompi.setEnabled(false);
        cmbVelocita.setEnabled(true);
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jProgressBar2 = new javax.swing.JProgressBar();
        jProgressBar4 = new javax.swing.JProgressBar();
        jProgressBar1 = new javax.swing.JProgressBar();
        jProgressBar3 = new javax.swing.JProgressBar();
        jPanel1 = new javax.swing.JPanel();
        jLabel1 = new javax.swing.JLabel();
        lblPercentuale1 = new javax.swing.JLabel();
        jPanel2 = new javax.swing.JPanel();
        jLabel3 = new javax.swing.JLabel();
        lblPercentuale2 = new javax.swing.JLabel();
        jPanel3 = new javax.swing.JPanel();
        jLabel5 = new javax.swing.JLabel();
        lblPercentuale3 = new javax.swing.JLabel();
        jPanel4 = new javax.swing.JPanel();
        jLabel9 = new javax.swing.JLabel();
        lblPercentuale4 = new javax.swing.JLabel();
        btnAvvia = new javax.swing.JButton();
        cmbVelocita = new javax.swing.JComboBox<>();
        btnPausa = new javax.swing.JButton();
        btnRiprendi = new javax.swing.JButton();
        btnInterrompi = new javax.swing.JButton();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        getContentPane().setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());
        getContentPane().add(jProgressBar2, new org.netbeans.lib.awtextra.AbsoluteConstraints(20, 70, 420, 50));
        getContentPane().add(jProgressBar4, new org.netbeans.lib.awtextra.AbsoluteConstraints(20, 190, 420, 50));
        getContentPane().add(jProgressBar1, new org.netbeans.lib.awtextra.AbsoluteConstraints(20, 10, 420, 50));
        getContentPane().add(jProgressBar3, new org.netbeans.lib.awtextra.AbsoluteConstraints(20, 130, 420, 50));

        jLabel1.setText("Runner1");

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(jLabel1)
                .addGap(18, 18, 18)
                .addComponent(lblPercentuale1)
                .addContainerGap())
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(lblPercentuale1, javax.swing.GroupLayout.DEFAULT_SIZE, 38, Short.MAX_VALUE)
                    .addComponent(jLabel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addContainerGap())
        );

        getContentPane().add(jPanel1, new org.netbeans.lib.awtextra.AbsoluteConstraints(450, 10, 120, 50));

        jLabel3.setText("Runner2");

        javax.swing.GroupLayout jPanel2Layout = new javax.swing.GroupLayout(jPanel2);
        jPanel2.setLayout(jPanel2Layout);
        jPanel2Layout.setHorizontalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(jLabel3)
                .addGap(18, 18, 18)
                .addComponent(lblPercentuale2)
                .addContainerGap())
        );
        jPanel2Layout.setVerticalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel2Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(lblPercentuale2, javax.swing.GroupLayout.DEFAULT_SIZE, 38, Short.MAX_VALUE)
                    .addComponent(jLabel3, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addContainerGap())
        );

        getContentPane().add(jPanel2, new org.netbeans.lib.awtextra.AbsoluteConstraints(450, 70, 120, 50));

        jLabel5.setText("Runner3");

        javax.swing.GroupLayout jPanel3Layout = new javax.swing.GroupLayout(jPanel3);
        jPanel3.setLayout(jPanel3Layout);
        jPanel3Layout.setHorizontalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel3Layout.createSequentialGroup()
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(jLabel5)
                .addGap(18, 18, 18)
                .addComponent(lblPercentuale3)
                .addContainerGap())
        );
        jPanel3Layout.setVerticalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel3Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(lblPercentuale3, javax.swing.GroupLayout.DEFAULT_SIZE, 38, Short.MAX_VALUE)
                    .addComponent(jLabel5, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addContainerGap())
        );

        getContentPane().add(jPanel3, new org.netbeans.lib.awtextra.AbsoluteConstraints(450, 130, 120, 50));

        jLabel9.setText("Runner4");

        javax.swing.GroupLayout jPanel4Layout = new javax.swing.GroupLayout(jPanel4);
        jPanel4.setLayout(jPanel4Layout);
        jPanel4Layout.setHorizontalGroup(
            jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel4Layout.createSequentialGroup()
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(jLabel9)
                .addGap(18, 18, 18)
                .addComponent(lblPercentuale4)
                .addContainerGap())
        );
        jPanel4Layout.setVerticalGroup(
            jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel4Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(lblPercentuale4, javax.swing.GroupLayout.DEFAULT_SIZE, 38, Short.MAX_VALUE)
                    .addComponent(jLabel9, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addContainerGap())
        );

        getContentPane().add(jPanel4, new org.netbeans.lib.awtextra.AbsoluteConstraints(450, 190, 120, 50));

        btnAvvia.setText("Avvia");
        btnAvvia.addActionListener(this::btnAvviaActionPerformed);
        getContentPane().add(btnAvvia, new org.netbeans.lib.awtextra.AbsoluteConstraints(250, 250, -1, -1));

        getContentPane().add(cmbVelocita, new org.netbeans.lib.awtextra.AbsoluteConstraints(20, 250, -1, -1));

        btnPausa.setText("Pausa");
        btnPausa.addActionListener(this::btnPausaActionPerformed);
        getContentPane().add(btnPausa, new org.netbeans.lib.awtextra.AbsoluteConstraints(370, 250, -1, -1));

        btnRiprendi.setText("Riprendi");
        btnRiprendi.addActionListener(this::btnRiprendiActionPerformed);
        getContentPane().add(btnRiprendi, new org.netbeans.lib.awtextra.AbsoluteConstraints(120, 250, -1, -1));

        btnInterrompi.setText("Interrompi");
        btnInterrompi.addActionListener(this::btnInterrompiActionPerformed);
        getContentPane().add(btnInterrompi, new org.netbeans.lib.awtextra.AbsoluteConstraints(470, 250, -1, -1));

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void btnAvviaActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnAvviaActionPerformed
        // TODO add your handling code here:
        avvia();
    }//GEN-LAST:event_btnAvviaActionPerformed

    private void btnPausaActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnPausaActionPerformed
        // TODO add your handling code here:
        pausa();
    }//GEN-LAST:event_btnPausaActionPerformed

    private void btnRiprendiActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnRiprendiActionPerformed
        // TODO add your handling code here:
        riprendi();
    }//GEN-LAST:event_btnRiprendiActionPerformed

    private void btnInterrompiActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnInterrompiActionPerformed
        // TODO add your handling code here:
        interrompi();
    }//GEN-LAST:event_btnInterrompiActionPerformed

    /**
     * @param args the command line arguments
     */
    public static void main(String args[]) {
        /* Set the Nimbus look and feel */
        //<editor-fold defaultstate="collapsed" desc=" Look and feel setting code (optional) ">
        /* If Nimbus (introduced in Java SE 6) is not available, stay with the default look and feel.
         * For details see http://download.oracle.com/javase/tutorial/uiswing/lookandfeel/plaf.html 
         */
        try {
            for (javax.swing.UIManager.LookAndFeelInfo info : javax.swing.UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    javax.swing.UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (ReflectiveOperationException | javax.swing.UnsupportedLookAndFeelException ex) {
            logger.log(java.util.logging.Level.SEVERE, null, ex);
        }
        //</editor-fold>

        /* Create and display the form */
        java.awt.EventQueue.invokeLater(() -> new Staffetta().setVisible(true));
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btnAvvia;
    private javax.swing.JButton btnInterrompi;
    private javax.swing.JButton btnPausa;
    private javax.swing.JButton btnRiprendi;
    private javax.swing.JComboBox<String> cmbVelocita;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel jLabel9;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JPanel jPanel3;
    private javax.swing.JPanel jPanel4;
    private javax.swing.JProgressBar jProgressBar1;
    private javax.swing.JProgressBar jProgressBar2;
    private javax.swing.JProgressBar jProgressBar3;
    private javax.swing.JProgressBar jProgressBar4;
    private javax.swing.JLabel lblPercentuale1;
    private javax.swing.JLabel lblPercentuale2;
    private javax.swing.JLabel lblPercentuale3;
    private javax.swing.JLabel lblPercentuale4;
    // End of variables declaration//GEN-END:variables
}
