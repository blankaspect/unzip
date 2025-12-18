/*====================================================================*\

ZipFileModel.java

Class: model of a zip file.

\*====================================================================*/


// PACKAGE


package uk.blankaspect.unzip;

//----------------------------------------------------------------------


// IMPORTS


import java.io.InputStream;
import java.io.IOException;

import java.nio.channels.FileChannel;
import java.nio.channels.OverlappingFileLockException;

import java.nio.charset.StandardCharsets;

import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.nio.file.StandardOpenOption;

import java.nio.file.attribute.FileAttribute;
import java.nio.file.attribute.FileTime;
import java.nio.file.attribute.PosixFileAttributes;
import java.nio.file.attribute.PosixFilePermissions;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.BitSet;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

import java.util.zip.CRC32;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import uk.blankaspect.common.bytechannel.ChannelUtils;

import uk.blankaspect.common.exception2.FileException;

import uk.blankaspect.common.filesystem.FilenameUtils;
import uk.blankaspect.common.filesystem.PathUtils;

import uk.blankaspect.common.logging.Logger;

import uk.blankaspect.common.map.InsertionOrderStringMap;

import uk.blankaspect.common.task.ITaskStatus;

//----------------------------------------------------------------------


// CLASS: MODEL OF A ZIP FILE


public class ZipFileModel
{

////////////////////////////////////////////////////////////////////////
//  Constants
////////////////////////////////////////////////////////////////////////

	private static final	int		MIN_EOCD_RECORD_LENGTH	= 22;

	private static final	byte[]	ENTRY_ID	= { 'P', 'K', (byte)0x03, (byte)0x04 };
	private static final	byte[]	EOCD_ID		= { 'P', 'K', (byte)0x05, (byte)0x06 };

	private static final	int		EXTRACTION_BUFFER_LENGTH	= 1 << 16;  // 65536

	/** Miscellaneous strings. */
	private static final	String	FILENAME_STR				= "Filename";
	private static final	String	NUM_DIRECTORIES_STR			= "Number of directories";
	private static final	String	NUM_FILES_STR				= "Number of files";
	private static final	String	TOTAL_SIZE_STR				= "Total size";
	private static final	String	TOTAL_COMPRESSED_SIZE_STR	= "Total compressed size";
	private static final	String	READING_STR					= "Reading";
	private static final	String	SORTING_STR					= "Sorting";
	private static final	String	EXTRACTING_FILE_STR			= "Extracting file to";
	private static final	String	EXTRACTING_FILES_STR		= "Extracting files";
	private static final	String	WRITING_STR					= "Writing";

	/** Error messages. */
	private interface ErrorMsg
	{
		String	FAILED_TO_OPEN_FILE =
				"Failed to open the file.";

		String	FAILED_TO_CLOSE_FILE =
				"Failed to close the file.";

		String	FAILED_TO_LOCK_FILE =
				"Failed to lock the file.";

		String	FILE_ACCESS_NOT_PERMITTED =
				"Access to the file was not permitted.";

		String	FAILED_TO_READ_FILE_ATTRIBUTES =
				"Failed to read the attributes of the file.";

		String	ERROR_READING_FILE =
				"An error occurred when reading the file.";

		String	ERROR_WRITING_FILE =
				"An error occurred when writing the file.";

		String	PREMATURE_END_OF_FILE =
				"The end of the file was reached prematurely when reading the file.";

		String	ZIP_FILE_CHANGED =
				"The zip file has changed since it was first read.";

		String	FAILED_TO_CREATE_DIRECTORY =
				"Failed to create the directory.";

		String	FAILED_TO_CREATE_TEMPORARY_FILE =
				"Failed to create a temporary file.";

		String	FAILED_TO_DELETE_FILE =
				"Failed to delete the existing file.";

		String	FAILED_TO_RENAME_FILE =
				"Temporary file: %s\nFailed to rename the temporary file to the specified filename.";

