package org.hibernate.build.gradle.xjc


import org.gradle.api.DefaultTask
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.provider.Property
import org.gradle.api.provider.SetProperty
import org.gradle.api.tasks.CacheableTask
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.InputFile
import org.gradle.api.tasks.OutputDirectory
import org.gradle.api.tasks.PathSensitive
import org.gradle.api.tasks.PathSensitivity
import org.gradle.api.tasks.TaskAction

/**
 * @author Steve Ebersole
 */
@CacheableTask
abstract class XjcTask extends DefaultTask {
    private final DirectoryProperty outputDirectory

    private final RegularFileProperty xsdFile
    private final RegularFileProperty xjcBindingFile
    private final SetProperty<String> xjcExtensions

    private final Property<String> jaxbVersion

    XjcTask() {
        xsdFile = project.getObjects().fileProperty()
        xjcBindingFile = project.getObjects().fileProperty()
        xjcExtensions = project.objects.setProperty( String.class )

        outputDirectory = project.objects.directoryProperty()

        jaxbVersion = project.objects.property( String.class )
    }

    @InputFile
    @PathSensitive( PathSensitivity.RELATIVE )
    RegularFileProperty getXsdFile() {
        return xsdFile
    }

    @InputFile
    @PathSensitive( PathSensitivity.RELATIVE )
    RegularFileProperty getXjcBindingFile() {
        return xjcBindingFile
    }

    @Input
    SetProperty<String> getXjcExtensions() {
        return xjcExtensions
    }

    @Input
    Property<String> getJaxbVersion() {
        return jaxbVersion
    }

    @OutputDirectory
    DirectoryProperty getOutputDirectory() {
        return outputDirectory
    }

    @TaskAction
    void generateJaxbBindings() {
        project.ant.xjc(
                destdir: outputDirectory.get().asFile.absolutePath,
                binding: xjcBindingFile.get().asFile.absolutePath,
                schema: xsdFile.get().asFile.absolutePath,
                target: jaxbVersion,
                extension: 'true') {
            arg line: '-no-header'
            arg line: '-npa'
            if ( !xjcExtensions.empty ) {
                arg line: xjcExtensions.collect { "-X${it}" }.join( " " )
            }
        }
    }
}
