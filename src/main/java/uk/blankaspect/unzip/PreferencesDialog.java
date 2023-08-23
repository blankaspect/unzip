/*====================================================================*\

PreferencesDialog.java

Class: preferences dialog.

\*====================================================================*/


// PACKAGE


package uk.blankaspect.unzip;

//----------------------------------------------------------------------


// IMPORTS


import java.lang.invoke.MethodHandles;

import java.nio.file.Path;

import java.util.List;

import javafx.geometry.HPos;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.geometry.Side;
import javafx.geometry.VPos;

import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Spinner;
import javafx.scene.control.Tab;
import javafx.scene.control.TextField;

import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;

import javafx.scene.paint.Color;

import javafx.stage.Window;

import uk.blankaspect.common.css.CssRuleSet;
import uk.blankaspect.common.css.CssSelector;

import uk.blankaspect.common.function.IProcedure0;
import uk.blankaspect.common.function.IProcedure1;

import uk.blankaspect.common.string.StringUtils;

import uk.blankaspect.ui.jfx.container.PathnamePane;

import uk.blankaspect.ui.jfx.dialog.ConfirmationDialog;
import uk.blankaspect.ui.jfx.dialog.MessageDialog;
import uk.blankaspect.ui.jfx.dialog.SimpleModalDialog;
import uk.blankaspect.ui.jfx.dialog.SingleTextFieldDialog;
import uk.blankaspect.ui.jfx.dialog.WarningDialog;

import uk.blankaspect.ui.jfx.image.MessageIcon32;

import uk.blankaspect.ui.jfx.listview.ListViewEditor;
import uk.blankaspect.ui.jfx.listview.ListViewStyle;
import uk.blankaspect.ui.jfx.listview.SimpleTextListView;

import uk.blankaspect.ui.jfx.locationchooser.LocationChooser;

import uk.blankaspect.ui.jfx.scene.SceneUtils;

import uk.blankaspect.ui.jfx.spinner.CollectionSpinner;
import uk.blankaspect.ui.jfx.spinner.SpinnerFactory;

import uk.blankaspect.ui.jfx.style.ColourProperty;
import uk.blankaspect.ui.jfx.style.FxProperty;
import uk.blankaspect.ui.jfx.style.RuleSetBuilder;
import uk.blankaspect.ui.jfx.style.StyleConstants;
import uk.blankaspect.ui.jfx.style.StyleManager;

import uk.blankaspect.ui.jfx.tabbedpane.TabPane2;
import uk.blankaspect.ui.jfx.tabbedpane.TabPaneUtils;

import uk.blankaspect.ui.jfx.textfield.FilterFactory;
import uk.blankaspect.ui.jfx.textfield.PathnameField;

import uk.blankaspect.ui.jfx.tooltip.TooltipDecorator;

//----------------------------------------------------------------------


// CLASS: PREFERENCES DIALOG


