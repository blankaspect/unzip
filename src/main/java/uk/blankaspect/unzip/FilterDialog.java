/*====================================================================*\

FilterDialog.java

Class: filter dialog.

\*====================================================================*/


// PACKAGE


package uk.blankaspect.unzip;

//----------------------------------------------------------------------


// IMPORTS


import java.lang.invoke.MethodHandles;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

import java.util.function.Predicate;

import java.util.stream.Stream;

import javafx.geometry.Dimension2D;
import javafx.geometry.HPos;
import javafx.geometry.Insets;
import javafx.geometry.Pos;

import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.MenuItem;
import javafx.scene.control.TextField;

import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCodeCombination;
import javafx.scene.input.KeyCombination;
import javafx.scene.input.KeyEvent;

import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;

import javafx.stage.Window;

import uk.blankaspect.common.basictree.ListNode;
import uk.blankaspect.common.basictree.MapNode;

import uk.blankaspect.common.exception2.UnexpectedRuntimeException;

import uk.blankaspect.common.function.IProcedure0;
import uk.blankaspect.common.function.IProcedure1;

import uk.blankaspect.common.matcher.SimpleWildcardPathnameMatcher;
import uk.blankaspect.common.matcher.SimpleWildcardPatternMatcher;

import uk.blankaspect.common.message.MessageConstants;

import uk.blankaspect.common.string.StringUtils;

import uk.blankaspect.ui.jfx.button.Buttons;
import uk.blankaspect.ui.jfx.button.ImageDataButton;

import uk.blankaspect.ui.jfx.combobox.SimpleComboBox;

import uk.blankaspect.ui.jfx.dialog.ConfirmationDialog;
import uk.blankaspect.ui.jfx.dialog.DialogState;
import uk.blankaspect.ui.jfx.dialog.SimpleModalDialog;
import uk.blankaspect.ui.jfx.dialog.SimpleModelessDialog;

import uk.blankaspect.ui.jfx.image.MessageIcon32;

import uk.blankaspect.ui.jfx.spinner.CollectionSpinner;

import uk.blankaspect.ui.jfx.tableview.TableViewEditor;

import uk.blankaspect.ui.jfx.text.TextUtils;

import uk.blankaspect.ui.jfx.tooltip.TooltipDecorator;

//----------------------------------------------------------------------


// CLASS: FILTER DIALOG


