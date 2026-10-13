pipeline {
    agent any

    options {
        timestamps()
        disableConcurrentBuilds()
        skipDefaultCheckout(true)
    }

    stages {

        stage('Checkout') {
            steps {
                checkout scm
            }
        }

        stage('Check Tool Versions') {
            steps {
                sh 'java -version'
                sh 'mvn -version'
                sh 'node --version'
                sh 'npm --version'
            }
        }

        stage('Backend Tests') {
            steps {
                sh 'mvn -B test'
            }

            post {
                always {
                    junit(
                        testResults: 'target/surefire-reports/*.xml',
                        allowEmptyResults: true
                    )
                }
            }
        }

        stage('Frontend Dependencies') {
            steps {
                dir('frontend') {
                    sh 'npm ci'
                }
            }
        }

        stage('Frontend Production Build') {
            steps {
                dir('frontend') {
                    sh 'npm run build'
                }
            }
        }
    }

    post {
        success {
            echo 'SUCCESS: Backend tests and Angular build completed.'
        }

        failure {
            echo 'FAILURE: Check the failed stage and console output.'
        }
    }
}