public class PreferencesDialog
	extends SimpleModalDialog<Preferences>
{

////////////////////////////////////////////////////////////////////////
//  Constants
////////////////////////////////////////////////////////////////////////

	private static final	double	CONTROL_H_GAP	= 6.0;
	private static final	double	CONTROL_V_GAP	= 6.0;

	private static final	Insets	CONTROL_PANE_PADDING	= new Insets(8.0, 12.0, 8.0, 12.0);

	private static final	Insets	TABBED_PANE_HEADER_PADDING	= new Insets(2.0, 2.0, 0.0, 2.0);

	private static final	double	MIN_TAB_WIDTH	= 72.0;

	private static final	int		MIN_CELL_VERTICAL_PADDING	= 0;
	private static final	int		MAX_CELL_VERTICAL_PADDING	= 9;

	private static final	int		MIN_COLUMN_HEADER_POP_UP_DELAY	= 0;
	private static final	int		MAX_COLUMN_HEADER_POP_UP_DELAY	= 5000;

	private static final	int		CELL_VERTICAL_PADDING_SPINNER_NUM_DIGITS	= 1;
	private static final	int		COLUMN_HEADER_POP_UP_DELAY_SPINNER_NUM_DIGITS	= 4;

	private static final	double	FILENAME_EXT_LIST_VIEW_WIDTH	= 160.0;
	private static final	double	FILENAME_EXT_LIST_VIEW_HEIGHT	= 240.0;

	private static final	Insets	FILENAME_EXT_LIST_VIEW_EDITOR_PADDING	= new Insets(4.0);

	private static final	Insets	FILENAME_EXT_LIST_VIEW_EDITOR_BUTTON_PANE_PADDING	= new Insets(2.0, 6.0, 2.0, 0.0);

	private static final	int		DIRECTORY_FIELD_NUM_COLUMNS	= 40;

	private static final	double	FILE_EDITOR_LIST_VIEW_WIDTH		= 240.0;
	private static final	double	FILE_EDITOR_LIST_VIEW_HEIGHT	= 240.0;

	private static final	Insets	FILE_EDITOR_LIST_VIEW_EDITOR_PADDING	= new Insets(4.0);

	private static final	Insets	FILE_EDITOR_LIST_VIEW_EDITOR_BUTTON_PANE_PADDING	= new Insets(2.0, 6.0, 2.0, 0.0);

	private static final	Path	DEFAULT_DIRECTORY	= Path.of(System.getProperty("user.dir", "."));

	private static final	String	FILENAME_SUFFIX_EDIT_NAME	= "filename suffix";
	private static final	String	FILENAME_SUFFIX_EDIT_LABEL	= "Filename suffix";

	private static final	String	PREFERENCES_STR							= "Preferences";
	private static final	String	THEME_STR								= "Theme";
	private static final	String	VERTICAL_PADDING_STR					= "Vertical padding of table row";
	private static final	String	COLUMN_HEADER_POP_UP_DELAY_STR			= "Table-column header pop-up delay";
	private static final	String	MS_STR									= "ms";
	private static final	String	REMOVE_FILENAME_SUFFIX_STR				= "Remove filename suffix";
	private static final	String	REMOVE_FILENAME_SUFFIX_QUESTION_STR		= "Do you want to remove the '%s' suffix?";
	private static final	String	DEFAULT_EXTRACTION_DIR_STR				= "Default extraction directory";
	private static final	String	CHOOSE_DEFAULT_EXTRACTION_DIR_STR		= "Choose default extraction directory";
	private static final	String	NO_DEFAULT_EXTRACTION_DIR_STR			= "A default extraction directory must be specified";
	private static final	String	FILE_EDITOR_EXTRACTION_DIR_STR			= "File-editor extraction directory";
	private static final	String	CHOOSE_FILE_EDITOR_EXTRACTION_DIR_STR	= "Choose file-editor extraction directory";
	private static final	String	FILE_EDITOR_STR							= "file editor";
	private static final	String	REMOVE_EDITOR_STR						= "Remove editor";
	private static final	String	REMOVE_EDITOR_QUESTION_STR				= "Editor: %s" + MessageDialog.MESSAGE_SEPARATOR
																				+ "Do you want to remove the selected editor?";
	private static final	String	REMOVE_STR								= "Remove";

	/** CSS colour properties. */
	private static final	List<ColourProperty>	COLOUR_PROPERTIES	= List.of
	(
		ColourProperty.of
		(
			FxProperty.BORDER_COLOUR,
			ColourKey.TABBED_PANE_BORDER,
			CssSelector.builder()
						.cls(StyleClass.TABBED_PANE)
						.build()
		)
	);

	/** CSS rule sets. */
	private static final	List<CssRuleSet>	RULE_SETS	= List.of
	(
		RuleSetBuilder.create()
						.selector(CssSelector.builder()
									.cls(StyleClass.TABBED_PANE)
									.build())
						.borders(Side.BOTTOM)
						.build()
	);

	/** CSS style classes. */
	private interface StyleClass
	{
		String	TABBED_PANE	= StyleConstants.CLASS_PREFIX + "unzip-preferences-dialog-tabbed-pane";
	}

	/** Keys of colours that are used in colour properties. */
	private interface ColourKey
	{
		String	PREFIX	= StyleManager.colourKeyPrefix(MethodHandles.lookup().lookupClass().getEnclosingClass());

		String	TABBED_PANE_BORDER	= PREFIX + "tabbedPane.border";
	}

////////////////////////////////////////////////////////////////////////
//  Class variables
////////////////////////////////////////////////////////////////////////

	private static	int	selectedTabIndex;

////////////////////////////////////////////////////////////////////////
//  Instance variables
////////////////////////////////////////////////////////////////////////

	private	Preferences	result;
	private	TabPane2	tabPane;

////////////////////////////////////////////////////////////////////////
//  Static initialiser
////////////////////////////////////////////////////////////////////////

	static
	{
		// Register the style properties of this class and its dependencies with the style manager
		StyleManager.INSTANCE.register(PreferencesDialog.class, COLOUR_PROPERTIES, RULE_SETS,
									   ListViewStyle.class);
	}

////////////////////////////////////////////////////////////////////////
//  Constructors
////////////////////////////////////////////////////////////////////////

	private PreferencesDialog(
		Window		owner,
		Preferences	preferences)
	{
		// Call superclass constructor
		super(owner, MethodHandles.lookup().lookupClass().getName(), null, PREFERENCES_STR);

		// Create tabbed pane
		tabPane = new TabPane2();
		tabPane.setBorder(SceneUtils.createSolidBorder(getColour(ColourKey.TABBED_PANE_BORDER), Side.BOTTOM));
		tabPane.setTabMinWidth(MIN_TAB_WIDTH);
		tabPane.getStyleClass().add(StyleClass.TABBED_PANE);

		// Set tabbed pane as content of dialog
		setContent(tabPane);

		// Set padding around header of tabbed pane
		tabPane.skinProperty().addListener(observable ->
				TabPaneUtils.setHeaderAreaPadding(tabPane, TABBED_PANE_HEADER_PADDING));

		// Add tabs to tabbed pane
		for (TabId tabId : TabId.values())
			tabPane.getTabs().add(tabId.createTab());

		// Select tab
		tabPane.getSelectionModel().select(selectedTabIndex);


		//----  Tab: appearance

		// Create procedure to select theme
		StyleManager styleManager = StyleManager.INSTANCE;
		IProcedure1<String> selectTheme = id ->
		{
			// Update theme
			styleManager.selectTheme(id);

			// Reapply style sheet to the scenes of all JavaFX windows
			styleManager.reapplyStylesheet();
		};

		// Spinner: theme
		String themeId = styleManager.getThemeId();
		CollectionSpinner<String> themeSpinner =
				CollectionSpinner.leftRightH(HPos.CENTER, true, styleManager.getThemeIds(), themeId, null,
											 id -> styleManager.findTheme(id).getName());
		themeSpinner.itemProperty().addListener((observable, oldId, id) -> selectTheme.invoke(id));

		// Pane: appearance
		HBox appearancePane = new HBox(CONTROL_H_GAP, new Label(THEME_STR), themeSpinner);
		appearancePane.setAlignment(Pos.CENTER);
		appearancePane.setPadding(CONTROL_PANE_PADDING);

		// Set content of tab
		getTab(TabId.APPEARANCE).setContent(appearancePane);


		//----  Tab: view

		// Pane: view
		GridPane viewPane = new GridPane();
		viewPane.setHgap(CONTROL_H_GAP);
		viewPane.setVgap(CONTROL_V_GAP);
		viewPane.setAlignment(Pos.CENTER);
		viewPane.setPadding(CONTROL_PANE_PADDING);

		// Initialise column constraints
		ColumnConstraints column = new ColumnConstraints();
		column.setMinWidth(GridPane.USE_PREF_SIZE);
		column.setHalignment(HPos.RIGHT);
		viewPane.getColumnConstraints().add(column);

		column = new ColumnConstraints();
		column.setHalignment(HPos.LEFT);
		viewPane.getColumnConstraints().add(column);

		// Initialise row index
		int row = 0;

		// Spinner: cell vertical padding
		Spinner<Integer> cellVerticalPaddingSpinner =
				SpinnerFactory.integerSpinner(MIN_CELL_VERTICAL_PADDING, MAX_CELL_VERTICAL_PADDING,
											  preferences.getCellVerticalPadding(), CELL_VERTICAL_PADDING_SPINNER_NUM_DIGITS);
		viewPane.addRow(row++, new Label(VERTICAL_PADDING_STR), cellVerticalPaddingSpinner);

		// Spinner: column-header pop-up delay
		Spinner<Integer> columnHeaderPopUpDelaySpinner =
				SpinnerFactory.integerSpinner(MIN_COLUMN_HEADER_POP_UP_DELAY, MAX_COLUMN_HEADER_POP_UP_DELAY,
											  preferences.getColumnHeaderPopUpDelay(),
											  COLUMN_HEADER_POP_UP_DELAY_SPINNER_NUM_DIGITS);

		// Pane: column-header pop-up delay
		HBox columnHeaderPopUpDelayPane = new HBox(4.0, columnHeaderPopUpDelaySpinner, new Label(MS_STR));
		columnHeaderPopUpDelayPane.setAlignment(Pos.CENTER_LEFT);
		viewPane.addRow(row++, new Label(COLUMN_HEADER_POP_UP_DELAY_STR), columnHeaderPopUpDelayPane);

		// Set content of tab
		getTab(TabId.VIEW).setContent(viewPane);


		//----  Tab: zip files

		// Create list view of zip filename suffixes
		SimpleTextListView<String> filenameSuffixListView =
				new SimpleTextListView<>(preferences.getZipFilenameSuffixes(), null);
		filenameSuffixListView.setPrefSize(FILENAME_EXT_LIST_VIEW_WIDTH, FILENAME_EXT_LIST_VIEW_HEIGHT);
		filenameSuffixListView.setMaxWidth(SimpleTextListView.USE_PREF_SIZE);

		// Create list-view editor for zip filename suffixes
		Window window = this;
		ListViewEditor<String> filenameSuffixListViewEditor =
				new ListViewEditor<>(filenameSuffixListView, new ListViewEditor.IEditor<>()
		{
			@Override
			public String edit(
				ListViewEditor.Action	action,
				String					suffix)
			{
				return SingleTextFieldDialog
							.show(window, "", action + " " + FILENAME_SUFFIX_EDIT_NAME, FILENAME_SUFFIX_EDIT_LABEL, suffix,
								  FilterFactory.createFilter((ch, index, text) ->
											Character.isWhitespace(ch) ? "" : Character.toString(ch)),
								  text -> !text.isEmpty() && !filenameSuffixListView.getItems().contains(text));
		}

			@Override
			public boolean hasDialog()
			{
				return true;
			}

			@Override
			public boolean isRemovable(
				String	suffix)
			{
				return !Constants.ZIP_FILENAME_EXTENSION.equals(suffix);
			}

			@Override
			public boolean canRemoveWithKeyPress()
			{
				return true;
			}

			@Override
			public boolean confirmRemove(
				String	suffix)
			{
				return ConfirmationDialog.show(window, REMOVE_FILENAME_SUFFIX_STR, MessageIcon32.QUESTION.get(),
											   String.format(REMOVE_FILENAME_SUFFIX_QUESTION_STR, suffix), REMOVE_STR);
			}
		},
		false);
		filenameSuffixListViewEditor.setAlignment(Pos.CENTER);
		filenameSuffixListViewEditor.setPadding(FILENAME_EXT_LIST_VIEW_EDITOR_PADDING);
		filenameSuffixListViewEditor.getButtonPane().setPadding(FILENAME_EXT_LIST_VIEW_EDITOR_BUTTON_PANE_PADDING);

		// Set content of tab
		getTab(TabId.ZIP_FILENAME_SUFFIXES).setContent(filenameSuffixListViewEditor);


		//----  Tab: file-system locations

		// Create pane: locations
		GridPane locationsPane = new GridPane();
		locationsPane.setHgap(CONTROL_H_GAP);
		locationsPane.setVgap(CONTROL_V_GAP);
		locationsPane.setAlignment(Pos.CENTER);
		locationsPane.setPadding(CONTROL_PANE_PADDING);

		// Initialise column constraints
		column = new ColumnConstraints();
		column.setMinWidth(GridPane.USE_PREF_SIZE);
		column.setHalignment(HPos.RIGHT);
		column.setHgrow(Priority.NEVER);
		locationsPane.getColumnConstraints().add(column);

		column = new ColumnConstraints();
		column.setHalignment(HPos.LEFT);
		column.setHgrow(Priority.NEVER);
		column.setFillWidth(false);
		locationsPane.getColumnConstraints().add(column);

		// Initialise row index
		row = 0;

		// Create directory chooser: default extraction directory
		LocationChooser defaultExtDirectoryChooser = LocationChooser.forDirectories();
		defaultExtDirectoryChooser.setDialogTitle(DEFAULT_EXTRACTION_DIR_STR);
		defaultExtDirectoryChooser.setDialogStateKey();

		// Create pathname field: default extraction directory
		PathnameField defaultExtDirectoryField =
				new PathnameField(preferences.getDefaultExtractionDirectory(), DIRECTORY_FIELD_NUM_COLUMNS);
		defaultExtDirectoryField.setShowInvalidPathnameError(true);

		// Create pathname pane: default extraction directory
		PathnamePane defaultExtDirectoryPane = new PathnamePane(defaultExtDirectoryField, event ->
		{
			// Set initial directory of directory chooser from content of directory field
			defaultExtDirectoryField.initChooser(defaultExtDirectoryChooser, DEFAULT_DIRECTORY);

			// Display directory-selection dialog
			Path directory = defaultExtDirectoryChooser.showSelectDialog(this);

			// Update directory field
			if (directory != null)
				defaultExtDirectoryField.setLocation(directory);
		});
		TooltipDecorator.addTooltip(defaultExtDirectoryPane.getButton(), CHOOSE_DEFAULT_EXTRACTION_DIR_STR);
		locationsPane.addRow(row++, new Label(DEFAULT_EXTRACTION_DIR_STR), defaultExtDirectoryPane);

		// Create directory chooser: file-editor extraction directory
		LocationChooser fileEditorExtDirectoryChooser = LocationChooser.forDirectories();
		fileEditorExtDirectoryChooser.setDialogTitle(FILE_EDITOR_EXTRACTION_DIR_STR);
		fileEditorExtDirectoryChooser.setDialogStateKey();

		// Create pathname field: file-editor extraction directory
		PathnameField fileEditorExtDirectoryField =
				new PathnameField(preferences.getFileEditorExtractionDirectory(), DIRECTORY_FIELD_NUM_COLUMNS);
		fileEditorExtDirectoryField.setShowInvalidPathnameError(true);

		// Create pathname pane: file-editor extraction directory
		PathnamePane fileEditorExtDirectoryPane = new PathnamePane(fileEditorExtDirectoryField, event ->
		{
			// Set initial directory of directory chooser from content of directory field
			fileEditorExtDirectoryField.initChooser(fileEditorExtDirectoryChooser, DEFAULT_DIRECTORY);

			// Display directory-selection dialog
			Path directory = fileEditorExtDirectoryChooser.showSelectDialog(this);

			// Update directory field
			if (directory != null)
				fileEditorExtDirectoryField.setLocation(directory);
		});
		TooltipDecorator.addTooltip(fileEditorExtDirectoryPane.getButton(), CHOOSE_FILE_EDITOR_EXTRACTION_DIR_STR);
		locationsPane.addRow(row++, new Label(FILE_EDITOR_EXTRACTION_DIR_STR), fileEditorExtDirectoryPane);

		// Set content of tab
		getTab(TabId.FILE_SYSTEM_LOCATIONS).setContent(locationsPane);


		//----  Tab: file editors

		// Create list view of file editors
		SimpleTextListView<FileEditor> fileEditorListView =
				new SimpleTextListView<>(preferences.getFileEditors(), editor -> editor.getName());
		fileEditorListView.setPrefSize(FILE_EDITOR_LIST_VIEW_WIDTH, FILE_EDITOR_LIST_VIEW_HEIGHT);
		fileEditorListView.setMaxWidth(SimpleTextListView.USE_PREF_SIZE);

		// Create list-view editor for file editors
		ListViewEditor<FileEditor> fileEditorListViewEditor =
				new ListViewEditor<>(fileEditorListView, new ListViewEditor.IEditor<>()
		{
			@Override
			public FileEditor edit(
				ListViewEditor.Action	action,
				FileEditor				editor)
			{
				return new FileEditorDialog(action + " " + FILE_EDITOR_STR, editor).showDialog();
			}

			@Override
			public boolean hasDialog()
			{
				return true;
			}

			@Override
			public boolean canRemoveWithKeyPress()
			{
				return true;
			}

			@Override
			public boolean confirmRemove(
				FileEditor	editor)
			{
				return ConfirmationDialog.show(window, REMOVE_EDITOR_STR, MessageIcon32.QUESTION.get(),
											   String.format(REMOVE_EDITOR_QUESTION_STR, editor.getName()), REMOVE_STR);
			}
		},
		false);
		fileEditorListViewEditor.setAlignment(Pos.CENTER);
		fileEditorListViewEditor.setPadding(FILE_EDITOR_LIST_VIEW_EDITOR_PADDING);
		fileEditorListViewEditor.getButtonPane().setPadding(FILE_EDITOR_LIST_VIEW_EDITOR_BUTTON_PANE_PADDING);

		// Set content of tab
		getTab(TabId.FILE_EDITORS).setContent(fileEditorListViewEditor);


		//----  Window

		// Create button: OK
		Button okButton = new Button(OK_STR);
		okButton.getProperties().put(BUTTON_GROUP_KEY, BUTTON_GROUP1);
		okButton.setOnAction(event ->
		{
			// Get pathname of default extraction directory
			String defaultExtDirectory = defaultExtDirectoryField.getText();
			if (StringUtils.isNullOrBlank(defaultExtDirectory))
			{
				// Switch to 'file-system locations' tab and request focus on 'default extraction directory' field
				tabPane.getSelectionModel().select(getTab(TabId.FILE_SYSTEM_LOCATIONS));
				defaultExtDirectoryField.requestFocus();
				defaultExtDirectoryField.end();

				// Display warning message
				WarningDialog.show(this, DEFAULT_EXTRACTION_DIR_STR, NO_DEFAULT_EXTRACTION_DIR_STR);
				return;
			}

			// Get pathname of file-editor extraction directory
			String fileEditorExtDirectory = fileEditorExtDirectoryField.getText();
			if (StringUtils.isNullOrBlank(fileEditorExtDirectory))
				fileEditorExtDirectory = null;

			// Set result
			result = new Preferences(
				themeSpinner.getItem(),
				cellVerticalPaddingSpinner.getValue(),
				columnHeaderPopUpDelaySpinner.getValue(),
				filenameSuffixListViewEditor.getItems(),
				defaultExtDirectory,
				fileEditorExtDirectory,
				fileEditorListViewEditor.getItems()
			);

			// Close dialog
			requestClose();
		});
		addButton(okButton, HPos.RIGHT);

		// Create button: cancel
		Button cancelButton = new Button(CANCEL_STR);
		cancelButton.getProperties().put(BUTTON_GROUP_KEY, BUTTON_GROUP1);
		cancelButton.setOnAction(event -> requestClose());
		addButton(cancelButton, HPos.RIGHT);

		// When window is closed, save index of selected tab and restore old theme
		setOnHiding(event ->
		{
			// Save index of selected tab
			selectedTabIndex = tabPane.getSelectionModel().getSelectedIndex();

			// If dialog was not accepted, restore old theme
			if ((result == null) && (themeId != null) && !themeId.equals(styleManager.getThemeId()))
				selectTheme.invoke(themeId);
		});

		// Apply new style sheet to scene
		applyStyleSheet();
	}

	//------------------------------------------------------------------

////////////////////////////////////////////////////////////////////////
//  Class methods
////////////////////////////////////////////////////////////////////////

	public static Preferences show(
		Window		owner,
		Preferences	preferences)
	{
		return new PreferencesDialog(owner, preferences).showDialog();
	}

	//------------------------------------------------------------------

	/**
	 * Returns the colour that is associated with the specified key in the colour map of the selected theme of the
	 * {@linkplain StyleManager style manager}.
	 *
	 * @param  key
	 *           the key of the desired colour.
	 * @return the colour that is associated with {@code key} in the colour map of the selected theme of the style
	 *         manager, or {@link StyleManager#DEFAULT_COLOUR} if there is no such colour.
	 */

	private static Color getColour(
		String	key)
	{
		return StyleManager.INSTANCE.getColourOrDefault(key);
	}

	//------------------------------------------------------------------

////////////////////////////////////////////////////////////////////////
//  Instance methods : overriding methods
////////////////////////////////////////////////////////////////////////

	@Override
	protected Preferences getResult()
	{
		return result;
	}

	//------------------------------------------------------------------

////////////////////////////////////////////////////////////////////////
//  Instance methods
////////////////////////////////////////////////////////////////////////

	private Tab getTab(
		TabId	tabId)
	{
		return tabPane.getTabs().get(tabId.ordinal());
	}

	//------------------------------------------------------------------

////////////////////////////////////////////////////////////////////////
//  Enumerated types
////////////////////////////////////////////////////////////////////////


	// ENUMERATION: TAB IDENTIFIER


	private enum TabId
	{

	////////////////////////////////////////////////////////////////////
	//  Constants
	////////////////////////////////////////////////////////////////////

		APPEARANCE
		(
			"Appearance"
		),

		VIEW
		(
			"View"
		),

		ZIP_FILENAME_SUFFIXES
		(
			"Zip filename suffixes"
		),

		FILE_SYSTEM_LOCATIONS
		(
			"File-system locations"
		),

		FILE_EDITORS
		(
			"File editors"
		);

	////////////////////////////////////////////////////////////////////
	//  Instance variables
	////////////////////////////////////////////////////////////////////

		private	String	text;

	////////////////////////////////////////////////////////////////////
	//  Constructors
	////////////////////////////////////////////////////////////////////

		private TabId(
			String	text)
		{
			this.text = text;
		}

		//--------------------------------------------------------------

	////////////////////////////////////////////////////////////////////
	//  Instance methods
	////////////////////////////////////////////////////////////////////

		private Tab createTab()
		{
			Tab tab = new Tab(text);
			tab.setClosable(false);
			return tab;
		}

		//--------------------------------------------------------------

	}

	//==================================================================

////////////////////////////////////////////////////////////////////////
//  Member classes : inner classes
////////////////////////////////////////////////////////////////////////


	// CLASS: FILE-EDITOR DIALOG


	private class FileEditorDialog
		extends SimpleModalDialog<FileEditor>
	{

	////////////////////////////////////////////////////////////////////
	//  Constants
	////////////////////////////////////////////////////////////////////

		private static final	int		NAME_FIELD_NUM_COLUMNS		= 24;
		private static final	int		COMMAND_FIELD_NUM_COLUMNS	= 48;

		private static final	double	LIST_VIEW_WIDTH		= 200.0;
		private static final	double	LIST_VIEW_HEIGHT	= 240.0;

		private static final	Insets	LIST_VIEW_EDITOR_BUTTON_PANE_PADDING	= new Insets(2.0, 6.0, 2.0, 0.0);

		private static final	String	EDIT_NAME	= "pattern";
		private static final	String	EDIT_LABEL	= "Filename pattern";

		private static final	String	NAME_STR				= "Name";
		private static final	String	COMMAND_STR				= "Command";
		private static final	String	FILENAME_PATTERNS_STR	= "Filename patterns";
		private static final	String	REMOVE_PATTERN_STR		= "Remove pattern";
		private static final	String	REMOVE_QUESTION_STR		= "Pattern: %s" + MessageDialog.MESSAGE_SEPARATOR
																	+ "Do you want to remove the selected pattern?";
		private static final	String	REMOVE_STR				= "Remove";

	////////////////////////////////////////////////////////////////////
	//  Instance variables
	////////////////////////////////////////////////////////////////////

		private	FileEditor	result;

	////////////////////////////////////////////////////////////////////
	//  Constructors
	////////////////////////////////////////////////////////////////////

		private FileEditorDialog(
			String		title,
			FileEditor	editor)
		{
			// Call superclass constructor
			super(PreferencesDialog.this, MethodHandles.lookup().lookupClass().getCanonicalName(), null, title);

			// Set properties
			setResizable(true);

			// Create control pane
			GridPane controlPane = new GridPane();
			controlPane.setHgap(CONTROL_H_GAP);
			controlPane.setVgap(CONTROL_V_GAP);
			controlPane.setAlignment(Pos.CENTER);

			// Initialise column constraints
			ColumnConstraints column1 = new ColumnConstraints();
			column1.setMinWidth(GridPane.USE_PREF_SIZE);
			column1.setHalignment(HPos.RIGHT);
			column1.setHgrow(Priority.NEVER);
			controlPane.getColumnConstraints().add(column1);

			ColumnConstraints column2 = new ColumnConstraints();
			column2.setHalignment(HPos.LEFT);
			column2.setHgrow(Priority.ALWAYS);
			column2.setFillWidth(false);
			controlPane.getColumnConstraints().add(column2);

			// Initialise row index
			int row = 0;

			// Create field: name
			TextField nameField = new TextField((editor == null) ? "" : editor.getName());
			nameField.setPrefColumnCount(NAME_FIELD_NUM_COLUMNS);
			controlPane.addRow(row++, new Label(NAME_STR), nameField);

			// Create field: command
			TextField commandField = new TextField((editor == null) ? "" : editor.getCommand());
			commandField.setPrefColumnCount(COMMAND_FIELD_NUM_COLUMNS);
			GridPane.setFillWidth(commandField, true);
			controlPane.addRow(row++, new Label(COMMAND_STR), commandField);

			// Create label: filename patterns
			Label filenamePatternsLabel = new Label(FILENAME_PATTERNS_STR);
			GridPane.setValignment(filenamePatternsLabel, VPos.TOP);
			GridPane.setMargin(filenamePatternsLabel, new Insets(4.0, 0.0, 0.0, 0.0));

			// Create list view for filename patterns
			SimpleTextListView<String> listView =
					new SimpleTextListView<>((editor == null) ? null : editor.getFilenamePatterns(), null);
			listView.setPrefSize(LIST_VIEW_WIDTH, LIST_VIEW_HEIGHT);

			// Create list-view editor for filename patterns
			Window window = this;
			ListViewEditor<String> listViewEditor = new ListViewEditor<>(listView, new ListViewEditor.IEditor<>()
			{
				@Override
				public String edit(
					ListViewEditor.Action	action,
					String					pattern)
				{
					return SingleTextFieldDialog.show(window, "", action + " " + EDIT_NAME, EDIT_LABEL, pattern, null,
													  text -> !listView.getItems().contains(text));
				}

				@Override
				public boolean hasDialog()
				{
					return true;
				}

				@Override
				public boolean canRemoveWithKeyPress()
				{
					return true;
				}

				@Override
				public boolean confirmRemove(
					String	pattern)
				{
					return ConfirmationDialog.show(window, REMOVE_PATTERN_STR, MessageIcon32.QUESTION.get(),
												   String.format(REMOVE_QUESTION_STR, pattern), REMOVE_STR);
				}
			},
			false);
			listViewEditor.getButtonPane().setPadding(LIST_VIEW_EDITOR_BUTTON_PANE_PADDING);
			GridPane.setVgrow(listViewEditor, Priority.ALWAYS);
			controlPane.addRow(row++, filenamePatternsLabel, listViewEditor);

			// Add control pane to content pane
			addContent(controlPane);

			// Create button: OK
			Button okButton = new Button(OK_STR);
			okButton.getProperties().put(BUTTON_GROUP_KEY, BUTTON_GROUP1);
			okButton.setOnAction(event ->
			{
				result = new FileEditor(nameField.getText(), commandField.getText(), listView.getItems());

				requestClose();
			});
			addButton(okButton, HPos.RIGHT);

			// Create procedure to update 'OK' button
			IProcedure0 updateOkButton = () ->
			{
				okButton.setDisable(StringUtils.isNullOrBlank(nameField.getText())
										|| StringUtils.isNullOrBlank(commandField.getText()));
			};

			// Update 'OK' button when content of text field changes
			nameField.textProperty().addListener(observable -> updateOkButton.invoke());
			commandField.textProperty().addListener(observable -> updateOkButton.invoke());

			// Update 'OK' button
			updateOkButton.invoke();

			// Create button: cancel
			Button cancelButton = new Button(CANCEL_STR);
			cancelButton.getProperties().put(BUTTON_GROUP_KEY, BUTTON_GROUP1);
			cancelButton.setOnAction(event -> requestClose());
			addButton(cancelButton, HPos.RIGHT);

			// Fire 'cancel' button if Escape key is pressed; fire 'OK' button if Ctrl+Enter is pressed
			setKeyFireButton(cancelButton, okButton);
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
