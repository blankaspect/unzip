/*====================================================================*\

ZipFileComparison.java

Class: comparison of two zip files.

\*====================================================================*/


// PACKAGE


package uk.blankaspect.unzip;

//----------------------------------------------------------------------


// IMPORTS


import java.io.IOException;

import java.nio.file.Files;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.LinkOption;
import java.nio.file.Path;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;

import java.util.zip.ZipEntry;

import uk.blankaspect.common.exception2.BaseException;
import uk.blankaspect.common.exception2.FileException;

import uk.blankaspect.common.namefilter.LocationFilter;

import uk.blankaspect.common.string.StringUtils;

import uk.blankaspect.common.zip.InputZipFile;

import uk.blankaspect.ui.jfx.font.FontUtils;

import uk.blankaspect.ui.jfx.text.TextUtils;

//----------------------------------------------------------------------


// CLASS: COMPARISON OF TWO ZIP FILES


public class ZipFileComparison
{

////////////////////////////////////////////////////////////////////////
//  Constants
////////////////////////////////////////////////////////////////////////

	/** Error messages. */
	private interface ErrorMsg
	{
		String	FILE_DOES_NOT_EXIST				= "The file does not exist.";
		String	NOT_A_FILE						= "The location does not denote a regular file.";
		String	FAILED_TO_CREATE_FILE_SYSTEM	= "Failed to create a file system for the file.";
	}

////////////////////////////////////////////////////////////////////////
//  Constructors
////////////////////////////////////////////////////////////////////////

	private ZipFileComparison()
	{
	}

	//------------------------------------------------------------------

////////////////////////////////////////////////////////////////////////
//  Class methods
////////////////////////////////////////////////////////////////////////

	public static List<Difference> compare(
		ZipFileModel			zipFile,
		Path					file,
		List<LocationFilter>	filters,
		Set<Field>				fields)
		throws BaseException
	{
		// Validate file
		if (!Files.exists(file, LinkOption.NOFOLLOW_LINKS))
			throw new FileException(ErrorMsg.FILE_DOES_NOT_EXIST, file);
		if (!Files.isRegularFile(file, LinkOption.NOFOLLOW_LINKS))
			throw new FileException(ErrorMsg.NOT_A_FILE, file);

		// Initialise list of differences between zip entries
		List<Difference> differences = new ArrayList<>();

		// Populate list of differences
		for (ZipEntryPair entryPair : getZipEntries(zipFile, file, filters))
		{
			entryPair.updateDifferences(fields);
			if (!entryPair.diffKinds.isEmpty())
				differences.add(new Difference(entryPair.diffKinds, entryPair.name));
		}

		// Return list of differences
		return differences;
	}

	//------------------------------------------------------------------

	private static void appendZipEntries(
		ZipFileModel			zipFile,
		List<LocationFilter>	filters,
		List<ZipEntryPair>		entryPairs)
		throws BaseException
	{
		FileSystem fileSystem = null;
		try
		{
			// Create file system for zip file
			Path file = zipFile.getLocation();
			try
			{
				fileSystem = FileSystems.newFileSystem(file);
			}
			catch (IOException e)
			{
				throw new FileException(ErrorMsg.FAILED_TO_CREATE_FILE_SYSTEM, e, file);
			}

			// Update matchers of location filters
			for (LocationFilter filter : filters)
				filter.updateMatcher(fileSystem);

			// Create list of filtered entries
			for (ZipFileEntry entry : zipFile.getEntries())
			{
				// Get name of entry
				String name = entry.getPathname();

				// If entry is included and not excluded, add it to list
				if (LocationFilter.accept(fileSystem.getPath(name), filters))
					entryPairs.add(new ZipEntryPair(entry));
			}
		}
		finally
		{
			// Close file system
			if (fileSystem != null)
			{
				try
				{
					fileSystem.close();
				}
				catch (IOException e)
				{
					// ignore
				}
			}
		}
	}

	//------------------------------------------------------------------

