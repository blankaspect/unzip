/*====================================================================*\

CheckLabel.java

Class: label with 'selected' state.

\*====================================================================*/


// PACKAGE


package uk.blankaspect.ui.jfx.label;

//----------------------------------------------------------------------


// IMPORTS


import java.util.List;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;

import javafx.css.PseudoClass;

import javafx.geometry.Bounds;

import javafx.scene.Group;
import javafx.scene.Node;

import javafx.scene.control.Label;

import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;

import javafx.scene.paint.Color;

import javafx.scene.shape.Polyline;
import javafx.scene.shape.Rectangle;
import javafx.scene.shape.StrokeType;

import uk.blankaspect.common.css.CssRuleSet;
import uk.blankaspect.common.css.CssSelector;

import uk.blankaspect.common.function.IProcedure0;

import uk.blankaspect.ui.jfx.scene.SceneUtils;

import uk.blankaspect.ui.jfx.shape.Shapes;

import uk.blankaspect.ui.jfx.style.ColourProperty;
import uk.blankaspect.ui.jfx.style.FxProperty;
import uk.blankaspect.ui.jfx.style.FxPseudoClass;
import uk.blankaspect.ui.jfx.style.RuleSetFactory;
import uk.blankaspect.ui.jfx.style.StyleConstants;
import uk.blankaspect.ui.jfx.style.StyleManager;
import uk.blankaspect.ui.jfx.style.StyleSelector;

import uk.blankaspect.ui.jfx.text.TextUtils;

//----------------------------------------------------------------------


// CLASS: LABEL WITH 'SELECTED' STATE


/**
 * This class implements a text label that has a <i>selected</i> state that is indicated by a graphic tick (check mark).
 */

