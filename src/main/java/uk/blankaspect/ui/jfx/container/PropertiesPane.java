/*====================================================================*\

PropertiesPane.java

Class: properties pane.

\*====================================================================*/


// PACKAGE


package uk.blankaspect.ui.jfx.container;

//----------------------------------------------------------------------


// IMPORTS


import java.lang.invoke.MethodHandles;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javafx.beans.property.ReadOnlyStringProperty;
import javafx.beans.property.SimpleStringProperty;

import javafx.geometry.HPos;
import javafx.geometry.Insets;
import javafx.geometry.Pos;

import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.MenuButton;
import javafx.scene.control.MenuItem;

import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCodeCombination;
import javafx.scene.input.KeyCombination;
import javafx.scene.input.KeyEvent;

import javafx.scene.layout.Background;
import javafx.scene.layout.Border;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Region;

import javafx.scene.paint.Color;

import javafx.stage.Window;
import javafx.stage.WindowEvent;

import uk.blankaspect.common.css.CssSelector;

import uk.blankaspect.common.exception2.BaseException;

import uk.blankaspect.common.function.IFunction1;

import uk.blankaspect.common.tuple.IStrKVPair;

import uk.blankaspect.ui.jfx.button.Buttons;

import uk.blankaspect.ui.jfx.clipboard.ClipboardUtils;

import uk.blankaspect.ui.jfx.dialog.ErrorDialog;
import uk.blankaspect.ui.jfx.dialog.SimpleModalDialog;

import uk.blankaspect.ui.jfx.dialog.SimpleDialog.ILocator;

import uk.blankaspect.ui.jfx.label.OverlayLabel;

import uk.blankaspect.ui.jfx.scene.SceneUtils;

import uk.blankaspect.ui.jfx.style.ColourProperty;
import uk.blankaspect.ui.jfx.style.FxProperty;
import uk.blankaspect.ui.jfx.style.StyleConstants;
import uk.blankaspect.ui.jfx.style.StyleManager;

import uk.blankaspect.ui.jfx.window.WindowUtils;

//----------------------------------------------------------------------


// CLASS: PROPERTIES PANE


/**
 * This class implements a pane in which a set of name&ndash;value pairs may be displayed.
 */

