/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package volvis;

import com.jogamp.opengl.util.texture.Texture;
import com.jogamp.opengl.util.texture.awt.AWTTextureIO;
import gui.TransferFunctionEditorPanel;
import gui.RaycastRendererPanel;
import gui.TransferFunctionEditor;
import java.awt.image.BufferedImage;
import java.util.TreeMap;
import javax.media.opengl.GL2;

import runnables.*;
import util.TFChangeListener;
import util.VectorMath;
import volume.Volume;

/**
 *
 * @author michel
 */
public class RaycastRenderer extends Renderer implements TFChangeListener {

    public Volume volume = null;
    public RaycastRendererPanel panel;
    TransferFunctionEditorPanel ow_panel;
    public TransferFunction tFunc;
    TransferFunctionEditor tfEditor;
    TreeMap<Short, Double> opacity_types = new TreeMap<Short, Double>();
    public int NUM_THREADS = 8;

    public Double[][][] transparencies;


    public RaycastRenderer() {
        panel = new RaycastRendererPanel(this);
        ow_panel = new TransferFunctionEditorPanel(this);
        panel.setSpeedLabel("0");
        ow_panel.setSpeedLabel("0");
    }

    public void setVolume(Volume vol) {
        volume = vol;

        // set up image for storing the resulting rendering
        // the image width and height are equal to the length of the volume diagonal
        int imageSize = (int) Math.floor(Math.sqrt(vol.getDimX() * vol.getDimX() + vol.getDimY() * vol.getDimY()
                + vol.getDimZ() * vol.getDimZ()));
        if (imageSize % 2 != 0) {
            imageSize = imageSize + 1;
        }
        image = new BufferedImage(imageSize, imageSize, BufferedImage.TYPE_INT_ARGB);
        tFunc = new TransferFunction(volume.getMinimum(), volume.getMaximum());
        tFunc.addTFChangeListener(this);
        tfEditor = new TransferFunctionEditor(tFunc, volume.getHistogram());
        panel.setTransferFunctionEditor(tfEditor);
        ow_panel.setTransferFunctionEditor(tfEditor);

        // create a parallel 3D representation of the voxels' transparencies for the Opacity Weighting
        // (this implementation is only slightly slower than single array)
        transparencies = new Double[volume.getDimX()][volume.getDimY()][volume.getDimZ()];


    }

    @Override
    public void changed() {
        for (int i = 0; i < listeners.size(); i++) {
            listeners.get(i).changed();
        }

        if(panel.OPACITY_enabled && panel.surpress_opacity_recalculation) calculateTransparencies();
        panel.surpress_opacity_recalculation = false;
    }

    public void clearImage (){
        // clear image
        for (int j = 0; j < image.getHeight(); j++) {
            for (int i = 0; i < image.getWidth(); i++) {
                image.setRGB(i, j, 0);
            }
        }
    }

    public RaycastRendererPanel getPanel() {
        return panel;
    }

    public TransferFunctionEditorPanel getOwPanel(){
        return ow_panel;
    }

    // get a voxel from the volume data by nearest neighbor interpolation
     public short getVoxel(double[] coord) {

        int x = (int) Math.round(coord[0]);
        int y = (int) Math.round(coord[1]);
        int z = (int) Math.round(coord[2]);

        if ((x >= 0) && (x < volume.getDimX()) && (y >= 0) && (y < volume.getDimY())
                && (z >= 0) && (z < volume.getDimZ())) {
            return volume.getVoxel(x, y, z);
        } else {
            return 0;
        }
    }



    // get a voxel from the volume data by trilinear interpolation
    public short getVoxel_trilinear(double[] coord){

        int[][] neighbor = new int[2][3];

        // Get the 8 corners of the cube this voxel is in (0 is low, 1 is high)
        neighbor[0][0] = (int) Math.floor(coord[0]);
        neighbor[0][1] = (int) Math.floor(coord[1]);
        neighbor[0][2] = (int) Math.floor(coord[2]);
        neighbor[1][0] = (int) Math.ceil(coord[0]);
        neighbor[1][1] = (int) Math.ceil(coord[1]);
        neighbor[1][2] = (int) Math.ceil(coord[2]);



        // Initialize the values for the surrounding voxels
        short[] neihgbor_values = new short[8];
        for(int x = 0; x <=1; x++){
            for(int y = 0; y <=1; y++){
                for(int z = 0; z <=1; z++) {


                    if ((neighbor[x][0] >= 0) && (neighbor[x][0] < volume.getDimX()) && (neighbor[y][1] >= 0) && (neighbor[y][1] < volume.getDimY())
                            && (neighbor[z][2] >= 0) && (neighbor[z][2] < volume.getDimZ())) {
                        neihgbor_values[x * 4 + y * 2 + z] = volume.getVoxel(neighbor[x][0], neighbor[y][1], neighbor[z][2]);
                    }
                    else{
                        neihgbor_values[x * 4 + y * 2 + z] = 0;
                    }
                }
            }
        }

        short result = 0;

        for(int x = 0; x <=1; x++){
            for(int y = 0; y <=1; y++){
                for(int z = 0; z <=1; z++) {
                    // multiply the value of the neighbor with the volume of the area across (tri-linear)
                    result += neihgbor_values[x*4+y*2+z] *
                            ((1-2*x) * (neighbor[1-x][0] - coord[0])) *
                            ((1-2*y) * (neighbor[1-y][1] - coord[1])) *
                            ((1-2*z) * (neighbor[1-z][2] - coord[2]));
                }
            }
        }

        return result;
    }


