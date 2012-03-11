package cuboidLocale;

// TODO: Auto-generated Javadoc
/**
 * The Class PrimitiveCuboid.
 */
public class PrimitiveCuboid
{

	/** The id. */
	public long id = -1;

	/** The world. */
	public String world = null;

	/** The xyz a. */
	public double[] xyzA = {0, 0, 0};

	/** The xyz b. */
	public double[] xyzB = {0, 0, 0};

	/** The low index. */
	long lowIndex[] = new long[3];

	/** The high index. */
	long highIndex[] = new long[3];

	/**
	 * Normalize the corners so that all A is <= B This is CRITICAL for the
	 * correct functioning of the MortonCodes, and nice to have for comparison
	 * to a point.
	 */
	final private void normalize()
	{
		double temp;
		for(int i = 0; i < 3; i++)
		{
			if(this.xyzA[i] > this.xyzB[i])
			{
				temp = this.xyzA[i];
				this.xyzA[i] = this.xyzB[i];
				this.xyzB[i] = temp;
			}
		}
	}

	/**
	 * Instantiates a new primitive cuboid.
	 * @param xyzA
	 * the xyz a
	 * @param xyzB
	 * the xyz b
	 */
	public PrimitiveCuboid(double[] xyzA, double[] xyzB)
	{
		this.xyzA = xyzA.clone();
		this.xyzB = xyzB.clone();
		this.normalize();
	}

	/**
	 * Instantiates a new primitive cuboid.
	 * @param xyzA2
	 * the xyz a2
	 * @param d
	 * the d
	 * @param e
	 * the e
	 * @param tmp
	 * the tmp
	 * @param f
	 * the f
	 * @param xyzB2
	 * the xyz b2
	 */
	public PrimitiveCuboid(double xyzA2, double d, double e, double tmp,
			double f, double xyzB2)
	{
		this.xyzA[0] = xyzA2;
		this.xyzA[1] = d;
		this.xyzA[2] = e;

		this.xyzB[0] = tmp;
		this.xyzB[1] = f;
		this.xyzB[2] = xyzB2;

		this.normalize();
	}

	/**
	 * Includes point.
	 * @param d
	 * the d
	 * @param e
	 * the e
	 * @param f
	 * the f
	 * @return true, if successful
	 */
	final public boolean includesPoint(double d, double e, double f)
	{
		if(this.xyzA[0] <= d && this.xyzA[1] <= e && this.xyzA[2] <= f
				&& this.xyzB[0] >= d && this.xyzB[1] >= e && this.xyzB[2] >= f){ return true; }
		return false;
	}

	/**
	 * Includes point.
	 * @param pt
	 * the pt
	 * @return true, if successful
	 */
	final public boolean includesPoint(double[] pt)
	{
		return this.includesPoint(pt[0], pt[1], pt[2]);
	}

}
