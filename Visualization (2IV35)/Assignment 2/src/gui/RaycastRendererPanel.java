/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package gui;

import java.awt.BorderLayout;

import volvis.RaycastRenderer;

import javax.swing.*;

/**
 *
 * @author michel
 */


public class RaycastRendererPanel extends javax.swing.JPanel {

    RaycastRenderer renderer;
    TransferFunctionEditor tfEditor;
    public int mip_steps = 20;
    public int composite_steps = 20;
    public int opacity_steps = 20;
    public boolean mip_raycasting;
    public boolean composite_raycasting;
    public boolean opacity_raycasting;
    public boolean surpress_opacity_recalculation;
    public boolean MIP_enabled;
    public boolean COMPOSITE_enabled;
    public boolean SLICER_enabled = true;
    public boolean OPACITY_enabled;

    public enum practical_function{
        SLICE, MIP_RAYCAST, COMPOSITE, OPACITY
    }
    public practical_function current_function = practical_function.SLICE;

    
    /**
     * Creates new form RaycastRendererPanel
     */
    public RaycastRendererPanel(RaycastRenderer renderer) {
        initComponents();
        this.renderer = renderer;
        this.tfPanel.setLayout(new BorderLayout());
    }

    public void setSpeedLabel(String text) {
        renderingSpeedLabel.setText(text);
    }
    
    public void setTransferFunctionEditor(TransferFunctionEditor ed) {
        if (tfEditor != null) {
            tfPanel.remove(tfEditor);
        }
        tfEditor = ed;
        tfPanel.add(ed, BorderLayout.CENTER);
        tfPanel.repaint();
        repaint();
    }

    private void update_UI(String active){



        SLICER_enabled = (active.equals("SLICER"));
        MIP_enabled = (active.equals("MIP"));
        COMPOSITE_enabled = (active.equals("COMPOSITE"));
        OPACITY_enabled = (active.equals("OPACITY"));

        mip_slider.setEnabled(!mip_raycasting && MIP_enabled);
        mip_raycasting_button.setEnabled(MIP_enabled);
        mip_step_button.setEnabled(MIP_enabled);

        composite_slider.setEnabled(!composite_raycasting && COMPOSITE_enabled);
        composite_raycasting_button.setEnabled(COMPOSITE_enabled);
        composite_step_button.setEnabled(COMPOSITE_enabled);

        opacity_slider.setEnabled(OPACITY_enabled);
        opacity_raycasting_button.setEnabled(OPACITY_enabled);
        opacity_step_button.setEnabled(OPACITY_enabled);

        renderer.tFunc.changed();

    }

    private void slicer_buttonActionPerformed(java.awt.event.ActionEvent evt){
        this.current_function = practical_function.SLICE;
        update_UI("SLICER");
    }

    private void mip_buttonActionPerformed(java.awt.event.ActionEvent evt){
        this.current_function = practical_function.MIP_RAYCAST;
        update_UI("MIP");
    }

    private void composite_buttonActionPerformed(java.awt.event.ActionEvent evt){
        this.current_function = practical_function.COMPOSITE;
        update_UI("COMPOSITE");
    }

    private void opacity_buttonActionPerformed(java.awt.event.ActionEvent evt){
        renderer.calculateTransparencies();
        this.current_function = practical_function.OPACITY;
        update_UI("OPACITY");
    }

    private void mip_slider_moved(javax.swing.event.ChangeEvent e) {
        JSlider source = (JSlider) e.getSource();
        if (!source.getValueIsAdjusting()) {
            mip_steps = Math.max(source.getValue(),1);
            renderer.tFunc.changed();
            repaint();
        }
    }

    private void composite_slider_moved(javax.swing.event.ChangeEvent e) {
        JSlider source = (JSlider) e.getSource();
        if (!source.getValueIsAdjusting()) {
            composite_steps = Math.max(source.getValue(),1);
            renderer.tFunc.changed();
            repaint();
        }
    }


    private void mip_raycasting_buttonActionPerformed(java.awt.event.ActionEvent evt){
        this.mip_raycasting = true;
        mip_slider.setEnabled(false);
        renderer.tFunc.changed();
        repaint();
    }


    private void mip_step_buttonActionPerformed(java.awt.event.ActionEvent evt){
        this.mip_raycasting = false;
        mip_slider.setEnabled(true);
        renderer.tFunc.changed();
        repaint();
    }

    private void composite_raycasting_buttonActionPerformed(java.awt.event.ActionEvent evt){
        this.composite_raycasting = true;
        composite_slider.setEnabled(false);
        renderer.tFunc.changed();
        repaint();
    }


    private void composite_step_buttonActionPerformed(java.awt.event.ActionEvent evt){
        this.composite_raycasting = false;
        composite_slider.setEnabled(true);
        renderer.tFunc.changed();
        repaint();
    }

