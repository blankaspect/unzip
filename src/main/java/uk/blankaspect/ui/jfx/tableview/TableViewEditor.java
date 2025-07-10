/*====================================================================*\

TableViewEditor.java

Class: table-view editor.

\*====================================================================*/


// PACKAGE


package uk.blankaspect.ui.jfx.tableview;

//----------------------------------------------------------------------


// IMPORTS


import java.lang.invoke.MethodHandles;

import java.util.EnumMap;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javafx.application.Platform;

import javafx.beans.InvalidationListener;

import javafx.geometry.Insets;

import javafx.scene.Node;

import javafx.scene.control.ButtonBase;
import javafx.scene.control.TableRow;
import javafx.scene.control.TableView;

import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;

import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;

import javafx.scene.paint.Color;

import uk.blankaspect.common.css.CssSelector;

import uk.blankaspect.common.function.IProcedure0;

import uk.blankaspect.ui.jfx.button.Buttons;
import uk.blankaspect.ui.jfx.button.GraphicButton;
import uk.blankaspect.ui.jfx.button.ImageDataButton;

import uk.blankaspect.ui.jfx.image.EditorButtonImages01;

import uk.blankaspect.ui.jfx.scene.SceneUtils;

import uk.blankaspect.ui.jfx.style.ColourProperty;
import uk.blankaspect.ui.jfx.style.FxProperty;
import uk.blankaspect.ui.jfx.style.StyleConstants;
import uk.blankaspect.ui.jfx.style.StyleManager;

//----------------------------------------------------------------------


// CLASS: TABLE-VIEW EDITOR


/**
 * This class implements an editor for a {@linkplain TableView table view} that allows items to be added to, inserted
 * into or deleted from the table, items of the table to be edited, and the order of items in the table to be changed.
 *
 * @param <T>
 *          the type of an item of the table.
 */

