/*====================================================================*\

NumberSelectedPane.java

Class: 'number selected' pane.

\*====================================================================*/


// PACKAGE


package uk.blankaspect.unzip;

//----------------------------------------------------------------------


// IMPORTS


import java.util.List;

import javafx.geometry.Insets;
import javafx.geometry.Pos;

import javafx.scene.control.Label;

import javafx.scene.layout.HBox;

import javafx.scene.paint.Color;

import uk.blankaspect.common.css.CssSelector;

import uk.blankaspect.common.number.NumberUtils;

import uk.blankaspect.ui.jfx.scene.SceneUtils;

import uk.blankaspect.ui.jfx.style.ColourProperty;
import uk.blankaspect.ui.jfx.style.FxProperty;
import uk.blankaspect.ui.jfx.style.StyleConstants;
import uk.blankaspect.ui.jfx.style.StyleManager;

import uk.blankaspect.ui.jfx.text.TextUtils;

//----------------------------------------------------------------------


// CLASS: 'NUMBER SELECTED' PANE


public class NumberSelectedPane
	extends HBox
{

////////////////////////////////////////////////////////////////////////
//  Constants
////////////////////////////////////////////////////////////////////////

	private static final	String	SEPARATOR	= " / ";

	private static final	Insets	LABEL_PADDING	= new Insets(2.0, 6.0, 2.0, 6.0);

	private static final	Insets	PADDING	= new Insets(6.0, 8.0, 6.0, 8.0);

	/** Miscellaneous strings. */
	private static final	String	NUM_SELECTED_STR	= "Number of selected entries";

	/** CSS colour properties. */
	private static final	List<ColourProperty>	COLOUR_PROPERTIES	= List.of
	(
		ColourProperty.of
		(
			FxProperty.TEXT_FILL,
			ColourKey.TEXT,
			CssSelector.builder()
						.cls(StyleClass.NUMBER_SELECTED_PANE)
						.build()
		),
		ColourProperty.of
		(
			FxProperty.BACKGROUND_COLOUR,
			ColourKey.BACKGROUND,
			CssSelector.builder()
						.cls(StyleClass.NUMBER_SELECTED_PANE)
						.build()
		),
		ColourProperty.of
		(
			FxProperty.BORDER_COLOUR,
			ColourKey.BORDER,
			CssSelector.builder()
						.cls(StyleClass.NUMBER_SELECTED_PANE)
						.build()
		)
	);

	/** CSS style classes. */
	private interface StyleClass
	{
		String	NUMBER_SELECTED_PANE	= StyleConstants.CLASS_PREFIX + "unzip-number-selected-pane";
	}

	/** Keys of colours that are used in colour properties. */
	private interface ColourKey
	{
		String	BACKGROUND	= "numberSelectedPane.background";
		String	BORDER		= "numberSelectedPane.border";
		String	TEXT		= "numberSelectedPane.text";
	}

////////////////////////////////////////////////////////////////////////
//  Instance variables
////////////////////////////////////////////////////////////////////////

	private	int		numItems;
	private	Label	numSelectedLabel;

////////////////////////////////////////////////////////////////////////
//  Static initialiser
////////////////////////////////////////////////////////////////////////

	static
	{
		// Register the style properties of this class with the style manager
		StyleManager.INSTANCE.register(NumberSelectedPane.class, COLOUR_PROPERTIES);
	}

////////////////////////////////////////////////////////////////////////
//  Constructors
////////////////////////////////////////////////////////////////////////

	public NumberSelectedPane()
	{
		// Initialise instance variables
		numItems = -1;

		// Set properties
		setSpacing(6.0);
		setAlignment(Pos.CENTER_LEFT);
		setPadding(PADDING);

		// Create label: number selected
		numSelectedLabel = new Label();
		numSelectedLabel.setMaxWidth(Double.MAX_VALUE);
		numSelectedLabel.setAlignment(Pos.CENTER_RIGHT);
		numSelectedLabel.setPadding(LABEL_PADDING);
		numSelectedLabel.setTextFill(getColour(ColourKey.TEXT));
		numSelectedLabel.setBackground(SceneUtils.createColouredBackground(getColour(ColourKey.BACKGROUND)));
		numSelectedLabel.setBorder(SceneUtils.createSolidBorder(getColour(ColourKey.BORDER)));
		numSelectedLabel.getStyleClass().add(StyleClass.NUMBER_SELECTED_PANE);

		// Update 'number selected' label
		update(0, 0);

		// Add children
		getChildren().addAll(new Label(NUM_SELECTED_STR), numSelectedLabel);
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

	public void update(
		int	numItems,
		int	numSelected)
	{
		// If number of items has changed, update preferred width of 'number selected' label
		if (this.numItems != numItems)
		{
			// Update instance variable
			this.numItems = numItems;

			// Update preferred width of label
			String prototypeText = "0".repeat(NumberUtils.getNumDecDigitsInt(numItems)) + SEPARATOR + numItems;
			double textWidth = TextUtils.textWidth(numSelectedLabel.getFont(), prototypeText);
			Insets insets = numSelectedLabel.getInsets();
			numSelectedLabel.setPrefWidth(Math.ceil(textWidth + insets.getLeft() + insets.getRight()));
		}

		// Update text of 'number selected' label
		numSelectedLabel.setText(numSelected + SEPARATOR + numItems);
	}

	//------------------------------------------------------------------

}

//----------------------------------------------------------------------
