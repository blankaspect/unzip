/*====================================================================*\

ZipFileTableView.java

Class: zip-file table view.

\*====================================================================*/


// PACKAGE


package uk.blankaspect.unzip;

//----------------------------------------------------------------------


// IMPORTS


import java.lang.invoke.MethodHandles;

import java.time.LocalDateTime;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.EnumMap;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;

import java.util.function.Predicate;

import javafx.application.Platform;

import javafx.beans.InvalidationListener;

import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.beans.property.SimpleSetProperty;

import javafx.collections.FXCollections;

import javafx.css.PseudoClass;

import javafx.geometry.HPos;
import javafx.geometry.Insets;
import javafx.geometry.Point2D;
import javafx.geometry.Pos;
import javafx.geometry.Side;
import javafx.geometry.VPos;

import javafx.scene.Node;
import javafx.scene.Parent;

import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ContentDisplay;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.Label;
import javafx.scene.control.Labeled;
import javafx.scene.control.MenuItem;
import javafx.scene.control.OverrunStyle;
import javafx.scene.control.SelectionMode;
import javafx.scene.control.SeparatorMenuItem;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableRow;
import javafx.scene.control.TableView;

import javafx.scene.image.Image;

import javafx.scene.input.ContextMenuEvent;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCodeCombination;
import javafx.scene.input.KeyCombination;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;

import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;

import javafx.scene.paint.Color;

import javafx.stage.Window;

import uk.blankaspect.common.css.CssRuleSet;
import uk.blankaspect.common.css.CssSelector;

import uk.blankaspect.common.exception2.BaseException;
import uk.blankaspect.common.exception2.LocationException;

import uk.blankaspect.common.function.IFunction0;
import uk.blankaspect.common.function.IFunction1;
import uk.blankaspect.common.function.IFunction2;
import uk.blankaspect.common.function.IProcedure0;

import uk.blankaspect.common.geometry.VHPos;

import uk.blankaspect.common.logging.Logger;

import uk.blankaspect.common.string.StringUtils;

import uk.blankaspect.common.text.Tabulator;

import uk.blankaspect.ui.jfx.button.Buttons;

import uk.blankaspect.ui.jfx.clipboard.ClipboardUtils;

import uk.blankaspect.ui.jfx.colour.ColourUtils;

import uk.blankaspect.ui.jfx.container.LabelTitledPane;
import uk.blankaspect.ui.jfx.container.PropertiesPane;

import uk.blankaspect.ui.jfx.dialog.ErrorDialog;
import uk.blankaspect.ui.jfx.dialog.SimpleModalDialog;

import uk.blankaspect.ui.jfx.font.Fonts;

import uk.blankaspect.ui.jfx.image.HatchedImageFactory;

import uk.blankaspect.ui.jfx.label.Labels;

import uk.blankaspect.ui.jfx.math.FxGeomUtils;

import uk.blankaspect.ui.jfx.popup.CellPopUpManager;
import uk.blankaspect.ui.jfx.popup.LabelPopUpManager;
import uk.blankaspect.ui.jfx.popup.PopUpUtils;

import uk.blankaspect.ui.jfx.scene.SceneUtils;

import uk.blankaspect.ui.jfx.spinner.CollectionSpinner;

import uk.blankaspect.ui.jfx.style.ColourProperty;
import uk.blankaspect.ui.jfx.style.DataUriImageMap;
import uk.blankaspect.ui.jfx.style.FxProperty;
import uk.blankaspect.ui.jfx.style.FxStyleClass;
import uk.blankaspect.ui.jfx.style.RuleSetBuilder;
import uk.blankaspect.ui.jfx.style.StyleConstants;
import uk.blankaspect.ui.jfx.style.StyleManager;
import uk.blankaspect.ui.jfx.style.StyleSelector;

import uk.blankaspect.ui.jfx.tableview.ElasticFilteredList;
import uk.blankaspect.ui.jfx.tableview.TableViewStyle;

import uk.blankaspect.ui.jfx.text.TextUtils;

//----------------------------------------------------------------------


// CLASS: ZIP-FILE TABLE VIEW


