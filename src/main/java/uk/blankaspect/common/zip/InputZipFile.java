/*====================================================================*\

InputZipFile.java

Class: input zip file.

\*====================================================================*/


// PACKAGE


package uk.blankaspect.common.zip;

//----------------------------------------------------------------------


// IMPORTS


import java.io.IOException;

import java.nio.channels.FileChannel;
import java.nio.channels.OverlappingFileLockException;

import java.nio.charset.StandardCharsets;

import java.nio.file.Path;
import java.nio.file.StandardOpenOption;

import java.util.Collections;
import java.util.List;

import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import uk.blankaspect.common.exception2.FileException;

//----------------------------------------------------------------------


// CLASS: INPUT ZIP FILE


/**
 * This class implements a zip file for reading.
 */

public class InputZipFile
{

////////////////////////////////////////////////////////////////////////
//  Constants
////////////////////////////////////////////////////////////////////////

	/** Error messages. */
	private interface ErrorMsg
	{
		String	FAILED_TO_OPEN_FILE			= "Failed to open the file.";
		String	FAILED_TO_CLOSE_FILE		= "Failed to close the file.";
		String	FAILED_TO_LOCK_FILE			= "Failed to lock the file.";
		String	FILE_ACCESS_NOT_PERMITTED	= "Access to the file was not permitted.";
	}

////////////////////////////////////////////////////////////////////////
//  Instance variables
////////////////////////////////////////////////////////////////////////

	/** The file-system location of the zip file. */
	private	Path		location;

	/** The file channel. */
	private	FileChannel	channel;

	/** The zip file. */
	private	ZipFile		zipFile;

////////////////////////////////////////////////////////////////////////
//  Constructors
////////////////////////////////////////////////////////////////////////

	/**
	 * Creates a new instance of a zip file that is associated with the specified file-system location.
	 *
	 * @param location
	 *          the file-system location of the zip file.
	 */

	public InputZipFile(
		Path	location)
	{
		// Initialise instance variables
		this.location = location;
	}

	//------------------------------------------------------------------

////////////////////////////////////////////////////////////////////////
//  Instance methods
////////////////////////////////////////////////////////////////////////

	/**
	 * Returns the file-system location of this zip file.
	 *
	 * @return the file-system location of this zip file.
	 */

	public Path getLocation()
	{
		return location;
	}

	//------------------------------------------------------------------

	/**
	 * Returns the {@link ZipFile} object that is associated with this zip file.
	 *
	 * @return the {@link ZipFile} object that is associated with this zip file.
	 */

	public ZipFile getZipFile()
	{
		return zipFile;
	}

	//------------------------------------------------------------------

	/**
	 * Returns {@code true} if this zip file is open.
	 *
	 * @return {@code true} if this zip file is open.
	 */

	public boolean isOpen()
	{
		return (zipFile != null);
	}

	//------------------------------------------------------------------

	/**
	 * Opens this zip file for reading.
	 *
	 * @throws FileException
	 *           if an error occurred when opening the file.
	 * @throws IllegalStateException
	 *           if this file is already open.
	 */

	public void open()
		throws FileException
	{
		// Test whether file is open
		if (isOpen())
			throw new IllegalStateException("File is open");

		// Open file channel for reading
		try
		{
			channel = FileChannel.open(location, StandardOpenOption.READ);
		}
		catch (Exception e)
		{
			throw new FileException(ErrorMsg.FAILED_TO_OPEN_FILE, e, location);
		}

		// Lock channel
		try
		{
			if (channel.tryLock(0, Long.MAX_VALUE, true) == null)
				throw new FileException(ErrorMsg.FAILED_TO_LOCK_FILE, location);
		}
		catch (OverlappingFileLockException e)
		{
			// ignore
		}
		catch (IOException e)
		{
			throw new FileException(ErrorMsg.FAILED_TO_LOCK_FILE, e, location);
		}

		// Open zip file
		try
		{
			zipFile = new ZipFile(location.toFile(), StandardCharsets.UTF_8);
		}
		catch (SecurityException e)
		{
			throw new FileException(ErrorMsg.FILE_ACCESS_NOT_PERMITTED, e, location);
		}
		catch (Exception e)
		{
			throw new FileException(ErrorMsg.FAILED_TO_OPEN_FILE, e, location);
		}
	}

