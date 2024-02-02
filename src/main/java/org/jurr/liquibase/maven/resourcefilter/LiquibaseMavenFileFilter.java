package org.jurr.liquibase.maven.resourcefilter;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.io.UnsupportedEncodingException;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.xml.stream.XMLStreamException;

import org.apache.maven.execution.MavenSession;
import org.apache.maven.project.MavenProject;
import org.apache.maven.shared.filtering.DefaultMavenFileFilter;
import org.apache.maven.shared.filtering.FilterWrapper;
import org.apache.maven.shared.filtering.MavenFilteringException;
import org.apache.maven.shared.utils.StringUtils;
import org.sonatype.plexus.build.incremental.BuildContext;

public class LiquibaseMavenFileFilter extends DefaultMavenFileFilter
{
	private final LiquibaseProperties liquibaseProperties;
	
	public LiquibaseMavenFileFilter(final BuildContext buildContext, final MavenProject mavenProject, final MavenSession mavenSession) throws MavenFilteringException
	{
		super(buildContext);

		try
		{
			this.liquibaseProperties = new LiquibaseProperties(mavenProject.getProperties(), mavenSession.getUserProperties());
		}
		catch (IOException e)
		{
			throw new MavenFilteringException(e.getMessage(), e);
		}
	}
	
	@Override
	public void copyFile(final File from, final File to, final boolean filtering, final List<FilterWrapper> filterWrappers, final String encoding, final boolean overwrite) throws MavenFilteringException
	{
		try
		{
			final boolean sourceFileIsEmpty = Files.size(from.toPath()) <= 0;
			if (!filtering || sourceFileIsEmpty || liquibaseProperties.getContexts().isEmpty())
			{
				super.copyFile(from, to, filtering, filterWrappers, encoding, overwrite);
				return;
			}
		}
		catch (IOException e)
		{
			throw new MavenFilteringException(e.getMessage(), e);
		}

		Path temp = null;
		try
		{
			temp = Files.createTempFile(to.toPath().getParent(), "LiquibaseMavenFileFilter", null);
			filterLiquibaseFile(from, temp, encoding);

			super.copyFile(temp.toFile(), to, filtering, filterWrappers, encoding, overwrite);

			getLogger().debug("Filtered file {} to {}", from, to);
		}
		catch (IOException e)
		{
			throw new MavenFilteringException(e.getMessage(), e);
		}
		finally
		{
			if (temp != null)
			{
				try
				{
					Files.deleteIfExists(temp);
				}
				catch (IOException e)
				{
					throw new MavenFilteringException(e.getMessage(), e);
				}
			}
		}
	}

	private void filterLiquibaseFile(@Nonnull final File from, @Nonnull final Path temp, @Nullable final String encoding) throws IOException, MavenFilteringException
	{
		try (Reader fileReader = getFileReader(encoding, from);
			 Writer fileWriter = getFileWriter(encoding, temp.toFile()))
		{
			LiquibaseFileFilter.filter(liquibaseProperties, fileReader, fileWriter);
		}
		catch (XMLStreamException e)
		{
			getLogger().warn("Unable to read file as XML: {}", from);
		}
	}

	/**
	 * @see org.apache.maven.shared.filtering.DefaultMavenFileFilter.getFileReader(String, File)
	 */
	private Reader getFileReader(final String encoding, final File from) throws FileNotFoundException, UnsupportedEncodingException
	{
		if (StringUtils.isEmpty(encoding))
		{
			return new BufferedReader(new FileReader(from));
		}
		else
		{
			FileInputStream instream = new FileInputStream(from);
			return new BufferedReader(new InputStreamReader(instream, encoding));
		}
	}

	/**
	 * @see org.apache.maven.shared.filtering.DefaultMavenFileFilter.getFileWriter(String, File)
	 */
	private Writer getFileWriter(final String encoding, final File to) throws IOException
	{
		if (StringUtils.isEmpty(encoding))
		{
			return new FileWriter(to);
		}
		else
		{
			FileOutputStream outstream = new FileOutputStream(to);

			return new OutputStreamWriter(outstream, encoding);
		}
	}
}