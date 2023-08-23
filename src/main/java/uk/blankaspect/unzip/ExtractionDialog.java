/*====================================================================*\

ExtractionDialog.java

Class: extraction dialog.

\*====================================================================*/


// PACKAGE


package uk.blankaspect.unzip;

//----------------------------------------------------------------------


// IMPORTS


import java.io.File;

import java.lang.invoke.MethodHandles;

import java.nio.file.Files;
import java.nio.file.InvalidPathException;
import java.nio.file.LinkOption;
import java.nio.file.Path;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;

import java.util.function.Predicate;

import javafx.collections.FXCollections;

import javafx.geometry.Bounds;
import javafx.geometry.Dimension2D;
import javafx.geometry.HPos;
import javafx.geometry.Insets;
import javafx.geometry.Point2D;
import javafx.geometry.Pos;

import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.RadioButton;
import javafx.scene.control.ToggleGroup;

import javafx.scene.input.TransferMode;

import javafx.scene.layout.Background;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;

import javafx.scene.paint.Color;

import javafx.stage.Window;

import uk.blankaspect.common.basictree.ListNode;
import uk.blankaspect.common.basictree.MapNode;
import uk.blankaspect.common.basictree.StringNode;

import uk.blankaspect.common.css.CssSelector;

import uk.blankaspect.common.filesystem.PathnameUtils;

import uk.blankaspect.common.function.IFunction0;
import uk.blankaspect.common.function.IProcedure0;
import uk.blankaspect.common.function.IProcedure1;

import uk.blankaspect.common.string.StringUtils;

import uk.blankaspect.ui.jfx.button.ImageButton;

import uk.blankaspect.ui.jfx.clipboard.ClipboardUtils;

import uk.blankaspect.ui.jfx.combobox.SimpleComboBox;

import uk.blankaspect.ui.jfx.dialog.ConfirmationDialog;
import uk.blankaspect.ui.jfx.dialog.MessageDialog;
import uk.blankaspect.ui.jfx.dialog.PathnameFieldDialog;
import uk.blankaspect.ui.jfx.dialog.SimpleModalDialog;

import uk.blankaspect.ui.jfx.image.MessageIcon24;
import uk.blankaspect.ui.jfx.image.MessageIcon32;

import uk.blankaspect.ui.jfx.listview.ListViewEditor;
import uk.blankaspect.ui.jfx.listview.SimpleTextListView;

import uk.blankaspect.ui.jfx.locationchooser.LocationChooser;

import uk.blankaspect.ui.jfx.popup.MessagePopUp;

import uk.blankaspect.ui.jfx.scene.SceneUtils;

import uk.blankaspect.ui.jfx.style.ColourProperty;
import uk.blankaspect.ui.jfx.style.FxProperty;
import uk.blankaspect.ui.jfx.style.FxPseudoClass;
import uk.blankaspect.ui.jfx.style.StyleConstants;
import uk.blankaspect.ui.jfx.style.StyleManager;

import uk.blankaspect.ui.jfx.text.TextUtils;

import uk.blankaspect.ui.jfx.tooltip.TooltipDecorator;

import uk.blankaspect.ui.jfx.widtheq.RadioButtonWidthEqualiser;

import uk.blankaspect.ui.jfx.window.WindowState;
import uk.blankaspect.ui.jfx.window.WindowUtils;

//----------------------------------------------------------------------


// CLASS: EXTRACTION DIALOG


