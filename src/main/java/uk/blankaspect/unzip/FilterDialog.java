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
import java.util.List;

import java.util.function.Predicate;

import javafx.collections.FXCollections;

import javafx.geometry.Dimension2D;
import javafx.geometry.HPos;
import javafx.geometry.Insets;
import javafx.geometry.Point2D;
import javafx.geometry.Pos;

import javafx.scene.control.Button;
import javafx.scene.control.Label;

import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCodeCombination;
import javafx.scene.input.KeyCombination;
import javafx.scene.input.KeyEvent;

import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;

import javafx.stage.Window;

import uk.blankaspect.common.basictree.ListNode;
import uk.blankaspect.common.basictree.MapNode;
import uk.blankaspect.common.basictree.StringNode;

import uk.blankaspect.common.function.IProcedure0;

import uk.blankaspect.common.matcher.SimpleWildcardPathnameMatcher;
import uk.blankaspect.common.matcher.SimpleWildcardPatternMatcher;

import uk.blankaspect.common.string.StringUtils;

import uk.blankaspect.ui.jfx.button.ImageButton;

import uk.blankaspect.ui.jfx.combobox.SimpleComboBox;

import uk.blankaspect.ui.jfx.dialog.ConfirmationDialog;
import uk.blankaspect.ui.jfx.dialog.MessageDialog;
import uk.blankaspect.ui.jfx.dialog.SimpleModalDialog;
import uk.blankaspect.ui.jfx.dialog.SimpleModelessDialog;
import uk.blankaspect.ui.jfx.dialog.SingleTextFieldDialog;

import uk.blankaspect.ui.jfx.image.MessageIcon32;

import uk.blankaspect.ui.jfx.listview.ListViewEditor;
import uk.blankaspect.ui.jfx.listview.ListViewStyle;
import uk.blankaspect.ui.jfx.listview.SimpleTextListView;

import uk.blankaspect.ui.jfx.spinner.CollectionSpinner;

import uk.blankaspect.ui.jfx.style.StyleManager;

import uk.blankaspect.ui.jfx.tooltip.TooltipDecorator;

import uk.blankaspect.ui.jfx.window.WindowState;
import uk.blankaspect.ui.jfx.window.WindowUtils;

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
	private static final	String	SHOW_PATTERNS_STR	= "Show patterns (Ctrl+Space in field)";
	private static final	String	ADD_PATTERN_STR		= "Add pattern to list";
	private static final	String	EDIT_PATTERNS_STR	= "Edit list of patterns";
	private static final	String	SCOPE_STR			= "Scope";
	private static final	String	CLEAR_STR			= "Clear";

////////////////////////////////////////////////////////////////////////
//  Class variables
////////////////////////////////////////////////////////////////////////

	private static	State	state	= new State();

////////////////////////////////////////////////////////////////////////
//  Instance variables
////////////////////////////////////////////////////////////////////////

	private	SimpleComboBox<String>		patternComboBox;
	private	CollectionSpinner<Scope>	scopeSpinner;

////////////////////////////////////////////////////////////////////////
//  Static initialiser
////////////////////////////////////////////////////////////////////////

	static
	{
		// Register the style properties of dependencies of this class with the style manager
		StyleManager.INSTANCE.registerDependencies(ListViewStyle.class);
	}

