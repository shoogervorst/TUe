package runnables;

import util.VectorMath;
import volvis.RaycastRenderer;

import java.util.TreeMap;

/**
 * Created by Stijn on 2-7-2015.
 */
public class TransparencyRunner implements Runnable {
    private int t;
    public RaycastRenderer renderer;


    public TransparencyRunner(int t, RaycastRenderer renderer) {
        this.t = t;
        this.renderer = renderer;
    }

    public void run() {
        double[] test = new double[3];
        double gradient_vector_length = 0;
        double voxel_value;
        double temp_transparency;
        short volume_max = renderer.volume.getMaximum();
        TreeMap<Short, Double> opacity_types;

        for(int i = 0; i < renderer.volume.getDimX(); i++){
            for(int j = 0; j < renderer.volume.getDimY(); j++) {
                for (int k = t * renderer.volume.getDimZ() / renderer.NUM_THREADS; k < (t + 1) * renderer.volume.getDimZ() / renderer.NUM_THREADS; k++) {

                    temp_transparency = 0;

                    VectorMath.setVector(test, i, j, k);
                    gradient_vector_length = VectorMath.length(getGradientVector(test)) / volume_max;

                    voxel_value = renderer.getVoxel(test);

                    // find the right class this voxel belongs in (linear search, even though entries are sorted. number of classes is expected to be small)
                    opacity_types = renderer.tFunc.getSortedControlPoints();
                    Short lastkey = opacity_types.firstKey();
                    for(Short z : opacity_types.keySet()) {
                        // find the range this voxel falls in
                        if(voxel_value >= lastkey && voxel_value < z) {
                            temp_transparency = gradient_vector_length * ((opacity_types.get(z) * ((voxel_value - lastkey)/(z - lastkey))+(opacity_types.get(lastkey) * ((z - voxel_value)/(z - lastkey)))));
                            break;
                        }
                        lastkey = z;
                    }
                    renderer.transparencies[i][j][k] = temp_transparency;
                }
            }
        }
    }

    private double[] getGradientVector(double[] coord) {
        double[] result = new double[3];
        short nextv;
        short prevv;


        //TODO: Make smarter (save the next i for each dimension)
        for (int i = 0; i < 3; i++){
            double[] next = coord.clone();
            double[] prev = coord.clone();

            next[i] += 1;
            prev[i] -= 1;
            nextv = renderer.getVoxel(next);
            prevv = renderer.getVoxel(prev);
            if(nextv == 0 || prevv ==0){
                result[i] = 0;
            }
            else
            {
                result[i] = 0.5 * (nextv - prevv);
            }
        }

        return result;
    }
}