    void slicer(double[] viewMatrix) {

        this.clearImage();

        // vector uVec and vVec define a plane through the origin, 
        // perpendicular to the view vector viewVec
        double[] viewVec = new double[3];
        double[] uVec = new double[3];
        double[] vVec = new double[3];
        VectorMath.setVector(viewVec, viewMatrix[2], viewMatrix[6], viewMatrix[10]);
        VectorMath.setVector(uVec, viewMatrix[0], viewMatrix[4], viewMatrix[8]);
        VectorMath.setVector(vVec, viewMatrix[1], viewMatrix[5], viewMatrix[9]);

        // image is square
        int imageCenter = image.getWidth() / 2;

        double[] pixelCoord = new double[3];
        double[] volumeCenter = new double[3];
        VectorMath.setVector(volumeCenter, (double)volume.getDimX() / 2, (double)volume.getDimY() / 2, (double)volume.getDimZ() / 2);

        // sample on a plane through the origin of the volume data
        for (int j = 0; j < image.getHeight(); j++) {
            for (int i = 0; i < image.getWidth(); i++) {
                pixelCoord[0] = uVec[0] * (i - imageCenter) + vVec[0] * (j - imageCenter)
                        + volumeCenter[0];
                pixelCoord[1] = uVec[1] * (i - imageCenter) + vVec[1] * (j - imageCenter)
                        + volumeCenter[1];
                pixelCoord[2] = uVec[2] * (i - imageCenter) + vVec[2] * (j - imageCenter)
                        + volumeCenter[2];

                int val = getVoxel(pixelCoord);
                // Apply the transfer function to obtain a color
                TFColor voxelColor = tFunc.getColor(val);
                
                // BufferedImage expects a pixel color packed as ARGB in an int
                int c_alpha = voxelColor.a <= 1.0 ? (int) Math.floor(voxelColor.a * 255) : 255;
                int c_red = voxelColor.r <= 1.0 ? (int) Math.floor(voxelColor.r * 255) : 255;
                int c_green = voxelColor.g <= 1.0 ? (int) Math.floor(voxelColor.g * 255) : 255;
                int c_blue = voxelColor.b <= 1.0 ? (int) Math.floor(voxelColor.b * 255) : 255;
                int pixelColor = (c_alpha << 24) | (c_red << 16) | (c_green << 8) | c_blue;
                image.setRGB(i, j, pixelColor);
            }
        }


    }