		String	NOT_A_ZIP_FILE =
				"The file is not recognised as a zip file.";

		String	FAILED_TO_READ_ZIP_ENTRY =
				"Failed to read an entry of the input file.";

		String	FAILED_TO_SET_FILE_TIMESTAMP =
				"Failed to set the timestamp of the output file.";

		String	INCORRECT_CRC =
				"The CRC value of the file is incorrect.";
	}

////////////////////////////////////////////////////////////////////////
//  Instance variables
////////////////////////////////////////////////////////////////////////

	private	Path				location;
	private	FileTime			timestamp;
	private	int					numDirectories;
	private	long				totalSize;
	private	long				totalCompressedSize;
	private	List<ZipFileEntry>	entries;
	private	byte[]				extractionBuffer;

////////////////////////////////////////////////////////////////////////
//  Constructors
////////////////////////////////////////////////////////////////////////

	public ZipFileModel()
	{
		// Initialise instance variables
		entries = new ArrayList<>();
	}

	//------------------------------------------------------------------

////////////////////////////////////////////////////////////////////////
//  Instance methods
////////////////////////////////////////////////////////////////////////

	public Path getLocation()
	{
		return location;
	}

	//------------------------------------------------------------------

	public FileTime getTimestamp()
	{
		return timestamp;
	}

	//------------------------------------------------------------------

	public void setTimestamp(
		FileTime	timestamp)
	{
		this.timestamp = timestamp;
	}

	//------------------------------------------------------------------

	public List<ZipFileEntry> getEntries()
	{
		return Collections.unmodifiableList(entries);
	}

	//------------------------------------------------------------------

	public Map<String, String> getProperties()
	{
		return InsertionOrderStringMap
				.create()
				.integerFormatter(Utils.INTEGER_FORMATTER)
				.add(FILENAME_STR,              location.getFileName())
				.add(NUM_DIRECTORIES_STR,       numDirectories)
				.add(NUM_FILES_STR,             entries.size())
				.add(TOTAL_SIZE_STR,            totalSize)
				.add(TOTAL_COMPRESSED_SIZE_STR, totalCompressedSize);
	}

	//------------------------------------------------------------------

	public int[] rowIndicesToEntryIndices(
		int[]	indices)
	{
		int[] entryIndices = new int[indices.length];
		for (int i = 0; i < entryIndices.length; i++)
			entryIndices[i] = entries.get(indices[i]).getIndex();
		Arrays.sort(entryIndices);
		return entryIndices;
	}

	//------------------------------------------------------------------

