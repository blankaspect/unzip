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

import javafx.geometry.HPos;

import javafx.stage.Window;

import uk.blankaspect.common.basictree.MapNode;

import uk.blankaspect.common.filesystem.PathUtils;

import uk.blankaspect.common.logging.Logger;

import uk.blankaspect.ui.jfx.dialog.ButtonInfo;
import uk.blankaspect.ui.jfx.dialog.ErrorDialog;
import uk.blankaspect.ui.jfx.dialog.MessageDialog;

import uk.blankaspect.ui.jfx.image.MessageIcon32;

//----------------------------------------------------------------------


// CLASS: UTILITY METHODS


public class Utils
{

////////////////////////////////////////////////////////////////////////
//  Constants
////////////////////////////////////////////////////////////////////////

	/** The formatter that is applied to integer values to group digits in threes. */
	public static final	DecimalFormat	INTEGER_FORMATTER;

	/** Miscellaneous strings. */
	private static final	String	SAVE_BEYOND_SESSION_STR	= "Do you want to save the %s beyond the current session?";
	private static final	String	SAVE_STR				= "Save";
	private static final	String	DONT_SAVE_STR			= "Don't save";
	private static final	String	CANCEL_STR				= "Cancel";

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
			node.addString(key, normalisePathname(PathUtils.absString(location)));
	}

	//------------------------------------------------------------------

	public static Path decodeLocation(
		MapNode	node,
		String	key)
	{
		return Path.of(denormalisePathname(node.getString(key)));
	}

	//------------------------------------------------------------------

	public static Boolean askSaveBeyondSession(
		Window	owner,
		String	title,
		String	itemStr)
	{
		return switch (MessageDialog.show(owner, title, MessageIcon32.QUESTION.get(),
										  String.format(SAVE_BEYOND_SESSION_STR, itemStr),
										  ButtonInfo.of(HPos.RIGHT, SAVE_STR, DONT_SAVE_STR, CANCEL_STR)))
		{
			case 0  -> Boolean.TRUE;
			case 1  -> Boolean.FALSE;
			default -> null;
		};
	}

	//------------------------------------------------------------------

}

//----------------------------------------------------------------------
