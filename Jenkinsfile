pipeline {
    triggers {
        pollSCM('') // Enabling being build on Push
    }
    agent any
	options {
		buildDiscarder(logRotator(numToKeepStr:'50'))
		disableConcurrentBuilds()
	}

	stages {
        stage('Clone sources') {
            steps {
                git url: 'https://github.com/cysec-platform/cysec-translation-tool.git',credentialsId: 'cysec-jenkins-public', branch: env.BRANCH_NAME
            }
        }

        stage('build') {
            steps {
                sh 'mvn clean compile'
            }
        }
        stage('Main test') {
            steps {
				script{
					try {
						sh 'mvn test'
					} finally {
						junit '**/target/surefire-reports/TEST-*.xml'
					}
                }
            }
        }

        stage('package') {
            steps {
                sh 'mvn package'
            }
        }
        stage('publish') {
            steps {
                sh 'mvn deploy'
            }
        }

    }
    post {
        //always {
          //publishHTML([allowMissing: false, alwaysLinkToLastBuild: false, keepAll: false, reportDir: 'build/reports/tests/test/', reportFiles: 'index.html', reportName: 'GEIGER UI Report', reportTitles: 'GEIGER-UI'])
        //}
        success {
            archiveArtifacts artifacts: '**/target/*.jar', fingerprint: true
			// step([$class: 'JavadocArchiver', javadocDir: 'build/docs/javadoc', keepAll: false])
            updateGitlabCommitStatus(name: 'build', state: 'success')
        }
        failure {
          updateGitlabCommitStatus(name: 'build', state: 'failed')
        }
    }
}
