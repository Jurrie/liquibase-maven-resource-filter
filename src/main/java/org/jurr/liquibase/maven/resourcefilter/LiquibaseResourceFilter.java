package org.jurr.liquibase.maven.resourcefilter;

import java.util.Collections;
import java.util.List;

import javax.inject.Inject;

import org.apache.maven.shared.filtering.DefaultMavenResourcesFiltering;
import org.apache.maven.shared.filtering.MavenFilteringException;
import org.apache.maven.shared.filtering.MavenResourcesExecution;
import org.apache.maven.shared.filtering.MavenResourcesFiltering;
import org.codehaus.plexus.component.annotations.Component;
import org.sonatype.plexus.build.incremental.BuildContext;

@Component(role = MavenResourcesFiltering.class, hint = "LiquibaseResourceFilter")
public class LiquibaseResourceFilter implements MavenResourcesFiltering
{
	private final BuildContext buildContext;

	@Inject
	public LiquibaseResourceFilter(final BuildContext buildContext)
	{
		this.buildContext = buildContext;
	}

	@Override
	public List<String> getDefaultNonFilteredFileExtensions()
	{
		return Collections.emptyList();
	}

	@Override
	public boolean filteredFileExtension(final String fileName, final List<String> userNonFilteredFileExtensions)
	{
		return fileName.toLowerCase().endsWith(".xml");
	}

	@Override
	public void filterResources(MavenResourcesExecution mavenResourcesExecution) throws MavenFilteringException
	{
		new DefaultMavenResourcesFiltering(new LiquibaseMavenFileFilter(buildContext, mavenResourcesExecution.getMavenProject(), mavenResourcesExecution.getMavenSession()), buildContext).filterResources(mavenResourcesExecution);
	}
}
