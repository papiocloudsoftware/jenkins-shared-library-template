# jenkins-shared-library-template

Template repository for how to create a [Jenkins Shared Library].  

Jenkins shared libraries allow for the creation of custom DSL to be used within a
Jenkinsfile.  This can include simple things like calling an executable with predefined
arguments, to the full blown creation of a custom DSL.  The most common use cases are the 
abstraction/encapsulation of build/deployment logic.  By encapsultating the steps
required to build or deploy in a shared library, the implementation can change with
no updates required to the client repositories!

## Getting Started

 * Create your own (empty) public/private repository
 * Clone the template repo: `git clone https://github.com/papiocloudsoftware/jenkins-shared-library-template.git <name-of-your-repo>`
 * Delete the origin and add your own
   * `cd <name-of-your-repo>`
   * `git remote remove origin`
   * `git remote add origin <url to your repo>`
 * Update the `settings.gradle` `rootProject.name` property to be the name of your repository
 * Commit changes and push: `git add -A && git commit -m "Updating settings.gradle" && git push --set-upstream origin main`
 * Optional: Install [Papio Pipelines] to setup builds for shared library and allow easy access to shared library.

You can now customize the shared library by adding your own functionality!
 
## Structure

This repository structure follows the Jenkins documented structure with some additions
for testing and type safety.

```
(root)
+- src                     # Groovy source files
|   +- org
|       +- foo
|           +- Bar.groovy  # for org.foo.Bar class
+- test
|   +- org
|       +- foo
|           +- BarSpec.groovy # Spock specification for org.foo.Bar class
|   +- vars
|       +- FooSpec.groovy # Spock specification for foo.groovy global var
+- vars
|   +- foo.groovy          # for global 'foo' variable
|   +- foo.txt             # help for 'foo' variable
+- resources               # resource files (external libraries only)
|   +- org
|       +- foo
|           +- bar.json    # static helper data for org.foo.Bar
```

## Releasing

The repository uses [Gradle] for building/testing and Jenkins ([Papio Pipelines]) for Continuous
Integration/Releasing.  Each commit builds and tests the repo (all branches). After
successful build/test on the `main` branch, the repository tag `release` is updated
to the most recent commit on `main`.  This allows for stable references of the shared
library in client builds.

## Usage

When using Pipelines, importing the library for use in a client Jenkinsfile is
trivial.  Use the [gitHubLibrary] dsl provided by Pipelines. The DSL supports public
or private shared library repositories.

### Example

```
gitHubLibrary("jenkins-shared-library-template") // The name of your shared library repo

pipeline {
  agent any
  stages {
    ...
    stage("Build") {
      steps {
        foo() // Runs vars/foo.groovy from the shared library
      }
    }
  }
}
```

## Extending

### Encapsulation

Imagine you have a Jenkinsfile with the following stage:

```
...
    stage("Build & Release") {
      sh "docker build -t my-docker-image:latest ."
      withCredentials([string(credentialsId: "dockerhub-token", variable: "DOCKERHUB_TOKEN")]) {
        sh "echo ${DOCKERHUB_TOKEN} | docker login -u my-docker-user --password-stdin"
      }
      sh "docker push my-docker-image:latest"
    }
...
```

This could be encapsulated into the following reusable var: `vars/dockerHubPublish.groovy`

```
def call(Map options) {
  String repository = options.repository
  if (!repository) {
    error "Missing required option: 'repository'"
  }
  String tag = options.tag ?: "latest"
  String imageId = "${repository}:${tag}"
  String credentialsId = options.credentialsId ?: "dockerhub-token" // allows for overriding but defaults to org token

  // Call steps as if in Jenkinsfile
   
  sh "docker build -t ${imageId} ." // Use String interpolation to generate shell command
  withCredentials([string(credentialsId: credentialsId, variable: "DOCKERHUB_TOKEN")]) {
    sh "echo ${DOCKERHUB_TOKEN} | docker login -u my-docker-user --password-stdin"
  }
  sh "docker push ${imageId}"
}
```

This turns any Jenkinsfile stage in any repository that needs to publish to DockerHub into

```
...
    stage("Build & Release") {
      dockerHubPublish(repository: "my-docker-image")
    }
...
```

Now if anything needs to change with how the organization publishes to DockerHub, it has been nicely encapsulated in one location!

#### Method Overloading

Shared library vars also support [Method Overloading].  For example, in the same `vars/dockerHubPublish.groovy`, we could
provide a second `call` method that takes a single String for the repository (the only required argument).

```
def call(String repository) {
  // Call the call(Map) method passing the argument through
  call(repository: repository)
}

def call(Map options) {
  ...
}
```

Now the Jenkinsfile call can be simplified to: `dockerHubPublish("my-docker-image")`

### Writing Tests

Test can be written using the [Spock Framework].  Classes defined in the [src](./src) directory can
be written for testability and unit tested no different from other classes in other projects.  When testing
vars, extends the base class [AbstractVarSpecification].  This class has a `loadVar` method
that will load a var from the [vars](./vars) directory into an interface that is callable. Any cross
var referencing will call the corresponding var but references to Jenkins provided or Jenkins Plugin
provided DSL must be mocked in the [MockJenkinsGlobals] class.

[Jenkins Shared Library]: https://www.jenkins.io/doc/book/pipeline/shared-libraries/
[Gradle]: https://docs.gradle.org/current/userguide/userguide.html
[Papio Pipelines]: https://github.com/marketplace/papio-pipelines
[gitHubLibrary]: https://github.com/papiocloudsoftware/papio-pipelines/blob/master/docs/steps/gitHubLibrary.md
[Method Overloading]: https://www.w3schools.com/java/java_methods_overloading.asp
[Spock Framework]: https://spockframework.org/spock/docs/2.0/index.html
[AbstractVarSpecification]: ./test/vars/util/AbstractVarSpecification.groovy
[MockJenkinsGlobals]: ./test/vars/util/MockJenkinsGlobals.groovy
