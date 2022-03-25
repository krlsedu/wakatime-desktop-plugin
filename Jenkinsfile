#!groovy

pipeline {
  agent none
  stages {
    stage('Build') {

      agent any
        tools {
                maven 'M3'
            }
          steps {
            sh 'mvn clean install'
          }
        }
    stage('Tests') {
      agent any
        tools {
                maven 'M3'
            }
      steps {
        sh 'mvn test'
      }
    }
    stage('Gerar versão') {
    agent any
    tools {
            maven 'M3'
        }
      steps {
        script {
            if (env.BRANCH_NAME == 'master') {
                echo 'Master'
                PRE_RELEASE = ''
                TAG = VersionNumber(versionNumberString: '${BUILD_DATE_FORMATTED, "yyyyMMdd"}.${BUILDS_TODAY}.${BUILD_NUMBER}')
            } else {
                echo 'Dev'
                PRE_RELEASE = ' --pre-release'
                TAG = 'Alpha-'+VersionNumber(versionNumberString: '${BUILD_DATE_FORMATTED, "yyyyMMdd"}.${BUILDS_TODAY}.${BUILD_NUMBER}')
            }
        }
        sh 'git pull origin master'
        sh 'mvn versions:set versions:commit -DnewVersion='+TAG
        sh 'mvn clean install'

        echo "Compressing artifacts into one file"
        sh 'zip -r timetracker-desktop-plugin.zip target'

        withCredentials([usernamePassword(credentialsId: 'github_global', passwordVariable: 'password', usernameVariable: 'user')]) {

            echo "Creating a new release in github"
            sh 'github-release release --user krlsedu --security-token '+env.password+' --repo timetracker-desktop-plugin --tag '+TAG+' --name "'+TAG+'"'

            echo "Uploading the artifacts into github"
            sh 'ls -l'

            sleep(time:3,unit:"SECONDS")

            sh 'github-release upload --user krlsedu --security-token '+env.password+' --repo timetracker-desktop-plugin --tag '+TAG+' --name "'+TAG+'" --file timetracker-desktop-plugin.zip'

            sh "git add ."
            sh "git config --global user.email 'krlsedu@gmail.com'"
            sh "git config --global user.name 'Carlos Eduardo Duarte Schwalm'"
            sh "git commit -m 'Triggered Build: "+TAG+"'"
          sh 'git push https://krlsedu:${password}@github.com/krlsedu/timetracker-desktop-plugin.git HEAD:master'
        }
      }
    }
  }
}