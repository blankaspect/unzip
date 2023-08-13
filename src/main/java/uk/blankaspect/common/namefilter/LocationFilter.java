/*====================================================================*\

LocationFilter.java

Class: filter for file-system locations.

\*====================================================================*/


// PACKAGE


package uk.blankaspect.common.namefilter;

//----------------------------------------------------------------------


// IMPORTS


import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.nio.file.PathMatcher;

import java.util.Arrays;
import java.util.Objects;

import java.util.regex.PatternSyntaxException;

import uk.blankaspect.common.exception2.BaseException;

//----------------------------------------------------------------------


// CLASS: FILTER FOR FILE-SYSTEM LOCATIONS


/**
 * This class implements a filter for file-system locations that uses {@link PathMatcher} to match a given location.
 */

public class LocationFilter
{

////////////////////////////////////////////////////////////////////////
//  Constants
////////////////////////////////////////////////////////////////////////

	/** The identifier of the <i>glob</i> syntax of {@link FileSystem#getPathMatcher(String)}. */
	public static final	String	PATH_MATCHER_SYNTAX_GLOB	= "glob";

	/** The identifier of the <i>regular-expression</i> syntax of {@link FileSystem#getPathMatcher(String)}. */
	public static final	String	PATH_MATCHER_SYNTAX_REGEX	= "regex";

	/** An inclusive filter that matches all file-system locations. */
	public static final	LocationFilter	INCLUDE_ALL	= new LocationFilter();

	/** Error messages. */
	private interface ErrorMsg
	{
		String	INVALID_PATTERN		= "Pattern: %s\nThe pattern is invalid.";
		String	UNRECOGNISED_SYNTAX	= "Syntax: %s\nThe pattern syntax is not recognised.";
	}

////////////////////////////////////////////////////////////////////////
//  Instance variables
////////////////////////////////////////////////////////////////////////

	/** Flag: if {@code true}, this is an inclusive filter; otherwise, it is an exclusive filter. */
	private	boolean		inclusive;

	/** The kind of pattern. */
	private	PatternKind	patternKind;

	/** The filter pattern. */
	private	String		pattern;

	/** The file-system path matcher. */
	private	PathMatcher	matcher;

////////////////////////////////////////////////////////////////////////
//  Constructors
////////////////////////////////////////////////////////////////////////

	/**
	 * Creates a new instance of a location filter with the specified pattern.
	 * <p>
	 * The filter's matcher must be initialised by calling {@link #updateMatcher(FileSystem)} before the {@link
	 * #matches(Path)} method is called.
	 * </p>
	 *
	 * @param inclusive
	 *          if {@code true}, the filter will be inclusive; otherwise, it will be exclusive.
	 * @param patternKind
	 *          the kind of pattern.
	 * @param pattern
	 *          the pattern, whose syntax depends on {@code patternKind}.
	 */

	public LocationFilter(
		boolean		inclusive,
		PatternKind	patternKind,
		String		pattern)
	{
		// Validate arguments
		if (patternKind == null)
			throw new IllegalArgumentException("Null pattern kind");
		if (pattern == null)
			throw new IllegalArgumentException("Null pattern");

		// Initialise instance variables
		this.inclusive = inclusive;
		this.patternKind = patternKind;
		this.pattern = pattern;
	}

	//------------------------------------------------------------------

	/**
	 * Creates a new instance of a location filter with the specified pattern, for use with the specified file system.
	 *
	 * @param  inclusive
	 *          if {@code true}, the filter will be inclusive; otherwise, it will be exclusive.
	 * @param  patternKind
	 *           the kind of pattern.
	 * @param  pattern
	 *           the pattern, whose syntax depends on {@code patternKind}.
	 * @param  fileSystem
	 *           the file system with which the matcher of the filter may be used.
	 * @throws BaseException
	 *           if {@code patternKind} is not supported by the file system, or if {@code pattern} is invalid.
	 */

	public LocationFilter(
		boolean		inclusive,
		PatternKind	patternKind,
		String		pattern,
		FileSystem	fileSystem)
		throws BaseException
	{
		// Call alternative constructor
		this(inclusive, patternKind, pattern);

		// Initialise matcher
		updateMatcher((fileSystem == null) ? FileSystems.getDefault() : fileSystem);
	}

	//------------------------------------------------------------------

	/**
	 * Creates a new instance of an inclusive filter whose {@link #matches(Path)} method matches all file-system
	 * locations.
	 */

	private LocationFilter()
	{
		// Initialise instance variables
		inclusive = true;
	}

	//------------------------------------------------------------------

////////////////////////////////////////////////////////////////////////
//  Class methods
////////////////////////////////////////////////////////////////////////

	/**
	 * Returns {@code true} if the specified file-system location is accepted by the specified filters.  The location is
	 * accepted if it matches any of the inclusion filters and it does not match any of the exclusion filters.
	 *
	 * @param  location
	 *           the location to which {@code filters} will be applied.
	 * @param  filters
	 *           the filters that will be applied to {@code location}.
	 * @return {@code true} if {@code location} is accepted by {@code filters}.
	 */

