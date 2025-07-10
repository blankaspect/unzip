/*====================================================================*\

PersistableItemTableView.java

Class: table view for persistable items.

\*====================================================================*/


// PACKAGE


package uk.blankaspect.unzip;

//----------------------------------------------------------------------


// IMPORTS


import java.lang.invoke.MethodHandles;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import javafx.application.Platform;

import javafx.beans.property.ReadOnlyObjectWrapper;

import javafx.collections.ObservableList;

import javafx.geometry.HPos;
import javafx.geometry.Insets;
import javafx.geometry.Point2D;
import javafx.geometry.Pos;
import javafx.geometry.Side;
import javafx.geometry.VPos;

import javafx.scene.Node;

import javafx.scene.control.ContentDisplay;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.Label;
import javafx.scene.control.MenuItem;
import javafx.scene.control.OverrunStyle;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TablePosition;
import javafx.scene.control.TableRow;
import javafx.scene.control.TableView;

import javafx.scene.input.ContextMenuEvent;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;

import javafx.scene.paint.Color;

import javafx.scene.shape.Shape;

import javafx.stage.Window;

import uk.blankaspect.common.css.CssSelector;

import uk.blankaspect.common.function.IFunction1;

import uk.blankaspect.common.geometry.VHPos;

import uk.blankaspect.ui.jfx.font.FontUtils;

import uk.blankaspect.ui.jfx.math.FxGeomUtils;

import uk.blankaspect.ui.jfx.popup.CellPopUpManager;
import uk.blankaspect.ui.jfx.popup.PopUpUtils;

import uk.blankaspect.ui.jfx.scene.SceneUtils;

import uk.blankaspect.ui.jfx.shape.Shapes;

import uk.blankaspect.ui.jfx.style.ColourProperty;
import uk.blankaspect.ui.jfx.style.FxProperty;
import uk.blankaspect.ui.jfx.style.StyleConstants;
import uk.blankaspect.ui.jfx.style.StyleManager;
import uk.blankaspect.ui.jfx.style.StyleSelector;

import uk.blankaspect.ui.jfx.tableview.ElasticList;
import uk.blankaspect.ui.jfx.tableview.SimpleTableView;
import uk.blankaspect.ui.jfx.tableview.TableViewStyle;

import uk.blankaspect.ui.jfx.text.TextUtils;

//----------------------------------------------------------------------


// CLASS: TABLE VIEW FOR PERSISTABLE ITEMS


