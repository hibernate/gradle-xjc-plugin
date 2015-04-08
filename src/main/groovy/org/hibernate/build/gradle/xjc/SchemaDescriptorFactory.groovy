package org.hibernate.build.gradle.xjc

import org.gradle.api.NamedDomainObjectFactory
import org.gradle.api.Project

/**
 * Used to inject the "domain object name" and project into the SchemaDescriptor as it is
 * created so that it can set some sensible defaults values
 *
 * @author Steve Ebersole
 */
class SchemaDescriptorFactory implements NamedDomainObjectFactory<SchemaDescriptor> {
	final Project project;

	SchemaDescriptorFactory(Project project) {
		this.project = project
	}

	@Override
	SchemaDescriptor create(String name) {
		return new SchemaDescriptor( project, name )
	}
}
