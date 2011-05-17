package cuboidLocale;

import java.util.UUID;

public class PrimitiveCuboid{
  public UUID uuid = null;
  public String world = null;
  public double[] xyzA = {0,0,0};
  public double[] xyzB = {0,0,0};
  long lowIndex[] = new long[3];
  long highIndex[] = new long[3];
  
  /**
   * Normalize the corners so that all A is <= B
   * This is CRITICAL for the correct functioning of the MortonCodes, and nice to have for comparison to a point
   */
  final private void normalize(){
    double temp;
    for(int i=0; i<3; i++){
      if(this.xyzA[i] > this.xyzB[i]){
        temp = this.xyzA[i];
        this.xyzA[i] = this.xyzB[i];
        this.xyzB[i] = temp;
      }
    }
  }
  
  public PrimitiveCuboid(double[] xyzA, double[] xyzB){
    this.xyzA = xyzA.clone();
    this.xyzB = xyzB.clone();
    this.normalize();
  }
  
  public PrimitiveCuboid(double xyzA2,double d,double e, double tmp, double f, double xyzB2){
    this.xyzA[0] = xyzA2;
    this.xyzA[1] = d;
    this.xyzA[2] = e;
    
    this.xyzB[0] = tmp;
    this.xyzB[1] = f;
    this.xyzB[2] = xyzB2;
    
    this.normalize();
  }
  
  final public boolean includesPoint(double d, double e, double f){
    if(this.xyzA[0] <= d && this.xyzA[1] <= e && this.xyzA[2] <= f &&
       this.xyzB[0] >= d && this.xyzB[1] >= e && this.xyzB[2] >= f
    ){
      return true;
    }
    return false;
  }
  
  final public boolean includesPoint(double[] pt){
    return this.includesPoint(pt[0], pt[1], pt[2]);
  }
  
}
