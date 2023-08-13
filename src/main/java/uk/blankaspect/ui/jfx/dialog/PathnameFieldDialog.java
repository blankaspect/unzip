/*====================================================================*\

PathnameFieldDialog.java

Class: pathname-field dialog.

\*====================================================================*/


// PACKAGE


package uk.blankaspect.ui.jfx.dialog;

//----------------------------------------------------------------------


// IMPORTS


import java.util.function.Predicate;

import javafx.geometry.HPos;
import javafx.geometry.Pos;

import javafx.scene.control.Button;
import javafx.scene.control.Label;

import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;

import javafx.stage.Window;
import javafx.stage.WindowEvent;

import uk.blankaspect.ui.jfx.container.PathnamePane;

import uk.blankaspect.ui.jfx.textfield.PathnameField;

import uk.blankaspect.ui.jfx.window.WindowUtils;

//----------------------------------------------------------------------


// CLASS: PATHNAME-FIELD DIALOG


/**
 * This class implements a modal dialog that contains a {@linkplain PathnameField pathname field}.
 */

public class PathnameFieldDialog
	extends SimpleModalDialog<String>
{

////////////////////////////////////////////////////////////////////////
//  Constants
////////////////////////////////////////////////////////////////////////

	/** The gap between adjacent controls in the control pane. */
	private static final	double	CONTROL_PANE_H_GAP	= 6.0;

	/** The number of columns of the pathname field. */
	private static final	int		PATHNAME_FIELD_NUM_COLUMNS	= 40;

////////////////////////////////////////////////////////////////////////
//  Instance variables
////////////////////////////////////////////////////////////////////////

	/** The validator of the content of the pathname field. */
	private	Predicate<String>	validator;

	/** The result of this dialog. */
	private	String				result;

	/** The pathname field. */
	private	PathnameField		pathnameField;

	/** The pathname pane. */
	private	PathnamePane		pathnamePane;

	/** The <i>OK</i> button. */
	private	Button				okButton;

////////////////////////////////////////////////////////////////////////
//  Constructors
////////////////////////////////////////////////////////////////////////

	/**
	 * Creates a new instance of a modal dialog that contains a {@linkplain PathnameField pathname field}.
	 *
	 * @param owner
	 *          the window that will be the owner of this dialog, or {@code null} for a top-level dialog that has no
	 *          owner.
	 * @param boundsKey
	 *          the key with which the dialog will be associated in the map of locations and sizes.  If the map contains
	 *          an entry for the specified key, the location and size of the dialog will be set to the associated values
	 *          when the dialog is displayed.  If the key is {@code null}, it will be ignored.
	 * @param title
	 *          the title of the dialog.
	 * @param labelText
	 *          the text of the label of the pathname field.
	 * @param pathname
	 *          the initial pathname.
	 * @param validator
	 *          the validator of the content of the pathname field.  If {@code null}, no validator will be applied.
	 */

	public PathnameFieldDialog(
		Window				owner,
		String				boundsKey,
		String				title,
		String				labelText,
		String				pathname,
		Predicate<String>	validator)
	{
		// Call superclass constructor
		super(owner, boundsKey, title);

		// Allow dialog to be resized
		setResizable(true);

		// Initialise instance variables
		this.validator = validator;

		// Create pathname field
		pathnameField = new PathnameField((pathname == null) ? "" : pathname, PATHNAME_FIELD_NUM_COLUMNS);

		// Create pathname pane
		pathnamePane = new PathnamePane(pathnameField);
		HBox.setHgrow(pathnamePane, Priority.ALWAYS);

		// Create control pane
		HBox controlPane = new HBox(CONTROL_PANE_H_GAP, new Label(labelText), pathnamePane);
		controlPane.setAlignment(Pos.CENTER);

		// Add control pane to content pane
		addContent(controlPane);

		// Create button: OK
		okButton = new Button(OK_STR);
		okButton.getProperties().put(BUTTON_GROUP_KEY, BUTTON_GROUP1);
		okButton.setOnAction(event ->
		{
			result = pathnameField.getText();
			hide();
		});
		addButton(okButton, HPos.RIGHT);

		// Disable 'OK' button if content of pathname field is invalid
		pathnameField.textProperty().addListener(observable -> updateOkButton());

		// Fire 'OK' button when 'Enter' key is pressed in pathname field
		pathnameField.setOnAction(event -> okButton.fire());

		// Update 'OK' button
		updateOkButton();

		// Create button: cancel
		Button cancelButton = new Button(CANCEL_STR);
		cancelButton.getProperties().put(BUTTON_GROUP_KEY, BUTTON_GROUP1);
		cancelButton.setOnAction(event -> requestClose());
		addButton(cancelButton, HPos.RIGHT);

		// Fire 'cancel' button if Escape key is pressed; fire 'OK' button if Ctrl+Enter is pressed
		setKeyFireButton(cancelButton, okButton);

		// Resize dialog to scene
		sizeToScene();

		// When dialog is shown, prevent its height from changing; request focus on text field
		addEventHandler(WindowEvent.WINDOW_SHOWN, event ->
		{
			// Prevent height of dialog from changing
			WindowUtils.preventHeightChange(this);

			// Request focus on text field
			pathnameField.requestFocus();
			pathnameField.selectAll();
		});
	}

	//------------------------------------------------------------------

////////////////////////////////////////////////////////////////////////
//  Instance methods : overriding methods
////////////////////////////////////////////////////////////////////////

	/**
	 * {@inheritDoc}
	 */

	@Override
	protected String getResult()
	{
		return result;
	}

	//------------------------------------------------------------------

////////////////////////////////////////////////////////////////////////
//  Instance methods
////////////////////////////////////////////////////////////////////////

	/**
	 * Returns the pathname field.
	 *
	 * @return the pathname field.
	 */

	public PathnameField getPathnameField()
	{
		return pathnameField;
	}

	//------------------------------------------------------------------

	/**
	 * Returns the button that is adjacent to the pathname field.
	 *
	 * @return the button that is adjacent to the pathname field.
	 */

	public Button getButton()
	{
		return pathnamePane.getButton();
	}

	//------------------------------------------------------------------

	/**
	 * Sets the validator of the content of the pathname field.
	 *
	 * @param validator
	 *          the validator of the content of the pathname field, or {@code null} to apply no validator.
	 */

	public void setValidator(
		Predicate<String>	validator)
	{
		// Update instance variable
		this.validator = validator;

		// Update 'OK' button
		updateOkButton();
	}

	//------------------------------------------------------------------

	/**
	 * Enables or disables the <i>OK</i> button according to the result of applying any validator to the content of the
	 * pathname field.
	 */

	private void updateOkButton()
	{
		okButton.setDisable((validator == null) ? (pathnameField.getLocation() == null)
												: !validator.test(pathnameField.getText()));
	}

	//------------------------------------------------------------------

}

//----------------------------------------------------------------------
