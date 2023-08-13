/*====================================================================*\

ListViewEditor.java

Class: list-view editor.

\*====================================================================*/


// PACKAGE


package uk.blankaspect.ui.jfx.listview;

//----------------------------------------------------------------------


// IMPORTS


import java.util.EnumMap;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javafx.collections.ObservableList;

import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.event.EventTarget;

import javafx.geometry.Insets;

import javafx.scene.Node;

import javafx.scene.control.Button;
import javafx.scene.control.ButtonBase;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;

import javafx.scene.image.Image;

import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;

import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;

import javafx.scene.paint.Color;

import uk.blankaspect.common.css.CssSelector;

import uk.blankaspect.common.function.IProcedure0;
import uk.blankaspect.common.function.IProcedure2;

import uk.blankaspect.ui.jfx.button.GraphicButton;
import uk.blankaspect.ui.jfx.button.ImageButton;

import uk.blankaspect.ui.jfx.image.EditorButtonImages01;

import uk.blankaspect.ui.jfx.scene.SceneUtils;

import uk.blankaspect.ui.jfx.style.ColourProperty;
import uk.blankaspect.ui.jfx.style.FxProperty;
import uk.blankaspect.ui.jfx.style.StyleConstants;
import uk.blankaspect.ui.jfx.style.StyleManager;

//----------------------------------------------------------------------


// CLASS: LIST-VIEW EDITOR


/**
 * This class implements an editor for a {@linkplain ListView list view} that allows items to be added to, inserted into
 * or removed from the list, items of the list to be edited, and the order of items in the list to be changed.
 *
 * @param <T>
 *          the type of an item of the list.
 */

public class ListViewEditor<T>
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
						.cls(StyleClass.LIST_VIEW_EDITOR)
						.desc(GraphicButton.StyleClass.GRAPHIC_BUTTON).pseudo(GraphicButton.PseudoClassKey.INACTIVE)
						.desc(GraphicButton.StyleClass.INNER_VIEW)
						.build()
		),
		ColourProperty.of
		(
			FxProperty.STROKE,
			ColourKey.GRAPHIC_BUTTON_BORDER,
			CssSelector.builder()
						.cls(StyleClass.LIST_VIEW_EDITOR)
						.desc(GraphicButton.StyleClass.GRAPHIC_BUTTON).pseudo(GraphicButton.PseudoClassKey.INACTIVE)
						.desc(GraphicButton.StyleClass.INNER_VIEW)
						.build()
		)
	);

	/** CSS style classes. */
	private interface StyleClass
	{
		String	LIST_VIEW_EDITOR	= StyleConstants.CLASS_PREFIX + "list-view-editor";
	}

	/** Keys of colours that are used in colour properties. */
	private interface ColourKey
	{
		String	GRAPHIC_BUTTON_BACKGROUND	= "listViewEditor.graphicButton.background";
		String	GRAPHIC_BUTTON_BORDER		= "listViewEditor.graphicButton.border";
	}

////////////////////////////////////////////////////////////////////////
//  Instance variables
////////////////////////////////////////////////////////////////////////

	/** A map of the action buttons. */
	private	Map<Action, ButtonBase>	buttons;

	/** The list view. */
	private	ListView<T>				listView;

	/** The button pane. */
	private	VBox					buttonPane;

////////////////////////////////////////////////////////////////////////
//  Static initialiser
////////////////////////////////////////////////////////////////////////

	static
	{
		// Register the style properties of this class with the style manager
		StyleManager.INSTANCE.register(ListViewEditor.class, COLOUR_PROPERTIES, ListViewStyle.class);
	}

