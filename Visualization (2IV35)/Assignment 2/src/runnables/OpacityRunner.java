package runnables;

import util.VectorMath;
import volvis.RaycastRenderer;
import volvis.TFColor;

/**
 * Created by Stijn on 2-7-2015.
 */
public class OpacityRunner implements Runnable {
    private int t;
    public RaycastRenderer renderer;
    double max;
    int stepsize;
    double[] viewVec, vVec, uVec;


    public OpacityRunner(int t, RaycastRenderer renderer, double max, double[] viewVec, double[] vVec, double[] uVec) {
        this.t = t;
        this.renderer = renderer;
        this.max = max;
        this.stepsize = 1;
        this.viewVec = viewVec;
        this.vVec = vVec;
        this.uVec = uVec;




    }

    public void run() {

        // Unit viewVec
        double[] normalVec = VectorMath.normalize(viewVec);

        // image is square
        int imageCenter = renderer.image.getWidth() / 2;

        double[] volumeCenter = new double[3];

        VectorMath.setVector(volumeCenter, (double) renderer.volume.getDimX() / 2, (double) renderer.volume.getDimY() / 2, (double) renderer.volume.getDimZ() / 2);

        double[] pixelCoord = new double[3];

        // steps is either the full length of the thing (steps of size 1), or 20 in interactive mode, or the number
        // specified by the UI.
        int steps = (renderer.panel.opacity_raycasting)?(int)max-1:(renderer.interactiveMode)?Math.min(20,renderer.panel.opacity_steps):renderer.panel.opacity_steps;

        double[] nextCoord = new double[3];
        int voxel_value;
        double alpha;

        TFColor finalColor, voxelColor;

        // Steps
        // =========
        // 1
        // -----
        // We preprocess the voxels once and give them transparencies based on classifications of different tissue types
        // This must become parametrized as well. Changing it means the preprocess step must occur anew too.
        // NOTE: the assumption is that tissue types only touch tissue types that are directly below or above that type.
        // 2
        // ------
        // Next, we color all the voxels according to the colors used by the Transfer Function Editor
        // 3
        // -----
        // We then take samples along the viewing ray and compute the final image using compositing of the image with
        // transparencies that we computed instead of the transfer function transparency.


        double maxX = renderer.volume.getDimX();
        double maxY = renderer.volume.getDimY();
        double maxZ = renderer.volume.getDimZ();

        double[] stepVector = new double[3];
        stepVector[0] =  normalVec[0] * (!renderer.interactiveMode && renderer.panel.opacity_raycasting?1:(maxX / (steps + 1)));
        stepVector[1] =  normalVec[1] * (!renderer.interactiveMode && renderer.panel.opacity_raycasting?1:(maxY / (steps + 1)));
        stepVector[2] =  normalVec[2] * (!renderer.interactiveMode && renderer.panel.opacity_raycasting?1:(maxZ / (steps + 1)));

        for (int j = 0; j < renderer.image.getHeight(); j+=stepsize) {
            for (int i = t * renderer.image.getWidth() / renderer.NUM_THREADS; i < (t + 1) * renderer.image.getWidth()/renderer.NUM_THREADS; i+=stepsize) {
                pixelCoord[0] = uVec[0] * (i - imageCenter) + vVec[0] * (j - imageCenter) + maxX / 2;
                pixelCoord[1] = uVec[1] * (i - imageCenter) + vVec[1] * (j - imageCenter) + maxY / 2;
                pixelCoord[2] = uVec[2] * (i - imageCenter) + vVec[2] * (j - imageCenter) + maxZ / 2 ;

                // start at the back of the volume seen from the current viewpoint
                nextCoord[0] = pixelCoord[0] + normalVec[0] * maxX / 2;
                nextCoord[1] = pixelCoord[1] + normalVec[1] * maxY / 2;
                nextCoord[2] = pixelCoord[2] + normalVec[2] * maxZ / 2;

                finalColor = new TFColor(0,0,0,0);

                while(
                        nextCoord[0] <= (maxX + imageCenter) && nextCoord[0] >= -(maxX + imageCenter) &&
                        nextCoord[1] <= (maxY + imageCenter) && nextCoord[1] >= -(maxY + imageCenter) &&
                        nextCoord[2] <= (maxZ + imageCenter) && nextCoord[2] >= -(maxZ + imageCenter)
                        )
                {
                    voxel_value = renderer.getVoxel(nextCoord);
                    voxelColor = renderer.tFunc.getColor(voxel_value);


                    // find the transparency based on the class of the voxel (we use the nearest neighbour for the transparency)
                    // instead of the alpha assigned by the transfer function.

                    int x = (int)Math.round(nextCoord[0]);
                    int y = (int)Math.round(nextCoord[1]);
                    int z = (int)Math.round(nextCoord[2]);
                    alpha = (x < 0 || y < 0 || z < 0 || x >= maxX || y >= maxY || z >= maxZ)?0:renderer.transparencies[x][y][z];
                    // alpha = (voxel_value!=0)?transparencies[(int)Math.round(nextCoord[0])][(int)Math.round(nextCoord[1])][(int)Math.round(nextCoord[2])]:0;
                    // alpha = Math.max(0, Math.min(1, alpha));
                    finalColor.r = voxelColor.r * (alpha) + (1 - alpha) * finalColor.r;
                    finalColor.g = voxelColor.g * (alpha) + (1 - alpha) * finalColor.g;
                    finalColor.b = voxelColor.b * (alpha) + (1 - alpha) * finalColor.b;
                    finalColor.a = alpha + (1 - alpha) * finalColor.a;

                    nextCoord[0] -= stepVector[0];
                    nextCoord[1] -= stepVector[1];
                    nextCoord[2] -= stepVector[2];

                }

                // BufferedImage expects a pixel color packed as ARGB in an int
                int c_alpha = finalColor.a <= 1.0 ? (int) Math.floor(finalColor.a * 255) : 255;
                int c_red = finalColor.r <= 1.0 ? (int) Math.floor(finalColor.r * 255) : 255;
                int c_green = finalColor.g <= 1.0 ? (int) Math.floor(finalColor.g * 255) : 255;
                int c_blue = finalColor.b <= 1.0 ? (int) Math.floor(finalColor.b * 255) : 255;
                int pixelColor = (c_alpha << 24) | (c_red << 16) | (c_green << 8) | c_blue;

                //apply this color to the range of the stepsize
                for (int di = 0; di < stepsize && i + di < renderer.image.getWidth(); di++) {
                    for (int dj = 0; dj < stepsize && j + dj < renderer.image.getHeight(); dj++) {
                        renderer.image.setRGB(i + di, j + dj, pixelColor);
                    }
                }
            }
        }
    }

}