/*====================================================================*\

UnzipApp.java

Class: zip-file extractor application.

\*====================================================================*/


// PACKAGE


package uk.blankaspect.unzip;

//----------------------------------------------------------------------


// IMPORTS


import java.io.IOException;

import java.lang.invoke.MethodHandles;

import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;

import java.nio.file.attribute.FileTime;

import java.util.ArrayList;
import java.util.BitSet;
import java.util.EnumSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import java.util.concurrent.Executors;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import javafx.application.Application;
import javafx.application.Platform;

import javafx.beans.InvalidationListener;

import javafx.beans.property.SimpleObjectProperty;

import javafx.concurrent.Task;

import javafx.geometry.Dimension2D;
import javafx.geometry.HPos;
import javafx.geometry.Insets;
import javafx.geometry.Point2D;
import javafx.geometry.Pos;

import javafx.scene.Scene;

import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuBar;
import javafx.scene.control.MenuItem;
import javafx.scene.control.SeparatorMenuItem;
import javafx.scene.control.TableColumn;

import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCodeCombination;
import javafx.scene.input.KeyCombination;
import javafx.scene.input.TransferMode;

import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;

import javafx.stage.Stage;

import uk.blankaspect.common.basictree.MapNode;

import uk.blankaspect.common.build.BuildUtils;

import uk.blankaspect.common.cls.ClassUtils;

import uk.blankaspect.common.config.AppAuxDirectory;
import uk.blankaspect.common.config.AppConfig;

import uk.blankaspect.common.exception2.BaseException;
import uk.blankaspect.common.exception2.FileException;
import uk.blankaspect.common.exception2.LocationException;

import uk.blankaspect.common.filesystem.FileSystemUtils;
import uk.blankaspect.common.filesystem.PathnameUtils;

import uk.blankaspect.common.function.IProcedure0;

import uk.blankaspect.common.logging.Logger;
import uk.blankaspect.common.logging.LoggerUtils;
import uk.blankaspect.common.logging.LogLevel;

import uk.blankaspect.common.resource.ResourceProperties;
import uk.blankaspect.common.resource.ResourceUtils;

import uk.blankaspect.common.string.StringUtils;

import uk.blankaspect.common.thread.DaemonFactory;

import uk.blankaspect.ui.jfx.clipboard.ClipboardUtils;

import uk.blankaspect.ui.jfx.container.PropertiesPane;

import uk.blankaspect.ui.jfx.dialog.ConfirmationDialog;
import uk.blankaspect.ui.jfx.dialog.ErrorDialog;
import uk.blankaspect.ui.jfx.dialog.NotificationDialog;
import uk.blankaspect.ui.jfx.dialog.SimpleModalDialog;
import uk.blankaspect.ui.jfx.dialog.SimpleProgressDialog;

import uk.blankaspect.ui.jfx.image.MessageIcon32;

import uk.blankaspect.ui.jfx.locationchooser.FileMatcher;
import uk.blankaspect.ui.jfx.locationchooser.LocationChooser;

import uk.blankaspect.ui.jfx.scene.SceneUtils;

import uk.blankaspect.ui.jfx.selectionmodel.SelectionModelUtils;

import uk.blankaspect.ui.jfx.spinner.CollectionSpinner;

import uk.blankaspect.ui.jfx.style.StyleManager;

import uk.blankaspect.ui.jfx.window.WindowState;

//----------------------------------------------------------------------


// CLASS: ZIP-FILE EXTRACTOR APPLICATION