	private static void appendZipEntries(
		InputZipFile			zipFile,
		List<LocationFilter>	filters,
		List<ZipEntryPair>		entryPairs)
		throws BaseException
	{
		FileSystem fileSystem = null;
		try
		{
			// Open zip file
			zipFile.open();

			// Create file system for zip file
			try
			{
				fileSystem = FileSystems.newFileSystem(zipFile.location());
			}
			catch (IOException e)
			{
				throw new FileException(ErrorMsg.FAILED_TO_CREATE_FILE_SYSTEM, e, zipFile.location());
			}

			// Update matchers of location filters
			for (LocationFilter filter : filters)
				filter.updateMatcher(fileSystem);

			// Create list of filtered entries
			for (ZipEntry entry : zipFile.getEntries())
			{
				// Process entry if it is not a directory
				if (!entry.isDirectory())
				{
					// Get name of entry
					String name = entry.getName();

					// If entry is included and not excluded, add it to list
					if (LocationFilter.accept(fileSystem.getPath(name), filters))
					{
						boolean found = false;
						for (ZipEntryPair entryPair : entryPairs)
						{
							if (name.equals(entryPair.name))
							{
								entryPair.addSecondEntry(entry);
								found = true;
								break;
							}
						}
						if (!found)
							entryPairs.add(new ZipEntryPair(entry));
					}
				}
			}

			// Close zip file
			zipFile.close();
		}
		catch (BaseException e)
		{
			// Close zip file
			zipFile.closeIgnoreException();

			// Rethrow exception
			throw e;
		}
		finally
		{
			// Close file system
			if (fileSystem != null)
			{
				try
				{
					fileSystem.close();
				}
				catch (IOException e)
				{
					// ignore
				}
			}
		}
	}

	//------------------------------------------------------------------

	private static List<ZipEntryPair> getZipEntries(
		ZipFileModel			zipFile,
		Path					file,
		List<LocationFilter>	filters)
		throws BaseException
	{
		// Initialise list of zip-entry pairs
		List<ZipEntryPair> entryPairs = new ArrayList<>();

		// Append entries of first zip file to list
		appendZipEntries(zipFile, filters, entryPairs);

		// Append entries of second zip file to list
		appendZipEntries(new InputZipFile(file), filters, entryPairs);

		// Sort zip-entry pairs
		entryPairs.sort(Comparator.comparing(entry -> entry.name));

		// Return list of zip-entry pairs
		return entryPairs;
	}

	//------------------------------------------------------------------

////////////////////////////////////////////////////////////////////////
//  Enumerated types
////////////////////////////////////////////////////////////////////////


	// ENUMERATION: COMPARISON FIELDS


	public enum Field
	{

	////////////////////////////////////////////////////////////////////
	//  Constants
	////////////////////////////////////////////////////////////////////

		TIMESTAMP
		(
			"Timestamp",
			DiffKind.TIMESTAMP
		),

		SIZE
		(
			"Size",
			DiffKind.SIZE
		),

		CRC
		(
			"CRC",
			DiffKind.CRC
		);

	////////////////////////////////////////////////////////////////////
	//  Instance variables
	////////////////////////////////////////////////////////////////////

		private	String		text;
		private	DiffKind	diffKind;

	////////////////////////////////////////////////////////////////////
	//  Constructors
	////////////////////////////////////////////////////////////////////

		/**
		 * Creates a new instance of an enumeration constant for a comparison field.
		 *
		 * @param text
		 *          the text that will be associated with the field.
		 * @param diffKind
		 *          the {@linkplain DiffKind kind of difference} that will be associated with the field.
		 */

		private Field(
			String		text,
			DiffKind	diffKind)
		{
			// Initialise instance variables
			this.text = text;
			this.diffKind = diffKind;
		}

		//--------------------------------------------------------------

	////////////////////////////////////////////////////////////////////
	//  Class methods
	////////////////////////////////////////////////////////////////////

		public static Field forKey(
			String	key)
		{
			return Arrays.stream(values()).filter(value -> value.getKey().equals(key)).findFirst().orElse(null);
		}

		//--------------------------------------------------------------

	////////////////////////////////////////////////////////////////////
	//  Instance methods : overriding methods
	////////////////////////////////////////////////////////////////////

		/**
		 * {@inheritDoc}
		 */

		@Override
		public String toString()
		{
			return text;
		}

		//--------------------------------------------------------------

	////////////////////////////////////////////////////////////////////
	//  Instance methods
	////////////////////////////////////////////////////////////////////

		public String getKey()
		{
			return StringUtils.toCamelCase(name());
		}

		//--------------------------------------------------------------

	}

	//==================================================================


	// ENUMERATION: KINDS OF DIFFERENCE BETWEEN ZIP ENTRIES


	public enum DiffKind
	{

	////////////////////////////////////////////////////////////////////
	//  Constants
	////////////////////////////////////////////////////////////////////

		FILE1_ONLY
		(
			'1'
		),

		FILE2_ONLY
		(
			'2'
		),

		TIMESTAMP
		(
			'T'
		),

		SIZE
		(
			'S'
		),

		CRC
		(
			'C'
		);

	////////////////////////////////////////////////////////////////////
	//  Instance variables
	////////////////////////////////////////////////////////////////////

		private	char	key;

	////////////////////////////////////////////////////////////////////
	//  Constructors
	////////////////////////////////////////////////////////////////////

