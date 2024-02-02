package org.jurr.liquibase.maven.resourcefilter;

import java.io.Reader;
import java.io.Writer;
import java.util.Set;

import javax.annotation.Nonnull;
import javax.xml.namespace.QName;
import javax.xml.stream.FactoryConfigurationError;
import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLEventWriter;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.events.Attribute;
import javax.xml.stream.events.Comment;
import javax.xml.stream.events.StartElement;
import javax.xml.stream.events.XMLEvent;

import org.apache.maven.shared.filtering.MavenFilteringException;

public final class LiquibaseFileFilter
{
	private static final QName CHANGESET_TAG = new QName("http://www.liquibase.org/xml/ns/dbchangelog", "changeSet");
	private static final QName CONTEXT_ATTRIBUTE = new QName(null, "context");
	
	private LiquibaseFileFilter()
	{
	}

	public static void filter(@Nonnull final LiquibaseProperties liquibaseProperties, @Nonnull final Reader input, @Nonnull final Writer output) throws MavenFilteringException, XMLStreamException
	{
		try
		{
			final XMLEventReader reader = XMLInputFactory.newInstance().createXMLEventReader(input);
			final XMLEventWriter writer = XMLOutputFactory.newInstance().createXMLEventWriter(output);

			filter(liquibaseProperties, reader, writer);

			reader.close();
			writer.close();
		}
		catch (FactoryConfigurationError e)
		{
			throw new MavenFilteringException(e.getMessage(), e);
		}
	}

	private static void filter(@Nonnull final LiquibaseProperties liquibaseProperties, @Nonnull final XMLEventReader input, @Nonnull final XMLEventWriter output) throws XMLStreamException
	{
		while (input.hasNext())
		{
			final XMLEvent nextEvent = input.nextEvent();

			if (nextEvent.isStartElement())
			{
				final StartElement startElement = (StartElement) nextEvent;
				if (startElement.getName().equals(CHANGESET_TAG))
				{
					handleChangeSetStartElement(liquibaseProperties, input, output, (StartElement) nextEvent);
				}
				else
				{
					output.add(nextEvent);
				}
			}
			else
			{
				output.add(nextEvent);
			}
		}
	}

	private static void handleChangeSetStartElement(@Nonnull final LiquibaseProperties liquibaseProperties, @Nonnull final XMLEventReader input, @Nonnull final XMLEventWriter output, @Nonnull final StartElement startElement) throws XMLStreamException
	{
		final Attribute attribute = startElement.getAttributeByName(CONTEXT_ATTRIBUTE);
		if (attribute != null && changeSetShouldBeSkipped(attribute, liquibaseProperties))
		{
			skipToMatchingEndElement(input);
			skipTextElements(input);
		}
		else
		{
			output.add(startElement);
		}
	}

	private static void skipToMatchingEndElement(@Nonnull final XMLEventReader input) throws XMLStreamException
	{
		int nesting = 1; // We already have read the first start element
		while (input.hasNext())
		{
			final XMLEvent next = input.nextEvent();
			if (next.isStartElement())
			{
				nesting++;
			}
			else if (next.isEndElement())
			{
				nesting--;
			}

			if (nesting == 0)
			{
				break;
			}
		}
	}

	private static void skipTextElements(@Nonnull final XMLEventReader input) throws XMLStreamException
	{
		while (input.hasNext())
		{
			final XMLEvent next = input.peek();
			if (next.isCharacters() || next instanceof Comment)
			{
				input.nextEvent();
			}
			else
			{
				break;
			}
		}
	}

	private static boolean changeSetShouldBeSkipped(@Nonnull final Attribute attribute, @Nonnull final LiquibaseProperties liquibaseProperties)
	{
		final String allContexts = attribute.getValue();
		final Set<String> contexts = LiquibaseProperties.splitContexts(allContexts);

		if (!contexts.isEmpty())
		{
			for (String ctx : contexts)
			{
				if (liquibaseProperties.getContexts().contains(ctx))
				{
					return false;
				}
			}
		}

		return true;
	}
}