////////////////////////////////////////////////////////////////////////
//  Constructors
////////////////////////////////////////////////////////////////////////

	/**
	 * Creates a new instance of an editor for the specified list view.
	 *
	 * @param listView
	 *          the list view that will be contained by this editor.
	 * @param editor
	 *          the editor that will be used to manage operations on the list.
	 * @param imageButtons
	 *          if {@code true}, the action buttons will contain images; otherwise, they will contain text.
	 */

	public ListViewEditor(
		ListView<T>	listView,
		IEditor<T>	editor,
		boolean		imageButtons)
	{
		// Initialise instance variables
		buttons = new EnumMap<>(Action.class);
		this.listView = listView;

		// Set properties
		setSpacing(imageButtons ? HGAP_IMAGE_BUTTONS : HGAP_TEXT_BUTTONS);
		getStyleClass().add(StyleClass.LIST_VIEW_EDITOR);

		// Initialise list view
		HBox.setHgrow(listView, Priority.ALWAYS);

		// Create button pane
		buttonPane = new VBox(imageButtons ? VGAP_IMAGE_BUTTONS : VGAP_TEXT_BUTTONS);

		// Create factory for action button
		IProcedure2<Action, EventHandler<ActionEvent>> buttonFactory = (action, actionHandler) ->
		{
			// Initialise button
			ButtonBase button = null;

			// Case: image button
			if (imageButtons)
			{
				// Create button
				ImageButton imageButton = new ImageButton(action.image, action.text);

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
				button = new Button(action.text + ((editor.hasDialog() && action.content) ? ELLIPSIS_STR : ""));

				// Set button properties
				button.setMaxWidth(Double.MAX_VALUE);
				button.setMinHeight(USE_PREF_SIZE);
				button.setPadding(BUTTON_PADDING);
			}

			// Set action-event handler
			button.setOnAction(actionHandler);

			// Add button to map
			buttons.put(action, button);

			// Add button to container
			buttonPane.getChildren().add(button);
		};

		// Create button: add
		if (editor.getActions().contains(Action.ADD))
		{
			buttonFactory.invoke(Action.ADD, event ->
			{
				T item = editor.edit(Action.ADD, null);
				if (item != null)
				{
					getItems().add(item);
					int index = getEndIndex();
					listView.getSelectionModel().select(index);
					listView.scrollTo(index);
				}
			});
		}

		// Create button: insert
		if (editor.getActions().contains(Action.INSERT))
		{
			buttonFactory.invoke(Action.INSERT, event ->
			{
				int index = listView.getSelectionModel().getSelectedIndex();
				if (index >= 0)
				{
					T item = editor.edit(Action.INSERT, null);
					if (item != null)
					{
						getItems().add(index, item);
						listView.getSelectionModel().select(index);
						listView.scrollTo(index);
					}
				}
			});
		}

		// Create button: edit
		if (editor.getActions().contains(Action.EDIT))
		{
			buttonFactory.invoke(Action.EDIT, event ->
			{
				int index = listView.getSelectionModel().getSelectedIndex();
				if (index >= 0)
				{
					T item = editor.edit(Action.EDIT, getItems().get(index));
					if (item != null)
						getItems().set(index, item);
				}
			});
		}

		// Create button: remove
		if (editor.getActions().contains(Action.REMOVE))
		{
			// Create button
			buttonFactory.invoke(Action.REMOVE, event ->
			{
				T item = listView.getSelectionModel().getSelectedItem();
				if (editor.isRemovable(item))
				{
					// Get selected index
					int index = listView.getSelectionModel().getSelectedIndex();

					// If an item is selected and its removal is confirmed, remove it
					if ((index >= 0) && editor.confirmRemove(item))
						getItems().remove(index);
				}
			});

			// Remove selected item unconditionally if mouse is clicked on 'remove' button with Shift key down
			if (editor.allowUnconditionalRemoval())
			{
				buttons.get(Action.REMOVE).addEventHandler(MouseEvent.MOUSE_CLICKED, event ->
				{
					if ((event.getButton() == MouseButton.PRIMARY) && event.isShiftDown()
							&& editor.isRemovable(listView.getSelectionModel().getSelectedItem()))
					{
						// Get selected index
						int index = listView.getSelectionModel().getSelectedIndex();

						// If an item is selected, remove it
						if (index >= 0)
							getItems().remove(index);

						// Consume mouse event
						event.consume();
					}
				});
			}
		}

		// Remove selected item if Delete key is pressed
		if (editor.canRemoveWithKeyPress())
		{
			listView.addEventHandler(KeyEvent.KEY_PRESSED, event ->
			{
				if (event.getCode() == KeyCode.DELETE)
				{
					T item = listView.getSelectionModel().getSelectedItem();
					if (editor.isRemovable(item))
					{
						// Get selected index
						int index = listView.getSelectionModel().getSelectedIndex();

						// If an item is selected and its removal is allowed, remove it
						if ((index >= 0) && (event.isShiftDown() || editor.confirmRemove(item)))
							getItems().remove(index);
					}
				}
			});
		}

		// Create button: move up
		if (editor.getActions().contains(Action.MOVE_UP))
		{
			buttonFactory.invoke(Action.MOVE_UP, event ->
			{
				int index = listView.getSelectionModel().getSelectedIndex();
				if (index > 0)
				{
					T item = getItems().remove(index);
					getItems().add(--index, item);
					listView.getSelectionModel().select(index);
					listView.scrollTo(index);
				}
			});
		}

		// Create button: move down
		if (editor.getActions().contains(Action.MOVE_DOWN))
		{
			buttonFactory.invoke(Action.MOVE_DOWN, event ->
			{
				int index = listView.getSelectionModel().getSelectedIndex();
				if ((index >= 0) && (index < getEndIndex()))
				{
					T item = getItems().remove(index);
					getItems().add(++index, item);
					listView.getSelectionModel().select(index);
					listView.scrollTo(index);
				}
			});
		}

		// Create procedure to update buttons
		IProcedure0 updateButtons = () ->
		{
			int index = listView.getSelectionModel().getSelectedIndex();
			boolean noSelection = (index < 0);

			for (Action action : buttons.keySet())
			{
				ButtonBase button = buttons.get(action);
				switch (action)
				{
					case ADD:
						button.setDisable(false);
						break;

					case INSERT:
					case EDIT:
						button.setDisable(noSelection);
						break;

					case REMOVE:
						button.setDisable(noSelection || !editor.isRemovable(listView.getSelectionModel().getSelectedItem()));
						break;

					case MOVE_UP:
						button.setDisable(index < 1);
						break;

					case MOVE_DOWN:
						button.setDisable(noSelection || (index >= getEndIndex()));
						break;
				}
			}
		};

		// Update buttons when selection in list view changes
		listView.getSelectionModel().selectedIndexProperty().addListener(observable -> updateButtons.invoke());

		// Update buttons
		updateButtons.invoke();

		// Fire 'edit' button when mouse is double-clicked on list
		ButtonBase button = buttons.get(Action.EDIT);
		if (button != null)
		{
			listView.addEventHandler(MouseEvent.MOUSE_CLICKED, event ->
			{
				if ((event.getButton() == MouseButton.PRIMARY) && (event.getClickCount() == 2))
				{
					ListCell<T> cell = findCell(event);
					if ((cell != null) && (cell.getIndex() == listView.getSelectionModel().getSelectedIndex()))
						button.fire();
				}
			});
		}

		// Add children to this container
		getChildren().addAll(listView, buttonPane);
	}

	//------------------------------------------------------------------

