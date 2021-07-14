package org.hibernate.build.gradle.xjc

import org.gradle.api.DefaultTask
import org.gradle.api.Project
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.plugins.JavaPluginConvention
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.InputFile
import org.gradle.api.tasks.OutputDirectory
import org.gradle.api.tasks.SourceSet
import org.gradle.api.tasks.TaskAction


/**
 * @author Steve Ebersole
 */
class XjcTask extends DefaultTask {
    private final SchemaDescriptor schemaDescriptor
    private final DirectoryProperty outputDirectory

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
    RegularFileProperty getXsdFile() {
        return schemaDescriptor.getXsdFile()
    }

    @InputFile
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
            arg line: "-npa "
            if ( !schemaDescriptor.xjcExtensions.empty ) {
                arg line: schemaDescriptor.xjcExtensions.collect { "-X${it}" }.join( " " )
            }
        }
    }
}
