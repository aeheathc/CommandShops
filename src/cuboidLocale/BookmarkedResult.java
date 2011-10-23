package cuboidLocale;

import java.util.ArrayList;

// TODO: Auto-generated Javadoc
/**
 * The Class BookmarkedResult.
 */
public class BookmarkedResult
{

	/** The results. */
	public ArrayList<PrimitiveCuboid> results;

	/** The bookmark. */
	public QuadNode bookmark;

	/**
	 * Instantiates a new bookmarked result.
	 */
	public BookmarkedResult()
	{}

	/**
	 * Instantiates a new bookmarked result.
	 * @param node
	 * the node
	 * @param c
	 * the c
	 */
	public BookmarkedResult(QuadNode node, ArrayList<PrimitiveCuboid> c)
	{
		bookmark = node;
		results = c;
	}
}
