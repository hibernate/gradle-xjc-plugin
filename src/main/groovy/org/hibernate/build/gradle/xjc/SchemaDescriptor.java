package org.hibernate.build.gradle.xjc;

import java.util.HashSet;
import java.util.Set;

import org.gradle.api.Named;
import org.gradle.api.Project;
import org.gradle.api.file.RegularFileProperty;
import org.gradle.api.provider.Property;
import org.gradle.api.tasks.Input;
import org.gradle.api.tasks.InputFile;

/**
 * @author Steve Ebersole
 */
@SuppressWarnings("UnstableApiUsage")
public class SchemaDescriptor implements Named {
	private final String name;
	private final Project project;

	private final RegularFileProperty xsdFile;
	private final RegularFileProperty xjcBindingFile;
	private Set<String> xjcExtensions = new HashSet<>();

	private final Property<String> jaxbVersion;

	public SchemaDescriptor(String name, Project project) {
		this.name = name;
		this.project = project;

		xsdFile = project.getObjects().fileProperty();
		xjcBindingFile = project.getObjects().fileProperty();

		jaxbVersion = project.getObjects().property( String.class );
		jaxbVersion.convention( "2.0" );
	}

	@Override
	public final String getName() {
		return name;
	}

	public Property<String> getJaxbVersion() {
		return jaxbVersion;
	}

	@InputFile
	public RegularFileProperty getXsdFile() {
		return xsdFile;
	}

	public void setXsdFile(Object reference) {
		xsdFile.set( project.file( reference ) );
	}

	public void xsdFile(Object reference) {
		setXsdFile( reference );
	}

	@InputFile
	public RegularFileProperty getXjcBindingFile() {
		return xjcBindingFile;
	}

	public void setXjcBindingFile(Object reference) {
		xjcBindingFile.set( project.file( reference ) );
	}

	public void xjcBindingFile(Object reference) {
		setXjcBindingFile( reference );
	}

	@Input
	public Set<String> getXjcExtensions() {
		return xjcExtensions;
	}

	public void setXjcExtensions(Set<String> xjcExtensions) {
		this.xjcExtensions = xjcExtensions;
	}
}
