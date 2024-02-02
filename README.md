# Liquibase Maven resource filter

This is a file filter for the Maven resources plugin.
It is used to filter Liquibase XML files, to only include certain contexts, and skip others.
As this is used by maven-resources-plugin, the source files are not changed.
Only the files that end up in the target directory are.

## Usage
```
<plugin>
	<groupId>org.apache.maven.plugins</groupId>
	<artifactId>maven-resources-plugin</artifactId>
	<version>3.0.1</version>
	<executions>
		<execution>
			<id>copy-and-filter-remote-resources</id>
			<phase>generate-resources</phase>
			<goals>
				<goal>copy-resources</goal>
			</goals>
			<configuration>
				<resources>
					<resource>
						<directory>${project.build.directory}/maven-shared-archive-resources/</directory>
						<include>**/*.xml</include>
						<filtering>true</filtering>
					</resource>
					<resource>
						<directory>${project.build.directory}/maven-shared-archive-resources/</directory>
						<exclude>**/*.xml</exclude>
						<filtering>false</filtering>
					</resource>
				</resources>
				<outputDirectory>target/classes/liquibase</outputDirectory>
				<mavenFilteringHints>
					<mavenFilteringHint>LiquibaseResourceFilter</mavenFilteringHint>
				</mavenFilteringHints>
			</configuration>
		</execution>
	</executions>
	<dependencies>
		<dependency>
			<groupId>org.jurr.liquibase.maven.resourcefilter</groupId>
			<artifactId>liquibase-maven-resource-filter</artifactId>
			<version>1.0.0-SNAPSHOT</version>
		</dependency>
	</dependencies>
</plugin>
```

The plugin reads the Liquibase properties that you set for the org.liquibase:liquibase-maven-plugin plugin.
The contexts given with -Dliquibase.contexts parameter (or in the -Dliquibase.propertyFile) are included.
ChangeSets without context are included, as Liquibase would also include them during run.

## Caveats
Files that do not have the `.xml` extension will not be filtered, but only copied.
Files that do have the `.xml` extension are expected to be valid XML files.
They do not have to be Liquibase files, but do have to contain some XML tags.