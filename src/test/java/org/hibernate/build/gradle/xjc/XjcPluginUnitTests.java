package org.hibernate.build.gradle.xjc;

import org.gradle.api.Project;
import org.gradle.api.Task;
import org.gradle.testfixtures.ProjectBuilder;

import org.junit.jupiter.api.Test;

/**
 * @author Steve Ebersole
 */
public class XjcPluginUnitTests {
	@Test
	public void testPlugin() {
		final Project project = ProjectBuilder.builder().build();
		project.getRepositories().add( project.getRepositories().mavenCentral() );

		project.getPlugins().apply( XjcPlugin.class );

		final XjcExtension xjcExtension = (XjcExtension) project.getExtensions().getByName( "xjc" );
		xjcExtension.getSchemas().create( "schema1" );
		xjcExtension.getSchemas().create( "schema2" );

		final XjcTask xjcSchema1 = (XjcTask) project.getTasks().getByName( "xjcSchema1" );
		xjcSchema1.getOutputDirectory().set( project.file( "path/to/output/schema1" ) );
		xjcSchema1.getXsdFile().set( project.file( "path/to/schema1.xsd" ) );
		xjcSchema1.getXjcBindingFile().set( project.file( "path/to/schema1.xjc" ) );

		project.getTasks().getByName( "xjcSchema2" );
	}
}
