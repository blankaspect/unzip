/*====================================================================*\

FileEditor.java

Class: file editor.

\*====================================================================*/


// PACKAGE


package uk.blankaspect.unzip;

//----------------------------------------------------------------------


// IMPORTS


import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Path;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

import java.util.regex.PatternSyntaxException;

import uk.blankaspect.common.basictree.MapNode;
import uk.blankaspect.common.basictree.StringNode;

import uk.blankaspect.common.collection.CollectionUtils;

import uk.blankaspect.common.exception2.BaseException;

//----------------------------------------------------------------------


// CLASS: FILE EDITOR


public class FileEditor
{

////////////////////////////////////////////////////////////////////////
//  Constants
////////////////////////////////////////////////////////////////////////

	public static final		int		MAX_NUM_EDITORS			= 256;
	public static final		int		MAX_NUM_FILE_FILTERS	= 256;

	public static final		char	COMMAND_ESCAPE_CHAR					= '%';
	public static final		char	COMMAND_PATHNAME_PLACEHOLDER_CHAR	= 'f';
	public static final		char	COMMAND_URI_PLACEHOLDER_CHAR		= 'u';

	private static final	String	GLOB_PATTERN_SYNTAX	="glob";

	private interface PropertyKey
	{
		String	COMMAND				= "command";
		String	FILENAME_PATTERNS	= "filenamePatterns";
		String	NAME				= "name";
	}

	private interface ErrorMsg
	{
		String	INVALID_PATTERN =
				"Pattern: %s\nThe pattern is invalid.";

		String	UNRECOGNISED_SYNTAX =
				"Syntax: %s\nThe pattern syntax is not recognised.";
	}

////////////////////////////////////////////////////////////////////////
//  Instance variables
////////////////////////////////////////////////////////////////////////

	private	String			name;
	private	String			command;
	private	List<String>	filenamePatterns;

////////////////////////////////////////////////////////////////////////
//  Constructors
////////////////////////////////////////////////////////////////////////

	public FileEditor()
	{
		// Initialise instance variables
		filenamePatterns = Collections.emptyList();
	}

	//------------------------------------------------------------------

	public FileEditor(
		String				name,
		String				command,
		Collection<String>	filenamePatterns)
	{
		// Validate arguments
		if (name == null)
			throw new IllegalArgumentException("Null name");
		if (command == null)
			throw new IllegalArgumentException("Null command");

		// Initialise instance variables
		this.name = name;
		this.command = command;
		this.filenamePatterns = CollectionUtils.isNullOrEmpty(filenamePatterns) ? Collections.emptyList()
																				: new ArrayList<>(filenamePatterns);
	}

	//------------------------------------------------------------------

////////////////////////////////////////////////////////////////////////
//  Class methods
////////////////////////////////////////////////////////////////////////

	public static boolean isValidPattern(
		String	filenamePattern)
	{
		// Validate filename pattern by creating matcher for it
		try
		{
			// Create matcher
			FileSystems.getDefault().getPathMatcher(GLOB_PATTERN_SYNTAX + ":" + filenamePattern);

			// Pattern is valid
			return true;
		}
		catch (UnsupportedOperationException | PatternSyntaxException e)
		{
			// ignore
		}

		// Pattern is not valid
		return false;
	}

	//------------------------------------------------------------------

	public static FileEditor findFileEditor(
		String				filename,
		List<FileEditor>	editors)
		throws BaseException
	{
		// Test target against filename patterns of editors
		for (FileEditor editor : editors)
		{
			if (editor.filenameMatches(filename))
				return editor;
		}

		// Find first editor that has no filename patterns
		for (FileEditor editor : editors)
		{
			if (!editor.hasFilenamePatterns())
				return editor;
		}

		// No editor was found
		return null;
	}

	//------------------------------------------------------------------

////////////////////////////////////////////////////////////////////////
//  Instance methods : overriding methods
////////////////////////////////////////////////////////////////////////

	@Override
	public boolean equals(
		Object	obj)
	{
		if (this == obj)
			return true;

		return (obj instanceof FileEditor other) && Objects.equals(name, other.name)
				&& Objects.equals(command, other.command) && filenamePatterns.equals(other.filenamePatterns);
	}

	//------------------------------------------------------------------

	@Override
	public int hashCode()
	{
		int code = Objects.hashCode(name);
		code = 31 * code + Objects.hashCode(command);
		code = 31 * code + filenamePatterns.hashCode();
		return code;
	}

	//------------------------------------------------------------------

	@Override
	public String toString()
	{
		return name;
	}

	//------------------------------------------------------------------

////////////////////////////////////////////////////////////////////////
//  Instance methods
////////////////////////////////////////////////////////////////////////

	public String getName()
	{
		return name;
	}

	//------------------------------------------------------------------

	public String getCommand()
	{
		return command;
	}

	//------------------------------------------------------------------

	public int getNumFilenamePatterns()
	{
		return filenamePatterns.size();
	}

	//------------------------------------------------------------------

	public boolean hasFilenamePatterns()
	{
		return !filenamePatterns.isEmpty();
	}

	//------------------------------------------------------------------

	public String getFilenamePatterns(
		int	index)
	{
		return filenamePatterns.get(index);
	}

	//------------------------------------------------------------------

	public List<String> getFilenamePatterns()
	{
		return Collections.unmodifiableList(filenamePatterns);
	}

	//------------------------------------------------------------------

	public boolean filenameMatches(
		String	filename)
		throws BaseException
	{
		// Create file system for filename matcher
		FileSystem fileSystem = FileSystems.getDefault();

		// Create filename target
		Path target = Path.of(filename);

		// Test target against filename patterns
		for (String pattern : filenamePatterns)
		{
			try
			{
				if (fileSystem.getPathMatcher(GLOB_PATTERN_SYNTAX + ":" + pattern).matches(target))
					return true;
			}
			catch (UnsupportedOperationException e)
			{
				throw new BaseException(ErrorMsg.UNRECOGNISED_SYNTAX, e, GLOB_PATTERN_SYNTAX);
			}
			catch (PatternSyntaxException e)
			{
				throw new BaseException(ErrorMsg.INVALID_PATTERN, e, pattern);
			}
		}

		// No match
		return false;
	}

	//------------------------------------------------------------------

	public MapNode encode()
	{
		// Create root node
		MapNode rootNode = new MapNode();

		// Encode name
		rootNode.addString(PropertyKey.NAME, name);

		// Encode command
		rootNode.addString(PropertyKey.COMMAND, command);

		// Encode filename patterns
		if (!filenamePatterns.isEmpty())
			rootNode.addList(PropertyKey.FILENAME_PATTERNS).addStrings(filenamePatterns);

		// Return root node
		return rootNode;
	}

	//------------------------------------------------------------------

	public void decode(
		MapNode	rootNode)
	{
		// Decode name
		String key = PropertyKey.NAME;
		if (rootNode.hasString(key))
			name = rootNode.getString(key);

		// Decode command
		key = PropertyKey.COMMAND;
		if (rootNode.hasString(key))
			command = rootNode.getString(key);

		// Decode filename patterns
		filenamePatterns = Collections.emptyList();
		key = PropertyKey.FILENAME_PATTERNS;
		if (rootNode.hasList(key))
		{
			filenamePatterns = new ArrayList<>();
			for (StringNode node : rootNode.getListNode(key).stringNodes())
				filenamePatterns.add(node.getValue());
		}
	}

	//------------------------------------------------------------------

}

//----------------------------------------------------------------------
