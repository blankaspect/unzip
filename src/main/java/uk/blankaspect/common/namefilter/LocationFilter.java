/*====================================================================*\

LocationFilter.java

Class: filter for file-system locations.

\*====================================================================*/


// PACKAGE


package uk.blankaspect.common.namefilter;

//----------------------------------------------------------------------


// IMPORTS


import java.nio.file.Files;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.nio.file.PathMatcher;

import java.util.List;
import java.util.Objects;

import java.util.regex.PatternSyntaxException;

import java.util.stream.Stream;

import uk.blankaspect.common.exception2.BaseException;

import uk.blankaspect.common.string.StringUtils;

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

	/** An inclusive filter that matches all regular files. */
	public static final	LocationFilter	INCLUDE_ALL_FILES	= new LocationFilter()
	{
		@Override
		public boolean matches(
			Path	location)
		{
			return Files.isRegularFile(location, LinkOption.NOFOLLOW_LINKS);
		}
	};

	/** An inclusive filter that matches all directories. */
	public static final	LocationFilter	INCLUDE_ALL_DIRECTORIES	= new LocationFilter()
	{
		@Override
		public boolean matches(
			Path	location)
		{
			return Files.isDirectory(location, LinkOption.NOFOLLOW_LINKS);
		}
	};

	/** Error messages. */
	private interface ErrorMsg
	{
		String	INVALID_PATTERN		= "Pattern: %s\nThe pattern is invalid.";
		String	UNRECOGNISED_SYNTAX	= "Syntax: %s\nThe pattern syntax is not recognised.";
	}

////////////////////////////////////////////////////////////////////////
//  Instance variables
////////////////////////////////////////////////////////////////////////

	/** The kind of filter. */
	private	Kind		kind;

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
	 * Creates a new instance of the specified kind of location filter with the specified pattern.
	 * <p>
	 * The filter's matcher must be initialised by calling {@link #updateMatcher(FileSystem)} before the {@link
	 * #matches(Path)} method is called.
	 * </p>
	 *
	 * @param kind
	 *          the kind of location filter.
	 * @param patternKind
	 *          the kind of pattern.
	 * @param pattern
	 *          the pattern, whose syntax depends on {@code patternKind}.
	 */

	public LocationFilter(
		Kind		kind,
		PatternKind	patternKind,
		String		pattern)
	{
		// Validate arguments
		if (kind == null)
			throw new IllegalArgumentException("Null filter kind");
		if (patternKind == null)
			throw new IllegalArgumentException("Null pattern kind");
		if (pattern == null)
			throw new IllegalArgumentException("Null pattern");

		// Initialise instance variables
		this.kind = kind;
		this.patternKind = patternKind;
		this.pattern = pattern;
	}

	//------------------------------------------------------------------

	/**
	 * Creates a new instance of the specified kind of location filter with the specified pattern, for use with the
	 * specified file system.
	 *
	 * @param  kind
	 *           the kind of location filter.
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
		Kind		kind,
		PatternKind	patternKind,
		String		pattern,
		FileSystem	fileSystem)
		throws BaseException
	{
		// Call alternative constructor
		this(kind, patternKind, pattern);

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
		kind = Kind.INCLUDE;
	}

	//------------------------------------------------------------------

////////////////////////////////////////////////////////////////////////
//  Class methods
////////////////////////////////////////////////////////////////////////

	/**
	 * Creates and returns a new instance of an <i>include</i> filter with the specified pattern, for use with the
	 * default file system.
	 *
	 * @param  patternKind
	 *           the kind of pattern.
	 * @param  pattern
	 *           the pattern, whose syntax depends on {@code patternKind}.
	 * @return a new instance of an <i>include</i> filter with the specified pattern, for use with the default file
	 *         system.
	 * @throws BaseException
	 *           if {@code patternKind} is not supported by the file system, or if {@code pattern} is invalid.
	 */

	public static LocationFilter include(
		PatternKind	patternKind,
		String		pattern)
		throws BaseException
	{
		return new LocationFilter(Kind.INCLUDE, patternKind, pattern, null);
	}

	//------------------------------------------------------------------

	/**
	 * Creates and returns a new instance of an <i>exclude</i> filter with the specified pattern, for use with the
	 * default file system.
	 *
	 * @param  patternKind
	 *           the kind of pattern.
	 * @param  pattern
	 *           the pattern, whose syntax depends on {@code patternKind}.
	 * @return a new instance of an <i>exclude</i> filter with the specified pattern, for use with the default file
	 *         system.
	 * @throws BaseException
	 *           if {@code patternKind} is not supported by the file system, or if {@code pattern} is invalid.
	 */

	public static LocationFilter exclude(
		PatternKind	patternKind,
		String		pattern)
		throws BaseException
	{
		return new LocationFilter(Kind.EXCLUDE, patternKind, pattern, null);
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
		return accept(location, List.of(filters));
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

		return (obj instanceof LocationFilter other) && (kind == other.kind) && (patternKind == other.patternKind)
				&& Objects.equals(pattern, other.pattern);
	}

	//------------------------------------------------------------------

	/**
	 * {@inheritDoc}
	 */

	@Override
	public int hashCode()
	{
		int code = Objects.hashCode(kind);
		code = 31 * code + Objects.hashCode(patternKind);
		code = 31 * code + Objects.hashCode(pattern);
		return code;
	}

	//------------------------------------------------------------------