public class ZipFileTableView
	extends TableView<ZipFileEntry>
{

////////////////////////////////////////////////////////////////////////
//  Constants
////////////////////////////////////////////////////////////////////////

	/** The default delay (in milliseconds) before a pop-up for a header cell is displayed after it is activated. */
	public static final		int		DEFAULT_HEADER_CELL_POP_UP_DELAY	= 1500;

	/** The extra width of the table to allow for the vertical scroll bar. */
	private static final	double	EXTRA_WIDTH	= 17.0;

	/** The padding around the label of a header cell. */
	private static final	Insets	HEADER_CELL_LABEL_PADDING	= new Insets(3.0, 4.0, 3.0, 4.0);

	/** The padding around the pop-up for a header cell. */
	private static final	Insets	HEADER_CELL_POP_UP_PADDING	= new Insets(2.0, 6.0, 2.0, 6.0);

	/** The width of the header image. */
	private static final	int		HEADER_IMAGE_WIDTH	= 120;

	/** The height of the header image. */
	private static final	int		HEADER_IMAGE_HEIGHT	= 60;

	/** The spacing between adjacent hatching lines of the header image. */
	private static final	double	HEADER_IMAGE_SPACING	= 3.0;

	/** The factor by which the size of the default font is multiplied to give the size of the font of the placeholder
		label. */
	private static final	double	PLACEHOLDER_LABEL_FONT_SIZE_FACTOR	= 1.25;

	/** The maximum initial width of the properties dialog. */
	private static final	double	PROPERTIES_DIALOG_MAX_INITIAL_WIDTH	= 960.0;

	/** The key that is used when storing the location of a zip-file entry properties dialog. */
	private static final	String	ZIP_ENTRY_PROPERTIES_KEY	= "zipEntryProperties";

	/** The key combination that invokes the editor on the selected item. */
	private static final	KeyCombination	KEY_COMBO_EDIT_ITEM	=
			new KeyCodeCombination(KeyCode.ENTER, KeyCombination.CONTROL_DOWN);

	/** Miscellaneous strings. */
	private static final	String	ELLIPSIS_STR		= "...";
	private static final	String	NO_ENTRIES_STR		= "No entries";
	private static final	String	EDIT_STR			= "Edit";
	private static final	String	SELECT_EDITOR_STR	= "Select editor";
	private static final	String	PROPERTIES_STR		= "Properties";
	private static final	String	COPY_ENTRY_TEXT_STR	= "Copy text of selected entries";
	private static final	String	PATHNAME_STR		= "Pathname";

	/** The pseudo-class that is associated with the <i>filtered</i> state. */
	private static final	PseudoClass	FILTERED_PSEUDO_CLASS	= PseudoClass.getPseudoClass(PseudoClassKey.FILTERED);

	/** CSS colour properties. */
	private static final	List<ColourProperty>	COLOUR_PROPERTIES	= List.of
	(
		ColourProperty.of
		(
			FxProperty.BORDER_COLOUR,
			ColourKey.HEADER_CELL_BORDER,
			CssSelector.builder()
					.cls(StyleClass.ZIP_FILE_TABLE_VIEW)
					.desc(FxStyleClass.COLUMN_HEADER)
					.build(),
			CssSelector.builder()
					.cls(StyleClass.ZIP_FILE_TABLE_VIEW)
					.desc(FxStyleClass.FILLER)
					.build()
		),
		ColourProperty.of
		(
			FxProperty.BACKGROUND_COLOUR,
			TableViewStyle.ColourKey.CELL_BACKGROUND_EMPTY,
			CssSelector.builder()
					.cls(StyleClass.ZIP_FILE_TABLE_VIEW)
					.desc(StyleClass.PLACEHOLDER_LABEL)
					.build()
		),
		ColourProperty.of
		(
			FxProperty.TEXT_FILL,
			ColourKey.PLACEHOLDER_TEXT,
			CssSelector.builder()
					.cls(StyleClass.ZIP_FILE_TABLE_VIEW)
					.desc(StyleClass.PLACEHOLDER_LABEL)
					.build()
		)
	);

	/** CSS style classes. */
	private interface StyleClass
	{
		String	ZIP_FILE_TABLE_VIEW	= StyleConstants.APP_CLASS_PREFIX + "zip-file-table-view";

		String	PLACEHOLDER_LABEL	= StyleConstants.CLASS_PREFIX + "placeholder-label";
		String	CRC_CELL			= StyleConstants.CLASS_PREFIX + "crc-cell";
	}

	/** Keys of CSS pseudo-classes. */
	private interface PseudoClassKey
	{
		String	FILTERED	= "filtered";
	}

	/** Keys of colours that are used in colour properties. */
	private interface ColourKey
	{
		String	PREFIX	= StyleManager.colourKeyPrefix(MethodHandles.lookup().lookupClass().getEnclosingClass());

		String	HEADER_CELL_BACKGROUND1				= PREFIX + "header.cell.background1";
		String	HEADER_CELL_BACKGROUND2				= PREFIX + "header.cell.background2";
		String	HEADER_CELL_BACKGROUND1_FILTERED	= PREFIX + "header.cell.background1.filtered";
		String	HEADER_CELL_BACKGROUND2_FILTERED	= PREFIX + "header.cell.background2.filtered";
		String	HEADER_CELL_BORDER					= PREFIX + "header.cell.border";
		String	PLACEHOLDER_TEXT					= PREFIX + "placeholder.text";
	}

	/** Keys of images that are used in CSS rule sets. */
	private interface ImageKey
	{
		String	HEADER_CELL_BACKGROUND			= ColourKey.HEADER_CELL_BACKGROUND1;
		String	HEADER_CELL_BACKGROUND_FILTERED	= ColourKey.HEADER_CELL_BACKGROUND1_FILTERED;
	}

////////////////////////////////////////////////////////////////////////
//  Class variables
////////////////////////////////////////////////////////////////////////

	/** The delay (in milliseconds) before a pop-up for a header cell is displayed after it is activated. */
	private static	int	headerCellPopUpDelay	= DEFAULT_HEADER_CELL_POP_UP_DELAY;

////////////////////////////////////////////////////////////////////////
//  Instance variables
////////////////////////////////////////////////////////////////////////

	/** The manager of the pop-up windows that are displayed for the cells of this table view. */
	private	CellPopUpManager					cellPopUpManager;

	/** A list of the items that are represented in this table view. */
	private	ElasticFilteredList<ZipFileEntry>	itemList;

	/** A list of the cells of this table view. */
	private	List<Cell<?>>						cells;

	/** The manager of pop-ups for the header cells of this table view. */
	private	LabelPopUpManager					headerPopUpManager;

	/** Flag: if {@code true}, the header of this table view has been initialised. */
	private	boolean								headerInitialised;

	// WORKAROUND for a bug in JavaFX: isFocused() sometimes returns false when the table view has focus
	/** Flag: if {@code true}, this table view has keyboard focus. */
	private	boolean								focused;

////////////////////////////////////////////////////////////////////////
//  Static initialiser
////////////////////////////////////////////////////////////////////////

	static
	{
		// Load default colours of this class
		try
		{
			StyleManager.INSTANCE.loadDefaultColours(ZipFileTableView.class);
		}
		catch (LocationException e)
		{
			Logger.INSTANCE.error(e);
		}
	}

////////////////////////////////////////////////////////////////////////
//  Constructors
////////////////////////////////////////////////////////////////////////

	public ZipFileTableView()
	{
		// Initialise instance variables
		cellPopUpManager = new CellPopUpManager(Cell.POP_UP_DELAY);
		itemList = new ElasticFilteredList<>(this);
		cells = new ArrayList<>();

		// Set properties
		getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
		getStyleClass().addAll(StyleClass.ZIP_FILE_TABLE_VIEW, TableViewStyle.StyleClass.TABLE_VIEW);

		// Create procedure to create rule sets
		IFunction0<List<CssRuleSet>> createRuleSets = () ->
		{
			// Create hatched images and add them to data-scheme URI map
			try
			{
				// Create function to create hatched image
				IFunction2<Image, String, String> createImage = (colour1Key, colour2Key) ->
				{
					return HatchedImageFactory.diagonal(HEADER_IMAGE_WIDTH, HEADER_IMAGE_HEIGHT, HEADER_IMAGE_SPACING,
														getColour(colour1Key), getColour(colour2Key));
				};

				// Create hatched images and add them to data-scheme URI map
				DataUriImageMap.INSTANCE.put(ImageKey.HEADER_CELL_BACKGROUND,
											 createImage.invoke(ColourKey.HEADER_CELL_BACKGROUND1,
																ColourKey.HEADER_CELL_BACKGROUND2));
				DataUriImageMap.INSTANCE.put(ImageKey.HEADER_CELL_BACKGROUND_FILTERED,
											 createImage.invoke(ColourKey.HEADER_CELL_BACKGROUND1_FILTERED,
																ColourKey.HEADER_CELL_BACKGROUND2_FILTERED));
			}
			catch (BaseException e)
			{
				Logger.INSTANCE.error(e);
			}

			// Create and return CSS rule sets
			return List.of
			(
				RuleSetBuilder.create()
						.selector(CssSelector.builder()
								.cls(StyleClass.ZIP_FILE_TABLE_VIEW)
								.desc(FxStyleClass.COLUMN_HEADER)
								.build())
						.selector(CssSelector.builder()
								.cls(StyleClass.ZIP_FILE_TABLE_VIEW)
								.desc(FxStyleClass.FILLER)
								.build())
						.repeatingImageBackground(ImageKey.HEADER_CELL_BACKGROUND)
						.build(),
				RuleSetBuilder.create()
						.selector(CssSelector.builder()
								.cls(StyleClass.ZIP_FILE_TABLE_VIEW)
								.desc(FxStyleClass.COLUMN_HEADER).pseudo(PseudoClassKey.FILTERED)
								.build())
						.selector(CssSelector.builder()
								.cls(StyleClass.ZIP_FILE_TABLE_VIEW)
								.desc(FxStyleClass.FILLER).pseudo(PseudoClassKey.FILTERED)
								.build())
						.repeatingImageBackground(ImageKey.HEADER_CELL_BACKGROUND_FILTERED)
						.build(),
				RuleSetBuilder.create()
						.selector(CssSelector.builder()
								.cls(StyleClass.ZIP_FILE_TABLE_VIEW)
								.desc(FxStyleClass.COLUMN_HEADER)
								.build())
						.borders(Side.RIGHT, Side.BOTTOM)
						.build(),
				RuleSetBuilder.create()
						.selector(CssSelector.builder()
								.cls(StyleClass.ZIP_FILE_TABLE_VIEW)
								.desc(FxStyleClass.FILLER)
								.build())
						.borders(Side.BOTTOM)
						.build(),
				RuleSetBuilder.create()
						.selector(CssSelector.builder()
								.cls(StyleClass.ZIP_FILE_TABLE_VIEW)
								.desc(StyleClass.CRC_CELL)
								.desc(FxStyleClass.TEXT)
								.build())
						.grayFontSmoothing()
						.build()
			);
		};

		// Register the style properties of this class and its dependencies with the style manager
		StyleManager.INSTANCE.register(getClass(), COLOUR_PROPERTIES, createRuleSets.invoke(), TableViewStyle.class);

		// Update rule sets when theme changes
		StyleManager.INSTANCE.themeProperty().addListener(observable ->
				StyleManager.INSTANCE.updateRuleSets(getClass(), createRuleSets.invoke()));

		// Set height of cell
		updateCellHeight();

		// Create columns
		double width = EXTRA_WIDTH;
		for (Column column : Column.values())
		{
			// Create table column
			TableColumn<ZipFileEntry, ?> tableColumn = column.createColumn(this);

			// Set properties of table column
			tableColumn.setId(column.getKey());
			tableColumn.setPrefWidth(column.prefWidth);

			// Add column to list
			getColumns().add(tableColumn);

			// Increment width
			width += column.prefWidth;
		}

		// Set preferred width
		setPrefWidth(width);

		// Set row factory
		if (StyleManager.INSTANCE.notUsingStyleSheet())
		{
			setRowFactory(table ->
			{
				TableRow<ZipFileEntry> row = new TableRow<>();
				row.setBackground(SceneUtils.createColouredBackground(
						getColour(TableViewStyle.ColourKey.CELL_BACKGROUND_EMPTY)));
				return row;
			});
		}

		// Set placeholder
		Label placeholderLabel = Labels.expansive(NO_ENTRIES_STR, PLACEHOLDER_LABEL_FONT_SIZE_FACTOR,
												  getColour(ColourKey.PLACEHOLDER_TEXT),
												  getColour(TableViewStyle.ColourKey.CELL_BACKGROUND_EMPTY));
		placeholderLabel.getStyleClass().add(StyleClass.PLACEHOLDER_LABEL);
		setPlaceholder(placeholderLabel);

		// Update cell backgrounds on change of state
		if (StyleManager.INSTANCE.notUsingStyleSheet())
		{
			// Update cell backgrounds when selection changes
			getSelectionModel().getSelectedIndices().addListener((InvalidationListener) observable ->
					updateCellBackgrounds());

			// Update cell backgrounds when focus changes
// WORKAROUND : see 'focused' instance variable
//			focusedProperty().addListener(observable -> updateCellBackgrounds());
			focusedProperty().addListener((observable, oldFocused, newFocused) ->
			{
				focused = newFocused;
				updateCellBackgrounds();
			});

			// Update cell backgrounds when focused row changes
			getFocusModel().focusedIndexProperty().addListener(observable -> updateCellBackgrounds());
		}

		// Ensure cells are redrawn if scroll bar is hidden
		widthProperty().addListener(observable -> Platform.runLater(this::refresh));

		// Edit selected file if Ctrl+Enter is pressed when a single item is selected
		addEventHandler(KeyEvent.KEY_PRESSED, event ->
		{
			if (KEY_COMBO_EDIT_ITEM.match(event) && isSingleSelection())
				UnzipApp.instance().editFile(getSelectionModel().getSelectedItem(), false);
		});

		// Display context menu in response to request
		addEventHandler(ContextMenuEvent.CONTEXT_MENU_REQUESTED, event ->
		{
			// Create context menu
			ContextMenu menu = new ContextMenu();

			// Add menu item: extract
			MenuItem menuItem = UnzipApp.instance().createMenuItemExtract();
			menuItem.setDisable(getSelectionModel().isEmpty());
			menu.getItems().add(menuItem);

			// Add separator
			menu.getItems().add(new SeparatorMenuItem());

			// Add menu item: edit
			menuItem = new MenuItem(EDIT_STR);
			menuItem.setDisable(!isSingleSelection());
			menuItem.setOnAction(event0 -> UnzipApp.instance().editFile(getSelectionModel().getSelectedItem(), false));
			menu.getItems().add(menuItem);

			// Add menu item: select editor
			menuItem = new MenuItem(SELECT_EDITOR_STR);
			menuItem.setDisable(!isSingleSelection());
			menuItem.setOnAction(event0 -> UnzipApp.instance().editFile(getSelectionModel().getSelectedItem(), true));
			menu.getItems().add(menuItem);

			// Add separator
			menu.getItems().add(new SeparatorMenuItem());

			// Add menu item: properties
			menuItem = new MenuItem(PROPERTIES_STR);
			menuItem.setDisable(!isSingleSelection());
			menuItem.setOnAction(event0 -> onShowProperties());
			menu.getItems().add(menuItem);

			// Add separator
			menu.getItems().add(new SeparatorMenuItem());

			// Add menu item: copy entry text
			menuItem = new MenuItem(COPY_ENTRY_TEXT_STR + ELLIPSIS_STR);
			menuItem.setDisable(getSelectionModel().isEmpty());
			menuItem.setOnAction(event0 -> onCopyEntryText());
			menu.getItems().add(menuItem);

			// Display context menu
			if (!menu.getItems().isEmpty())
				menu.show(getWindow(), event.getScreenX(), event.getScreenY());
		});
	}

	//------------------------------------------------------------------

////////////////////////////////////////////////////////////////////////
//  Class methods
////////////////////////////////////////////////////////////////////////

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
	protected void layoutChildren()
	{
		// Call superclass method
		super.layoutChildren();

		// Create function to create background colour
		IFunction2<Color, String, String> createColour = (colour1Key, colour2Key) ->
				ColourUtils.interpolateRgb(getColour(colour1Key), getColour(colour2Key), 0.5);

		// Get colours of background and border of column headers
		boolean filtered = (itemList.getFilter() != null);
		Color backgroundColour = filtered ? createColour.invoke(ColourKey.HEADER_CELL_BACKGROUND1_FILTERED,
																ColourKey.HEADER_CELL_BACKGROUND2_FILTERED)
										  : createColour.invoke(ColourKey.HEADER_CELL_BACKGROUND1,
																ColourKey.HEADER_CELL_BACKGROUND2);
		Color borderColour = getColour(ColourKey.HEADER_CELL_BORDER);

		// Set background and border of column headers
		for (Node node : lookupAll(StyleSelector.COLUMN_HEADER))
		{
			if (node instanceof Region header)
			{
				if (StyleManager.INSTANCE.notUsingStyleSheet())
				{
					header.setBackground(SceneUtils.createColouredBackground(backgroundColour));
					header.setBorder(SceneUtils.createSolidBorder(borderColour, Side.RIGHT, Side.BOTTOM));
				}
				header.pseudoClassStateChanged(FILTERED_PSEUDO_CLASS, filtered);
			}
		}

		// Set background and border of filler
		if (lookup(StyleSelector.FILLER) instanceof Region filler)
		{
			if (StyleManager.INSTANCE.notUsingStyleSheet())
			{
				filler.setBackground(SceneUtils.createColouredBackground(backgroundColour));
				filler.setBorder(SceneUtils.createSolidBorder(borderColour, Side.BOTTOM));
			}
			filler.pseudoClassStateChanged(FILTERED_PSEUDO_CLASS, filtered);
		}

		// Set alignment and padding of header labels
		if (!headerInitialised)
		{
			for (Node node : lookupAll(StyleSelector.COLUMN_HEADER_LABEL))
			{
				if (node instanceof Labeled label)
				{
					// Get column
					Parent columnHeader = node.getParent();
					Column column = Column.forKey(columnHeader.getId());

					// Get alignment of column and insets of column header
					HPos hAlignment = column.hAlignment;
					Insets insets = (columnHeader instanceof Region region) ? region.getInsets() : Insets.EMPTY;

					// Set properties of label
					label.setAlignment(FxGeomUtils.getPos(VPos.CENTER, hAlignment));
					label.setPadding(HEADER_CELL_LABEL_PADDING);
					label.setTextFill(getColour(TableViewStyle.ColourKey.CELL_TEXT));
					label.getStyleClass().add(TableViewStyle.StyleClass.CELL_LABEL);

					// Create pop-up manager for label
					if (headerPopUpManager == null)
					{
						headerPopUpManager = new LabelPopUpManager((text, graphic) ->
						{
							Label popUpLabel = new Label(text, graphic);
							popUpLabel.setPadding(HEADER_CELL_POP_UP_PADDING);
							popUpLabel.setBackground(SceneUtils.createColouredBackground(
									getColour(TableViewStyle.ColourKey.CELL_POPUP_BACKGROUND)));
							popUpLabel.setBorder(SceneUtils.createSolidBorder(
									getColour(TableViewStyle.ColourKey.CELL_POPUP_BORDER)));
							popUpLabel.getStyleClass().add(TableViewStyle.StyleClass.CELL_POPUP_LABEL);
							return popUpLabel;
						});
						headerPopUpManager.setDelay(headerCellPopUpDelay);
					}

					// Create pop-up for label
					VHPos.H hPos = switch (hAlignment)
					{
						case LEFT   -> VHPos.H.LEFT;
						case CENTER -> VHPos.H.CENTRE;
						case RIGHT  -> VHPos.H.RIGHT;
					};
					double x = switch (hAlignment)
					{
						case LEFT   -> (insets == null) ? 0.0 : -(insets.getLeft() + 1.0);
						case CENTER -> 0.0;
						case RIGHT  -> (insets == null) ? 0.0 : insets.getRight();
					};
					PopUpUtils.createPopUp(headerPopUpManager, label, VHPos.of(VHPos.V.TOP, hPos),
										   VHPos.of(VHPos.V.BOTTOM, hPos), x, 0.0, () -> column.tooltipText, null);
				}

				// Prevent reinitialisation of header
				headerInitialised = true;
			}
		}
	}

	//------------------------------------------------------------------

////////////////////////////////////////////////////////////////////////
//  Instance methods
////////////////////////////////////////////////////////////////////////

	public void setZipFile(
		ZipFileModel	zipFile)
	{
		//

		// Update items
		itemList.update((zipFile == null) ? Collections.emptyList() : zipFile.getEntries());

		// Redraw cells
		refresh();

		// Display first item
		scrollTo(0);
	}

	//------------------------------------------------------------------

	public void setColumnWidths(
		Map<String, Double>	columnWidths)
	{
		for (TableColumn<ZipFileEntry, ?> column : getColumns())
		{
			String id = column.getId();
			if (columnWidths.containsKey(id))
				column.setPrefWidth(columnWidths.get(id));
		}
	}

	//------------------------------------------------------------------

	public void setFilter(
		Predicate<ZipFileEntry>	filter)
	{
		// Set filter on list of items
		itemList.setFilter((filter == null) ? null : filter);
	}

	//------------------------------------------------------------------

	public void setHeaderCellPopUpDelay(
		int	delay)
	{
		// Update class variable
		headerCellPopUpDelay = (delay > 0) ? delay : -1;

		// Update pop-up manager
		headerPopUpManager.setDelay(headerCellPopUpDelay);
	}

	//------------------------------------------------------------------

	public void updateCellHeight()
	{
		double height = Math.max(TextUtils.textHeightCeil(), TextUtils.textHeightCeil(Fonts.monoFont()));
		height += 2.0 * (double)UnzipApp.instance().getPreferences().getCellVerticalPadding() + 1.0;
		setFixedCellSize(height);
	}

	//------------------------------------------------------------------

	private Window getWindow()
	{
		return SceneUtils.getWindow(this);
	}

	//------------------------------------------------------------------

	private boolean isSingleSelection()
	{
		return (getSelectionModel().getSelectedIndices().size() == 1);
	}

	//------------------------------------------------------------------

	private void updateCellBackgrounds()
	{
		for (Cell<?> cell : cells)
			cell.updateBackground();
	}

	//------------------------------------------------------------------

	private void onShowProperties()
	{
		ZipFileEntry entry = getSelectionModel().getSelectedItem();
		if (entry != null)
		{
			PropertiesPane.create()
					.padding(new Insets(2.0))
					.nameConverter(name ->
							name.chars().allMatch(ch -> Character.isUpperCase(ch)) ? name : name.toLowerCase())
					.valueLabelHasContextMenu(true)
					.properties1(entry.getProperties())
					.dialogMaxInitialWidth(PROPERTIES_DIALOG_MAX_INITIAL_WIDTH)
					.showModalDialog(getWindow(), ZIP_ENTRY_PROPERTIES_KEY, PROPERTIES_STR);
		}
	}

	//------------------------------------------------------------------

	private void onCopyEntryText()
	{
		// Display dialog for selecting columns and field separator
		CopyEntryTextDialog.State result = new CopyEntryTextDialog(COPY_ENTRY_TEXT_STR).showDialog();
		if (result == null)
			return;

		// Get columns as list
		List<Column> columns = new ArrayList<>(result.columns);
		if (result.combineDirectoryFilename)
			columns.remove(Column.FILENAME);

		// Create function to test whether to combine directory and filename
		IFunction1<Boolean, Column> combineDirectoryFilename = column ->
				(column == Column.DIRECTORY) && result.combineDirectoryFilename;

		// Get number of columns
		int numColumns = columns.size();

		// Initialise output text
		String outText = null;

		// Convert entries to text
		switch (result.fieldSeparator)
		{
			case SPACES:
			{
				// Create array of flags for right-aligned columns
				boolean[] rightAligned = new boolean[numColumns];
				for (int i = 0; i < numColumns; i++)
					rightAligned[i] = (columns.get(i).hAlignment == HPos.RIGHT);

				// Create array of gaps between columns
				int[] gaps = new int[numColumns - 1];
				Arrays.fill(gaps, 2);

				// Create list of rows of fields
				List<String[]> rows = new ArrayList<>();
				int numEntries = getSelectionModel().getSelectedItems().size();
				for (int i = result.includeHeader ? -1 : 0; i < numEntries; i++)
				{
					String[] fields = new String[numColumns];
					for (int j = 0; j < numColumns; j++)
					{
						Column column = columns.get(j);
						String text = null;
						if (i < 0)
							text = combineDirectoryFilename.invoke(column) ? PATHNAME_STR : column.text;
						else
						{
							ZipFileEntry entry = getSelectionModel().getSelectedItems().get(i);
							text = column.getValueString(entry);
							if (combineDirectoryFilename.invoke(column))
								text += ZipFileEntry.SEPARATOR_CHAR + Column.FILENAME.getValueString(entry);
						}
						fields[j] = text;
					}
					rows.add(fields);
				}

				// Tabulate rows
				Tabulator.Result table = Tabulator.tabulate(numColumns, rightAligned, gaps, rows);
				outText = table.text();
				if (result.includeHeader)
				{
					int index = outText.indexOf('\n') + 1;
					outText = outText.substring(0, index) + "-".repeat(table.maxLineLength()) + "\n"
								+ outText.substring(index);
				}
				break;
			}

			case TAB:
			{
				// Initialise buffer
				StringBuilder buffer = new StringBuilder(1024);

				// Append column headers
				if (result.includeHeader)
				{
					for (int i = 0; i < numColumns; i++)
					{
						if (i > 0)
							buffer.append('\t');

						Column column = columns.get(i);
						buffer.append(combineDirectoryFilename.invoke(column) ? PATHNAME_STR : column.text);
					}
					buffer.append('\n');
				}

				// Append entries
				for (ZipFileEntry entry : getSelectionModel().getSelectedItems())
				{
					for (int i = 0; i < numColumns; i++)
					{
						if (i > 0)
							buffer.append('\t');

						Column column = columns.get(i);
						String text = column.getValueString(entry);
						if (combineDirectoryFilename.invoke(column))
							text += ZipFileEntry.SEPARATOR_CHAR + Column.FILENAME.getValueString(entry);
						buffer.append(text);
					}
					buffer.append('\n');
				}

				// Set result
				outText = buffer.toString();
				break;
			}
		}

		// Put text on system clipboard
		try
		{
			ClipboardUtils.putTextThrow(outText);
		}
		catch (BaseException e)
		{
			ErrorDialog.show(getWindow(), COPY_ENTRY_TEXT_STR, e);
		}
	}

	//------------------------------------------------------------------

////////////////////////////////////////////////////////////////////////
//  Enumerated types
////////////////////////////////////////////////////////////////////////


	// ENUMERATION: COLUMN OF TABLE VIEW


	private enum Column
	{

	////////////////////////////////////////////////////////////////////
	//  Constants
	////////////////////////////////////////////////////////////////////

		DIRECTORY
		(
			"Directory",
			null,
			"Parent directory",
			HPos.LEFT,
			TextUtils.textHeightCeil(20.0)
		)
		{
			@Override
			protected String getValueString(
				ZipFileEntry	entry)
			{
				return entry.getDirectoryPathname();
			}

			//----------------------------------------------------------

			@Override
			protected TableColumn<ZipFileEntry, String> createColumn(
				ZipFileTableView	tableView)
			{
				TableColumn<ZipFileEntry, String> column = new TableColumn<>(toString());
				column.setCellFactory(column0 -> tableView.new TextCell(this));
				column.setCellValueFactory(features ->
						new ReadOnlyObjectWrapper<>(getValueString(features.getValue())));
				column.setComparator(ZipFileEntry.DIRECTORY_PATHNAME_COMPARATOR);
				return column;
			}

			//----------------------------------------------------------
		},

		FILENAME
		(
			"Filename",
			null,
			null,
			HPos.LEFT,
			TextUtils.textHeightCeil(15.0)
		)
		{
			@Override
			protected String getValueString(
				ZipFileEntry	entry)
			{
				return entry.getFilename();
			}

			//----------------------------------------------------------

			@Override
			protected TableColumn<ZipFileEntry, String> createColumn(
				ZipFileTableView	tableView)
			{
				TableColumn<ZipFileEntry, String> column = new TableColumn<>(toString());
				column.setCellFactory(column0 -> tableView.new TextCell(this));
				column.setCellValueFactory(features ->
						new ReadOnlyObjectWrapper<>(getValueString(features.getValue())));
				return column;
			}

			//----------------------------------------------------------
		},

		TIMESTAMP
		(
			"Timestamp",
			null,
			null,
			HPos.LEFT,
			TextUtils.textWidth(Constants.TIMESTAMP_FORMATTER.format(LocalDateTime.now()))
		)
		{
			@Override
			protected String getValueString(
				ZipFileEntry	entry)
			{
				LocalDateTime dateTime = entry.getDateTime();
				return (dateTime == null) ? "" : Constants.TIMESTAMP_FORMATTER.format(dateTime);
			}

			//----------------------------------------------------------

			@Override
			protected TableColumn<ZipFileEntry, LocalDateTime> createColumn(
				ZipFileTableView	tableView)
			{
				TableColumn<ZipFileEntry, LocalDateTime> column = new TableColumn<>(toString());
				column.setCellFactory(column0 -> tableView.new TimestampCell(this));
				column.setCellValueFactory(features -> new ReadOnlyObjectWrapper<>(features.getValue().getDateTime()));
				return column;
			}

			//----------------------------------------------------------
		},

		SIZE
		(
			"Size",
			null,
			"Original size",
			HPos.RIGHT,
			TextUtils.textWidth(Utils.formatDecimal(0xFF_FFFF_FFFFL))
		)
		{
			@Override
			protected String getValueString(
				ZipFileEntry	entry)
			{
				long size = entry.getSize();
				return (size < 0) ? "" : Utils.formatDecimal(size);
			}

			//----------------------------------------------------------

			@Override
			protected TableColumn<ZipFileEntry, Long> createColumn(
				ZipFileTableView	tableView)
			{
				TableColumn<ZipFileEntry, Long> column = new TableColumn<>(toString());
				column.setCellFactory(column0 -> tableView.new SizeCell(this));
				column.setCellValueFactory(features ->
				{
					long size = features.getValue().getSize();
					return new ReadOnlyObjectWrapper<>((size < 0) ? null : Long.valueOf(size));
				});
				return column;
			}

			//----------------------------------------------------------
		},

		COMPRESSED_SIZE
		(
			"Comp size",
			"Compressed size",
			"Compressed size",
			HPos.RIGHT,
			TextUtils.textWidth(Utils.formatDecimal(0xFF_FFFF_FFFFL))
		)
		{
			@Override
			protected String getValueString(
				ZipFileEntry	entry)
			{
				long size = entry.getCompressedSize();
				return (size < 0) ? "" : Utils.formatDecimal(size);
			}

			//----------------------------------------------------------

			@Override
			protected TableColumn<ZipFileEntry, Long> createColumn(
				ZipFileTableView	tableView)
			{
				TableColumn<ZipFileEntry, Long> column = new TableColumn<>(toString());
				column.setCellFactory(column0 -> tableView.new SizeCell(this));
				column.setCellValueFactory(features ->
				{
					long size = features.getValue().getCompressedSize();
					return new ReadOnlyObjectWrapper<>((size < 0) ? null : Long.valueOf(size));
				});
				return column;
			}

			//----------------------------------------------------------
		},

		CRC
		(
			"CRC",
			null,
			"32-bit cyclic redundancy check",
			HPos.RIGHT,
			TextUtils.textWidth(Fonts.monoFont(), "D".repeat(8))
		)
		{
			@Override
			protected String getValueString(
				ZipFileEntry	entry)
			{
				long crc = entry.getCrc();
				return (crc < 0) ? "" : Utils.crcToString(crc);
			}

			//----------------------------------------------------------

			@Override
			protected TableColumn<ZipFileEntry, Long> createColumn(
				ZipFileTableView	tableView)
			{
				TableColumn<ZipFileEntry, Long> column = new TableColumn<>(toString());
				column.setCellFactory(column0 -> tableView.new CrcCell(this));
				column.setCellValueFactory(features ->
				{
					long crc = features.getValue().getCrc();
					return new ReadOnlyObjectWrapper<>((crc < 0) ? null : Long.valueOf(crc));
				});
				return column;
			}

			//----------------------------------------------------------
		};

	////////////////////////////////////////////////////////////////////
	//  Instance variables
	////////////////////////////////////////////////////////////////////

		private	String	text;
		private	String	longText;
		private	String	tooltipText;
		private	HPos	hAlignment;
		private	double	prefWidth;

	////////////////////////////////////////////////////////////////////
	//  Constructors
	////////////////////////////////////////////////////////////////////

		private Column(
			String	text,
			String	longText,
			String	tooltipText,
			HPos	hAlignment,
			double	textWidth)
		{
			// Initialise instance variables
			this.text = text;
			this.longText = (longText == null) ? text : longText;
			this.tooltipText = (tooltipText == null) ? text : tooltipText;
			this.hAlignment = hAlignment;
			prefWidth = Math.ceil(textWidth + 2.0 * Cell.LABEL_H_PADDING + 1.0);
		}

		//--------------------------------------------------------------

	////////////////////////////////////////////////////////////////////
	//  Class methods
	////////////////////////////////////////////////////////////////////

		/**
		 * Returns the column that is associated with the specified key.
		 *
		 * @param  key
		 *          the key whose associated column is required.
		 * @return the column that is associated with {@code key}, or {@code null} if there is no such column.
		 */

		protected static Column forKey(
			String	key)
		{
			return Arrays.stream(values()).filter(value -> value.getKey().equals(key)).findFirst().orElse(null);
		}

		//--------------------------------------------------------------

	////////////////////////////////////////////////////////////////////
	//  Abstract methods
	////////////////////////////////////////////////////////////////////

		protected abstract String getValueString(
			ZipFileEntry	entry);

		//--------------------------------------------------------------

		protected abstract TableColumn<ZipFileEntry, ?> createColumn(
			ZipFileTableView	tableView);

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

		private String getKey()
		{
			return StringUtils.toCamelCase(name());
		}

		//--------------------------------------------------------------

	}

	//==================================================================

////////////////////////////////////////////////////////////////////////
//  Member classes : inner classes
////////////////////////////////////////////////////////////////////////


	// CLASS: CELL


	private abstract class Cell<T>
		extends TableCell<ZipFileEntry, T>
		implements CellPopUpManager.ICell<T>
	{

	////////////////////////////////////////////////////////////////////
	//  Constants
	////////////////////////////////////////////////////////////////////

		/** The horizontal padding of the label of a cell. */
		private static final	double	LABEL_H_PADDING	= 6.0;

		/** The delay (in milliseconds) before a pop-up for a cell is displayed after it is activated. */
		private static final	int		POP_UP_DELAY	= 500;

	////////////////////////////////////////////////////////////////////
	//  Instance variables
	////////////////////////////////////////////////////////////////////

		private	Column	column;

	////////////////////////////////////////////////////////////////////
	//  Constructors
	////////////////////////////////////////////////////////////////////

		private Cell(
			Column	column)
		{
			// Initialise instance variables
			this.column = column;

			// Set properties
			setContentDisplay(ContentDisplay.GRAPHIC_ONLY);
			setAlignment(FxGeomUtils.getPos(VPos.CENTER, column.hAlignment));
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
			addEventHandler(MouseEvent.MOUSE_EXITED, event ->
			{
				if (CellPopUpManager.deactivatePopUpOnMouseExited())
					cellPopUpManager.deactivate();
			});

			// When a mouse button is released, deactivate any cell pop-up
			addEventFilter(MouseEvent.MOUSE_RELEASED, event ->
			{
				if (cellPopUpManager.deactivate())
					event.consume();
			});

			// If mouse is double-clicked on zip-file entry, edit entry
			addEventHandler(MouseEvent.MOUSE_CLICKED, event ->
			{
				if ((event.getButton() == MouseButton.PRIMARY) && (event.getClickCount() == 2))
				{
					List<ZipFileEntry> entries = getItems();
					int index = getIndex();
					if ((index >= 0) && (index < entries.size()))
						UnzipApp.instance().editFile(entries.get(index), false);
				}
			});
		}

		//--------------------------------------------------------------

	////////////////////////////////////////////////////////////////////
	//  Instance methods : CellPopUpManager.ICell interface
	////////////////////////////////////////////////////////////////////

		/**
		 * {@inheritDoc}
		 */

		@Override
		public String getIdentifier()
		{
			return (getItem() == null) ? null : getIndex() + ":" + column.getKey();
		}

		//--------------------------------------------------------------

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
						getColour(TableViewStyle.ColourKey.CELL_POPUP_BACKGROUND)));
				label.setBorder(SceneUtils.createSolidBorder(getColour(TableViewStyle.ColourKey.CELL_POPUP_BORDER)));
				label.getStyleClass().add(TableViewStyle.StyleClass.CELL_POPUP_LABEL);
			}

			// Return label
			return label;
		}

		//--------------------------------------------------------------

		/**
		 * {@inheritDoc}
		 */

		@Override
		public Point2D getPrefPopUpLocation(
			Node	content)
		{
			VHPos pos = switch (getAlignment().getHpos())
			{
				case LEFT   -> VHPos.CENTRE_LEFT;
				case CENTER -> VHPos.CENTRE_CENTRE;
				case RIGHT  -> VHPos.CENTRE_RIGHT;
			};
			double x = switch (getAlignment().getHpos())
			{
				case LEFT   -> -1.0;
				case CENTER -> 0.0;
				case RIGHT  -> 1.0;
			};
			Node node = getGraphic();
			return (node == null) ? null
								  : PopUpUtils.createLocator(node, pos, pos, x, 0.0)
												.getLocation(content.getLayoutBounds(), null);
		}

		//--------------------------------------------------------------

		/**
		 * {@inheritDoc}
		 */

		@Override
		public Window getWindow()
		{
			return ZipFileTableView.this.getWindow();
		}

		//--------------------------------------------------------------

	////////////////////////////////////////////////////////////////////
	//  Instance methods : overriding methods
	////////////////////////////////////////////////////////////////////

		@Override
		protected void updateItem(
			T		value,
			boolean	empty)
		{
			// Call superclass method
			super.updateItem(value, empty);

			// Update background
			updateBackground();

			// Set border
			setBorder(empty ? null
							: SceneUtils.createSolidBorder(getColour(TableViewStyle.ColourKey.CELL_BORDER),
														   Side.RIGHT, Side.BOTTOM));

			// Set graphic
			setGraphic(empty ? null : createLabel());
		}

		//--------------------------------------------------------------

	////////////////////////////////////////////////////////////////////
	//  Instance methods
	////////////////////////////////////////////////////////////////////

		protected void updateBackground()
		{
			if (StyleManager.INSTANCE.notUsingStyleSheet())
			{
				int index = getIndex();
				boolean selected = getSelectionModel().getSelectedIndices().contains(index);
// WORKAROUND
//				boolean focused = getTableView().isFocused();
				Color colour = isEmpty()
									? null
									: selected
										? focused
											? getColour(TableViewStyle.ColourKey.CELL_BACKGROUND_SELECTED_FOCUSED)
											: getColour(TableViewStyle.ColourKey.CELL_BACKGROUND_SELECTED)
										: (index % 2 == 0)
											? getColour(TableViewStyle.ColourKey.CELL_BACKGROUND_EVEN)
											: getColour(TableViewStyle.ColourKey.CELL_BACKGROUND_ODD);
				if (!selected && focused && (getFocusModel().getFocusedIndex() == index))
				{
					setBackground(SceneUtils.createColouredBackground(
							getColour(TableViewStyle.ColourKey.CELL_BACKGROUND_FOCUSED), new Insets(0.0, 1.0, 1.0, 0.0),
							colour, new Insets(1.0, 1.0, 2.0, 0.0)));
				}
				else
					setBackground(SceneUtils.createColouredBackground(colour));
			}
		}

		//--------------------------------------------------------------

		protected String getLabelText()
		{
			return null;
		}

		//--------------------------------------------------------------

		/**
		 * Creates and returns a label for the item of this cell.
		 *
		 * @return a label for the item of this cell.
		 */

		private Label createLabel()
		{
			// Create label
			Label label = null;
			if (getItem() != null)
			{
				// Get alignment of cell
				Pos alignment = getAlignment();

				// Create label
				label = new Label(getLabelText());
				label.setAlignment(alignment);
				if (alignment.getHpos() == HPos.RIGHT)
					label.setTextOverrun(OverrunStyle.LEADING_ELLIPSIS);
				double vPadding = (double)UnzipApp.instance().getPreferences().getCellVerticalPadding();
				label.setPadding(new Insets(vPadding, LABEL_H_PADDING, vPadding, LABEL_H_PADDING));
				label.setTextFill(getColour(TableViewStyle.ColourKey.CELL_TEXT));
				if (column == Column.CRC)
				{
					label.setFont(Fonts.monoFont());
					label.getStyleClass().add(StyleClass.CRC_CELL);
				}
				label.getStyleClass().add(TableViewStyle.StyleClass.CELL_LABEL);
			}

			// Return label
			return label;
		}

		//--------------------------------------------------------------

	}

	//==================================================================


	// CLASS: TEXT CELL


	private class TextCell
		extends Cell<String>
	{

	////////////////////////////////////////////////////////////////////
	//  Constructors
	////////////////////////////////////////////////////////////////////

		private TextCell(
			Column	column)
		{
			// Call superclass constructor
			super(column);
		}

		//--------------------------------------------------------------

	////////////////////////////////////////////////////////////////////
	//  Instance methods : overriding methods
	////////////////////////////////////////////////////////////////////

		@Override
		protected String getLabelText()
		{
			return getItem();
		}

		//--------------------------------------------------------------

	}

	//==================================================================


	// CLASS: TIMESTAMP CELL


	private class TimestampCell
		extends Cell<LocalDateTime>
	{

	////////////////////////////////////////////////////////////////////
	//  Constructors
	////////////////////////////////////////////////////////////////////

		private TimestampCell(
			Column	column)
		{
			// Call superclass constructor
			super(column);
		}

		//--------------------------------------------------------------

	////////////////////////////////////////////////////////////////////
	//  Instance methods : overriding methods
	////////////////////////////////////////////////////////////////////

		@Override
		protected String getLabelText()
		{
			LocalDateTime item = getItem();
			return (item == null) ? null : Constants.TIMESTAMP_FORMATTER.format(item);
		}

		//--------------------------------------------------------------

	}

	//==================================================================


	// CLASS: SIZE CELL


	private class SizeCell
		extends Cell<Long>
	{

	////////////////////////////////////////////////////////////////////
	//  Constructors
	////////////////////////////////////////////////////////////////////

		private SizeCell(
			Column	column)
		{
			// Call superclass constructor
			super(column);
		}

		//--------------------------------------------------------------

	////////////////////////////////////////////////////////////////////
	//  Instance methods : overriding methods
	////////////////////////////////////////////////////////////////////

		@Override
		protected String getLabelText()
		{
			Long item = getItem();
			return (item == null) ? null : Utils.formatDecimal(item);
		}

		//--------------------------------------------------------------

	}

	//==================================================================


	// CLASS: CRC CELL


	private class CrcCell
		extends Cell<Long>
	{

	////////////////////////////////////////////////////////////////////
	//  Constructors
	////////////////////////////////////////////////////////////////////

		private CrcCell(
			Column	column)
		{
			// Call superclass constructor
			super(column);
		}

		//--------------------------------------------------------------

	////////////////////////////////////////////////////////////////////
	//  Instance methods : overriding methods
	////////////////////////////////////////////////////////////////////

		@Override
		protected String getLabelText()
		{
			Long item = getItem();
			return (item == null) ? null : Utils.crcToString(item);
		}

		//--------------------------------------------------------------

	}

	//==================================================================


	// CLASS: 'COPY ENTRY TEXT' DIALOG


	private class CopyEntryTextDialog
		extends SimpleModalDialog<CopyEntryTextDialog.State>
	{

	////////////////////////////////////////////////////////////////////
	//  Constants
	////////////////////////////////////////////////////////////////////

		private static final	double	COLUMNS_PANE_GAP	= 8.0;

		private static final	Insets	COLUMNS_PANE_PADDING	= new Insets(8.0);

		private static final	double	ROWS_PANE_H_GAP	= 6.0;
		private static final	double	ROWS_PANE_V_GAP	= 6.0;

		private static final	Insets	ROWS_PANE_PADDING	= new Insets(0.0, 8.0, 6.0, 8.0);

		private static final	double	OUTER_PANE_GAP	= 3.0;

		private static final	Insets	OUTER_PANE_PADDING	= new Insets(3.0, 3.0, 0.0, 3.0);

		private static final	String	COLUMNS_STR			= "Columns";
		private static final	String	COMBINE_STR			= "Combine directory and filename";
		private static final	String	ROWS_STR			= "Rows";
		private static final	String	INCLUDE_HEADER_STR	= "Include header";
		private static final	String	FIELD_SEPARATOR_STR	= "Field separator";
		private static final	String	COPY_STR			= "Copy";

	////////////////////////////////////////////////////////////////////
	//  Class variables
	////////////////////////////////////////////////////////////////////

		private static	State	state	= new State(EnumSet.allOf(Column.class), false, false, FieldSeparator.SPACES);

	////////////////////////////////////////////////////////////////////
	//  Instance variables
	////////////////////////////////////////////////////////////////////

		private	State	result;

	////////////////////////////////////////////////////////////////////
	//  Constructors
	////////////////////////////////////////////////////////////////////

		private CopyEntryTextDialog(
			String	title)
		{
			// Call superclass constructor
			super(getWindow(), MethodHandles.lookup().lookupClass().getCanonicalName(), null, title);

			// Create pane: columns
			VBox columnsPane = new VBox(COLUMNS_PANE_GAP);
			columnsPane.setMaxWidth(Region.USE_PREF_SIZE);
			columnsPane.setPadding(COLUMNS_PANE_PADDING);

			// Create check boxes for columns
			SimpleSetProperty<Column> columns =
					new SimpleSetProperty<>(FXCollections.observableSet(EnumSet.copyOf(state.columns)));
			EnumMap<Column, CheckBox> columnCheckBoxes = new EnumMap<>(Column.class);
			for (Column column : Column.values())
			{
				CheckBox checkBox = new CheckBox(column.longText);
				checkBox.setSelected(state.columns.contains(column));
				checkBox.selectedProperty().addListener((observable, oldSelected, selected) ->
				{
					if (selected)
						columns.add(column);
					else
						columns.remove(column);
				});
				columnCheckBoxes.put(column, checkBox);
				columnsPane.getChildren().add(checkBox);
			}

			// Create check box: combine directory and filename
			CheckBox combineDirFilenameCheckBox = new CheckBox(COMBINE_STR);
			combineDirFilenameCheckBox.setSelected(state.combineDirectoryFilename);
			combineDirFilenameCheckBox.disableProperty()
					.bind(columnCheckBoxes.get(Column.DIRECTORY).selectedProperty()
						  .and(columnCheckBoxes.get(Column.FILENAME).selectedProperty()).not());
			VBox.setMargin(combineDirFilenameCheckBox, new Insets(0.0, 0.0, 0.0, 8.0));
			columnsPane.getChildren().add(Column.FILENAME.ordinal() + 1, combineDirFilenameCheckBox);

			// Create titled pane: columns
			LabelTitledPane titledColumnsPane = new LabelTitledPane(COLUMNS_STR, columnsPane);
			VBox.setVgrow(titledColumnsPane, Priority.ALWAYS);

			// Create pane: rows
			GridPane rowsPane = new GridPane();
			rowsPane.setHgap(ROWS_PANE_H_GAP);
			rowsPane.setVgap(ROWS_PANE_V_GAP);
			rowsPane.setAlignment(Pos.CENTER);
			rowsPane.setPadding(ROWS_PANE_PADDING);

			// Initialise column constraints
			ColumnConstraints column = new ColumnConstraints();
			column.setMinWidth(Region.USE_PREF_SIZE);
			column.setHalignment(HPos.RIGHT);
			column.setHgrow(Priority.NEVER);
			rowsPane.getColumnConstraints().add(column);

			column = new ColumnConstraints();
			column.setHalignment(HPos.LEFT);
			column.setHgrow(Priority.NEVER);
			column.setFillWidth(false);
			rowsPane.getColumnConstraints().add(column);

			// Initialise row index
			int row = 0;

			// Create check box: include header
			CheckBox includeHeaderCheckBox = new CheckBox(INCLUDE_HEADER_STR);
			includeHeaderCheckBox.setSelected(state.includeHeader);
			GridPane.setMargin(includeHeaderCheckBox, new Insets(0.0, 0.0, 2.0, 0.0));
			rowsPane.add(includeHeaderCheckBox, 1, row++);

			// Create spinner: field separator
			CollectionSpinner<FieldSeparator> fieldSeparatorSpinner =
					CollectionSpinner.leftRightH(HPos.CENTER, true, FieldSeparator.class, state.fieldSeparator, null,
												 null);
			rowsPane.addRow(row++, new Label(FIELD_SEPARATOR_STR), fieldSeparatorSpinner);

			// Create titled pane: rows
			LabelTitledPane titledRowsPane = new LabelTitledPane(ROWS_STR, rowsPane);

			// Create outer pane
			VBox outerPane = new VBox(OUTER_PANE_GAP, titledColumnsPane, titledRowsPane);
			outerPane.setAlignment(Pos.TOP_CENTER);
			outerPane.setPadding(OUTER_PANE_PADDING);

			// Set outer pane as content pane
			setContent(outerPane);

			// Create function to get state from components of user interface
			IFunction0<State> getState = () ->
					new State(EnumSet.copyOf(columns),
							  !combineDirFilenameCheckBox.isDisabled() && combineDirFilenameCheckBox.isSelected(),
							  includeHeaderCheckBox.isSelected(), fieldSeparatorSpinner.getItem());

			// Create button: copy
			Button copyButton = Buttons.hNoShrink(COPY_STR);
			copyButton.getProperties().put(BUTTON_GROUP_KEY, BUTTON_GROUP1);
			copyButton.setOnAction(event ->
			{
				result = getState.invoke();
				requestClose();
			});
			addButton(copyButton, HPos.RIGHT);

			// Create procedure to update 'copy' button
			IProcedure0 updateCopyButton = () -> copyButton.setDisable(columns.isEmpty());

			// Update 'copy' button when set of selected columns changes
			columns.addListener((InvalidationListener) observable -> updateCopyButton.invoke());

			// Update 'copy' button
			updateCopyButton.invoke();

			// Create button: cancel
			Button cancelButton = Buttons.hNoShrink(CANCEL_STR);
			cancelButton.getProperties().put(BUTTON_GROUP_KEY, BUTTON_GROUP1);
			cancelButton.setOnAction(event -> requestClose());
			addButton(cancelButton, HPos.RIGHT);

			// Fire 'cancel' button if Escape key is pressed
			setKeyFireButton(cancelButton, null);

			// Save dialog state when dialog is closed
			setOnHiding(event -> state = getState.invoke());

			// Apply new style sheet to scene
			applyStyleSheet();
		}

		//--------------------------------------------------------------

	////////////////////////////////////////////////////////////////////
	//  Instance methods : overriding methods
	////////////////////////////////////////////////////////////////////

		@Override
		protected State getResult()
		{
			return result;
		}

		//--------------------------------------------------------------

	////////////////////////////////////////////////////////////////////
	//  Enumerated types
	////////////////////////////////////////////////////////////////////


		// ENUMERATION: FIELD SEPARATOR


		private enum FieldSeparator
		{

		////////////////////////////////////////////////////////////////
		//  Constants
		////////////////////////////////////////////////////////////////

			SPACES
			(
				"Spaces"
			),

			TAB
			(
				"Tab"
			);

		////////////////////////////////////////////////////////////////
		//  Instance variables
		////////////////////////////////////////////////////////////////

			private	String	text;

		////////////////////////////////////////////////////////////////
		//  Constructors
		////////////////////////////////////////////////////////////////

			private FieldSeparator(
				String	text)
			{
				// Initialise instance variables
				this.text = text;
			}

			//----------------------------------------------------------

		////////////////////////////////////////////////////////////////
		//  Instance methods : overriding methods
		////////////////////////////////////////////////////////////////

			@Override
			public String toString()
			{
				return text;
			}

			//----------------------------------------------------------

		}

		//==============================================================

	////////////////////////////////////////////////////////////////////
	//  Member records
	////////////////////////////////////////////////////////////////////


		// RECORD: RESULT


		private record State(
			EnumSet<Column>	columns,
			boolean			combineDirectoryFilename,
			boolean			includeHeader,
			FieldSeparator	fieldSeparator)
		{ }

		//==============================================================

	}

	//==================================================================

}

//----------------------------------------------------------------------