public class ExtractionDialog
	extends SimpleModalDialog<ExtractionDialog.Result>
{

////////////////////////////////////////////////////////////////////////
//  Constants
////////////////////////////////////////////////////////////////////////

	/** The horizontal gap between adjacent columns of the control pane. */
	private static final	double	CONTROL_PANE_H_GAP	= 6.0;

	/** The vertical gap between adjacent rows of the control pane. */
	private static final	double	CONTROL_PANE_V_GAP	= 6.0;

	/** The padding around the control pane. */
	private static final	Insets	CONTROL_PANE_PADDING	= new Insets(4.0, 0.0, 4.0, 4.0);

	/** The padding around the <i>choose directory</i> button. */
	private static final	Insets	CHOOSE_DIRECTORY_BUTTON_PADDING	= new Insets(2.0, 8.0, 2.0, 8.0);

	/** The padding around an output-directory label. */
	private static final	Insets	DIRECTORY_LABEL_PADDING	= new Insets(2.0, 6.0, 2.0, 6.0);

	/** The preferred number of columns of a directory field. */
	private static final	int		DIRECTORY_FIELD_NUM_COLUMNS	= 40;

	/** The margins around the <i>flatten</i> check box. */
	private static final	Insets	FLATTEN_CHECK_BOX_MARGINS	= new Insets(4.0, 0.0, 0.0, 0.0);

	/** The default initial directory of a file chooser. */
	private static final	Path	DEFAULT_DIRECTORY	= Path.of(System.getProperty("user.home", "."));

	/** The file-system location filter for drag-and-drop actions. */
	private static final	Predicate<Path>	DRAG_AND_DROP_FILTER	= location ->
			Files.isDirectory(location, LinkOption.NOFOLLOW_LINKS);

	/** Miscellaneous strings. */
	private static final	String	ELLIPSIS_STR				= "...";
	private static final	String	EXTRACT_FILES_STR			= "Extract files";
	private static final	String	COPY_TO_EDITABLE_STR		= "Copy to editable field";
	private static final	String	FLATTEN_STR					= "Flatten";
	private static final	String	FILENAME_ONLY_STR			= "Filename only: omit parent directory";
	private static final	String	EXTRACT_STR					= "Extract";
	private static final	String	OUTPUT_DIRECTORY_STR		= "Output directory";
	private static final	String	NOT_A_VALID_PATHNAME_STR	= "'%s' is not a valid pathname.";
	private static final	String	CHOOSE_DIRECTORY_STR		= "Choose directory";
	private static final	String	ADD_DIRECTORY_STR			= "Add directory to list";
	private static final	String	EDIT_DIRECTORIES_STR		= "Edit list of directories";

	/** CSS colour properties. */
	private static final	List<ColourProperty>	COLOUR_PROPERTIES	= List.of
	(
		ColourProperty.of
		(
			FxProperty.TEXT_FILL,
			ColourKey.DIRECTORY_LABEL_TEXT,
			CssSelector.builder()
						.cls(StyleClass.EXTRACTION_DIALOG)
						.desc(StyleClass.DIRECTORY_LABEL)
						.build()
		),
		ColourProperty.of
		(
			FxProperty.BACKGROUND_COLOUR,
			ColourKey.DIRECTORY_LABEL_BACKGROUND,
			CssSelector.builder()
						.cls(StyleClass.EXTRACTION_DIALOG)
						.desc(StyleClass.DIRECTORY_LABEL)
						.build()
		),
		ColourProperty.of
		(
			FxProperty.BACKGROUND_COLOUR,
			Color.TRANSPARENT,
			CssSelector.builder()
						.cls(StyleClass.EXTRACTION_DIALOG)
						.desc(StyleClass.DIRECTORY_LABEL).pseudo(FxPseudoClass.DISABLED)
						.build()
		),
		ColourProperty.of
		(
			FxProperty.BORDER_COLOUR,
			ColourKey.DIRECTORY_LABEL_BORDER,
			CssSelector.builder()
						.cls(StyleClass.EXTRACTION_DIALOG)
						.desc(StyleClass.DIRECTORY_LABEL)
						.build()
		),
		ColourProperty.of
		(
			FxProperty.BORDER_COLOUR,
			ColourKey.DIRECTORY_LABEL_BORDER_DISABLED,
			CssSelector.builder()
						.cls(StyleClass.EXTRACTION_DIALOG)
						.desc(StyleClass.DIRECTORY_LABEL).pseudo(FxPseudoClass.DISABLED)
						.build()
		)
	);

	/** CSS style classes. */
	private interface StyleClass
	{
		String	EXTRACTION_DIALOG	= StyleConstants.CLASS_PREFIX + "unzip-extraction-dialog";

		String	DIRECTORY_LABEL		= StyleConstants.CLASS_PREFIX + "directory-label";
	}

	/** Keys of colours that are used in colour properties. */
	private interface ColourKey
	{
		String	PREFIX	= StyleManager.colourKeyPrefix(MethodHandles.lookup().lookupClass().getEnclosingClass());

		String	DIRECTORY_LABEL_BACKGROUND		= PREFIX + "directoryLabel.background";
		String	DIRECTORY_LABEL_BORDER			= PREFIX + "directoryLabel.border";
		String	DIRECTORY_LABEL_BORDER_DISABLED	= PREFIX + "directoryLabel.border.disabled";
		String	DIRECTORY_LABEL_TEXT			= PREFIX + "directoryLabel.text";
	}

////////////////////////////////////////////////////////////////////////
//  Class variables
////////////////////////////////////////////////////////////////////////

	private static	State	state	= new State();

////////////////////////////////////////////////////////////////////////
//  Instance variables
////////////////////////////////////////////////////////////////////////

	private	Result			result;
	private	MessagePopUp	invalidPathnamePopUp;
	private	Button			extractButton;

////////////////////////////////////////////////////////////////////////
//  Static initialiser
////////////////////////////////////////////////////////////////////////

	static
	{
		// Register the style properties of this class with the style manager
		StyleManager.INSTANCE.register(ExtractionDialog.class, COLOUR_PROPERTIES);
	}

////////////////////////////////////////////////////////////////////////
//  Constructors
////////////////////////////////////////////////////////////////////////

	public ExtractionDialog(
		Window	owner,
		Path	sourceDirectory)
	{
		// Call superclass constructor
		super(owner, EXTRACT_FILES_STR, state.getLocator(), state.getSize());

		// Set properties
		setResizable(true);

		// Create equaliser for radio buttons
		RadioButtonWidthEqualiser rbwe = new RadioButtonWidthEqualiser();

		// Create control pane
		GridPane controlPane = new GridPane()
		{
			@Override
			protected double computePrefWidth(
				double	height)
			{
				// Update radio-button widths
				rbwe.updateWidths();

				// Call superclass method
				return super.computePrefWidth(height);
			}
		};
		controlPane.setHgap(CONTROL_PANE_H_GAP);
		controlPane.setVgap(CONTROL_PANE_V_GAP);
		controlPane.setAlignment(Pos.CENTER);
		controlPane.setPadding(CONTROL_PANE_PADDING);
		controlPane.getStyleClass().add(StyleClass.EXTRACTION_DIALOG);

		// Initialise column constraints
		ColumnConstraints column1 = new ColumnConstraints();
		column1.setMinWidth(GridPane.USE_PREF_SIZE);
		column1.setHalignment(HPos.RIGHT);
		column1.setHgrow(Priority.NEVER);
		controlPane.getColumnConstraints().add(column1);

		ColumnConstraints column2 = new ColumnConstraints();
		column2.setHalignment(HPos.LEFT);
		column2.setHgrow(Priority.ALWAYS);
		controlPane.getColumnConstraints().add(column2);

		// Initialise row index
		int row = 0;

		// Create radio buttons for kinds of output directory
		ToggleGroup toggleGroup = new ToggleGroup();
		EnumMap<DirectoryKind, RadioButton> directoryKindButtons = new EnumMap<>(DirectoryKind.class);
		for (DirectoryKind directoryKind : DirectoryKind.values())
			directoryKindButtons.put(directoryKind, directoryKind.createRadioButton(rbwe, toggleGroup));

		// Create factory for output-directory label
		IFunction0<Label> directoryLabelFactory = () ->
		{
			// Create label
			Label label = new Label();
			label.setPadding(DIRECTORY_LABEL_PADDING);
			label.setTextFill(getColour(ColourKey.DIRECTORY_LABEL_TEXT));
			label.getStyleClass().add(StyleClass.DIRECTORY_LABEL);

			// Create procedure to update label
			IProcedure0 updateLabel = () ->
			{
				if (label.isDisable())
				{
					label.setBackground(Background.EMPTY);
					label.setBorder(SceneUtils.createSolidBorder(getColour(ColourKey.DIRECTORY_LABEL_BORDER_DISABLED)));
				}
				else
				{
					label.setBackground(SceneUtils.createColouredBackground(getColour(ColourKey.DIRECTORY_LABEL_BACKGROUND)));
					label.setBorder(SceneUtils.createSolidBorder(getColour(ColourKey.DIRECTORY_LABEL_BORDER)));
				}
			};

			// Update label when its 'disabled' state changes
			label.disabledProperty().addListener(observable -> updateLabel.invoke());

			// Update label
			updateLabel.invoke();

			// Return label
			return label;
		};

		// Create combo box for directory
		SimpleComboBox<String> directoryComboBox =
				new SimpleComboBox<>(SimpleComboBox.IDENTITY_STRING_CONVERTER, state.directories);
		directoryComboBox.setMaxWidth(Double.MAX_VALUE);
		directoryComboBox.getTextField().setPrefColumnCount(DIRECTORY_FIELD_NUM_COLUMNS);
		HBox.setHgrow(directoryComboBox, Priority.ALWAYS);

		// Create procedure to hide 'invalid pathname' pop-up
		IProcedure0 hideInvalidPathnamePopUp = () ->
		{
			if (invalidPathnamePopUp != null)
			{
				invalidPathnamePopUp.hide();
				invalidPathnamePopUp = null;
			}
		};

		// Create function to return location of directory from content of editor of directory combo box
		IFunction0<Path> getEditableDirectory = () ->
		{
			Path directory = null;
			String text = directoryComboBox.getText();
			if (!StringUtils.isNullOrBlank(text))
			{
				String pathname = PathnameUtils.parsePathname(text);
				try
				{
					directory = Path.of(pathname);
				}
				catch (InvalidPathException e)
				{
					hideInvalidPathnamePopUp.invoke();

					invalidPathnamePopUp = new MessagePopUp(MessageIcon24.ERROR,
															String.format(NOT_A_VALID_PATHNAME_STR, pathname));
					Bounds bounds = directoryComboBox.localToScreen(directoryComboBox.getLayoutBounds());
					invalidPathnamePopUp.show(this, bounds.getMinX(), bounds.getMaxY() - 1.0);
				}
			}
			return directory;
		};

		// Create directory chooser
		LocationChooser directoryChooser = LocationChooser.forDirectories();
		directoryChooser.setDialogTitle(OUTPUT_DIRECTORY_STR);
		directoryChooser.setDialogStateKey();
		directoryChooser.initDirectory(DEFAULT_DIRECTORY);

		// Create button: choose directory
		Button chooseDirectoryButton = new Button(ELLIPSIS_STR);
		chooseDirectoryButton.setPadding(CHOOSE_DIRECTORY_BUTTON_PADDING);
		chooseDirectoryButton.prefHeightProperty().bind(directoryComboBox.heightProperty());
		chooseDirectoryButton.setOnAction(event ->
		{
			// Set initial directory of directory chooser from text of directory combo box
			Path directory = getEditableDirectory.invoke();
			if (directory != null)
			{
				directoryChooser.initDirectoryWithParent(directory);
				directoryChooser.setInitialFilename(directory.getFileName().toString());
			}

			// Display directory-selection dialog
			directory = directoryChooser.showSelectDialog(this);

			// Update directory combo box
			if (directory != null)
				directoryComboBox.setText(directory.toString());
		});
		TooltipDecorator.addTooltip(chooseDirectoryButton, CHOOSE_DIRECTORY_STR);
		HBox.setMargin(chooseDirectoryButton, new Insets(0.0, 2.0, 0.0, 6.0));

		// Create button: add directory to list
		ImageButton addDirectoryButton = new ImageButton(Images.PLUS_SIGN, ADD_DIRECTORY_STR);
		addDirectoryButton.setOnAction(event ->
		{
			String text = directoryComboBox.getText();
			if (!StringUtils.isNullOrBlank(text))
			{
				List<String> items = new ArrayList<>(directoryComboBox.getItems());
				items.remove(text);
				items.add(0, text);
				directoryComboBox.setItems(items);
				directoryComboBox.setValue(text);
			}
		});

		// Create button: edit list of directories
		ImageButton editDirectoriesButton = new ImageButton(Images.PENCIL, EDIT_DIRECTORIES_STR);
		editDirectoriesButton.setOnAction(event ->
		{
			String directory = directoryComboBox.getValue();
			List<String> directories =
					new EditDirectoriesDialog(this, directoryChooser, directoryComboBox.getItems()).showDialog();
			if (directories != null)
			{
				directoryComboBox.setItems(directories);
				if (!directories.isEmpty())
				{
					int index = (directory == null) ? -1 : directories.indexOf(directory);
					if (index < 0)
						directoryComboBox.setValue(null);
					else
						directoryComboBox.selectIndex(index);
				}
			}
		});

		// Create pane: editable directory
		HBox editableDirectoryPane = new HBox(2.0, directoryComboBox, chooseDirectoryButton, addDirectoryButton,
											  editDirectoriesButton);
		editableDirectoryPane.setAlignment(Pos.CENTER_LEFT);

		// Get user preferences
		Preferences preferences = UnzipApp.instance().getPreferences();

		// Create function to return directory from selected control
		IFunction0<Path> getDirectory = () ->
		{
			Path directory = switch ((DirectoryKind)toggleGroup.getSelectedToggle().getUserData())
			{
				case DEFAULT  ->
				{
					String pathname = preferences.getDefaultExtractionDirectory();
					yield StringUtils.isNullOrBlank(pathname) ? null : Path.of(pathname);
				}
				case SOURCE   -> sourceDirectory;
				case EDITABLE -> getEditableDirectory.invoke();
			};
			return directory;
		};

		// Create procedure to update 'extract' button
		IProcedure0 updateExtractButton = () -> extractButton.setDisable(getDirectory.invoke() == null);

		// Hide 'invalid pathname' pop-up and update 'extract' button when directory is selected in combo box
		directoryComboBox.valueProperty().addListener(observable ->
		{
			hideInvalidPathnamePopUp.invoke();
			updateExtractButton.invoke();
		});

		// Hide 'invalid pathname' pop-up and update 'extract' button when content of text field of directory combo box
		// changes
		directoryComboBox.getTextField().textProperty().addListener(observable ->
		{
			hideInvalidPathnamePopUp.invoke();
			updateExtractButton.invoke();
		});

		// Create procedure to set pathname on editor of directory combo box
		IProcedure1<String> setDirectory = pathname ->
		{
			directoryKindButtons.get(DirectoryKind.EDITABLE).setSelected(true);
			directoryComboBox.setText(pathname);
		};

		// Calculate preferred width of a directory label
		double directoryLabelWidth = TextUtils.textWidthCeil("M".repeat(DIRECTORY_FIELD_NUM_COLUMNS));

		// Create label: default directory
		Label defaultDirectoryLabel = directoryLabelFactory.invoke();
		defaultDirectoryLabel.setPrefWidth(directoryLabelWidth);
		defaultDirectoryLabel.setMaxWidth(Double.MAX_VALUE);
		defaultDirectoryLabel.setText(preferences.getDefaultExtractionDirectory());
		HBox.setHgrow(defaultDirectoryLabel, Priority.ALWAYS);

		// Create button: copy default directory to editor of directory combo box
		ImageButton defaultDirectoryCopyButton = new ImageButton(Images.COPY_TEXT_DOWN, COPY_TO_EDITABLE_STR);
		defaultDirectoryCopyButton.setOnAction(event ->
		{
			// Get pathname of default directory
			String pathname = defaultDirectoryLabel.getText();

			// Set pathname of directory on editor of directory combo box
			if (pathname != null)
				setDirectory.invoke(pathname);
		});

		// Create pane: default directory
		HBox defaultDirectoryPane = new HBox(4.0, defaultDirectoryLabel, defaultDirectoryCopyButton);
		defaultDirectoryPane.setAlignment(Pos.CENTER_LEFT);

		// Create label: source directory
		Label sourceDirectoryLabel = directoryLabelFactory.invoke();
		sourceDirectoryLabel.setPrefWidth(directoryLabelWidth);
		sourceDirectoryLabel.setMaxWidth(Double.MAX_VALUE);
		if (sourceDirectory != null)
			sourceDirectoryLabel.setText(sourceDirectory.toString());
		HBox.setHgrow(sourceDirectoryLabel, Priority.ALWAYS);

		// Create button: copy source directory to editor of directory combo box
		ImageButton sourceDirectoryCopyButton = new ImageButton(Images.COPY_TEXT_DOWN, COPY_TO_EDITABLE_STR);
		sourceDirectoryCopyButton.setOnAction(event ->
		{
			// Get pathname of source directory
			String pathname = sourceDirectoryLabel.getText();

			// Set pathname of directory on editor of directory combo box
			if (pathname != null)
				setDirectory.invoke(pathname);
		});

		// Create pane: source directory
		HBox sourceDirectoryPane = new HBox(4.0, sourceDirectoryLabel, sourceDirectoryCopyButton);
		sourceDirectoryPane.setAlignment(Pos.CENTER_LEFT);

		// Add directory panes to control pane
		for (DirectoryKind directoryKind : directoryKindButtons.keySet())
		{
			HBox pane = switch (directoryKind)
			{
				case DEFAULT  -> defaultDirectoryPane;
				case SOURCE   -> sourceDirectoryPane;
				case EDITABLE -> editableDirectoryPane;
			};
			controlPane.addRow(row++, directoryKindButtons.get(directoryKind), pane);
		}

		// Check box: flatten
		CheckBox flattenCheckBox = new CheckBox(FLATTEN_STR);
		flattenCheckBox.setSelected(state.flatten);
		GridPane.setMargin(flattenCheckBox, FLATTEN_CHECK_BOX_MARGINS);
		TooltipDecorator.addTooltip(flattenCheckBox, FILENAME_ONLY_STR);
		controlPane.add(flattenCheckBox, 1, row++);

		// Add control pane to content pane
		addContent(controlPane);

		// Create button: extract
		extractButton = new Button(EXTRACT_STR);
		extractButton.getProperties().put(BUTTON_GROUP_KEY, BUTTON_GROUP1);
		extractButton.setOnAction(event ->
		{
			Path directory = getDirectory.invoke();
			if (directory != null)
			{
				result = new Result(directory, flattenCheckBox.isSelected());
				requestClose();
			}
		});
		addButton(extractButton, HPos.RIGHT);

		// Enable/disable directory-related components when selected radio button changes
		toggleGroup.selectedToggleProperty().addListener((observable, oldToggle, toggle) ->
		{
			// Enable/disable directory-related components
			if (toggle != null)
			{
				switch ((DirectoryKind)toggle.getUserData())
				{
					case DEFAULT:
						defaultDirectoryLabel.setDisable(false);
						sourceDirectoryLabel.setDisable(true);
						editableDirectoryPane.setDisable(true);
						break;

					case SOURCE:
						defaultDirectoryLabel.setDisable(true);
						sourceDirectoryLabel.setDisable(false);
						editableDirectoryPane.setDisable(true);
						break;

					case EDITABLE:
						defaultDirectoryLabel.setDisable(true);
						sourceDirectoryLabel.setDisable(true);
						editableDirectoryPane.setDisable(false);
						break;
				}
			}

			// Update 'extract' button
			updateExtractButton.invoke();
		});
		toggleGroup.selectToggle(directoryKindButtons.get(state.directoryKind));

		// Update 'extract' button
		updateExtractButton.invoke();

		// Create button: cancel
		Button cancelButton = new Button(CANCEL_STR);
		cancelButton.getProperties().put(BUTTON_GROUP_KEY, BUTTON_GROUP1);
		cancelButton.setOnAction(event -> requestClose());
		addButton(cancelButton, HPos.RIGHT);

		// Fire 'cancel' button if Escape key is pressed; fire 'extract' button if Ctrl+Enter is pressed
		setKeyFireButton(cancelButton, extractButton);

		// Update images of image buttons
		Images.updateImageButtons(getScene());

		// Set drag-and-drop handler to accept a directory
		getScene().setOnDragOver(event ->
		{
			// Accept drag if dragboard contains a directory
			if (ClipboardUtils.locationMatches(event.getDragboard(), DRAG_AND_DROP_FILTER))
				event.acceptTransferModes(TransferMode.COPY);

			// Consume event
			event.consume();
		});

		// Set drag-and-drop handler to set pathname of directory on editor of directory combo box
		getScene().setOnDragDropped(event ->
		{
			// Get location of first directory from dragboard
			Path directory = ClipboardUtils.firstMatchingLocation(event.getDragboard(), DRAG_AND_DROP_FILTER);

			// Indicate that drag-and-drop is complete
			event.setDropCompleted(true);

			// Set pathname of directory on editor of directory combo box
			if (directory != null)
				setDirectory.invoke(directory.toString());

			// Consume event
			event.consume();
		});

		// When dialog is shown, prevent its height from changing
		setOnShown(event -> WindowUtils.preventHeightChange(this));

		// Save dialog state when dialog is closed
		setOnHiding(event ->
		{
			state.restoreAndUpdate(this, true);
			state.directoryKind = (DirectoryKind)toggleGroup.getSelectedToggle().getUserData();
			state.directory = directoryComboBox.getText();
			state.directories.clear();
			state.directories.addAll(directoryComboBox.getItems());
			state.directoryIndex = directoryComboBox.getValueIndex();
			state.flatten = flattenCheckBox.isSelected();
		});

		// If directory kind is 'editable', request focus on directory combo box
		if (directoryKindButtons.get(DirectoryKind.EDITABLE).isSelected())
			directoryComboBox.requestFocus();

		// Process saved directory
		if (!StringUtils.isNullOrEmpty(state.directory))
		{
			int index = (File.separatorChar == '\\') ? StringUtils.indexOfIgnoreCase(state.directory, state.directories)
													 : state.directories.indexOf(state.directory);
			if (index < 0)
				directoryComboBox.setText(state.directory);
			else
				directoryComboBox.setValue(state.directories.get(index));
		}
	}

	//------------------------------------------------------------------

////////////////////////////////////////////////////////////////////////
//  Class methods
////////////////////////////////////////////////////////////////////////

	public static MapNode encodeState()
	{
		return state.encodeTree();
	}

	//------------------------------------------------------------------

	public static void decodeState(
		MapNode	mapNode)
	{
		state.decodeTree(mapNode);
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
	protected Result getResult()
	{
		return result;
	}

	//------------------------------------------------------------------

////////////////////////////////////////////////////////////////////////
//  Enumerated types
////////////////////////////////////////////////////////////////////////


	// ENUMERATION: DIRECTORY KIND


	private enum DirectoryKind
	{

	////////////////////////////////////////////////////////////////////
	//  Constants
	////////////////////////////////////////////////////////////////////

		DEFAULT
		(
			"Default"
		),

		SOURCE
		(
			"Source"
		),

		EDITABLE
		(
			"Editable"
		);

	////////////////////////////////////////////////////////////////////
	//  Instance variables
	////////////////////////////////////////////////////////////////////

		private	String	text;

	////////////////////////////////////////////////////////////////////
	//  Constructors
	////////////////////////////////////////////////////////////////////

		private DirectoryKind(
			String	text)
		{
			// Initialise instance variables
			this.text = text;
		}

		//--------------------------------------------------------------

	////////////////////////////////////////////////////////////////////
	//  Instance methods : overriding methods
	////////////////////////////////////////////////////////////////////

		@Override
		public String toString()
		{
			return text;
		}

		//--------------------------------------------------------------

	////////////////////////////////////////////////////////////////////
	//  Instance methods
	////////////////////////////////////////////////////////////////////

		public RadioButton createRadioButton(
			RadioButtonWidthEqualiser	widthEqualiser,
			ToggleGroup					toggleGroup)
		{
			RadioButton button = widthEqualiser.createRadioButton(" " + text);
			button.setMinWidth(RadioButton.USE_PREF_SIZE);
			button.setToggleGroup(toggleGroup);
			button.setUserData(this);
			return button;
		}

		//--------------------------------------------------------------

	}

	//==================================================================

////////////////////////////////////////////////////////////////////////
//  Member records
////////////////////////////////////////////////////////////////////////


	// RECORD: RESULT


	public record Result(
		Path	directory,
		boolean	flatten)
	{ }

	//==================================================================

////////////////////////////////////////////////////////////////////////
//  Member classes : non-inner classes
////////////////////////////////////////////////////////////////////////


	// CLASS: STATE


	private static class State
		extends WindowState
	{

	////////////////////////////////////////////////////////////////////
	//  Constants
	////////////////////////////////////////////////////////////////////

		/** Keys of properties. */
		private interface PropertyKey
		{
			String	DIRECTORIES			= "directories";
			String	DIRECTORY_INDEX		= "directoryIndex";
		}

	////////////////////////////////////////////////////////////////////
	//  Instance variables
	////////////////////////////////////////////////////////////////////

		private DirectoryKind	directoryKind;
		private	String			directory;
		private List<String>	directories;
		private int				directoryIndex;
		private	boolean			flatten;

	////////////////////////////////////////////////////////////////////
	//  Constructors
	////////////////////////////////////////////////////////////////////

		private State()
		{
			// Call superclass constructor
			super(true, true);

			// Initialise instance variables
			directoryKind = DirectoryKind.DEFAULT;
			directories = new ArrayList<>();
			directoryIndex = -1;
		}

		//--------------------------------------------------------------

	////////////////////////////////////////////////////////////////////
	//  Instance methods : overriding methods
	////////////////////////////////////////////////////////////////////

		/**
		 * {@inheritDoc}
		 */

		@Override
		public Dimension2D getSize()
		{
			Dimension2D size = super.getSize();
			return (size == null) ? null : new Dimension2D(size.getWidth(), 0.0);
		}

		//--------------------------------------------------------------

		/**
		 * {@inheritDoc}
		 */

		@Override
		public MapNode encodeTree()
		{
			// Call superclass method
			MapNode rootNode = super.encodeTree();

			// Encode directories and directory index
			if (!directories.isEmpty())
			{
				// Encode directories
				ListNode directoriesNode = rootNode.addList(PropertyKey.DIRECTORIES);
				for (String directory : directories)
					directoriesNode.addString(directory.replace(File.separatorChar, '/'));

				// Encode directory index
				if (directoryIndex >= 0)
					rootNode.addInt(PropertyKey.DIRECTORY_INDEX, directoryIndex);
			}

			// Return root node
			return rootNode;
		}

		//--------------------------------------------------------------

		/**
		 * {@inheritDoc}
		 */

		@Override
		public void decodeTree(
			MapNode	rootNode)
		{
			// Call superclass method
			super.decodeTree(rootNode);

			// Decode directories
			String key = PropertyKey.DIRECTORIES;
			if (rootNode.hasList(key))
			{
				directories.clear();
				for (StringNode node : rootNode.getListNode(key).stringNodes())
					directories.add(node.getValue().replace('/', File.separatorChar));
			}

			// Decode directory index
			directoryIndex = rootNode.getInt(PropertyKey.DIRECTORY_INDEX, -1);
		}

		//--------------------------------------------------------------

	////////////////////////////////////////////////////////////////////
	//  Instance methods
	////////////////////////////////////////////////////////////////////

		/**
		 * Returns a locator function that returns the location from this dialog state.
		 *
		 * @return a locator function that returns the location from this dialog state, or {@code null} if the
		 *         location is {@code null}.
		 */

		private ILocator getLocator()
		{
			Point2D location = getLocation();
			return (location == null) ? null : (width, height) -> location;
		}

		//--------------------------------------------------------------

	}

	//==================================================================


	// CLASS: 'EDIT DIRECTORIES' DIALOG


	private static class EditDirectoriesDialog
		extends SimpleModalDialog<List<String>>
	{

	////////////////////////////////////////////////////////////////////
	//  Constants
	////////////////////////////////////////////////////////////////////

		private static final	double	LIST_VIEW_WIDTH		= 520.0;
		private static final	double	LIST_VIEW_HEIGHT	= 240.0;

		private static final	Insets	EDITOR_BUTTON_PANE_PADDING	= new Insets(2.0, 6.0, 2.0, 0.0);

		private static final	Insets	CONTENT_PANE_PADDING	= new Insets(2.0);

		private static final	String	EDIT_NAME	= "directory";
		private static final	String	EDIT_LABEL	= "Directory";

		private static final	String	REMOVE_DIRECTORY_STR	= "Remove directory";
		private static final	String	REMOVE_QUESTION_STR		= "Directory: %s" + MessageDialog.MESSAGE_SEPARATOR
																	+ "Do you want to remove the selected directory?";
		private static final	String	REMOVE_STR				= "Remove";

	////////////////////////////////////////////////////////////////////
	//  Instance variables
	////////////////////////////////////////////////////////////////////

		private	List<String>	result;

	////////////////////////////////////////////////////////////////////
	//  Constructors
	////////////////////////////////////////////////////////////////////

		private EditDirectoriesDialog(
			Window			owner,
			LocationChooser	directoryChooser,
			List<String>	directories)
		{
			// Call superclass constructor
			super(owner, MethodHandles.lookup().lookupClass().getCanonicalName(), EDIT_DIRECTORIES_STR);

			// Set properties
			setResizable(true);

			// Create list view
			SimpleTextListView<String> listView =
					new SimpleTextListView<>(FXCollections.observableArrayList(directories), null);
			listView.setPrefSize(LIST_VIEW_WIDTH, LIST_VIEW_HEIGHT);

			// Create list-view editor
			Window window = this;
			ListViewEditor<String> editor = new ListViewEditor<>(listView, new ListViewEditor.IEditor<>()
			{
				@Override
				public String edit(
					ListViewEditor.Action	action,
					String					pathname)
				{
					PathnameFieldDialog dialog =
							new PathnameFieldDialog(window, MethodHandles.lookup().lookupClass().getCanonicalName(),
													action + " " + EDIT_NAME, EDIT_LABEL, pathname,
													text -> !listView.getItems().contains(text));
					TooltipDecorator.addTooltip(dialog.getButton(), CHOOSE_DIRECTORY_STR);
					dialog.getButton().setOnAction(event ->
					{
						// Set initial directory of directory chooser from content of pathname field
						dialog.getPathnameField().initChooser(directoryChooser, DEFAULT_DIRECTORY);

						// Display directory-selection dialog
						Path directory = directoryChooser.showSelectDialog(window);

						// Update pathname field
						if (directory != null)
							dialog.getPathnameField().setLocation(directory);
					});
					return dialog.showDialog();
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
					String	pathname)
				{
					return ConfirmationDialog.show(window, REMOVE_DIRECTORY_STR, MessageIcon32.QUESTION.get(),
												   String.format(REMOVE_QUESTION_STR, pathname), REMOVE_STR);
				}
			},
			false);
			editor.getButtonPane().setPadding(EDITOR_BUTTON_PANE_PADDING);

			// Add list-view editor to content
			addContent(editor);

			// Adjust padding around content pane
			getContentPane().setPadding(CONTENT_PANE_PADDING);

			// Create button: OK
			Button okButton = new Button(OK_STR);
			okButton.getProperties().put(BUTTON_GROUP_KEY, BUTTON_GROUP1);
			okButton.setOnAction(event ->
			{
				result = listView.getItems();
				requestClose();
			});
			addButton(okButton, HPos.RIGHT);

			// Create button: cancel
			Button cancelButton = new Button(CANCEL_STR);
			cancelButton.getProperties().put(BUTTON_GROUP_KEY, BUTTON_GROUP1);
			cancelButton.setOnAction(event -> requestClose());
			addButton(cancelButton, HPos.RIGHT);

			// Fire 'cancel' button if Escape key is pressed; fire 'OK' button if Ctrl+Enter is pressed
			setKeyFireButton(cancelButton, okButton);

			// Apply style sheet to scene
			applyStyleSheet();
		}

		//--------------------------------------------------------------

	////////////////////////////////////////////////////////////////////
	//  Instance methods : overriding methods
	////////////////////////////////////////////////////////////////////

		@Override
		protected List<String> getResult()
		{
			return result;
		}

		//--------------------------------------------------------------

	}

	//==================================================================

}

//----------------------------------------------------------------------
