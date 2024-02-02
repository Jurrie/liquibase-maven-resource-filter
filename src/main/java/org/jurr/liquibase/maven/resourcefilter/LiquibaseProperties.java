package org.jurr.liquibase.maven.resourcefilter;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Collections;
import java.util.HashSet;
import java.util.Properties;
import java.util.Set;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import org.apache.maven.shared.utils.StringUtils;

public class LiquibaseProperties
{
	private static final String LIQUIBASE_PREPEND = "liquibase.";
	private static final String LIQUIBASE_CONTEXTS_PROPERTY = "contexts";

	private final Set<String> contexts;

	public LiquibaseProperties(@Nonnull final Properties mavenProjectProperties, @Nonnull final Properties mavenUserProperties) throws IOException
	{
		final Properties liquibaseProperties = loadLiquibasePropertiesFile(mavenProjectProperties, mavenUserProperties);

		final String allContexts = resolveOptionalLiquibaseProperty(LIQUIBASE_CONTEXTS_PROPERTY, liquibaseProperties, mavenProjectProperties, mavenUserProperties);
		contexts = splitContexts(allContexts);
	}

	@Nonnull
	public static Set<String> splitContexts(@Nullable final String allContexts)
	{
		if (StringUtils.isNotBlank(allContexts))
		{
			final String[] contextsArray = StringUtils.split(allContexts, ",");
			final HashSet<String> result = new HashSet<>(contextsArray.length);
			Collections.addAll(result, contextsArray);
			return Collections.unmodifiableSet(result);
		}
		else
		{
			return Collections.emptySet();
		}
	}

	@Nonnull
	public Set<String> getContexts()
	{
		return contexts;
	}

	@Nonnull
	private Properties loadLiquibasePropertiesFile(@Nonnull final Properties mavenProjectProperties, @Nonnull final Properties mavenUserProperties) throws IOException
	{
		final Properties liquibaseProperties = new Properties();

		// First try to load the property file, then try to load the individual properties
		String liquibasePropertyFileString = mavenProjectProperties.getProperty(LIQUIBASE_PREPEND + "propertyFile");

		if (!StringUtils.isBlank(liquibasePropertyFileString))
		{
			liquibasePropertyFileString = mavenUserProperties.getProperty(LIQUIBASE_PREPEND + "propertyFile");
		}

		if (!StringUtils.isBlank(liquibasePropertyFileString))
		{
			final File liquibasePropertyFile = new File(liquibasePropertyFileString);
			if (liquibasePropertyFile.exists())
			{
				try (InputStream liquibasePropertyFileInputStream = new FileInputStream(liquibasePropertyFile))
				{
					liquibaseProperties.load(liquibasePropertyFileInputStream);
				}
			}
		}

		return liquibaseProperties;
	}

	@Nullable
	private static String resolveOptionalLiquibaseProperty(@Nonnull final String propertyName, @Nonnull final Properties liquibaseProperties, @Nonnull final Properties mavenProjectProperties, @Nonnull final Properties mavenUserProperties)
	{
		String value = mavenProjectProperties.getProperty(LIQUIBASE_PREPEND + propertyName);
		if (StringUtils.isNotBlank(value))
		{
			return value;
		}

		value = mavenUserProperties.getProperty(LIQUIBASE_PREPEND + propertyName);
		if (StringUtils.isNotBlank(value))
		{
			return value;
		}

		value = mavenUserProperties.getProperty(LIQUIBASE_PREPEND + propertyName);
		if (StringUtils.isNotBlank(value))
		{
			return value;
		}

		value = (String) liquibaseProperties.get(propertyName);
		if (StringUtils.isNotBlank(value))
		{
			return value;
		}

		return null;
	}
}