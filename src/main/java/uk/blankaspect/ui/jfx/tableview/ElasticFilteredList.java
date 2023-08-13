/*====================================================================*\

ElasticFilteredList.java

Class: elastic filtered list of items for a list view or table view.

\*====================================================================*/


// PACKAGE


package uk.blankaspect.ui.jfx.tableview;

//----------------------------------------------------------------------


// IMPORTS


import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;

import java.util.function.Predicate;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import javafx.collections.transformation.FilteredList;
import javafx.collections.transformation.SortedList;

import javafx.scene.control.ListView;
import javafx.scene.control.TableView;

//----------------------------------------------------------------------


// CLASS: ELASTIC FILTERED LIST OF ITEMS FOR A LIST VIEW OR TABLE VIEW


/**
 * This class implements a list of items for a JavaFX {@linkplain ListView list view} or {@linkplain TableView table
 * view}.  An instance of this class consists of three underlying lists:
 * <ol>
 *   <li>
 *     A base list of unfiltered and unsorted items (the innermost list).
 *   <li>
 *   <li>
 *     A {@linkplain FilteredList filtered list} of the items of the base list to which a filter function is applied.
 *   <li>
 *   <li>
 *     A {@linkplain SortedList sorted list} of the items of the filtered list (the outermost list).
 *   <li>
 * </ol>
 * The nesting of the lists allows the unsorted order of items to be restored when the sorting of the columns of a table
 * view is removed.
 */

public class ElasticFilteredList<T>
{

////////////////////////////////////////////////////////////////////////
//  Instance variables
////////////////////////////////////////////////////////////////////////

	/** The underlying unfiltered and unsorted list of the items that are represented in the table view. */
	private	ObservableList<T>	baseList;

	/** The underlying filtered list of the items that are represented in the table view. */
	private	FilteredList<T>		filteredList;

	/** The underlying sorted list of the items that are represented in the table view. */
	private	SortedList<T>		sortedList;

////////////////////////////////////////////////////////////////////////
//  Constructors
////////////////////////////////////////////////////////////////////////

	/**
	 * Creates a new instance of a list of items for the specified list view.
	 *
	 * @param listView
	 *          the list view to which this list will be applied.
	 */

	public ElasticFilteredList(
		ListView<T>	listView)
	{
		// Validate arguments
		if (listView == null)
			throw new IllegalArgumentException("Null list view");

		// Initialise instance variables
		baseList = FXCollections.observableArrayList();
		filteredList = new FilteredList<>(baseList);
		sortedList = new SortedList<>(filteredList);

		// Set items on list view
		listView.setItems(sortedList);
	}

	//------------------------------------------------------------------

	/**
	 * Creates a new instance of a list of items for the specified table view.
	 *
	 * @param tableView
	 *          the table view to which this list will be applied.
	 */

	public ElasticFilteredList(
		TableView<T>	tableView)
	{
		// Validate arguments
		if (tableView == null)
			throw new IllegalArgumentException("Null table view");

		// Initialise instance variables
		baseList = FXCollections.observableArrayList();
		filteredList = new FilteredList<>(baseList);
		sortedList = new SortedList<>(filteredList);

		// Set items on table view
		tableView.setItems(sortedList);

		// Bind comparator of sorted list to comparator of table view
		sortedList.comparatorProperty().bind(tableView.comparatorProperty());
	}

	//------------------------------------------------------------------

////////////////////////////////////////////////////////////////////////
//  Instance methods
////////////////////////////////////////////////////////////////////////

	/**
	 * Returns the number of items in the unfiltered list.
	 *
	 * @return the number of items in the unfiltered list.
	 */

	public int getNumItems()
	{
		return baseList.size();
	}

	//------------------------------------------------------------------

	/**
	 * Returns a modifiable copy of this list that contains the items of the unfiltered list in unsorted order.
	 *
	 * @return a modifiable copy of this list that contains the items of the unfiltered list in unsorted order.
	 */

	public List<T> getItems()
	{
		return new ArrayList<>(baseList);
	}

	//------------------------------------------------------------------

	/**
	 * Returns the index of the specified item in the unfiltered and unsorted list of items.  The target item is
	 * compared to the items in the unfiltered and unsorted list using reference equality ({@code item1 == item2}), not
	 * object equality ({@code item1.equals(item2)}).
	 *
	 * @param  item
	 *           the target item.
	 * @return the index of {@code item} in the unfiltered and unsorted list of items, or -1 if the item was not found
	 *         in the list.
	 */

