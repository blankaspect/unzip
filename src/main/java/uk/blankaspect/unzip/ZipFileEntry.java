/*====================================================================*\

ZipFileEntry.java

Class: zip-file entry.

\*====================================================================*/


// PACKAGE


package uk.blankaspect.unzip;

//----------------------------------------------------------------------


// IMPORTS


import java.nio.file.Path;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;

import java.util.Comparator;
import java.util.List;
import java.util.Map;

import java.util.zip.ZipEntry;

import uk.blankaspect.common.comparator.CompoundStringComparator;

import uk.blankaspect.common.map.InsertionOrderStringMap;

import uk.blankaspect.common.string.StringUtils;

//----------------------------------------------------------------------


// CLASS: ZIP-FILE ENTRY


public class ZipFileEntry
{

////////////////////////////////////////////////////////////////////////
//  Constants
////////////////////////////////////////////////////////////////////////

	public static final	char	SEPARATOR_CHAR	= '/';

	public static final	Comparator<String>	DIRECTORY_PATHNAME_COMPARATOR	=
			CompoundStringComparator.respectCase(SEPARATOR_CHAR);

	public static final	Comparator<String>	DIRECTORY_FILENAME_PATHNAME_COMPARATOR;

	/** Miscellaneous strings. */
	private static final	String	FILENAME_STR		= "Filename";
	private static final	String	PATHNAME_STR		= "Pathname";
	private static final	String	TIMESTAMP_STR		= "Timestamp";
	private static final	String	SIZE_STR			= "Size";
	private static final	String	COMPRESSED_SIZE_STR	= "Compressed size";
	private static final	String	CRC_STR				= "CRC";

////////////////////////////////////////////////////////////////////////
//  Instance variables
////////////////////////////////////////////////////////////////////////

	private	int		index;
	private	String	pathname;
	private	long	timestamp;
	private	long	size;
	private	long	compressedSize;
	private	long	crc;

////////////////////////////////////////////////////////////////////////
//  Static initialiser
////////////////////////////////////////////////////////////////////////

	static
	{
		DIRECTORY_FILENAME_PATHNAME_COMPARATOR	= (pathname1, pathname2) ->
		{
			// Split first pathname into (1) elements of directory pathname, and (2) filename
			List<String> elements1 = StringUtils.split(pathname1, SEPARATOR_CHAR);
			String filename1 = elements1.remove(elements1.size() - 1);
			int numElements1 = elements1.size();

			// Split second pathname into (1) elements of directory pathname, and (2) filename
			List<String> elements2 = StringUtils.split(pathname2, SEPARATOR_CHAR);
			String filename2 = elements2.remove(elements2.size() - 1);
			int numElements2 = elements2.size();

			// Compare elements of directory pathnames
			int numElements = Math.min(numElements1, numElements2);
			for (int i = 0; i < numElements; i++)
			{
				int result = elements1.get(i).compareTo(elements2.get(i));
				if (result != 0)
					return result;
			}
			int result = Integer.compare(numElements1, numElements2);

			// If directory pathnames are equal, compare filenames
			if (result == 0)
				result = filename1.compareTo(filename2);
			return result;
		};
	}

////////////////////////////////////////////////////////////////////////
//  Constructors
////////////////////////////////////////////////////////////////////////

	public ZipFileEntry(
		int			index,
		ZipEntry	entry)
	{
		// Initialise instance variables
		this.index = index;
		pathname = entry.getName();
		timestamp = entry.getTime();
		size = entry.getSize();
		compressedSize = entry.getCompressedSize();
		crc = entry.getCrc();
	}

	//------------------------------------------------------------------

////////////////////////////////////////////////////////////////////////
//  Instance methods
////////////////////////////////////////////////////////////////////////

	public int getIndex()
	{
		return index;
	}

	//------------------------------------------------------------------

	public String getPathname()
	{
		return pathname;
	}

	//------------------------------------------------------------------

	public long getTimestamp()
	{
		return timestamp;
	}

	//------------------------------------------------------------------

	public long getSize()
	{
		return size;
	}

	//------------------------------------------------------------------

	public long getCompressedSize()
	{
		return compressedSize;
	}

	//------------------------------------------------------------------

	public long getCrc()
	{
		return crc;
	}

	//------------------------------------------------------------------

	public String getDirectoryPathname()
	{
		int index = pathname.lastIndexOf(SEPARATOR_CHAR);
		return (index < 0) ? "" : pathname.substring(0, index);
	}

	//------------------------------------------------------------------

	public String getFilename()
	{
		int index = pathname.lastIndexOf(SEPARATOR_CHAR);
		return (index < 0) ? pathname : pathname.substring(index + 1);
	}

	//------------------------------------------------------------------

	public LocalDateTime getDateTime()
	{
		return (timestamp < 0) ? null : LocalDateTime.ofInstant(Instant.ofEpochMilli(timestamp), ZoneId.systemDefault());
	}

	//------------------------------------------------------------------

	public Path getOutputFile(
		Path	directory,
		boolean	flatten)
	{
		if (directory == null)
			throw new IllegalArgumentException("Null directory");

		int index = pathname.lastIndexOf(SEPARATOR_CHAR);
		return ((index < 0) || !flatten) ? directory.resolve(Utils.denormalisePathname(pathname))
										 : directory.resolve(pathname.substring(index + 1));
	}

	//------------------------------------------------------------------

	public Map<String, String> getProperties()
	{
		return InsertionOrderStringMap
				.create()
				.integerFormatter(Utils.INTEGER_FORMATTER)
				.add(FILENAME_STR,        getFilename())
				.add(PATHNAME_STR,        pathname)
				.add(TIMESTAMP_STR,       Constants.TIMESTAMP_FORMATTER.format(getDateTime()))
				.add(SIZE_STR,            size)
				.add(COMPRESSED_SIZE_STR, compressedSize)
				.add(CRC_STR,             Utils.crcToString(crc));
	}

	//------------------------------------------------------------------

}

//----------------------------------------------------------------------
