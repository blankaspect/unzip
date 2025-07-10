/*====================================================================*\

IPersistable.java

Interface: text-related data that can be stored between sessions.

\*====================================================================*/


// PACKAGE


package uk.blankaspect.unzip;

//----------------------------------------------------------------------


// INTERFACE: TEXT-RELATED DATA THAT CAN BE STORED BETWEEN SESSIONS


/**
 * This interface defines the methods that must be implemented by an item of text-related data that can be stored
 * between sessions.
 */

public interface IPersistable
{

////////////////////////////////////////////////////////////////////////
//  Methods
////////////////////////////////////////////////////////////////////////

	/**
	 * Returns the text at the specified index.
	 *
	 * @param  index
	 *           the index of the text.
	 * @return the text at {@code index}.
	 */

	String getText(
		int	index);

	//------------------------------------------------------------------

	/**
	 * Returns {@code true} if this item is persistent.
	 *
	 * @return {@code true} if this item is persistent.
	 */

	boolean isPersistent();

	//------------------------------------------------------------------

	/**
	 * Sets the <i>persistent</i> state of this item to the specified value.
	 *
	 * @param persistent
	 *          the value to which the <i>persistent</i> state of this item will be set.
	 */

	void setPersistent(
		boolean	persistent);

	//------------------------------------------------------------------

}

//----------------------------------------------------------------------