public class PropertiesPane
	extends GridPane
{

////////////////////////////////////////////////////////////////////////
//  Constants
////////////////////////////////////////////////////////////////////////

	/** The default horizontal gap between the name label and value label of a property. */
	private static final	double	DEFAULT_HORIZONTAL_GAP	= 6.0;

	/** The default vertical gap between adjacent properties. */
	private static final	double	DEFAULT_VERTICAL_GAP	= 5.0;

	/** The default padding around a properties pane. */
	private static final	Insets	DEFAULT_PADDING	= Insets.EMPTY;

	/** The default alignment of a properties pane. */
	private static final	Pos		DEFAULT_ALIGNMENT	= Pos.CENTER_LEFT;

	/** The default padding around a value label. */
	private static final	Insets	DEFAULT_VALUE_LABEL_PADDING	= new Insets(2.0, 6.0, 2.0, 6.0);

	/** The default padding around the <i>copy</i> button of a properties dialog. */
	private static final	Insets	DEFAULT_DIALOG_COPY_BUTTON_PADDING	= Insets.EMPTY;

	/** The default padding around the <i>close</i> button of a properties dialog. */
	private static final	Insets	DEFAULT_DIALOG_CLOSE_BUTTON_PADDING	= new Insets(4.0, 16.0, 4.0, 16.0);

	/** The key combination that fires the <i>copy</i> button of a properties dialog. */
	private static final	KeyCombination	KEY_COMBO_COPY	=
			new KeyCodeCombination(KeyCode.C, KeyCombination.CONTROL_DOWN);

	/** Miscellaneous strings. */
	private static final	String	EQUALS_STR			= " = ";
	private static final	String	COPY_STR			= "Copy";
	private static final	String	VALUES_STR			= "Values";
	private static final	String	NAMES_VALUES_STR	= "Names and values";

	/** CSS colour properties. */
	private static final	List<ColourProperty>	COLOUR_PROPERTIES	= List.of
	(
		ColourProperty.of
		(
			FxProperty.TEXT_FILL,
			ColourKey.VALUE_LABEL_TEXT,
			CssSelector.builder()
					.cls(StyleClass.PROPERTIES_PANE)
					.desc(StyleClass.VALUE_LABEL)
					.build()
		),
		ColourProperty.of
		(
			FxProperty.BACKGROUND_COLOUR,
			ColourKey.VALUE_LABEL_BACKGROUND,
			CssSelector.builder()
					.cls(StyleClass.PROPERTIES_PANE)
					.desc(StyleClass.VALUE_LABEL)
					.build()
		),
		ColourProperty.of
		(
			FxProperty.BORDER_COLOUR,
			ColourKey.VALUE_LABEL_BORDER,
			CssSelector.builder()
					.cls(StyleClass.PROPERTIES_PANE)
					.desc(StyleClass.VALUE_LABEL)
					.build()
		),
		ColourProperty.of
		(
			FxProperty.BORDER_COLOUR,
			ColourKey.VALUE_LABEL_POPUP_BORDER,
			CssSelector.builder()
					.cls(StyleClass.VALUE_LABEL_POPUP)
					.build()
		)
	);

	/** CSS style classes. */
	private interface StyleClass
	{
		String	PROPERTIES_PANE		= StyleConstants.CLASS_PREFIX + "properties-pane";
		String	VALUE_LABEL			= StyleConstants.CLASS_PREFIX + "value-label";
		String	VALUE_LABEL_POPUP	= PROPERTIES_PANE + "-value-label-popup";
	}

	/** Keys of colours that are used in colour properties. */
	private interface ColourKey
	{
		String	PREFIX	= StyleManager.colourKeyPrefix(MethodHandles.lookup().lookupClass().getEnclosingClass());

		String	VALUE_LABEL_BACKGROUND		= PREFIX + "valueLabel.background";
		String	VALUE_LABEL_BORDER			= PREFIX + "valueLabel.border";
		String	VALUE_LABEL_POPUP_BORDER	= PREFIX + "valueLabel.popup.border";
		String	VALUE_LABEL_TEXT			= PREFIX + "valueLabel.text";
	}

////////////////////////////////////////////////////////////////////////
//  Instance variables
////////////////////////////////////////////////////////////////////////

	private	Map<String, ReadOnlyStringProperty>	properties;
	private	IFunction1<String, String>			nameConverter;
	private	Insets								valueLabelPadding;
	private	Color								valueLabelBackgroundColour;
	private	Background							valueLabelBackground;
	private	Color								valueLabelBorderColour;
	private	Color								valueLabelPopUpBackgroundColour;
	private	Color								valueLabelPopUpBorderColour;
	private	Border								valueLabelBorder;
	private	double								dialogMaxInitialWidth;
	private	Insets								dialogCopyButtonPadding;
	private	Insets								dialogCloseButtonPadding;
	private	List<OverlayLabel>					valueLabels;

////////////////////////////////////////////////////////////////////////
//  Static initialiser
////////////////////////////////////////////////////////////////////////

	static
	{
		// Register the style properties of this class with the style manager
		StyleManager.INSTANCE.register(PropertiesPane.class, COLOUR_PROPERTIES);
	}

////////////////////////////////////////////////////////////////////////
//  Constructors
////////////////////////////////////////////////////////////////////////

	/**
	 * Creates a new instance of a pane in which a set of name&ndash;value pairs may be displayed.
	 */

	protected PropertiesPane()
	{
		// Initialise instance variables
		properties = new LinkedHashMap<>();
		valueLabelPadding = DEFAULT_VALUE_LABEL_PADDING;
		valueLabelBackgroundColour = getColour(ColourKey.VALUE_LABEL_BACKGROUND);
		valueLabelBackground = SceneUtils.createColouredBackground(valueLabelBackgroundColour);
		valueLabelBorderColour = getColour(ColourKey.VALUE_LABEL_BORDER);
		valueLabelBorder = SceneUtils.createSolidBorder(valueLabelBorderColour);
		valueLabelPopUpBorderColour = getColour(ColourKey.VALUE_LABEL_POPUP_BORDER);
		dialogCopyButtonPadding = DEFAULT_DIALOG_COPY_BUTTON_PADDING;
		dialogCloseButtonPadding = DEFAULT_DIALOG_CLOSE_BUTTON_PADDING;
		valueLabels = new ArrayList<>();

		// Set properties
		setHgap(DEFAULT_HORIZONTAL_GAP);
		setVgap(DEFAULT_VERTICAL_GAP);
		setPadding(DEFAULT_PADDING);
		setAlignment(DEFAULT_ALIGNMENT);
		getStyleClass().add(StyleClass.PROPERTIES_PANE);

		// Initialise column constraints
		ColumnConstraints column = new ColumnConstraints();
		column.setMinWidth(Region.USE_PREF_SIZE);
		column.setHalignment(HPos.RIGHT);
		getColumnConstraints().add(column);

		column = new ColumnConstraints();
		column.setHalignment(HPos.LEFT);
		getColumnConstraints().add(column);
	}

	//------------------------------------------------------------------

////////////////////////////////////////////////////////////////////////
//  Class methods
////////////////////////////////////////////////////////////////////////

	public static PropertiesPane create()
	{
		return new PropertiesPane();
	}

	//------------------------------------------------------------------

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

	public PropertiesPane horizontalGap(
		double	gap)
	{
		// Update pane property
		setHgap((gap < 0.0) ? DEFAULT_HORIZONTAL_GAP : gap);

		// Return this pane
		return this;
	}

	//------------------------------------------------------------------

	public PropertiesPane verticalGap(
		double	gap)
	{
		// Update pane property
		setVgap((gap < 0.0) ? DEFAULT_VERTICAL_GAP : gap);

		// Return this pane
		return this;
	}

	//------------------------------------------------------------------

	public PropertiesPane padding(
		Insets	padding)
	{
		// Update pane property
		setPadding((padding == null) ? DEFAULT_PADDING : padding);

		// Return this pane
		return this;
	}

	//------------------------------------------------------------------

	public PropertiesPane alignment(
		Pos	alignment)
	{
		// Update pane property
		setAlignment((alignment == null) ? DEFAULT_ALIGNMENT : alignment);

		// Return this pane
		return this;
	}

	//------------------------------------------------------------------

	public PropertiesPane nameConverter(
		IFunction1<String, String>	converter)
	{
		// Update instance variable
		nameConverter = converter;

		// Return this pane
		return this;
	}

	//------------------------------------------------------------------

	public PropertiesPane valueLabelPadding(
		Insets	padding)
	{
		// Update instance variable
		valueLabelPadding = (padding == null) ? DEFAULT_VALUE_LABEL_PADDING : padding;

		// Update labels for property values
		for (OverlayLabel label : valueLabels)
			label.setPadding(valueLabelPadding);

		// Return this pane
		return this;
	}

	//------------------------------------------------------------------

	public PropertiesPane valueLabelBackgroundColour(
		Color	colour)
	{
		// Update instance variable
		valueLabelBackgroundColour = (colour == null) ? getColour(ColourKey.VALUE_LABEL_BACKGROUND) : colour;

		// Update labels for property values
		valueLabelBackground = SceneUtils.createColouredBackground(valueLabelBackgroundColour);
		for (OverlayLabel label : valueLabels)
			label.setBackground(valueLabelBackground);

		// Return this pane
		return this;
	}

	//------------------------------------------------------------------

	public PropertiesPane valueLabelBorderColour(
		Color	colour)
	{
		// Update instance variable
		valueLabelBorderColour = (colour == null) ? getColour(ColourKey.VALUE_LABEL_BORDER) : colour;

		// Update labels for property values
		valueLabelBorder = SceneUtils.createSolidBorder(valueLabelBorderColour);
		for (OverlayLabel label : valueLabels)
			label.setBorder(valueLabelBorder);

		// Return this pane
		return this;
	}

	//------------------------------------------------------------------

	public PropertiesPane valueLabelPopUpBackgroundColour(
		Color	colour)
	{
		// Update instance variable
		valueLabelPopUpBackgroundColour = colour;

		// Update labels for property values
		for (OverlayLabel label : valueLabels)
			label.setPopUpBackgroundColour(valueLabelPopUpBackgroundColour);

		// Return this pane
		return this;
	}

	//------------------------------------------------------------------

	public PropertiesPane valueLabelPopUpBorderColour(
		Color	colour)
	{
		// Update instance variable
		valueLabelPopUpBorderColour = (colour == null) ? getColour(ColourKey.VALUE_LABEL_POPUP_BORDER) : colour;

		// Update labels for property values
		for (OverlayLabel label : valueLabels)
			label.setPopUpBorderColour(valueLabelPopUpBorderColour);

		// Return this pane
		return this;
	}

	//------------------------------------------------------------------

	public PropertiesPane dialogMaxInitialWidth(
		double	maxWidth)
	{
		// Update instance variable
		dialogMaxInitialWidth = maxWidth;

		// Return this pane
		return this;
	}

	//------------------------------------------------------------------

	public PropertiesPane dialogCopyButtonPadding(
		Insets	padding)
	{
		// Update instance variable
		dialogCopyButtonPadding = (padding == null) ? DEFAULT_DIALOG_COPY_BUTTON_PADDING : padding;

		// Return this pane
		return this;
	}

	//------------------------------------------------------------------

	public PropertiesPane dialogCloseButtonPadding(
		Insets	padding)
	{
		// Update instance variable
		dialogCloseButtonPadding = (padding == null) ? DEFAULT_DIALOG_CLOSE_BUTTON_PADDING : padding;

		// Return this pane
		return this;
	}

	//------------------------------------------------------------------

	public PropertiesPane clear()
	{
		// Unbind text property of each value label
		valueLabels.forEach(label -> label.textProperty().unbind());

		// Clear map of properties and list of value labels
		properties.clear();
		valueLabels.clear();

		// Remove children of this pane
		getChildren().clear();

		// Return this pane
		return this;
	}

	//------------------------------------------------------------------

	public PropertiesPane property(
		String	name,
		String	value)
	{
		// Add property to map
		properties.put(name, new SimpleStringProperty(value));

		// Create label for property value
		OverlayLabel valueLabel = createValueLabel(name);
		valueLabel.setText(value);

		// Return this pane
		return this;
	}

	//------------------------------------------------------------------

	public PropertiesPane property(
		IStrKVPair	property)
	{
		// Validate argument
		if (property == null)
			throw new IllegalArgumentException("Null property");

		// Add property to map; create label for property value
		return property(property.key(), property.value());
	}

	//------------------------------------------------------------------

	public PropertiesPane property(
		String					name,
		ReadOnlyStringProperty	value)
	{
		// Add property to map
		properties.put(name, value);

		// Create label for property value
		OverlayLabel valueLabel = createValueLabel(name);
		valueLabel.textProperty().bind(value);

		// Return this pane
		return this;
	}

	//------------------------------------------------------------------

	public PropertiesPane properties(
		Iterable<? extends IStrKVPair>	properties)
	{
		// Validate argument
		if (properties == null)
			throw new IllegalArgumentException("Null properties");

		// Clear map of properties and list of value labels; remove children of this pane
		clear();

		// Add properties
		for (IStrKVPair property : properties)
			property(property);

		// Return this pane
		return this;
	}

	//------------------------------------------------------------------

	public PropertiesPane properties1(
		Map<String, String>	properties)
	{
		// Validate argument
		if (properties == null)
			throw new IllegalArgumentException("Null properties");

		// Clear map of properties and list of value labels; remove children of this pane
		clear();

		// Add properties
		for (Map.Entry<String, String> entry : properties.entrySet())
			property(entry.getKey(), entry.getValue());

		// Return this pane
		return this;
	}

	//------------------------------------------------------------------

	public PropertiesPane properties2(
		Map<String, ? extends ReadOnlyStringProperty>	properties)
	{
		// Validate argument
		if (properties == null)
			throw new IllegalArgumentException("Null properties");

		// Clear map of properties and list of value labels; remove children of this pane
		clear();

		// Add properties
		for (Map.Entry<String, ? extends ReadOnlyStringProperty> entry : properties.entrySet())
			property(entry.getKey(), entry.getValue());

		// Return this pane
		return this;
	}

	//------------------------------------------------------------------

	public void showDialog(
		Window	owner,
		String	locationKeySuffix,
		String	title)
	{
		showDialog(owner, locationKeySuffix, title, null);
	}

	//------------------------------------------------------------------

	public void showDialog(
		Window		owner,
		String		title,
		ILocator	locator)
	{
		showDialog(owner, null, title, locator);
	}

	//------------------------------------------------------------------

	protected void showDialog(
		Window		owner,
		String		locationKeySuffix,
		String		title,
		ILocator	locator)
	{
		// Get location key
		String locationKey = null;
		if (locationKeySuffix != null)
		{
			locationKey = MethodHandles.lookup().lookupClass().getCanonicalName();
			if (!locationKeySuffix.isEmpty())
				locationKey += "-" + locationKeySuffix;
		}

		// Create dialog
		SimpleModalDialog<Void> dialog = new SimpleModalDialog<>(owner, locationKey, null, title, 1, locator, null)
		{
			{
				// Set properties
				setResizable(dialogMaxInitialWidth > 0.0);

				// Get properties pane
				PropertiesPane propertiesPane = PropertiesPane.this;
				propertiesPane.setAlignment(Pos.CENTER);

				// Add properties pane to content pane
				addContent(propertiesPane);

				// Create menu button: copy
				MenuButton copyButton = new MenuButton(COPY_STR);
				copyButton.getProperties().put(BUTTON_GROUP_KEY, BUTTON_GROUP1);
				copyButton.setPadding(dialogCopyButtonPadding);
				addButton(copyButton, HPos.LEFT, false);

				// Add menu item: values
				MenuItem menuItem = new MenuItem(VALUES_STR);
				menuItem.setOnAction(event ->
				{
					try
					{
						ClipboardUtils.putTextThrow(valuesToString());
					}
					catch (BaseException e)
					{
						ErrorDialog.show(this, COPY_STR + " : " + VALUES_STR, e);
					}
				});
				copyButton.getItems().add(menuItem);

				// Add menu item: names and values
				menuItem = new MenuItem(NAMES_VALUES_STR);
				menuItem.setOnAction(event ->
				{
					try
					{
						ClipboardUtils.putTextThrow(namesValuesToString());
					}
					catch (BaseException e)
					{
						ErrorDialog.show(this, COPY_STR + " : " + NAMES_VALUES_STR, e);
					}
				});
				copyButton.getItems().add(menuItem);

				// Create button: close
				Button closeButton = Buttons.hNoShrink(CLOSE_STR);
				closeButton.getProperties().put(BUTTON_GROUP_KEY, BUTTON_GROUP1);
				closeButton.setOnAction(event -> requestClose());
				addButton(closeButton, HPos.RIGHT);
				closeButton.setPadding(dialogCloseButtonPadding);

				// Fire 'close' button if Escape key is pressed
				setKeyFireButton(closeButton, null);

				// Resize dialog to scene
				sizeToScene();

				// Fire 'copy' button if Ctrl+C key is pressed
				addEventFilter(KeyEvent.KEY_PRESSED, event ->
				{
					if (KEY_COMBO_COPY.match(event))
					{
						// Fire 'copy' button
						copyButton.fire();

						// Consume event
						event.consume();
					}
				});

				// If dialog is resizable, prevent height of dialog from changing and ensure that initial width of
				// dialog does not exceed maximum
				if (isResizable())
				{
					// When dialog is shown, reduce its width if it exceeds maximum and prevent its height from changing
					addEventHandler(WindowEvent.WINDOW_SHOWN, event ->
					{
						// Reduce width of dialog if it exceeds maximum
						if (getWidth() > dialogMaxInitialWidth)
							setWidth(dialogMaxInitialWidth);

						// Prevent height of dialog from changing
						WindowUtils.preventHeightChange(this);
					});
				}
			}
		};

		// Show dialog
		dialog.showDialog();
	}

	//------------------------------------------------------------------

	protected OverlayLabel createValueLabel(
		String	name)
	{
		// Create label for property value
		OverlayLabel label = new OverlayLabel.Primary();
		label.setPadding(valueLabelPadding);
		label.setBackground(valueLabelBackground);
		label.setBorder(valueLabelBorder);
		label.setPopUpBorderColour(valueLabelPopUpBorderColour);
		label.setPopUpStyleClass(StyleClass.VALUE_LABEL_POPUP);
		label.getStyleClass().add(StyleClass.VALUE_LABEL);
		valueLabels.add(label);

		// Add labels for property name and value to this pane
		addRow(getRowCount(), new Label(name), label);

		// Return label for property value
		return label;
	}

	//------------------------------------------------------------------

	protected String valuesToString()
	{
		StringBuilder buffer = new StringBuilder(256);
		for (String name : properties.keySet())
		{
			buffer.append(properties.get(name).get());
			buffer.append('\n');
		}
		return buffer.toString();
	}

	//------------------------------------------------------------------

	protected String namesValuesToString()
	{
		StringBuilder buffer = new StringBuilder(256);
		for (String name : properties.keySet())
		{
			if (nameConverter == null)
				buffer.append(name);
			else
				buffer.append(nameConverter.invoke(name));
			buffer.append(EQUALS_STR);
			buffer.append(properties.get(name).get());
			buffer.append('\n');
		}
		return buffer.toString();
	}

	//------------------------------------------------------------------

}

//----------------------------------------------------------------------