public class PersistableItemTableView<S extends IPersistable>
	extends TableView<S>
{

////////////////////////////////////////////////////////////////////////
//  Constants
////////////////////////////////////////////////////////////////////////

	/** The extra width of the table to allow for the vertical scroll bar. */
	private static final	double	EXTRA_WIDTH	= 17.0;

	/** The padding around the label of a header cell. */
	private static final	Insets	HEADER_CELL_LABEL_PADDING	= new Insets(3.0, 4.0, 3.0, 4.0);

	/** The logical size of a <i>tick</i> or <i>cross</i> icon. */
	private static final	double	ICON_SIZE	= 0.85 * TextUtils.textHeight();

	/** The prefix of the identifier of a text column. */
	private static final	String	TEXT_COLUMN_ID_PREFIX	= "column";

	/** The identifier of the <i>save</i> column. */
	private static final	String	SAVE_COLUMN_ID	= "save";

	/** Miscellaneous strings. */
	private static final	String	SAVE_STR	= "Save";

	/** CSS colour properties. */
	private static final	List<ColourProperty>	COLOUR_PROPERTIES	= List.of
	(
		ColourProperty.of
		(
			FxProperty.STROKE,
			ColourKey.ICON,
			CssSelector.builder()
					.cls(StyleClass.PERSISTABLE_ITEM_TABLE_VIEW)
					.desc(StyleClass.ICON)
					.build()
		)
	);

	/** CSS style classes. */
	public interface StyleClass
	{
		String	PERSISTABLE_ITEM_TABLE_VIEW	= StyleConstants.APP_CLASS_PREFIX + "persistable-item-table-view";

		String	ICON	= StyleConstants.CLASS_PREFIX + "icon";
	}

	/** Keys of colours that are used in colour properties. */
	private interface ColourKey
	{
		String	PREFIX	= StyleManager.colourKeyPrefix(MethodHandles.lookup().lookupClass().getEnclosingClass());

		String	ICON	= PREFIX + "icon";
	}

////////////////////////////////////////////////////////////////////////
//  Instance variables
////////////////////////////////////////////////////////////////////////

	private	IFunction1<MenuItem, Boolean>	menuItemFactory;

	/** The manager of the pop-up windows that are displayed for the cells of this table view. */
	private	CellPopUpManager				cellPopUpManager;

	private	ElasticList<S>					itemList;
	private	List<Cell<?>>					cells;
	private	List<TableRow<S>>				rows;
	private	boolean							headerInitialised;

	// WORKAROUND for a bug in JavaFX: isFocused() sometimes returns false when the table view has focus
	/** Flag: if {@code true}, this table view has keyboard focus. */
	private	boolean							focused;

////////////////////////////////////////////////////////////////////////
//  Static initialiser
////////////////////////////////////////////////////////////////////////

	static
	{
		// Register the style properties of this class and its dependencies with the style manager
		StyleManager.INSTANCE.register(PersistableItemTableView.class, COLOUR_PROPERTIES,
									   SimpleTableView.class);
	}

////////////////////////////////////////////////////////////////////////
//  Constructors
////////////////////////////////////////////////////////////////////////

	@SuppressWarnings("unchecked")
	public PersistableItemTableView(
		List<ColumnInfo>	columnInfos)
	{
		// Initialise instance variables
		cellPopUpManager = new CellPopUpManager(Cell.POP_UP_DELAY);
		itemList = new ElasticList<>(this);
		cells = new ArrayList<>();
		rows = new ArrayList<>();

		// Set properties
		setId(StyleClass.PERSISTABLE_ITEM_TABLE_VIEW);
		getStyleClass().addAll(StyleClass.PERSISTABLE_ITEM_TABLE_VIEW, TableViewStyle.StyleClass.TABLE_VIEW);

		// Create text columns
		double width = EXTRA_WIDTH;
		for (int i = 0; i < columnInfos.size(); i++)
		{
			// Get column index
			int index = i;

			// Get column info for column
			ColumnInfo columnInfo = columnInfos.get(index);

			// Create table column
			TableColumn<S, String> column = new TableColumn<>(columnInfo.title);
			column.setCellFactory(column0 -> new TextCell());
			column.setCellValueFactory(features -> new ReadOnlyObjectWrapper<>(features.getValue().getText(index)));
			column.setId(TEXT_COLUMN_ID_PREFIX + i);
			column.setPrefWidth(Math.ceil(columnInfo.textWidth + PersistableItemTableView.Cell.LABEL_PADDING.getLeft()
											+ PersistableItemTableView.Cell.LABEL_PADDING.getRight() + 1.0));

			// Add column to list
			getColumns().add(column);

			// Increment total width
			width += column.getPrefWidth();
		}

		// Create 'save' column
		TableColumn<S, Boolean> column = new TableColumn<>(SAVE_STR);
		column.setResizable(false);
		column.setSortable(false);
		column.setCellFactory(column0 -> new BooleanCell());
		column.setCellValueFactory(features -> new ReadOnlyObjectWrapper<>(features.getValue().isPersistent()));
		column.setId(SAVE_COLUMN_ID);
		column.setPrefWidth(Math.ceil(TextUtils.textWidth(FontUtils.boldFont(), SAVE_STR)
										+ PersistableItemTableView.Cell.LABEL_PADDING.getLeft()
										+ PersistableItemTableView.Cell.LABEL_PADDING.getRight() + 1.0));

		// Add column to list
		getColumns().add(column);

		// Increment total width
		width += column.getPrefWidth();

		// Set preferred width
		setPrefWidth(width);

		// Set row factory
		setRowFactory(table ->
		{
			TableRow<S> row = new TableRow<>();
			rows.add(row);
			row.setBackground(SceneUtils.createColouredBackground(
					getColour(TableViewStyle.ColourKey.CELL_BACKGROUND_EMPTY)));
			return row;
		});

		// Update cell backgrounds on change of state
		if (StyleManager.INSTANCE.notUsingStyleSheet())
		{
			// Update cell backgrounds when selection changes
			getSelectionModel().selectedIndexProperty().addListener(observable -> updateCellBackgrounds());

			// Update cell backgrounds when focus changes
// WORKAROUND : see 'focused' instance variable
//			focusedProperty().addListener(observable -> updateCellBackgrounds());
			focusedProperty().addListener((observable, oldFocused, newFocused) ->
			{
				focused = newFocused;
				updateCellBackgrounds();
			});

			// Update cell backgrounds when focused cell changes
			getFocusModel().focusedCellProperty().addListener(observable -> updateCellBackgrounds());
		}

		// Ensure cells are redrawn if scroll bar is hidden
		widthProperty().addListener(observable -> Platform.runLater(this::refresh));

		// Edit focused cell when Space key is pressed
		addEventHandler(KeyEvent.KEY_PRESSED, event ->
		{
			if (event.getCode() == KeyCode.SPACE)
			{
				// Get position of focused cell
				TablePosition<S, ?> focusedCell = getFocusModel().getFocusedCell();

				// Search for focused cell and edit it
				for (Cell<?> cell : cells)
				{
					if (!cell.isEmpty() && (focusedCell.getRow() == cell.getIndex())
							&& (cell instanceof PersistableItemTableView.BooleanCell booleanCell))
						booleanCell.toggleEnabled();
				}

				// Consume event
				event.consume();
			}
		});

		// Display context menu in response to request
		addEventHandler(ContextMenuEvent.CONTEXT_MENU_REQUESTED, event ->
		{
			if ((menuItemFactory != null) && !getSelectionModel().isEmpty())
			{
				// Create context menu
				ContextMenu menu = new ContextMenu();

				// Create tick icon
				Shape tickIcon = Shapes.tick01(ICON_SIZE);
				tickIcon.setStroke(getColour(ColourKey.ICON));
				tickIcon.getStyleClass().add(StyleClass.ICON);

				// Create cross icon
				Shape crossIcon = Shapes.cross01(ICON_SIZE);
				crossIcon.setStroke(getColour(ColourKey.ICON));
				crossIcon.getStyleClass().add(StyleClass.ICON);

				// Get width of icon
				double iconWidth = Math.max(Math.ceil(tickIcon.getLayoutBounds().getWidth()),
											Math.ceil(crossIcon.getLayoutBounds().getWidth()));

				// Add menu item: save
				MenuItem menuItem = menuItemFactory.invoke(true);
				menuItem.setGraphic(Shapes.tile(tickIcon, iconWidth));
				menu.getItems().add(menuItem);

				// Add menu item: don't save
				menuItem = menuItemFactory.invoke(false);
				menuItem.setGraphic(Shapes.tile(crossIcon, iconWidth));
				menu.getItems().add(menuItem);

				// Display context menu
				menu.show(SceneUtils.getWindow(this), event.getScreenX(), event.getScreenY());
			}
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

		// Set alignment and padding of header labels
		if (!headerInitialised)
		{
			for (Node node : lookupAll(StyleSelector.COLUMN_HEADER_LABEL))
			{
				if (node instanceof Label label)
				{
					label.setAlignment(FxGeomUtils.getPos(
											VPos.CENTER,
											node.getParent().getId().equals(SAVE_COLUMN_ID) ? HPos.CENTER : HPos.LEFT));
					label.setPadding(HEADER_CELL_LABEL_PADDING);
				}
			}
			headerInitialised = true;
		}
	}

	//------------------------------------------------------------------

////////////////////////////////////////////////////////////////////////
//  Instance methods
////////////////////////////////////////////////////////////////////////

	public void setMenuItemFactory(
		IFunction1<MenuItem, Boolean>	factory)
	{
		menuItemFactory = factory;
	}

	//------------------------------------------------------------------

	public ObservableList<S> getItemList()
	{
		return (itemList == null) ? getItems() : itemList.getBaseList();
	}

	//------------------------------------------------------------------

	public void setItems(
		Collection<? extends S>	items)
	{
		// Set items on table view
		itemList.update(items);
	}

	//------------------------------------------------------------------

	private void updateCellBackgrounds()
	{
		for (Cell<?> cell : cells)
			cell.updateBackground();
	}

	//------------------------------------------------------------------

	private Window getWindow()
	{
		return SceneUtils.getWindow(this);
	}

	//------------------------------------------------------------------

////////////////////////////////////////////////////////////////////////
//  Member records
////////////////////////////////////////////////////////////////////////


	// RECORD: COLUMN INFORMATION


	public record ColumnInfo(
		String	title,
		double	textWidth)
	{ }

	//==================================================================

////////////////////////////////////////////////////////////////////////
//  Member classes : inner classes
////////////////////////////////////////////////////////////////////////


	// CLASS: CELL


	private abstract class Cell<T>
		extends TableCell<S, T>
		implements CellPopUpManager.ICell<T>
	{

	////////////////////////////////////////////////////////////////////
	//  Constants
	////////////////////////////////////////////////////////////////////

		/** The padding around the label of a cell. */
		private static final	Insets	LABEL_PADDING	= new Insets(2.0, 6.0, 2.0, 6.0);

		/** The delay (in milliseconds) before a pop-up for a cell is displayed after it is activated. */
		private static final	int		POP_UP_DELAY	= 500;

	////////////////////////////////////////////////////////////////////
	//  Constructors
	////////////////////////////////////////////////////////////////////

		private Cell(
			HPos	hAlignment)
		{
			// Set properties
			setContentDisplay(ContentDisplay.GRAPHIC_ONLY);
			setAlignment(FxGeomUtils.getPos(VPos.CENTER, hAlignment));
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
			return (getItem() == null) ? null : getIndex() + ":" + getTableColumn().getId();
		}

		//--------------------------------------------------------------

		/**
		 * {@inheritDoc}
		 */

		@Override
		public Node getPopUpContent()
		{
			// Create label
			Label label = isEditing() ? null : createLabel();

			// Set properties of label
			if (label != null)
			{
				label.setBackground(SceneUtils.createColouredBackground(
						getColour(TableViewStyle.ColourKey.CELL_POPUP_BACKGROUND)));
				label.setBorder(SceneUtils.createSolidBorder(
						getColour(TableViewStyle.ColourKey.CELL_POPUP_BORDER)));
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
			VHPos pos = null;
			double x = 0.0;
			switch (getAlignment().getHpos())
			{
				case LEFT:
					pos = VHPos.CENTRE_LEFT;
					x = -1.0;
					break;

				case CENTER:
					pos = VHPos.CENTRE_CENTRE;
					break;

				case RIGHT:
					pos = VHPos.CENTRE_RIGHT;
					x = 1.0;
					break;
			}

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
			return PersistableItemTableView.this.getWindow();
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
							: SceneUtils.createSolidBorder(
										getColour(TableViewStyle.ColourKey.CELL_BORDER), Side.RIGHT, Side.BOTTOM));

			// Update cell contents
			update();
		}

		//--------------------------------------------------------------

	////////////////////////////////////////////////////////////////////
	//  Instance methods
	////////////////////////////////////////////////////////////////////

		protected S getTableItem()
		{
			List<S> items = getItems();
			int index = getIndex();
			return ((index < 0) || (index >= items.size())) ? null : items.get(index);
		}

		//--------------------------------------------------------------

		protected void update()
		{
			setGraphic(isEmpty() ? null : createLabel());
		}

		//--------------------------------------------------------------

		protected String getLabelText()
		{
			return null;
		}

		//--------------------------------------------------------------

		protected Node getLabelGraphic()
		{
			return null;
		}

		//--------------------------------------------------------------

		/**
		 * Creates and returns a label for the item of this cell.
		 *
		 * @return a label for the item of this cell.
		 */

		protected Label createLabel()
		{
			// Create label
			Label label = null;
			if (getItem() != null)
			{
				// Get alignment of cell
				Pos alignment = getAlignment();

				// Create label
				label = new Label(getLabelText(), getLabelGraphic());

				// Set properties of label
				label.setAlignment(alignment);
				if (alignment.getHpos() == HPos.RIGHT)
					label.setTextOverrun(OverrunStyle.LEADING_ELLIPSIS);
				label.setPadding(LABEL_PADDING);
				label.setTextFill(getColour(TableViewStyle.ColourKey.CELL_TEXT));
				label.getStyleClass().add(TableViewStyle.StyleClass.CELL_LABEL);
			}

			// Return label
			return label;
		}

		//--------------------------------------------------------------

		private void updateBackground()
		{
			if (StyleManager.INSTANCE.notUsingStyleSheet())
			{
				int index = getIndex();
				boolean selected = (getSelectionModel().getSelectedIndex() == index);
// WORKAROUND
//				boolean focused = getTableView().isFocused();
				@SuppressWarnings("unchecked")
				TablePosition<S, ?> focusedCell = getFocusModel().getFocusedCell();
				boolean cellFocused =
						(focusedCell.getRow() == index) && (focusedCell.getTableColumn() == getTableColumn());
				Color colour = isEmpty()
									? null
									: cellFocused && selected
										? focused
											? getColour(TableViewStyle.ColourKey.CELL_BACKGROUND_SELECTED_FOCUSED)
											: getColour(TableViewStyle.ColourKey.CELL_BACKGROUND_SELECTED)
										: (index % 2 == 0)
											? getColour(TableViewStyle.ColourKey.CELL_BACKGROUND_EVEN)
											: getColour(TableViewStyle.ColourKey.CELL_BACKGROUND_ODD);
				if (!selected && focused && cellFocused)
				{
					setBackground(SceneUtils.createColouredBackground(
							getColour(TableViewStyle.ColourKey.CELL_BACKGROUND_FOCUSED),
							new Insets(0.0, 1.0, 1.0, 0.0), colour, new Insets(1.0, 1.0, 2.0, 0.0)));
				}
				else
					setBackground(SceneUtils.createColouredBackground(colour));
			}
		}

		//--------------------------------------------------------------

	}

	//==================================================================


	// CLASS: BOOLEAN CELL


	private class BooleanCell
		extends Cell<Boolean>
	{

	////////////////////////////////////////////////////////////////////
	//  Instance variables
	////////////////////////////////////////////////////////////////////

		private	boolean	selectedWhenMousePressed;

	////////////////////////////////////////////////////////////////////
	//  Constructors
	////////////////////////////////////////////////////////////////////

		private BooleanCell()
		{
			// Call superclass constructor
			super(HPos.CENTER);

			// Test whether cell is already selected when primary mouse button is pressed on it
			addEventFilter(MouseEvent.MOUSE_PRESSED, event ->
			{
				if (event.getButton() == MouseButton.PRIMARY)
				{
					S item = getTableItem();
					selectedWhenMousePressed =
							(item != null) && getSelectionModel().getSelectedItems().contains(item);
				}
			});

			// Toggle 'enabled' state of view item when primary mouse button is clicked on cell
			addEventHandler(MouseEvent.MOUSE_CLICKED, event ->
			{
				if ((event.getButton() == MouseButton.PRIMARY) && !event.isControlDown() && !event.isShiftDown()
						&& cellPopUpManager.isActivated() && selectedWhenMousePressed)
					toggleEnabled();
			});
		}

		//--------------------------------------------------------------

	////////////////////////////////////////////////////////////////////
	//  Instance methods : overriding methods
	////////////////////////////////////////////////////////////////////

		@Override
		public Node getPopUpContent()
		{
			return null;
		}

		//--------------------------------------------------------------

		@Override
		protected void update()
		{
			Node graphic = null;
			if (!isEmpty() && (getItem() != null) && getTableItem().isPersistent())
			{
				Shape tick = Shapes.tick01();
				tick.setStroke(getColour(ColourKey.ICON));
				tick.getStyleClass().add(StyleClass.ICON);
				graphic = Shapes.tile(tick);
			}
			setGraphic(graphic);
		}

		//--------------------------------------------------------------

	////////////////////////////////////////////////////////////////////
	//  Instance methods
	////////////////////////////////////////////////////////////////////

		private void toggleEnabled()
		{
			S tableItem = getTableItem();
			if (tableItem != null)
			{
				tableItem.setPersistent(!tableItem.isPersistent());
				update();
			}
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

		private TextCell()
		{
			// Call superclass constructor
			super(HPos.LEFT);
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

		@Override
		protected Label createLabel()
		{
			// Call superclass method
			Label label = super.createLabel();

			// Set properties of label
			if (label != null)
				label.setMaxWidth(Double.MAX_VALUE);

			// Return label
			return label;
		}

		//--------------------------------------------------------------

	}

	//==================================================================

}

//----------------------------------------------------------------------
