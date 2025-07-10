/*====================================================================*\

ReplaceFilesDialog.java

Class: 'replace files' dialog.

\*====================================================================*/


// PACKAGE


package uk.blankaspect.unzip;

//----------------------------------------------------------------------


// IMPORTS


import java.nio.file.Path;

import java.util.ArrayList;
import java.util.BitSet;
import java.util.List;

import javafx.beans.InvalidationListener;

import javafx.event.ActionEvent;
import javafx.event.EventHandler;

import javafx.geometry.HPos;
import javafx.geometry.Insets;
import javafx.geometry.Point2D;
import javafx.geometry.Pos;

import javafx.scene.control.Button;
import javafx.scene.control.SelectionMode;

import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;

import javafx.stage.Window;

import uk.blankaspect.common.basictree.MapNode;

import uk.blankaspect.common.filesystem.PathUtils;

import uk.blankaspect.common.function.IFunction2;
import uk.blankaspect.common.function.IProcedure0;

import uk.blankaspect.ui.jfx.button.Buttons;

import uk.blankaspect.ui.jfx.dialog.SimpleModalDialog;

import uk.blankaspect.ui.jfx.listview.SimpleTextListView;

import uk.blankaspect.ui.jfx.selectionmodel.SelectionModelUtils;

import uk.blankaspect.ui.jfx.window.WindowState;

//----------------------------------------------------------------------


// CLASS: 'REPLACE FILES' DIALOG


