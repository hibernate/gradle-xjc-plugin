package org.hibernate.build.gradle.xjc;

import org.gradle.api.NamedDomainObjectFactory;
import org.gradle.api.Project;

/**
 * Used to inject the "domain object name" and project into the SchemaDescriptor as it is
 * created so that it can set some sensible defaults values
 *
 * @author Steve Ebersole
 */
@SuppressWarnings("UnstableApiUsage")
public class SchemaDescriptorFactory implements NamedDomainObjectFactory<SchemaDescriptor> {
	private final XjcExtension xjcExtension;
	private final Project project;

	public SchemaDescriptorFactory(XjcExtension xjcExtension, Project project) {
		this.xjcExtension = xjcExtension;
		this.project = project;
	}

	@Override
	public SchemaDescriptor create(String name) {
		final SchemaDescriptor schemaDescriptor = new SchemaDescriptor( name, project );
		project.getTasks().create(
				determineXjcTaskName(schemaDescriptor), XjcTask.class, schemaDescriptor, xjcExtension, project
		);
		return schemaDescriptor;
	}

	private static String determineXjcTaskName(SchemaDescriptor schemaDescriptor) {
		assert schemaDescriptor.getName() != null;

		final char initialLetterCap = Character.toUpperCase( schemaDescriptor.getName().charAt( 0 ) );
		final String rest = schemaDescriptor.getName().substring( 1 );

		return "xjc" + initialLetterCap + rest;
	}
}
