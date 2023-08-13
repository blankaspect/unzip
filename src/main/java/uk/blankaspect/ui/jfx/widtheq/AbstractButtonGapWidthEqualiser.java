/*====================================================================*\

AbstractButtonGapWidthEqualiser.java

Class: abstract button gap-width equaliser.

\*====================================================================*/


// PACKAGE


package uk.blankaspect.ui.jfx.widtheq;

//----------------------------------------------------------------------


// IMPORTS


import java.util.ArrayList;
import java.util.List;

import javafx.scene.Parent;

import javafx.scene.control.ButtonBase;
import javafx.scene.control.RadioButton;

import uk.blankaspect.common.function.IFunction1;

import uk.blankaspect.ui.jfx.button.ButtonUtils;

//----------------------------------------------------------------------


// CLASS: ABSTRACT BUTTON GAP-WIDTH EQUALISER


/**
 * This is the asbtract base class of a manager of a collection of {@linkplain CheckBox check boxes} or {@linkplain
 * RadioButton radio buttons} (referred to generically as <i>buttons</i>) that adjusts the gap between the actuator and
 * the label of each managed button so that the widths of all the buttons in the collection are equal to the width of
 * the widest button.
 * <p>
 * The widths of buttons are typically updated from the {@link Parent#computePrefWidth(double) computePrefWidth(double)}
 * method of the parent of the buttons.
 * </p>
 *
 * @param <T>
 *          the type of the button: {@link CheckBox} or {@link RadioButton}.
 */

public abstract class AbstractButtonGapWidthEqualiser<T extends ButtonBase>
{

////////////////////////////////////////////////////////////////////////
//  Constants
////////////////////////////////////////////////////////////////////////

	/** The default minimum gap between the actuator and the label of a button. */
	public static final		double	DEFAULT_MIN_ACTUATOR_LABEL_GAP	= 6.0;

////////////////////////////////////////////////////////////////////////
//  Member interfaces
////////////////////////////////////////////////////////////////////////


	// INTERFACE: FACTORY


	/**
	 * This functional interface defines the method that must be implemented by a factory for creating the buttons that
	 * are managed by an {@link AbstractButtonGapWidthEqualiser}.
	 */

	@FunctionalInterface
	public interface IFactory<T>
	{

	////////////////////////////////////////////////////////////////////
	//  Methods
	////////////////////////////////////////////////////////////////////

		/**
		 * Creates and returns a button with the specified text.
		 *
		 * @param  text
		 *           the text of the button.
		 * @return a button whose text is <i>text</i>.
		 */

		T create(String text);

		//--------------------------------------------------------------

	}

	//==================================================================

////////////////////////////////////////////////////////////////////////
//  Instance variables
////////////////////////////////////////////////////////////////////////

	/** The collection of buttons that are managed by this width equaliser. */
	private	List<T>		buttons;

	/** The minimum gap between the actuator and the label of a button. */
	private	double		minActuatorLabelGap;

	/** The default factory for creating buttons. */
	private	IFactory<T>	defaultFactory;

	/** The factory for creating buttons. */
	private	IFactory<T>	factory;

////////////////////////////////////////////////////////////////////////
//  Constructors
////////////////////////////////////////////////////////////////////////

	/**
	 * Creates a new instance of a width equaliser with a default factory for creating buttons.
	 *
	 * @param factoryFunction
	 *          the function that returns a button for some given text.
	 */

	protected AbstractButtonGapWidthEqualiser(
		IFunction1<T, String>	factoryFunction)
	{
		// Initialise instance variables
		buttons = new ArrayList<>();
		minActuatorLabelGap = DEFAULT_MIN_ACTUATOR_LABEL_GAP;
		defaultFactory = text ->
		{
			// Create button
			T button = factoryFunction.invoke(text);

			// Set gap between actuator and label of button
			ButtonUtils.setActuatorLabelGap(button, minActuatorLabelGap);

			// Return button
			return button;
		};
		factory = defaultFactory;
	}

	//------------------------------------------------------------------

////////////////////////////////////////////////////////////////////////
//  Instance methods
////////////////////////////////////////////////////////////////////////

	/**
	 * Sets the minimum gap between the actuator and the label of a button to the specified value.
	 *
	 * @param gap
	 *          the value to which the minimum gap between the actuator and the label of a button will be set.
	 */

	public void setMinActuatorLabelGap(
		double	gap)
	{
		minActuatorLabelGap = gap;
		updateWidths();
	}

	//------------------------------------------------------------------

	/**
	 * Sets the button factory of this width equaliser to the specified value.
	 *
	 * @param factory
	 *          the button factory; {@code null} for the default factory.
	 */

	public void setFactory(
		IFactory<T>	factory)
	{
		this.factory = (factory == null) ? defaultFactory : factory;
	}

	//------------------------------------------------------------------

	/**
	 * Adds the specified button to the list of managed buttons.
	 *
	 * @param button
	 *          the button that will be added to the list of managed buttons.
	 */

	public void add(
		T	button)
	{
		buttons.add(button);
	}

	//------------------------------------------------------------------

	/**
	 * Removes the specified button from the list of managed buttons.
	 *
	 * @param button
	 *          the button that will be removed from the list of managed buttons.
	 */

	public void remove(
		T	button)
	{
		buttons.remove(button);
	}

	//------------------------------------------------------------------

	/**
	 * Removes all buttons from the list of managed buttons.
	 */

	public void clear()
	{
		buttons.clear();
	}

	//------------------------------------------------------------------

	/**
	 * Sets the widths of the managed buttons to the width of the widest button.
	 *
	 * @return {@code true} if the width of one or more buttons was set.
	 */

	public boolean updateWidths()
	{
		// Find maximum preferred width and maximum actuator-label gap of buttons
		double maxWidth = 0.0;
		double maxGap = 0.0;
		for (T button : buttons)
		{
			// Update maximum preferred width
			double width = button.prefWidth(-1.0);
			if (maxWidth < width)
				maxWidth = width;

			// Update maximum actuator-label gap
			double gap = width - button.getLabelPadding().getLeft();
			if (maxGap < gap)
				maxGap = gap;
		}

		// Set actuator-label gap of each button so that its width is equal to maximum width
		boolean widthSet = false;
		if (maxWidth > 0.0)
		{
			maxWidth = Math.ceil(maxWidth);
			maxGap += minActuatorLabelGap;
			for (T button : buttons)
			{
				if (button.getPrefWidth() < maxWidth)
				{
					// Set actuator-label gap
					double gap = button.prefWidth(-1.0) - button.getLabelPadding().getLeft();
					ButtonUtils.setActuatorLabelGap(button, Math.floor(maxGap - gap));

					// Set preferred width
					button.setPrefWidth(maxWidth);

					// Indicate that width was set
					widthSet = true;
				}
			}
		}

		// Return flag to indicate whether the preferred width of a button was set
		return widthSet;
	}

	//------------------------------------------------------------------

	/**
	 * Returns the factory for creating buttons.
	 *
	 * @return the factory for creating buttons.
	 */

	protected IFactory<T> getFactory()
	{
		return factory;
	}

	//------------------------------------------------------------------

	/**
	 * Creates and returns a button with the specified text.
	 *
	 * @param  text
	 *           the text of the button.
	 * @return a button whose text is <i>text</i>.
	 */

	protected T create(
		String	text)
	{
		T button = factory.create(text);
		buttons.add(button);
		return button;
	}

	//------------------------------------------------------------------

}

//----------------------------------------------------------------------