public class FilterDialog
	extends SimpleModelessDialog
{

////////////////////////////////////////////////////////////////////////
//  Constants
////////////////////////////////////////////////////////////////////////

	/** The horizontal gap between adjacent columns of the control pane. */
	private static final	double	CONTROL_PANE_H_GAP	= 6.0;

	/** The vertical gap between adjacent rows of the control pane. */
	private static final	double	CONTROL_PANE_V_GAP	= 6.0;

	/** The padding around the control pane. */
	private static final	Insets	CONTROL_PANE_PADDING	= new Insets(2.0, 4.0, 2.0, 4.0);

	private static final	int		PATTERN_FIELD_NUM_COLUMNS	= 36;

	private static final	KeyCombination	KEY_COMBO_OWNER_WINDOW	=
			new KeyCodeCombination(KeyCode.TAB, KeyCombination.CONTROL_DOWN);

	// Miscellaneous strings
	private static final	String	ENTRY_FILTER_STR	= "Zip-file entry filter";
	private static final	String	PATTERN_STR			= "Pattern";
	private static final	String	SHOW_LIST_STR		= "Show list (Ctrl+Space in field)";
	private static final	String	FILTER_STR			= "filter";
	private static final	String	ADD_FILTER_STR		= "Add filter to list";
	private static final	String	EDIT_FILTERS_STR	= "Edit list of filters";
	private static final	String	SCOPE_STR			= "Scope";
	private static final	String	CLEAR_STR			= "Clear";

////////////////////////////////////////////////////////////////////////
//  Class variables
////////////////////////////////////////////////////////////////////////

	private static	State	state	= new State();

////////////////////////////////////////////////////////////////////////
//  Instance variables
////////////////////////////////////////////////////////////////////////

	private	SimpleComboBox<Filter>		filterComboBox;
	private	CollectionSpinner<Scope>	scopeSpinner;
	private	boolean						updatingFilter;

////////////////////////////////////////////////////////////////////////
//  Constructors
////////////////////////////////////////////////////////////////////////

	public FilterDialog(
		Window	owner)
	{
		// Call superclass constructor
		super(owner, ENTRY_FILTER_STR, state.locator(), state.getSize());

		// Set properties
		setResizable(true);

		// Create control pane
		GridPane controlPane = new GridPane();
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

		// Create procedure to apply filter to table view
		IProcedure1<Filter> applyFilter = filter ->
		{
			// Create zip-entry filter
			String pattern = (filter == null) ? null : filter.pattern;
			Predicate<ZipFileEntry> zipFilter = StringUtils.isNullOrBlank(pattern) ? null : switch (filter.scope)
			{
				case FILENAME  ->
						entry -> SimpleWildcardPatternMatcher.allIgnoreCase(pattern).match(entry.getFilename());
				case DIRECTORY ->
						entry -> SimpleWildcardPathnameMatcher.ignoreCase(pattern).match(entry.getDirectoryPathname());
				case PATHNAME  ->
						entry -> SimpleWildcardPathnameMatcher.ignoreCase(pattern).match(entry.getPathname());
			};

			// Set zip-entry filter on table view
			UnzipApp.instance().getTableView().setFilter(zipFilter);
		};

		// Create combo box: filter
		filterComboBox = new SimpleComboBox<>(new SimpleComboBox.IConverter<>()
		{
			@Override
			public String toText(
				Filter	filter)
			{
				return (filter == null) ? null : filter.pattern;
			}

			@Override
			public Filter fromText(
				String	pattern)
			{
				return StringUtils.isNullOrBlank(pattern) ? null : new Filter(pattern, scopeSpinner.getItem(), false);
			}

			@Override
			public Filter copy(
				Filter	filter)
			{
				return (filter == null) ? null : filter.clone();
			}
		},
		state.filters);
		filterComboBox.allowNullCommit(true);
		filterComboBox.commitOnFocusLost(UnzipApp.instance().getPreferences().isComboBoxCommitOnFocusLost());
		filterComboBox.setMaxWidth(Double.MAX_VALUE);
		filterComboBox.textField().setPrefColumnCount(PATTERN_FIELD_NUM_COLUMNS);
		filterComboBox.valueProperty().addListener((observable, oldFilter, filter) ->
		{
			// Prevent updates
			updatingFilter = true;

			// Update scope spinner
			if (filter != null)
				scopeSpinner.setItem(filter.scope);

			// Apply filter to table view
			applyFilter.invoke(filter);

			// Allow updates
			updatingFilter = false;
		});
		TooltipDecorator.addTooltip(filterComboBox.button(), SHOW_LIST_STR);
		HBox.setHgrow(filterComboBox, Priority.ALWAYS);

		// Create button: add filter to list
		ImageDataButton addFilterButton = new ImageDataButton(Images.ImageId.PLUS_SIGN, ADD_FILTER_STR);
		HBox.setMargin(addFilterButton, new Insets(0.0, 0.0, 0.0, 4.0));

		// Create procedure to update 'add filter to list' button
		IProcedure0 updateAddFilterButton = () ->
				addFilterButton.setDisable(StringUtils.isNullOrBlank(filterComboBox.text()));

		// Handle action on 'add filter to list' button
		addFilterButton.setOnAction(event ->
		{
			// Get pattern from combo box
			String pattern = filterComboBox.text();

			// If there is a pattern, create filter and add it to list of items of combo box
			if (!StringUtils.isNullOrBlank(pattern))
			{
				// Ask whether to save filter
				Boolean save = Utils.askSaveBeyondSession(this, ADD_FILTER_STR, FILTER_STR);

				// If not cancelled, add filter to list
				if (save != null)
				{
					Filter filter = new Filter(pattern, scopeSpinner.getItem(), save);
					List<Filter> items = new ArrayList<>(filterComboBox.items());
					int index = items.indexOf(filter);
					if (index < 0)
						items.add(filter);
					else
						items.set(index, filter);
					filterComboBox.items(items);
					filterComboBox.value(filter.clone());
				}
			}
		});

		// Update 'add filter to list' button when content of combo-box editor changes
		filterComboBox.textField().textProperty().addListener(observable -> updateAddFilterButton.invoke());

		// Create button: edit list of filters
		ImageDataButton editFiltersButton = new ImageDataButton(Images.ImageId.PENCIL, EDIT_FILTERS_STR);
		editFiltersButton.setOnAction(event ->
		{
			Filter filter = filterComboBox.value();
			List<Filter> filters = new FilterListDialog(this, filterComboBox.items()).showDialog();
			if (filters != null)
			{
				filterComboBox.items(filters);
				if (!filters.isEmpty())
				{
					int index = (filter == null) ? -1 : filters.indexOf(filter);
					if (index < 0)
						filterComboBox.value(null);
					else
						filterComboBox.selectIndex(index);
				}
			}
		});

		// Update 'add filter to list' button
		updateAddFilterButton.invoke();

		// Create filter pane
		HBox filterPane = new HBox(2.0, filterComboBox, addFilterButton, editFiltersButton);
		filterPane.setAlignment(Pos.CENTER_LEFT);
		controlPane.addRow(row++, new Label(PATTERN_STR), filterPane);

		// Create spinner: scope
		scopeSpinner = CollectionSpinner.leftRightH(HPos.CENTER, true, Scope.class, null, null, null);
		scopeSpinner.itemProperty().addListener((observable, oldScope, scope) ->
		{
			// If filter is being updated, stop
			if (updatingFilter)
				return;

			// Set value of filter combo box to content of text field
			filterComboBox.commitValue();

			// Set scope on filter
			Filter filter = filterComboBox.value();
			if (filter != null)
				filter.scope = scope;

			// Apply filter to table view
			applyFilter.invoke(filterComboBox.value());
		});
		controlPane.addRow(row++, new Label(SCOPE_STR), scopeSpinner);

		// Add control pane to content pane
		addContent(controlPane);

		// Create button: clear
		Button clearButton = Buttons.hNoShrink(CLEAR_STR);
		clearButton.getProperties().put(BUTTON_GROUP_KEY, BUTTON_GROUP1);
		clearButton.setOnAction(event -> clearFilter());
		addButton(clearButton, HPos.LEFT);

		// Create button: close
		Button closeButton = Buttons.hNoShrink(CLOSE_STR);
		closeButton.getProperties().put(BUTTON_GROUP_KEY, BUTTON_GROUP1);
		closeButton.setOnAction(event -> requestClose());
		addButton(closeButton, HPos.RIGHT);

		// Fire 'cancel' button if Escape key is pressed
		setKeyFireButton(closeButton, null);

		// Update images of image buttons
		ImageDataButton.updateButtons(getScene());

		// When dialog is shown, set saved filter on combo box and scope spinner
		setOnShown(event ->
		{
			Filter filter = state.filter;
			if (filter != null)
				filterComboBox.setTextAndCommit(filter.pattern);
			scopeSpinner.setItem((filter == null) ? Scope.FILENAME : filter.scope);
		});

		// Save dialog state when dialog is closed
		setOnHiding(event ->
		{
			// Save state
			state.restoreAndUpdate(this, true);
			state.filter = filterComboBox.value();
			state.filters.clear();
			state.filters.addAll(filterComboBox.items());

			// Clear filter
			clearFilter();
		});

		// If Ctrl+Tab is pressed, request focus on owner window
		getScene().addEventFilter(KeyEvent.KEY_PRESSED, event ->
		{
			if (KEY_COMBO_OWNER_WINDOW.match(event))
			{
				// Request focus on owner window
				owner.requestFocus();

				// Consume event
				event.consume();
			}
		});

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
		MapNode	rootNode)
	{
		state.decodeTree(rootNode);
	}

	//------------------------------------------------------------------

////////////////////////////////////////////////////////////////////////
//  Instance methods : overriding methods
////////////////////////////////////////////////////////////////////////

	/**
	 * Prevents the height of this dialog from changing.
	 */

	@Override
	protected void onWindowShown()
	{
		// Call superclass method
		super.onWindowShown();

		// Prevent height of window from changing
		setMinHeight(prefHeight());
		setMaxHeight(prefHeight());
	}

	//------------------------------------------------------------------

////////////////////////////////////////////////////////////////////////
//  Instance methods
////////////////////////////////////////////////////////////////////////

	public void clearFilter()
	{
		filterComboBox.value(null);
	}

	//------------------------------------------------------------------

////////////////////////////////////////////////////////////////////////
//  Enumerated types
////////////////////////////////////////////////////////////////////////


	// ENUMERATION: FILTER SCOPE


	private enum Scope
	{

	////////////////////////////////////////////////////////////////////
	//  Constants
	////////////////////////////////////////////////////////////////////

		DIRECTORY
		(
			"Directory"
		),

		FILENAME
		(
			"Filename"
		),

		PATHNAME
		(
			"Full pathname"
		);

	////////////////////////////////////////////////////////////////////
	//  Instance variables
	////////////////////////////////////////////////////////////////////

		private	String	key;
		private	String	text;

	////////////////////////////////////////////////////////////////////
	//  Constructors
	////////////////////////////////////////////////////////////////////

		private Scope(
			String	text)
		{
			// Initialise instance variables
			key = StringUtils.toCamelCase(name());
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

	}

	//==================================================================

////////////////////////////////////////////////////////////////////////
//  Member classes : non-inner classes
////////////////////////////////////////////////////////////////////////


	// CLASS: FILTER


	private static class Filter
		implements IPersistable, Cloneable
	{

	////////////////////////////////////////////////////////////////////
	//  Instance variables
	////////////////////////////////////////////////////////////////////

		private	String	pattern;
		private	Scope	scope;
		private	boolean	persistent;

	////////////////////////////////////////////////////////////////////
	//  Constructors
	////////////////////////////////////////////////////////////////////

		private Filter(
			String	pattern,
			Scope	scope,
			boolean	persistent)
		{
			// Initialise instance variables
			this.pattern = pattern;
			this.scope = scope;
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
				case 0  -> pattern;
				case 1  -> scope.text;
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

			return (obj instanceof Filter other) && Objects.equals(pattern, other.pattern) && (scope == other.scope);
		}

		//--------------------------------------------------------------

		/**
		 * {@inheritDoc}
		 */

		@Override
		public int hashCode()
		{
			return 31 * Objects.hashCode(pattern) + Objects.hashCode(scope);
		}

		//--------------------------------------------------------------

		/**
		 * {@inheritDoc}
		 */

		@Override
		public Filter clone()
		{
			try
			{
				return (Filter)super.clone();
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
		extends DialogState
	{

	////////////////////////////////////////////////////////////////////
	//  Constants
	////////////////////////////////////////////////////////////////////

		/** Keys of properties. */
		private interface PropertyKey
		{
			String	FILTERS	= "filters";
			String	PATTERN	= "pattern";
			String	SCOPE	= "scope";
		}

	////////////////////////////////////////////////////////////////////
	//  Instance variables
	////////////////////////////////////////////////////////////////////

		private Filter			filter;
		private List<Filter>	filters;

	////////////////////////////////////////////////////////////////////
	//  Constructors
	////////////////////////////////////////////////////////////////////

		private State()
		{
			// Call superclass constructor
			super(true, true);

			// Initialise instance variables
			filters = new ArrayList<>();
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

			// Encode filters
			if (filters.stream().anyMatch(filter -> filter.persistent))
			{
				// Encode filters
				ListNode filtersNode = rootNode.addList(PropertyKey.FILTERS);
				for (int i = 0; i < filters.size(); i++)
				{
					Filter filter = filters.get(i);
					if (filter.persistent)
					{
						MapNode filterNode = new MapNode();
						filterNode.addString(PropertyKey.PATTERN, filter.pattern);
						filterNode.addString(PropertyKey.SCOPE, filter.scope.key);

						filtersNode.add(filterNode);
					}
				}
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

			// Decode filters
			String key = PropertyKey.FILTERS;
			if (rootNode.hasList(key))
			{
				// Clear filters
				filters.clear();

				// Decode filters
				for (MapNode node : rootNode.getListNode(key).mapNodes())
				{
					key = PropertyKey.PATTERN;
					if (node.hasString(key))
					{
						Scope scope =
								node.getEnumValue(Scope.class, PropertyKey.SCOPE, value -> value.key, Scope.FILENAME);
						filters.add(new Filter(node.getString(key), scope, true));
					}
				}
			}
		}

		//--------------------------------------------------------------

	}

	//==================================================================


	// CLASS: DIALOG FOR EDITING A LIST OF FILTERS


	private static class FilterListDialog
		extends SimpleModalDialog<List<Filter>>
	{

	////////////////////////////////////////////////////////////////////
	//  Constants
	////////////////////////////////////////////////////////////////////

		private static final	double	PATTERN_COLUMN_WIDTH_FACTOR	= 20.0;

		private static final	double	TABLE_VIEW_HEIGHT	= 240.0;

		private static final	Insets	EDITOR_BUTTON_PANE_PADDING	= new Insets(2.0, 6.0, 2.0, 0.0);

		private static final	Insets	CONTENT_PANE_PADDING	= new Insets(2.0);

		private static final	String	EDIT_NAME	= "filter";

		// Miscellaneous strings
		private static final	String	REMOVE_FILTER_STR		= "Remove filter";
		private static final	String	REMOVE_QUESTION_STR		=
				"Filter: %s" + MessageConstants.LABEL_SEPARATOR + "Do you want to remove the selected filter?";
		private static final	String	REMOVE_STR				= "Remove";
		private static final	String	SAVE_SELECTED_STR		= "Save selected filters";
		private static final	String	DONT_SAVE_SELECTED_STR	= "Don't save selected filters";

	////////////////////////////////////////////////////////////////////
	//  Instance variables
	////////////////////////////////////////////////////////////////////

		/** The result of this dialog. */
		private	List<Filter>	result;

	////////////////////////////////////////////////////////////////////
	//  Constructors
	////////////////////////////////////////////////////////////////////

		private FilterListDialog(
			Window			owner,
			List<Filter>	filters)
		{
			// Call superclass constructor
			super(owner, MethodHandles.lookup().lookupClass().getCanonicalName(), EDIT_FILTERS_STR);

			// Set properties
			setResizable(true);

			// Create column information for table view
			List<PersistableItemTableView.ColumnInfo> columnInfos = List.of(
				new PersistableItemTableView.ColumnInfo(PATTERN_STR,
														TextUtils.textHeightCeil(PATTERN_COLUMN_WIDTH_FACTOR)),
				new PersistableItemTableView.ColumnInfo(
						SCOPE_STR,
						TextUtils.maxWidthCeil(Arrays.stream(Scope.values()).map(Scope::toString).toList()))
			);

			// Create table view
			PersistableItemTableView<Filter> tableView = new PersistableItemTableView<>(columnInfos);
			tableView.setPrefHeight(TABLE_VIEW_HEIGHT);
			tableView.setItems(filters.stream().map(filter -> filter.clone()).toList());
			tableView.setMenuItemFactory(save ->
			{
				MenuItem menuItem = new MenuItem(save ? SAVE_SELECTED_STR : DONT_SAVE_SELECTED_STR);
				Stream<Filter> selectedItems = tableView.getSelectionModel().getSelectedItems().stream();
				menuItem.setDisable(save ? selectedItems.allMatch(item -> item.persistent)
										 : selectedItems.noneMatch(item -> item.persistent));
				menuItem.setOnAction(event ->
				{
					// Update 'persistent' flag of selected filters
					for (Filter filter : tableView.getSelectionModel().getSelectedItems())
						filter.persistent = save;

					// Redraw table view
					tableView.refresh();
				});
				return menuItem;
			});

			// Create table-view editor
			Window window = this;
			TableViewEditor.IEditor<Filter> editor0 = new TableViewEditor.IEditor<>()
			{
				@Override
				public Filter edit(
					TableViewEditor.Action	action,
					Filter					target)
				{
					return new EditFilterDialog(window, action + " " + EDIT_NAME, target).showDialog();
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
					Filter	filter)
				{
					return ConfirmationDialog.show(window, REMOVE_FILTER_STR, MessageIcon32.QUESTION.get(),
												   String.format(REMOVE_QUESTION_STR, filter.pattern), REMOVE_STR);
				}
			};
			TableViewEditor<Filter> editor = new TableViewEditor<>(tableView, editor0, false)
			{
				@Override
				public List<Filter> getItems()
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

			// Apply sheet to scene
			applyStyleSheet();
		}

		//--------------------------------------------------------------

	////////////////////////////////////////////////////////////////////
	//  Instance methods : overriding methods
	////////////////////////////////////////////////////////////////////

		@Override
		protected List<Filter> getResult()
		{
			return result;
		}

		//--------------------------------------------------------------

	}

	//==================================================================


	// CLASS: DIALOG FOR EDITING A FILTER


	private static class EditFilterDialog
		extends SimpleModalDialog<Filter>
	{

	////////////////////////////////////////////////////////////////////
	//  Constants
	////////////////////////////////////////////////////////////////////

		/** The horizontal gap between adjacent columns of the control pane. */
		private static final	double	CONTROL_PANE_H_GAP	= 6.0;

		/** The vertical gap between adjacent rows of the control pane. */
		private static final	double	CONTROL_PANE_V_GAP	= 6.0;

		// Miscellaneous strings
		private static final	String	SAVE_BEYOND_SESSION_STR	= "Save the filter beyond the current session";

	////////////////////////////////////////////////////////////////////
	//  Instance variables
	////////////////////////////////////////////////////////////////////

		/** The result of this dialog. */
		private	Filter	result;

	////////////////////////////////////////////////////////////////////
	//  Constructors
	////////////////////////////////////////////////////////////////////

		private EditFilterDialog(
			Window	owner,
			String	title,
			Filter	filter)
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

			// Create text field: pattern
			TextField patternField = new TextField((filter == null) ? null : filter.pattern);
			patternField.setPrefColumnCount(PATTERN_FIELD_NUM_COLUMNS);
			controlPane.addRow(row++, new Label(PATTERN_STR), patternField);

			// Create spinner: scope
			CollectionSpinner<Scope> scopeSpinner =
					CollectionSpinner.leftRightH(HPos.CENTER, true, Scope.class,
												 (filter == null) ? Scope.FILENAME : filter.scope, null, null);
			controlPane.addRow(row++, new Label(SCOPE_STR), scopeSpinner);

			// Create check box: persistent
			CheckBox persistentCheckBox = new CheckBox(SAVE_BEYOND_SESSION_STR);
			persistentCheckBox.setSelected((filter != null) && filter.persistent);
			GridPane.setMargin(persistentCheckBox, new Insets(2.0, 0.0, 2.0, 0.0));
			controlPane.add(persistentCheckBox, 1, row);

			// Add control pane to content pane
			addContent(controlPane);

			// Create button: OK
			Button okButton = Buttons.hNoShrink(OK_STR);
			okButton.getProperties().put(BUTTON_GROUP_KEY, BUTTON_GROUP1);
			okButton.setOnAction(event ->
			{
				result = new Filter(patternField.getText(), scopeSpinner.getItem(), persistentCheckBox.isSelected());
				requestClose();
			});
			addButton(okButton, HPos.RIGHT);

			// Create procedure to update 'OK' button
			IProcedure0 updateOkButton = () ->
					okButton.setDisable(StringUtils.isNullOrBlank(patternField.getText()));

			// Update 'OK' button when pattern changes
			patternField.textProperty().addListener(observable -> updateOkButton.invoke());

			// Fire 'OK' button if Enter key is pressed in pattern field
			patternField.setOnAction(event -> okButton.fire());

			// Update 'OK' button
			updateOkButton.invoke();

			// Create button: cancel
			Button cancelButton = Buttons.hNoShrink(CANCEL_STR);
			cancelButton.getProperties().put(BUTTON_GROUP_KEY, BUTTON_GROUP1);
			cancelButton.setOnAction(event -> requestClose());
			addButton(cancelButton, HPos.RIGHT);

			// Fire 'cancel' button if Escape key is pressed; fire 'OK' button if Ctrl+Enter is pressed
			setKeyFireButton(cancelButton, okButton);

			// Apply sheet to scene
			applyStyleSheet();
		}

		//--------------------------------------------------------------

	////////////////////////////////////////////////////////////////////
	//  Instance methods : overriding methods
	////////////////////////////////////////////////////////////////////

		@Override
		protected Filter getResult()
		{
			return result;
		}

		//--------------------------------------------------------------

	}

	//==================================================================

}

//----------------------------------------------------------------------
