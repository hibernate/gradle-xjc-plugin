package org.hibernate.build.gradle.xjc;

import org.gradle.api.NamedDomainObjectFactory;
import org.gradle.api.Project;
import org.gradle.api.plugins.JavaPluginConvention;
import org.gradle.api.tasks.SourceSet;

/**
 * Used to inject the "domain object name" and project into the SchemaDescriptor as it is
 * created so that it can set some sensible defaults values
 *
 * @author Steve Ebersole
 */
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

		final XjcTask xjcTask = project.getTasks().create(
				determineXjcTaskName( schemaDescriptor ), XjcTask.class, schemaDescriptor, xjcExtension, project
		);
		xjcTask.getOutputDirectory().convention( xjcExtension.getOutputDirectory().dir( name ) );

		final SourceSet mainSourceSet = project.getConvention()
				.getPlugin( JavaPluginConvention.class )
				.getSourceSets()
				.findByName( SourceSet.MAIN_SOURCE_SET_NAME );
		mainSourceSet.getJava().srcDir( xjcTask.getOutputDirectory() );

		project.getTasks().getByName( "xjc" ).dependsOn( xjcTask );

		return schemaDescriptor;
	}

	private static String determineXjcTaskName(SchemaDescriptor schemaDescriptor) {
		assert schemaDescriptor.getName() != null;

		final char initialLetterCap = Character.toUpperCase( schemaDescriptor.getName().charAt( 0 ) );
		final String rest = schemaDescriptor.getName().substring( 1 );

		return "xjc" + initialLetterCap + rest;
	}
}
