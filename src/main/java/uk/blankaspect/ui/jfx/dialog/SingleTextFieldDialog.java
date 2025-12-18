/*====================================================================*\

SingleTextFieldDialog.java

Class: single text-field dialog.

\*====================================================================*/


// PACKAGE


package uk.blankaspect.ui.jfx.dialog;

//----------------------------------------------------------------------


// IMPORTS


import java.util.function.Predicate;
import java.util.function.UnaryOperator;

import javafx.geometry.HPos;
import javafx.geometry.Pos;

import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.control.TextFormatter;

import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;

import javafx.stage.Window;
import javafx.stage.WindowEvent;

import uk.blankaspect.ui.jfx.button.Buttons;

import uk.blankaspect.ui.jfx.label.Labels;

import uk.blankaspect.ui.jfx.window.WindowUtils;

//----------------------------------------------------------------------


// CLASS: SINGLE TEXT-FIELD DIALOG


/**
 * This class implements a modal dialog that contains a single text field.
 */

public class SingleTextFieldDialog
	extends SimpleModalDialog<String>
{

////////////////////////////////////////////////////////////////////////
//  Constants
////////////////////////////////////////////////////////////////////////

	/** The gap between adjacent controls in the control pane. */
	private static final	double	CONTROL_PANE_H_GAP	= 6.0;

	/** The number of columns of the text field. */
	private static final	int		TEXT_FIELD_NUM_COLUMNS	= 24;

////////////////////////////////////////////////////////////////////////
//  Instance variables
////////////////////////////////////////////////////////////////////////

	/** The validator of the content of the text field. */
	private	Predicate<String>	validator;

	/** The result of this dialog. */
	private	String				result;

	/** The text field. */
	private	TextField			textField;

	/** The <i>OK</i> button. */
	private	Button				okButton;

////////////////////////////////////////////////////////////////////////
//  Constructors
////////////////////////////////////////////////////////////////////////

	/**
	 * Creates a new instance of a modal dialog that contains a single text field in which the specified text may be
	 * edited.
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
	 *          the text of the label of the text field.
	 * @param text
	 *          the initial value of the text.
	 * @param filter
	 *          the filter that will be applied to the text field of the dialog.  If {@code null}, no filter will be
	 *          applied.
	 * @param validator
	 *          the validator of the content of the text field.  If {@code null}, no validator will be applied.
	 */

	public SingleTextFieldDialog(
		Window								owner,
		String								boundsKey,
		String								title,
		String								labelText,
		String								text,
		UnaryOperator<TextFormatter.Change>	filter,
		Predicate<String>					validator)
	{
		// Call superclass constructor
		super(owner, boundsKey, title);

		// Allow dialog to be resized
		setResizable(true);

		// Initialise instance variables
		this.validator = validator;

		// Create text field
		textField = new TextField((text == null) ? "" : text);
		textField.setPrefColumnCount(TEXT_FIELD_NUM_COLUMNS);
		if (filter != null)
			textField.setTextFormatter(new TextFormatter<>(filter));
		HBox.setHgrow(textField, Priority.ALWAYS);

		// Create control pane
		HBox controlPane = new HBox(CONTROL_PANE_H_GAP, Labels.hNoShrink(labelText), textField);
		controlPane.setAlignment(Pos.CENTER);

		// Add control pane to content pane
		addContent(controlPane);

		// Create button: OK
		okButton = Buttons.hNoShrink(OK_STR);
		okButton.getProperties().put(BUTTON_GROUP_KEY, BUTTON_GROUP1);
		okButton.setOnAction(event ->
		{
			result = textField.getText();
			hide();
		});
		addButton(okButton, HPos.RIGHT);

		// Disable 'OK' button if content of text field is invalid
		textField.textProperty().addListener(observable -> updateOkButton());

		// Fire 'OK' button when 'Enter' key is pressed in text field
		textField.setOnAction(event -> okButton.fire());

		// Update 'OK' button
		updateOkButton();

		// Create button: cancel
		Button cancelButton = Buttons.hNoShrink(CANCEL_STR);
		cancelButton.getProperties().put(BUTTON_GROUP_KEY, BUTTON_GROUP1);
		cancelButton.setOnAction(event -> requestClose());
		addButton(cancelButton, HPos.RIGHT);

		// Fire 'cancel' button if Escape key is pressed; fire 'OK' button if Ctrl+Enter is pressed
		setKeyFireButton(cancelButton, okButton);

		// When dialog is shown, request focus on text field
		addEventHandler(WindowEvent.WINDOW_SHOWN, event ->
		{
			textField.requestFocus();
			textField.selectAll();
		});
	}

	//------------------------------------------------------------------

////////////////////////////////////////////////////////////////////////
//  Class methods
////////////////////////////////////////////////////////////////////////

	/**
	 * Creates a new instance of a modal dialog that contains a single text field in which the specified text may be
	 * edited, displays the dialog, and returns the content of the text field.
	 *
	 * @param  owner
	 *           the window that will be the owner of this dialog, or {@code null} for a top-level dialog that has no
	 *           owner.
	 * @param  boundsKey
	 *           the key with which the dialog will be associated in the map of locations and sizes.  If the map
	 *           contains an entry for the specified key, the location and size of the dialog will be set to the
	 *           associated values when the dialog is displayed.  If the key is {@code null}, it will be ignored.
	 * @param  title
	 *           the title of the dialog.
	 * @param  labelText
	 *           the text of the label of the text field.
	 * @param  text
	 *           the initial value of the text.
	 * @return the text, if the dialog was accepted; {@code null} otherwise.
	 */

	public static String show(
		Window	owner,
		String	boundsKey,
		String	title,
		String	labelText,
		String	text)
	{
		return show(owner, boundsKey, title, labelText, text, null, null);
	}

	//------------------------------------------------------------------

	/**
	 * Creates a new instance of a modal dialog that contains a single text field in which the specified text may be
	 * edited, displays the dialog, and returns the content of the text field.
	 *
	 * @param  owner
	 *           the window that will be the owner of this dialog, or {@code null} for a top-level dialog that has no
	 *           owner.
	 * @param  boundsKey
	 *           the key with which the dialog will be associated in the map of locations and sizes.  If the map
	 *           contains an entry for the specified key, the location and size of the dialog will be set to the
	 *           associated values when the dialog is displayed.  If the key is {@code null}, it will be ignored.
	 * @param  title
	 *           the title of the dialog.
	 * @param  labelText
	 *           the text of the label of the text field.
	 * @param  text
	 *           the initial value of the text.
	 * @param  filter
	 *           the filter that will be applied to the text field of the dialog.  If {@code null}, no filter will be
	 *           applied.
	 * @param  validator
	 *           the validator of the content of the text field.  If {@code null}, no validator will be applied.
	 * @return the text, if the dialog was accepted; {@code null} otherwise.
	 */

	public static String show(
		Window								owner,
		String								boundsKey,
		String								title,
		String								labelText,
		String								text,
		UnaryOperator<TextFormatter.Change>	filter,
		Predicate<String>					validator)
	{
		return new SingleTextFieldDialog(owner, boundsKey, title, labelText, text, filter, validator).showDialog();
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
		WindowUtils.preventHeightChange(this);
	}

	//------------------------------------------------------------------

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
	 * Returns the text field.
	 *
	 * @return the text field.
	 */

	public TextField getTextField()
	{
		return textField;
	}

	//------------------------------------------------------------------

	/**
	 * Sets the filter that will be applied to the text field.
	 *
	 * @param filter
	 *          the filter that will be applied to the text field, or {@code null} to apply no filter.
	 */

	public void setFilter(
		UnaryOperator<TextFormatter.Change>	filter)
	{
		textField.setTextFormatter((filter == null) ? null : new TextFormatter<>(filter));
	}

	//------------------------------------------------------------------

	/**
	 * Sets the validator of the content of the text field.
	 *
	 * @param validator
	 *          the validator of the content of the text field, or {@code null} to apply no validator.
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
	 * text field.
	 */

	private void updateOkButton()
	{
		okButton.setDisable((validator != null) && !validator.test(textField.getText()));
	}

	//------------------------------------------------------------------

}

//----------------------------------------------------------------------