    void mip_raycast(double[] viewMatrix) {

        this.clearImage();

        // vector uVec and vVec define a plane through the origin,
        // perpendicular to the view vector viewVec
        double[] viewVec = new double[3];
        double[] uVec = new double[3];
        double[] vVec = new double[3];
        VectorMath.setVector(viewVec, viewMatrix[2], viewMatrix[6], viewMatrix[10]);
        VectorMath.setVector(uVec, viewMatrix[0], viewMatrix[4], viewMatrix[8]);
        VectorMath.setVector(vVec, viewMatrix[1], viewMatrix[5], viewMatrix[9]);

        // Unit viewVec
        double[] normalVec = VectorMath.normalize(viewVec);

        // image is square
        int imageCenter = image.getWidth() / 2;

        double[] pixelCoord = new double[3];
        double[] volumeCenter = new double[3];

        VectorMath.setVector(volumeCenter, (double) volume.getDimX() / 2, (double) volume.getDimY() / 2, (double) volume.getDimZ() / 2);

        double max;
        double[] cornerVector = new double[3];
        double[] planeNormal = new double[3];
        planeNormal = VectorMath.crossproduct(uVec, vVec, planeNormal);

        double temp_val;
        double temp_max = -Double.MAX_VALUE;
        double temp_min = Double.MAX_VALUE;

        // Calculate the largest distance from the viewplane through the entire volume

        for (int i = 0; i <= 1; i++) {
            for (int j = 0; j <= 1; j++) {
                for (int z = 0; z <= 1; z++) {
                    VectorMath.setVector(cornerVector, volume.getDimX() * i, volume.getDimY() * j, volume.getDimZ() * z);
                    temp_val = VectorMath.dotproduct(VectorMath.scalarMultiply(-1.0d, cornerVector), planeNormal);

                    if (temp_val > temp_max) temp_max = temp_val;
                    if (temp_val < temp_min) temp_min = temp_val;


                }
            }
        }

        max = temp_max - temp_min;

        Thread[] threads = new Thread[NUM_THREADS];

        for (int t = 0; t < threads.length; t++) {
            threads[t] = new Thread(new MIPRunner(t, this, max, viewVec, vVec, uVec));
            threads[t].start();
        }

        try {
            for (int i = 0; i < threads.length; i++) {
                threads[i].join();
            }
        }
        catch (Exception e)
        {
            System.out.println("Something went wrong in computing the Maximum Intensity Projection of the image");
        }


    }

    void composite(double[] viewMatrix) {

        this.clearImage();

        // vector uVec and vVec define a plane through the origin,
        // perpendicular to the view vector viewVec
        double[] viewVec = new double[3];
        double[] uVec = new double[3];
        double[] vVec = new double[3];
        VectorMath.setVector(viewVec, viewMatrix[2], viewMatrix[6], viewMatrix[10]);
        VectorMath.setVector(uVec, viewMatrix[0], viewMatrix[4], viewMatrix[8]);
        VectorMath.setVector(vVec, viewMatrix[1], viewMatrix[5], viewMatrix[9]);

        double max;
        double[] cornerVector = new double[3];
        double[] planeNormal = new double[3];
        planeNormal = VectorMath.crossproduct(uVec, vVec, planeNormal);

        double temp_val;
        double temp_max = -1 * Double.MAX_VALUE;
        double temp_min = Double.MAX_VALUE;

        // Calculate the largest distance from the viewplane through the entire volume

        for (int i = 0; i <= 1; i++) {
            for (int j = 0; j <= 1; j++) {
                for (int z = 0; z <= 1; z++) {
                    VectorMath.setVector(cornerVector, volume.getDimX() * i, volume.getDimY() * j, volume.getDimZ() * z);
                    temp_val = VectorMath.dotproduct(VectorMath.scalarMultiply(-1.0d, cornerVector), planeNormal);

                    if (temp_val > temp_max) temp_max = temp_val;
                    if (temp_val < temp_min) temp_min = temp_val;
                }
            }
        }

        max = temp_max - temp_min;


        Thread[] threads = new Thread[NUM_THREADS];

        for (int t = 0; t < threads.length; t++) {
            threads[t] = new Thread(new CompositeRunner(t, this, max, viewVec, vVec, uVec));
            threads[t].start();
        }

        try {
            for (int i = 0; i < threads.length; i++) {
                threads[i].join();
            }
        }
        catch (Exception e)
        {
            System.out.println("Something went wrong in computing the compositing of the image");
        }


    }

    private void opacityWeighting(double[] viewMatrix){

        this.clearImage();

        // vector uVec and vVec define a plane through the origin,
        // perpendicular to the view vector viewVec
        double[] viewVec = new double[3];
        double[] uVec = new double[3];
        double[] vVec = new double[3];
        VectorMath.setVector(viewVec, viewMatrix[2], viewMatrix[6], viewMatrix[10]);
        VectorMath.setVector(uVec, viewMatrix[0], viewMatrix[4], viewMatrix[8]);
        VectorMath.setVector(vVec, viewMatrix[1], viewMatrix[5], viewMatrix[9]);


        double max;
        double[] cornerVector = new double[3];
        double[] planeNormal = new double[3];
        planeNormal = VectorMath.crossproduct(uVec, vVec, planeNormal);

        double temp_val;
        double temp_max = -Double.MAX_VALUE;
        double temp_min = Double.MAX_VALUE;

        // Calculate the largest distance from the viewplane through the entire volume

        for (int i = 0; i <= 1; i++) {
            for (int j = 0; j <= 1; j++) {
                for (int z = 0; z <= 1; z++) {
                    VectorMath.setVector(cornerVector, volume.getDimX() * i, volume.getDimY() * j, volume.getDimZ() * z);
                    temp_val = VectorMath.dotproduct(VectorMath.scalarMultiply(-1.0d, cornerVector), planeNormal);

                    if (temp_val > temp_max) temp_max = temp_val;
                    if (temp_val < temp_min) temp_min = temp_val;
                }
            }
        }

        max = temp_max - temp_min;


        Thread[] threads = new Thread[NUM_THREADS];

        for (int t = 0; t < threads.length; t++) {
            threads[t] = new Thread(new OpacityRunner(t, this, max, viewVec, vVec, uVec));
            threads[t].start();
        }

        try {
            for (int i = 0; i < threads.length; i++) {
                threads[i].join();
            }
        }
        catch (Exception e)
        {
            System.out.println("Something went wrong in computing the opacity weighting");
        }

    }

