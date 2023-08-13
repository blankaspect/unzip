/*====================================================================*\

ComparisonDialog.java

Class: zip-file comparison dialog.

\*====================================================================*/


// PACKAGE


package uk.blankaspect.unzip;

//----------------------------------------------------------------------


// IMPORTS


import java.lang.invoke.MethodHandles;

import java.nio.file.Path;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.EnumMap;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import java.util.function.Predicate;

import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

import java.util.stream.Collectors;

import javafx.application.Platform;

import javafx.beans.InvalidationListener;

import javafx.beans.property.SimpleSetProperty;

import javafx.collections.FXCollections;

import javafx.geometry.Bounds;
import javafx.geometry.Dimension2D;
import javafx.geometry.HPos;
import javafx.geometry.Insets;
import javafx.geometry.Point2D;
import javafx.geometry.Pos;
import javafx.geometry.Side;
import javafx.geometry.VPos;

import javafx.scene.Group;
import javafx.scene.Node;

import javafx.scene.control.Button;
import javafx.scene.control.ContentDisplay;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TextField;

import javafx.scene.image.ImageView;

import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.TransferMode;

import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;

import javafx.scene.paint.Color;

import javafx.scene.shape.Arc;
import javafx.scene.shape.ArcType;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Rectangle;
import javafx.scene.shape.StrokeType;

import javafx.scene.text.Font;
import javafx.scene.text.TextBoundsType;

import javafx.stage.Window;

import javafx.util.StringConverter;

import uk.blankaspect.common.basictree.ListNode;
import uk.blankaspect.common.basictree.MapNode;

import uk.blankaspect.common.css.CssRuleSet;
import uk.blankaspect.common.css.CssSelector;

import uk.blankaspect.common.exception2.BaseException;

import uk.blankaspect.common.function.IFunction0;
import uk.blankaspect.common.function.IFunction1;
import uk.blankaspect.common.function.IProcedure0;

import uk.blankaspect.common.geometry.VHPos;

import uk.blankaspect.common.namefilter.LocationFilter;
import uk.blankaspect.common.namefilter.PatternKind;

import uk.blankaspect.common.string.StringUtils;

import uk.blankaspect.ui.jfx.button.ImageButton;

import uk.blankaspect.ui.jfx.clipboard.ClipboardUtils;

import uk.blankaspect.ui.jfx.combobox.SimpleComboBox;

import uk.blankaspect.ui.jfx.container.PathnamePane;

import uk.blankaspect.ui.jfx.dialog.ConfirmationDialog;
import uk.blankaspect.ui.jfx.dialog.ErrorDialog;
import uk.blankaspect.ui.jfx.dialog.MessageDialog;
import uk.blankaspect.ui.jfx.dialog.NotificationDialog;
import uk.blankaspect.ui.jfx.dialog.SimpleModalDialog;

import uk.blankaspect.ui.jfx.font.FontUtils;

import uk.blankaspect.ui.jfx.image.ImageUtils;
import uk.blankaspect.ui.jfx.image.MessageIcon32;

import uk.blankaspect.ui.jfx.label.CheckLabel;

import uk.blankaspect.ui.jfx.listview.ListViewEditor;
import uk.blankaspect.ui.jfx.listview.ListViewStyle;
import uk.blankaspect.ui.jfx.listview.SimpleTextListView;

import uk.blankaspect.ui.jfx.locationchooser.FileMatcher;
import uk.blankaspect.ui.jfx.locationchooser.LocationChooser;

import uk.blankaspect.ui.jfx.popup.CellPopUpManager;
import uk.blankaspect.ui.jfx.popup.PopUpUtils;

import uk.blankaspect.ui.jfx.scene.SceneUtils;

import uk.blankaspect.ui.jfx.spinner.CollectionSpinner;

import uk.blankaspect.ui.jfx.style.ColourProperty;
import uk.blankaspect.ui.jfx.style.FxProperty;
import uk.blankaspect.ui.jfx.style.FxPseudoClass;
import uk.blankaspect.ui.jfx.style.RuleSetFactory;
import uk.blankaspect.ui.jfx.style.StyleConstants;
import uk.blankaspect.ui.jfx.style.StyleManager;
import uk.blankaspect.ui.jfx.style.StyleSelector;
import uk.blankaspect.ui.jfx.style.StyleUtils;

import uk.blankaspect.ui.jfx.tableview.SimpleTableView;

import uk.blankaspect.ui.jfx.text.Text2;
import uk.blankaspect.ui.jfx.text.TextUtils;

import uk.blankaspect.ui.jfx.textfield.PathnameField;

import uk.blankaspect.ui.jfx.tooltip.TooltipDecorator;

import uk.blankaspect.ui.jfx.window.WindowState;

//----------------------------------------------------------------------


// CLASS: ZIP-FILE COMPARISON DIALOG