	//------------------------------------------------------------------

	/**
	 * Closes this zip file.
	 *
	 * @throws FileException
	 *           if an error occurred when closing the file.
	 * @throws IllegalStateException
	 *           if this file is not open.
	 */

	public void close()
		throws FileException
	{
		// Test whether file is open
		if (!isOpen())
			throw new IllegalStateException("File is not open");

		// Close channel to unlock it
		if (channel != null)
		{
			try
			{
				channel.close();
			}
			catch (IOException e)
			{
				throw new FileException(ErrorMsg.FAILED_TO_CLOSE_FILE, e, location);
			}
			finally
			{
				channel = null;
			}
		}

		// Close zip file
		try
		{
			zipFile.close();
		}
		catch (IOException e)
		{
			throw new FileException(ErrorMsg.FAILED_TO_CLOSE_FILE, e, location);
		}
		finally
		{
			zipFile = null;
		}
	}

	//------------------------------------------------------------------

	/**
	 * Closes this zip file, ignoring any exception that occurs when the file is closed.
	 */

	public void closeIgnoreException()
	{
		// Close channel to unlock it
		if (channel != null)
		{
			try
			{
				channel.close();
			}
			catch (IOException e)
			{
				// ignore
			}
			finally
			{
				channel = null;
			}
		}

		// Close zip file
		if (zipFile != null)
		{
			try
			{
				zipFile.close();
			}
			catch (IOException e)
			{
				// ignore
			}
			finally
			{
				zipFile = null;
			}
		}
	}

	//------------------------------------------------------------------

	/**
	 * Returns a list of the entries of this zip file.
	 *
	 * @return a list of the entries of this zip file.
	 */

	public List<? extends ZipEntry> getEntries()
	{
		return Collections.list(zipFile.entries());
	}

	//------------------------------------------------------------------

////////////////////////////////////////////////////////////////////////
//  Member classes : non-inner classes
////////////////////////////////////////////////////////////////////////


	// CLASS: ZIP-ENTRY EXCEPTION


	/**
	 * This class implements an exception that is associated with an entry of a zip file.
	 */

	public static class ZipEntryException
		extends FileException
	{

	////////////////////////////////////////////////////////////////////
	//  Constants
	////////////////////////////////////////////////////////////////////

		/** Miscellaneous strings. */
		private static final	String	ENTRY_STR	= "Entry: ";

	////////////////////////////////////////////////////////////////////
	//  Constructors
	////////////////////////////////////////////////////////////////////

		/**
		 * Creates a new instance of an exception with the specified detail message, associated location and associated
		 * zip-file entry.
		 *
		 * @param message
		 *          the detail message of the exception.
		 * @param location
		 *          the location with which the exception will be associated, which may be {@code null}.
		 * @param entry
		 *          the zip-file entry with which the exception will be associated, which may be {@code null}.
		 * @param replacements
		 *          the objects whose string representations will replace placeholders in {@code message}.
		 */

		public ZipEntryException(
			String		message,
			Path		location,
			ZipEntry	entry,
			Object...	replacements)
		{
			// Call alternative constructor
			this(message, null, location, entry, replacements);
		}

		//--------------------------------------------------------------

		/**
		 * Creates a new instance of an exception with the specified detail message, cause, associated location and
		 * associated zip-file entry.
		 *
		 * @param message
		 *          the detail message of the exception.
		 * @param cause
		 *          the underlying cause of the exception, which may be {@code null}.
		 * @param location
		 *          the location with which the exception will be associated, which may be {@code null}.
		 * @param entry
		 *          the zip-file entry with which the exception will be associated, which may be {@code null}.
		 * @param replacements
		 *          the objects whose string representations will replace placeholders in {@code message}.
		 */

		public ZipEntryException(
			String		message,
			Throwable	cause,
			Path		location,
			ZipEntry	entry,
			Object...	replacements)
		{
			// Call superclass constructor
			super(((entry == null) ? "" : ENTRY_STR + entry.getName() + "\n") + message, cause, location, replacements);
		}

		//--------------------------------------------------------------

	}

	//==================================================================

}

//----------------------------------------------------------------------