////////////////////////////////////////////////////////////////////////
//  Class methods
////////////////////////////////////////////////////////////////////////

	/**
	 * Returns the colour that is associated with the specified key in the colour map of the selected theme of the
	 * {@linkplain StyleManager style manager}.
	 *
	 * @param  key
	 *           the key of the desired colour.
	 * @return the colour that is associated with {@code key} in the colour map of the selected theme of the style
	 *         manager, or {@link StyleManager#DEFAULT_COLOUR} if there is no such colour.
	 */

	private static Color getColour(
		String	key)
	{
		Color colour = StyleManager.INSTANCE.getColour(key);
		return (colour == null) ? StyleManager.DEFAULT_COLOUR : colour;
	}

	//------------------------------------------------------------------

////////////////////////////////////////////////////////////////////////
//  Instance methods
////////////////////////////////////////////////////////////////////////

	/**
	 * Returns the list view of this editor.
	 *
	 * @return the list view of this editor.
	 */

	public ListView<T> getListView()
	{
		return listView;
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
	 * Returns an observable list of the items of the list view.
	 *
	 * @return an observable list of the items of the list view.
	 */

	public ObservableList<T> getItems()
	{
		return listView.getItems();
	}

	//------------------------------------------------------------------

	/**
	 * Returns the index of the last item of the list view.
	 *
	 * @return the index of the last item of the list view, or -1 if the list is empty.
	 */

	private int getEndIndex()
	{
		return listView.getItems().size() - 1;
	}

	//------------------------------------------------------------------

	/**
	 * Returns the cell of the list view that is the target of the specified mouse event.
	 *
	 * @param  event
	 *           the mouse event for which the targetted cell of the list view is required.
	 * @return the cell of the list view that is the target of {@code event}, or {@code null} if there is no such cell.
	 */

	@SuppressWarnings("unchecked")
	private ListCell<T> findCell(
		MouseEvent	event)
	{
		EventTarget target = event.getTarget();
		Node node = SceneUtils.searchAscending((Node)target, listView, false, node0 -> node0 instanceof ListCell<?>);
		return (node instanceof ListCell<?> cell) ? (ListCell<T>)cell : null;
	}

	//------------------------------------------------------------------

////////////////////////////////////////////////////////////////////////
//  Enumerated types
////////////////////////////////////////////////////////////////////////


	// ENUMERATION: ACTION


	/**
	 * This is an enumeration of the actions that may be performed on the items of a list.
	 */

	public enum Action
	{

	////////////////////////////////////////////////////////////////////
	//  Constants
	////////////////////////////////////////////////////////////////////

		/**
		 * Add an item to the end of the list.
		 */
		ADD
		(
			"Add",
			EditorButtonImages01.ADD,
			true
		),

		/**
		 * Insert an item into the list at the index of the current selection.
		 */
		INSERT
		(
			"Insert",
			EditorButtonImages01.INSERT,
			true
		),

		/**
		 * Edit the selected item of the list.
		 */
		EDIT
		(
			"Edit",
			EditorButtonImages01.EDIT,
			true
		),

		/**
		 * Remove the selected item from the list.
		 */
		REMOVE
		(
			"Remove",
			EditorButtonImages01.REMOVE,
			false
		),

		/**
		 * Move the selected item up in the list (ie, decrement the index of the item).
		 */
		MOVE_UP
		(
			"Move up",
			EditorButtonImages01.MOVE_UP,
			false
		),

		/**
		 * Move the selected item down in the list (ie, increment the index of the item).
		 */
		MOVE_DOWN
		(
			"Move down",
			EditorButtonImages01.MOVE_DOWN,
			false
		);

	////////////////////////////////////////////////////////////////////
	//  Instance variables
	////////////////////////////////////////////////////////////////////

		/** The text that represents this action. */
		private	String	text;

		/** The image that represents this action. */
		private	Image	image;

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
		 * @param image
		 *          the image that will represent the action.
		 * @param content
		 *          if (@code true}, the action relates to the content of an item.
		 */

		private Action(
			String	text,
			Image	image,
			boolean	content)
		{
			// Initialise instance variables
			this.text = text;
			this.image = image;
			this.content = content;
		}

		//--------------------------------------------------------------

	////////////////////////////////////////////////////////////////////
	//  Instance methods
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
	 * This interface defines the methods that must be implemented by the editor of an item of a {@linkplain ListView
	 * list view}.
	 *
	 * @param <T>
	 *          the type of an item of the list.
	 */

	public interface IEditor<T>
	{

	////////////////////////////////////////////////////////////////////
	//  Methods
	////////////////////////////////////////////////////////////////////

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
		 * Returns {@code true} if the specified item may be removed from the list.  By default, this method returns
		 * {@code true}.
		 *
		 * @param  item
		 *           the item of interest.
		 * @return {@code true} if {@code item} may be removed from the list.
		 */

		default boolean isRemovable(
			T	item)
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