    private void opacity_slider_moved(javax.swing.event.ChangeEvent e) {
        JSlider source = (JSlider) e.getSource();
        if (!source.getValueIsAdjusting()) {
            opacity_steps = Math.max(source.getValue(),1);
            this.surpress_opacity_recalculation = true;
            renderer.tFunc.changed();
            repaint();
        }
    }


    private void opacity_raycasting_buttonActionPerformed(java.awt.event.ActionEvent evt){
        this.opacity_raycasting = true;
        opacity_slider.setEnabled(false);
        this.surpress_opacity_recalculation = true;
        renderer.tFunc.changed();
        repaint();
    }


    private void opacity_step_buttonActionPerformed(java.awt.event.ActionEvent evt){
        this.opacity_raycasting = false;
        opacity_slider.setEnabled(true);
        this.surpress_opacity_recalculation = true;
        renderer.tFunc.changed();
        repaint();
    }


    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jLabel1 = new javax.swing.JLabel();
        renderingSpeedLabel = new javax.swing.JLabel();
        tfPanel = new javax.swing.JPanel();

        jLabel1.setText("Rendering time (ms):");

        renderingSpeedLabel.setText("jLabel2");

        slicer_button = new javax.swing.JButton();
        mip_button = new javax.swing.JButton();
        composite_button = new javax.swing.JButton();
        mip_raycasting_button = new javax.swing.JButton();
        mip_step_button = new javax.swing.JButton();
        composite_raycasting_button = new javax.swing.JButton();
        composite_step_button = new javax.swing.JButton();
        opacity_button = new javax.swing.JButton();
        opacity_raycasting_button = new javax.swing.JButton();
        opacity_step_button = new javax.swing.JButton();


        slicer_button.setText("Slicer");
        mip_button.setText("MIP Raycast");
        composite_button.setText("Composite");
        mip_raycasting_button.setText("Raycasting");
        mip_step_button.setText("Stepwise rendering");
        composite_raycasting_button.setText("Raycasting");
        composite_step_button.setText("Stepwise rendering");
        opacity_button.setText("Opacity Weighting");
        opacity_raycasting_button.setText("Raycasting");
        opacity_step_button.setText("Stepwise rendering");


