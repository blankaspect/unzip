/*====================================================================*\

ComparisonParams.java

Class: zip-file comparison parameters.

\*====================================================================*/


// PACKAGE


package uk.blankaspect.unzip;

//----------------------------------------------------------------------


// IMPORTS


import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;

import uk.blankaspect.common.basictree.ListNode;
import uk.blankaspect.common.basictree.MapNode;
import uk.blankaspect.common.basictree.StringNode;

import uk.blankaspect.common.exception2.UnexpectedRuntimeException;

import uk.blankaspect.common.namefilter.LocationFilter;
import uk.blankaspect.common.namefilter.PatternKind;

//----------------------------------------------------------------------


// CLASS: ZIP-FILE COMPARISON PARAMETERS


public class ComparisonParams
	implements IPersistable, Cloneable
{

////////////////////////////////////////////////////////////////////////
//  Constants
////////////////////////////////////////////////////////////////////////

	/** Keys of properties. */
	private interface PropertyKey
	{
		String	FIELDS			= "fields";
		String	FILTERS			= "filters";
		String	INCLUSIVE		= "inclusive";		// (XXX legacy)
		String	KIND			= "kind";
		String	NAME			= "name";
		String	PATTERN			= "pattern";
		String	PATTERN_KIND	= "patternKind";
	}

////////////////////////////////////////////////////////////////////////
//  Instance variables
////////////////////////////////////////////////////////////////////////

	private	String							name;
	private	List<LocationFilter>			filters;
	private	Set<ZipFileComparison.Field>	fields;
	private	boolean							persistent;

////////////////////////////////////////////////////////////////////////
//  Constructors
////////////////////////////////////////////////////////////////////////

	public ComparisonParams()
	{
		// Call alternative constructor
		this("", true);
	}

	//------------------------------------------------------------------

	public ComparisonParams(
		String	name,
		boolean	persistent)
	{
		// Call alternative constructor
		this(name, Collections.emptyList(), EnumSet.noneOf(ZipFileComparison.Field.class), persistent);
	}

	//------------------------------------------------------------------

	public ComparisonParams(
		String									name,
		Collection<? extends LocationFilter>	filters,
		Collection<ZipFileComparison.Field>		fields,
		boolean									persistent)
	{
		// Validate arguments
		if (name == null)
			throw new IllegalArgumentException("Null name");
		if (filters == null)
			throw new IllegalArgumentException("Null filters");
		if (fields == null)
			throw new IllegalArgumentException("Null fields");

		// Initialise instance variables
		this.name = name;
		this.filters = new ArrayList<>(filters);
		this.fields = fields.isEmpty() ? EnumSet.noneOf(ZipFileComparison.Field.class) : EnumSet.copyOf(fields);
		this.persistent = persistent;
	}

	//------------------------------------------------------------------

////////////////////////////////////////////////////////////////////////
//  Instance methods : IPersistable interface
////////////////////////////////////////////////////////////////////////

	/**
	 * {@inheritDoc}
	 */

	@Override
	public String getText(
		int	index)
	{
		return switch (index)
		{
			case 0  -> name;
			default -> throw new UnexpectedRuntimeException();
		};
	}

	//------------------------------------------------------------------

	/**
	 * {@inheritDoc}
	 */

	@Override
	public boolean isPersistent()
	{
		return persistent;
	}

	//------------------------------------------------------------------

	/**
	 * {@inheritDoc}
	 */

	@Override
	public void setPersistent(
		boolean	persistent)
	{
		this.persistent = persistent;
	}

	//------------------------------------------------------------------

////////////////////////////////////////////////////////////////////////
//  Instance methods : overriding methods
////////////////////////////////////////////////////////////////////////

	/**
	 * {@inheritDoc}
	 */

	@Override
	public ComparisonParams clone()
	{
		try
		{
			ComparisonParams copy = (ComparisonParams)super.clone();
			copy.filters = new ArrayList<>(filters);
			copy.fields = EnumSet.copyOf(fields);
			return copy;
		}
		catch (CloneNotSupportedException e)
		{
			throw new UnexpectedRuntimeException(e);
		}
	}

	//------------------------------------------------------------------

////////////////////////////////////////////////////////////////////////
//  Instance methods
////////////////////////////////////////////////////////////////////////

	public String getName()
	{
		return name;
	}

	//------------------------------------------------------------------

	public List<LocationFilter> getFilters()
	{
		return Collections.unmodifiableList(filters);
	}

	//------------------------------------------------------------------

	public Set<ZipFileComparison.Field> getFields()
	{
		return Collections.unmodifiableSet(fields);
	}

	//------------------------------------------------------------------

	/**
	 * Encodes this set of parameters as a tree of {@linkplain AbstractNode nodes} and returns the root of the tree.
	 *
	 * @return the root of the tree of {@linkplain AbstractNode nodes} as which this set of parameters was encoded.
	 */

	public MapNode encode()
	{
		// Create root node
		MapNode rootNode = new MapNode();

		// Encode name
		rootNode.addString(PropertyKey.NAME, name);

		// Encode filters
		if (!filters.isEmpty())
		{
			// Create filters node
			ListNode filtersNode = rootNode.addList(PropertyKey.FILTERS);

			// Encode filters
			for (LocationFilter filter : filters)
			{
				// Create filter node
				MapNode filterNode = new MapNode();
				filtersNode.add(filterNode);

				// Encode filter kind
				filterNode.addString(PropertyKey.KIND, filter.getKind().key());

				// Encode pattern kind
				PatternKind patternKind = filter.getPatternKind();
				if (patternKind != null)
					filterNode.addString(PropertyKey.PATTERN_KIND, patternKind.getKey());

				// Encode pattern
				String pattern = filter.getPattern();
				if (pattern != null)
					filterNode.addString(PropertyKey.PATTERN, pattern);
			}
		}

		// Encode fields
		if (!fields.isEmpty())
		{
			// Create fields node
			ListNode fieldsNode = rootNode.addList(PropertyKey.FIELDS);

			// Encode fields
			for (ZipFileComparison.Field field : fields)
				fieldsNode.addString(field.getKey());
		}

		// Return root node
		return rootNode;
	}

	//------------------------------------------------------------------

	/**
	 * Decodes this set of parameters from the tree of {@linkplain AbstractNode nodes} whose root is the specified node.
	 *
	 * @param rootNode
	 *          the root of the tree of {@linkplain AbstractNode nodes} from which this set of parameters will be
	 *          decoded.
	 */

	public void decode(
		MapNode	rootNode)
	{
		// Decode name
		name = rootNode.getString(PropertyKey.NAME, "");

		// Decode filters
		filters.clear();
		String key = PropertyKey.FILTERS;
		if (rootNode.hasList(key))
		{
			for (MapNode filterNode : rootNode.getListNode(key).mapNodes())
			{
				// Decode filter kind
				LocationFilter.Kind kind = filterNode.getEnumValue(LocationFilter.Kind.class, PropertyKey.KIND,
																   LocationFilter.Kind::key, null);
				// (XXX legacy)
				if (kind == null)
				{
					kind = filterNode.hasBoolean(PropertyKey.INCLUSIVE) ? LocationFilter.Kind.INCLUDE
																		: LocationFilter.Kind.EXCLUDE;
				}

				// Decode pattern kind
				PatternKind patternKind = filterNode.getEnumValue(PatternKind.class, PropertyKey.PATTERN_KIND,
																  PatternKind::getKey, null);

				// Decode pattern; add filter to list
				if (patternKind != null)
				{
					// Decode pattern
					String pattern = filterNode.getString(PropertyKey.PATTERN, null);

					// Create filter and add it to list
					if (pattern != null)
						filters.add(new LocationFilter(kind, patternKind, pattern));
				}
			}
		}

		// Decode fields
		fields.clear();
		key = PropertyKey.FIELDS;
		if (rootNode.hasList(key))
		{
			for (StringNode fieldNode : rootNode.getListNode(key).stringNodes())
			{
				ZipFileComparison.Field field = ZipFileComparison.Field.forKey(fieldNode.getValue());
				if (field != null)
					fields.add(field);
			}
		}
	}

	//------------------------------------------------------------------

}

//----------------------------------------------------------------------
