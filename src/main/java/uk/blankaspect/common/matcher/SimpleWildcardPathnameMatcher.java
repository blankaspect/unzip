/*====================================================================*\

SimpleWildcardPathnameMatcher.java

Class: simple file-system pathname matcher that supports wildcards.

\*====================================================================*/


// PACKAGE


package uk.blankaspect.common.matcher;

//----------------------------------------------------------------------


// IMPORTS


import java.io.File;

import java.util.ArrayList;
import java.util.List;

import uk.blankaspect.common.string.StringUtils;

//----------------------------------------------------------------------


// CLASS: SIMPLE FILE-SYSTEM PATHNAME MATCHER THAT SUPPORTS WILDCARDS


public class SimpleWildcardPathnameMatcher
{

////////////////////////////////////////////////////////////////////////
//  Constants
////////////////////////////////////////////////////////////////////////

	public static final		char	NAME_SINGLE_WILDCARD_CHAR	= '?';
	public static final		char	NAME_MULTIPLE_WILDCARD_CHAR	= '*';
	public static final		String	PATH_WILDCARD	= "**";

	private static final	char	SEPARATOR_CHAR	= '/';
	private static final	String	SEPARATOR		= Character.toString(SEPARATOR_CHAR);

////////////////////////////////////////////////////////////////////////
//  Instance variables
////////////////////////////////////////////////////////////////////////

	private	String		pattern;
	private	boolean		ignoreCase;
	private	List<Token>	tokens;

////////////////////////////////////////////////////////////////////////
//  Constructors
////////////////////////////////////////////////////////////////////////

	public SimpleWildcardPathnameMatcher(
		String	pattern,
		boolean	ignoreCase)
	{
		// Validate arguments
		if (pattern == null)
			throw new IllegalArgumentException("Null pattern");

		// Initialise instance variables
		this.pattern = pattern;
		this.ignoreCase = ignoreCase;
		tokens = new ArrayList<>();

		// Fix up pattern for directory
		pattern = pattern.replace(File.separatorChar, SEPARATOR_CHAR);
		if (pattern.endsWith(SEPARATOR))
			pattern += PATH_WILDCARD;

		// Decompose pattern into tokens
		boolean pathWildcard = false;
		for (String str : StringUtils.split(pattern, SEPARATOR_CHAR))
		{
			// Case: path wildcard
			if (PATH_WILDCARD.equals(str))
				pathWildcard = true;

			// Case: not path wildcard
			else
			{
				// Replace sequence of path wildcards with single path-wildcard token
				if (pathWildcard)
				{
					tokens.add(new Token());
					pathWildcard = false;
				}

				// Add non-path-wildcard token
				tokens.add(new Token(str));
			}
		}

		// Replace pending sequence of path wildcards with single path-wildcard token
		if (pathWildcard)
			tokens.add(new Token());
	}

	//------------------------------------------------------------------

////////////////////////////////////////////////////////////////////////
//  Class methods
////////////////////////////////////////////////////////////////////////

	public static SimpleWildcardPathnameMatcher respectCase(
		String	pattern)
	{
		return new SimpleWildcardPathnameMatcher(pattern, false);
	}

	//------------------------------------------------------------------

	public static SimpleWildcardPathnameMatcher ignoreCase(
		String	pattern)
	{
		return new SimpleWildcardPathnameMatcher(pattern, true);
	}

	//------------------------------------------------------------------

////////////////////////////////////////////////////////////////////////
//  Instance methods
////////////////////////////////////////////////////////////////////////

	public String getPattern()
	{
		return pattern;
	}

	//------------------------------------------------------------------

	public boolean match(
		String	pathname)
	{
		// No match if pathname is null
		if (pathname == null)
			return false;

		// Split pathname into its elements
		List<String> elements = StringUtils.split(pathname.replace(File.separatorChar, SEPARATOR_CHAR), SEPARATOR_CHAR);

		// Match pathname elements against pattern tokens
		return matchTail(elements, 0, 0);
	}

	//------------------------------------------------------------------

	private boolean matchTail(
		List<String>	elements,
		int				elementIndex,
		int				tokenIndex)
	{
		// Get number of pathname elements and number of pattern tokens
		int numElements = elements.size();
		int numTokens = tokens.size();

		// Match pathname elements against pattern tokens
		while (tokenIndex < numTokens)
		{
			// Get next pattern token
			Token token = tokens.get(tokenIndex++);

			// If token is path wildcard, match pathname elements against remaining tokens ...
			if (token.pathWildcard)
			{
				// If path wildcard is final token, it matches all remaining elements
				if (tokenIndex >= numTokens)
					return true;

				// Match pathname elements against remaining tokens
				for (int i = elementIndex; i < numElements; i++)
				{
					if (matchTail(elements, i, tokenIndex))
						return true;
				}
				return false;
			}

			// ... otherwise, if there are more pathname elements, match next element against pattern token
			if ((elementIndex >= numElements) || !token.match(elements.get(elementIndex++), ignoreCase))
				return false;
		}

		// Test whether all pathname elements have been matched against all pattern tokens
		return (elementIndex >= numElements) && (tokenIndex >= numTokens);
	}

	//------------------------------------------------------------------

////////////////////////////////////////////////////////////////////////
//  Member classes : non-inner classes
////////////////////////////////////////////////////////////////////////


	// CLASS: TOKEN


	private static class Token
	{

	////////////////////////////////////////////////////////////////////
	//  Instance variables
	////////////////////////////////////////////////////////////////////

		private	boolean							pathWildcard;
		private	String							pattern;
		private	SimpleWildcardPatternMatcher	matcher;

	////////////////////////////////////////////////////////////////////
	//  Constructors
	////////////////////////////////////////////////////////////////////

		private Token()
		{
			// Initialise instance variables
			pathWildcard = true;
		}

		//--------------------------------------------------------------

		private Token(
			String	pattern)
		{
			// Initialise instance variables
			this.pattern = pattern;
		}

		//--------------------------------------------------------------

	////////////////////////////////////////////////////////////////////
	//  Instance methods
	////////////////////////////////////////////////////////////////////

		private boolean match(
			String	name,
			boolean	ignoreCase)
		{
			// Test for path wildcard
			if (pathWildcard)
				throw new IllegalStateException("Cannot match against path wildcard");

			// Initialise matcher, if necessary
			if (matcher == null)
			{
				matcher = ignoreCase ? SimpleWildcardPatternMatcher.allIgnoreCase(pattern)
									 : SimpleWildcardPatternMatcher.all(pattern);
			}

			// Return result of matching name against token
			return matcher.match(name);
		}

		//--------------------------------------------------------------

	}

	//==================================================================

}

//----------------------------------------------------------------------
