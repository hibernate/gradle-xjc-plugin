package org.hibernate.build.gradle.xjc

import org.gradle.api.Project

/**
 * @author Steve Ebersole
 */
class SchemaDescriptor {
	final String name;
	final Project project;

	File xsd;
	File xjcBinding;
	String packageName

	String jaxbVersion = '2.0'

	Set<String> xjcExtensions = [];

	SchemaDescriptor(Project project, String name) {
		this.name = name
		this.project = project
	}
}
