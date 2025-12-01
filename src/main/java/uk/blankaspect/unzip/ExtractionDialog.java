/*====================================================================*\

ExtractionDialog.java

Class: extraction dialog.

\*====================================================================*/


// PACKAGE


package uk.blankaspect.unzip;

//----------------------------------------------------------------------


// IMPORTS


import java.lang.invoke.MethodHandles;

import java.nio.file.Files;
import java.nio.file.InvalidPathException;
import java.nio.file.LinkOption;
import java.nio.file.Path;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Objects;

import java.util.function.Predicate;

import java.util.stream.Stream;

import javafx.geometry.Bounds;
import javafx.geometry.Dimension2D;
import javafx.geometry.HPos;
import javafx.geometry.Insets;
import javafx.geometry.Point2D;
import javafx.geometry.Pos;

import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.MenuItem;
import javafx.scene.control.RadioButton;
import javafx.scene.control.ToggleGroup;

import javafx.scene.input.TransferMode;

import javafx.scene.layout.Background;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;

import javafx.scene.paint.Color;

import javafx.stage.Window;

import uk.blankaspect.common.basictree.ListNode;
import uk.blankaspect.common.basictree.MapNode;
import uk.blankaspect.common.basictree.StringNode;

import uk.blankaspect.common.css.CssSelector;

import uk.blankaspect.common.exception2.UnexpectedRuntimeException;

import uk.blankaspect.common.filesystem.PathnameUtils;
import uk.blankaspect.common.filesystem.PathUtils;

import uk.blankaspect.common.function.IFunction0;
import uk.blankaspect.common.function.IProcedure0;
import uk.blankaspect.common.function.IProcedure1;

import uk.blankaspect.common.message.MessageConstants;

import uk.blankaspect.common.misc.SystemUtils;

import uk.blankaspect.common.os.OsUtils;

import uk.blankaspect.common.string.StringUtils;

import uk.blankaspect.ui.jfx.button.Buttons;
import uk.blankaspect.ui.jfx.button.ImageDataButton;

import uk.blankaspect.ui.jfx.clipboard.ClipboardUtils;

import uk.blankaspect.ui.jfx.combobox.SimpleComboBox;

import uk.blankaspect.ui.jfx.container.PathnamePane;

import uk.blankaspect.ui.jfx.dialog.ConfirmationDialog;
import uk.blankaspect.ui.jfx.dialog.SimpleModalDialog;

import uk.blankaspect.ui.jfx.image.MessageIcon24;
import uk.blankaspect.ui.jfx.image.MessageIcon32;

import uk.blankaspect.ui.jfx.locationchooser.DirectoryMatcher;
import uk.blankaspect.ui.jfx.locationchooser.LocationChooser;

import uk.blankaspect.ui.jfx.popup.MessagePopUp;

import uk.blankaspect.ui.jfx.scene.SceneUtils;

import uk.blankaspect.ui.jfx.style.ColourProperty;
import uk.blankaspect.ui.jfx.style.FxProperty;
import uk.blankaspect.ui.jfx.style.FxPseudoClass;
import uk.blankaspect.ui.jfx.style.StyleConstants;
import uk.blankaspect.ui.jfx.style.StyleManager;

import uk.blankaspect.ui.jfx.tableview.TableViewEditor;

import uk.blankaspect.ui.jfx.text.TextUtils;

