/*====================================================================*\

SelectionModelUtils.java

Class: selection-model-related utility methods.

\*====================================================================*/


// PACKAGE


package uk.blankaspect.ui.jfx.selectionmodel;

//----------------------------------------------------------------------


// IMPORTS


import java.util.ArrayList;
import java.util.List;

import javafx.scene.control.MultipleSelectionModel;
import javafx.scene.control.SelectionModel;

//----------------------------------------------------------------------


// CLASS: SELECTION-MODEL-RELATED UTILITY METHODS


/**
 * This class contains utility methods that relate to {@linkplain SelectionModel selection models}.
 */

public class SelectionModelUtils
{

////////////////////////////////////////////////////////////////////////
//  Constructors
////////////////////////////////////////////////////////////////////////

	/**
	 * Prevents this class from being instantiated externally.
	 */

	private SelectionModelUtils()
	{
	}

	//------------------------------------------------------------------

////////////////////////////////////////////////////////////////////////
//  Class methods
////////////////////////////////////////////////////////////////////////

	public static void invertSelection(
		MultipleSelectionModel<?>	selectionModel,
		int							numItems)
	{
		switch (selectionModel.getSelectionMode())
		{
			case SINGLE:
			{
				// Get index of selected item
				int index = selectionModel.getSelectedIndex();

				// Select all items
				selectionModel.selectAll();

				// Deselect item that was previously selected
				if (index >= 0)
					selectionModel.clearSelection(index);
				break;
			}

			case MULTIPLE:
			{
				// Get indices of selected items
				List<Integer> indices = new ArrayList<>(selectionModel.getSelectedIndices());

				// Sort indices
				indices.sort(null);

				// If no items are selected, select all items ...
				if (indices.isEmpty())
					selectionModel.selectAll();

				// ... otherwise, invert selection
				else
				{
					// Clear selection
					selectionModel.clearSelection();

					// Select items that were previously not selected
					if (indices.size() < numItems)
					{
						int startIndex = 0;
						for (int index : indices)
						{
							if (index > startIndex)
								selectionModel.selectRange(startIndex, index);
							startIndex = index + 1;
						}

						if (startIndex < numItems)
							selectionModel.selectRange(startIndex, numItems);
					}
				}
				break;
			}
		}
	}

	//------------------------------------------------------------------

}

//----------------------------------------------------------------------
