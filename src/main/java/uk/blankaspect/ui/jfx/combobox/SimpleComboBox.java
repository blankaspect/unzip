/*====================================================================*\

SimpleComboBox.java

Class: simple combo box.

\*====================================================================*/


// PACKAGE


package uk.blankaspect.ui.jfx.combobox;

//----------------------------------------------------------------------


// IMPORTS


import java.lang.invoke.MethodHandles;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

import javafx.application.Platform;

import javafx.beans.InvalidationListener;

import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.beans.property.SimpleObjectProperty;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import javafx.css.PseudoClass;

import javafx.event.ActionEvent;

import javafx.geometry.Bounds;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.geometry.Side;

import javafx.scene.Group;

import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;

import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCodeCombination;
import javafx.scene.input.KeyCombination;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseButton;

import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;

import javafx.scene.paint.Color;

import javafx.scene.shape.Rectangle;
import javafx.scene.shape.Shape;

import javafx.stage.Popup;

import uk.blankaspect.common.css.CssRuleSet;
import uk.blankaspect.common.css.CssSelector;

import uk.blankaspect.common.function.IProcedure0;

import uk.blankaspect.common.geometry.VHDirection;

import uk.blankaspect.ui.jfx.button.GraphicButton;

import uk.blankaspect.ui.jfx.scene.SceneUtils;

import uk.blankaspect.ui.jfx.shape.Shapes;

import uk.blankaspect.ui.jfx.style.ColourProperty;
import uk.blankaspect.ui.jfx.style.FxProperty;
import uk.blankaspect.ui.jfx.style.FxPseudoClass;
import uk.blankaspect.ui.jfx.style.RuleSetBuilder;
import uk.blankaspect.ui.jfx.style.StyleConstants;
import uk.blankaspect.ui.jfx.style.StyleManager;

import uk.blankaspect.ui.jfx.text.TextUtils;

//----------------------------------------------------------------------


// CLASS: SIMPLE COMBO BOX


