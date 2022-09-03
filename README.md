# Gradle XJC Plugin

This is a plugin for performing XJC generation (JAXB binding model generation) as part of a Gradle build.  This plugin
is different from others I looked into using in that it centers around defining XJC for specific schemas.  Other JAXB/XJC
plugins (https://github.com/jacobono/gradle-jaxb-plugin e.g.) centered around directories.  And maybe that is the
better approach overall and the approach here simply shows my lack of fu with JAXB and XJC in general.  But this is
what I need for the Hibernate build and so here is the plugin for that style of XJC processing.

### Usage

To use the plugin, you must first get it added to your build:

    buildscript {
        repositories {
            mavenCentral()
        }
        dependencies {
            classpath 'org.hibernate.build.gradle:gradle-xjc-plugin:<version>'
        }
    }

    apply plugin: 'org.hibernate.build.gradle.xjc'

where `<version>` is the version you want to use.

This plugin uses the XJC Ant task (ultimately I'd like to get away from the Ant usage).  You need to tell it the dependencies needed
for loading that Ant task.  The plugin adds a configuration named `xjc` where you can add the needed dependencies.
The plugin does automatically add `gradleApi()` as a xjc dependency because the newer jvnet tasks seem to rely on
slf4j, so we pass the version of slf4j used by the running Gradle.  However, all (other) dependencies needed for the
the Ant task need to be defined here.  E.g.:

    dependencies {
        xjc 'org.glassfish.jaxb:jaxb-xjc:2.2.11'
        xjc 'org.jvnet.jaxb2_commons:jaxb2-basics:0.9.3'
        xjc 'org.jvnet.jaxb2_commons:jaxb2-basics-ant:0.9.3'
    }

The plugin also adds an extension for configuring its processing.

By default, the plugin will generate all output to `${project.buildDir}/generated-src/xjc/main` (packages are created
under this directory).  You can instruct the plugin to use a different directory:

    xjc {
        outputDirectory = file('some/other/dir')
    }

The plugin makes use of a Gradle feature called a `NamedDomainObjectContainer`.  Basically, it allows dynamic
extension of the DSL for in an `a posteriori` manner.  For what its worth, this is how many things in Gradle itself
(like Configurations, SourceSets, etc) work.  The plugin defines a `schemas` NamedDomainObjectContainer under the xjc
extension.  The type of the domain objects in this container (of type `org.hibernate.build.gradle.xjc.SchemaDescriptor`)
describe the processing for a particular XSD.  Let see an example:

    xjc {
        ...
        // access the schemas NamedDomainObjectContainer
        schemas {
            // and add a new SchemaDescriptor to it under the name 'cfg'
            cfg {
                // and now, configure the SchemaDescriptor
                xsdFile = file('src/main/resources/org/hibernate/xsd/cfg/legacy-configuration-4.0.xsd')
                xjcBindingFile = file('src/main/xjc-bindings/hbm-configuration-bindings.xjb')
            }

            // and add a new SchemaDescriptor to it under the name 'hbm'
            hbm {
                xsdFile = file('src/main/resources/org/hibernate/xsd/mapping/legacy-mapping-4.0.xsd')
                xjcBindingFile = file('src/main/xjc-bindings/hbm-mapping-bindings.xjb')
                xjcExtensions = ['inheritance', 'simplify']
            }
        }
    }

`SchemaDescriptor` allows configuration of:

* xsdFile - The XSD schema file
* xjcBindingFile - The XJC binding file to apply
* xjcExtensions - Any XJC extensions to be enabled (appropriate dependencies should be defined using xjc configuration)