	public void readEntries(
		Path		location,
		ITaskStatus	taskStatus)
		throws FileException
	{
		FileChannel channel = null;
		ZipFile zipFile = null;
		try
		{
			// Set message and indeterminate progress
			taskStatus.setSpacedMessage(READING_STR, PathUtils.abs(location));
			taskStatus.setProgress(-1.0);

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

			// Test for ZIP file
			try
			{
				// Test size of file
				if (channel.size() < MIN_EOCD_RECORD_LENGTH)
					throw new FileException(ErrorMsg.NOT_A_ZIP_FILE, location);

				// Read ID
				byte[] buffer = new byte[ENTRY_ID.length];
				ChannelUtils.read(channel, buffer);

				// Test for local file header (non-empty archive) or 'end of central directory' record (empty archive)
				if (!Arrays.equals(buffer, ENTRY_ID) && !Arrays.equals(buffer, EOCD_ID))
					throw new FileException(ErrorMsg.NOT_A_ZIP_FILE, location);

				// Return to start of file
				channel.position(0);
			}
			catch (IOException e)
			{
				throw new FileException(ErrorMsg.ERROR_READING_FILE, e, location);
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

			// Update instance variables
			this.location = location;
			try
			{
				timestamp = Files.getLastModifiedTime(location, LinkOption.NOFOLLOW_LINKS);
			}
			catch (Exception e)
			{
				timestamp = null;

				Logger.INSTANCE.error(e);
			}

			// Reset instance variables
			numDirectories = 0;
			totalSize = 0;
			totalCompressedSize = 0;

			// Initialise progress
			taskStatus.setProgress(0.0);

			// Collect entries
			entries.clear();
			List<? extends ZipEntry> zipEntries = Collections.list(zipFile.entries());
			int numEntries = zipEntries.size();
			int index = 0;
			for (ZipEntry entry : zipEntries)
			{
				// Test whether task has been cancelled
				if (taskStatus.isCancelled())
					break;

				// If entry is directory, update directory count ...
				if (entry.isDirectory())
					++numDirectories;

				// ... otherwise, add entry to list
				else
				{
					// Add entry to list
					entries.add(new ZipFileEntry(index, entry));

					// Update total sizes
					totalSize += entry.getSize();
					totalCompressedSize += entry.getCompressedSize();
				}

				// Increment index
				++index;

				// Update progress
				taskStatus.setProgress((double)index / (double)numEntries);
			}

			// Update message; set indeterminate progress
			taskStatus.setSpacedMessage(SORTING_STR, PathUtils.abs(location));
			taskStatus.setProgress(-1.0);

			// Sort entries
			if (!taskStatus.isCancelled())
			{
				entries.sort(Comparator.comparing(ZipFileEntry::getPathname,
												  ZipFileEntry.DIRECTORY_FILENAME_PATHNAME_COMPARATOR));
			}

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
		catch (FileException e)
		{
			// Close channel to unlock it
			if (channel != null)
			{
				try
				{
					channel.close();
				}
				catch (IOException e0)
				{
					// ignore
				}
			}

			// Close zip file
			if (zipFile != null)
			{
				try
				{
					zipFile.close();
				}
				catch (IOException e0)
				{
					// ignore
				}
			}

			// Rethrow exception
			throw e;
		}
	}

	//------------------------------------------------------------------

	public int extractEntries(
		List<ZipFileEntry>	entries,
		BitSet				selection,
		Path				outDirectory,
		boolean				flatten,
		ITaskStatus			taskStatus)
		throws FileException
	{
		ZipFile zipFile = null;
		try
		{
			// Set message and indeterminate progress
			taskStatus.setMessage(EXTRACTING_FILES_STR);
			taskStatus.setProgress(-1.0);

			// Get total size of entries
			long totalEntrySize = 0;
			for (int i = selection.nextSetBit(0); i >= 0; i = selection.nextSetBit(i + 1))
				totalEntrySize += entries.get(i).getSize();

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

			// Initialise progress
			taskStatus.setProgress(0.0);

			// Extract entries
			int extractedCount = 0;
			List<? extends ZipEntry> zipEntries = Collections.list(zipFile.entries());
			long totalExtractedSize = 0;
			for (int i = selection.nextSetBit(0); i >= 0; i = selection.nextSetBit(i + 1))
			{
				// Test whether task has been cancelled
				if (taskStatus.isCancelled())
					break;

				// Test entry from list against entry from file
				ZipFileEntry entry = entries.get(i);
				int index = entry.getIndex();
				if ((index >= zipEntries.size()) || (zipEntries.get(index).getCrc() != entry.getCrc()))
					throw new FileException(ErrorMsg.ZIP_FILE_CHANGED, location);

				// Get location of output file
				Path outFile = entry.getOutputFile(outDirectory, flatten);

				// Update message
				taskStatus.setSpacedMessage(WRITING_STR, PathUtils.abs(outFile));

				// Write entry to file
				extractEntry(zipFile, zipEntries.get(index), outFile);

				// Increment count of files extracted
				++extractedCount;

				// Update total size of extracted files
				totalExtractedSize += entry.getSize();

				// Update progress
				taskStatus.setProgress((double)totalExtractedSize / (double)totalEntrySize);
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

			// Return number of files extracted
			return extractedCount;
		}
		catch (FileException e)
		{
			// Close zip file
			if (zipFile != null)
			{
				try
				{
					zipFile.close();
				}
				catch (IOException e0)
				{
					// ignore
				}
			}

			// Rethrow exception
			throw e;
		}
	}

	//------------------------------------------------------------------

	public void extractEntry(
		ZipFileEntry	entry,
		Path			outDirectory,
		ITaskStatus		taskStatus)
		throws FileException
	{
		ZipFile zipFile = null;
		try
		{
			// Get location of output file
			Path outFile = entry.getOutputFile(outDirectory, true);

			// Set message and indeterminate progress
			taskStatus.setSpacedMessage(EXTRACTING_FILE_STR, PathUtils.abs(outFile));
			taskStatus.setProgress(-1.0);

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

			// Test entry against entry from file
			List<? extends ZipEntry> zipEntries = Collections.list(zipFile.entries());
			int index = entry.getIndex();
			if ((index >= zipEntries.size()) || (zipEntries.get(index).getCrc() != entry.getCrc()))
				throw new FileException(ErrorMsg.ZIP_FILE_CHANGED, location);

			// Update message
			taskStatus.setSpacedMessage(WRITING_STR, PathUtils.abs(outFile));

			// Write entry to file
			extractEntry(zipFile, zipEntries.get(index), outFile);

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
		catch (FileException e)
		{
			// Close zip file
			if (zipFile != null)
			{
				try
				{
					zipFile.close();
				}
				catch (IOException e0)
				{
					// ignore
				}
			}

			// Rethrow exception
			throw e;
		}
	}

	//------------------------------------------------------------------

	private void extractEntry(
		ZipFile		zipFile,
		ZipEntry	entry,
		Path		outFile)
		throws FileException
	{
		// Initialise variables
		InputStream inStream = null;
		FileChannel outChannel = null;
		Path tempFile = null;
		boolean oldFileDeleted = false;

		// Read zip entry and write it to file
		try
		{
			// Open input stream on zip entry
			try
			{
				inStream = zipFile.getInputStream(entry);
			}
			catch (Exception e)
			{
				throw new FileException(ErrorMsg.FAILED_TO_READ_ZIP_ENTRY, location, e);
			}

			// Read file permissions of an existing file
			FileAttribute<?>[] attrs = {};
			if (Files.exists(outFile, LinkOption.NOFOLLOW_LINKS))
			{
				try
				{
					PosixFileAttributes posixAttrs =
							Files.readAttributes(outFile, PosixFileAttributes.class, LinkOption.NOFOLLOW_LINKS);
					attrs = new FileAttribute<?>[] { PosixFilePermissions.asFileAttribute(posixAttrs.permissions()) };
				}
				catch (UnsupportedOperationException e)
				{
					// ignore
				}
				catch (Exception e)
				{
					throw new FileException(ErrorMsg.FAILED_TO_READ_FILE_ATTRIBUTES, e, outFile);
				}
			}

			// Create parent directory
			Path directory = PathUtils.absParent(outFile);
			try
			{
				Files.createDirectories(directory);
			}
			catch (Exception e)
			{
				throw new FileException(ErrorMsg.FAILED_TO_CREATE_DIRECTORY, e, directory);
			}

			// Create temporary file
			try
			{
				tempFile = FilenameUtils.tempLocation(outFile);
				Files.createFile(tempFile, attrs);
			}
			catch (Exception e)
			{
				throw new FileException(ErrorMsg.FAILED_TO_CREATE_TEMPORARY_FILE, e, tempFile);
			}

			// Open channel for writing
			try
			{
				outChannel = FileChannel.open(tempFile, StandardOpenOption.WRITE);
			}
			catch (Exception e)
			{
				throw new FileException(ErrorMsg.FAILED_TO_OPEN_FILE, e, tempFile);
			}

			// Lock output channel
			try
			{
				if (outChannel.tryLock() == null)
					throw new FileException(ErrorMsg.FAILED_TO_LOCK_FILE, tempFile);
			}
			catch (IOException e)
			{
				throw new FileException(ErrorMsg.FAILED_TO_LOCK_FILE, e, tempFile);
			}

			// Allocate buffer
			if (extractionBuffer == null)
				extractionBuffer = new byte[EXTRACTION_BUFFER_LENGTH];

			// Read from zip entry and write output file
			CRC32 crc = new CRC32();
			long inLength = entry.getSize();
			int blockLength = 0;
			for (long offset = 0; offset < inLength; offset += blockLength)
			{
				// Read input stream
				try
				{
					blockLength = (int)Math.min(inLength - offset, extractionBuffer.length);
					blockLength = inStream.read(extractionBuffer, 0, blockLength);
					if (blockLength < 0)
						throw new FileException(ErrorMsg.PREMATURE_END_OF_FILE, location);
				}
				catch (IOException e)
				{
					throw new FileException(ErrorMsg.ERROR_READING_FILE, e, location);
				}

				// Write output channel
				if (blockLength > 0)
				{
					// Write contents of buffer to output channel
					try
					{
						ChannelUtils.write(outChannel, extractionBuffer, 0, blockLength);
					}
					catch (Exception e)
					{
						throw new FileException(ErrorMsg.ERROR_WRITING_FILE, e, tempFile);
					}

					// Update CRC
					crc.update(extractionBuffer, 0, blockLength);
				}
			}

			// Close input stream
			try
			{
				inStream.close();
			}
			catch (Exception e)
			{
				throw new FileException(ErrorMsg.FAILED_TO_CLOSE_FILE, e, location);
			}
			finally
			{
				inStream = null;
			}

			// Close output channel
			try
			{
				outChannel.close();
			}
			catch (Exception e)
			{
				throw new FileException(ErrorMsg.FAILED_TO_CLOSE_FILE, e, tempFile);
			}
			finally
			{
				outChannel = null;
			}

			// Delete any existing file
			try
			{
				Files.deleteIfExists(outFile);
				oldFileDeleted = true;
			}
			catch (Exception e)
			{
				throw new FileException(ErrorMsg.FAILED_TO_DELETE_FILE, e, outFile);
			}

			// Rename temporary file
			try
			{
				Files.move(tempFile, outFile, StandardCopyOption.ATOMIC_MOVE);
			}
			catch (Exception e)
			{
				throw new FileException(ErrorMsg.FAILED_TO_RENAME_FILE, e, outFile, PathUtils.abs(tempFile));
			}

			// Set timestamp of output file
			try
			{
				long timestamp = entry.getTime();
				if (timestamp >= 0)
					Files.setLastModifiedTime(outFile, FileTime.fromMillis(timestamp));
			}
			catch (Exception e)
			{
				throw new FileException(ErrorMsg.FAILED_TO_SET_FILE_TIMESTAMP, e, outFile);
			}

			// Check CRC
			if (entry.getCrc() != crc.getValue())
				throw new FileException(ErrorMsg.INCORRECT_CRC, outFile);
		}
		catch (FileException e)
		{
			// Close input stream
			if (inStream != null)
			{
				try
				{
					inStream.close();
				}
				catch (Exception e0)
				{
					// ignore
				}
			}

			// Close output channel
			if (outChannel != null)
			{
				try
				{
					outChannel.close();
				}
				catch (Exception e0)
				{
					// ignore
				}
			}

			// Delete temporary file
			if (!oldFileDeleted && (tempFile != null))
			{
				try
				{
					Files.deleteIfExists(tempFile);
				}
				catch (Exception e0)
				{
					// ignore
				}
			}

			// Rethrow exception
			throw e;
		}
	}

	//------------------------------------------------------------------

}

//----------------------------------------------------------------------