    public void calculateTransparencies(){

        opacity_types = tFunc.getSortedControlPoints();

        // For each of the voxels, perform the following calculation:
        // Get the (length of the) gradient vector
        // get the alpha of the layer above (user parameter) and multiply by (f(x) - F_i) / (F_i+1 - F_i)
        // where F_i = the value given to the range i (set by the user)
        // and add the current alpha times (F_i+1 - f(x)) / (F_i+1 - F_i).


        Thread[] threads = new Thread[NUM_THREADS];

        for (int t = 0; t < threads.length; t++) {
            threads[t] = new Thread(new TransparencyRunner(t, this));
            threads[t].start();
        }

        try {
            for (int i = 0; i < threads.length; i++) {
                threads[i].join();
            }
        }
        catch (Exception e)
        {
            System.out.println("Something went wrong in computing the transparencies");
        }

    }

    private void drawBoundingBox(GL2 gl) {
        gl.glPushAttrib(GL2.GL_CURRENT_BIT);
        gl.glDisable(GL2.GL_LIGHTING);
        gl.glColor4d(1.0, 1.0, 1.0, 1.0);
        gl.glLineWidth(1.5f);
        gl.glEnable(GL2.GL_LINE_SMOOTH);
        gl.glHint(GL2.GL_LINE_SMOOTH_HINT, GL2.GL_NICEST);
        gl.glEnable(GL2.GL_BLEND);
        gl.glBlendFunc(GL2.GL_SRC_ALPHA, GL2.GL_ONE_MINUS_SRC_ALPHA);

        gl.glBegin(GL2.GL_LINE_LOOP);
        gl.glVertex3d(-volume.getDimX() / 2.0, -volume.getDimY() / 2.0, volume.getDimZ() / 2.0);
        gl.glVertex3d(-volume.getDimX() / 2.0, volume.getDimY() / 2.0, volume.getDimZ() / 2.0);
        gl.glVertex3d(volume.getDimX() / 2.0, volume.getDimY() / 2.0, volume.getDimZ() / 2.0);
        gl.glVertex3d(volume.getDimX() / 2.0, -volume.getDimY() / 2.0, volume.getDimZ() / 2.0);
        gl.glEnd();

        gl.glBegin(GL2.GL_LINE_LOOP);
        gl.glVertex3d(-volume.getDimX() / 2.0, -volume.getDimY() / 2.0, -volume.getDimZ() / 2.0);
        gl.glVertex3d(-volume.getDimX() / 2.0, volume.getDimY() / 2.0, -volume.getDimZ() / 2.0);
        gl.glVertex3d(volume.getDimX() / 2.0, volume.getDimY() / 2.0, -volume.getDimZ() / 2.0);
        gl.glVertex3d(volume.getDimX() / 2.0, -volume.getDimY() / 2.0, -volume.getDimZ() / 2.0);
        gl.glEnd();

        gl.glBegin(GL2.GL_LINE_LOOP);
        gl.glVertex3d(volume.getDimX() / 2.0, -volume.getDimY() / 2.0, -volume.getDimZ() / 2.0);
        gl.glVertex3d(volume.getDimX() / 2.0, -volume.getDimY() / 2.0, volume.getDimZ() / 2.0);
        gl.glVertex3d(volume.getDimX() / 2.0, volume.getDimY() / 2.0, volume.getDimZ() / 2.0);
        gl.glVertex3d(volume.getDimX() / 2.0, volume.getDimY() / 2.0, -volume.getDimZ() / 2.0);
        gl.glEnd();

        gl.glBegin(GL2.GL_LINE_LOOP);
        gl.glVertex3d(-volume.getDimX() / 2.0, -volume.getDimY() / 2.0, -volume.getDimZ() / 2.0);
        gl.glVertex3d(-volume.getDimX() / 2.0, -volume.getDimY() / 2.0, volume.getDimZ() / 2.0);
        gl.glVertex3d(-volume.getDimX() / 2.0, volume.getDimY() / 2.0, volume.getDimZ() / 2.0);
        gl.glVertex3d(-volume.getDimX() / 2.0, volume.getDimY() / 2.0, -volume.getDimZ() / 2.0);
        gl.glEnd();

        gl.glBegin(GL2.GL_LINE_LOOP);
        gl.glVertex3d(-volume.getDimX() / 2.0, volume.getDimY() / 2.0, -volume.getDimZ() / 2.0);
        gl.glVertex3d(-volume.getDimX() / 2.0, volume.getDimY() / 2.0, volume.getDimZ() / 2.0);
        gl.glVertex3d(volume.getDimX() / 2.0, volume.getDimY() / 2.0, volume.getDimZ() / 2.0);
        gl.glVertex3d(volume.getDimX() / 2.0, volume.getDimY() / 2.0, -volume.getDimZ() / 2.0);
        gl.glEnd();

        gl.glBegin(GL2.GL_LINE_LOOP);
        gl.glVertex3d(-volume.getDimX() / 2.0, -volume.getDimY() / 2.0, -volume.getDimZ() / 2.0);
        gl.glVertex3d(-volume.getDimX() / 2.0, -volume.getDimY() / 2.0, volume.getDimZ() / 2.0);
        gl.glVertex3d(volume.getDimX() / 2.0, -volume.getDimY() / 2.0, volume.getDimZ() / 2.0);
        gl.glVertex3d(volume.getDimX() / 2.0, -volume.getDimY() / 2.0, -volume.getDimZ() / 2.0);
        gl.glEnd();

        gl.glDisable(GL2.GL_LINE_SMOOTH);
        gl.glDisable(GL2.GL_BLEND);
        gl.glEnable(GL2.GL_LIGHTING);
        gl.glPopAttrib();

    }

