pipeline {
  agent any

  stages {
    stage("Build") {
      steps {
        sh "./gradlew build"
      }
    }
    stage("Release") {
      when {
        branch "main"
        not { changeRequest() }
      }
      steps {
        sh "git tag -f release"
        gitPush(followTags: true, force: true)
      }
    }
  }

}
