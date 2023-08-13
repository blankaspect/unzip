/*====================================================================*\

Utils.java

Class: utility methods.

\*====================================================================*/


// PACKAGE


package uk.blankaspect.unzip;

//----------------------------------------------------------------------


// IMPORTS


import java.io.File;

import java.nio.file.Path;

import java.text.DecimalFormat;

import javafx.stage.Window;

import uk.blankaspect.common.basictree.MapNode;

import uk.blankaspect.common.logging.Logger;

import uk.blankaspect.ui.jfx.dialog.ErrorDialog;

//----------------------------------------------------------------------


// CLASS: UTILITY METHODS


public class Utils
{

////////////////////////////////////////////////////////////////////////
//  Constants
////////////////////////////////////////////////////////////////////////

	/** The formatter that is applied to integer values to group digits in threes. */
	public static final	DecimalFormat	INTEGER_FORMATTER;

////////////////////////////////////////////////////////////////////////
//  Static initialiser
////////////////////////////////////////////////////////////////////////

	static
	{
		INTEGER_FORMATTER = new DecimalFormat();
		INTEGER_FORMATTER.setGroupingSize(3);
	}

////////////////////////////////////////////////////////////////////////
//  Constructors
////////////////////////////////////////////////////////////////////////

	private Utils()
	{
	}

	//------------------------------------------------------------------

////////////////////////////////////////////////////////////////////////
//  Class methods
////////////////////////////////////////////////////////////////////////

	public static String formatDecimal(
		long	value)
	{
		return INTEGER_FORMATTER.format(value);
	}

	//------------------------------------------------------------------

	public static String crcToString(
		long	crc)
	{
		return Long.toHexString(crc).toUpperCase();
	}

	//------------------------------------------------------------------

	public static void showErrorMessage(
		Window	window,
		String	title,
		String	message)
	{
		// Log error
		Logger.INSTANCE.error(message);

		// Display error dialog
		ErrorDialog.show(window, title, message);
	}

	//------------------------------------------------------------------

	public static void showErrorMessage(
		Window		window,
		String		title,
		Throwable	exception)
	{
		// Log error
		Logger.INSTANCE.error(title, exception);

		// Display error dialog
		ErrorDialog.show(window, title, exception);
	}

	//------------------------------------------------------------------

	public static void showErrorMessage(
		Window		window,
		String		title,
		String		message,
		Throwable	exception)
	{
		// Log error
		Logger.INSTANCE.error(message, exception);

		// Display error dialog
		ErrorDialog.show(window, title, message, exception);
	}

	//------------------------------------------------------------------

	public static String normalisePathname(
		String	pathname)
	{
		return pathname.replace(File.separatorChar, Constants.SEPARATOR_CHAR);
	}

	//------------------------------------------------------------------

	public static String denormalisePathname(
		String	pathname)
	{
		return pathname.replace(Constants.SEPARATOR_CHAR, File.separatorChar);
	}

	//------------------------------------------------------------------

	public static void encodeLocation(
		MapNode	node,
		String	key,
		Path	location)
	{
		if (location != null)
			node.addString(key, normalisePathname(location.toAbsolutePath().toString()));
	}

	//------------------------------------------------------------------

	public static Path decodeLocation(
		MapNode	node,
		String	key)
	{
		return Path.of(denormalisePathname(node.getString(key)));
	}

	//------------------------------------------------------------------

}

//----------------------------------------------------------------------