////////////////////////////////////////////////////////////////////////
//  Constructors
////////////////////////////////////////////////////////////////////////

	public FilterDialog(
		Window	owner)
	{
		// Call superclass constructor
		super(owner, ENTRY_FILTER_STR, state.getLocator(), state.getSize());

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
		column.setMinWidth(GridPane.USE_PREF_SIZE);
		column.setHalignment(HPos.RIGHT);
		column.setHgrow(Priority.NEVER);
		controlPane.getColumnConstraints().add(column);

		column = new ColumnConstraints();
		column.setHalignment(HPos.LEFT);
		column.setHgrow(Priority.ALWAYS);
		controlPane.getColumnConstraints().add(column);

		// Initialise row index
		int row = 0;

		// Create procedure to update filter
		IProcedure0 updateFilter = () ->
		{
			// Create filter from pattern
			String pattern = patternComboBox.getValue();
			Predicate<ZipFileEntry> filter = StringUtils.isNullOrBlank(pattern)
														? null
														: switch (scopeSpinner.getItem())
			{
				case FILENAME      ->
						entry -> SimpleWildcardPatternMatcher.allIgnoreCase(pattern).match(entry.getFilename());
				case DIRECTORY     ->
						entry -> SimpleWildcardPathnameMatcher.ignoreCase(pattern).match(entry.getDirectoryPathname());
				case FULL_PATHNAME ->
						entry -> SimpleWildcardPathnameMatcher.ignoreCase(pattern).match(entry.getPathname());
			};

			// Set filter on table view
			UnzipApp.instance().getTableView().setFilter(filter);
		};

		// Create combo box: pattern
		patternComboBox = new SimpleComboBox<>(SimpleComboBox.IDENTITY_STRING_CONVERTER, state.patterns);
		patternComboBox.setMaxWidth(Double.MAX_VALUE);
		patternComboBox.getTextField().setPrefColumnCount(PATTERN_FIELD_NUM_COLUMNS);
		patternComboBox.valueProperty().addListener(observable -> updateFilter.invoke());
		TooltipDecorator.addTooltip(patternComboBox, SHOW_PATTERNS_STR);
		HBox.setHgrow(patternComboBox, Priority.ALWAYS);

		// Create button: add pattern to list
		ImageButton addPatternButton = new ImageButton(Images.PLUS_SIGN, ADD_PATTERN_STR);
		addPatternButton.setOnAction(event ->
		{
			String text = patternComboBox.getText();
			if (!StringUtils.isNullOrBlank(text))
			{
				List<String> items = new ArrayList<>(patternComboBox.getItems());
				items.remove(text);
				items.add(0, text);
				patternComboBox.setItems(items);
				patternComboBox.setValue(text);
			}
		});
		HBox.setMargin(addPatternButton, new Insets(0.0, 0.0, 0.0, 4.0));

		// Create button: edit list of patterns
		ImageButton editPatternsButton = new ImageButton(Images.PENCIL, EDIT_PATTERNS_STR);
		editPatternsButton.setOnAction(event ->
		{
			String pattern = patternComboBox.getValue();
			List<String> patterns = new EditPatternsDialog(this, patternComboBox.getItems()).showDialog();
			if (patterns != null)
			{
				patternComboBox.setItems(patterns);
				if (!patterns.isEmpty())
				{
					int index = (pattern == null) ? -1 : patterns.indexOf(pattern);
					if (index < 0)
						patternComboBox.setValue(null);
					else
						patternComboBox.selectIndex(index);
				}
			}
		});

		// Create pattern pane
		HBox patternPane = new HBox(2.0, patternComboBox, addPatternButton, editPatternsButton);
		patternPane.setAlignment(Pos.CENTER_LEFT);
		controlPane.addRow(row++, new Label(PATTERN_STR), patternPane);

		// Create spinner: scope
		scopeSpinner = CollectionSpinner.leftRightH(HPos.CENTER, true, Scope.class, state.scope, null, null);
		scopeSpinner.itemProperty().addListener(observable -> updateFilter.invoke());
		controlPane.addRow(row++, new Label(SCOPE_STR), scopeSpinner);

		// Add control pane to content pane
		addContent(controlPane);

		// Create button: clear
		Button clearButton = new Button(CLEAR_STR);
		clearButton.getProperties().put(BUTTON_GROUP_KEY, BUTTON_GROUP1);
		clearButton.setOnAction(event -> clearFilter());
		addButton(clearButton, HPos.LEFT);

		// Update filter
		updateFilter.invoke();

		// Create button: close
		Button closeButton = new Button(CLOSE_STR);
		closeButton.getProperties().put(BUTTON_GROUP_KEY, BUTTON_GROUP1);
		closeButton.setOnAction(event -> requestClose());
		addButton(closeButton, HPos.RIGHT);

		// Fire 'cancel' button if Escape key is pressed
		setKeyFireButton(closeButton, null);

		// Update images of image buttons
		Images.updateImageButtons(getScene());

		// When dialog is shown, prevent its height from changing; set saved pattern on text field of combo box
		setOnShown(event ->
		{
			// Prevent height of dialog from changing
			WindowUtils.preventHeightChange(this);

			// Set saved pattern on text field of combo box
			patternComboBox.setText(state.pattern);
		});

		// Save dialog state when dialog is closed
		setOnHiding(event ->
		{
			// Save state
			state.restoreAndUpdate(this, true);
			state.pattern = patternComboBox.getText();
			state.patterns.clear();
			state.patterns.addAll(patternComboBox.getItems());
			state.patternIndex = patternComboBox.getValueIndex();
			state.scope = scopeSpinner.getItem();

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

////////////////////////////////////////////////////////////////////////
//  Instance methods
////////////////////////////////////////////////////////////////////////

	public void clearFilter()
	{
		patternComboBox.setValue(null);
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

		FULL_PATHNAME
		(
			"Full pathname"
		);

	////////////////////////////////////////////////////////////////////
	//  Instance variables
	////////////////////////////////////////////////////////////////////

		private	String	text;

	////////////////////////////////////////////////////////////////////
	//  Constructors
	////////////////////////////////////////////////////////////////////

		private Scope(
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

	}

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
			String	PATTERN_INDEX	= "patternIndex";
			String	PATTERNS		= "patterns";
		}

	////////////////////////////////////////////////////////////////////
	//  Instance variables
	////////////////////////////////////////////////////////////////////

		private String			pattern;
		private List<String>	patterns;
		private int				patternIndex;
		private	Scope			scope;

	////////////////////////////////////////////////////////////////////
	//  Constructors
	////////////////////////////////////////////////////////////////////

		private State()
		{
			// Call superclass constructor
			super(true, true);

			// Initialise instance variables
			patterns = new ArrayList<>();
			patternIndex = -1;
			scope = Scope.FILENAME;
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

			// Encode patterns and pattern index
			if (!patterns.isEmpty())
			{
				// Encode patterns
				ListNode patternsNode = rootNode.addList(PropertyKey.PATTERNS);
				for (String pattern : patterns)
					patternsNode.addString(pattern);

				// Encode pattern index
				if (patternIndex >= 0)
					rootNode.addInt(PropertyKey.PATTERN_INDEX, patternIndex);
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

			// Decode patterns
			String key = PropertyKey.PATTERNS;
			if (rootNode.hasList(key))
			{
				patterns.clear();
				for (StringNode node : rootNode.getListNode(key).stringNodes())
					patterns.add(node.getValue());
			}

			// Decode pattern index
			patternIndex = rootNode.getInt(PropertyKey.PATTERN_INDEX, -1);
		}

		//--------------------------------------------------------------

	////////////////////////////////////////////////////////////////////
	//  Instance methods
	////////////////////////////////////////////////////////////////////

		/**
		 * Returns a locator function that returns the location from this dialog state.
		 *
		 * @return a locator function that returns the location from this dialog state, or {@code null} if the location
		 *         is {@code null}.
		 */

		private ILocator getLocator()
		{
			Point2D location = getLocation();
			return (location == null) ? null : (width, height) -> location;
		}

		//--------------------------------------------------------------

	}

	//==================================================================


	// CLASS: 'EDIT PATTERNS' DIALOG


	private static class EditPatternsDialog
		extends SimpleModalDialog<List<String>>
	{

	////////////////////////////////////////////////////////////////////
	//  Constants
	////////////////////////////////////////////////////////////////////

		private static final	double	LIST_VIEW_WIDTH		= 520.0;
		private static final	double	LIST_VIEW_HEIGHT	= 240.0;

		private static final	Insets	EDITOR_BUTTON_PANE_PADDING	= new Insets(2.0, 6.0, 2.0, 0.0);

		private static final	Insets	CONTENT_PANE_PADDING	= new Insets(2.0);

		private static final	String	EDIT_NAME	= "pattern";
		private static final	String	EDIT_LABEL	= "Pattern";

		private static final	String	REMOVE_PATTERN_STR	= "Remove pattern";
		private static final	String	REMOVE_QUESTION_STR	= "Pattern: %s" + MessageDialog.MESSAGE_SEPARATOR
																+ "Do you want to remove the selected pattern?";
		private static final	String	REMOVE_STR			= "Remove";

	////////////////////////////////////////////////////////////////////
	//  Instance variables
	////////////////////////////////////////////////////////////////////

		private	List<String>	result;

	////////////////////////////////////////////////////////////////////
	//  Constructors
	////////////////////////////////////////////////////////////////////

		private EditPatternsDialog(
			Window			owner,
			List<String>	patterns)
		{
			// Call superclass constructor
			super(owner, MethodHandles.lookup().lookupClass().getCanonicalName(), EDIT_PATTERNS_STR);

			// Set properties
			setResizable(true);

			// Create list view
			SimpleTextListView<String> listView =
					new SimpleTextListView<>(FXCollections.observableArrayList(patterns), null);
			listView.setPrefSize(LIST_VIEW_WIDTH, LIST_VIEW_HEIGHT);

			// Create list-view editor
			Window window = this;
			ListViewEditor<String> editor = new ListViewEditor<>(listView, new ListViewEditor.IEditor<>()
			{
				@Override
				public String edit(
					ListViewEditor.Action	action,
					String					target)
				{
					return SingleTextFieldDialog.show(window, MethodHandles.lookup().lookupClass().getCanonicalName(),
													  action + " " + EDIT_NAME, EDIT_LABEL, target, null,
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
					String	pathname)
				{
					return ConfirmationDialog.show(window, REMOVE_PATTERN_STR, MessageIcon32.QUESTION.get(),
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