	public static boolean accept(
		Path								location,
		Iterable<? extends LocationFilter>	filters)
	{
		// Assume rejection
		boolean accept = false;

		// Apply inclusion filters to path
		for (LocationFilter filter : filters)
		{
			if (filter.isInclusive() && filter.matches(location))
			{
				accept = true;
				break;
			}
		}

		// Apply exclusion filters to path
		if (accept)
		{
			for (LocationFilter filter : filters)
			{
				if (!filter.isInclusive() && filter.matches(location))
				{
					accept = false;
					break;
				}
			}
		}

		// Return result
		return accept;
	}

	//------------------------------------------------------------------

	/**
	 * Returns {@code true} if the specified file-system location is accepted by the specified filters.  The location is
	 * accepted if it matches any of the inclusion filters and it does not match any of the exclusion filters.
	 *
	 * @param  location
	 *           the location to which {@code filters} will be applied.
	 * @param  filters
	 *           the filters that will be applied to {@code location}.
	 * @return {@code true} if {@code location} is accepted by {@code filters}.
	 */

	public static boolean accept(
		Path				location,
		LocationFilter...	filters)
	{
		return accept(location, Arrays.asList(filters));
	}

	//------------------------------------------------------------------

////////////////////////////////////////////////////////////////////////
//  Instance methods : overriding methods
////////////////////////////////////////////////////////////////////////

	/**
	 * {@inheritDoc}
	 */

	@Override
	public boolean equals(
		Object	obj)
	{
		if (this == obj)
			return true;

		return (obj instanceof LocationFilter other) && (inclusive == other.inclusive)
				&& (patternKind == other.patternKind) && Objects.equals(pattern, other.pattern);
	}

	//------------------------------------------------------------------

	/**
	 * {@inheritDoc}
	 */

	@Override
	public int hashCode()
	{
		int code = Boolean.hashCode(inclusive);
		code = code * 31 + Objects.hashCode(patternKind);
		code = code * 31 + Objects.hashCode(pattern);
		return code;
	}

	//------------------------------------------------------------------

////////////////////////////////////////////////////////////////////////
//  Instance methods
////////////////////////////////////////////////////////////////////////

	/**
	 * Returns {@code true} if this is an inclusive filter, {@code false} if this is an exclusive filter.
	 *
	 * @return {@code true} if this is an inclusive filter, {@code false} if this is an exclusive filter.
	 */

	public boolean isInclusive()
	{
		return inclusive;
	}

	//------------------------------------------------------------------

	/**
	 * Returns the kind of pattern.
	 *
	 * @return the kind of pattern.
	 */

	public PatternKind getPatternKind()
	{
		return patternKind;
	}

	//------------------------------------------------------------------

	/**
	 * Returns the pattern.
	 *
	 * @return the pattern.
	 */

	public String getPattern()
	{
		return pattern;
	}

	//------------------------------------------------------------------

	/**
	 * Returns the file-system path matcher.
	 *
	 * @return the file-system path matcher.
	 */

	public PathMatcher getMatcher()
	{
		return matcher;
	}

	//------------------------------------------------------------------

	/**
	 * Returns {@code true} if the {@linkplain #getMatcher() matcher} of this filter matches the specified file-system
	 * location.
	 *
	 * @param  location
	 *           the location that will be tested by the matcher of this filter.
	 * @return {@code true} if the {@linkplain #getMatcher() matcher} of this filter matches {@code location}.
	 */

	public boolean matches(
		Path	location)
	{
		return (patternKind == null)
				|| ((matcher != null) && matcher.matches(patternKind.isFilename() ? location.getFileName() : location));
	}

	//------------------------------------------------------------------

	/**
	 * Updates the {@linkplain #getMatcher() matcher} of this filter so that it may be applied to locations of the
	 * specified file system.
	 *
	 * @param  fileSystem
	 *           the file system with which the matcher of this filter may be used.
	 * @throws BaseException
	 *           if the kind of pattern of this filter is not supported by the file system, or if the pattern of this
	 *           filter is invalid.
	 */

	public void updateMatcher(
		FileSystem	fileSystem)
		throws BaseException
	{
		if (patternKind != null)
		{
			// Get syntax of matcher
			String syntax = patternKind.isGlob() ? PATH_MATCHER_SYNTAX_GLOB : PATH_MATCHER_SYNTAX_REGEX;

			// Update matcher
			try
			{
				matcher = fileSystem.getPathMatcher(syntax + ":" + pattern);
			}
			catch (UnsupportedOperationException e)
			{
				throw new BaseException(ErrorMsg.UNRECOGNISED_SYNTAX, e, syntax);
			}
			catch (PatternSyntaxException e)
			{
				throw new BaseException(ErrorMsg.INVALID_PATTERN, e, pattern);
			}
		}
	}

	//------------------------------------------------------------------

}

//----------------------------------------------------------------------