public class ReplaceFilesDialog
	extends SimpleModalDialog<BitSet>
{

////////////////////////////////////////////////////////////////////////
//  Constants
////////////////////////////////////////////////////////////////////////

	private static final	double	LIST_VIEW_WIDTH		= 640.0;
	private static final	double	LIST_VIEW_HEIGHT	= 320.0;

	private static final	Insets	LIST_BUTTON_PADDING	= new Insets(3.0, 8.0, 3.0, 8.0);

	private static final	Insets	LIST_BUTTON_PANE_PADDING	= new Insets(8.0);

	private static final	Insets	CONTENT_PANE_PADDING	= new Insets(2.0, 0.0, 0.0, 2.0);

	private static final	String	REPLACE_FILES_STR		= "Replace files";
	private static final	String	SELECT_ALL_STR			= "Select all";
	private static final	String	DESELECT_ALL_STR		= "Deselect all";
	private static final	String	INVERT_SELECTION_STR	= "Invert selection";
	private static final	String	EXTRACT_STR				= "Extract";

////////////////////////////////////////////////////////////////////////
//  Class variables
////////////////////////////////////////////////////////////////////////

	private static	State	state	= new State();

////////////////////////////////////////////////////////////////////////
//  Instance variables
////////////////////////////////////////////////////////////////////////

	private	BitSet	result;

////////////////////////////////////////////////////////////////////////
//  Constructors
////////////////////////////////////////////////////////////////////////

	public ReplaceFilesDialog(
		Window				owner,
		List<ZipFileEntry>	entries,
		BitSet				selection,
		Path				directory,
		boolean				flatten)
	{
		// Call superclass constructor
		super(owner, REPLACE_FILES_STR, state.getLocator(), state.getSize());

		// Set properties
		setResizable(true);

		// Extract selected entries
		List<Integer> indices = new ArrayList<>();
		List<ZipFileEntry> items = new ArrayList<>();
		for (int i = selection.nextSetBit(0); i >= 0; i = selection.nextSetBit(i + 1))
		{
			indices.add(i);
			items.add(entries.get(i));
		}

		// Get number of selected entries
		int numItems = items.size();

		// Create list view of locations of files
		SimpleTextListView<ZipFileEntry> listView = new SimpleTextListView<>(items, entry ->
				PathUtils.absString(entry.getOutputFile(directory, flatten)));
		listView.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
		listView.setPrefSize(LIST_VIEW_WIDTH, LIST_VIEW_HEIGHT);
		HBox.setHgrow(listView, Priority.ALWAYS);

		// Create button factory
		IFunction2<Button, String, EventHandler<ActionEvent>> buttonFactory = (text, actionHandler) ->
		{
			// Create button
			Button button = Buttons.hExpansive(text);

			// Set properties
			button.setPadding(LIST_BUTTON_PADDING);
			button.setOnAction(actionHandler);

			// Return button
			return button;
		};

		// Button: select all
		Button selectAllButton = buttonFactory.invoke(SELECT_ALL_STR, event ->
		{
			// Select all entries
			listView.getSelectionModel().selectAll();

			// Request focus on list view
			listView.requestFocus();
		});

		// Button: deselect all
		Button deselectAllButton = buttonFactory.invoke(DESELECT_ALL_STR, event ->
		{
			// Clear selection
			listView.getSelectionModel().clearSelection();

			// Request focus on list view
			listView.requestFocus();
		});

		// Button: invert selection
		Button invertSelectionButton = buttonFactory.invoke(INVERT_SELECTION_STR, event ->
		{
			// Invert selection
			SelectionModelUtils.invertSelection(listView.getSelectionModel(), numItems);

			// Request focus on list view
			listView.requestFocus();
		});

		// Create pane for list-related buttons
		VBox listButtonPane = new VBox(12.0, selectAllButton, deselectAllButton, invertSelectionButton);
		listButtonPane.setAlignment(Pos.TOP_CENTER);
		listButtonPane.setPadding(LIST_BUTTON_PANE_PADDING);

		// Create list-view pane
		HBox listViewPane = new HBox(listView, listButtonPane);
		VBox.setVgrow(listViewPane, Priority.ALWAYS);

		// Create 'number selected' pane
		NumberSelectedPane numSelectedPane = new NumberSelectedPane();

		// Create selection pane
		VBox selectionPane = new VBox(listViewPane, numSelectedPane);

		// Add selection pane to content pane
		addContent(selectionPane);

		// Adjust padding around content pane
		getContentPane().setPadding(CONTENT_PANE_PADDING);

		// Create button: extract
		Button extractButton = Buttons.hNoShrink(EXTRACT_STR);
		extractButton.getProperties().put(BUTTON_GROUP_KEY, BUTTON_GROUP1);
		extractButton.setOnAction(event ->
		{
			// Set result
			result = new BitSet(entries.size());
			for (int index : listView.getSelectionModel().getSelectedIndices())
				result.set(indices.get(index));

			// Close dialog
			requestClose();
		});
		addButton(extractButton, HPos.RIGHT);

		// Create procedure to update 'number selected' label
		IProcedure0 updateNumSelected = () ->
				numSelectedPane.update(numItems, listView.getSelectionModel().getSelectedIndices().size());

		// Update 'number selected' label when selected entries change
		listView.getSelectionModel().getSelectedIndices().addListener((InvalidationListener) observable ->
				updateNumSelected.invoke());

		// Update 'number selected' label
		updateNumSelected.invoke();

		// Create button: cancel
		Button cancelButton = Buttons.hNoShrink(CANCEL_STR);
		cancelButton.getProperties().put(BUTTON_GROUP_KEY, BUTTON_GROUP1);
		cancelButton.setOnAction(event -> requestClose());
		addButton(cancelButton, HPos.RIGHT);

		// Fire 'cancel' button if Escape key is pressed; fire 'extract' button if Ctrl+Enter is pressed
		setKeyFireButton(cancelButton, extractButton);

		// Save dialog state when dialog is closed
		setOnHiding(event -> state.restoreAndUpdate(this, true));

		// Apply new style sheet to scene
		applyStyleSheet();
	}

	//------------------------------------------------------------------

////////////////////////////////////////////////////////////////////////
//  Class methods
////////////////////////////////////////////////////////////////////////

	public static MapNode encodeState()
	{
		return state.encodeTree();
	}

	//------------------------------------------------------------------

	public static void decodeState(
		MapNode	mapNode)
	{
		state.decodeTree(mapNode);
	}

	//------------------------------------------------------------------

////////////////////////////////////////////////////////////////////////
//  Instance methods : overriding methods
////////////////////////////////////////////////////////////////////////

	@Override
	protected BitSet getResult()
	{
		return result;
	}

	//------------------------------------------------------------------

////////////////////////////////////////////////////////////////////////
//  Member classes : non-inner classes
////////////////////////////////////////////////////////////////////////


	// CLASS: STATE


	private static class State
		extends WindowState
	{

	////////////////////////////////////////////////////////////////////
	//  Constructors
	////////////////////////////////////////////////////////////////////

		private State()
		{
			// Call superclass constructor
			super(true, true);
		}

		//--------------------------------------------------------------

	////////////////////////////////////////////////////////////////////
	//  Instance methods
	////////////////////////////////////////////////////////////////////

		/**
		 * Returns a locator function that returns the location from this dialog state.
		 *
		 * @return a locator function that returns the location from this dialog state, or {@code null} if the
		 *         location is {@code null}.
		 */

		private ILocator getLocator()
		{
			Point2D location = getLocation();
			return (location == null) ? null : (width, height) -> location;
		}

		//--------------------------------------------------------------

	}

	//==================================================================

}

//----------------------------------------------------------------------