	public int indexOf(
		T	item)
	{
		int numItems = baseList.size();
		for (int i = 0; i < numItems; i++)
		{
			if (baseList.get(i) == item)
				return i;
		}
		return -1;
	}

	//------------------------------------------------------------------

	/**
	 * Adds the specified item to the end of this list.
	 *
	 * @param item
	 *          the item that will be added to the end of this list.
	 */

	public void add(
		T	item)
	{
		baseList.add(item);
	}

	//------------------------------------------------------------------

	/**
	 * Inserts the specified item into this list at the specified index.  The index relates to the filtered and sorted
	 * list.
	 *
	 * @param index
	 *          the index at which {@code item} will be inserted into this list.
	 * @param item
	 *          the item that will be inserted into this list.
	 */

	public void add(
		int	index,
		T	item)
	{
		baseList.add(filteredList.getSourceIndex(sortedList.getSourceIndex(index)), item);
	}

	//------------------------------------------------------------------

	/**
	 * Removes the item at the specified index from this list.  The index relates to the filtered and sorted list.
	 *
	 * @param  index
	 *           the index of the item that will be removed from this list.
	 * @return the item that was removed from this list.
	 */

	public T remove(
		int	index)
	{
		return baseList.remove(filteredList.getSourceIndex(sortedList.getSourceIndex(index)));
	}

	//------------------------------------------------------------------

	/**
	 * Removes the specified item from this list.
	 *
	 * @param  item
	 *           the item that will be removed from this list.
	 * @return {@code true} if this list contained {@code item}.
	 */

	public boolean remove(
		T	item)
	{
		return baseList.remove(item);
	}

	//------------------------------------------------------------------

	/**
	 * Replaces the item at the specified index in this list with specified item.  The index relates to the filtered and
	 * sorted list.
	 *
	 * @param index
	 *          the index of the item that will be replaced by {@code item}.
	 * @param item
	 *          the item that will replace the item at {@code index} in this list.
	 */

	public void set(
		int	index,
		T	item)
	{
		baseList.set(filteredList.getSourceIndex(sortedList.getSourceIndex(index)), item);
	}

	//------------------------------------------------------------------

	/**
	 * Returns the filter function that is applied to the filtered list.
	 *
	 * @return the filter function that is applied to the filtered list, or {@code null} if no filter is applied.
	 */

	public Predicate<? super T> getFilter()
	{
		return filteredList.getPredicate();
	}

	//------------------------------------------------------------------

	/**
	 * Sets the specified filter function on the filtered list.  An item will be excluded from the outer list if the
	 * function returns {@code false} for the item.  If the function is {@code null}, all items will be included.
	 *
	 * @param filter
	 *          the filter function that will be set on the filtered list.  If it is {@code null}, no filter will be
	 *          applied.
	 */

	public void setFilter(
		Predicate<? super T>	filter)
	{
		filteredList.setPredicate(filter);
	}

	//------------------------------------------------------------------

	/**
	 * Returns the comparison function that is applied to the sorted list.
	 *
	 * @return the comparison function that is applied to the sorted list, or {@code null} if no comparator is applied.
	 */

	public Comparator<? super T> getComparator()
	{
		return sortedList.getComparator();
	}

	//------------------------------------------------------------------

	/**
	 * Sets the specified comparison function on the sorted list.  This method cannot be used when this list is used
	 * with a table view because the comparator of the sorted list is bound to the comparator of the table view.
	 *
	 * @param comparator
	 *          the comparison function that will be set on the sorted list.  If it is {@code null}, no comparator will
	 *          be applied.
	 */

	public void setComparator(
		Comparator<? super T>	comparator)
	{
		sortedList.setComparator(comparator);
	}

	//------------------------------------------------------------------

	/**
	 * Updates this list with the specified items.
	 *
	 * @param items
	 *          the items that will be set on this list.
	 */

	public void update(
		Collection<? extends T>	items)
	{
		baseList.setAll(items);
	}

	//------------------------------------------------------------------

	/**
	 * Updates this list with the specified items.
	 *
	 * @param items
	 *          the items that will be set on this list.
	 */

	@SuppressWarnings("unchecked")
	public void update(
		T...	items)
	{
		baseList.setAll(items);
	}

	//------------------------------------------------------------------

}

//----------------------------------------------------------------------
