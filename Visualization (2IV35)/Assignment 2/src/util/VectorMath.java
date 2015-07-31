/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package util;

/**
 *
 * @author michel
 */
public class VectorMath {

    // assign coefficients c0..c2 to vector v
    public static void setVector(double[] v, double c0, double c1, double c2) {
        v[0] = c0;
        v[1] = c1;
        v[2] = c2;
    }

    // compute dotproduct of vectors v and w
    public static double dotproduct(double[] v, double[] w) {
        double r = 0;
        for (int i=0; i<3; i++) {
            r += v[i] * w[i];
        }
        return r;
    }

    // compute distance between vectors v and w
    public static double distance(double[] v, double[] w) {
        double[] tmp = new double[3];
        VectorMath.setVector(tmp, v[0]-w[0], v[1]-w[1], v[2]-w[2]);
        return Math.sqrt(VectorMath.dotproduct(tmp, tmp));
    }

    // compute crossproduct of v and w
    public static double[] crossproduct(double[] v, double[] w, double[] r) {
        r[0] = v[1] * w[2] - v[2] * w[1];
        r[1] = v[2] * w[0] - v[0] * w[2];
        r[2] = v[0] * w[1] - v[1] * w[0];
        return r;
    }
    
    // compute length of vector v
    public static double length(double[] v) {
        return Math.sqrt(v[0]*v[0] + v[1]*v[1] + v[2]*v[2]);
    }
    
    public static double[] normalize(double[] v){
        double length = length(v);
        double[] w = new double[3];
        w[0] = v[0]/length;
        w[1] = v[1]/length;
        w[2] = v[2]/length;
        return w;
    }

    public static double[] scalarMultiply(double s, double[] v){
        double[] w = new double[3];
        w[0]=v[0]*s;
        w[1]=v[1]*s;
        w[2]=v[2]*s;
        return w;
    }

    public static String toString(double[] v){
        String s = "";
        for(int i = 0; i < v.length-1; i++){
            s+= v[i] + "  ";
        }
        s+=v[v.length-1];
        return s;
    }
}
