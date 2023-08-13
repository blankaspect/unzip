/*====================================================================*\

RadioButtonWidthEqualiser.java

Class: radio-button width equaliser.

\*====================================================================*/


// PACKAGE


package uk.blankaspect.ui.jfx.widtheq;

//----------------------------------------------------------------------


// IMPORTS


import javafx.scene.Parent;

import javafx.scene.control.RadioButton;

//----------------------------------------------------------------------


// CLASS: RADIO-BUTTON WIDTH EQUALISER


/**
 * This class provides a means of managing a collection of {@linkplain RadioButton radio buttons} so that the widths of
 * the buttons in the collection are equal to the width of the widest button.  Widths are equalised by adjusting the gap
 * between the actuator and the label of each managed button.
 * <p>
 * The widths of radio buttons are typically updated from the {@link Parent#computePrefWidth(double)
 * computePrefWidth(double)} method of the parent of the buttons.  For example:
 * </p>
 * <pre>
 * RadioButtonWidthEqualiser rbwe = new RadioButtonWidthEqualiser();
 * VBox pane = new VBox(5.0, rbwe.createRadioButton("Label"), rbwe.createRadioButton("Longer label"))
 * {
 *     &#x40;Override
 *     protected double computePrefWidth(double height)
 *     {
 *         // Update radio-button widths
 *         rbwe.updateWidths();
 *
 *         // Call superclass method
 *         return super.computePrefWidth(height);
 *    }
 * };
 * </pre>
 */

public class RadioButtonWidthEqualiser
	extends AbstractButtonGapWidthEqualiser<RadioButton>
{

////////////////////////////////////////////////////////////////////////
//  Constructors
////////////////////////////////////////////////////////////////////////

	/**
	 * Creates a new instance of a radio-button width equaliser with a default factory for creating radio buttons.
	 */

	public RadioButtonWidthEqualiser()
	{
		// Call superclass constructor
		super(RadioButton::new);
	}

	//------------------------------------------------------------------

////////////////////////////////////////////////////////////////////////
//  Instance methods
////////////////////////////////////////////////////////////////////////

	/**
	 * Creates and returns a new instance of a radio button with the specified text.
	 *
	 * @param  text
	 *           the text of the radio button.
	 * @return a new instance of a radio button whose text is <i>text</i>.
	 */

	public RadioButton createRadioButton(
		String	text)
	{
		RadioButton button = getFactory().create(text);
		add(button);
		return button;
	}

	//------------------------------------------------------------------

}

//----------------------------------------------------------------------