        slicer_button.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                slicer_buttonActionPerformed(evt);
            }
        });
        mip_button.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                mip_buttonActionPerformed(evt);
            }
        });
        composite_button.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                composite_buttonActionPerformed(evt);
            }
        });
        opacity_button.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                opacity_buttonActionPerformed(evt);
            }
        });
        mip_raycasting_button.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                mip_raycasting_buttonActionPerformed(evt);
            }
        });
        mip_step_button.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                mip_step_buttonActionPerformed(evt);
            }
        });
        composite_raycasting_button.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                composite_raycasting_buttonActionPerformed(evt);
            }
        });
        composite_step_button.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                composite_step_buttonActionPerformed(evt);
            }
        });
        opacity_raycasting_button.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                opacity_raycasting_buttonActionPerformed(evt);
            }
        });
        opacity_step_button.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                opacity_step_buttonActionPerformed(evt);
            }
        });
        // two options: slow, detailed raycasting
        // or fast Object order slicing in parametrized steps

        final int SLIDER_MAX = 200;
        final int SLIDER_MIN = 0;
        final int SLIDER_INIT = 20;

        mip_slider = new javax.swing.JSlider(JSlider.HORIZONTAL, SLIDER_MIN, SLIDER_MAX, SLIDER_INIT);
        composite_slider = new javax.swing.JSlider(JSlider.HORIZONTAL, SLIDER_MIN, SLIDER_MAX, SLIDER_INIT);
        opacity_slider = new javax.swing.JSlider(JSlider.HORIZONTAL, SLIDER_MIN, SLIDER_MAX, SLIDER_INIT);

        mip_slider.setMajorTickSpacing(10);
        mip_slider.setPaintLabels(true);
        mip_slider.setPaintTicks(true);
        mip_slider.setEnabled(false);

        composite_slider.setMajorTickSpacing(10);
        composite_slider.setPaintLabels(true);
        composite_slider.setPaintTicks(true);
        composite_slider.setEnabled(false);

        opacity_slider.setMajorTickSpacing(10);
        opacity_slider.setPaintLabels(true);
        opacity_slider.setPaintTicks(true);
        opacity_slider.setEnabled(false);

        mip_raycasting_button.setEnabled(false);
        mip_step_button.setEnabled(false);
        composite_raycasting_button.setEnabled(false);
        composite_step_button.setEnabled(false);
        opacity_raycasting_button.setEnabled(false);
        opacity_step_button.setEnabled(false);


        mip_slider.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(javax.swing.event.ChangeEvent e) {
                mip_slider_moved(e);
            }
        });

        composite_slider.addChangeListener(new javax.swing.event.ChangeListener(){
            public void stateChanged(javax.swing.event.ChangeEvent e) {
                composite_slider_moved(e);
            }
        });


        opacity_slider.addChangeListener(new javax.swing.event.ChangeListener(){
            public void stateChanged(javax.swing.event.ChangeEvent e) {
                opacity_slider_moved(e);
            }
        });

        // Here come the layout declarations

        javax.swing.GroupLayout tfPanelLayout = new javax.swing.GroupLayout(tfPanel);
        tfPanel.setLayout(tfPanelLayout);
        tfPanelLayout.setHorizontalGroup(
                tfPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addGap(0, 0, Short.MAX_VALUE)
        );
        tfPanelLayout.setVerticalGroup(
                tfPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addGap(0, 0, Short.MAX_VALUE)
        );

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
                layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)

                        .addGroup(layout.createSequentialGroup()
                                .addContainerGap()
                                .addComponent(jLabel1)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(renderingSpeedLabel))
                        .addGroup(layout.createSequentialGroup()
                                .addContainerGap()
                                .addComponent(slicer_button))
                        .addGroup(layout.createSequentialGroup()
                                .addContainerGap()
                                .addComponent(mip_button))
                        .addGroup(layout.createSequentialGroup()
                                .addContainerGap()
                                .addComponent(mip_step_button)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(mip_raycasting_button))
                        .addGroup(layout.createSequentialGroup()
                                .addContainerGap()
                                .addComponent(mip_slider)
                                .addPreferredGap(LayoutStyle.ComponentPlacement.UNRELATED))
                        .addGroup(layout.createSequentialGroup()
                                .addContainerGap()
                                .addComponent(composite_button))
                        .addGroup(layout.createSequentialGroup()
                                .addContainerGap()
                                .addComponent(composite_step_button)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(composite_raycasting_button)
                                .addContainerGap(339, Short.MAX_VALUE))
                        .addGroup(layout.createSequentialGroup()
                                .addContainerGap()
                                .addComponent(composite_slider)
                                .addPreferredGap(LayoutStyle.ComponentPlacement.UNRELATED))
                        .addGroup(layout.createSequentialGroup()
                                .addContainerGap()
                                .addComponent(opacity_button))
                        .addGroup(layout.createSequentialGroup()
                                .addContainerGap()
                                .addComponent(opacity_step_button)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(opacity_raycasting_button)
                                .addContainerGap(339, Short.MAX_VALUE))
                        .addGroup(layout.createSequentialGroup()
                                        .addContainerGap()
                                        .addComponent(opacity_slider)
                                        .addPreferredGap(LayoutStyle.ComponentPlacement.UNRELATED)
                        )
                        .addComponent(tfPanel, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
                layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addGroup(layout.createSequentialGroup()
                                        .addContainerGap()
                                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                                .addComponent(jLabel1)
                                                .addComponent(renderingSpeedLabel))
                                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                                .addComponent(slicer_button)
                                                .addGap(75))
                                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                                .addComponent(mip_button))
                                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                                .addComponent(mip_step_button)
                                                .addComponent(mip_raycasting_button))
                                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                                .addComponent(mip_slider)
                                                .addGap(75))
                                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                                .addComponent(composite_button))
                                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                                .addComponent(composite_step_button)
                                                .addComponent(composite_raycasting_button))
                                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                                .addComponent(composite_slider)
                                                .addGap(75))
                                        .addGroup(layout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                                                .addComponent(opacity_button)
                                                .addGap(30))
                                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                                .addComponent(opacity_step_button)
                                                .addComponent(opacity_raycasting_button))
                                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                                .addComponent(opacity_slider)
                                                .addGap(75))
                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                        .addComponent(tfPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                        );
    }// </editor-fold>//GEN-END:initComponents
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel renderingSpeedLabel;
    private javax.swing.JPanel tfPanel;
    private javax.swing.JButton slicer_button;
    private javax.swing.JButton mip_button;
    private javax.swing.JButton composite_button;
    private javax.swing.JButton opacity_button;
    private javax.swing.JButton mip_raycasting_button;
    private javax.swing.JButton mip_step_button;
    private javax.swing.JSlider mip_slider;
    private javax.swing.JButton composite_raycasting_button;
    private javax.swing.JButton composite_step_button;
    private javax.swing.JSlider composite_slider;
    private javax.swing.JButton opacity_raycasting_button;
    private javax.swing.JButton opacity_step_button;
    private javax.swing.JSlider opacity_slider;
    // End of variables declaration//GEN-END:variables
}