import uk.blankaspect.ui.jfx.textfield.PathnameField;

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

	/** The factor by which the text height is multiplied to give the preferred width of a directory label. */
	private static final	double	DIRECTORY_LABEL_WIDTH_FACTOR	= 25.0;

	/** The margins around the <i>flatten</i> check box. */
	private static final	Insets	FLATTEN_CHECK_BOX_MARGINS	= new Insets(4.0, 0.0, 0.0, 0.0);

	/** The default initial directory of a file chooser. */
	private static final	Path	DEFAULT_DIRECTORY	= SystemUtils.userHomeDirectory();

	/** The file-system location filter for drag-and-drop actions. */
	private static final	Predicate<Path>	DRAG_AND_DROP_FILTER	= location ->
			Files.isDirectory(location, LinkOption.NOFOLLOW_LINKS);

	/** Miscellaneous strings. */
	private static final	String	ELLIPSIS_STR				= "...";
	private static final	String	EXTRACT_FILES_STR			= "Extract files";
	private static final	String	COPY_TO_EDITABLE_STR		= "Copy to editable field";
	private static final	String	SHOW_LIST_STR				= "Show list (Ctrl+Space in field)";
	private static final	String	FLATTEN_STR					= "Flatten";
	private static final	String	FILENAME_ONLY_STR			= "Filename only: omit parent directory";
	private static final	String	EXTRACT_STR					= "Extract";
	private static final	String	OUTPUT_DIRECTORY_STR		= "Output directory";
	private static final	String	NOT_A_VALID_PATHNAME_STR	= "'%s' is not a valid pathname.";
	private static final	String	CHOOSE_DIRECTORY_STR		= "Choose a directory";
	private static final	String	ADD_DIRECTORY_STR			= "Add directory to list";
	private static final	String	DIRECTORY_STR				= "directory";
	private static final	String	EDIT_DIRECTORIES_STR		= "Edit list of directories";

	/** CSS colour properties. */
	private static final	List<ColourProperty>	COLOUR_PROPERTIES	= List.of
	(
		ColourProperty.of
		(
			FxProperty.TEXT_FILL,
			ColourKey.DIRECTORY_LABEL_TEXT,
			CssSelector.builder()
					.cls(StyleClass.EXTRACTION_DIALOG_ROOT)
					.desc(StyleClass.DIRECTORY_LABEL)
					.build()
		),
		ColourProperty.of
		(
			FxProperty.BACKGROUND_COLOUR,
			ColourKey.DIRECTORY_LABEL_BACKGROUND,
			CssSelector.builder()
					.cls(StyleClass.EXTRACTION_DIALOG_ROOT)
					.desc(StyleClass.DIRECTORY_LABEL)
					.build()
		),
		ColourProperty.of
		(
			FxProperty.BACKGROUND_COLOUR,
			Color.TRANSPARENT,
			CssSelector.builder()
					.cls(StyleClass.EXTRACTION_DIALOG_ROOT)
					.desc(StyleClass.DIRECTORY_LABEL).pseudo(FxPseudoClass.DISABLED)
					.build()
		),
		ColourProperty.of
		(
			FxProperty.BORDER_COLOUR,
			ColourKey.DIRECTORY_LABEL_BORDER,
			CssSelector.builder()
					.cls(StyleClass.EXTRACTION_DIALOG_ROOT)
					.desc(StyleClass.DIRECTORY_LABEL)
					.build()
		),
		ColourProperty.of
		(
			FxProperty.BORDER_COLOUR,
			ColourKey.DIRECTORY_LABEL_BORDER_DISABLED,
			CssSelector.builder()
					.cls(StyleClass.EXTRACTION_DIALOG_ROOT)
					.desc(StyleClass.DIRECTORY_LABEL).pseudo(FxPseudoClass.DISABLED)
					.build()
		)
	);

	/** CSS style classes. */
	private interface StyleClass
	{
		String	EXTRACTION_DIALOG_ROOT	= StyleConstants.APP_CLASS_PREFIX + "extraction-dialog-root";

		String	DIRECTORY_LABEL	= StyleConstants.CLASS_PREFIX + "directory-label";
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

		// Set style class on root node of scene graph
		getScene().getRoot().getStyleClass().add(StyleClass.EXTRACTION_DIALOG_ROOT);

		// Get user preferences
		Preferences preferences = UnzipApp.instance().getPreferences();

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

		// Initialise column constraints
		ColumnConstraints column = new ColumnConstraints();
		column.setMinWidth(Region.USE_PREF_SIZE);
		column.setHalignment(HPos.RIGHT);
		column.setHgrow(Priority.NEVER);
		controlPane.getColumnConstraints().add(column);

		column = new ColumnConstraints();
		column.setHalignment(HPos.LEFT);
		column.setHgrow(Priority.ALWAYS);
		controlPane.getColumnConstraints().add(column);

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
					label.setBackground(SceneUtils
							.createColouredBackground(getColour(ColourKey.DIRECTORY_LABEL_BACKGROUND)));
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
		SimpleComboBox.IConverter<Directory> converter = new SimpleComboBox.IConverter<>()
		{
			@Override
			public String toText(
				Directory	directory)
			{
				return (directory == null) ? null : directory.pathname;
			}

			@Override
			public Directory fromText(
				String	pathname)
			{
				return StringUtils.isNullOrBlank(pathname) ? null : new Directory(pathname, false);
			}

			@Override
			public Directory copy(
				Directory	directory)
			{
				return (directory == null) ? null : directory.clone();
			}
		};
		SimpleComboBox<Directory> directoryComboBox = new SimpleComboBox<>(converter, state.directories)
		{
			@Override
			protected void onPaste(
				Runnable	doPaste)
			{
				// If system clipboard contains the location of a directory, set it on text field ...
				if (ClipboardUtils.locationMatches(DirectoryMatcher.ANY_DIRECTORY::matches))
				{
					// Get first location of a directory from system clipboard
					Path location = ClipboardUtils.firstMatchingLocation(DirectoryMatcher.ANY_DIRECTORY::matches);

					// Set absolute location on text field
					if (location != null)
						setTextAndCommit(PathUtils.absString(location));
				}

				// ... otherwise, paste text into text field
				else
					super.onPaste(doPaste);
			}
		};
		directoryComboBox.setCommitOnFocusLost(preferences.isComboBoxCommitOnFocusLost());
		directoryComboBox.setMaxWidth(Double.MAX_VALUE);
		directoryComboBox.getTextField().setPrefColumnCount(DIRECTORY_FIELD_NUM_COLUMNS);
		TooltipDecorator.addTooltip(directoryComboBox.getButton(), SHOW_LIST_STR);
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

					invalidPathnamePopUp =
							new MessagePopUp(MessageIcon24.ERROR, String.format(NOT_A_VALID_PATHNAME_STR, pathname));
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
		Button chooseDirectoryButton = Buttons.hNoShrink(ELLIPSIS_STR);
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
				directoryComboBox.setTextAndCommit(directory.toString());
		});
		TooltipDecorator.addTooltip(chooseDirectoryButton, CHOOSE_DIRECTORY_STR);
		HBox.setMargin(chooseDirectoryButton, new Insets(0.0, 2.0, 0.0, 6.0));

		// Create button: add directory to list
		ImageDataButton addDirectoryButton = new ImageDataButton(Images.ImageId.PLUS_SIGN, ADD_DIRECTORY_STR);

		// Create procedure to update 'add directory to list' button
		IProcedure0 updateAddDirectoryButton = () ->
				addDirectoryButton.setDisable(StringUtils.isNullOrBlank(directoryComboBox.getText()));

		// Handle action on 'add directory to list' button
		addDirectoryButton.setOnAction(event ->
		{
			// Get pathname from combo box
			String pathname = directoryComboBox.getText();

			// If there is text, create directory and add it to list of items of combo box
			if (!StringUtils.isNullOrBlank(pathname))
			{
				// Ask whether to save directory
				Boolean save = Utils.askSaveBeyondSession(this, ADD_DIRECTORY_STR, DIRECTORY_STR);

				// If not cancelled, add directory to list
				if (save != null)
				{
					Directory directory = new Directory(pathname, save);
					List<Directory> items = new ArrayList<>(directoryComboBox.getItems());
					int index = items.indexOf(directory);
					if (index < 0)
						items.add(directory);
					else
						items.set(index, directory);
					directoryComboBox.setItems(items);
					directoryComboBox.setValue(directory.clone());
				}
			}
		});

		// Update 'add directory to list' button when content of combo-box editor changes
		directoryComboBox.getTextField().textProperty().addListener(observable -> updateAddDirectoryButton.invoke());

		// Create button: edit list of directories
		ImageDataButton editDirectoriesButton = new ImageDataButton(Images.ImageId.PENCIL, EDIT_DIRECTORIES_STR);
		editDirectoriesButton.setOnAction(event ->
		{
			Directory directory = directoryComboBox.getValue();
			List<Directory> directories =
					new DirectoryListDialog(this, directoryChooser, directoryComboBox.getItems()).showDialog();
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

		// Update 'add directory to list' button
		updateAddDirectoryButton.invoke();

		// Create pane: editable directory
		HBox editableDirectoryPane = new HBox(2.0, directoryComboBox, chooseDirectoryButton, addDirectoryButton,
											  editDirectoriesButton);
		editableDirectoryPane.setAlignment(Pos.CENTER_LEFT);

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
			directoryComboBox.setTextAndCommit(pathname);
		};

		// Calculate preferred width of a directory label
		double directoryLabelWidth = TextUtils.textHeightCeil(DIRECTORY_LABEL_WIDTH_FACTOR);

		// Create label: default directory
		Label defaultDirectoryLabel = directoryLabelFactory.invoke();
		defaultDirectoryLabel.setPrefWidth(directoryLabelWidth);
		defaultDirectoryLabel.setMaxWidth(Double.MAX_VALUE);
		defaultDirectoryLabel.setText(preferences.getDefaultExtractionDirectory());
		HBox.setHgrow(defaultDirectoryLabel, Priority.ALWAYS);

		// Create button: copy default directory to editor of directory combo box
		ImageDataButton defaultDirectoryCopyButton =
				new ImageDataButton(Images.ImageId.COPY_TEXT_DOWN, COPY_TO_EDITABLE_STR);
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
		ImageDataButton sourceDirectoryCopyButton =
				new ImageDataButton(Images.ImageId.COPY_TEXT_DOWN, COPY_TO_EDITABLE_STR);
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
		extractButton = Buttons.hNoShrink(EXTRACT_STR);
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
		Button cancelButton = Buttons.hNoShrink(CANCEL_STR);
		cancelButton.getProperties().put(BUTTON_GROUP_KEY, BUTTON_GROUP1);
		cancelButton.setOnAction(event -> requestClose());
		addButton(cancelButton, HPos.RIGHT);

		// Fire 'cancel' button if Escape key is pressed; fire 'extract' button if Ctrl+Enter is pressed
		setKeyFireButton(cancelButton, extractButton);

		// Update images of image buttons
		ImageDataButton.updateButtons(getScene());

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
			List<String> pathnames = state.directories.stream().map(dir -> dir.pathname).toList();
			int index = OsUtils.isWindows() ? StringUtils.indexOfIgnoreCase(state.directory, pathnames)
											: pathnames.indexOf(state.directory);
			if (index < 0)
				directoryComboBox.setTextAndCommit(state.directory);
			else
				directoryComboBox.setValue(state.directories.get(index));
		}

		// Apply style sheet to scene
		applyStyleSheet();
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
	 * Returns the colour that is associated with the specified key in the colour map of the current theme of the
	 * {@linkplain StyleManager style manager}.
	 *
	 * @param  key
	 *           the key of the desired colour.
	 * @return the colour that is associated with {@code key} in the colour map of the current theme of the style
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
			button.setMinWidth(Region.USE_PREF_SIZE);
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


	// CLASS: DIRECTORY


	private static class Directory
		implements IPersistable, Cloneable
	{

	////////////////////////////////////////////////////////////////////
	//  Instance variables
	////////////////////////////////////////////////////////////////////

		private	String	pathname;
		private	boolean	persistent;

	////////////////////////////////////////////////////////////////////
	//  Constructors
	////////////////////////////////////////////////////////////////////

		private Directory(
			String	pathname,
			boolean	persistent)
		{
			// Initialise instance variables
			this.pathname = pathname;
			this.persistent = persistent;
		}

		//--------------------------------------------------------------

	////////////////////////////////////////////////////////////////////
	//  Instance methods : IPersistable interface
	////////////////////////////////////////////////////////////////////

		/**
		 * {@inheritDoc}
		 */

		@Override
		public String getText(
			int	index)
		{
			return switch (index)
			{
				case 0  -> pathname;
				default -> throw new UnexpectedRuntimeException();
			};
		}

		//--------------------------------------------------------------

		/**
		 * {@inheritDoc}
		 */

		@Override
		public boolean isPersistent()
		{
			return persistent;
		}

		//--------------------------------------------------------------

		/**
		 * {@inheritDoc}
		 */

		@Override
		public void setPersistent(
			boolean	persistent)
		{
			this.persistent = persistent;
		}

		//--------------------------------------------------------------

	////////////////////////////////////////////////////////////////////
	//  Instance methods : overriding methods
	////////////////////////////////////////////////////////////////////

		/**
		 * {@inheritDoc}
		 */

		@Override
		public boolean equals(
			Object	obj)
		{
			if (this == obj)
				return true;

			return (obj instanceof Directory other) && Objects.equals(pathname, other.pathname);
		}

		//--------------------------------------------------------------

		/**
		 * {@inheritDoc}
		 */

		@Override
		public int hashCode()
		{
			return Objects.hashCode(pathname);
		}

		//--------------------------------------------------------------

		/**
		 * {@inheritDoc}
		 */

		@Override
		public Directory clone()
		{
			try
			{
				return (Directory)super.clone();
			}
			catch (CloneNotSupportedException e)
			{
				throw new UnexpectedRuntimeException(e);
			}
		}

		//--------------------------------------------------------------

	}

	//==================================================================


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
		private List<Directory>	directories;
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
			if (directories.stream().anyMatch(directory -> directory.persistent))
			{
				// Encode directories
				ListNode directoriesNode = rootNode.addList(PropertyKey.DIRECTORIES);
				int index = -1;
				int numDirectories = directories.size();
				for (int i = 0; i < numDirectories; i++)
				{
					Directory directory = directories.get(i);
					if (directory.persistent)
					{
						if (i == directoryIndex)
							index = directoriesNode.getNumElements();
						directoriesNode.addString(Utils.normalisePathname(directory.pathname));
					}
				}

				// Encode directory index
				if (index >= 0)
					rootNode.addInt(PropertyKey.DIRECTORY_INDEX, index);
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
					directories.add(new Directory(Utils.denormalisePathname(node.getValue()), true));
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


	// CLASS: DIALOG FOR EDITING A LIST OF DIRECTORIES


	private static class DirectoryListDialog
		extends SimpleModalDialog<List<Directory>>
	{

	////////////////////////////////////////////////////////////////////
	//  Constants
	////////////////////////////////////////////////////////////////////

		private static final	double	DIRECTORY_COLUMN_WIDTH_FACTOR	= 25.0;

		private static final	double	TABLE_VIEW_HEIGHT	= 240.0;

		private static final	Insets	EDITOR_BUTTON_PANE_PADDING	= new Insets(2.0, 6.0, 2.0, 0.0);

		private static final	Insets	CONTENT_PANE_PADDING	= new Insets(2.0);

		private static final	String	EDIT_NAME	= "directory";

		private static final	String	DIRECTORY_STR			= "Directory";
		private static final	String	REMOVE_DIRECTORY_STR	= "Remove directory";
		private static final	String	REMOVE_QUESTION_STR		= "Directory: %s" + MessageConstants.LABEL_SEPARATOR
																	+ "Do you want to remove the selected directory?";
		private static final	String	REMOVE_STR				= "Remove";
		private static final	String	SAVE_SELECTED_STR		= "Save selected directories";
		private static final	String	DONT_SAVE_SELECTED_STR	= "Don't save selected directories";

	////////////////////////////////////////////////////////////////////
	//  Instance variables
	////////////////////////////////////////////////////////////////////

		private	List<Directory>	result;

	////////////////////////////////////////////////////////////////////
	//  Constructors
	////////////////////////////////////////////////////////////////////

		private DirectoryListDialog(
			Window			owner,
			LocationChooser	directoryChooser,
			List<Directory>	directories)
		{
			// Call superclass constructor
			super(owner, MethodHandles.lookup().lookupClass().getCanonicalName(), EDIT_DIRECTORIES_STR);

			// Set properties
			setResizable(true);

			// Create column information for table view
			List<PersistableItemTableView.ColumnInfo> columnInfos = List.of(
				new PersistableItemTableView.ColumnInfo(DIRECTORY_STR,
														TextUtils.textHeightCeil(DIRECTORY_COLUMN_WIDTH_FACTOR))
			);

			// Create table view
			PersistableItemTableView<Directory> tableView = new PersistableItemTableView<>(columnInfos);
			tableView.setPrefHeight(TABLE_VIEW_HEIGHT);
			tableView.setItems(directories.stream().map(directory -> directory.clone()).toList());
			tableView.setMenuItemFactory(save ->
			{
				MenuItem menuItem = new MenuItem(save ? SAVE_SELECTED_STR : DONT_SAVE_SELECTED_STR);
				Stream<Directory> selectedItems = tableView.getSelectionModel().getSelectedItems().stream();
				menuItem.setDisable(save ? selectedItems.allMatch(item -> item.persistent)
										 : selectedItems.noneMatch(item -> item.persistent));
				menuItem.setOnAction(event ->
				{
					// Update 'persistent' flag of selected directories
					for (Directory directory : tableView.getSelectionModel().getSelectedItems())
						directory.persistent = save;

					// Redraw table view
					tableView.refresh();
				});
				return menuItem;
			});

			// Create table-view editor
			Window window = this;
			TableViewEditor.IEditor<Directory> editor0 = new TableViewEditor.IEditor<>()
			{
				@Override
				public Directory edit(
					TableViewEditor.Action	action,
					Directory				target)
				{
					return new DirectoryDialog(window, action + " " + EDIT_NAME, directoryChooser, target).showDialog();
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
					Directory	directory)
				{
					return ConfirmationDialog.show(window, REMOVE_DIRECTORY_STR, MessageIcon32.QUESTION.get(),
												   String.format(REMOVE_QUESTION_STR, directory.pathname), REMOVE_STR);
				}
			};
			TableViewEditor<Directory> editor = new TableViewEditor<>(tableView, editor0, false)
			{
				@Override
				public List<Directory> getItems()
				{
					return tableView.getItemList();
				}
			};
			editor.getButtonPane().setPadding(EDITOR_BUTTON_PANE_PADDING);

			// Add list-view editor to content
			addContent(editor);

			// Adjust padding around content pane
			getContentPane().setPadding(CONTENT_PANE_PADDING);

			// Create button: OK
			Button okButton = Buttons.hNoShrink(OK_STR);
			okButton.getProperties().put(BUTTON_GROUP_KEY, BUTTON_GROUP1);
			okButton.setOnAction(event ->
			{
				result = tableView.getItems();
				requestClose();
			});
			addButton(okButton, HPos.RIGHT);

			// Create button: cancel
			Button cancelButton = Buttons.hNoShrink(CANCEL_STR);
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
		protected List<Directory> getResult()
		{
			return result;
		}

		//--------------------------------------------------------------

	}

	//==================================================================


	// CLASS: DIALOG FOR EDITING A DIRECTORY


	private static class DirectoryDialog
		extends SimpleModalDialog<Directory>
	{

	////////////////////////////////////////////////////////////////////
	//  Constants
	////////////////////////////////////////////////////////////////////

		/** The horizontal gap between adjacent columns of the control pane. */
		private static final	double	CONTROL_PANE_H_GAP	= 6.0;

		/** The vertical gap between adjacent rows of the control pane. */
		private static final	double	CONTROL_PANE_V_GAP	= 6.0;

		// Miscellaneous strings
		private static final	String	DIRECTORY_STR			= "Directory";
		private static final	String	SAVE_BEYOND_SESSION_STR	= "Save the directory beyond the current session";

	////////////////////////////////////////////////////////////////////
	//  Instance variables
	////////////////////////////////////////////////////////////////////

		/** The result of this dialog. */
		private	Directory	result;

	////////////////////////////////////////////////////////////////////
	//  Constructors
	////////////////////////////////////////////////////////////////////

		private DirectoryDialog(
			Window			owner,
			String			title,
			LocationChooser	directoryChooser,
			Directory		directory)
		{
			// Call superclass constructor
			super(owner, MethodHandles.lookup().lookupClass().getName(), null, title);

			// Create control pane
			GridPane controlPane = new GridPane();
			controlPane.setHgap(CONTROL_PANE_H_GAP);
			controlPane.setVgap(CONTROL_PANE_V_GAP);
			controlPane.setAlignment(Pos.CENTER);

			// Initialise column constraints
			ColumnConstraints column = new ColumnConstraints();
			column.setMinWidth(Region.USE_PREF_SIZE);
			column.setHalignment(HPos.RIGHT);
			column.setHgrow(Priority.NEVER);
			controlPane.getColumnConstraints().add(column);

			column = new ColumnConstraints();
			column.setHalignment(HPos.LEFT);
			column.setHgrow(Priority.ALWAYS);
			controlPane.getColumnConstraints().add(column);

			// Initialise row index
			int row = 0;

			// Create pathname field: directory
			PathnameField directoryField =
					new PathnameField((directory == null) ? null : directory.pathname, DIRECTORY_FIELD_NUM_COLUMNS);

			// Create pathname pane: directory
			PathnamePane directoryPane = new PathnamePane(directoryField, event ->
			{
				// Set initial directory of directory chooser from content of pathname field
				directoryField.setLocationMatcher(PathnameField.DIRECTORY_MATCHER);
				directoryField.initChooser(directoryChooser, DEFAULT_DIRECTORY);

				// Display directory-selection dialog
				Path dir = directoryChooser.showSelectDialog(this);

				// Update directory field
				if (dir != null)
					directoryField.setLocation(dir);
			});
			TooltipDecorator.addTooltip(directoryPane.getButton(), CHOOSE_DIRECTORY_STR);
			controlPane.addRow(row++, new Label(DIRECTORY_STR), directoryPane);

			// Create check box: persistent
			CheckBox persistentCheckBox = new CheckBox(SAVE_BEYOND_SESSION_STR);
			persistentCheckBox.setSelected((directory != null) && directory.persistent);
			GridPane.setMargin(persistentCheckBox, new Insets(2.0, 0.0, 2.0, 0.0));
			controlPane.add(persistentCheckBox, 1, row);

			// Add control pane to content pane
			addContent(controlPane);

			// Create button: OK
			Button okButton = Buttons.hNoShrink(OK_STR);
			okButton.getProperties().put(BUTTON_GROUP_KEY, BUTTON_GROUP1);
			okButton.setOnAction(event ->
			{
				result = new Directory(directoryField.getText(), persistentCheckBox.isSelected());
				requestClose();
			});
			addButton(okButton, HPos.RIGHT);

			// Create procedure to update 'OK' button
			IProcedure0 updateOkButton = () ->
					okButton.setDisable(StringUtils.isNullOrBlank(directoryField.getText()));

			// Update 'OK' button when directory changes
			directoryField.textProperty().addListener(observable -> updateOkButton.invoke());

			// Fire 'OK' button if Enter is pressed in directory field
			directoryField.setOnAction(event -> okButton.fire());

			// Update 'OK' button
			updateOkButton.invoke();

			// Create button: cancel
			Button cancelButton = Buttons.hNoShrink(CANCEL_STR);
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
		protected Directory getResult()
		{
			return result;
		}

		//--------------------------------------------------------------

	}

	//==================================================================

}

//----------------------------------------------------------------------
