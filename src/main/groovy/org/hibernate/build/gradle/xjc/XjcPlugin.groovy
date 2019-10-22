package org.hibernate.build.gradle.xjc

import org.gradle.api.Action
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.Task
import org.gradle.api.artifacts.Configuration
import org.gradle.api.plugins.JavaPlugin
import org.gradle.api.plugins.JavaPluginConvention
import org.gradle.api.tasks.SourceSet

/**
 * @author Steve Ebersole
 */
class XjcPlugin implements Plugin<Project> {
	@Override
	public void apply(Project project) {
		project.plugins.apply( JavaPlugin )

		// Create the Plugin extension object (for users to configure our execution).
		XjcExtension extension = project.getExtensions().create( "xjc", XjcExtension, project )

		// Create the Gradle Configuration for users to be able to specify JAXB/XJC dependencies...
		Configuration configuration = project.getConfigurations().maybeCreate( "xjc" )
		configuration.description = 'Dependencies for running xjc (JAXB class generation)'

		// Create the xjc task
		Task xjcTask = project.tasks.maybeCreate( "xjc" )
		xjcTask.group = 'sourceGeneration'
		xjcTask.description = 'Executes XJC for generation of a JAXB binding model for a XSD.'

		SourceSet mainSourceSet = project.convention.getPlugin( JavaPluginConvention ).sourceSets.findByName( "main" );
		project.tasks.findByName( mainSourceSet.compileJavaTaskName ).dependsOn xjcTask

		prepareDefaults( project, extension, configuration )

		project.afterEvaluate(
				new Action<Project>() {
					@Override
					void execute(Project evaluatedProject) {
						if ( extension.schemas.empty ) {
							xjcTask.enabled = false
							return;
						}

						mainSourceSet.java.srcDir( extension.outputDir )

						xjcTask.outputs.dir( extension.outputDir )

						xjcTask.inputs.files( extension.schemas*.xsd )
						xjcTask.inputs.files( extension.schemas*.xjcBinding )

						xjcTask.doLast {
							extension.outputDir.mkdirs()

							evaluatedProject.ant.taskdef(name: 'xjc', classname: extension.xjcTaskName, classpath: configuration.asPath)

							extension.schemas.all { SchemaDescriptor descriptor ->
								evaluatedProject.ant.xjc(
										destdir: extension.outputDir,
										binding: descriptor.xjcBinding,
										schema: descriptor.xsd,
										target: descriptor.jaxbVersion,
										extension: 'true') {
									if ( !descriptor.xjcExtensions.empty ) {
										arg line: descriptor.xjcExtensions.collect { "-X${it}" }.join( " " )
									}
								}
							}
						}
					}
				}
		)
	}

	static def prepareDefaults(
			Project project,
			XjcExtension xjcExtension,
			Configuration configuration) {
		configuration.incoming.beforeResolve {
			// add the default dependencies if either:
			//		1) the user did not specify any dependencies
			//		2) the user specified that any dependencies they added should supplement the defaults

			if ( configuration.dependencies.empty
					|| xjcExtension.xjcConfigurationSupplementsDefaultDependencies ) {
				configuration.dependencies.add( project.dependencies.create( 'org.glassfish.jaxb:jaxb-xjc:2.2.11' ) )
				configuration.dependencies.add( project.dependencies.create( 'org.jvnet.jaxb2_commons:jaxb2-basics:0.9.3' ) )
				configuration.dependencies.add( project.dependencies.create( 'org.jvnet.jaxb2_commons:jaxb2-basics-ant:0.9.3' ) )
			}

			// The most recent xjc tasks use slf4j, so pass along Gradle's slf4j
			configuration.dependencies.add( project.dependencies.gradleApi() )
		}
	}
}
