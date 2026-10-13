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
                sh 'docker --version'
                sh 'docker compose version'
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

        stage('Build Backend Docker Image') {
            steps {
                sh 'docker build -t leave-request-backend:${BUILD_NUMBER} .'
            }
        }

        stage('Build Frontend Docker Image') {
            steps {
                sh 'docker build -t leave-request-frontend:${BUILD_NUMBER} ./frontend'
            }
        }

        stage('Validate Deployment Configuration') {
            steps {
                script {
                    withEnv(["IMAGE_TAG=${env.BUILD_NUMBER}"]) {
                        sh '''
                            docker compose \
                              -p leave-request-system \
                              -f compose.deploy.yaml \
                              config --quiet
                        '''
                    }
                }
            }
        }

        stage('Deploy to Local Docker') {
            steps {
                script {
                    withEnv(["IMAGE_TAG=${env.BUILD_NUMBER}"]) {
                        sh '''
                            set -eu

                            COMPOSE="docker compose -p leave-request-system -f compose.deploy.yaml"

                            echo "Preparing deployment of image tag: $IMAGE_TAG"

                            # Record current image IDs for possible rollback.
                            OLD_BACKEND_ID="$(docker inspect --format='{{.Image}}' leave-request-backend 2>/dev/null || true)"
                            OLD_FRONTEND_ID="$(docker inspect --format='{{.Image}}' leave-request-frontend 2>/dev/null || true)"

                            # Preserve references to the currently running images.
                            if [ -n "$OLD_BACKEND_ID" ]; then
                                docker image tag "$OLD_BACKEND_ID" leave-request-backend:rollback
                            fi

                            if [ -n "$OLD_FRONTEND_ID" ]; then
                                docker image tag "$OLD_FRONTEND_ID" leave-request-frontend:rollback
                            fi

                            check_health() {
                                curl -fsS http://leave-request-backend:8080/api/leave-requests >/dev/null &&
                                curl -fsS http://leave-request-frontend/ >/dev/null &&
                                curl -fsS http://leave-request-frontend/api/leave-requests >/dev/null
                            }

                            wait_for_health() {
                                limit="$1"
                                attempt=1

                                while [ "$attempt" -le "$limit" ]; do
                                    if check_health; then
                                        return 0
                                    fi

                                    sleep 2
                                    attempt=$((attempt + 1))
                                done

                                return 1
                            }

                            rollback() {
                                if [ -z "$OLD_BACKEND_ID" ] || [ -z "$OLD_FRONTEND_ID" ]; then
                                    echo "Rollback unavailable: previous image IDs were not captured."
                                    return 1
                                fi

                                echo "Attempting rollback to the previous application images..."

                                if ! IMAGE_TAG=rollback docker compose \
                                    -p leave-request-system \
                                    -f compose.deploy.yaml \
                                    up -d --no-build --force-recreate backend frontend; then
                                    echo "Rollback deployment command failed."
                                    return 1
                                fi

                                if wait_for_health 30; then
                                    echo "Rollback completed; previous application images are responding."
                                    return 0
                                fi

                                echo "Rollback health checks failed. Manual investigation is required."
                                return 1
                            }

                            # Deploy the newly built application images.
                            if ! docker compose \
                                -p leave-request-system \
                                -f compose.deploy.yaml \
                                up -d --no-build; then

                                echo "Deployment command failed."
                                rollback || true
                                exit 1
                            fi

                            echo "Waiting for backend and frontend health checks..."

                            # Allow up to two minutes for application startup.
                            if wait_for_health 60; then
                                echo "SUCCESS: Backend API and frontend responded successfully."
                                exit 0
                            fi

                            echo "Deployment health checks failed."
                            rollback || true

                            # Keep the Jenkins build failed even if rollback succeeds.
                            exit 1
                        '''
                    }
                }
            }
        }
    }

    post {
        success {
            echo 'SUCCESS: Tests, frontend build, Docker image builds, deployment, and health checks completed.'
        }

        failure {
            echo 'FAILURE: Inspect the failed stage and Console Output. Verify application health before retrying.'
        }
    }
}