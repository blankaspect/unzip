/*====================================================================*\

Preferences.java

Class: user preferences.

\*====================================================================*/


// PACKAGE


package uk.blankaspect.unzip;

//----------------------------------------------------------------------


// IMPORTS


import java.io.File;

import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import java.util.function.Predicate;

import uk.blankaspect.common.basictree.ListNode;
import uk.blankaspect.common.basictree.MapNode;
import uk.blankaspect.common.basictree.StringNode;

import uk.blankaspect.common.filesystem.PathnameUtils;

import uk.blankaspect.common.misc.SystemUtils;

import uk.blankaspect.common.string.StringUtils;

import uk.blankaspect.ui.jfx.locationchooser.FileMatcher;

//----------------------------------------------------------------------


// CLASS: USER PREFERENCES


public class Preferences
{

////////////////////////////////////////////////////////////////////////
//  Constants
////////////////////////////////////////////////////////////////////////

	private static final	int		DEFAULT_CELL_VERTICAL_PADDING	= 2;

	private static final	boolean		DEFAULT_COMBO_BOX_COMMIT_ON_FOCUS_LOST	= true;

	private static final	List<String>	DEFAULT_FILENAME_SUFFIXES	= List.of
	(
		".jar",
		Constants.ZIP_FILENAME_EXTENSION
	);

	private static final	String	ZIP_FILES_STR	= "Zip files";

	private interface PropertyKey
	{
		String	COMBO_BOX							= "comboBox";
		String	COMMIT_ON_FOCUS_LOST				= "commitOnFocusLost";
		String	DEFAULT_EXTRACTION_DIRECTORY		= "defaultExtractionDirectory";
		String	FILE_EDITOR_EXTRACTION_DIRECTORY	= "fileEditorExtractionDirectory";
		String	FILE_EDITORS						= "fileEditors";
		String	USER_INTERFACE						= "userInterface";
		String	ZIP_FILENAME_SUFFIXES				= "zipFilenameSuffixes";
	}

////////////////////////////////////////////////////////////////////////
//  Instance variables
////////////////////////////////////////////////////////////////////////

	private	int					cellVerticalPadding;
	private	int					columnHeaderPopUpDelay;
	private	boolean				comboBoxCommitOnFocusLost;
	private	List<String>		zipFilenameSuffixes;
	private	FileMatcher			zipFileFilter;
	private	Predicate<Path>		zipFileDragAndDropFilter;
	private	String				defaultExtractionDirectory;
	private	String				fileEditorExtractionDirectory;
	private	List<FileEditor>	fileEditors;

////////////////////////////////////////////////////////////////////////
//  Constructors
////////////////////////////////////////////////////////////////////////

	public Preferences()
	{
		// Call alternative constructor
		this(DEFAULT_CELL_VERTICAL_PADDING, ZipFileTableView.DEFAULT_HEADER_CELL_POP_UP_DELAY,
			 DEFAULT_COMBO_BOX_COMMIT_ON_FOCUS_LOST, DEFAULT_FILENAME_SUFFIXES, SystemUtils.userHomeDirectoryPathname(),
			 null, Collections.emptyList());
	}

	//------------------------------------------------------------------

	public Preferences(
		int									cellVerticalPadding,
		int									columnHeaderPopUpDelay,
		boolean								comboBoxCommitOnFocusLost,
		Collection<String>					zipFilenameSuffixes,
		String								defaultExtractionDirectory,
		String								fileEditorExtractionDirectory,
		Collection<? extends FileEditor>	fileEditors)
	{
		// Initialise instance variables
		this.cellVerticalPadding = cellVerticalPadding;
		this.columnHeaderPopUpDelay = columnHeaderPopUpDelay;
		this.comboBoxCommitOnFocusLost = comboBoxCommitOnFocusLost;
		this.zipFilenameSuffixes = new ArrayList<>(zipFilenameSuffixes);
		this.defaultExtractionDirectory = defaultExtractionDirectory;
		this.fileEditorExtractionDirectory = fileEditorExtractionDirectory;
		this.fileEditors = new ArrayList<>(fileEditors);

		// Update instance variables that depend on filename suffixes
		updateFilenameSuffixes();
	}

	//------------------------------------------------------------------

////////////////////////////////////////////////////////////////////////
//  Instance methods
////////////////////////////////////////////////////////////////////////

	public int getCellVerticalPadding()
	{
		return cellVerticalPadding;
	}

	//------------------------------------------------------------------

	public void setCellVerticalPadding(
		int	padding)
	{
		cellVerticalPadding = padding;
	}

	//------------------------------------------------------------------

	public int getColumnHeaderPopUpDelay()
	{
		return columnHeaderPopUpDelay;
	}

	//------------------------------------------------------------------

	public void setColumnHeaderPopUpDelay(
		int	delay)
	{
		columnHeaderPopUpDelay = delay;
	}

	//------------------------------------------------------------------

	public boolean isComboBoxCommitOnFocusLost()
	{
		return comboBoxCommitOnFocusLost;
	}

	//------------------------------------------------------------------

	public void setComboBoxCommitOnFocusLost(
		boolean	commitOnFocusLost)
	{
		comboBoxCommitOnFocusLost = commitOnFocusLost;
	}

	//------------------------------------------------------------------

	public List<String> getZipFilenameSuffixes()
	{
		return Collections.unmodifiableList(zipFilenameSuffixes);
	}

	//------------------------------------------------------------------

	public FileMatcher getZipFileFilter()
	{
		return zipFileFilter;
	}

	//------------------------------------------------------------------