public class SimpleComboBox<T>
	extends HBox
{

////////////////////////////////////////////////////////////////////////
//  Constants
////////////////////////////////////////////////////////////////////////

	/**
	 * A string converter whose type argument is {@link String} and whose {@link IConverter#toText(Object)
	 * toText(Object)} and {@link IConverter#fromText(String) fromText(String)} methods return their argument unchanged.
	 */
	public static final		IConverter<String>	IDENTITY_STRING_CONVERTER	= new IConverter<>()
	{
		@Override
		public String toText(
			String	item)
		{
			return item;
		}

		@Override
		public String fromText(
			String	text)
		{
			return text;
		}
	};

	/**
	 * The ways in which the value of an item or the textual representation of an item in the list of items can be
	 * matched against an appropriate target.
	 */
	public enum ListMatch
	{
		/**
		 * The textual representation of an item is matched.
		 */
		TEXT,

		/**
		 * The textual representation of an item is matched, ignoring letter case.
		 */
		TEXT_IGNORE_CASE,

		/**
		 * The value of an item is matched for equality.
		 */
		VALUE,

		/**
		 * The value of an item is matched for identity.
		 */
		VALUE_IDENTITY
	}

	/**
	 * The position at which an item is added to the list of items when it is committed.
	 */
	public enum ListAddPos
	{
		/**
		 * An item is not added to the list of items.
		 */
		NONE,

		/**
		 * An item is added to the front of the list of items.
		 */
		FRONT,

		/**
		 * An item is added to the back of the list of items.
		 */
		BACK
	}

	/** The default way in which an item in the list of items is matched against an appropriate target. */
	private static final	ListMatch	DEFAULT_LIST_MATCH	= ListMatch.VALUE;

	/** The default position at which an item is added to the list of items when it is committed. */
	private static final	ListAddPos	DEFAULT_LIST_ADD_POSITION	= ListAddPos.NONE;

	/** The opacity of a disabled component. */
	private static final	double	DISABLED_OPACITY	= 0.4;

	/** The preferred number of rows of the list view. */
	private static final	int		LIST_VIEW_NUM_ROWS	= 10;

	/** The gap between the text and the graphic of a cell of the list view. */
	private static final	double	LIST_VIEW_CELL_GRAPHIC_TEXT_GAP	= 6.0;

	/** The padding at the top and bottom of a cell of the list view. */
	private static final	double	LIST_VIEW_CELL_VERTICAL_PADDING	= 3.0;

	/** The logical size of a <i>tick</i> icon. */
	private static final	double	LIST_VIEW_TICK_ICON_SIZE	= 0.85 * TextUtils.textHeight();

	/** The key combination that causes the list view to be displayed. */
	private static final	KeyCombination	KEY_COMBO_LIST_TRIGGER	=
			new KeyCodeCombination(KeyCode.SPACE, KeyCombination.CONTROL_DOWN);

	/** The factor by which the height of the default font is multiplied in determining the size of the icon of the
		list-view trigger button. */
	private static final	double	BUTTON_ICON_SIZE_FACTOR	= 0.8;

	/** CSS colour properties. */
	private static final	List<ColourProperty>	COLOUR_PROPERTIES	= List.of
	(
		ColourProperty.of
		(
			FxProperty.BORDER_COLOUR,
			ColourKey.LIST_BUTTON_PANE_BORDER,
			CssSelector.builder()
					.cls(StyleClass.SIMPLE_COMBO_BOX)
					.desc(StyleClass.LIST_BUTTON_PANE)
					.build()
		),
		ColourProperty.of
		(
			FxProperty.STROKE,
			ColourKey.LIST_VIEW_TICK,
			CssSelector.builder()
					.cls(StyleClass.LIST_VIEW_TICK)
					.build()
		)
	);

	/** CSS rule sets. */
	private static final	List<CssRuleSet>	RULE_SETS	= List.of
	(
		RuleSetBuilder.create()
				.selector(CssSelector.builder()
						.cls(StyleClass.SIMPLE_COMBO_BOX)
						.desc(StyleClass.LIST_BUTTON_PANE)
						.build())
				.borders(Side.TOP, Side.RIGHT, Side.BOTTOM)
				.build()
	);

	/** CSS style classes. */
	private interface StyleClass
	{
		String	SIMPLE_COMBO_BOX	= StyleConstants.CLASS_PREFIX + "simple-combo-box";

		String	LIST_BUTTON_PANE	= StyleConstants.CLASS_PREFIX + "list-button-pane";
		String	LIST_VIEW_TICK		= SIMPLE_COMBO_BOX + "-list-view-tick";
	}

	/** Keys of colours that are used in colour properties. */
	private interface ColourKey
	{
		String	PREFIX	= StyleManager.colourKeyPrefix(MethodHandles.lookup().lookupClass().getEnclosingClass());

		String	LIST_BUTTON_ICON		= PREFIX + "listButton.icon";
		String	LIST_BUTTON_PANE_BORDER	= PREFIX + "listButtonPane.border";
		String	LIST_VIEW_TICK			= PREFIX + "listView.tick";
	}

////////////////////////////////////////////////////////////////////////
//  Instance variables
////////////////////////////////////////////////////////////////////////

	private	IConverter<T>			converter;
	private	boolean					commitOnFocusLost;
	private	boolean					allowNullCommit;
	private	ListMatch				listMatch;
	private	ListAddPos				listAddPosition;
	private	SimpleObjectProperty<T>	value;
	private	int						valueIndex;
	private	ObservableList<T>		items;
	private	TextField				textField;
	private	GraphicButton			button;
	private	ListView<String>		listView;
	private	Popup					popUp;

////////////////////////////////////////////////////////////////////////
//  Static initialiser
////////////////////////////////////////////////////////////////////////

	static
	{
		// Register the style properties of this class with the style manager
		StyleManager.INSTANCE.register(SimpleComboBox.class, COLOUR_PROPERTIES, RULE_SETS);
	}

////////////////////////////////////////////////////////////////////////
//  Constructors
////////////////////////////////////////////////////////////////////////

	public SimpleComboBox(
		IConverter<T>	converter)
	{
		// Validate argument
		if (converter == null)
			throw new IllegalArgumentException("Null converter");

		// Initialise instance variables
		this.converter = converter;
		listMatch = DEFAULT_LIST_MATCH;
		listAddPosition = DEFAULT_LIST_ADD_POSITION;
		value = new SimpleObjectProperty<>();
		valueIndex = -1;
		items = FXCollections.observableArrayList();

		// Set properties
		setSpacing(-1.0);
		setAlignment(Pos.CENTER_LEFT);
		setMaxWidth(Region.USE_PREF_SIZE);
		getStyleClass().add(StyleClass.SIMPLE_COMBO_BOX);

		// Create text field
		textField = new TextField()
		{
			@Override
			public void paste()
			{
				onPaste(() -> super.paste());
			}
		};
		HBox.setHgrow(textField, Priority.ALWAYS);

		// Create list view
		double cellHeight = TextUtils.textHeight() + 2.0 * LIST_VIEW_CELL_VERTICAL_PADDING + 1.0;
		listView = new ListView<>();
		listView.setFixedCellSize(cellHeight);
		listView.setCellFactory(listView0 ->
		{
			// Create cell
			ListCell<String> cell = new ListCell<>()
			{
				Group	marker;
				Shape	blank;

				// Constructor
				{
					// Initialise instance variables
					Shape tickIcon = Shapes.tick01(LIST_VIEW_TICK_ICON_SIZE);
					tickIcon.setStroke(getColour(ColourKey.LIST_VIEW_TICK));
					tickIcon.getStyleClass().add(StyleClass.LIST_VIEW_TICK);
					marker = Shapes.tile(tickIcon);
					Bounds bounds = marker.getLayoutBounds();
					blank = new Rectangle(bounds.getWidth(), bounds.getHeight(), Color.TRANSPARENT);

					// Set properties
					setGraphicTextGap(LIST_VIEW_CELL_GRAPHIC_TEXT_GAP);
				}

				@Override
				protected void updateItem(
					String	item,
					boolean	empty)
				{
					// Call superclass method
					super.updateItem(item, empty);

					// Set graphic
					setGraphic((empty || (getIndex() != valueIndex)) ? blank : marker);

					// Set text
					setText(empty ? null : item);
				}
			};

			// Set 'selected' pseudo-class of cell if it is selected or mouse is hovering over it
			cell.getPseudoClassStates().addListener((InvalidationListener) observable ->
			{
				boolean selected =
						!cell.isEmpty() && (listView.getSelectionModel().getSelectedIndex() == cell.getIndex());
				boolean hovered =
						cell.getPseudoClassStates().contains(PseudoClass.getPseudoClass(FxPseudoClass.HOVERED));
				cell.pseudoClassStateChanged(PseudoClass.getPseudoClass(FxPseudoClass.SELECTED), selected || hovered);
			});

			// Return cell
			return cell;
		});
		listView.prefWidthProperty().bind(widthProperty().subtract(2.0));

		// Create procedure to update value of combo box from selection in list view
		IProcedure0 updateValueFromList = () ->
		{
			// Update value and text field if item was selected in list view
			int index = listView.getSelectionModel().getSelectedIndex();
			if (index >= 0)
			{
				// Update instance variables
				valueIndex = index;
				value.set(converter.copy(items.get(index)));

				// Update text field
				text(listView.getItems().get(index));
			}

			// Hide pop-up
			popUp.hide();
		};

		// Handle 'key pressed' event on list view
		listView.setOnKeyPressed(event ->
		{
			// Update value
			if (event.getCode() == KeyCode.ENTER)
				updateValueFromList.invoke();

			// Hide pop-up
			else if (event.getCode() == KeyCode.ESCAPE)
				popUp.hide();

			// Consume event
			event.consume();
		});

		// Update value if mouse is clicked on list view
		listView.setOnMouseClicked(event ->
		{
			if (event.getButton() == MouseButton.PRIMARY)
			{
				// Update value
				updateValueFromList.invoke();

				// Consume event
				event.consume();
			}
		});

		// Create pop-up for list view
		popUp = new Popup();
		popUp.getContent().add(listView);
		popUp.setAutoHide(true);
		popUp.setOnHidden(event ->
		{
			// Request focus on text field
			textField.requestFocus();

			// Enable button
			button.setDisable(false);
		});

		// Create procedure to display list view in pop-up window
		IProcedure0 showListView = () ->
		{
			// Disable button
			button.setDisable(true);

			// Set preferred height of list view
			int numRows = Math.min(Math.max(1, items.size()), LIST_VIEW_NUM_ROWS);
			double height = (double)numRows * cellHeight + 2.0;
			listView.setPrefHeight(height);

			// Clear selection in list view
			listView.getSelectionModel().clearSelection();

			// Create list of string representations of items
			ObservableList<String> strings = FXCollections.observableArrayList();
			for (T item : items)
			{
				String text = converter.toText(item);
				strings.add((text == null) ? "" : text);
			}

			// Set string representations of items on list view
			listView.setItems(strings);

			// Display pop-up
			Bounds bounds = textField.localToScreen(textField.getLayoutBounds());
			popUp.show(textField, bounds.getMinX(), bounds.getMaxY());
		};

		// Handle action event on text field
		textField.addEventHandler(ActionEvent.ACTION, event -> commitValue());

		// Handle 'key pressed' event on text field
		textField.addEventHandler(KeyEvent.KEY_PRESSED, event ->
		{
			// Show list-view pop-up
			if (KEY_COMBO_LIST_TRIGGER.match(event))
			{
				// Show list-view pop-up
				showListView.invoke();

				// Select first item in list view
				if (!listView.getItems().isEmpty())
				{
					listView.getSelectionModel().select(0);
					listView.refresh();
				}

				// Consume event
				event.consume();
			}

			// Hide list-view pop-up
			else if (event.getCode() == KeyCode.ESCAPE)
			{
				// Hide pop-up
				popUp.hide();

				// Consume event
				event.consume();
			}
		});

		// When text field loses focus, optionally commit value
		textField.focusedProperty().addListener((observable, oldFocused, focused) ->
		{
			if (oldFocused && !focused)
			{
				if (commitOnFocusLost)
					commitValue();
				else
					textField.setText(converter.toText(value()));
			}
		});

		// Create icon for button that triggers list view
		double textHeight = TextUtils.textHeight();
		Shape buttonIcon = Shapes.arrowhead01(VHDirection.DOWN, BUTTON_ICON_SIZE_FACTOR * textHeight);
		buttonIcon.setFill(getColour(ColourKey.LIST_BUTTON_ICON));

		// Create button that triggers list view
		button = new GraphicButton(Shapes.tile(buttonIcon, Math.ceil(textHeight)));
		button.setDisable(true);
		button.setOnAction(event -> showListView.invoke());
		button.prefHeightProperty().bind(textField.heightProperty());

		// Disable button if list of items is empty
		items.addListener((InvalidationListener) observable -> button.setDisable(items.isEmpty()));

		// Create pane to provide three-sided border around button
		StackPane buttonPane = new StackPane(button);
		buttonPane.setPadding(new Insets(0.0, 0.0, 0.0, 1.0));
		buttonPane.setBorder(SceneUtils.createSolidBorder(getColour(ColourKey.LIST_BUTTON_PANE_BORDER),
														  Side.TOP, Side.RIGHT, Side.BOTTOM));
		buttonPane.getStyleClass().add(StyleClass.LIST_BUTTON_PANE);

		// Reduce opacity of border around button when combo box is disabled
		disabledProperty().addListener((observable, oldDisabled, disabled) ->
				buttonPane.setOpacity(disabled ? DISABLED_OPACITY : 1.0));

		// Create outer pane for button
		StackPane outerButtonPane = new StackPane(buttonPane, button);
		outerButtonPane.setMaxHeight(Region.USE_PREF_SIZE);

		// Add children to this component
		getChildren().addAll(textField, outerButtonPane);
	}

	//------------------------------------------------------------------

	public SimpleComboBox(
		IConverter<T>			converter,
		Collection<? extends T>	items)
	{
		// Call alternative constructor
		this(converter);

		// Set items
		items(items);
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
//  Instance methods : overriding methods
////////////////////////////////////////////////////////////////////////

	@Override
	public void requestFocus()
	{
		textField.requestFocus();
	}

	//------------------------------------------------------------------

////////////////////////////////////////////////////////////////////////
//  Instance methods
////////////////////////////////////////////////////////////////////////

	public T value()
	{
		return value.get();
	}

	//------------------------------------------------------------------

	public void value(
		T	value)
	{
		// Update instance variables
		valueIndex = findItemIndex(value);
		this.value.set(value);

		// Update text field
		text(converter.toText(value));
	}

	//------------------------------------------------------------------

	public ReadOnlyObjectProperty<T> valueProperty()
	{
		return value;
	}

	//------------------------------------------------------------------

	public int valueIndex()
	{
		return valueIndex;
	}

	//------------------------------------------------------------------

	public TextField textField()
	{
		return textField;
	}

	//------------------------------------------------------------------

	public GraphicButton button()
	{
		return button;
	}

	//------------------------------------------------------------------

	public String text()
	{
		return textField.getText();
	}

	//------------------------------------------------------------------

	public void text(
		String	text)
	{
		textField.setText(text);
		Platform.runLater(textField::end);
	}

	//------------------------------------------------------------------

	public void setTextAndCommit(
		String	text)
	{
		text(text);
		commitValue();
	}

	//------------------------------------------------------------------

	public List<T> items()
	{
		return Collections.unmodifiableList(items);
	}

	//------------------------------------------------------------------

	public void items(
		Collection<? extends T>	items)
	{
		// Validate argument
		if (items == null)
			throw new IllegalArgumentException("Null items");

		// Update instance variable
		this.items.setAll(items);

		// Clear selection
		selectIndex(-1);
	}

	//------------------------------------------------------------------

	public void commitOnFocusLost(
		boolean	commit)
	{
		commitOnFocusLost = commit;
	}

	//------------------------------------------------------------------

	public void allowNullCommit(
		boolean	allow)
	{
		allowNullCommit = allow;
	}

	//------------------------------------------------------------------

	public void lListMatch(
		ListMatch	match)
	{
		// Validate argument
		if (match == null)
			throw new IllegalArgumentException("Null match");

		// Update instance variable
		listMatch = match;
	}

	//------------------------------------------------------------------

	public void listAddPos(
		ListAddPos	position)
	{
		// Validate argument
		if (position == null)
			throw new IllegalArgumentException("Null position");

		// Update instance variable
		listAddPosition = position;
	}

	//------------------------------------------------------------------

	public void selectIndex(
		int	index)
	{
		// Get value from list
		T value = (index < 0) ? null : converter.copy(items.get(index));

		// Update instance variables
		valueIndex = index;
		this.value.set(value);

		// Update text field
		text(converter.toText(value));
	}

	//------------------------------------------------------------------

	public void selectItem(
		T	item)
	{
		selectIndex(findItemIndex(item));
	}

	//------------------------------------------------------------------

	public void commitValue()
	{
		// Get content of text field
		String text = textField.getText();

		// Convert text to value
		T value = converter.fromText(text);

		// Find index of matching item in list
		int index = findItemIndex(text, value);

		// Case: value does not match an existing item
		if (index < 0)
		{
			switch (listAddPosition)
			{
				case NONE:
					// do nothing
					break;

				case FRONT:
					index = 0;
					items.add(index, value);
					break;

				case BACK:
					index = items.size();
					items.add(value);
					break;
			}
		}

		// Case: value matches an existing item
		else
		{
			int i = index;
			index = switch (listAddPosition)
			{
				case NONE  -> index;
				case FRONT -> 0;
				case BACK  -> items.size();
			};
			if (i != index)
				items.add(index, items.remove(i));
		}

		// Update index of item in list
		valueIndex = index;

		// Commit value
		if (allowNullCommit || (value != null))
		{
			// Update instance variables
			this.value.set((value == null) ? null : converter.copy(value));

			// Fire 'action' event
			fireEvent(new ActionEvent());
		}
	}

	//------------------------------------------------------------------

	protected void onPaste(
		Runnable	doPaste)
	{
		// Get content of text field
		String text = textField.getText();

		// Paste text into text field
		doPaste.run();

		// If content of text field has changed, commit value
		if (!Objects.equals(text, textField.getText()))
			commitValue();
	}

	//------------------------------------------------------------------

	private int findItemIndex(
		T	value)
	{
		return findItemIndex(converter.toText(value), value);
	}

	//------------------------------------------------------------------

	private int findItemIndex(
		String	text,
		T		value)
	{
		int index = -1;
		int numItems = items.size();
		switch (listMatch)
		{
			case TEXT:
				if (text != null)
				{
					for (int i = 0; i < numItems; i++)
					{
						if (text.equals(converter.toText(items.get(i))))
						{
							index = i;
							break;
						}
					}
				}
				break;

			case TEXT_IGNORE_CASE:
				if (text != null)
				{
					for (int i = 0; i < numItems; i++)
					{
						if (text.equalsIgnoreCase(converter.toText(items.get(i))))
						{
							index = i;
							break;
						}
					}
				}
				break;

			case VALUE:
				if (value != null)
					index = items.indexOf(value);
				break;

			case VALUE_IDENTITY:
				if (value != null)
				{
					for (int i = 0; i < numItems; i++)
					{
						if (value == items.get(i))
						{
							index = i;
							break;
						}
					}
				}
				break;
		}
		return index;
	}

	//------------------------------------------------------------------

////////////////////////////////////////////////////////////////////////
//  Member interfaces
////////////////////////////////////////////////////////////////////////


	// INTERFACE: CONVERTER


	/**
	 * This interface defines the methods that are used by a {@link SimpleComboBox} to convert an item to and from text
	 * and to create a copy of an item.
	 *
	 * @param <T>
	 *          the type of the item.
	 */

	public interface IConverter<T>
	{

	////////////////////////////////////////////////////////////////////
	//  Methods
	////////////////////////////////////////////////////////////////////

		/**
		 * Returns a textual representation of the specified item.
		 *
		 * @param  item
		 *           the item for which a textual representation is desired.
		 * @return a textual representation of the specified item, or {@code null} if {@code item} is not valid.
		 */

		String toText(
			T	item);

		//--------------------------------------------------------------

		/**
		 * Returns the item that is represented by the specified text.
		 *
		 * @param  text
		 *           the text whose associated item is desired.
		 * @return the item that is represented by {@code text}, or {@code null} if there is no such item.
		 */

		T fromText(
			String	text);

		//--------------------------------------------------------------

		/**
		 * Returns a copy of the specified item.  The default implementation of this method returns the argument itself.
		 *
		 * @param  item
		 *           the item for which a copy is desired.
		 * @return a copy of the specified item.
		 */

		default T copy(
			T	item)
		{
			return item;
		}

		//--------------------------------------------------------------

	}

	//==================================================================

}

//----------------------------------------------------------------------