		/**
		 * Creates a new instance of an enumeration constant for a kind of difference.
		 *
		 * @param key
		 *          the key that will be associated with the kind of difference.
		 */

		private DiffKind(
			char	key)
		{
			// Initialise instance variables
			this.key = key;
		}

		//--------------------------------------------------------------

	////////////////////////////////////////////////////////////////////
	//  Class methods
	////////////////////////////////////////////////////////////////////

		public static String diffKindsToString(
			Collection<DiffKind>	diffKinds)
		{
			char[] chars = new char[values().length];
			for (int i = 0; i < chars.length; i++)
			{
				DiffKind diffKind = values()[i];
				chars[i] = diffKinds.contains(diffKind) ? diffKind.key : ' ';
			}
			return new String(chars);
		}

		//--------------------------------------------------------------

		public static int diffKindsToBitArray(
			Collection<DiffKind>	diffKinds)
		{
			int result = 0;
			int mask = 1 << values().length;
			for (DiffKind diffKind : values())
			{
				mask >>>= 1;
				if (diffKinds.contains(diffKind))
					result |= mask;
			}
			return result;
		}

		//--------------------------------------------------------------

		public static double getMaxKeyWidth()
		{
			double maxWidth = 0.0;
			for (DiffKind diffKind : values())
			{
				double width = TextUtils.textWidth(FontUtils.boldFont(), Character.toString(diffKind.key));
				if (maxWidth < width)
					maxWidth = width;
			}
			return Math.ceil(maxWidth);
		}

		//--------------------------------------------------------------

	}

	//==================================================================

////////////////////////////////////////////////////////////////////////
//  Member records
////////////////////////////////////////////////////////////////////////


	// RECORD: DIFFERENCE


	public record Difference(
		Set<DiffKind>	diffKinds,
		String			pathname)
	{ }

	//==================================================================

////////////////////////////////////////////////////////////////////////
//  Member classes : non-inner classes
////////////////////////////////////////////////////////////////////////


	// CLASS: ZIP-ENTRY PAIR


	private static class ZipEntryPair
	{

	////////////////////////////////////////////////////////////////////
	//  Instance variables
	////////////////////////////////////////////////////////////////////

		private	String			name;
		private	ZipFileEntry	entry1;
		private	ZipEntry		entry2;
		private	Set<DiffKind>	diffKinds;

	////////////////////////////////////////////////////////////////////
	//  Constructors
	////////////////////////////////////////////////////////////////////

		private ZipEntryPair(
			ZipFileEntry	entry)
		{
			// Validate arguments
			if (entry == null)
				throw new IllegalArgumentException("Null entry");

			// Initialise instance variables
			name = entry.getPathname();
			if (name == null)
				throw new IllegalArgumentException("Null entry name");

			entry1 = entry;
			diffKinds = EnumSet.noneOf(DiffKind.class);
		}

		//--------------------------------------------------------------

		private ZipEntryPair(
			ZipEntry	entry)
		{
			// Validate arguments
			if (entry == null)
				throw new IllegalArgumentException("Null entry");

			// Initialise instance variables
			name = entry.getName();
			if (name == null)
				throw new IllegalArgumentException("Null entry name");

			entry2 = entry;
			diffKinds = EnumSet.noneOf(DiffKind.class);
		}

		//--------------------------------------------------------------

	////////////////////////////////////////////////////////////////////
	//  Instance methods
	////////////////////////////////////////////////////////////////////

		private void addSecondEntry(
			ZipEntry	entry)
		{
			// Validate argument
			if (entry == null)
				throw new IllegalArgumentException("Null entry");

			// Test entry name
			if ((name != null) && !name.equals(entry.getName()))
				throw new IllegalArgumentException("Conflicting entry names");

			// Update instance variable
			entry2 = entry;
		}

		//--------------------------------------------------------------

		private void updateDifferences(
			Collection<Field>	fields)
		{
			diffKinds = EnumSet.noneOf(DiffKind.class);

			if ((entry1 == null) || (entry2 == null))
				diffKinds.add((entry1 == null) ? DiffKind.FILE2_ONLY : DiffKind.FILE1_ONLY);
			else
			{
				for (Field field : fields)
				{
					boolean different = switch (field)
					{
						case TIMESTAMP -> (entry1.getTimestamp() != entry2.getTime());
						case SIZE      -> (entry1.getSize() != entry2.getSize());
						case CRC       -> (entry1.getCrc() != entry2.getCrc());
					};
					if (different)
						diffKinds.add(field.diffKind);
				}
			}
		}

		//--------------------------------------------------------------

	}

	//==================================================================

}

//----------------------------------------------------------------------