	public Predicate<Path> getZipFileDragAndDropFilter()
	{
		return zipFileDragAndDropFilter;
	}

	//------------------------------------------------------------------

	public String getDefaultExtractionDirectory()
	{
		return defaultExtractionDirectory;
	}

	//------------------------------------------------------------------

	public String getFileEditorExtractionDirectory()
	{
		return fileEditorExtractionDirectory;
	}

	//------------------------------------------------------------------

	public List<FileEditor> getFileEditors()
	{
		return Collections.unmodifiableList(fileEditors);
	}

	//------------------------------------------------------------------

	public void setFileEditors(
		Collection<? extends FileEditor>	editors)
	{
		fileEditors.clear();
		fileEditors.addAll(editors);
	}

	//------------------------------------------------------------------

	/**
	 * Encodes these preferences to the tree of {@linkplain AbstractNode nodes} whose root is the specified node.
	 *
	 * @param rootNode
	 *          the root of the tree of {@linkplain AbstractNode nodes} to which these preferences will be encoded.
	 */

	public void encode(
		MapNode	rootNode)
	{
		// Encode combo box, commit on focus lost
		rootNode.addMap(PropertyKey.USER_INTERFACE)
				.addMap(PropertyKey.COMBO_BOX)
				.addBoolean(PropertyKey.COMMIT_ON_FOCUS_LOST, comboBoxCommitOnFocusLost);

		// Encode zip filename suffixes
		if (!zipFilenameSuffixes.isEmpty())
		{
			ListNode filenameSuffixesNode = rootNode.addList(PropertyKey.ZIP_FILENAME_SUFFIXES);
			for (String suffix : zipFilenameSuffixes)
				filenameSuffixesNode.addString(suffix);
		}

		// Encode default extraction directory
		if (!StringUtils.isNullOrBlank(defaultExtractionDirectory))
		{
			rootNode.addString(PropertyKey.DEFAULT_EXTRACTION_DIRECTORY,
							   defaultExtractionDirectory.replace(File.separatorChar, '/'));
		}

		// Encode file-editor extraction directory
		if (!StringUtils.isNullOrBlank(fileEditorExtractionDirectory))
		{
			rootNode.addString(PropertyKey.FILE_EDITOR_EXTRACTION_DIRECTORY,
							   fileEditorExtractionDirectory.replace(File.separatorChar, '/'));
		}

		// Encode file editors
		if (!fileEditors.isEmpty())
		{
			ListNode fileEditorsNode = rootNode.addList(PropertyKey.FILE_EDITORS);
			for (FileEditor editor : fileEditors)
				fileEditorsNode.add(editor.encode());
		}
	}

	//------------------------------------------------------------------

	/**
	 * Decodes these preferences from the tree of {@linkplain AbstractNode nodes} whose root is the specified node.
	 *
	 * @param rootNode
	 *          the root of the tree of {@linkplain AbstractNode nodes} from which these preferences will be decoded.
	 */

	public void decode(
		MapNode	rootNode)
	{
		// Decode combo box, commit on focus lost
		String key = PropertyKey.USER_INTERFACE;
		if (rootNode.hasMap(key))
		{
			MapNode uiNode = rootNode.getMapNode(key);
			key = PropertyKey.COMBO_BOX;
			if (uiNode.hasMap(key))
			{
				comboBoxCommitOnFocusLost = uiNode.getMapNode(key)
						.getBoolean(PropertyKey.COMMIT_ON_FOCUS_LOST, DEFAULT_COMBO_BOX_COMMIT_ON_FOCUS_LOST);
			}
		}

		// Decode zip filename suffixes
		zipFilenameSuffixes.clear();
		key = PropertyKey.ZIP_FILENAME_SUFFIXES;
		if (rootNode.hasList(key))
		{
			for (StringNode node : rootNode.getListNode(key).stringNodes())
				zipFilenameSuffixes.add(node.getValue());
		}
		if (zipFilenameSuffixes.isEmpty())
			zipFilenameSuffixes.add(Constants.ZIP_FILENAME_EXTENSION);

		// Update instance variables that depend on filename suffixes
		updateFilenameSuffixes();

		// Decode default extraction directory
		key = PropertyKey.DEFAULT_EXTRACTION_DIRECTORY;
		if (rootNode.hasString(key))
			defaultExtractionDirectory = rootNode.getString(key).replace('/', File.separatorChar);

		// Decode file-editor extraction directory
		key = PropertyKey.FILE_EDITOR_EXTRACTION_DIRECTORY;
		if (rootNode.hasString(key))
			fileEditorExtractionDirectory = rootNode.getString(key).replace('/', File.separatorChar);

		// Decode file editors
		fileEditors.clear();
		key = PropertyKey.FILE_EDITORS;
		if (rootNode.hasList(key))
		{
			for (MapNode node : rootNode.getListNode(key).mapNodes())
			{
				FileEditor editor = new FileEditor();
				editor.decode(node);
				if (!(StringUtils.isNullOrBlank(editor.getName()) || StringUtils.isNullOrBlank(editor.getCommand())))
					fileEditors.add(editor);
			}
		}
	}

	//------------------------------------------------------------------

	private void updateFilenameSuffixes()
	{
		zipFileFilter = new FileMatcher(ZIP_FILES_STR, zipFilenameSuffixes);
		zipFileDragAndDropFilter = location ->
				Files.isRegularFile(location, LinkOption.NOFOLLOW_LINKS)
						&& PathnameUtils.suffixMatches(location, zipFilenameSuffixes);
	}

	//------------------------------------------------------------------

}

//----------------------------------------------------------------------