public class CheckLabel
	extends Label
{

////////////////////////////////////////////////////////////////////////
//  Constants
////////////////////////////////////////////////////////////////////////

	/** The default gap between the text and graphic. */
	private static final	double	DEFAULT_GRAPHIC_TEXT_GAP	= 4.0;

	/** The opacity of the text of a dimmed label. */
	private static final	double	DIMMED_OPACITY	= 0.4;

	/** The pseudo-class that is associated with the <i>selected</i> state. */
	private static final	PseudoClass	SELECTED_PSEUDO_CLASS	= PseudoClass.getPseudoClass(FxPseudoClass.SELECTED);

	/** CSS colour properties. */
	private static final	List<ColourProperty>	COLOUR_PROPERTIES	= List.of
	(
		ColourProperty.of
		(
			FxProperty.STROKE,
			ColourKey.TICK_BOX,
			CssSelector.builder()
						.cls(StyleClass.CHECK_LABEL)
						.desc(StyleClass.TICK_BOX)
						.build()
		),
		ColourProperty.of
		(
			FxProperty.STROKE,
			Color.TRANSPARENT,
			CssSelector.builder()
						.cls(StyleClass.CHECK_LABEL)
						.desc(StyleClass.TICK)
						.build()
		),
		ColourProperty.of
		(
			FxProperty.STROKE,
			ColourKey.TICK,
			CssSelector.builder()
						.cls(StyleClass.CHECK_LABEL).pseudo(FxPseudoClass.SELECTED)
						.desc(StyleClass.TICK)
						.build()
		)
	);

	/** CSS rule sets. */
	private static final	List<CssRuleSet>	RULE_SETS	= List.of
	(
		RuleSetFactory.doubleSolidBorder
		(
			CssSelector.builder()
						.cls(StyleClass.CHECK_LABEL)
						.build(),
			Color.TRANSPARENT,
			ColourKey.BORDER
		),
		RuleSetFactory.outerFocusBorder
		(
			CssSelector.builder()
						.cls(StyleClass.CHECK_LABEL).pseudo(FxPseudoClass.FOCUSED)
						.build(),
			ColourKey.BORDER
		)
	);

	/** CSS style classes. */
	public interface StyleClass
	{
		String	CHECK_LABEL	= StyleConstants.CLASS_PREFIX + "check-label";
		String	TICK		= StyleConstants.CLASS_PREFIX + "tick";
		String	TICK_BOX	= StyleConstants.CLASS_PREFIX + "tick-box";
	}

	/** Keys of colours that are used in colour properties. */
	public interface ColourKey
	{
		String	BORDER		= "checkLabel.border";
		String	TICK		= "checkLabel.tick";
		String	TICK_BOX	= "checkLabel.tickBox";
	}

////////////////////////////////////////////////////////////////////////
//  Instance variables
////////////////////////////////////////////////////////////////////////

	/** Flag: if {@code true}, this label is in the <i>selected</i> state. */
	private	SimpleBooleanProperty	selected;

	/** The colour of the border. */
	private	Color					borderColour;

	/** The colour of the tick box. */
	private	Color					tickBoxColour;

	/** The colour of the tick. */
	private	Color					tickColour;

	/** Flag: if {@code true}, the text of this label will be dimmed if {@link #selected} is {@code false}. */
	private	boolean					dimIfUnselected;

	/** The tick that indicates the <i>selected</i> state. */
	private	Polyline				tick;

	/** The box that contains {@link #tick}. */
	private	Rectangle				tickBox;

////////////////////////////////////////////////////////////////////////
//  Static initialiser
////////////////////////////////////////////////////////////////////////

	static
	{
		// Register the style properties of this class with the style manager
		StyleManager.INSTANCE.register(CheckLabel.class, COLOUR_PROPERTIES, RULE_SETS);
	}

////////////////////////////////////////////////////////////////////////
//  Constructors
////////////////////////////////////////////////////////////////////////

	/**
	 * Creates a new instance of an empty label.
	 */

	public CheckLabel()
	{
		// Initialise instance variables
		selected = new SimpleBooleanProperty(false);
		borderColour = getColour(ColourKey.BORDER);
		tickBoxColour = getColour(ColourKey.TICK_BOX);
		tickColour = getColour(ColourKey.TICK);

		// Set properties
		setGraphicTextGap(DEFAULT_GRAPHIC_TEXT_GAP);
		setFocusTraversable(true);
		getStyleClass().add(StyleClass.CHECK_LABEL);

		// Update UI when focus changes
		focusedProperty().addListener(observable -> updateBorder());

		// Request focus when mouse button is pressed
		addEventHandler(MouseEvent.MOUSE_PRESSED, event -> requestFocus());

		// Togged 'selected' state when mouse button is clicked
		addEventHandler(MouseEvent.MOUSE_CLICKED, event ->
		{
			if (event.getButton() == MouseButton.PRIMARY)
			{
				// Togged 'selected' state
				setSelected(!isSelected());

				// Consume event
				event.consume();
			}
		});

		// Togged 'selected' state when space key is pressed
		addEventHandler(KeyEvent.KEY_PRESSED, event ->
		{
			if (event.getCode() == KeyCode.SPACE)
			{
				// Togged 'selected' state
				setSelected(!isSelected());

				// Consume event
				event.consume();
			}
		});

		// Update tick and text when 'selected' state changes
		selectedProperty().addListener((observable, oldSelected, selected) ->
		{
			pseudoClassStateChanged(SELECTED_PSEUDO_CLASS, selected);
			updateTick();
			layoutChildren();
		});

		// Create procedure to update tick and box
		IProcedure0 updateTickAndBox = () ->
		{
			// Create tick box
			double boxSize = TextUtils.textHeightCeil(getFont());
			tickBox = new Rectangle(boxSize, boxSize, Color.TRANSPARENT);
			tickBox.setStrokeType(StrokeType.INSIDE);
			tickBox.getStyleClass().add(StyleClass.TICK_BOX);

			// Create tick
			tick = Shapes.tick01(getFont());
			tick.getStyleClass().add(StyleClass.TICK);

			// Centre tick in box
			Bounds bounds = tick.getLayoutBounds();
			tick.relocate(0.5 * (boxSize - bounds.getWidth()), 0.5 * (boxSize - bounds.getHeight()));

			// Set graphic of label
			setGraphic(new Group(tickBox, tick));

			// Update UI components
			updateTickBox();
			updateTick();
		};

		// Update tick and box when font changes
		fontProperty().addListener(observable -> updateTickAndBox.invoke());

		// Update tick and box
		updateTickAndBox.invoke();

		// Update UI components
		updateBorder();
	}

	//------------------------------------------------------------------

	/**
	 * Creates a new instance of a label with the specified text.
	 *
	 * @param text
	 *          the text of the label.
	 */

	public CheckLabel(
		String	text)
	{
		// Call alternative constructor
		this();

		// Set text
		setText(text);
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
//  Instance methods : overriding methods
////////////////////////////////////////////////////////////////////////

	/**
	 * {@inheritDoc}
	 */

	@Override
	protected void layoutChildren()
	{
		// Update opacity of text node
		Node textNode = lookup(StyleSelector.TEXT);
		if (textNode != null)
			textNode.setOpacity((!dimIfUnselected || isSelected()) ? 1.0 : DIMMED_OPACITY);

		// Call superclass method
		super.layoutChildren();
	}

	//------------------------------------------------------------------

////////////////////////////////////////////////////////////////////////
//  Instance methods
////////////////////////////////////////////////////////////////////////

	/**
	 * Returns the value of the <i>selected</i> flag.
	 *
	 * @return the value of the <i>selected</i> flag.
	 */

	public boolean isSelected()
	{
		return selected.get();
	}

	//------------------------------------------------------------------

	/**
	 * Sets the <i>selected</i> flag to the specified value.
	 *
	 * @param selected
	 *          the value to which the <i>selected</i> flag will be set.
	 */

	public void setSelected(
		boolean	selected)
	{
		this.selected.set(selected);
	}

	//------------------------------------------------------------------

	/**
	 * Returns the <i>selected</i> flag as a property.
	 *
	 * @return the <i>selected</i> flag as a property.
	 */

	public BooleanProperty selectedProperty()
	{
		return selected;
	}

	//------------------------------------------------------------------

	/**
	 * Returns the border colour.
	 *
	 * @return the border colour.
	 */

	public Color getBorderColour()
	{
		return borderColour;
	}

	//------------------------------------------------------------------

	/**
	 * Sets the border colour to the specified value.
	 *
	 * @param colour
	 *          the value to which the border colour will be set.  If it is {@code null}, the default colour will be
	 *          used.
	 */

	public void setBorderColour(
		Color	colour)
	{
		// Update instance variable
		borderColour = (colour == null) ? getColour(ColourKey.BORDER) : colour;

		// Update UI component
		updateBorder();
	}

	//------------------------------------------------------------------

	/**
	 * Returns the colour of the tick box.
	 *
	 * @return the colour of the tick box.
	 */

	public Color getTickBoxColour()
	{
		return tickBoxColour;
	}

	//------------------------------------------------------------------

	/**
	 * Sets the colour of the tick box to the specified value.
	 *
	 * @param colour
	 *          the value to which the colour of the tick box will be set.  If it is {@code null}, the default colour
	 *          will be used.
	 */

	public void setTickBoxColour(
		Color	colour)
	{
		// Update instance variable
		tickBoxColour = (colour == null) ? getColour(ColourKey.TICK_BOX) : colour;

		// Update UI component
		updateTickBox();
	}

	//------------------------------------------------------------------

	/**
	 * Returns the colour of the tick.
	 *
	 * @return the colour of the tick.
	 */

	public Color getTickColour()
	{
		return tickColour;
	}

	//------------------------------------------------------------------

	/**
	 * Sets the colour of the tick to the specified value.
	 *
	 * @param colour
	 *          the value to which the colour of the tick will be set.  If it is {@code null}, the default colour will
	 *          be used.
	 */

	public void setTickColour(
		Color	colour)
	{
		// Update instance variable
		tickColour = (colour == null) ? getColour(ColourKey.TICK) : colour;

		// Update UI component
		updateTick();
	}

	//------------------------------------------------------------------

	/**
	 * Returns the value of the <i>dim if not selected</i> flag.
	 *
	 * @return the value of the <i>dim if not selected</i> flag.
	 */

	public boolean isDimIfUnselected()
	{
		return dimIfUnselected;
	}

	//------------------------------------------------------------------

	/**
	 * Sets the <i>dim if not selected</i> flag to the specified value.
	 *
	 * @param dimIfUnselected
	 *          the value to which the <i>dim if not selected</i> flag will be set.
	 */

	public void setDimIfUnselected(
		boolean	dimIfUnselected)
	{
		// Update instance variable
		this.dimIfUnselected = dimIfUnselected;

		// Update UI component
		updateBorder();
	}

	//------------------------------------------------------------------

	/**
	 * Updates the border of this label.
	 */

	private void updateBorder()
	{
		if (StyleManager.INSTANCE.notUsingStyleSheet())
		{
			setBorder(isFocused() ? SceneUtils.createOuterFocusBorder(borderColour)
								  : SceneUtils.createSolidBorder(Color.TRANSPARENT, borderColour));
		}
	}

	//------------------------------------------------------------------

	/**
	 * Updates the tick box.
	 */

	private void updateTickBox()
	{
		if (StyleManager.INSTANCE.notUsingStyleSheet())
			tickBox.setStroke(tickBoxColour);
	}

	//------------------------------------------------------------------

	/**
	 * Updates the tick.
	 */

	private void updateTick()
	{
		if (StyleManager.INSTANCE.notUsingStyleSheet())
			tick.setStroke(isSelected() ? tickColour : Color.TRANSPARENT);
	}

	//------------------------------------------------------------------

}

//----------------------------------------------------------------------