public class TableViewEditor<T>
	extends HBox
{

////////////////////////////////////////////////////////////////////////
//  Constants
////////////////////////////////////////////////////////////////////////

	/** The horizontal gap between adjacent components of the pane if the buttons contain text. */
	private static final	double	HGAP_TEXT_BUTTONS	= 8.0;

	/** The horizontal gap between adjacent components of the pane if the buttons contain images. */
	private static final	double	HGAP_IMAGE_BUTTONS	= 6.0;

	/** The gap between adjacent buttons that contain text. */
	private static final	double	VGAP_TEXT_BUTTONS	= 6.0;

	/** The gap between adjacent buttons that contain images. */
	private static final	double	VGAP_IMAGE_BUTTONS	= 4.0;

	/** The padding around a text button. */
	private static final	Insets	BUTTON_PADDING	= new Insets(2.0, 6.0, 2.0, 6.0);

	/** Miscellaneous strings. */
	private static final	String	ELLIPSIS_STR	= "...";

	/** CSS colour properties. */
	private static final	List<ColourProperty>	COLOUR_PROPERTIES	= List.of
	(
		ColourProperty.of
		(
			FxProperty.FILL,
			ColourKey.GRAPHIC_BUTTON_BACKGROUND,
			CssSelector.builder()
					.cls(StyleClass.TABLE_VIEW_EDITOR)
					.desc(GraphicButton.StyleClass.GRAPHIC_BUTTON).pseudo(GraphicButton.PseudoClassKey.INACTIVE)
					.desc(GraphicButton.StyleClass.INNER_VIEW)
					.build()
		),
		ColourProperty.of
		(
			FxProperty.STROKE,
			ColourKey.GRAPHIC_BUTTON_BORDER,
			CssSelector.builder()
					.cls(StyleClass.TABLE_VIEW_EDITOR)
					.desc(GraphicButton.StyleClass.GRAPHIC_BUTTON).pseudo(GraphicButton.PseudoClassKey.INACTIVE)
					.desc(GraphicButton.StyleClass.INNER_VIEW)
					.build()
		)
	);

	/** CSS style classes. */
	private interface StyleClass
	{
		String	TABLE_VIEW_EDITOR	= StyleConstants.CLASS_PREFIX + "table-view-editor";
	}

	/** Keys of colours that are used in colour properties. */
	private interface ColourKey
	{
		String	PREFIX	= StyleManager.colourKeyPrefix(MethodHandles.lookup().lookupClass().getEnclosingClass());

		String	GRAPHIC_BUTTON_BACKGROUND	= PREFIX + "graphicButton.background";
		String	GRAPHIC_BUTTON_BORDER		= PREFIX + "graphicButton.border";
	}

////////////////////////////////////////////////////////////////////////
//  Instance variables
////////////////////////////////////////////////////////////////////////

	/** A map of the action buttons. */
	private	Map<Action, ButtonBase>	buttons;

	/** The table view. */
	private	TableView<T>			tableView;

	/** The button pane. */
	private	VBox					buttonPane;

////////////////////////////////////////////////////////////////////////
//  Static initialiser
////////////////////////////////////////////////////////////////////////

	static
	{
		// Register the style properties of this class and its dependencies with the style manager
		StyleManager.INSTANCE.register(TableViewEditor.class, COLOUR_PROPERTIES,
									   TableViewStyle.class);

		// Initialise button images
		EditorButtonImages01.init();
	}

////////////////////////////////////////////////////////////////////////
//  Constructors
////////////////////////////////////////////////////////////////////////

	/**
	 * Creates a new instance of an editor for the specified table view.
	 *
	 * @param tableView
	 *          the table view that will be contained by this editor.
	 * @param editor
	 *          the editor that will be used to manage actions on the table.
	 * @param imageButtons
	 *          if {@code true}, the action buttons will contain images; otherwise, they will contain text.
	 */

	public TableViewEditor(
		TableView<T>	tableView,
		IEditor<T>		editor,
		boolean			imageButtons)
	{
		// Initialise instance variables
		buttons = new EnumMap<>(Action.class);
		this.tableView = tableView;

		// Set properties
		setSpacing(imageButtons ? HGAP_IMAGE_BUTTONS : HGAP_TEXT_BUTTONS);
		getStyleClass().add(StyleClass.TABLE_VIEW_EDITOR);

		// Initialise table view
		HBox.setHgrow(tableView, Priority.ALWAYS);

		// Create button pane
		buttonPane = new VBox(imageButtons ? VGAP_IMAGE_BUTTONS : VGAP_TEXT_BUTTONS);
		buttonPane.setMinWidth(Region.USE_PREF_SIZE);

		// Create buttons
		for (Action action : editor.getActions())
		{
			// Don't create button for unsupported action
			if (!editor.getActions().contains(action))
				continue;

			// Initialise button
			ButtonBase button = null;

			// Case: image button
			if (imageButtons)
			{
				// Create button
				ImageDataButton imageButton = new ImageDataButton(action.imageId, action.text);

				// Set button properties
				imageButton.setPadding(Insets.EMPTY);
				imageButton.setBackgroundColour(getColour(ColourKey.GRAPHIC_BUTTON_BACKGROUND));
				imageButton.setBorderColour(getColour(ColourKey.GRAPHIC_BUTTON_BORDER));

				// Set button
				button = imageButton;
			}

			// Case: text button
			else
			{
				// Create button
				button = Buttons.hNoShrink(action.text + ((editor.hasDialog() && action.content) ? ELLIPSIS_STR : ""));

				// Set button properties
				button.setMaxWidth(Double.MAX_VALUE);
				button.setMinHeight(Region.USE_PREF_SIZE);
				button.setPadding(BUTTON_PADDING);
			}

			// Set action-event handler
			button.setOnAction(event ->
			{
				boolean singleSelection = (tableView.getSelectionModel().getSelectedIndices().size() == 1);
				T item = tableView.getSelectionModel().getSelectedItem();
				boolean allowed = singleSelection && editor.allowAction(action, item);

				switch (action)
				{
					case ADD:
					{
						if ((item == null) || allowed)
						{
							item = editor.edit(action, null);
							if (item != null)
							{
								getItems().add(item);
								int index = getEndIndex();
								tableView.getSelectionModel().select(index);
								tableView.scrollTo(index);
							}
						}
						break;
					}

					case INSERT:
					{
						if (allowed)
						{
							int index = tableView.getSelectionModel().getSelectedIndex();
							item = editor.edit(action, null);
							if (item != null)
							{
								getItems().add(index, item);
								tableView.getSelectionModel().select(index);
								tableView.scrollTo(index);
							}
						}
						break;
					}

					case EDIT:
					{
						if (allowed)
						{
							int index = tableView.getSelectionModel().getSelectedIndex();
							item = editor.edit(action, getItems().get(index));
							if (item != null)
								getItems().set(index, item);
						}
						break;
					}

					case REMOVE:
					{
						if (allowed)
						{
							// Get selected index
							int index = tableView.getSelectionModel().getSelectedIndex();

							// If removal of item is confirmed, remove it
							if (editor.confirmRemove(item))
								getItems().remove(index);
						}
						break;
					}

					case MOVE_UP:
					{
						if (allowed)
						{
							int index = tableView.getSelectionModel().getSelectedIndex();
							if (index > 0)
							{
								item = getItems().remove(index);
								getItems().add(--index, item);
								tableView.getSelectionModel().select(index);
								tableView.scrollTo(index);
							}
						}
						break;
					}

					case MOVE_DOWN:
					{
						if (allowed)
						{
							int index = tableView.getSelectionModel().getSelectedIndex();
							if (index < getEndIndex())
							{
								item = getItems().remove(index);
								getItems().add(++index, item);
								tableView.getSelectionModel().select(index);
								tableView.scrollTo(index);
							}
						}
						break;
					}
				}
			});

			// Add button to map
			buttons.put(action, button);

			// Add button to container
			buttonPane.getChildren().add(button);

			// Remove selected item if mouse is clicked on 'remove' button with Shift key down and item is removable
			if ((action == Action.REMOVE) && editor.allowUnconditionalRemoval())
			{
				button.addEventHandler(MouseEvent.MOUSE_CLICKED, event ->
				{
					if ((event.getButton() == MouseButton.PRIMARY) && event.isShiftDown())
					{
						// If a removable item is selected, remove it
						T item = tableView.getSelectionModel().getSelectedItem();
						if ((item != null) && editor.allowAction(action, item))
							getItems().remove(tableView.getSelectionModel().getSelectedIndex());

						// Consume mouse event
						event.consume();
					}
				});
			}
		}

		// Remove selected item if Delete key is pressed
		if (editor.canRemoveWithKeyPress())
		{
			tableView.addEventHandler(KeyEvent.KEY_PRESSED, event ->
			{
				if (event.getCode() == KeyCode.DELETE)
				{
					T item = tableView.getSelectionModel().getSelectedItem();
					if ((item != null) && editor.allowAction(Action.REMOVE, item))
					{
						// Remove item if Shift key is pressed or removal of item is confirmed
						if (event.isShiftDown() || editor.confirmRemove(item))
							getItems().remove(tableView.getSelectionModel().getSelectedIndex());
					}
				}
			});
		}

		// Create procedure to update buttons
		IProcedure0 updateButtons = () ->
		{
			T item = tableView.getSelectionModel().getSelectedItem();
			int index = tableView.getSelectionModel().getSelectedIndex();
			int numSelected = tableView.getSelectionModel().getSelectedIndices().size();
			boolean singleSelection = (numSelected == 1);

			for (Action action : buttons.keySet())
			{
				ButtonBase button = buttons.get(action);
				if (singleSelection && !editor.allowAction(action, item))
					button.setDisable(true);
				else
				{
					switch (action)
					{
						case ADD:
							button.setDisable(numSelected > 1);
							break;

						case INSERT:
						case EDIT:
						case REMOVE:
							button.setDisable(!singleSelection);
							break;

						case MOVE_UP:
							button.setDisable(!singleSelection || (index < 1));
							break;

						case MOVE_DOWN:
							button.setDisable(!singleSelection || (index < 0) || (index >= getEndIndex()));
							break;
					}
				}
			}
		};

		// Update buttons when selection in table view changes
		tableView.getSelectionModel().getSelectedIndices().addListener((InvalidationListener) observable ->
				Platform.runLater(updateButtons::invoke));

		// Update buttons
		updateButtons.invoke();

		// Fire 'edit' button when mouse is double-clicked on table
		ButtonBase button = buttons.get(Action.EDIT);
		if (button != null)
		{
			tableView.addEventHandler(MouseEvent.MOUSE_CLICKED, event ->
			{
				if ((event.getButton() == MouseButton.PRIMARY) && (event.getClickCount() == 2)
						&& (event.getTarget() instanceof Node target))
				{
					Node node = SceneUtils.searchAscending(target, tableView, false,
														   node0 -> node0 instanceof TableRow<?>);
					if ((node instanceof TableRow<?> row)
							&& (row.getIndex() == tableView.getSelectionModel().getSelectedIndex()))
						button.fire();
				}
			});
		}

		// Add children to this container
		getChildren().addAll(tableView, buttonPane);
	}

	//------------------------------------------------------------------

////////////////////////////////////////////////////////////////////////
//  Class methods
////////////////////////////////////////////////////////////////////////

	/**
	 * Returns the colour that is associated with the specified key in the colour map of the current theme of the
	 * {@linkplain StyleManager style manager}.
	 *
	 * @param  key
	 *           the key of the desired colour.
	 * @return the colour that is associated with {@code key} in the colour map of the current theme of the style
	 *         manager, or {@link StyleManager#DEFAULT_COLOUR} if there is no such colour.
	 */

	private static Color getColour(
		String	key)
	{
		return StyleManager.INSTANCE.getColourOrDefault(key);
	}

	//------------------------------------------------------------------

////////////////////////////////////////////////////////////////////////
//  Instance methods
////////////////////////////////////////////////////////////////////////

	/**
	 * Returns the table view of this editor.
	 *
	 * @return the table view of this editor.
	 */

	public TableView<T> getTableView()
	{
		return tableView;
	}

	//------------------------------------------------------------------

	/**
	 * Returns the button pane of this editor.
	 *
	 * @return the button pane of this editor.
	 */

	public VBox getButtonPane()
	{
		return buttonPane;
	}

	//------------------------------------------------------------------

	/**
	 * Returns the button that is associated with the specified action.
	 *
	 * @param  action
	 *           the action whose associated button is desired.
	 * @return the button that is associated with {@code action}.
	 */

	public ButtonBase getButton(
		Action	action)
	{
		return buttons.get(action);
	}

	//------------------------------------------------------------------

	/**
	 * Returns a list of the items of the table view.
	 *
	 * @return a list of the items of the table view.
	 */

	public List<T> getItems()
	{
		return tableView.getItems();
	}

	//------------------------------------------------------------------

	/**
	 * Returns the index of the last item of the table view.
	 *
	 * @return the index of the last item of the table view, or -1 if the table is empty.
	 */

	private int getEndIndex()
	{
		return tableView.getItems().size() - 1;
	}

	//------------------------------------------------------------------

////////////////////////////////////////////////////////////////////////
//  Enumerated types
////////////////////////////////////////////////////////////////////////


	// ENUMERATION: ACTION


	/**
	 * This is an enumeration of the actions that may be performed on the items of a table.
	 */

	public enum Action
	{

	////////////////////////////////////////////////////////////////////
	//  Constants
	////////////////////////////////////////////////////////////////////

		/**
		 * Add an item to the end of the table.
		 */
		ADD
		(
			"Add",
			EditorButtonImages01.ImageId.ADD,
			true
		),

		/**
		 * Insert an item into the table at the index of the current selection.
		 */
		INSERT
		(
			"Insert",
			EditorButtonImages01.ImageId.INSERT,
			true
		),

		/**
		 * Edit the selected item of the table.
		 */
		EDIT
		(
			"Edit",
			EditorButtonImages01.ImageId.EDIT,
			true
		),

		/**
		 * Remove the selected item from the table.
		 */
		REMOVE
		(
			"Remove",
			EditorButtonImages01.ImageId.REMOVE,
			false
		),

		/**
		 * Move the selected item up in the table (ie, decrement the index of the item).
		 */
		MOVE_UP
		(
			"Move up",
			EditorButtonImages01.ImageId.MOVE_UP,
			false
		),

		/**
		 * Move the selected item down in the table (ie, increment the index of the item).
		 */
		MOVE_DOWN
		(
			"Move down",
			EditorButtonImages01.ImageId.MOVE_DOWN,
			false
		);

	////////////////////////////////////////////////////////////////////
	//  Instance variables
	////////////////////////////////////////////////////////////////////

		/** The text that represents this action. */
		private	String	text;

		/** The identifier of the image that represents this action. */
		private	String	imageId;

		/** Flag: if (@code true}, this action relates to the content of an item. */
		private	boolean	content;

	////////////////////////////////////////////////////////////////////
	//  Constructors
	////////////////////////////////////////////////////////////////////

		/**
		 * Creates a new instance of an enumeration constant for an action.
		 *
		 * @param text
		 *          the text that will represent the action.
		 * @param imageId
		 *          the identifier of the image that will represent the action.
		 * @param content
		 *          if (@code true}, the action relates to the content of an item.
		 */

		private Action(
			String	text,
			String	imageId,
			boolean	content)
		{
			// Initialise instance variables
			this.text = text;
			this.imageId = imageId;
			this.content = content;
		}

		//--------------------------------------------------------------

	////////////////////////////////////////////////////////////////////
	//  Instance methods : overriding methods
	////////////////////////////////////////////////////////////////////

		/**
		 * {@inheritDoc}
		 */

		@Override
		public String toString()
		{
			return text;
		}

		//--------------------------------------------------------------

	}

	//==================================================================

////////////////////////////////////////////////////////////////////////
//  Member interfaces
////////////////////////////////////////////////////////////////////////


	// INTERFACE: EDITOR


	/**
	 * This interface defines the methods that must be implemented by the editor of an item of a {@linkplain TableView
	 * table view}.
	 *
	 * @param <T>
	 *          the type of an item of the table.
	 */

	public interface IEditor<T>
	{

	////////////////////////////////////////////////////////////////////
	//  Methods
	////////////////////////////////////////////////////////////////////

		/**
		 * Returns the {@linkplain Action actions} that are supported by this editor.  By default, this method returns
		 * all the available actions.
		 *
		 * @return the actions that are supported by this editor.
		 */

		default Set<Action> getActions()
		{
			return EnumSet.allOf(Action.class);
		}

		//--------------------------------------------------------------

		/**
		 * Returns {@code true} if the specified action may be performed on the specified item.  By default, this method
		 * returns {@code true}.
		 *
		 * @param  action
		 *           the action to be performed.
		 * @param  item
		 *           the item of interest.
		 * @return {@code true} if {@code action} may be performed on {@code item}.
		 */

		default boolean allowAction(
			Action	action,
			T		item)
		{
			return true;
		}

		//--------------------------------------------------------------

		/**
		 * Edits the specified item and returns the result.
		 *
		 * @param  action
		 *           the editing action.
		 * @param  item
		 *           the item that will be edited, or {@code null} if a new item should be created.
		 * @return the item that is the result of editing, or {@code null} if there was no result.
		 */

		T edit(
			Action	action,
			T		item);

		//--------------------------------------------------------------

		/**
		 * Returns {@code true} if this editor has a dialog.
		 *
		 * @return {@code true} if this editor has a dialog.
		 */

		boolean hasDialog();

		//--------------------------------------------------------------

		/**
		 * Returns {@code true} if this editor allows the selected item to be removed unconditionally in response to a
		 * modified <i>remove</i> command.  By default, this method returns {@code true}.
		 *
		 * @return {@code true} if this editor allows the selected item to be removed unconditionally in response to a
		 *         modified <i>remove</i> command.
		 */

		default boolean allowUnconditionalRemoval()
		{
			return true;
		}

		//--------------------------------------------------------------

		/**
		 * Returns {@code true} if the <i>Delete</i> key may be used to remove the selected item.  This method is
		 * ignored if {@link #canRemove(Object)} prevents the removal of the item by returning {@code false}.  By
		 * default, this method returns {@code false}.
		 *
		 * @return {@code true} if the <i>Delete</i> key may be used to remove the selected item.
		 */

		default boolean canRemoveWithKeyPress()
		{
			return false;
		}

		//--------------------------------------------------------------

		/**
		 * Returns {@code true} if the specified item can be removed in response to a <i>remove</i> command.  This
		 * method may be overridden to seek confirmation from the user for the removal of the item.  By default, this
		 * method returns {@code true}.
		 *
		 * @param  item
		 *           the item for which a <i>remove</i> command was issued.
		 * @return {@code true} if {@code item} can be removed in response to a <i>remove</i> command.
		 */

		default boolean confirmRemove(
			T	item)
		{
			return true;
		}

		//--------------------------------------------------------------

	}

	//==================================================================

}

//----------------------------------------------------------------------
