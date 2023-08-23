/*====================================================================*\

SimpleComboBox.java

Class: simple combo box.

\*====================================================================*/


// PACKAGE


package uk.blankaspect.ui.jfx.combobox;

//----------------------------------------------------------------------


// IMPORTS


import java.lang.invoke.MethodHandles;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

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
import javafx.scene.layout.StackPane;

import javafx.scene.paint.Color;

import javafx.scene.shape.Rectangle;
import javafx.scene.shape.Shape;

import javafx.stage.Popup;

import javafx.util.StringConverter;

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
	 * A string converter whose type argument is {@link String} and whose {@link StringConverter#toString(Object)
	 * toString(Object)} and {@link StringConverter#fromString(String) fromString(String)} methods return their argument
	 * unchanged.
	 */
	public static final		StringConverter<String>	IDENTITY_STRING_CONVERTER	= new StringConverter<>()
	{
		@Override
		public String toString(
			String	object)
		{
			return object;
		}

		@Override
		public String fromString(
			String	string)
		{
			return string;
		}
	};

	/** The opacity of a disabled component. */
	private static final	double	DISABLED_OPACITY	= 0.4;

	/** The preferred number of rows of the list view. */
	private static final	int		LIST_VIEW_NUM_ROWS	= 10;

	/** The gap between the text and the graphic of a cell of the list view. */
	private static final	double	LIST_VIEW_CELL_GRAPHIC_TEXT_GAP	= 6.0;

	/** The padding at the top and bottom of a cell of the list view. */
	private static final	double	LIST_VIEW_CELL_VERTICAL_PADDING	= 3.0;

	/** The height of a <i>tick</i> icon. */
	private static final	double	LIST_VIEW_TICK_ICON_SIZE	= 0.85 * TextUtils.textHeight();

	/** The key combination that causes the list view to be displayed. */
	private static final	KeyCombination	KEY_COMBO_LIST_TRIGGER	=
			new KeyCodeCombination(KeyCode.SPACE, KeyCombination.CONTROL_DOWN);

	/** The factor by which the height of the default font is multiplied in determining the size of the arrowhead icon
		of the list-view trigger button. */
	private static final	double	BUTTON_ARROWHEAD_SIZE_FACTOR	= 0.8;

	/** CSS colour properties. */
	private static final	List<ColourProperty>	COLOUR_PROPERTIES	= List.of
	(
		ColourProperty.of
		(
			FxProperty.FILL,
			ColourKey.LIST_BUTTON_ICON,
			CssSelector.builder()
						.cls(StyleClass.SIMPLE_COMBO_BOX)
						.desc(StyleClass.LIST_BUTTON)
						.build()
		),
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
	public interface StyleClass
	{
		String	SIMPLE_COMBO_BOX	= StyleConstants.CLASS_PREFIX + "simple-combo-box";

		String	LIST_BUTTON			= StyleConstants.CLASS_PREFIX + "list-button";
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

	private	StringConverter<T>		converter;
	private	SimpleObjectProperty<T>	value;
	private	int						valueIndex;
	private	List<T>					items;
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
		StringConverter<T>	converter)
	{
		// Validate argument
		if (converter == null)
			throw new IllegalArgumentException("Null converter");

		// Initialise instance variables
		this.converter = converter;
		value = new SimpleObjectProperty<>();
		valueIndex = -1;

		// Set properties
		setSpacing(-1.0);
		setAlignment(Pos.CENTER_LEFT);
		setMaxWidth(USE_PREF_SIZE);
		getStyleClass().add(StyleClass.SIMPLE_COMBO_BOX);

		// Create text field
		textField = new TextField();
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
				boolean selected = !cell.isEmpty() && (listView.getSelectionModel().getSelectedIndex() == cell.getIndex());
				boolean hovered = cell.getPseudoClassStates().contains(PseudoClass.getPseudoClass(FxPseudoClass.HOVER));
				cell.pseudoClassStateChanged(PseudoClass.getPseudoClass(FxPseudoClass.SELECTED), selected || hovered);
			});

			// Return cell
			return cell;
		});
		listView.prefWidthProperty().bind(widthProperty().subtract(2.0));

		// Create procedure to update value of combo box
		IProcedure0 updateValue = () ->
		{
			// Update value and text field if item was selected in list view
			int index = listView.getSelectionModel().getSelectedIndex();
			if (index >= 0)
			{
				// Update instance variables
				valueIndex = index;
				value.set(items.get(index));

				// Update text field
				setText(listView.getItems().get(index));
			}

			// Hide pop-up
			popUp.hide();
		};

		// Handle 'key pressed' event on list view
		listView.setOnKeyPressed(event ->
		{
			// Update value
			if (event.getCode() == KeyCode.ENTER)
				updateValue.invoke();

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
				updateValue.invoke();

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

			// Redraw cells of list view
			listView.refresh();

			// Display pop-up
			Bounds bounds = textField.localToScreen(textField.getLayoutBounds());
			popUp.show(textField, bounds.getMinX(), bounds.getMaxY());
		};

		// Handle action event on text field
		textField.addEventHandler(ActionEvent.ACTION, event ->
		{
			T value = converter.fromString(textField.getText());
			if (value != null)
			{
				valueIndex = items.indexOf(value);
				this.value.set(value);
			}
		});

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

		// Create button that triggers list view
		button = new GraphicButton(new Rectangle());
		button.getStyleClass().add(StyleClass.LIST_BUTTON);
		button.setOnAction(event -> showListView.invoke());

		// Create arrowhead icon for button when height of text field is known
		textField.heightProperty().addListener((observable, oldHeight, height) ->
		{
			double textHeight = TextUtils.textHeight();
			Shape arrowhead = Shapes.arrowhead01(VHDirection.DOWN, BUTTON_ARROWHEAD_SIZE_FACTOR * textHeight);
			arrowhead.setFill(getColour(ColourKey.LIST_BUTTON_ICON));
			button.setGraphic(Shapes.tile(arrowhead, Math.ceil(textHeight), Math.rint(height.doubleValue() - 8.0)));
		});

		// Simulate a border around the button
		StackPane buttonPane = new StackPane(button);
		buttonPane.setMaxHeight(StackPane.USE_PREF_SIZE);
		buttonPane.setPadding(new Insets(0.0, 0.0, 0.0, 1.0));
		buttonPane.setBorder(SceneUtils.createSolidBorder(getColour(ColourKey.LIST_BUTTON_PANE_BORDER),
														  Side.TOP, Side.RIGHT, Side.BOTTOM));
		buttonPane.prefHeightProperty().bind(textField.heightProperty());
		buttonPane.getStyleClass().add(StyleClass.LIST_BUTTON_PANE);

		// Reduce opacity of border around button when combo box is disabled
		disabledProperty().addListener((observable, oldDisabled, disabled) ->
				buttonPane.setOpacity(disabled ? DISABLED_OPACITY : 1.0));

		// Add children to this component
		getChildren().addAll(textField, new StackPane(buttonPane, button));
	}

	//------------------------------------------------------------------

	public SimpleComboBox(
		StringConverter<T>		converter,
		Collection<? extends T>	items)
	{
		// Call alternative constructor
		this(converter);

		// Set items
		setItems(items);
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

	public T getValue()
	{
		return value.get();
	}

	//------------------------------------------------------------------

	public void setValue(
		T	value)
	{
		// Update instance variables
		valueIndex = items.indexOf(value);
		this.value.set(value);

		// Update text field
		setText(converter.toString(value));
	}

	//------------------------------------------------------------------

	public ReadOnlyObjectProperty<T> valueProperty()
	{
		return value;
	}

	//------------------------------------------------------------------

	public int getValueIndex()
	{
		return valueIndex;
	}

	//------------------------------------------------------------------

	public TextField getTextField()
	{
		return textField;
	}

	//------------------------------------------------------------------

	public String getText()
	{
		return textField.getText();
	}

	//------------------------------------------------------------------

	public void setText(
		String	text)
	{
		textField.setText(text);
		Platform.runLater(() -> textField.end());
	}

	//------------------------------------------------------------------

	public List<T> getItems()
	{
		return Collections.unmodifiableList(items);
	}

	//------------------------------------------------------------------

	public void setItems(
		Collection<? extends T>	items)
	{
		// Validate argument
		if (items == null)
			throw new IllegalArgumentException("Null items");

		// Update instance variable
		this.items = new ArrayList<>(items);

		// Create list of string representations of items
		ObservableList<String> strings = FXCollections.observableArrayList();
		for (T item : items)
			strings.add(converter.toString(item));

		// Set string representations of items on list view
		listView.setItems(strings);

		// Clear selection
		selectIndex(-1);
	}

	//------------------------------------------------------------------

	public void selectIndex(
		int	index)
	{
		// Update selected index of list view
		listView.getSelectionModel().select(index);

		// Update instance variables
		valueIndex = index;
		value.set((index < 0) ? null : items.get(index));

		// Update text field
		setText((index < 0) ? null : listView.getItems().get(index));
	}

	//------------------------------------------------------------------

	public void selectItem(
		T	item)
	{
		selectIndex(items.indexOf(item));
	}

	//------------------------------------------------------------------

}

//----------------------------------------------------------------------