public class ComparisonDialog
	extends SimpleModalDialog<Void>
{

////////////////////////////////////////////////////////////////////////
//  Constants
////////////////////////////////////////////////////////////////////////

	/** The horizontal gap between adjacent controls. */
	private static final	double	CONTROL_H_GAP	= 6.0;

	/** The vertical gap between adjacent controls. */
	private static final	double	CONTROL_V_GAP	= 6.0;

	private static final	int		PARAM_SET_FIELD_NUM_COLUMNS	= 32;

	private static final	Insets	CHECK_LABEL_PADDING	= new Insets(1.0, 6.0, 1.0, 1.0);

	/** The default initial directory of a file chooser. */
	private static final	Path	DEFAULT_DIRECTORY	= Path.of(System.getProperty("user.dir", "."));

	/** Miscellaneous strings. */
	private static final	String	COMPARE_WITH_FILE_STR		= "Compare current file with chosen file";
	private static final	String	COMPARE_FILES_STR			= "Compare files";
	private static final	String	CHOOSE_FILE_STR				= "Choose file for comparison";
	private static final	String	FILE_STR					= "File";
	private static final	String	PARAM_SET_STR				= "Parameter set";
	private static final	String	ADD_PARAM_SET_STR			= "Add parameter set to list";
	private static final	String	EDIT_PARAM_SETS_STR			= "Edit list of parameter sets";
	private static final	String	UPDATE_PARAM_SET_STR		= "Update parameter set";
	private static final	String	ASK_UPDATE_PARAM_SET_STR	= "The list already contains a parameter set named '%s'.\n"
																	+ "Do you want to update it with the current parameters?";
	private static final	String	UPDATE_STR					= "Update";
	private static final	String	FILTERS_STR					= "Filters";
	private static final	String	REMOVE_STR					= "Remove";
	private static final	String	FILTER_STR					= "filter";
	private static final	String	REMOVE_FILTER_STR			= "Remove %s";
	private static final	String	REMOVE_FILTER_QUESTION_STR	= "Do you want to remove the selected %s?";
	private static final	String	FIELDS_STR					= "Fields";
	private static final	String	COMPARE_STR					= "Compare";
	private static final	String	NO_DIFFERENCES_STR			= "There are no differences between the selected fields\n"
																	+ "of the zip entries.";

	/** CSS colour properties. */
	private static final	List<ColourProperty>	COLOUR_PROPERTIES	= List.of
	(
		ColourProperty.of
		(
			FxProperty.STROKE,
			ColourKey.CHECK_LABEL_TICK_BOX,
			CssSelector.builder()
						.cls(StyleClass.COMPARISON_DIALOG)
						.desc(CheckLabel.StyleClass.CHECK_LABEL)
						.desc(CheckLabel.StyleClass.TICK_BOX)
						.build()
		),
		ColourProperty.of
		(
			FxProperty.FILL,
			ColourKey.PATTERN_KIND_ICON_FILL_GLOB,
			CssSelector.builder()
						.cls(StyleClass.FILTER_LIST_VIEW)
						.desc(StyleClass.PATTERN_KIND_ICON_GLOB)
						.build()
		),
		ColourProperty.of
		(
			FxProperty.FILL,
			ColourKey.PATTERN_KIND_ICON_FILL_REGEX,
			CssSelector.builder()
						.cls(StyleClass.FILTER_LIST_VIEW)
						.desc(StyleClass.PATTERN_KIND_ICON_REGEX)
						.build()
		),
		ColourProperty.of
		(
			FxProperty.STROKE,
			ColourKey.PATTERN_KIND_ICON_STROKE,
			CssSelector.builder()
						.cls(StyleClass.FILTER_LIST_VIEW)
						.desc(StyleClass.PATTERN_KIND_ICON)
						.build()
		),
		ColourProperty.of
		(
			FxProperty.STROKE,
			ColourKey.PATTERN_KIND_ICON_STROKE_GLOB,
			CssSelector.builder()
						.cls(StyleClass.FILTER_LIST_VIEW)
						.desc(StyleClass.PATTERN_KIND_ICON_GLOB)
						.build()
		),
		ColourProperty.of
		(
			FxProperty.STROKE,
			ColourKey.PATTERN_KIND_ICON_STROKE_REGEX,
			CssSelector.builder()
						.cls(StyleClass.FILTER_LIST_VIEW)
						.desc(StyleClass.PATTERN_KIND_ICON_REGEX)
						.build()
		),
		ColourProperty.of
		(
			FxProperty.TEXT_FILL,
			ColourKey.PATTERN_KIND_ICON_TEXT,
			CssSelector.builder()
						.cls(StyleClass.FILTER_LIST_VIEW)
						.desc(StyleClass.PATTERN_KIND_ICON_TEXT)
						.build()
		),
		ColourProperty.of
		(
			FxProperty.TEXT_FILL,
			ColourKey.FILTER_LIST_VIEW_PLACEHOLDER_TEXT,
			CssSelector.builder()
						.cls(StyleClass.FILTER_LIST_VIEW)
						.desc(StyleClass.PLACEHOLDER_LABEL)
						.build()
		),
		ColourProperty.of
		(
			FxProperty.TEXT_FILL,
			ColourKey.RESULT_DIALOG_DIFFERENCE_TEXT,
			CssSelector.builder()
						.cls(ListViewStyle.StyleClass.CELL_LABEL)
						.desc(StyleClass.RESULT_DIALOG_DIFFERENCE)
						.build()
		)
	);

	/** CSS rule sets. */
	private static final	List<CssRuleSet>	RULE_SETS	= List.of
	(
		RuleSetFactory.doubleSolidBorder
		(
			CssSelector.builder()
						.cls(StyleClass.COMPARISON_DIALOG)
						.desc(CheckLabel.StyleClass.CHECK_LABEL)
						.build(),
			Color.TRANSPARENT,
			ColourKey.CHECK_LABEL_BORDER
		),
		RuleSetFactory.outerFocusBorder
		(
			CssSelector.builder()
						.cls(StyleClass.COMPARISON_DIALOG)
						.desc(CheckLabel.StyleClass.CHECK_LABEL).pseudo(FxPseudoClass.FOCUSED)
						.build(),
			ColourKey.CHECK_LABEL_BORDER
		)
	);

	/** CSS style classes. */
	private interface StyleClass
	{
		String	COMPARISON_DIALOG			= StyleConstants.CLASS_PREFIX + "unzip-comparison-dialog";
		String	FILTER_LIST_VIEW			= COMPARISON_DIALOG + "-filter-list-view";
		String	PATTERN_KIND_ICON			= StyleConstants.CLASS_PREFIX + "pattern-kind-icon";
		String	PATTERN_KIND_ICON_GLOB		= StyleConstants.CLASS_PREFIX + "pattern-kind-icon-glob";
		String	PATTERN_KIND_ICON_REGEX		= StyleConstants.CLASS_PREFIX + "pattern-kind-icon-regex";
		String	PATTERN_KIND_ICON_TEXT		= StyleConstants.CLASS_PREFIX + "pattern-kind-icon-text";
		String	PLACEHOLDER_LABEL			= StyleConstants.CLASS_PREFIX + "placeholder-label";
		String	RESULT_DIALOG_DIFFERENCE	= COMPARISON_DIALOG + "-result-dialog-difference";
	}

	/** Keys of colours that are used in colour properties. */
	private interface ColourKey
	{
		String	CHECK_LABEL_BORDER					= "comparisonDialog.checkLabel.border";
		String	CHECK_LABEL_TICK_BOX				= "comparisonDialog.checkLabel.tickBox";
		String	FILTER_LIST_VIEW_PLACEHOLDER_TEXT	= "comparisonDialog.filterListView.placeholder.text";
		String	PATTERN_KIND_ICON_FILL_GLOB			= "comparisonDialog.patternKindIcon.fill.glob";
		String	PATTERN_KIND_ICON_FILL_REGEX		= "comparisonDialog.patternKindIcon.fill.regex";
		String	PATTERN_KIND_ICON_STROKE			= "comparisonDialog.patternKindIcon.stroke";
		String	PATTERN_KIND_ICON_STROKE_GLOB		= "comparisonDialog.patternKindIcon.stroke.glob";
		String	PATTERN_KIND_ICON_STROKE_REGEX		= "comparisonDialog.patternKindIcon.stroke.regex";
		String	PATTERN_KIND_ICON_TEXT				= "comparisonDialog.patternKindIcon.text";
		String	RESULT_DIALOG_DIFFERENCE_TEXT		= "comparisonDialog.resultDialog.difference.text";
	}

////////////////////////////////////////////////////////////////////////
//  Class variables
////////////////////////////////////////////////////////////////////////

	private static	State	state	= new State();

////////////////////////////////////////////////////////////////////////
//  Instance variables
////////////////////////////////////////////////////////////////////////

	private	FilterListView								filterListView;
	private	Map<ZipFileComparison.Field, CheckLabel>	fieldCheckLabels;

////////////////////////////////////////////////////////////////////////
//  Static initialiser
////////////////////////////////////////////////////////////////////////

	static
	{
		// Register the style properties of this class and its dependencies with the style manager
		StyleManager.INSTANCE.register(ComparisonDialog.class, COLOUR_PROPERTIES, RULE_SETS);
	}

////////////////////////////////////////////////////////////////////////
//  Constructors
////////////////////////////////////////////////////////////////////////

	public ComparisonDialog(
		Window			owner,
		ZipFileModel	zipFile)
	{
		// Call superclass constructor
		super(owner, COMPARE_WITH_FILE_STR, state.getLocator(), state.getSize());

		// Set properties
		setResizable(true);

		// Create control pane
		GridPane controlPane = new GridPane();
		controlPane.setHgap(CONTROL_H_GAP);
		controlPane.setVgap(CONTROL_V_GAP);
		controlPane.setAlignment(Pos.CENTER);
		controlPane.getStyleClass().add(StyleClass.COMPARISON_DIALOG);

		// Initialise column constraints
		ColumnConstraints column = new ColumnConstraints();
		column.setMinWidth(GridPane.USE_PREF_SIZE);
		column.setHalignment(HPos.RIGHT);
		column.setHgrow(Priority.NEVER);
		controlPane.getColumnConstraints().add(column);

		column = new ColumnConstraints();
		column.setHalignment(HPos.LEFT);
		controlPane.getColumnConstraints().add(column);

		// Initialise row index
		int row = 0;

		// File chooser: comparand file
		LocationChooser comparandFileChooser = LocationChooser.forFiles();
		comparandFileChooser.setDialogTitle(CHOOSE_FILE_STR);
		comparandFileChooser.setDialogStateKey();
		comparandFileChooser.addFilters(UnzipApp.instance().getPreferences().getZipFileFilter(), FileMatcher.ALL_FILES);
		comparandFileChooser.setInitialFilter(0);

		// Pathname field: comparand file
		PathnameField comparandFileField = new PathnameField();
		comparandFileField.setShowInvalidPathnameError(true);

		// Pathname pane: comparand file
		PathnamePane comparandFilePane = new PathnamePane(comparandFileField, event ->
		{
			// Set initial directory and filename of file chooser
			comparandFileField.initChooser(comparandFileChooser, state.directory, DEFAULT_DIRECTORY);

			// Display file chooser
			Path file = comparandFileChooser.showSelectDialog(this);

			// Update pathname field
			if (file != null)
			{
				// Update state
				state.directory = file.toAbsolutePath().getParent();

				// Update pathname field
				comparandFileField.setLocation(file);
			}
		});
		TooltipDecorator.addTooltip(comparandFilePane.getButton(), CHOOSE_FILE_STR);
		controlPane.addRow(row++, new Label(FILE_STR), comparandFilePane);

		// Combo box: parameter set
		SimpleComboBox<ComparisonParams> paramSetComboBox = new SimpleComboBox<>(new StringConverter<>()
		{
			@Override
			public String toString(
				ComparisonParams	paramSet)
			{
				return (paramSet == null) ? null : paramSet.getName();
			}

			@Override
			public ComparisonParams fromString(
				String	name)
			{
				return StringUtils.isNullOrBlank(name) ? null : new ComparisonParams(name);
			}
		},
		state.paramSets);
		paramSetComboBox.setMaxWidth(Double.MAX_VALUE);
		paramSetComboBox.getTextField().setPrefColumnCount(PARAM_SET_FIELD_NUM_COLUMNS);
		HBox.setHgrow(paramSetComboBox, Priority.ALWAYS);

		// Create function to return index of named parameter set
		IFunction1<Integer, String> findParamSet = name ->
		{
			for (int i = 0; i < paramSetComboBox.getItems().size(); i++)
			{
				if (name.equals(paramSetComboBox.getItems().get(i).getName()))
					return i;
			}
			return -1;
		};

		// Create function to return parameter set from UI components
		IFunction0<ComparisonParams> getParamSet = () ->
		{
			String name = paramSetComboBox.getText();
			if (name == null)
				name = "";
			List<LocationFilter> filters = filterListView.getItems();
			Set<ZipFileComparison.Field> fields =
					fieldCheckLabels.entrySet().stream()
												.filter(entry -> entry.getValue().isSelected())
												.map(entry -> entry.getKey())
												.collect(Collectors.toSet());
			return new ComparisonParams(name, filters, fields);
		};

		// Create button: add parameter set to list
		ImageButton addParamSetButton = new ImageButton(Images.PLUS_SIGN, ADD_PARAM_SET_STR);

		// Create procedure to update 'add parameter set to list' button
		IProcedure0 updateAddParamSetButton = () ->
				addParamSetButton.setDisable(StringUtils.isNullOrBlank(paramSetComboBox.getText()));

		// Create button: edit list of parameter sets
		ImageButton editParamSetsButton = new ImageButton(Images.PENCIL, EDIT_PARAM_SETS_STR);

		// Create procedure to update 'edit list of parameter sets' button
		IProcedure0 updateEditParamSetsButton = () ->
				editParamSetsButton.setDisable(paramSetComboBox.getItems().isEmpty());

		// Handle action on 'add parameter set to list' button
		addParamSetButton.setOnAction(event ->
		{
			String name = paramSetComboBox.getText();
			if (!StringUtils.isNullOrBlank(name))
			{
				List<ComparisonParams> items = new ArrayList<>(paramSetComboBox.getItems());
				int index = findParamSet.invoke(name);
				if (index < 0)
				{
					ComparisonParams paramSet = getParamSet.invoke();
					items.add(0, paramSet);
					paramSetComboBox.setItems(items);
					paramSetComboBox.setValue(paramSet);
					updateEditParamSetsButton.invoke();
				}
				else if (ConfirmationDialog.show(this, UPDATE_PARAM_SET_STR, MessageIcon32.QUESTION.get(),
												 String.format(ASK_UPDATE_PARAM_SET_STR, name), UPDATE_STR))
				{
					ComparisonParams paramSet = getParamSet.invoke();
					items.set(index, paramSet);
					paramSetComboBox.setItems(items);
					paramSetComboBox.setValue(paramSet);
					updateEditParamSetsButton.invoke();
				}
			}
		});
		HBox.setMargin(addParamSetButton, new Insets(0.0, 0.0, 0.0, 4.0));

		// Update 'add parameter set to list' button when content of combo-box editor changes
		paramSetComboBox.getTextField().textProperty().addListener(observable -> updateAddParamSetButton.invoke());

		// Handle action on 'edit list of parameter sets' button
		editParamSetsButton.setOnAction(event ->
		{
			ComparisonParams paramSet = paramSetComboBox.getValue();
			List<ComparisonParams> paramSets = new EditParamSetsDialog(this, paramSetComboBox.getItems()).showDialog();
			if (paramSets != null)
			{
				paramSetComboBox.setItems(paramSets);
				if (!paramSets.isEmpty())
				{
					int index = (paramSet == null) ? -1 : findParamSet.invoke(paramSet.getName());
					if (index < 0)
						paramSetComboBox.setValue(null);
					else
						paramSetComboBox.selectIndex(index);
				}
				updateEditParamSetsButton.invoke();
			}
		});

		// Update 'add parameter set to list' button
		updateAddParamSetButton.invoke();

		// Update 'edit list of parameter sets' button
		updateEditParamSetsButton.invoke();

		// Pane: parameter set
		HBox paramSetPane = new HBox(2.0, paramSetComboBox, addParamSetButton, editParamSetsButton);
		paramSetPane.setAlignment(Pos.CENTER_LEFT);
		controlPane.addRow(row++, new Label(PARAM_SET_STR), paramSetPane);

		// Label: filters
		Label filtersLabel = new Label(FILTERS_STR);
		GridPane.setValignment(filtersLabel, VPos.TOP);
		GridPane.setMargin(filtersLabel, new Insets(4.0, 0.0, 0.0, 0.0));

		// Create filter list view
		filterListView = new FilterListView();

		// Create filter list editor
		ListViewEditor<LocationFilter> filterListEditor =
				new ListViewEditor<>(filterListView, new ListViewEditor.IEditor<>()
		{
			@Override
			public LocationFilter edit(
				ListViewEditor.Action	action,
				LocationFilter			filter)
			{
				return new FilterDialog(ComparisonDialog.this, action + " " + FILTER_STR, filter).showDialog();
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
				LocationFilter	filter)
			{
				return ConfirmationDialog.show(ComparisonDialog.this, String.format(REMOVE_FILTER_STR, FILTER_STR),
											   MessageIcon32.QUESTION.get(),
											   String.format(REMOVE_FILTER_QUESTION_STR, FILTER_STR), REMOVE_STR);
			}
		},
		false);
		GridPane.setVgrow(filterListEditor, Priority.ALWAYS);
		controlPane.addRow(row++, filtersLabel, filterListEditor);

		// Label: fields
		Label fieldsLabel = new Label(FIELDS_STR);
		GridPane.setValignment(fieldsLabel, VPos.TOP);
		GridPane.setMargin(fieldsLabel, new Insets(3.0, 0.0, 0.0, 0.0));

		// Create check labels for comparison fields
		SimpleSetProperty<ZipFileComparison.Field> selectedFields =
				new SimpleSetProperty<>(FXCollections.observableSet(EnumSet.noneOf(ZipFileComparison.Field.class)));
		fieldCheckLabels = new EnumMap<>(ZipFileComparison.Field.class);
		for (ZipFileComparison.Field field : ZipFileComparison.Field.values())
		{
			CheckLabel checkLabel = new CheckLabel(field.toString());
			checkLabel.setMaxWidth(Double.MAX_VALUE);
			checkLabel.setPadding(CHECK_LABEL_PADDING);
			checkLabel.setBorderColour(getColour(ColourKey.CHECK_LABEL_BORDER));
			checkLabel.setDimIfUnselected(true);
			checkLabel.selectedProperty().addListener((observable, oldSelected, selected) ->
			{
				if (selected)
					selectedFields.add(field);
				else
					selectedFields.remove(field);
			});
			fieldCheckLabels.put(field, checkLabel);
		}

		// Pane: fields
		VBox fieldPane = new VBox();
		fieldPane.setMaxWidth(VBox.USE_PREF_SIZE);
		fieldPane.getChildren().addAll(fieldCheckLabels.values());
		controlPane.addRow(row++, fieldsLabel, fieldPane);

		// Create procedure to update parameter set
		IProcedure0 updateParamSet = () ->
		{
			ComparisonParams paramSet = paramSetComboBox.getValue();
			if (paramSet != null)
			{
				// Set filters on list view
				filterListView.setItems(FXCollections.observableArrayList(paramSet.getFilters()));

				// Update check labels for comparison fields
				for (ZipFileComparison.Field field : fieldCheckLabels.keySet())
					fieldCheckLabels.get(field).setSelected(paramSet.getFields().contains(field));
			}
		};

		// Update parameters when parameter set is selected in combo box
		paramSetComboBox.valueProperty().addListener(observable -> updateParamSet.invoke());

		// Update parameter set
		updateParamSet.invoke();

		// Add control pane to content pane
		addContent(controlPane);

		// Create button: compare
		Button compareButton = new Button(COMPARE_STR);
		compareButton.getProperties().put(BUTTON_GROUP_KEY, BUTTON_GROUP1);
		compareButton.setOnAction(event ->
		{
			Path file = comparandFileField.getLocation();
			ComparisonParams paramSet = getParamSet.invoke();
			try
			{
				// Get differences between filtered zip entries
				List<ZipFileComparison.Difference> differences =
						ZipFileComparison.compare(zipFile, file, paramSet.getFilters(), paramSet.getFields());

				// Report result
				if (differences.isEmpty())
					NotificationDialog.show(this, COMPARE_FILES_STR, MessageIcon32.INFORMATION.get(), NO_DIFFERENCES_STR);
				else
					new ResultDialog(this, differences).showDialog();
			}
			catch (BaseException e)
			{
				ErrorDialog.show(this, COMPARE_FILES_STR, e);
			}
		});
		addButton(compareButton, HPos.LEFT);

		// Create procedure to update 'compare' button
		IProcedure0 updateCompareButton = () ->
				compareButton.setDisable((comparandFileField.getLocation() == null) || selectedFields.isEmpty());

		// Update 'compare' button when text of comparand file field changes
		comparandFileField.textProperty().addListener(observable -> updateCompareButton.invoke());

		// Update 'compare' button when set of selected fields changes
		selectedFields.addListener((InvalidationListener) observable -> updateCompareButton.invoke());

		// Update 'compare' button
		updateCompareButton.invoke();

		// Create button: close
		Button closeButton = new Button(CLOSE_STR);
		closeButton.getProperties().put(BUTTON_GROUP_KEY, BUTTON_GROUP1);
		closeButton.setOnAction(event -> requestClose());
		addButton(closeButton, HPos.RIGHT);

		// Fire 'close' button if Escape key is pressed; fire 'compare' button if Ctrl+Enter is pressed
		setKeyFireButton(closeButton, compareButton);

		// Update images of image buttons
		Images.updateImageButtons(getScene());

		// Get drag-and-drop filter
		Predicate<Path> dragAndDropFilter = UnzipApp.instance().getPreferences().getZipFileDragAndDropFilter();

		// Set drag-and-drop handler to accept a zip file
		getScene().setOnDragOver(event ->
		{
			// Accept drag if dragboard contains a zip file
			if (ClipboardUtils.locationMatches(event.getDragboard(), dragAndDropFilter))
				event.acceptTransferModes(TransferMode.COPY);

			// Consume event
			event.consume();
		});

		// Set drag-and-drop handler to set location of zip file on comparand pathname field
		getScene().setOnDragDropped(event ->
		{
			// Get location of first zip file from dragboard
			Path file = ClipboardUtils.firstMatchingLocation(event.getDragboard(), dragAndDropFilter);

			// Indicate that drag-and-drop is complete
			event.setDropCompleted(true);

			// Set location of zip file on comparand pathname field
			if (file != null)
				comparandFileField.setLocation(file);

			// Consume event
			event.consume();
		});

		// Save dialog state when dialog is closed
		setOnHiding(event ->
		{
			state.restoreAndUpdate(this, true);
			state.paramSetName = paramSetComboBox.getText();
			state.paramSets.clear();
			state.paramSets.addAll(paramSetComboBox.getItems());
		});

		// Process saved name of parameter set
		if (!StringUtils.isNullOrEmpty(state.paramSetName))
		{
			// Search saved list of parameter sets for saved name
			ComparisonParams paramSet = state.paramSets.stream()
														.filter(ps -> ps.getName().equals(state.paramSetName))
														.findFirst()
														.orElse(null);

			// If saved name doesn't match a parameter set, set name on text field of combo box ...
			if (paramSet == null)
				paramSetComboBox.setText(state.paramSetName);

			// ... otherwise, select parameter set
			else
				paramSetComboBox.setValue(paramSet);
		}

		// Apply new style sheet to scene
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
		Color colour = StyleManager.INSTANCE.getColour(key);
		return (colour == null) ? StyleManager.DEFAULT_COLOUR : colour;
	}

	//------------------------------------------------------------------

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
			String	DIRECTORY		= "directory";
			String	PARAMETER_SETS	= "parameterSets";
		}

	////////////////////////////////////////////////////////////////////
	//  Instance variables
	////////////////////////////////////////////////////////////////////

		private	Path					directory;
		private	String					paramSetName;
		private	List<ComparisonParams>	paramSets;

	////////////////////////////////////////////////////////////////////
	//  Constructors
	////////////////////////////////////////////////////////////////////

		private State()
		{
			// Call superclass constructor
			super(true, true);

			// Initialise instance variables
			paramSets = new ArrayList<>();
		}

		//--------------------------------------------------------------

	////////////////////////////////////////////////////////////////////
	//  Instance methods : overriding methods
	////////////////////////////////////////////////////////////////////

		/**
		 * {@inheritDoc}
		 */

		@Override
		public MapNode encodeTree()
		{
			// Call superclass method
			MapNode rootNode = super.encodeTree();

			// Encode directory
			Utils.encodeLocation(rootNode, PropertyKey.DIRECTORY, directory);

			// Encode parameter sets
			if (!paramSets.isEmpty())
			{
				ListNode paramSetsNode = rootNode.addList(PropertyKey.PARAMETER_SETS);
				for (ComparisonParams paramSet : paramSets)
					paramSetsNode.add(paramSet.encode());
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

			// Decode directory
			String key = PropertyKey.DIRECTORY;
			if (rootNode.hasString(key))
				directory = Utils.decodeLocation(rootNode, key);

			// Decode parameter sets
			key = PropertyKey.PARAMETER_SETS;
			if (rootNode.hasList(key))
			{
				paramSets.clear();
				for (MapNode node : rootNode.getListNode(key).mapNodes())
				{
					ComparisonParams paramSet = new ComparisonParams("");
					paramSet.decode(node);
					paramSets.add(paramSet);
				}
			}
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


	// CLASS: PATTERN-KIND ICON FACTORY


	private static class PatternKindIconFactory
	{

	////////////////////////////////////////////////////////////////////
	//  Constants
	////////////////////////////////////////////////////////////////////

		private static final	double	PADDING	= 3.0;

		private static final	TextBoundsType	TEXT_BOUNDS_TYPE	= TextBoundsType.VISUAL;

	////////////////////////////////////////////////////////////////////
	//  Instance variables
	////////////////////////////////////////////////////////////////////

		private	Font	font;

	////////////////////////////////////////////////////////////////////
	//  Constructors
	////////////////////////////////////////////////////////////////////

		private PatternKindIconFactory(
			Font	font)
		{
			// Initialise instance variables
			this.font = (font == null) ? Font.getDefault() : font;
		}

		//--------------------------------------------------------------

	////////////////////////////////////////////////////////////////////
	//  Class methods
	////////////////////////////////////////////////////////////////////

		private static String patternKindToText(
			PatternKind	patternKind)
		{
			return Character.toString(patternKind.getShortKey()).toUpperCase();
		}

		//--------------------------------------------------------------

	////////////////////////////////////////////////////////////////////
	//  Instance methods
	////////////////////////////////////////////////////////////////////

		private Dimension2D getTextSize(
			PatternKind	patternKind)
		{
			// Create text node
			Text2 textNode = new Text2(patternKindToText(patternKind));
			textNode.setBoundsType(TEXT_BOUNDS_TYPE);
			textNode.setFont(font);

			// Return dimensions of text node
			Bounds bounds = textNode.getLayoutBounds();
			return new Dimension2D(bounds.getWidth(), bounds.getHeight());
		}

		//--------------------------------------------------------------

		private Group createIcon(
			PatternKind	patternKind,
			double		radius)
		{
			// Create group
			Group group = new Group();

			// Add padding to radius
			radius += PADDING;

			// Initialise 'glob' flag
			boolean glob = patternKind.isGlob();

			// Create semicircle and half-disc
			if (patternKind.isFilename())
			{
				// Create semicircle (left)
				Arc arc = new Arc(radius, radius, radius, radius, 90.0, 180.0);
				arc.setStrokeWidth(1.0);
				arc.setFill(null);
				arc.setStrokeType(StrokeType.INSIDE);
				arc.setStroke(getColour(ColourKey.PATTERN_KIND_ICON_STROKE));
				arc.getStyleClass().add(StyleClass.PATTERN_KIND_ICON);
				group.getChildren().add(arc);

				// Create half-disc (right)
				arc = new Arc(radius, radius, radius, radius, 270.0, 180.0);
				arc.setType(ArcType.CHORD);
				arc.setStrokeWidth(1.0);
				arc.setStrokeType(StrokeType.INSIDE);
				arc.setFill(getColour(glob ? ColourKey.PATTERN_KIND_ICON_FILL_GLOB
										   : ColourKey.PATTERN_KIND_ICON_FILL_REGEX));
				arc.setStroke(getColour(glob ? ColourKey.PATTERN_KIND_ICON_STROKE_GLOB
											 : ColourKey.PATTERN_KIND_ICON_STROKE_REGEX));
				arc.getStyleClass().add(glob ? StyleClass.PATTERN_KIND_ICON_GLOB : StyleClass.PATTERN_KIND_ICON_REGEX);
				group.getChildren().add(arc);
			}

			// Create disc
			else
			{
				Circle disc = new Circle(radius, radius, radius);
				disc.setStrokeWidth(1.0);
				disc.setStrokeType(StrokeType.INSIDE);
				disc.setFill(getColour(glob ? ColourKey.PATTERN_KIND_ICON_FILL_GLOB
											: ColourKey.PATTERN_KIND_ICON_FILL_REGEX));
				disc.setStroke(getColour(glob ? ColourKey.PATTERN_KIND_ICON_STROKE_GLOB
											  : ColourKey.PATTERN_KIND_ICON_STROKE_REGEX));
				disc.getStyleClass().add(glob ? StyleClass.PATTERN_KIND_ICON_GLOB : StyleClass.PATTERN_KIND_ICON_REGEX);
				group.getChildren().add(disc);
			}

			// Create text node
			Text2 textNode = new Text2(patternKindToText(patternKind));
			textNode.setBoundsType(TEXT_BOUNDS_TYPE);
			textNode.setFont(font);
			textNode.setFill(getColour(ColourKey.PATTERN_KIND_ICON_TEXT));
			textNode.getStyleClass().add(StyleClass.PATTERN_KIND_ICON_TEXT);
			group.getChildren().add(textNode);

			// Centre text node in shape
			Bounds bounds = textNode.getLayoutBounds();
			textNode.setLayoutX(radius - 0.5 * bounds.getWidth());
			textNode.setLayoutY(radius + 0.5 * bounds.getHeight());

			// Return group
			return group;
		}

		//--------------------------------------------------------------

	}

	//==================================================================


	// CLASS: FILTER LIST VIEW


	private static class FilterListView
		extends ListView<LocationFilter>
	{

	////////////////////////////////////////////////////////////////////
	//  Constants
	////////////////////////////////////////////////////////////////////

		/** The preferred height of the list view. */
		private static final	double	HEIGHT	= 160.0;

		/** The scale factor of the font size of the placeholder label. */
		private static final	double	PLACEHOLDER_LABEL_FONT_SIZE_FACTOR	= 1.2;

		/** Miscellaneous strings. */
		private static final	String	NO_FILTERS_STR	= "No filters";

	////////////////////////////////////////////////////////////////////
	//  Instance variables
	////////////////////////////////////////////////////////////////////

		/** The manager of the pop-up windows that are displayed for the cells of this list view. */
		private	CellPopUpManager		cellPopUpManager;

		/** The cells of this list view. */
		private	List<Cell>				cells;

		/** The factory that creates pattern-kind icons. */
		private	PatternKindIconFactory	iconFactory;

		/** The radius of pattern-kind icons. */
		private	double					iconRadius;

	////////////////////////////////////////////////////////////////////
	//  Constructors
	////////////////////////////////////////////////////////////////////

		private FilterListView()
		{
			// Initialise instance variables
			cellPopUpManager = new CellPopUpManager(Cell.POP_UP_DELAY);
			cells = new ArrayList<>();
			iconFactory = new PatternKindIconFactory(FontUtils.boldFont());
			double maxHalfWidth = 0.0;
			double maxHalfHeight = 0.0;
			for (PatternKind patternKind : PatternKind.values())
			{
				Dimension2D size = iconFactory.getTextSize(patternKind);
				maxHalfWidth = Math.max(maxHalfWidth, 0.5 * size.getWidth());
				maxHalfHeight = Math.max(maxHalfHeight, 0.5 * size.getHeight());
			}
			iconRadius = Math.ceil(Math.sqrt(maxHalfWidth * maxHalfWidth + maxHalfHeight * maxHalfHeight));

			// Set properties
			setPrefHeight(HEIGHT);
			setCellFactory(listView -> new Cell());
			getStyleClass().addAll(StyleClass.FILTER_LIST_VIEW, ListViewStyle.StyleClass.LIST_VIEW);

			// Set placeholder label
			Label placeholderLabel = new Label(NO_FILTERS_STR);
			placeholderLabel.setTextFill(getColour(ColourKey.FILTER_LIST_VIEW_PLACEHOLDER_TEXT));
			placeholderLabel.setFont(FontUtils.defaultFont(PLACEHOLDER_LABEL_FONT_SIZE_FACTOR));
			placeholderLabel.getStyleClass().add(StyleClass.PLACEHOLDER_LABEL);
			setPlaceholder(placeholderLabel);

			// Update cell backgrounds on change of state
			if (StyleManager.INSTANCE.notUsingStyleSheet())
			{
				// Update cell backgrounds when selection changes
				getSelectionModel().selectedIndexProperty().addListener(observable -> updateCellBackgrounds());

				// Update cell backgrounds when focus changes
				focusedProperty().addListener(observable -> updateCellBackgrounds());

				// Update cell backgrounds when focused row changes
				getFocusModel().focusedIndexProperty().addListener(observable -> updateCellBackgrounds());
			}

			// Ensure cells are redrawn if scroll bar is hidden
			widthProperty().addListener(observable -> Platform.runLater(() -> refresh()));

			// Set background of empty cells
			if (StyleManager.INSTANCE.notUsingStyleSheet())
			{
				skinProperty().addListener((observable, oldSkin, skin) ->
				{
					if (skin != null)
					{
						Node node = lookup(StyleSelector.VIRTUAL_FLOW);
						if (node instanceof Region region)
						{
							region.setBackground(SceneUtils.createColouredBackground(
									getColour(ListViewStyle.ColourKey.CELL_BACKGROUND_EMPTY)));
						}
					}
				});
			}
		}

		//--------------------------------------------------------------

	////////////////////////////////////////////////////////////////////
	//  Instance methods
	////////////////////////////////////////////////////////////////////

		/**
		 * Updates the backgrounds of the cells of this list view.
		 */

		private void updateCellBackgrounds()
		{
			for (Cell cell : cells)
				cell.updateBackground();
		}

		//--------------------------------------------------------------

		/**
		 * Returns the window that contains this list view.
		 *
		 * @return the window that contains this list view.
		 */

		private Window getWindow()
		{
			return SceneUtils.getWindow(this);
		}

		//--------------------------------------------------------------

	////////////////////////////////////////////////////////////////////
	//  Member classes : inner classes
	////////////////////////////////////////////////////////////////////


		// CLASS: CELL OF LIST VIEW


		/**
		 * This class implements a cell of the enclosing list view.
		 */

		private class Cell
			extends ListCell<LocationFilter>
			implements CellPopUpManager.ICell<LocationFilter>
		{

		////////////////////////////////////////////////////////////////
		//  Constants
		////////////////////////////////////////////////////////////////

			private static final	double	ICON_GAP		= 4.0;
			private static final	double	ICON_TEXT_GAP	= 6.0;

			/** The padding around the label of a cell. */
			private static final	Insets	LABEL_PADDING	= new Insets(1.0, 6.0, 1.0, 6.0);

			/** The delay (in milliseconds) before a pop-up for a cell is displayed after it is activated. */
			private static final	int		POP_UP_DELAY	= 500;

			/** Miscellaneous strings. */
			private static final	String	INCLUDE_STR			= "Include";
			private static final	String	EXCLUDE_STR			= "Exclude";
			private static final	String	GLOB_FILENAME_STR	= "glob, filename";
			private static final	String	GLOB_PATHNAME_STR	= "glob, pathname";
			private static final	String	REGEX_FILENAME_STR	= "regex, filename";
			private static final	String	REGEX_PATHNAME_STR	= "regex, pathname";

		////////////////////////////////////////////////////////////////
		//  Constructors
		////////////////////////////////////////////////////////////////

			/**
			 * Creates a new instance of a cell for the enclosing list view.
			 */

			private Cell()
			{
				// Set properties
				setContentDisplay(ContentDisplay.GRAPHIC_ONLY);
				setAlignment(Pos.CENTER_LEFT);
				setPadding(Insets.EMPTY);

				// Add cell to list
				cells.add(this);

				// Activate pop-up for cell; clear selection if cell is empty
				addEventHandler(MouseEvent.MOUSE_PRESSED, event ->
				{
					// Activate pop-up for cell
					if (event.getButton() == MouseButton.PRIMARY)
						cellPopUpManager.activate(getIdentifier(), cells.iterator());

					// Clear selection if cell is empty
					if (isEmpty())
						getSelectionModel().clearSelection();
				});

				// When mouse leaves cell, deactivate any cell pop-up
				addEventHandler(MouseEvent.MOUSE_EXITED, event -> cellPopUpManager.deactivate());

				// When a mouse button is released, deactivate any cell pop-up
				addEventFilter(MouseEvent.MOUSE_RELEASED, event ->
				{
					if (cellPopUpManager.deactivate())
						event.consume();
				});
			}

			//----------------------------------------------------------

		////////////////////////////////////////////////////////////////
		//  Instance methods : CellPopUpManager.ICell interface
		////////////////////////////////////////////////////////////////

			/**
			 * {@inheritDoc}
			 */

			@Override
			public Node getPopUpContent()
			{
				// Create label
				Label label = createLabel();

				// Set properties of label
				if (label != null)
				{
					label.setBackground(SceneUtils.createColouredBackground(
							getColour(ListViewStyle.ColourKey.CELL_POPUP_BACKGROUND)));
					label.setBorder(SceneUtils.createSolidBorder(getColour(ListViewStyle.ColourKey.CELL_POPUP_BORDER)));
					label.getStyleClass().add(ListViewStyle.StyleClass.CELL_POPUP_LABEL);
				}

				// Return label
				return label;
			}

			//----------------------------------------------------------

			/**
			 * {@inheritDoc}
			 */

			@Override
			public Point2D getPrefPopUpLocation(
				Node	content)
			{
				Node node = getGraphic();
				return (node == null) ? null
									  : PopUpUtils.createLocator(node, VHPos.CENTRE_LEFT, VHPos.CENTRE_LEFT, -1.0, 0.0)
													.getLocation(content.getLayoutBounds(), null);
			}

			//----------------------------------------------------------

			/**
			 * {@inheritDoc}
			 */

			@Override
			public Window getWindow()
			{
				return FilterListView.this.getWindow();
			}

			//----------------------------------------------------------

		////////////////////////////////////////////////////////////////
		//  Instance methods : overriding methods
		////////////////////////////////////////////////////////////////

			/**
			 * {@inheritDoc}
			 */

			@Override
			protected void updateItem(
				LocationFilter	filter,
				boolean			empty)
			{
				// Call superclass method
				super.updateItem(filter, empty);

				// Set background
				updateBackground();

				// Set border
				Color colour = empty ? Color.TRANSPARENT : getColour(ListViewStyle.ColourKey.CELL_BORDER);
				setBorder(SceneUtils.createSolidBorder(colour, Side.BOTTOM));

				// Set graphic
				Label label = empty ? null : createLabel();
				if (label != null)
				{
					Node node = label.getGraphic();
					Object data = node.getUserData();
					if (data != null)
					{
						node.setPickOnBounds(true);
						TooltipDecorator.addTooltip(node, data.toString());
					}
				}
				setGraphic(label);
			}

			//----------------------------------------------------------

		////////////////////////////////////////////////////////////////
		//  Instance methods
		////////////////////////////////////////////////////////////////

			/**
			 * Updates the background of this cell.
			 */

			private void updateBackground()
			{
				int index = getIndex();
				boolean selected = getSelectionModel().getSelectedIndices().contains(index);
				boolean focused = getListView().isFocused();
				Color colour = isEmpty()
									? null
									: selected
											? focused
													? getColour(ListViewStyle.ColourKey.CELL_BACKGROUND_SELECTED_FOCUSED)
													: getColour(ListViewStyle.ColourKey.CELL_BACKGROUND_SELECTED)
											: (index % 2 == 0)
													? getColour(ListViewStyle.ColourKey.CELL_BACKGROUND_EVEN)
													: getColour(ListViewStyle.ColourKey.CELL_BACKGROUND_ODD);
				if (!selected && focused && (getFocusModel().getFocusedIndex() == index))
				{
					setBackground(SceneUtils.createColouredBackground(
							getColour(ListViewStyle.ColourKey.CELL_BACKGROUND_FOCUSED), new Insets(0.0, 0.0, 1.0, 0.0),
							colour, new Insets(1.0, 1.0, 2.0, 1.0)));
				}
				else
					setBackground(SceneUtils.createColouredBackground(colour));
			}

			//----------------------------------------------------------

			/**
			 * Creates and returns a label for the item of this cell.
			 *
			 * @return a label for the item of this cell.
			 */

			private Label createLabel()
			{
				// Get filter
				LocationFilter filter = getItem();

				// Create label
				Label label = null;
				if (filter != null)
				{
					// Create filter-kind icon
					ImageView filterKindIcon = ImageUtils.createSmoothImageView(filter.isInclusive() ? ImageData.INCLUDE
																									 : ImageData.EXCLUDE);

					// Create pattern-kind icon
					Group patternKindIcon = iconFactory.createIcon(filter.getPatternKind(), iconRadius);
					patternKindIcon.setLayoutX(filterKindIcon.getImage().getWidth() + ICON_GAP);

					// Align filter-kind icon and pattern-kind icon vertically
					double filterKindHeight = filterKindIcon.getLayoutBounds().getHeight();
					double patternKindHeight = patternKindIcon.getLayoutBounds().getHeight();
					if (filterKindHeight < patternKindHeight)
						filterKindIcon.relocate(0.0, 0.5 * (patternKindHeight - filterKindHeight));
					else
						patternKindIcon.relocate(0.0, 0.5 * (filterKindHeight - patternKindHeight));

					// Create container for filter-kind icon and pattern-kind icon
					Group group = new Group(filterKindIcon, patternKindIcon);
					StringBuilder buffer = new StringBuilder();
					buffer.append(filter.isInclusive() ? INCLUDE_STR : EXCLUDE_STR);
					switch (filter.getPatternKind())
					{
						case GLOB_FILENAME:
							buffer.append(" : ");
							buffer.append(GLOB_FILENAME_STR);
							break;

						case GLOB_PATHNAME:
							buffer.append(" : ");
							buffer.append(GLOB_PATHNAME_STR);
							break;

						case REGEX_FILENAME:
							buffer.append(" : ");
							buffer.append(REGEX_FILENAME_STR);
							break;

						case REGEX_PATHNAME:
							buffer.append(" : ");
							buffer.append(REGEX_PATHNAME_STR);
							break;
					}
					group.setUserData(buffer.toString());

					// Create label
					label = new Label(filter.getPattern(), group);
					label.setGraphicTextGap(ICON_TEXT_GAP);
					label.setPadding(LABEL_PADDING);
					label.setTextFill(getColour(ListViewStyle.ColourKey.CELL_TEXT));
					label.getStyleClass().add(ListViewStyle.StyleClass.CELL_LABEL);
				}

				// Return label
				return label;
			}

			//----------------------------------------------------------

		}

		//==============================================================

	////////////////////////////////////////////////////////////////////
	//  Image data
	////////////////////////////////////////////////////////////////////

		private interface ImageData
		{
			byte[]	INCLUDE	=
			{
				(byte)0x89, (byte)0x50, (byte)0x4E, (byte)0x47, (byte)0x0D, (byte)0x0A, (byte)0x1A, (byte)0x0A,
				(byte)0x00, (byte)0x00, (byte)0x00, (byte)0x0D, (byte)0x49, (byte)0x48, (byte)0x44, (byte)0x52,
				(byte)0x00, (byte)0x00, (byte)0x00, (byte)0x10, (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x10,
				(byte)0x08, (byte)0x06, (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x1F, (byte)0xF3, (byte)0xFF,
				(byte)0x61, (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x8A, (byte)0x49, (byte)0x44, (byte)0x41,
				(byte)0x54, (byte)0x78, (byte)0x5E, (byte)0xCD, (byte)0x93, (byte)0xDD, (byte)0x09, (byte)0x80,
				(byte)0x30, (byte)0x0C, (byte)0x84, (byte)0x6F, (byte)0x21, (byte)0x5D, (byte)0xD9, (byte)0x17,
				(byte)0xAD, (byte)0x75, (byte)0x09, (byte)0x11, (byte)0x1C, (byte)0x40, (byte)0x37, (byte)0xB0,
				(byte)0x6E, (byte)0xE0, (byte)0x02, (byte)0x52, (byte)0x7B, (byte)0xE2, (byte)0x4F, (byte)0xC9,
				(byte)0x83, (byte)0x8A, (byte)0x55, (byte)0xE8, (byte)0xC1, (byte)0xF7, (byte)0x12, (byte)0x2E,
				(byte)0x47, (byte)0x5A, (byte)0x12, (byte)0xE0, (byte)0x13, (byte)0x29, (byte)0xA4, (byte)0x8E,
				(byte)0x1E, (byte)0x1A, (byte)0x06, (byte)0x15, (byte)0xC6, (byte)0x4B, (byte)0xE8, (byte)0xA1,
				(byte)0x37, (byte)0x47, (byte)0xE2, (byte)0x07, (byte)0xF4, (byte)0xA8, (byte)0x31, (byte)0xA3,
				(byte)0x79, (byte)0x08, (byte)0xBD, (byte)0x0A, (byte)0xDD, (byte)0x19, (byte)0x50, (byte)0xBA,
				(byte)0x54, (byte)0x69, (byte)0x72, (byte)0xB4, (byte)0x53, (byte)0x6B, (byte)0x89, (byte)0xAC,
				(byte)0xAF, (byte)0x68, (byte)0x0C, (byte)0x67, (byte)0x00, (byte)0xC7, (byte)0x92, (byte)0x06,
				(byte)0x87, (byte)0xDD, (byte)0x24, (byte)0xEB, (byte)0x5B, (byte)0x80, (byte)0x89, (byte)0x30,
				(byte)0x80, (byte)0xEF, (byte)0xBD, (byte)0xD3, (byte)0xF1, (byte)0x27, (byte)0xBF, (byte)0x04,
				(byte)0x48, (byte)0xF6, (byte)0x26, (byte)0x59, (byte)0x8F, (byte)0x31, (byte)0x20, (byte)0x78,
				(byte)0x91, (byte)0xDE, (byte)0xAC, (byte)0x72, (byte)0xE1, (byte)0xAF, (byte)0x32, (byte)0x0F,
				(byte)0x83, (byte)0xBB, (byte)0xCD, (byte)0xB1, (byte)0xE4, (byte)0xF1, (byte)0x48, (byte)0xE8,
				(byte)0x61, (byte)0x73, (byte)0xE6, (byte)0x1F, (byte)0x53, (byte)0x80, (byte)0x16, (byte)0xA2,
				(byte)0x4B, (byte)0x93, (byte)0x44, (byte)0xAB, (byte)0x23, (byte)0x49, (byte)0x6A, (byte)0x00,
				(byte)0x00, (byte)0x00, (byte)0x00, (byte)0x49, (byte)0x45, (byte)0x4E, (byte)0x44, (byte)0xAE,
				(byte)0x42, (byte)0x60, (byte)0x82
			};

			byte[]	EXCLUDE	=
			{
				(byte)0x89, (byte)0x50, (byte)0x4E, (byte)0x47, (byte)0x0D, (byte)0x0A, (byte)0x1A, (byte)0x0A,
				(byte)0x00, (byte)0x00, (byte)0x00, (byte)0x0D, (byte)0x49, (byte)0x48, (byte)0x44, (byte)0x52,
				(byte)0x00, (byte)0x00, (byte)0x00, (byte)0x10, (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x10,
				(byte)0x08, (byte)0x06, (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x1F, (byte)0xF3, (byte)0xFF,
				(byte)0x61, (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x83, (byte)0x49, (byte)0x44, (byte)0x41,
				(byte)0x54, (byte)0x78, (byte)0x5E, (byte)0xDD, (byte)0x93, (byte)0x4D, (byte)0x0A, (byte)0x83,
				(byte)0x30, (byte)0x18, (byte)0x44, (byte)0xDF, (byte)0x85, (byte)0xF4, (byte)0xFE, (byte)0x5B,
				(byte)0x15, (byte)0x63, (byte)0xBB, (byte)0xB2, (byte)0x27, (byte)0xD0, (byte)0xF4, (byte)0x14,
				(byte)0xAE, (byte)0x44, (byte)0x33, (byte)0x4A, (byte)0x8B, (byte)0x46, (byte)0xD4, (byte)0xD6,
				(byte)0x1F, (byte)0x10, (byte)0x07, (byte)0xDE, (byte)0x26, (byte)0xCC, (byte)0x0C, (byte)0x59,
				(byte)0xCC, (byte)0x07, (byte)0x47, (byte)0x28, (byte)0x86, (byte)0x30, (byte)0x83, (byte)0xD7,
				(byte)0x03, (byte)0xEC, (byte)0x13, (byte)0xDE, (byte)0x4B, (byte)0xC8, (byte)0x93, (byte)0x3A,
				(byte)0x6F, (byte)0x04, (byte)0xC1, (byte)0xB7, (byte)0x40, (byte)0xE1, (byte)0x12, (byte)0x6A,
				(byte)0xFB, (byte)0x23, (byte)0x85, (byte)0xC3, (byte)0x40, (byte)0xFE, (byte)0xC9, (byte)0xA3,
				(byte)0x56, (byte)0xDF, (byte)0xB4, (byte)0x86, (byte)0xFB, (byte)0x4D, (byte)0xD9, (byte)0xA7,
				(byte)0xD9, (byte)0x56, (byte)0xA0, (byte)0x4C, (byte)0x17, (byte)0x96, (byte)0xAE, (byte)0x53,
				(byte)0x50, (byte)0x25, (byte)0x49, (byte)0xB3, (byte)0x26, (byte)0x79, (byte)0xCE, (byte)0x2B,
				(byte)0xF8, (byte)0x87, (byte)0x1B, (byte)0x14, (byte)0x8C, (byte)0x86, (byte)0xA4, (byte)0x6D,
				(byte)0x6B, (byte)0x9E, (byte)0xBE, (byte)0x69, (byte)0x8E, (byte)0xC9, (byte)0x94, (byte)0x75,
				(byte)0x18, (byte)0x7A, (byte)0xD0, (byte)0x4F, (byte)0xFC, (byte)0xE3, (byte)0xF1, (byte)0x91,
				(byte)0x47, (byte)0x5E, (byte)0x33, (byte)0x3C, (byte)0xA6, (byte)0x3D, (byte)0x6A, (byte)0x01,
				(byte)0x59, (byte)0x7E, (byte)0x87, (byte)0x90, (byte)0x85, (byte)0x0B, (byte)0x2E, (byte)0xBA,
				(byte)0x00, (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x49, (byte)0x45, (byte)0x4E, (byte)0x44,
				(byte)0xAE, (byte)0x42, (byte)0x60, (byte)0x82
			};
		}

		//==============================================================

	}

	//==================================================================


	// CLASS: FILTER DIALOG


	/**
	 * This class implements a modal dialog in which a filter may be edited.
	 */

	private static class FilterDialog
		extends SimpleModalDialog<LocationFilter>
	{

	////////////////////////////////////////////////////////////////////
	//  Constants
	////////////////////////////////////////////////////////////////////

		/** The horizontal gap between adjacent columns of the control pane. */
		private static final	double	CONTROL_PANE_H_GAP	= 6.0;

		/** The vertical gap between adjacent rows of the control pane. */
		private static final	double	CONTROL_PANE_V_GAP	= 6.0;

		/** The number of columns of the pattern field. */
		private static final	int		PATTERN_FIELD_NUM_COLUMNS	= 30;

		/** Miscellaneous strings. */
		private static final	String	FILTER_KIND_STR		= "Filter kind";
		private static final	String	PATTERN_KIND_STR	= "Pattern kind";
		private static final	String	PATTERN_STR			= "Pattern";

	////////////////////////////////////////////////////////////////////
	//  Class variables
	////////////////////////////////////////////////////////////////////

		private static	FilterKind	filterKind	= FilterKind.INCLUDE;
		private static	PatternKind	patternKind	= PatternKind.GLOB_PATHNAME;

	////////////////////////////////////////////////////////////////////
	//  Instance variables
	////////////////////////////////////////////////////////////////////

		private	LocationFilter	result;

	////////////////////////////////////////////////////////////////////
	//  Constructors
	////////////////////////////////////////////////////////////////////

		/**
		 * Creates a new instance of a modal dialog in which a filter may be edited.
		 *
		 * @param owner
		 *          the window that will own this dialog, or {@code null} for a top-level dialog that has no owner.
		 * @param title
		 *          the title of the dialog.
		 * @param filter
		 *          the initial filter, which may be {@code null}.
		 */

		private FilterDialog(
			Window			owner,
			String			title,
			LocationFilter	filter)
		{
			// Call superclass constructor
			super(owner, MethodHandles.lookup().lookupClass().getCanonicalName(), null, title);

			// Create control pane
			GridPane controlPane = new GridPane();
			controlPane.setHgap(CONTROL_PANE_H_GAP);
			controlPane.setVgap(CONTROL_PANE_V_GAP);

			// Initialise column constraints
			ColumnConstraints column1 = new ColumnConstraints();
			column1.setMinWidth(GridPane.USE_PREF_SIZE);
			column1.setHalignment(HPos.RIGHT);
			column1.setFillWidth(false);
			controlPane.getColumnConstraints().add(column1);

			ColumnConstraints column2 = new ColumnConstraints();
			column2.setHalignment(HPos.LEFT);
			column2.setFillWidth(false);
			controlPane.getColumnConstraints().add(column2);

			// Initialise row index
			int row = 0;

			// Spinner: filter kind
			FilterKind initFilterKind = (filter == null)
												? filterKind
												: filter.isInclusive()
														? FilterKind.INCLUDE
														: FilterKind.EXCLUDE;
			CollectionSpinner<FilterKind> filterKindSpinner =
					CollectionSpinner.leftRightH(HPos.LEFT, true, FilterKind.class, initFilterKind, null, null);
			controlPane.addRow(row++, new Label(FILTER_KIND_STR), filterKindSpinner);

			// Spinner: pattern kind
			PatternKind initPatternKind = (filter == null) ? patternKind : filter.getPatternKind();
			CollectionSpinner<PatternKind> patternKindSpinner =
					CollectionSpinner.leftRightH(HPos.LEFT, true, PatternKind.class, initPatternKind, null, null);
			controlPane.addRow(row++, new Label(PATTERN_KIND_STR), patternKindSpinner);

			// Text field: pattern
			TextField patternField = new TextField((filter == null) ? "" : filter.getPattern());
			patternField.setPrefColumnCount(PATTERN_FIELD_NUM_COLUMNS);
			controlPane.addRow(row++, new Label(PATTERN_STR), patternField);

			// Add control pane to content pane
			addContent(controlPane);

			// Create button: OK
			Button okButton = new Button(OK_STR);
			okButton.getProperties().put(BUTTON_GROUP_KEY, BUTTON_GROUP1);
			okButton.setOnAction(event ->
			{
				try
				{
					result = new LocationFilter((filterKindSpinner.getItem() == FilterKind.INCLUDE),
												patternKindSpinner.getItem(), patternField.getText(), null);
					hide();
				}
				catch (BaseException e)
				{
					ErrorDialog.show(this, title, e);
				}
			});
			addButton(okButton, HPos.RIGHT);

			// Create procedure to enable/disable 'OK' button according to validity of pattern
			IProcedure0 updateOkButton = () ->
			{
				boolean valid = false;
				String text = patternField.getText();
				if (!text.isEmpty())
				{
					switch (patternKindSpinner.getItem())
					{
						case GLOB_FILENAME:
						case GLOB_PATHNAME:
							valid = true;
							break;

						case REGEX_FILENAME:
						case REGEX_PATHNAME:
							try
							{
								Pattern.compile(text);
								valid = true;
							}
							catch (PatternSyntaxException e)
							{
								// ignore
							}
							break;
					}
				}
				okButton.setDisable(!valid);
			};

			// Enable/disable 'OK' button when pattern kind or pattern changes
			patternKindSpinner.valueProperty().addListener(observable -> updateOkButton.invoke());
			patternField.textProperty().addListener(observable -> updateOkButton.invoke());

			// Enable/disable 'OK' button according to validity of pattern
			updateOkButton.invoke();

			// Create button: cancel
			Button cancelButton = new Button(CANCEL_STR);
			cancelButton.getProperties().put(BUTTON_GROUP_KEY, BUTTON_GROUP1);
			cancelButton.setOnAction(event -> requestClose());
			addButton(cancelButton, HPos.RIGHT);

			// Fire 'cancel' button if Escape key is pressed; fire 'OK' button if Ctrl+Enter is pressed
			setKeyFireButton(cancelButton, okButton);

			// Update state when dialog is closed
			setOnHiding(event ->
			{
				filterKind = filterKindSpinner.getItem();
				patternKind = patternKindSpinner.getItem();
			});

			// Apply new style sheet to scene
			applyStyleSheet();
		}

		//--------------------------------------------------------------

	////////////////////////////////////////////////////////////////////
	//  Instance methods : overriding methods
	////////////////////////////////////////////////////////////////////

		@Override
		protected LocationFilter getResult()
		{
			return result;
		}

		//--------------------------------------------------------------

	////////////////////////////////////////////////////////////////////
	//  Enumerated types
	////////////////////////////////////////////////////////////////////


		// ENUMERATION: KINDS OF FILTER


		private enum FilterKind
		{

		////////////////////////////////////////////////////////////////
		//  Constants
		////////////////////////////////////////////////////////////////

			INCLUDE,
			EXCLUDE;

		////////////////////////////////////////////////////////////////
		//  Constructors
		////////////////////////////////////////////////////////////////

			/**
			 * Creates a new instance of a kind of filter.
			 */

			private FilterKind()
			{
			}

			//----------------------------------------------------------

		////////////////////////////////////////////////////////////////
		//  Instance methods : overriding methods
		////////////////////////////////////////////////////////////////

			/**
			 * {@inheritDoc}
			 */
			@Override
			public String toString()
			{
				return StringUtils.firstCharToUpperCase(name().toLowerCase());
			}

			//----------------------------------------------------------

		}

		//==============================================================

	}

	//==================================================================


	// CLASS: 'EDIT PARAMETER SETS' DIALOG


	private static class EditParamSetsDialog
		extends SimpleModalDialog<List<ComparisonParams>>
	{

	////////////////////////////////////////////////////////////////////
	//  Constants
	////////////////////////////////////////////////////////////////////

		private static final	double	LIST_VIEW_WIDTH		= 240.0;
		private static final	double	LIST_VIEW_HEIGHT	= 288.0;

		private static final	Insets	EDITOR_BUTTON_PANE_PADDING	= new Insets(2.0, 6.0, 2.0, 0.0);

		private static final	Insets	CONTENT_PANE_PADDING	= new Insets(2.0);

		private static final	String	REMOVE_PARAM_SET_STR	= "Remove parameter set";
		private static final	String	REMOVE_QUESTION_STR		= "Parameter set: %s" + MessageDialog.MESSAGE_SEPARATOR
																	+ "Do you want to remove the selected parameter set?";
		private static final	String	REMOVE_STR				= "Remove";

	////////////////////////////////////////////////////////////////////
	//  Instance variables
	////////////////////////////////////////////////////////////////////

		private	List<ComparisonParams>	result;

	////////////////////////////////////////////////////////////////////
	//  Constructors
	////////////////////////////////////////////////////////////////////

		private EditParamSetsDialog(
			Window					owner,
			List<ComparisonParams>	directories)
		{
			// Call superclass constructor
			super(owner, MethodHandles.lookup().lookupClass().getCanonicalName(), EDIT_PARAM_SETS_STR);

			// Set properties
			setResizable(true);

			// Create list view
			SimpleTextListView<ComparisonParams> listView =
					new SimpleTextListView<>(FXCollections.observableArrayList(directories),
											 paramSet -> paramSet.getName());
			listView.setPrefSize(LIST_VIEW_WIDTH, LIST_VIEW_HEIGHT);

			// Create list-view editor
			Window window = this;
			ListViewEditor<ComparisonParams> editor = new ListViewEditor<>(listView, new ListViewEditor.IEditor<>()
			{
				@Override
				public ComparisonParams edit(
					ListViewEditor.Action	action,
					ComparisonParams		paramSet)
				{
					return null;
				}

				@Override
				public Set<ListViewEditor.Action> getActions()
				{
					return EnumSet.of(ListViewEditor.Action.REMOVE, ListViewEditor.Action.MOVE_UP,
									  ListViewEditor.Action.MOVE_DOWN);
				}

				@Override
				public boolean hasDialog()
				{
					return false;
				}

				@Override
				public boolean canRemoveWithKeyPress()
				{
					return true;
				}

				@Override
				public boolean confirmRemove(
					ComparisonParams	paramSet)
				{
					return ConfirmationDialog.show(window, REMOVE_PARAM_SET_STR, MessageIcon32.QUESTION.get(),
												   String.format(REMOVE_QUESTION_STR, paramSet.getName()), REMOVE_STR);
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
		}

		//--------------------------------------------------------------

	////////////////////////////////////////////////////////////////////
	//  Instance methods : overriding methods
	////////////////////////////////////////////////////////////////////

		@Override
		protected List<ComparisonParams> getResult()
		{
			return result;
		}

		//--------------------------------------------------------------

	}

	//==================================================================


	// CLASS: RESULT DIALOG


	private static class ResultDialog
		extends SimpleModalDialog<Void>
	{

	////////////////////////////////////////////////////////////////////
	//  Constants
	////////////////////////////////////////////////////////////////////

		private static final	double	TABLE_VIEW_HEIGHT	= 360.0;

		private static final	Insets	CONTENT_PANE_PADDING	= new Insets(2.0, 2.0, 0.0, 2.0);

		private static final	String	COMPARISON_DIFFERENCES_STR	= "Comparison differences";
		private static final	String	DIFFS_STR					= "Diffs";
		private static final	String	DIFFERENCES_STR				= "Differences";
		private static final	String	PATHNAME_STR				= "Pathname";
		private static final	String	COPY_STR					= "Copy";

		private interface ColumnId
		{
			String	DIFFS		= "diffs";
			String	PATHNAME	= "pathname";
		}

	////////////////////////////////////////////////////////////////////
	//  Class variables
	////////////////////////////////////////////////////////////////////

		private static	double	diffKindMaxKeyWidth	= ZipFileComparison.DiffKind.getMaxKeyWidth();

	////////////////////////////////////////////////////////////////////
	//  Constructors
	////////////////////////////////////////////////////////////////////

		private ResultDialog(
			Window								owner,
			List<ZipFileComparison.Difference>	differences)
		{
			// Call superclass constructor
			super(owner, MethodHandles.lookup().lookupClass().getCanonicalName(), COMPARISON_DIFFERENCES_STR);

			// Set properties
			setResizable(true);

			// Create table view
			SimpleTableView<ZipFileComparison.Difference> tableView =
					new SimpleTableView<>(List.of(createDiffsColumn(), createPathnameColumn()));
			tableView.setPrefHeight(TABLE_VIEW_HEIGHT);
			tableView.setItems(differences);

			// Add table view to content
			addContent(tableView);

			// Adjust padding around content pane
			getContentPane().setPadding(CONTENT_PANE_PADDING);

			// Remove border from content pane
			StyleUtils.setProperty(getContentPane(), FxProperty.BORDER_WIDTH.getName(), "0");

			// Create button: copy
			Button copyButton = new Button(COPY_STR);
			copyButton.getProperties().put(BUTTON_GROUP_KEY, BUTTON_GROUP1);
			copyButton.setOnAction(event ->
			{
				// Convert differences to text
				StringBuilder buffer = new StringBuilder(1024);
				for (ZipFileComparison.Difference difference : differences)
				{
					buffer.append(ZipFileComparison.DiffKind.diffKindsToString(difference.diffKinds()));
					buffer.append(" : ");
					buffer.append(difference.pathname());
					buffer.append('\n');
				}

				// Put text on system clipboard
				try
				{
					ClipboardUtils.putTextThrow(buffer.toString());
				}
				catch (BaseException e)
				{
					ErrorDialog.show(this, COPY_STR, e);
				}
			});
			addButton(copyButton, HPos.LEFT);

			// Create button: close
			Button closeButton = new Button(CLOSE_STR);
			closeButton.getProperties().put(BUTTON_GROUP_KEY, BUTTON_GROUP1);
			closeButton.setOnAction(event -> requestClose());
			addButton(closeButton, HPos.RIGHT);

			// Fire 'close' button if Escape key is pressed
			setKeyFireButton(closeButton, null);
		}

		//--------------------------------------------------------------

	////////////////////////////////////////////////////////////////////
	//  Class methods
	////////////////////////////////////////////////////////////////////

		private static SimpleTableView.IColumn<ZipFileComparison.Difference, Set<ZipFileComparison.DiffKind>>
				createDiffsColumn()
		{
			return new SimpleTableView.IColumn<>()
			{
				@Override
				public String getId()
				{
					return ColumnId.DIFFS;
				}

				@Override
				public String getTitle()
				{
					return DIFFS_STR;
				}

				@Override
				public String getLongTitle()
				{
					return DIFFERENCES_STR;
				}

				@Override
				public HPos getHAlignment()
				{
					return HPos.CENTER;
				}

				@Override
				public double getPrefWidth()
				{
					return ZipFileComparison.DiffKind.values().length * diffKindMaxKeyWidth;
				}

				@Override
				public TableColumn<ZipFileComparison.Difference, Set<ZipFileComparison.DiffKind>> createColumn(
					SimpleTableView<ZipFileComparison.Difference>	tableView)
				{
					TableColumn<ZipFileComparison.Difference, Set<ZipFileComparison.DiffKind>> column =
							SimpleTableView.IColumn.super.createColumn(tableView);
					column.setComparator(Comparator.comparingInt(ZipFileComparison.DiffKind::diffKindsToBitArray));
					return column;
				}

				@Override
				public Set<ZipFileComparison.DiffKind> getValue(
					ZipFileComparison.Difference	item)
				{
					return item.diffKinds();
				}

				@Override
				public String getText(
					Set<ZipFileComparison.DiffKind>	diffKinds)
				{
					return null;
				}

				@Override
				public Group getGraphic(
					Set<ZipFileComparison.DiffKind>	diffKinds)
				{
					// Get number of kinds of difference
					int numDiffKinds = ZipFileComparison.DiffKind.values().length;

					// Create group with background rectangle
					Group group = new Group(new Rectangle((double)numDiffKinds * diffKindMaxKeyWidth, 0.0,
														  Color.TRANSPARENT));

					// Create text nodes and add them to group
					String str = ZipFileComparison.DiffKind.diffKindsToString(diffKinds);
					double x = 0.0;
					for (int i = 0; i < numDiffKinds; i++)
					{
						// Create text node
						Text2 textNode = Text2.createCentred(Character.toString(str.charAt(i)));

						// Set properties of text node
						textNode.setFont(FontUtils.boldFont());
						textNode.setFill(getColour(ColourKey.RESULT_DIALOG_DIFFERENCE_TEXT));
						textNode.getStyleClass().add(StyleClass.RESULT_DIALOG_DIFFERENCE);

						// Set x coordinate of text node
						textNode.relocate(x + 0.5 * (diffKindMaxKeyWidth - textNode.getWidth()), 0.0);

						// Add text node to group
						group.getChildren().add(textNode);

						// Increment x coordinate
						x += diffKindMaxKeyWidth;
					}

					// Return group
					return group;
				}
			};
		}

		//--------------------------------------------------------------

		private static SimpleTableView.IColumn<ZipFileComparison.Difference, String> createPathnameColumn()
		{
			return new SimpleTableView.IColumn<>()
			{
				@Override
				public String getId()
				{
					return ColumnId.PATHNAME;
				}

				@Override
				public String getTitle()
				{
					return PATHNAME_STR;
				}

				@Override
				public double getPrefWidth()
				{
					return TextUtils.textWidthCeil("M".repeat(50));
				}

				@Override
				public TableColumn<ZipFileComparison.Difference, String> createColumn(
					SimpleTableView<ZipFileComparison.Difference>	tableView)
				{
					TableColumn<ZipFileComparison.Difference, String> column =
							SimpleTableView.IColumn.super.createColumn(tableView);
					column.setComparator(ZipFileEntry.DIRECTORY_FILENAME_PATHNAME_COMPARATOR);
					return column;
				}

				@Override
				public String getValue(
					ZipFileComparison.Difference	item)
				{
					return item.pathname();
				}

				@Override
				public String getText(
					String	pathname)
				{
					return pathname;
				}
			};
		}

		//--------------------------------------------------------------

	}

	//==================================================================

}

//----------------------------------------------------------------------
