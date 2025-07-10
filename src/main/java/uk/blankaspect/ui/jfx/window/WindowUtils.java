/*====================================================================*\

WindowUtils.java

Class: window-related utility methods.

\*====================================================================*/


// PACKAGE


package uk.blankaspect.ui.jfx.window;

//----------------------------------------------------------------------


// IMPORTS


import javafx.geometry.Bounds;
import javafx.geometry.Point2D;

import javafx.scene.Node;

import javafx.stage.Stage;
import javafx.stage.Window;

import uk.blankaspect.ui.jfx.exec.ExecUtils;

import uk.blankaspect.ui.jfx.scene.SceneUtils;

//----------------------------------------------------------------------


// CLASS: WINDOW-RELATED UTILITY METHODS


/**
 * This class contains utility methods that relate to JavaFX {@linkplain Window windows}.
 */

public class WindowUtils
{

////////////////////////////////////////////////////////////////////////
//  Constants
////////////////////////////////////////////////////////////////////////

	/** The delay (in milliseconds) before the height of a window is fixed by {@link #preventHeightChange(Stage)}. */
	private static final	int	FIX_WINDOW_HEIGHT_DELAY	= 100;

	/** The delay (in milliseconds) before making the window visible by restoring its opacity. */
	private static final	int	WINDOW_VISIBLE_DELAY	= 50;

////////////////////////////////////////////////////////////////////////
//  Constructors
////////////////////////////////////////////////////////////////////////

	/**
	 * Prevents this class from being instantiated externally.
	 */

	private WindowUtils()
	{
	}

	//------------------------------------------------------------------

////////////////////////////////////////////////////////////////////////
//  Class methods
////////////////////////////////////////////////////////////////////////


	public static void preventHeightChange(
		Stage	window)
	{
		// Fix the minimum and maximum height of the window after a delay
		ExecUtils.afterDelay(FIX_WINDOW_HEIGHT_DELAY, () ->
		{
			double height = Math.ceil(window.getHeight());
			window.setMinHeight(height);
			window.setMaxHeight(height);
		});
	}

	//------------------------------------------------------------------

	public static void showAtRelativeLocation(
		Stage	window,
		Node	locator)
	{
		// Make window invisible
		window.setOpacity(0.0);

		// Set location of window when window is shown
		window.setOnShown(event ->
		{
			// Get screen bounds of locator
			Bounds bounds = locator.localToScreen(locator.getLayoutBounds());

			// Get location of window relative to locator
			Point2D location = SceneUtils.getRelativeLocation(window.getWidth(), window.getHeight(), bounds.getMinX(),
															  bounds.getMinY());

			// Set location of window
			window.setX(location.getX());
			window.setY(location.getY());

			// Make window visible after a delay
			ExecUtils.afterDelay(WINDOW_VISIBLE_DELAY, () -> window.setOpacity(1.0));
		});

		// Display window
		window.showAndWait();
	}

	//------------------------------------------------------------------

}

//----------------------------------------------------------------------
