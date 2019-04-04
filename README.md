# About Pipeline Multibranch Defaults Plugin

[![Build Status][build-image]][build-link]

**Table of Contents**

- [Warnings](#warnings)
- [Security implications](#security-implications)
- [How it works](#how-it-works)
- [How to configure](#how-to-configure)
  - [Create a default Jenkinsfile](#create-a-default-jenkinsfile)
  - [Create a Multibranch Pipeline job](#create-a-multibranch-pipeline-job)
  - [Update the build configuration mode](#update-the-build-configuration-mode)
- [Example default Jenkinsfile](#example-default-jenkinsfile)
  - [Default scripted pipeline](#default-scripted-pipeline)
  - [Default declarative pipeline](#default-declarative-pipeline)
- [Example Job DSL configuration](#example-job-dsl-configuration)
- [Additional recommended plugins](#additional-recommended-plugins)
- [Release Notes](#release-notes)
- [Authors](#authors)
- [License](#license)

Normally, [Jenkins pipelines][pipeline] and [Multibranch pipelines][multibranch]
requires a developer to save a `Jenkinsfile` in their repository root on one or
more branches.  The Pipeline Multibranch Defaults Plugin allows a Jenkins
administrator to specify a default `Jenkinsfile` so that developers do not need
to have a `Jenkinsfile` in their repository.

This is useful because:

- Jenkins Administrators can define a default Jenkinsfile in which all
  repositories can use.  This makes it easy to centralize build configuration
  for all projects.
- Jenkins Administrators can define a set of required steps before and after a
  developer `Jenkinsfile` is run, but still run the normal repository
  `Jenkinsfile` by calling it with the [`load` step][step-load].  For example,
  - Security analysis can be run as a required step before running a user
    Jenkinsfile.
  - An Audit step can be run as a required step after running a user Jenkinsfile
    which might do things like archive the build logs and update ticketing
    systems with a download link to the archived logs.  This is great for
    organizations which have compliance and regulations to meet by governing
    bodies.

# Warnings

Pipeline multibranch defaults plugin v1.1 and older created a new Job type named
**Multibranch Pipeline with defaults**.  This is considered a design flaw.

Pipeline multibranch defaults plugin v2.0 and later allow specifying a default
`Jenkinsfile` directly with a normal **Multibranch Pipeline**.  If you're using
v2.0 or later of this plugin do not use the "Multibranch Pipeline with defaults"
project type.  It will be removed from later releases of this plugin.

# Security implications

This plugin supports running a default Jenkinsfile with or without a [pipeline
sandbox][sandbox].  If the [pipeline `load` step][step-load] is used, then the
default Jenkinsfile must be run with sandbox enabled.  Otherwise, unauthorized
developer code will be able to run within and modify the Jenkins server runtime.
This basically means all developers with commit access can affect any part of
the Jenkins infrastructure (malicious or not).  This is essentially the same
security implications as granting developers access to the [script
console][script-console].

# How it works

Configuration files will be referenced from the global Jenkins script store
provided by the [config-file provider plugin][config-file-provider].  If a
multibranch pipeline is configured to use a default `Jenkinsfile`, then the
following happens:

- Every branch discovered is assumed to have a `Jenkinsfile` because of the
  default `Jenkinsfile` setting.
- Every pull request discovered is assumed to have a `Jenkinsfile` because of
  the default `Jenkinsfile` setting.
- When discover tags is enabled, every tag is assumed to have a `Jenkinsfile`
  because of the default `Jenkinsfile` setting.

# How to configure

This section explains how to configure a [multibranch pipeline][multibranch] to
use the default `Jenkinsfile` feature provided by this plugin.

### Create a default Jenkinsfile

Before pipelines can be configured to use a default Jenkinsfile, one must
configure a groovy script in the global Config File Management provided by the
[Configfile-Provider Plugin][config-file-provider].

1. Visit _Manage Jenkins_ menu.
2. Visit _Manage files_ menu.

   ![screenshot of manage files][screenshot-manage-files]

3. Add a new config file of type **Groovy file**.  During this step you can give
   your config file the Script ID `Jenkinsfile` or whatever ID you would like.
   Leaving it to be a UUID is fine as well.
4. Fill in the settings for the config file.

   ![screenshot of adding a config file][screenshot-configure-file]

> Note: versions prior to the pipeline multibranch defaults plugin v2.0 required
> the Script ID to be "Jenkinsfile".  This is no longer required.

### Create a Multibranch Pipeline job

Create a new multibranch pipeline job as one normally would (or update an
existing multibranch pipeline job).  In the menu on the left, choose `New Item`
and select multibranch pipeline.

![multibranch pipeline screenshot][screenshot-multibranch]

### Update the build configuration mode

Under the _Build Configuration_ section there will be a _Mode_ setting.  Change
the _Mode_ to "by default Jenkinsfile".

![screenshot of defaults setting][screenshot-setting]

Configurable options include:

- **Script ID** - The ID of the default `Jenkinsfile` to use from the global
  Config File Management.
- **Run default Jenkinsfile within Groovy sandbox** - If enabled, the configured
  default `Jenkinsfile` will be run within a [Groovy sandbox][sandbox].  This
  option is **strongly recommended** if the `Jenkinsfile` is using the [`load`
  pipeline step to evaluate a groovy source file][step-load] from an SCM
  repository.

> Note: if disabling the groovy sandbox, then admin script approval may be
> required the first time the default `Jenkinsfile` is used.

# Example default Jenkinsfile

### Default scripted pipeline

A default scripted pipeline `Jenkinsfile` which does not allow user a
repository `Jenkinsfile` to run.

```groovy
node {
    stage("Hello world") {
        echo "Hello world"
    }
}
```

A default scripted pipeline `Jenkinsfile` which loads a repository
`Jenkinsfile`.  Use case is adding required security scans or additional
auditing steps.

```groovy
node('security-scanner') {
    stage('Pre-flight security scan') {
        checkout scm
        sh '/usr/local/bin/security-scan.sh'
    }
}
try {
    node {
        stage('Prepare Jenkinsfile environment') {
            checkout scm
        }
        // now that SCM is checked out we can load and execute the repository
        // Jenksinfile
        load 'Jenkinsfile'
    }
} catch(Exception e) {
    throw e
} finally {
    stage('Post-run auditing') {
        // do some audit stuff here
    }
}
```

### Default declarative pipeline

Using [declarative pipeline][pipeline] as your default Jenkinsfile is also
possible.  Here's an example:

```groovy
pipeline {
    agent none
    stages {
        stage('Pre-flight security scan') {
            agent 'security-scanner'
            steps {
                sh '/usr/local/bin/security-scan.sh'
            }
        }
        stage('Load user Jenkinsfile') {
            agent any
            steps {
                load 'Jenkinsfile'
            }
        }
    }
    post {
        always {
            stage('Post-run auditing') {
                // do some audit stuff here
            }
        }
    }
}
```

# Example Job DSL configuration

```groovy
multibranchPipelineJob('example') {
    // SCM source or additional configuration

    factory {
        pipelineBranchDefaultsProjectFactory {
            // The ID of the default Jenkinsfile to use from the global Config
            // File Management.
            scriptId 'Jenkinsfile'

            // If enabled, the configured default Jenkinsfile will be run within
            // a Groovy sandbox.
            useSandbox true
        }
    }
}

```

# Additional recommended plugins

The following plugins are recommended as great companion plugins to the pipeline
multibranch with defaults plugin.

- [Basic Branch Build Strategies Plugin][basic-branch-build-strategies] provides
  some basic branch build strategies for use with Branch API based projects.
  This plugin is especially good at limiting tags built.  This is good for
  projects adopting a tag-based workflow.
- [SCM Filter Branch PR Plugin][scm-filter-branch-pr] provides wildcard and
  regex filters for multibranch pipelines which include matching the destination
  branch of PRs with the filters.  This is necessary if one does not want all
  branches for a project to be matched by the default Jenkinsfile.  This also
  allows one to filter tags as well.
- [Job DSL plugin][job-dsl] allows Jobs and Views to be defined via DSLs.
  Multibranch pipelines can be automatically created with a default Jenkinsfile
  configured.  This plugin provides configuration as code for jobs.

# Release Notes

For release notes see [CHANGELOG](CHANGELOG.md).

# Authors

* **Saponenko Denis** - *Initial work* - [vaimr](https://github.com/vaimr)
* [Sam Gleske][samrocketman] - plugin maintainer since Sep 29, 2018

See also the list of [contributors](https://github.com/vaimr/workflow-multibranch-def-plugin/contributors) who participated in this project.

# License

[The MIT License](LICENSE)

[basic-branch-build-strategies]: http://wiki.jenkins.io/display/JENKINS/Basic+Branch+Build+Strategies+Plugin
[build-image]: https://ci.jenkins.io/buildStatus/icon?job=Plugins/pipeline-multibranch-defaults-plugin/master
[build-link]: https://ci.jenkins.io/job/Plugins/pipeline-multibranch-defaults-plugin/master
[config-file-provider]: https://github.com/jenkinsci/config-file-provider-plugin
[job-dsl]: https://wiki.jenkins-ci.org/display/JENKINS/Job+DSL+Plugin
[multibranch]: https://jenkins.io/doc/book/pipeline/multibranch/
[pipeline]: https://jenkins.io/doc/book/pipeline/
[samrocketman]: https://github.com/samrocketman
[sandbox]: https://jenkins.io/doc/book/managing/script-approval/
[scm-filter-branch-pr]: https://wiki.jenkins.io/display/JENKINS/SCM+Filter+Branch+PR+Plugin
[screenshot-configure-file]: https://user-images.githubusercontent.com/875669/46273282-b5c89880-c509-11e8-9425-dbeddf1266a8.png
[screenshot-manage-files]: https://user-images.githubusercontent.com/875669/46273112-fd9af000-c508-11e8-8464-ec5773121294.png
[screenshot-multibranch]: https://user-images.githubusercontent.com/875669/46272775-726d2a80-c507-11e8-859c-2836b579423e.png
[screenshot-setting]: https://user-images.githubusercontent.com/875669/46272894-fb846180-c507-11e8-86e3-50f8fa4df26f.png
[script-console]: https://wiki.jenkins.io/display/JENKINS/Jenkins+Script+Console
[step-load]: https://jenkins.io/doc/pipeline/steps/workflow-cps/#code-load-code-evaluate-a-groovy-source-file-into-the-pipeline-script
