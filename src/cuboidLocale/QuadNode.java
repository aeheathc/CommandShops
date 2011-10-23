package cuboidLocale;

import java.util.ArrayList;

// TODO: Auto-generated Javadoc
/**
 * The Class QuadNode.
 */
public class QuadNode
{

	/** The parent. */
	QuadNode parent = null;

	// children. Lower Left 0; Right + 1; Upper + 2
	/*
	 * +0 |+1 +2 2 | 3 ------------ +0 0 | 1 */
	/** The quads. */
	QuadNode[] quads = new QuadNode[4];

	/**
	 * Instantiates a new quad node.
	 * @param xyzA
	 * the xyz a
	 * @param xyzA2
	 * the xyz a2
	 * @param size
	 * the size
	 * @param parent
	 * the parent
	 */
	QuadNode(double xyzA, double xyzA2, long size, QuadNode parent)
	{
		this.x = xyzA;
		this.z = xyzA2;
		this.size = size;
		this.parent = parent;
		if(parent == null){ return; }
		// If the parent is holding cuboids it is our next list holder
		if(parent.cuboids.size() != 0)
		{
			this.nextListHolder = parent;
		}else
		{// Otherwise whatever it's next list holder is is also ours (even null)
			this.nextListHolder = parent.nextListHolder;
		}
	}

	/**
	 * Gets the info.
	 * @return the info
	 */
	String getInfo()
	{
		return "(" + x + "," + z + "; " + size + ")";
	}

	// Length of a side, always a power of two
	/** The size. */
	long size;
	// Indexed by least x and least z
	/** The x. */
	double x; // Traditional X in 2d

	/** The z. */
	double z; // What would be Y but this is minecraft

	// We only hold the cuboids that fit completely inside of us.
	// Cuboids are always held in their minimal bounding node
	/** The cuboids. */
	ArrayList<PrimitiveCuboid> cuboids = new ArrayList<PrimitiveCuboid>();
	// We only hold our list of cuboids, but to prevent duplicating lists and
	// having to climb to the root to find all of the cuboids that we need to
	// look at
	// Point to the next highest node with cuboids to examine
	/** The next list holder. */
	QuadNode nextListHolder;
}
