/*====================================================================*\

Constants.java

Interface: constants.

\*====================================================================*/


// PACKAGE


package uk.blankaspect.unzip;

//----------------------------------------------------------------------


// IMPORTS


import java.time.format.DateTimeFormatter;

//----------------------------------------------------------------------


// INTERFACE: CONSTANTS


public interface Constants
{

////////////////////////////////////////////////////////////////////////
//  Constants
////////////////////////////////////////////////////////////////////////

	/** The character that separates adjacent components of the pathname of a zip-file entry. */
	char	SEPARATOR_CHAR	= '/';

	/** The formatter for the timestamp of a file. */
	DateTimeFormatter	TIMESTAMP_FORMATTER	= DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

	/** The filename extension of a zip file. */
	String	ZIP_FILENAME_EXTENSION	= ".zip";
}

//----------------------------------------------------------------------
