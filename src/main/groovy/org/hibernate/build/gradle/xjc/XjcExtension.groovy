package org.hibernate.build.gradle.xjc

import org.gradle.api.NamedDomainObjectContainer
import org.gradle.api.Project

/**
 * @author Steve Ebersole
 */
class XjcExtension {
	final Project project

	boolean xjcConfigurationSupplementsDefaultDependencies = true

//	String xjcTaskName = 'com.sun.tools.xjc.XJCTask'
	String xjcTaskName = 'org.jvnet.jaxb2_commons.xjc.XJC2Task'

	File outputDir

	final NamedDomainObjectContainer<SchemaDescriptor> schemas;

	public XjcExtension(Project project) {
		this.project = project;

		// for now, although ultimately I think we want to move generated-src out of buildDir...
		outputDir = project.file( "${project.buildDir}/generated-src/xjc/main" )

		// Create a dynamic container for SchemaDescriptor definitions by the user
		schemas = project.container( SchemaDescriptor, new SchemaDescriptorFactory( project ) )
	}

	def schemas(Closure closure) {
		schemas.configure( closure )
	}
}
