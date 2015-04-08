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

		// Create the Gradle Configuration for users to be able to specify JAXB/XJC dependencies...
		Configuration configuration = project.getConfigurations().maybeCreate( "xjc" )
		configuration.description = 'Dependencies for running xjc (JAXB class generation)'

		// The most recent xjc tasks use slf4j, so pass along Gradle's slf4j
		project.dependencies.add( "xjc", project.dependencies.gradleApi() )

		// Create the Plugin extension object (for users to configure our execution).
		XjcExtension extension = project.getExtensions().create( "xjc", XjcExtension, project )

		// Create the xjc task
		Task jaxbTask = project.tasks.maybeCreate( "xjc" )
		jaxbTask.group = 'sourceGeneration'
		jaxbTask.description = 'Executes XJC for generation of a JAXB binding model for a XSD.'
		// initially we create it as disabled.  We will enable it later after we know we have
		// some SchemaDescriptors to process
		jaxbTask.enabled = false

		SourceSet mainSourceSet = project.convention.getPlugin( JavaPluginConvention ).sourceSets.findByName( "main" );
		mainSourceSet.java.srcDir( extension.outputDir )
		project.tasks.findByName( mainSourceSet.compileJavaTaskName ).dependsOn jaxbTask

		project.afterEvaluate(
				new Action<Project>() {
					@Override
					void execute(Project evaluatedProject) {
						if ( extension.schemas.empty ) {
							return;
						}

						jaxbTask.enabled = true

						jaxbTask.outputs.file( extension.outputDir )

						jaxbTask.inputs.files( extension.schemas*.xsd )
						jaxbTask.inputs.files( extension.schemas*.xjcBinding )

						jaxbTask.doLast {
							extension.outputDir.mkdirs()

							project.ant.taskdef(name: 'xjc', classname: extension.xjcTaskName, classpath: configuration.asPath)

							extension.schemas.all{ SchemaDescriptor descriptor->
								if ( descriptor.xjcExtensions.empty ) {
									project.ant.xjc(
											destdir: extension.outputDir,
											binding: descriptor.xjcBinding,
											schema: descriptor.xsd,
											target: descriptor.jaxbVersion
									)
								}
								else {
									project.ant.xjc(
											destdir: extension.outputDir,
											binding: descriptor.xjcBinding,
											schema: descriptor.xsd,
											target: descriptor.jaxbVersion,
											extension: 'true') {
										arg line: descriptor.xjcExtensions.collect{ "-X${it}" }.join( " " )
									}
								}
							}
						}

					}
				}
		)
	}
}