    @Override
    public void visualize(GL2 gl) {


        if (volume == null) {
            return;
        }

        drawBoundingBox(gl);

        gl.glGetDoublev(GL2.GL_MODELVIEW_MATRIX, viewMatrix, 0);

        long startTime = System.currentTimeMillis();
        switch(panel.current_function){
            case SLICE : slicer(viewMatrix); break;
            case MIP_RAYCAST : mip_raycast(viewMatrix); break;
            case COMPOSITE : composite(viewMatrix); break;
            case OPACITY : opacityWeighting(viewMatrix); break;

            default: slicer(viewMatrix);
        }
        long endTime = System.currentTimeMillis();
        double runningTime = (endTime - startTime);
        panel.setSpeedLabel(Double.toString(runningTime));
        ow_panel.setSpeedLabel(Double.toString(runningTime));

        Texture texture = AWTTextureIO.newTexture(gl.getGLProfile(), image, false);

        gl.glPushAttrib(GL2.GL_LIGHTING_BIT);
        gl.glDisable(GL2.GL_LIGHTING);
        gl.glEnable(GL2.GL_BLEND);
        gl.glBlendFunc(GL2.GL_SRC_ALPHA, GL2.GL_ONE_MINUS_SRC_ALPHA);

        // draw rendered image as a billboard texture
        texture.enable(gl);
        texture.bind(gl);
        double halfWidth = image.getWidth() / 2.0;
        gl.glPushMatrix();
        gl.glLoadIdentity();
        gl.glBegin(GL2.GL_QUADS);
        gl.glColor4f(1.0f, 1.0f, 1.0f, 1.0f);
        gl.glTexCoord2d(0.0, 0.0);
        gl.glVertex3d(-halfWidth, -halfWidth, 0.0);
        gl.glTexCoord2d(0.0, 1.0);
        gl.glVertex3d(-halfWidth, halfWidth, 0.0);
        gl.glTexCoord2d(1.0, 1.0);
        gl.glVertex3d(halfWidth, halfWidth, 0.0);
        gl.glTexCoord2d(1.0, 0.0);
        gl.glVertex3d(halfWidth, -halfWidth, 0.0);
        gl.glEnd();
        texture.disable(gl);
        texture.destroy(gl);
        gl.glPopMatrix();

        gl.glPopAttrib();


        if (gl.glGetError() > 0) {
            System.out.println("some OpenGL error: " + gl.glGetError());
        }

    }
    public BufferedImage image;
    private double[] viewMatrix = new double[4 * 4];

}
