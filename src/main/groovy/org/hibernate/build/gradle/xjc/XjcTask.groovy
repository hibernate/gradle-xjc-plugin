package org.hibernate.build.gradle.xjc

import javax.inject.Inject

import org.gradle.api.DefaultTask
import org.gradle.api.Project
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.plugins.JavaPluginConvention
import org.gradle.api.tasks.CacheableTask
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.InputFile
import org.gradle.api.tasks.OutputDirectory
import org.gradle.api.tasks.PathSensitive
import org.gradle.api.tasks.PathSensitivity
import org.gradle.api.tasks.SourceSet
import org.gradle.api.tasks.TaskAction


/**
 * @author Steve Ebersole
 */
@CacheableTask
class XjcTask extends DefaultTask {
    private final SchemaDescriptor schemaDescriptor
    private final DirectoryProperty outputDirectory

    @Inject
    XjcTask(SchemaDescriptor schemaDescriptor, XjcExtension xjcExtension, Project project) {
        this.schemaDescriptor = schemaDescriptor

        outputDirectory = project.getObjects().directoryProperty()
        outputDirectory.convention( xjcExtension.getOutputDirectory().dir( schemaDescriptor.getName() ) )

        final SourceSet mainSourceSet = project.getConvention()
                .getPlugin( JavaPluginConvention.class )
                .getSourceSets()
                .findByName( SourceSet.MAIN_SOURCE_SET_NAME )

        project.tasks.xjc.dependsOn( this )
        mainSourceSet.getJava().srcDir( outputDirectory )
    }

    @InputFile
    @PathSensitive( PathSensitivity.RELATIVE )
    RegularFileProperty getXsdFile() {
        return schemaDescriptor.getXsdFile()
    }

    @InputFile
    @PathSensitive( PathSensitivity.RELATIVE )
    RegularFileProperty getXjcBindingFile() {
        return schemaDescriptor.getXjcBindingFile()
    }

    @Input
    Set<String> getXjcExtensions() {
        return schemaDescriptor.getXjcExtensions()
    }

    @OutputDirectory
    DirectoryProperty getOutputDirectory() {
        return outputDirectory
    }

    @TaskAction
    void generateJaxbBindings() {
        project.ant.xjc(
                destdir: outputDirectory.get().asFile.absolutePath,
                binding: schemaDescriptor.xjcBindingFile.get().asFile.absolutePath,
                schema: schemaDescriptor.xsdFile.get().asFile.absolutePath,
                target: schemaDescriptor.jaxbVersion,
                extension: 'true') {
            arg line: '-no-header'
            arg line: '-npa'
            if ( !schemaDescriptor.xjcExtensions.empty ) {
                arg line: schemaDescriptor.xjcExtensions.collect { "-X${it}" }.join( " " )
            }
        }
    }
}
