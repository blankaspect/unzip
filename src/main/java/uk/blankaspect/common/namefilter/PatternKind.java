/*====================================================================*\

PatternKind.java

Class: kinds of filter pattern.

\*====================================================================*/


// PACKAGE


package uk.blankaspect.common.namefilter;

//----------------------------------------------------------------------


// IMPORTS


import java.util.stream.Stream;

import uk.blankaspect.common.misc.IStringKeyed;

//----------------------------------------------------------------------


// ENUMERATION: KINDS OF FILTER PATTERN


/**
 * This is an enumeration of the kinds of pattern that may be used to match a filename or pathname.
 */

public enum PatternKind
	implements IStringKeyed
{

////////////////////////////////////////////////////////////////////////
//  Constants
////////////////////////////////////////////////////////////////////////

	/**
	 * A glob pattern that is used to match filenames.
	 */
	GLOB_FILENAME
	(
		"globFilename",
		'g',
		"Glob, filename"
	),

	/**
	 * A glob pattern that is used to match pathnames at all levels of a directory structure.
	 */
	GLOB_PATHNAME
	(
		"globPathname",
		'G',
		"Glob, pathname"
	),

	/**
	 * A regular expression that is used to match filenames.
	 */
	REGEX_FILENAME
	(
		"regexFilename",
		'r',
		"Regular expression, filename"
	),

	/**
	 * A regular expression that is used to match pathnames at all levels of a directory structure.
	 */
	REGEX_PATHNAME
	(
		"regexPathname",
		'R',
		"Regular expression, pathname"
	);

////////////////////////////////////////////////////////////////////////
//  Instance variables
////////////////////////////////////////////////////////////////////////

	/** The key that is associated with this kind of pattern. */
	private	String	key;

	/** The short key that is associated with this kind of pattern. */
	private	char	shortKey;

	/** The textual representation of this kind of pattern. */
	private	String	text;

////////////////////////////////////////////////////////////////////////
//  Constructors
////////////////////////////////////////////////////////////////////////

	/**
	 * Creates a new instance of a kind of pattern.
	 *
	 * @param key
	 *          the key that will be associated with the kind of pattern.
	 * @param shortKey
	 *          the short key that will be associated with the kind of pattern.
	 * @param text
	 *          the textual representation of the kind of pattern.
	 */

	private PatternKind(
		String	key,
		char	shortKey,
		String	text)
	{
		// Initialise instance variables
		this.key = key;
		this.shortKey = shortKey;
		this.text = text;
	}

	//------------------------------------------------------------------

////////////////////////////////////////////////////////////////////////
//  Class methods
////////////////////////////////////////////////////////////////////////

	/**
	 * Returns the kind of pattern that is associated with the specified key.
	 *
	 * @param  key
	 *           the key whose associated kind of pattern is desired.
	 * @return the kind of pattern that is associated with {@code key}, or {@code null} if there is no such value.
	 */

	public static PatternKind forKey(
		String	key)
	{
		return Stream.of(values())
				.filter(value -> value.key.equals(key))
				.findFirst()
				.orElse(null);
	}

	//------------------------------------------------------------------

	/**
	 * Returns the kind of pattern that is associated with the specified short key.
	 *
	 * @param  key
	 *           the short key whose associated kind of pattern is desired.
	 * @return the kind of pattern that is associated with {@code key}, or {@code null} if there is no such kind.
	 */

	public static PatternKind forShortKey(
		char	key)
	{
		return Stream.of(values())
				.filter(value -> value.getShortKey() == key)
				.findFirst()
				.orElse(null);
	}

	//------------------------------------------------------------------

////////////////////////////////////////////////////////////////////////
//  Instance methods : IStringKeyed interface
////////////////////////////////////////////////////////////////////////

	/**
	 * Returns the key that is associated with this kind of pattern.
	 *
	 * @return the key that is associated with this kind of pattern.
	 */

	@Override
	public String getKey()
	{
		return key;
	}

	//------------------------------------------------------------------

////////////////////////////////////////////////////////////////////////
//  Instance methods : overriding methods
////////////////////////////////////////////////////////////////////////

	/**
	 * {@inheritDoc}
	 */

	@Override
	public String toString()
	{
		return text;
	}

	//------------------------------------------------------------------

////////////////////////////////////////////////////////////////////////
//  Instance methods
////////////////////////////////////////////////////////////////////////

	/**
	 * Returns the short key that is associated with this kind of pattern.
	 *
	 * @return the short key that is associated with this kind of pattern.
	 */

	public char getShortKey()
	{
		return shortKey;
	}

	//------------------------------------------------------------------

	/**
	 * Returns {@code true} if this kind of pattern is a glob.
	 *
	 * @return {@code true} if this kind of pattern is a glob.
	 */

	public boolean isGlob()
	{
		return (this == GLOB_FILENAME) || (this == GLOB_PATHNAME);
	}

	//------------------------------------------------------------------

	/**
	 * Returns {@code true} if this kind of pattern is a regular expression.
	 *
	 * @return {@code true} if this kind of pattern is a regular expression.
	 */

	public boolean isRegex()
	{
		return (this == REGEX_FILENAME) || (this == REGEX_PATHNAME);
	}

	//------------------------------------------------------------------

	/**
	 * Returns {@code true} if this kind of pattern is intended to be applied to filenames.
	 *
	 * @return {@code true} if this kind of pattern is intended to be applied to filenames.
	 */

	public boolean isFilename()
	{
		return (this == GLOB_FILENAME) || (this == REGEX_FILENAME);
	}

	//------------------------------------------------------------------

	/**
	 * Returns {@code true} if this kind of pattern is intended to be applied to pathnames.
	 *
	 * @return {@code true} if this kind of pattern is intended to be applied to pathnames.
	 */

	public boolean isPathname()
	{
		return (this == GLOB_PATHNAME) || (this == REGEX_PATHNAME);
	}

	//------------------------------------------------------------------

}

//----------------------------------------------------------------------