////////////////////////////////////////////////////////////////////////
//  Instance methods
////////////////////////////////////////////////////////////////////////

	/**
	 * Returns the kind of this filter.
	 *
	 * @return the kind of this filter.
	 */

	public Kind getKind()
	{
		return kind;
	}

	//------------------------------------------------------------------

	/**
	 * Returns {@code true} if this is an inclusive filter, {@code false} if this is an exclusive filter.
	 *
	 * @return {@code true} if this is an inclusive filter, {@code false} if this is an exclusive filter.
	 */

	public boolean isInclusive()
	{
		return (kind == Kind.INCLUDE);
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
		if (patternKind == null)
			return true;

		if (matcher != null)
		{
			if (patternKind.isFilename())
			{
				Path filename = location.getFileName();
				return matcher.matches((filename == null) ? Path.of("") : filename);
			}
			return matcher.matches(location);
		}
		return false;
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


	// ENUMERATION: KINDS OF FILTER


	/**
	 * This is an enumeration of the available kinds of {@linkplain LocationFilter location filter}.
	 */

	public enum Kind
	{

	////////////////////////////////////////////////////////////////////
	//  Constants
	////////////////////////////////////////////////////////////////////

		/**
		 * An inclusive filter.
		 */
		INCLUDE,

		/**
		 * An exclusive filter.
		 */
		EXCLUDE;

	////////////////////////////////////////////////////////////////////
	//  Instance variables
	////////////////////////////////////////////////////////////////////

		/** The key that is associated with this kind of location filter. */
		private	String	key;

	////////////////////////////////////////////////////////////////////
	//  Constructors
	////////////////////////////////////////////////////////////////////

		/**
		 * Creates a new instance of a kind of location filter.
		 */

		private Kind()
		{
			// Initialise instance variables
			key = name().toLowerCase();
		}

		//--------------------------------------------------------------

	////////////////////////////////////////////////////////////////////
	//  Class methods
	////////////////////////////////////////////////////////////////////

		/**
		 * Returns the kind of location filter that is associated with the specified key.
		 *
		 * @param  key
		 *           the key whose associated kind of location filter is desired.
		 * @return the kind of location filter that is associated with {@code key}, or {@code null} if there is no such
		 *         kind of filter.
		 */

		public static Kind forKey(
			String	key)
		{
			return Stream.of(values())
					.filter(value -> value.key.equals(key))
					.findFirst()
					.orElse(null);
		}

		//--------------------------------------------------------------

	////////////////////////////////////////////////////////////////////
	//  Instance methods : overriding methods
	////////////////////////////////////////////////////////////////////

		/**
		 * {@inheritDoc}
		 */

		@Override
		public String toString()
		{
			return StringUtils.firstCharToUpperCase(key);
		}

		//--------------------------------------------------------------

	////////////////////////////////////////////////////////////////////
	//  Instance methods
	////////////////////////////////////////////////////////////////////

		/**
		 * Returns the key that is associated with this kind of location filter.
		 *
		 * @return the key that is associated with this kind of location filter.
		 */

		public String key()
		{
			return key;
		}

		//--------------------------------------------------------------

	}

	//==================================================================

}

//----------------------------------------------------------------------
