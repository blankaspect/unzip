/*====================================================================*\

AbstractTask.java

Class: abstract task.

\*====================================================================*/


// PACKAGE


package uk.blankaspect.unzip;

//----------------------------------------------------------------------


// IMPORTS


import javafx.stage.Window;

import uk.blankaspect.common.logging.Logger;

import uk.blankaspect.ui.jfx.dialog.ErrorDialog;

import uk.blankaspect.ui.jfx.task.AbstractSoftCancelTask;

//----------------------------------------------------------------------


// CLASS: ABSTRACT TASK


public abstract class AbstractTask<V>
	extends AbstractSoftCancelTask<V>
{

////////////////////////////////////////////////////////////////////////
//  Constructors
////////////////////////////////////////////////////////////////////////

	protected AbstractTask()
	{
	}

	//------------------------------------------------------------------

////////////////////////////////////////////////////////////////////////
//  Instance methods
////////////////////////////////////////////////////////////////////////

	protected void showErrorMessage(
		Window	window)
	{
		// Log error
		Logger.INSTANCE.error(getTitle(), getException());

		// Display error dialog
		ErrorDialog.show(window, getTitle(), getException());
	}

	//------------------------------------------------------------------

}

//----------------------------------------------------------------------
