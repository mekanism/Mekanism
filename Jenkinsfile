pipeline {
  agent any

  /*tools {
    gradle "Gradle 4.4"
  }*/

  stages {
    stage('Build') {
      steps {
        sh "./gradlew"
      }
    }
    stage('Deploy') {
      when {
        branch 'master'
      }
      steps {
        echo "Current build result: ${currentBuild.result}"
        withAWS(region: 'us-east-1', credentials:'add82375-f3a1-4b2c-bf52-f557247e9c9e') {
          s3Upload(bucket:"aidancbrady", path:"mekanism/${env.BUILD_ID}", includePathPattern:'**/*', workingDir:'output') 
        }
      }
    }
    stage('Cleanup') {
      steps{
        dir('output') {
          deleteDir()
        }
      }
    }
  }
}