public class UnzipApp
	extends Application
{

////////////////////////////////////////////////////////////////////////
//  Constants
////////////////////////////////////////////////////////////////////////

	/** The short name of the application. */
	private static final	String	SHORT_NAME	= "Unzip";

	/** The long name of the application. */
	private static final	String	LONG_NAME	= "Zip-file extractor";

	/** The key with which the application is associated. */
	private static final	String	NAME_KEY	= "unzip";

	private static final	String	BUILD_PROPERTIES_FILENAME	= "build.properties";

	/** The name of the log file. */
	private static final	String	LOG_FILENAME	= NAME_KEY + ".log";

	/** The number of lines of a previous log file that are retained. */
	private static final	int		LOG_NUM_RETAINED_LINES	= 10000;

	/** The logging threshold. */
	private static final	LogLevel	LOG_THRESHOLD	= LogLevel.INFO;

	/** The formatting parameters of the logger. */
	private static final	Logger.Params	LOG_PARAMS	= new Logger.Params
	(
		null,
		null,
		EnumSet.of(Logger.Field.TIMESTAMP, Logger.Field.LEVEL, Logger.Field.SOURCE_LOCATION),
		true
	);

	/** The filename of the CSS style sheet. */
	private static final	String	STYLE_SHEET_FILENAME	= NAME_KEY + "-%02d.css";

	/** The default initial directory of a file chooser. */
	private static final	Path	DEFAULT_DIRECTORY	= Path.of(System.getProperty("user.home", "."));

	/** The interval (in milliseconds) between successive checks for a modified file. */
	private static final	int		CHECK_MODIFIED_FILE_INTERVAL	= 500;

	/** The suffix of the name of a thread on which a check for a modified file is performed. */
	private static final	String	CHECK_MODIFIED_FILE_THREAD_NAME_SUFFIX	= "checkModifiedFile";

	private static final	String	EDITOR_THREAD_NAME_SUFFIX	= "editor";

	private static final	String	TEMPORARY_DIRECTORY_PROPERTY_KEY	= "java.io.tmpdir";

	private static final	String	EXTRACTION_DIRECTORY_NAME	= "blankaspect." + UnzipApp.NAME_KEY;

	private static final	String	ZIP_FILE_PROPERTIES_KEY	= "zipFileProperties";

	private static final	KeyCombination	KEY_COMBO_FILTER_DIALOG	=
			new KeyCodeCombination(KeyCode.TAB, KeyCombination.CONTROL_DOWN);

	private static final	double	TABLE_VIEW_HEIGHT	= 506.0;

	/** The margins that are applied to the visual bounds of each screen when determining whether the saved location of
		the main window is within a screen. */
	private static final	Insets	SCREEN_MARGINS	= new Insets(0.0, 32.0, 32.0, 0.0);

	/** Miscellaneous strings. */
	private static final	String	ELLIPSIS_STR			= "...";
	private static final	String	STARTING_STR			= "Starting";
	private static final	String	TERMINATING_STR			= "Terminating";
	private static final	String	PID_STR					= "PID = ";
	private static final	String	ARGS1_STR				= "args[";
	private static final	String	ARGS2_STR				= "] = ";
	private static final	String	JAVA_VERSION_STR		= "Java version";
	private static final	String	CONFIG_ERROR_STR		= "Configuration error";
	private static final	String	FILE_STR				= "File";
	private static final	String	OPEN_STR				= "Open";
	private static final	String	CLOSE_STR				= "Close";
	private static final	String	COMPARE_STR				= "Compare";
	private static final	String	PROPERTIES_STR			= "Properties";
	private static final	String	EXIT_STR				= "Exit";
	private static final	String	EDIT_STR				= "Edit";
	private static final	String	SELECT_ALL_STR			= "Select all";
	private static final	String	DESELECT_ALL_STR		= "Deselect all";
	private static final	String	INVERT_SELECTION_STR	= "Invert selection";
	private static final	String	FILTER_ENTRIES_STR		= "Filter entries";
	private static final	String	PREFERENCES_STR			= "Preferences";
	private static final	String	ACTIONS_STR				= "Actions";
	private static final	String	EXTRACT_STR				= "Extract";
	private static final	String	OPEN_FILE_STR			= "Open file";
	private static final	String	EXTRACT_FILES_STR		= "Extract files";
	private static final	String	NUM_FILES_EXTRACTED_STR	= "Number of files extracted : ";
	private static final	String	MODIFIED_FILE_STR		= "Modified file";
	private static final	String	MODIFIED_MESSAGE_STR	= "The file '%s' has been modified externally.\n" +
																"Do you want to reload the modified file?";
	private static final	String	RELOAD_STR				= "Reload";
	private static final	String	EDIT_ENTRY_STR			= "Edit entry";

	/** Keys of properties. */
	private interface PropertyKey
	{
		String	APPEARANCE					= "appearance";
		String	CELL_VERTICAL_PADDING		= "cellVerticalPadding";
		String	COLUMN_HEADER_POP_UP_DELAY	= "columnHeaderPopUpDelay";
		String	COLUMN_WIDTHS				= "columnWidths";
		String	COMPARISON_DIALOG			= "comparisonDialog";
		String	EXTRACTION_DIALOG			= "extractionDialog";
		String	FILTER_DIALOG				= "filterDialog";
		String	MAIN_WINDOW					= "mainWindow";
		String	OPEN_FILE_DIRECTORY			= "openFileDirectory";
		String	REPLACE_FILES_DIALOG		= "replaceFilesDialog";
		String	THEME						= "theme";
		String	VIEW						= "view";
	}

	/** Keys of system properties. */
	private interface SystemPropertyKey
	{
		String	USE_STYLE_SHEET_FILE	= "useStyleSheetFile";
	}

	/** Error messages. */
	private interface ErrorMsg
	{
		String	FILE_DOES_NOT_EXIST						= "The file does not exist.";
		String	NOT_A_FILE								= "The location does not denote a regular file.";
		String	NO_AUXILIARY_DIRECTORY					= "The location of the auxiliary directory could not be determined.";
		String	NO_FILE_EDITORS							= "No file editors have been specified.";
		String	FAILED_TO_LOCATE_TEMPORARY_DIRECTORY	= "Failed to get the location of the system's temporary directory.";
		String	FAILED_TO_CREATE_DIRECTORY				= "Failed to create the directory.";
		String	MALFORMED_EDITOR_COMMAND				= "The editor command is malformed.";
		String	FAILED_TO_EXECUTE_EDITOR_COMMAND		= "Failed to execute the editor command.";
	}

////////////////////////////////////////////////////////////////////////
//  Class variables
////////////////////////////////////////////////////////////////////////

	/** The instance of this application. */
	private static	UnzipApp	instance;

	/** The index of the last thread that was created for a background task. */
	private static	int			threadIndex;

////////////////////////////////////////////////////////////////////////
//  Instance variables
////////////////////////////////////////////////////////////////////////

	private	ResourceProperties					buildProperties;
	private	String								versionStr;
	private	Configuration						configuration;
	private	Preferences							preferences;
	private	SimpleObjectProperty<ZipFileModel>	zipFile;
	private	WindowState							mainWindowState;
	private	Map<String, Double>					tableViewColumnWidths;
	private	List<Path>							locationsForDeletion;

	/** The directory that is associated with {@link #openFileChooser}. */
	private	Path								openFileDirectory;

	/** The main window. */
	private	Stage								primaryStage;

	/** The table view of zip-file entries. */
	private	ZipFileTableView					tableView;

	/** The file chooser for opening a zip file. */
	private	LocationChooser						openFileChooser;

	/** The dialog from which a filter may be applied to the zip-file entries that are displayed in the table view. */
	private	SimpleObjectProperty<FilterDialog>	filterDialog;

////////////////////////////////////////////////////////////////////////
//  Static initialiser
////////////////////////////////////////////////////////////////////////

	static
	{
		// Initialise logger and open log file
		LoggerUtils.openLogger(LOG_THRESHOLD, LOG_PARAMS, AppAuxDirectory.resolve(NAME_KEY, UnzipApp.class, LOG_FILENAME),
							   LOG_NUM_RETAINED_LINES);
	}

////////////////////////////////////////////////////////////////////////
//  Constructors
////////////////////////////////////////////////////////////////////////

	public UnzipApp()
	{
	}

	//------------------------------------------------------------------

////////////////////////////////////////////////////////////////////////
//  Class methods
////////////////////////////////////////////////////////////////////////

	public static void main(
		String[]	args)
	{
		launch(args);
	}

	//------------------------------------------------------------------

	public static UnzipApp instance()
	{
		return instance;
	}

	//------------------------------------------------------------------

	private static void executeTask(
		Runnable	task)
	{
		ExecutorService executor = Executors.newSingleThreadExecutor(runnable ->
				new Thread(runnable, NAME_KEY + "-" + ++threadIndex));
		executor.execute(task);
		executor.shutdown();
	}

	//------------------------------------------------------------------

////////////////////////////////////////////////////////////////////////
//  Instance methods : overriding methods
////////////////////////////////////////////////////////////////////////

	@Override
	public void init()
	{
		instance = this;
	}

	//------------------------------------------------------------------

	@Override
	public void start(
		Stage	primaryStage)
	{
		// Log stack trace of uncaught exception
		if (ClassUtils.isFromJar(getClass()))
		{
			Thread.setDefaultUncaughtExceptionHandler((thread, exception) ->
			{
				try
				{
					Logger.INSTANCE.error(exception);
				}
				catch (Exception e)
				{
					e.printStackTrace();
				}
			});
		}

		// Initialise instance variables
		preferences = new Preferences();
		zipFile = new SimpleObjectProperty<>();
		mainWindowState = new WindowState(false, true);
		tableViewColumnWidths = new LinkedHashMap<>();
		locationsForDeletion = new ArrayList<>();
		this.primaryStage = primaryStage;
		filterDialog = new SimpleObjectProperty<>();

		// Write 'starting' message to log
		StringBuilder buffer = new StringBuilder(256);
		buffer.append(STARTING_STR);
		buffer.append(' ');
		buffer.append(SHORT_NAME);
		buffer.append(" : ");
		buffer.append(PID_STR);
		buffer.append(ProcessHandle.current().pid());

		// Write command-line arguments to log
		List<String> args = getParameters().getRaw();
		if (!args.isEmpty())
		{
			for (int i = 0; i < args.size(); i++)
			{
				buffer.append('\n');
				buffer.append(ARGS1_STR);
				buffer.append(i);
				buffer.append(ARGS2_STR);
				buffer.append(args.get(i));
			}
		}
		Logger logger = Logger.INSTANCE;
		logger.saveParams();
		logger.setPrefix("-".repeat(64));
		logger.info(buffer.toString());
		logger.restoreParams();

		// Write Java version to log
		logger.info(JAVA_VERSION_STR + " " + System.getProperty("java.version"));

		// Read build properties and initialise version string
		try
		{
			buildProperties = new ResourceProperties(ResourceUtils.normalisedPathname(getClass(), BUILD_PROPERTIES_FILENAME));
			versionStr = BuildUtils.versionString(getClass(), buildProperties);
		}
		catch (LocationException e)
		{
			e.printStackTrace();
		}

		// Read configuration file and decode configuration
		BaseException configException = null;
		try
		{
			// Initialise configuration
			configuration = new Configuration();

			// Read configuration file
			configuration.read();

			// Decode configuration
			decodeConfig(configuration.getConfig());
		}
		catch (BaseException e)
		{
			configException = e;
		}

		// Get style manager
		StyleManager styleManager = StyleManager.INSTANCE;

		// Select theme
		String themeId = System.getProperty(StyleManager.SystemPropertyKey.THEME);
		if (StringUtils.isNullOrEmpty(themeId))
			themeId = preferences.getThemeId();
		if (themeId != null)
			styleManager.selectTheme(themeId);

		// Set ID and style-sheet filename on style manager
		if (Boolean.getBoolean(SystemPropertyKey.USE_STYLE_SHEET_FILE))
		{
			styleManager.setId(getClass().getSimpleName());
			styleManager.setStyleSheetFilename(STYLE_SHEET_FILENAME);
		}

		// Create table view
		tableView = new ZipFileTableView();
		tableView.setDisable(true);
		tableView.setPrefHeight(TABLE_VIEW_HEIGHT);
		if (configuration != null)
			tableView.setColumnWidths(tableViewColumnWidths);
		VBox.setVgrow(tableView, Priority.ALWAYS);

		// Create 'number selected' pane
		NumberSelectedPane numSelectedPane = new NumberSelectedPane();

		// Create procedure to update 'number selected' label
		IProcedure0 updateNumSelected = () ->
		{
			numSelectedPane.update(tableView.getItems().size(),
								   tableView.getSelectionModel().getSelectedIndices().size());
		};

		// Update 'number selected' label when number of entries changes
		tableView.getItems().addListener((InvalidationListener) observable -> updateNumSelected.invoke());

		// Update 'number selected' label when selected entries change
		tableView.getSelectionModel().getSelectedIndices().addListener((InvalidationListener) observable ->
				updateNumSelected.invoke());

		// Update 'number selected' label
		updateNumSelected.invoke();

		// Create scene
		Scene scene = new Scene(new VBox(createMenuBar(), tableView, numSelectedPane));

		// Add accelerators to scene
		scene.getAccelerators().put(KEY_COMBO_FILTER_DIALOG, () ->
		{
			// If filter dialog is displayed, request focus on it
			if (filterDialog.get() != null)
				filterDialog.get().requestFocus();
		});

		// Add style sheet to scene
		styleManager.addStyleSheet(scene);

		// Set drag-and-drop handler to accept a zip file
		scene.setOnDragOver(event ->
		{
			// Accept drag if dragboard contains a zip file
			if (ClipboardUtils.locationMatches(event.getDragboard(), preferences.getZipFileDragAndDropFilter()))
				event.acceptTransferModes(TransferMode.COPY);

			// Consume event
			event.consume();
		});

		// Set drag-and-drop handler to open zip file
		scene.setOnDragDropped(event ->
		{
			// Get location of first zip file from dragboard
			Path file = ClipboardUtils.firstMatchingLocation(event.getDragboard(),
															 preferences.getZipFileDragAndDropFilter());

			// Indicate that drag-and-drop is complete
			event.setDropCompleted(true);

			// Open zip file
			if (file != null)
				Platform.runLater(() -> openFile(file));

			// Consume event
			event.consume();
		});

		// Set properties of main window
		primaryStage.getIcons().addAll(Images.APP_ICON_IMAGES);

		// Set scene on main window
		primaryStage.setScene(scene);

		// Set location and size of main window when it is opening
		primaryStage.setOnShowing(event ->
		{
			// Set location of window
			Point2D location = mainWindowState.getLocation();
			if (location != null)
			{
				primaryStage.setX(location.getX());
				primaryStage.setY(location.getY());
			}

			// Set size of window
			Dimension2D size = mainWindowState.getSize();
			if (size == null)
				primaryStage.sizeToScene();
			else
			{
				primaryStage.setWidth(size.getWidth());
				primaryStage.setHeight(size.getHeight());
			}
		});

		// Set location of main window after it is shown
		primaryStage.setOnShown(event ->
		{
			// Get location of window
			Point2D location = mainWindowState.getLocation();

			// Invalidate location if top centre of window is not within a screen
			double width = primaryStage.getWidth();
			if ((location != null)
					&& !SceneUtils.isWithinScreen(location.getX() + 0.5 * width, location.getY(), SCREEN_MARGINS))
				location = null;

			// If there is no location, centre window within primary screen
			if (location == null)
			{
				location = SceneUtils.centreInScreen(width, primaryStage.getHeight());
				primaryStage.setX(location.getX());
				primaryStage.setY(location.getY());
			}
		});

		// Update title
		updateTitle();

		// Write configuration file when main window is closed
		if (configuration != null)
		{
			primaryStage.setOnHiding(event ->
			{
				// Update state of main window
				mainWindowState.restoreAndUpdate(primaryStage);

				// Write configuration
				if (configuration.canWrite())
				{
					try
					{
						encodeConfig(configuration.getConfig());
						configuration.write();
					}
					catch (FileException e)
					{
						// Display error message in dialog
						Utils.showErrorMessage(primaryStage, SHORT_NAME + " : " + CONFIG_ERROR_STR, e);
					}
				}
			});
		}

		// Display main window
		primaryStage.show();

		// Report any configuration error
		if (configException != null)
			Utils.showErrorMessage(primaryStage, SHORT_NAME + " : " + CONFIG_ERROR_STR, configException);

		// Add shutdown hook to delete temporary locations that weren't deleted in the stop() method
		Runtime.getRuntime().addShutdownHook(new Thread(() ->
		{
			for (int i = locationsForDeletion.size() - 1; i >= 0; i--)
			{
				try
				{
					FileSystemUtils.deleteWithRetries(locationsForDeletion.get(i), 3);
				}
				catch (Throwable e)
				{
					e.printStackTrace();
				}
			}
		}));

		// Open file that was specified on command line
		if (!args.isEmpty())
			openFile(Path.of(PathnameUtils.parsePathname(args.get(0))));

		// Start checking for modified file
		ScheduledExecutorService executor = Executors.newSingleThreadScheduledExecutor(runnable ->
				DaemonFactory.create(NAME_KEY + "-" + CHECK_MODIFIED_FILE_THREAD_NAME_SUFFIX, runnable));
		executor.scheduleWithFixedDelay(() ->
		{
			ZipFileModel zipFile = getZipFile();
			if (zipFile == null)
				return;

			Path file = zipFile.getLocation();
			FileTime oldTimestamp = zipFile.getTimestamp();
			try
			{
				FileTime timestamp = Files.getLastModifiedTime(file, LinkOption.NOFOLLOW_LINKS);
				if (!timestamp.equals(oldTimestamp))
				{
					zipFile.setTimestamp(timestamp);

					if (oldTimestamp == null)
						return;

					Platform.runLater(() ->
					{
						if (ConfirmationDialog.show(primaryStage, MODIFIED_FILE_STR, MessageIcon32.QUESTION.get(),
													String.format(MODIFIED_MESSAGE_STR, file.getFileName()),
													RELOAD_STR))
						{
							openFile(file);
						}
					});
				}
			}
			catch (IOException e)
			{
				// ignore
			}
		},
		CHECK_MODIFIED_FILE_INTERVAL, CHECK_MODIFIED_FILE_INTERVAL, TimeUnit.MILLISECONDS);
	}

	//------------------------------------------------------------------

	/**
	 * {@inheritDoc}
	 */

	@Override
	public void stop()
	{
		// Delete temporary locations
		for (int i = locationsForDeletion.size() - 1; i >= 0; i--)
		{
			try
			{
				FileSystemUtils.deleteWithRetries(locationsForDeletion.get(i), 3);
				locationsForDeletion.remove(i);
			}
			catch (Throwable e)
			{
				Logger.INSTANCE.error(e);
			}
		}

		// Write 'terminating' message to log
		Logger.INSTANCE.info(TERMINATING_STR + " " + SHORT_NAME);
	}

	//------------------------------------------------------------------

////////////////////////////////////////////////////////////////////////
//  Instance methods
////////////////////////////////////////////////////////////////////////

	public Preferences getPreferences()
	{
		return preferences;
	}

	//------------------------------------------------------------------

	public ZipFileTableView getTableView()
	{
		return tableView;
	}

	//------------------------------------------------------------------

	public MenuItem createMenuItemExtract()
	{
		MenuItem menuItem = new MenuItem(EXTRACT_STR + ELLIPSIS_STR);
		menuItem.setAccelerator(new KeyCodeCombination(KeyCode.E, KeyCombination.CONTROL_DOWN));
		menuItem.setOnAction(event -> onExtract());
		return menuItem;
	}

	//------------------------------------------------------------------

	public void editFile(
		ZipFileEntry	entry,
		boolean			selectEditor)
	{
		String title = EDIT_ENTRY_STR;
		List<String> arguments = new ArrayList<>();
		try
		{
			// Get file editors
			List<FileEditor> editors = preferences.getFileEditors();
			if (editors.isEmpty())
				throw new BaseException(ErrorMsg.NO_FILE_EDITORS);

			// Get file editor
			FileEditor editor = FileEditor.findFileEditor(entry.getFilename(), editors);
			if (selectEditor || (editor == null))
			{
				editor = new SelectEditorDialog(editor).showDialog();
				if (editor == null)
					return;
			}

			// Get location of parent of extraction directory
			String pathname = preferences.getFileEditorExtractionDirectory();
			if (pathname == null)
			{
				pathname = System.getProperty(TEMPORARY_DIRECTORY_PROPERTY_KEY);
				if (pathname == null)
					throw new BaseException(ErrorMsg.FAILED_TO_LOCATE_TEMPORARY_DIRECTORY);
			}
			Path parent = Path.of(pathname).resolve(EXTRACTION_DIRECTORY_NAME);

			// Find available location of subdirectory
			Path directory = FileSystemUtils.findAvailableLocationRandom(parent, "", "", 6, 0);

			// Get location of extracted file
			Path file = directory.resolve(entry.getFilename());

			// Parse editor command to create list of arguments
			try
			{
				StringBuilder buffer = new StringBuilder();
				String command = editor.getCommand();
				pathname = file.toString();
				int index = 0;
				while (index < command.length())
				{
					char ch = command.charAt(index++);
					switch (ch)
					{
						case FileEditor.COMMAND_ESCAPE_CHAR:
							if (index < command.length())
							{
								ch = command.charAt(index++);
								if (ch == FileEditor.COMMAND_PATHNAME_PLACEHOLDER_CHAR)
									buffer.append(pathname);
								else if (ch == FileEditor.COMMAND_URI_PLACEHOLDER_CHAR)
									buffer.append(Path.of(pathname).toUri());
								else
									buffer.append(ch);
							}
							break;

						case ' ':
							if (!buffer.isEmpty())
							{
								arguments.add(PathnameUtils.parsePathname(buffer.toString()));
								buffer.setLength(0);
							}
							break;

						default:
							buffer.append(ch);
							break;
					}
				}
				if (!buffer.isEmpty())
					arguments.add(PathnameUtils.parsePathname(buffer.toString()));
			}
			catch (IllegalArgumentException e)
			{
				throw new BaseException(ErrorMsg.MALFORMED_EDITOR_COMMAND);
			}

			// Create extraction directory
			try
			{
				// Create directory
				Files.createDirectories(directory);

				// Add location of directory to list of locations to be deleted when application terminates
				locationsForDeletion.add(directory);
			}
			catch (Exception e)
			{
				throw new FileException(ErrorMsg.FAILED_TO_CREATE_DIRECTORY, directory);
			}

			// Create task to extract entry
			Path outDirectory = directory;
			Task<Void> task = new AbstractTask<>()
			{
				{
					// Initialise task
					updateTitle(title);
				}

				@Override
				protected Void call()
					throws Exception
				{
					// Extract entry
					getZipFile().extractEntry(entry, outDirectory, createTaskStatus());

					// Return nothing
					return null;
				}

				@Override
				protected void succeeded()
				{
					// Add location of file to list of locations to be deleted when application terminates
					locationsForDeletion.add(file);

					// Execute editor command
					DaemonFactory.create(NAME_KEY + "-" + EDITOR_THREAD_NAME_SUFFIX + "-" + ++threadIndex, () ->
					{
						try
						{
							// Create process and start it
							ProcessBuilder processBuilder = new ProcessBuilder(arguments);
							processBuilder.inheritIO();
							processBuilder.start();
						}
						catch (IOException e)
						{
							Platform.runLater(() ->
									ErrorDialog.show(primaryStage, title, ErrorMsg.FAILED_TO_EXECUTE_EDITOR_COMMAND));
						}
					})
					.start();
				}

				@Override
				protected void failed()
				{
					// Display error message in dialog
					showErrorMessage(primaryStage);
				}
			};

			// Show progress of task in dialog
			new SimpleProgressDialog(primaryStage, task);

			// Execute task on background thread
			executeTask(task);
		}
		catch (BaseException e)
		{
			ErrorDialog.show(primaryStage, title, e);
		}
	}

	//------------------------------------------------------------------

	private ZipFileModel getZipFile()
	{
		return zipFile.get();
	}

	//------------------------------------------------------------------

	private void encodeConfig(
		MapNode	rootNode)
	{
		// Clear properties
		rootNode.clear();

		// Encode theme ID
		rootNode.addMap(PropertyKey.APPEARANCE).addString(PropertyKey.THEME, preferences.getThemeId());

		// Encode state of main window
		MapNode mainWindowNode = mainWindowState.encodeTree();
		rootNode.add(PropertyKey.MAIN_WINDOW, mainWindowNode);

		// Create view node
		MapNode viewNode = mainWindowNode.addMap(PropertyKey.VIEW);

		// Encode widths of columns of table
		MapNode columnsNode = new MapNode();
		for (TableColumn<ZipFileEntry, ?> column : tableView.getColumns())
			columnsNode.addDouble(column.getId(), column.getWidth());
		if (!columnsNode.isEmpty())
			viewNode.add(PropertyKey.COLUMN_WIDTHS, columnsNode);

		// Encode cell vertical padding
		viewNode.addInt(PropertyKey.CELL_VERTICAL_PADDING, preferences.getCellVerticalPadding());

		// Encode column-header pop-up delay
		viewNode.addInt(PropertyKey.COLUMN_HEADER_POP_UP_DELAY, preferences.getColumnHeaderPopUpDelay());

		// Encode 'open file' directory
		Utils.encodeLocation(rootNode, PropertyKey.OPEN_FILE_DIRECTORY, openFileDirectory);

		// Encode preferences
		preferences.encode(rootNode);

		// Encode state of extraction dialog
		MapNode extractionDialogNode = ExtractionDialog.encodeState();
		if (!extractionDialogNode.isEmpty())
			rootNode.add(PropertyKey.EXTRACTION_DIALOG, extractionDialogNode);

		// Encode state of 'replace files' dialog
		MapNode replaceFilesDialogNode = ReplaceFilesDialog.encodeState();
		if (!replaceFilesDialogNode.isEmpty())
			rootNode.add(PropertyKey.REPLACE_FILES_DIALOG, replaceFilesDialogNode);

		// Encode state of filter dialog
		MapNode filterDialogNode = FilterDialog.encodeState();
		if (!filterDialogNode.isEmpty())
			rootNode.add(PropertyKey.FILTER_DIALOG, filterDialogNode);

		// Encode state of comparison dialog
		MapNode comparisonDialogNode = ComparisonDialog.encodeState();
		if (!comparisonDialogNode.isEmpty())
			rootNode.add(PropertyKey.COMPARISON_DIALOG, comparisonDialogNode);
	}

	//------------------------------------------------------------------

	private void decodeConfig(
		MapNode	rootNode)
	{
		// Decode theme ID
		String key = PropertyKey.APPEARANCE;
		if (rootNode.hasMap(key))
			preferences.setThemeId(rootNode.getMapNode(key).getString(PropertyKey.THEME, StyleManager.DEFAULT_THEME_ID));

		// Decode properties of main window
		key = PropertyKey.MAIN_WINDOW;
		if (rootNode.hasMap(key))
		{
			// Decode state of main window
			MapNode mainWindowNode = rootNode.getMapNode(key);
			mainWindowState.decodeTree(mainWindowNode);

			// Decode view properties
			key = PropertyKey.VIEW;
			if (mainWindowNode.hasMap(key))
			{
				// Get view node
				MapNode viewNode = mainWindowNode.getMapNode(key);

				// Decode widths of columns of table
				tableViewColumnWidths.clear();
				key = PropertyKey.COLUMN_WIDTHS;
				if (viewNode.hasMap(key))
				{
					MapNode columnsNode = viewNode.getMapNode(key);
					for (String key0 : columnsNode.getKeys())
						tableViewColumnWidths.put(key0, columnsNode.getDouble(key0));
				}

				// Encode cell vertical padding
				key = PropertyKey.CELL_VERTICAL_PADDING;
				if (viewNode.hasInt(key))
					preferences.setCellVerticalPadding(viewNode.getInt(key));

				// Decode column-header pop-up delay
				preferences.setColumnHeaderPopUpDelay(viewNode.getInt(PropertyKey.COLUMN_HEADER_POP_UP_DELAY,
																	  ZipFileTableView.DEFAULT_HEADER_CELL_POP_UP_DELAY));
			}
		}

		// Decode 'open file' directory
		key = PropertyKey.OPEN_FILE_DIRECTORY;
		if (rootNode.hasString(key))
			openFileDirectory = Utils.decodeLocation(rootNode, key);

		// Decode preferences
		preferences.decode(rootNode);

		// Decode state of extraction dialog
		key = PropertyKey.EXTRACTION_DIALOG;
		if (rootNode.hasMap(key))
			ExtractionDialog.decodeState(rootNode.getMapNode(key));

		// Decode state of 'replace files' dialog
		key = PropertyKey.REPLACE_FILES_DIALOG;
		if (rootNode.hasMap(key))
			ReplaceFilesDialog.decodeState(rootNode.getMapNode(key));

		// Decode state of filter dialog
		key = PropertyKey.FILTER_DIALOG;
		if (rootNode.hasMap(key))
			FilterDialog.decodeState(rootNode.getMapNode(key));

		// Decode state of comparison dialog
		key = PropertyKey.COMPARISON_DIALOG;
		if (rootNode.hasMap(key))
			ComparisonDialog.decodeState(rootNode.getMapNode(key));
	}

	//------------------------------------------------------------------

	private MenuBar createMenuBar()
	{
		// Create menu bar
		MenuBar menuBar = new MenuBar();
		menuBar.setPadding(Insets.EMPTY);

		// Create menu: file
		Menu menu = new Menu(FILE_STR);
		menuBar.getMenus().add(menu);

		// Add menu item: open
		MenuItem menuItem = new MenuItem(OPEN_STR + ELLIPSIS_STR);
		menuItem.setAccelerator(new KeyCodeCombination(KeyCode.O, KeyCombination.CONTROL_DOWN));
		menuItem.setOnAction(event -> onOpenFile());
		menu.getItems().add(menuItem);

		// Add separator
		menu.getItems().add(new SeparatorMenuItem());

		// Add menu item: close
		menuItem = new MenuItem(CLOSE_STR);
		menuItem.disableProperty().bind(zipFile.isNull());
		menuItem.setAccelerator(new KeyCodeCombination(KeyCode.W, KeyCombination.CONTROL_DOWN));
		menuItem.setOnAction(event -> onCloseFile());
		menu.getItems().add(menuItem);

		// Add separator
		menu.getItems().add(new SeparatorMenuItem());

		// Add menu item: compare
		menuItem = new MenuItem(COMPARE_STR + ELLIPSIS_STR);
		menuItem.disableProperty().bind(zipFile.isNull());
		menuItem.setOnAction(event -> onCompare());
		menu.getItems().add(menuItem);

		// Add separator
		menu.getItems().add(new SeparatorMenuItem());

		// Add menu item: properties
		menuItem = new MenuItem(PROPERTIES_STR);
		menuItem.disableProperty().bind(zipFile.isNull());
		menuItem.setAccelerator(new KeyCodeCombination(KeyCode.ENTER, KeyCombination.ALT_DOWN));
		menuItem.setOnAction(event -> onShowProperties());
		menu.getItems().add(menuItem);

		// Add separator
		menu.getItems().add(new SeparatorMenuItem());

		// Add menu item: exit
		menuItem = new MenuItem(EXIT_STR);
		menuItem.setOnAction(event -> Platform.exit());
		menu.getItems().add(menuItem);

		// Create menu: edit
		menu = new Menu(EDIT_STR);
		menuBar.getMenus().add(menu);

		// Add menu item: select all
		menuItem = new MenuItem(SELECT_ALL_STR);
		menuItem.disableProperty().bind(zipFile.isNull());
		menuItem.setAccelerator(new KeyCodeCombination(KeyCode.A, KeyCombination.CONTROL_DOWN));
		menuItem.setOnAction(event -> onSelectAll());
		menu.getItems().add(menuItem);

		// Add menu item: deselect all
		menuItem = new MenuItem(DESELECT_ALL_STR);
		menuItem.disableProperty().bind(zipFile.isNull());
		menuItem.setOnAction(event -> onDeselectAll());
		menu.getItems().add(menuItem);

		// Add menu item: invert selection
		menuItem = new MenuItem(INVERT_SELECTION_STR);
		menuItem.disableProperty().bind(zipFile.isNull());
		menuItem.setAccelerator(new KeyCodeCombination(KeyCode.I, KeyCombination.CONTROL_DOWN));
		menuItem.setOnAction(event -> onInvertSelection());
		menu.getItems().add(menuItem);

		// Add separator
		menu.getItems().add(new SeparatorMenuItem());

		// Add menu item: filter entries
		menuItem = new MenuItem(FILTER_ENTRIES_STR + ELLIPSIS_STR);
		menuItem.disableProperty().bind(zipFile.isNull().or(filterDialog.isNotNull()));
		menuItem.setAccelerator(new KeyCodeCombination(KeyCode.F, KeyCombination.CONTROL_DOWN));
		menuItem.setOnAction(event -> onFilterEntries());
		menu.getItems().add(menuItem);

		// Add separator
		menu.getItems().add(new SeparatorMenuItem());

		// Add menu item: preferences
		menuItem = new MenuItem(PREFERENCES_STR);
		menuItem.setOnAction(event -> onEditPreferences());
		menu.getItems().add(menuItem);

		// Create menu: actions
		menu = new Menu(ACTIONS_STR);
		menuBar.getMenus().add(menu);

		// Add menu item: extract
		menuItem = createMenuItemExtract();
		menuItem.disableProperty().bind(tableView.getSelectionModel().selectedItemProperty().isNull());
		menu.getItems().add(menuItem);

		// Return menu bar
		return menuBar;
	}

	//------------------------------------------------------------------

	private void updateTitle()
	{
		ZipFileModel zipFile = getZipFile();
		primaryStage.setTitle((zipFile == null) ? LONG_NAME + " " + versionStr
												: LONG_NAME + " - " + zipFile.getLocation().toAbsolutePath());
	}

	//------------------------------------------------------------------

	private void closeFilterDialog()
	{
		FilterDialog dialog = filterDialog.get();
		if (dialog != null)
		{
			// Clear filter
			dialog.clearFilter();

			// Close dialog
			dialog.hide();
		}
	}

	//------------------------------------------------------------------

	private void openFile(
		Path	location)
	{
		// Log title of task
		String title = OPEN_FILE_STR;
		Logger.INSTANCE.info(title + " : " + location.toAbsolutePath());

		// Test for file
		try
		{
			// Test whether file exists
			if (!Files.exists(location, LinkOption.NOFOLLOW_LINKS))
				throw new FileException(ErrorMsg.FILE_DOES_NOT_EXIST, location);

			// Test for regular file
			if (!Files.isRegularFile(location, LinkOption.NOFOLLOW_LINKS))
				throw new FileException(ErrorMsg.NOT_A_FILE, location);
		}
		catch (FileException e)
		{
			// Log error
			Logger.INSTANCE.error(title, e);

			// Display error dialog
			ErrorDialog.show(primaryStage, title, e);
			return;
		}

		// Create task to open file
		Task<ZipFileModel> task = new AbstractTask<>()
		{
			{
				// Initialise task
				updateTitle(title);
			}

			@Override
			protected ZipFileModel call()
				throws Exception
			{
				// Create zip file
				ZipFileModel zipFile = new ZipFileModel();

				// Read zip-file entries
				zipFile.readEntries(location, createTaskStatus());

				// If task has been cancelled, change state to 'cancelled'
				hardCancel(false);

				// Return zip file
				return zipFile;
			}

			@Override
			protected void succeeded()
			{
				// Get result
				ZipFileModel result = getValue();

				// Update instance variable
				zipFile.set(result);

				// Update title
				UnzipApp.this.updateTitle();

				// Enable table view
				tableView.setDisable(false);

				// Set result on table view
				tableView.setZipFile(result);

				// WORKAROUND for bug in JavaFX
				tableView.setFocusTraversable(false);
				Platform.runLater(() ->
				{
					tableView.setFocusTraversable(true);
					tableView.requestFocus();
				});

				// Clear filter of filter dialog
				FilterDialog dialog = filterDialog.get();
				if (dialog != null)
					dialog.clearFilter();
			}

			@Override
			protected void failed()
			{
				// Display error message in dialog
				showErrorMessage(primaryStage);
			}
		};

		// Show progress of task in dialog
		new SimpleProgressDialog(primaryStage, task, SimpleProgressDialog.CancelMode.NO_INTERRUPT);

		// Execute task on background thread
		executeTask(task);
	}

	//------------------------------------------------------------------

	private void onOpenFile()
	{
		// Initialise file chooser
		if (openFileChooser == null)
		{
			openFileChooser = LocationChooser.forFiles();
			openFileChooser.setDialogTitle(OPEN_FILE_STR);
			openFileChooser.setDialogStateKey();
		}

		// Set filters of file chooser
		openFileChooser.clearFilters();
		openFileChooser.addFilters(preferences.getZipFileFilter(), FileMatcher.ALL_FILES);
		openFileChooser.setInitialFilter(0);

		// Set initial directory of file chooser
		openFileChooser.initDirectory(openFileDirectory, DEFAULT_DIRECTORY);

		// Display file chooser
		Path file = openFileChooser.showOpenDialog(primaryStage);

		// Open file
		if (file != null)
		{
			// Update directory
			openFileDirectory = file.toAbsolutePath().getParent();

			// Open file
			openFile(file);
		}
	}

	//------------------------------------------------------------------

	private void onCloseFile()
	{
		// Invalidate zip file
		zipFile.set(null);

		// Close search dialog
		closeFilterDialog();

		// Clear table view
		tableView.setZipFile(null);
		tableView.setDisable(true);

		// Update title
		updateTitle();
	}

	//------------------------------------------------------------------

	private void onCompare()
	{
		new ComparisonDialog(primaryStage, getZipFile()).showDialog();
	}

	//------------------------------------------------------------------

	private void onShowProperties()
	{
		ZipFileModel zipFile = getZipFile();
		if (zipFile != null)
		{
			PropertiesPane.create()
							.padding(new Insets(2.0))
							.nameConverter(name -> name.toLowerCase())
							.properties1(zipFile.getProperties())
							.showDialog(primaryStage, ZIP_FILE_PROPERTIES_KEY, PROPERTIES_STR);
		}
	}

	//------------------------------------------------------------------

	private void onSelectAll()
	{
		tableView.getSelectionModel().selectAll();
	}

	//------------------------------------------------------------------

	private void onDeselectAll()
	{
		tableView.getSelectionModel().clearSelection();
	}

	//------------------------------------------------------------------

	private void onInvertSelection()
	{
		SelectionModelUtils.invertSelection(tableView.getSelectionModel(), tableView.getItems().size());
	}

	//------------------------------------------------------------------

	private void onFilterEntries()
	{
		FilterDialog dialog = filterDialog.get();
		if (dialog == null)
		{
			dialog = new FilterDialog(primaryStage);
			dialog.setOnHidden(event -> filterDialog.set(null));
			filterDialog.set(dialog);

			dialog.show();
		}
	}

	//------------------------------------------------------------------

	private void onEditPreferences()
	{
		// Display dialog
		Preferences result = PreferencesDialog.show(primaryStage, preferences);

		// If dialog was accepted, update and apply preferences
		if (result != null)
		{
			// Update instance variable
			preferences = result;

			// Apply theme
			StyleManager styleManager = StyleManager.INSTANCE;
			String themeId = preferences.getThemeId();
			if ((themeId != null) && !themeId.equals(styleManager.getThemeId()))
			{
				// Update theme
				styleManager.selectTheme(themeId);

				// Reapply style sheet to the scenes of all JavaFX windows
				styleManager.reapplyStylesheet();
			}

			// Update height of cells of table view
			tableView.updateCellHeight();
			tableView.refresh();

			// Update header-cell pop-up delay
			tableView.setHeaderCellPopUpDelay(result.getColumnHeaderPopUpDelay());
		}
	}

	//------------------------------------------------------------------

	private void onExtract()
	{
		// Test for selected entries
		if (tableView.getSelectionModel().isEmpty())
			return;

		// Display dialog for extraction location
		ZipFileModel zipFile = getZipFile();
		ExtractionDialog.Result result = new ExtractionDialog(primaryStage, zipFile.getLocation().getParent()).showDialog();
		if (result == null)
			return;

		// Create bit arrays of selected entries and selected entries whose output file exists
		List<ZipFileEntry> entries = tableView.getItems();
		int numEntries = entries.size();
		BitSet selection = new BitSet(numEntries);
		BitSet conflicts = new BitSet(numEntries);
		int numConflicts = 0;
		for (int index : tableView.getSelectionModel().getSelectedIndices())
		{
			Path file = entries.get(index).getOutputFile(result.directory(), result.flatten());
			if (Files.exists(file, LinkOption.NOFOLLOW_LINKS))
			{
				conflicts.set(index);
				++numConflicts;
			}
			else
				selection.set(index);
		}

		// If there are selected entries whose output file exists, display dialog to resolve conflicts
		if (numConflicts > 0)
		{
			BitSet replace = new ReplaceFilesDialog(primaryStage, entries, conflicts, result.directory(),
													result.flatten()).showDialog();
			if (replace == null)
				return;
			selection.or(replace);
		}

		// Create task to extract files
		Task<Integer> task = new AbstractTask<>()
		{
			{
				// Initialise task
				updateTitle(EXTRACT_FILES_STR);
			}

			@Override
			protected Integer call()
				throws Exception
			{
				// Extract entries
				int numFilesExtracted = getZipFile().extractEntries(entries, selection, result.directory(),
																	result.flatten(), createTaskStatus());

				// If task has been cancelled, change state to 'cancelled'
				hardCancel(false);

				// Return number of files extracted
				return numFilesExtracted;
			}

			@Override
			protected void succeeded()
			{
				// Display number of files extracted
				NotificationDialog.show(primaryStage, getTitle(), MessageIcon32.INFORMATION.get(),
										NUM_FILES_EXTRACTED_STR + getValue());
			}

			@Override
			protected void failed()
			{
				// Display error message in dialog
				showErrorMessage(primaryStage);
			}
		};

		// Show progress of task in dialog
		new SimpleProgressDialog(primaryStage, task, SimpleProgressDialog.CancelMode.NO_INTERRUPT);

		// Execute task on background thread
		executeTask(task);
	}

	//------------------------------------------------------------------

////////////////////////////////////////////////////////////////////////
//  Member classes : non-inner classes
////////////////////////////////////////////////////////////////////////


	// CLASS: CONFIGURATION


	/**
	 * This class implements the configuration of the application.
	 */

	private static class Configuration
		extends AppConfig
	{

	////////////////////////////////////////////////////////////////////
	//  Constants
	////////////////////////////////////////////////////////////////////

		/** The identifier of a configuration file. */
		private static final	String	ID	= "DXA6WQF5J3PV88K1J3GO7B9K8";

	////////////////////////////////////////////////////////////////////
	//  Constructors
	////////////////////////////////////////////////////////////////////

		/**
		 * Creates a new instance of the configuration of the application.
		 *
		 * @throws BaseException
		 *           if the configuration directory could not be determined.
		 */

		private Configuration()
			throws BaseException
		{
			// Call superclass constructor
			super(ID, NAME_KEY, SHORT_NAME, LONG_NAME);

			// Get location of parent directory of config file
			AppAuxDirectory.Directory directory = AppAuxDirectory.getDirectory(NAME_KEY, UnzipApp.class);
			if (directory == null)
				throw new BaseException(ErrorMsg.NO_AUXILIARY_DIRECTORY);

			// Set parent directory of config file
			setDirectory(directory.location());
		}

		//--------------------------------------------------------------

	}

	//==================================================================

////////////////////////////////////////////////////////////////////////
//  Member classes : inner classes
////////////////////////////////////////////////////////////////////////


	// CLASS: 'SELECT EDITOR' DIALOG


	private class SelectEditorDialog
		extends SimpleModalDialog<FileEditor>
	{

	////////////////////////////////////////////////////////////////////
	//  Constants
	////////////////////////////////////////////////////////////////////

		private static final	Insets	CONTENT_PANE_PADDING	= new Insets(8.0, 12.0, 8.0, 12.0);

		private static final	String	SELECT_FILE_EDITOR_STR	= "Select file editor";
		private static final	String	FILE_EDITOR_STR			= "File editor";

	////////////////////////////////////////////////////////////////////
	//  Instance variables
	////////////////////////////////////////////////////////////////////

		private	FileEditor	result;

	////////////////////////////////////////////////////////////////////
	//  Constructors
	////////////////////////////////////////////////////////////////////

		private SelectEditorDialog(
			FileEditor	editor)
		{
			// Call superclass constructor
			super(primaryStage, MethodHandles.lookup().lookupClass().getCanonicalName(), null, SELECT_FILE_EDITOR_STR);

			// Create spinner: file editor
			CollectionSpinner<FileEditor> editorSpinner =
					CollectionSpinner.leftRightH(HPos.CENTER, true, preferences.getFileEditors(), editor, null,
												 FileEditor::getName);

			// Create control pane
			HBox controlPane = new HBox(6.0, new Label(FILE_EDITOR_STR), editorSpinner);
			controlPane.setAlignment(Pos.CENTER_LEFT);

			// Add control pane to content pane
			addContent(controlPane);

			// Adjust padding around content pane
			getContentPane().setPadding(CONTENT_PANE_PADDING);

			// Create button: edit
			Button editButton = new Button(EDIT_STR);
			editButton.getProperties().put(BUTTON_GROUP_KEY, BUTTON_GROUP1);
			editButton.setOnAction(event ->
			{
				result = editorSpinner.getItem();
				requestClose();
			});
			addButton(editButton, HPos.RIGHT);

			// Create button: cancel
			Button cancelButton = new Button(CANCEL_STR);
			cancelButton.getProperties().put(BUTTON_GROUP_KEY, BUTTON_GROUP1);
			cancelButton.setOnAction(event -> requestClose());
			addButton(cancelButton, HPos.RIGHT);

			// Fire 'cancel' button if Escape key is pressed; fire 'edit' button if Ctrl+Enter is pressed
			setKeyFireButton(cancelButton, editButton);

			// Apply new style sheet to scene
			applyStyleSheet();
		}

		//--------------------------------------------------------------

	////////////////////////////////////////////////////////////////////
	//  Instance methods : overriding methods
	////////////////////////////////////////////////////////////////////

		@Override
		protected FileEditor getResult()
		{
			return result;
		}

		//--------------------------------------------------------------

	}

	//==================================================================

}

//----------------------------------------------------------------------
