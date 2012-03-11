package com.aehdev.commandshops;
/**
 * Object representing a selection in progress.
 */
public class Selection
{
	/** The coordinates of the two points of the selection. */
	public int x,y,z,x2,y2,z2;
	
	/** Indicates which points have been selected so far in this selection. */
	public boolean a=false,b=false;
	
	/** World containing the selection. */
	public String world = "world";
	
	/**
	 * Check if the Selection has been fully made and the
	 * size of this Selection is within acceptable limits.
	 * @return true if the size is OK
	 */
	public boolean checkSize()
	{
		if(!a || !b) return false;
		double w1 = Math.abs(x - x2) + 1;
		double height = Math.abs(y - y2) + 1;
		double w2 = Math.abs(z - z2) + 1;
		return w1<=Config.MAX_WIDTH && w2<=Config.MAX_WIDTH && height<=Config.MAX_HEIGHT;
	}
}