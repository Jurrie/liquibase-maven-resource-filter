package org.jurr.liquibase.maven.resourcefilter;

import static org.junit.Assert.assertEquals;

import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.Properties;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.xml.stream.XMLStreamException;

import org.apache.maven.shared.filtering.MavenFilteringException;
import org.junit.Test;

public class LiquibaseFileFilterTest
{
	// TODO: Unit test that tests that input and output files are identical (so Liquibase MD5 hashes do not differ)

	// TODO: Unit test for LiquibaseMavenFileFilter that tests for empty files and no xml files (actually: test that when XMLStreamException occurrs, we just continue)

	// TODO: Unit test that tests that properties of session and/or mavenproject are honored and overriding is correct

	@Test(expected = XMLStreamException.class)
	public void testEmptyFile() throws IOException, MavenFilteringException, XMLStreamException
	{
		final String input = "";
		final LiquibaseProperties liquibaseProperties = getDummyLiquibaseProperties("");

		final StringWriter output = new StringWriter();
		LiquibaseFileFilter.filter(liquibaseProperties, new StringReader(input), output);

		assertEquals(input, output.toString());
	}

	@Test(expected = XMLStreamException.class)
	public void testNoXmlFile() throws IOException, MavenFilteringException, XMLStreamException
	{
		final String input = "This is not an XML file, so it will give parse errors.";
		final LiquibaseProperties liquibaseProperties = getDummyLiquibaseProperties("");

		final StringWriter output = new StringWriter();
		LiquibaseFileFilter.filter(liquibaseProperties, new StringReader(input), output);
	}

	@Test
	public void testNoLiquibaseFile() throws IOException, MavenFilteringException, XMLStreamException
	{
		final String input = "<?xml version=\"1.0\"?><xml><tag></tag></xml>";
		final LiquibaseProperties liquibaseProperties = getDummyLiquibaseProperties("");

		final StringWriter output = new StringWriter();
		LiquibaseFileFilter.filter(liquibaseProperties, new StringReader(input), output);

		assertEquals(input, output.toString());
	}

	@Test
	public void testFilterContextFile() throws IOException, MavenFilteringException, XMLStreamException
	{
		final StringBuilder in = new StringBuilder();
		final StringBuilder out = new StringBuilder();
		add(in, out, "<?xml version=\"1.0\"?>");
		add(in, out, "<databaseChangeLog xmlns=\"http://www.liquibase.org/xml/ns/dbchangelog\">\n");
		add(in, out, "	<changeSet author=\"j.doe\" context=\"includeCtx\" id=\"ID1\">\n");
		add(in, out, "		<!-- This should be included because 'includeCtx' is in the list of contexts -->");
		add(in, out, "	</changeSet>");
		add(in, null, "	<changeSet author=\"j.doe\" context=\"excludeCtx\" id=\"ID2\">\n");
		add(in, null, "		<!-- This should be excluded because 'excludeCtx' is not in the list of contexts -->");
		add(in, null, "	</changeSet>");
		add(in, out, "	<changeSet author=\"j.doe\" id=\"ID3\">\n");
		add(in, out, "		<!-- This should be included because there is no context given -->");
		add(in, out, "	</changeSet>");
		add(in, out, "	<changeSet author=\"j.doe\" context=\"testCtx,includeCtx,anotherTestCtx\" id=\"ID4\">\n");
		add(in, out, "		<!-- This should be included because 'includeCtx' is in the list of contexts -->");
		add(in, out, "	</changeSet>");
		add(in, out, "</databaseChangeLog>");
		final LiquibaseProperties liquibaseProperties = getDummyLiquibaseProperties("includeCtx");

		final StringWriter output = new StringWriter();
		LiquibaseFileFilter.filter(liquibaseProperties, new StringReader(in.toString()), output);

		assertEquals(out.toString(), output.toString());
	}

	@Test
	public void testFilterWholeFile() throws IOException, MavenFilteringException, XMLStreamException
	{
		final StringBuilder in = new StringBuilder();
		final StringBuilder out = new StringBuilder();
		add(in, out, "<?xml version=\"1.0\"?>");
		add(in, out, "<databaseChangeLog xmlns=\"http://www.liquibase.org/xml/ns/dbchangelog\">\n");
		add(in, null, "	<changeSet author=\"j.doe\" context=\"excludeCtx\" id=\"ID2\">\n");
		add(in, null, "		<!-- This should be excluded because 'excludeCtx' is not in the list of contexts -->");
		add(in, null, "	</changeSet>");
		add(null, out, "	");
		add(in, out, "</databaseChangeLog>");
		final LiquibaseProperties liquibaseProperties = getDummyLiquibaseProperties("");

		final StringWriter output = new StringWriter();
		LiquibaseFileFilter.filter(liquibaseProperties, new StringReader(in.toString()), output);

		assertEquals(out.toString(), output.toString());
	}

	private void add(@Nullable final StringBuilder in, @Nullable final StringBuilder out, @Nonnull final String msg)
	{
		if (in != null)
		{
			in.append(msg);
		}
		if (out != null)
		{
			out.append(msg);
		}
	}

	@Nonnull
	private LiquibaseProperties getDummyLiquibaseProperties(@Nonnull final String contexts) throws IOException
	{
		final Properties mavenSessionUserProperties = new Properties();
		mavenSessionUserProperties.setProperty("liquibase.url", "url");
		mavenSessionUserProperties.setProperty("liquibase.username", "username");
		mavenSessionUserProperties.setProperty("liquibase.password", "password");
		mavenSessionUserProperties.setProperty("liquibase.driver", "driver");
		mavenSessionUserProperties.setProperty("liquibase.contexts", contexts);
		return new LiquibaseProperties(new Properties(), mavenSessionUserProperties);
	}
}