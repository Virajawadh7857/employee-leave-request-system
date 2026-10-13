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

                            echo "Preparing deployment of image tag: $IMAGE_TAG"

                            # Capture the currently deployed image IDs.
                            OLD_BACKEND_ID="$(docker inspect \
                                --format='{{.Image}}' \
                                leave-request-backend 2>/dev/null || true)"

                            OLD_FRONTEND_ID="$(docker inspect \
                                --format='{{.Image}}' \
                                leave-request-frontend 2>/dev/null || true)"

                            # Preserve the existing images for rollback.
                            if [ -n "$OLD_BACKEND_ID" ]; then
                                docker image tag \
                                    "$OLD_BACKEND_ID" \
                                    leave-request-backend:rollback
                            fi

                            if [ -n "$OLD_FRONTEND_ID" ]; then
                                docker image tag \
                                    "$OLD_FRONTEND_ID" \
                                    leave-request-frontend:rollback
                            fi

                            # Use published ports instead of container hostnames.
                            # Jenkins has already verified these addresses work.
                            check_health() {
                                curl -fsS --connect-timeout 3 --max-time 5 \
                                    http://host.docker.internal:8080/api/leave-requests \
                                    >/dev/null &&
                                curl -fsS --connect-timeout 3 --max-time 5 \
                                    http://host.docker.internal:4200/ \
                                    >/dev/null &&
                                curl -fsS --connect-timeout 3 --max-time 5 \
                                    http://host.docker.internal:4200/api/leave-requests \
                                    >/dev/null
                            }

                            wait_for_health() {
                                limit="$1"
                                attempt=1

                                while [ "$attempt" -le "$limit" ]; do
                                    echo "Health check attempt $attempt/$limit"

                                    if check_health; then
                                        return 0
                                    fi

                                    sleep 2
                                    attempt=$((attempt + 1))
                                done

                                return 1
                            }

                            rollback() {
                                if [ -z "$OLD_BACKEND_ID" ] ||
                                   [ -z "$OLD_FRONTEND_ID" ]; then
                                    echo "Rollback unavailable: previous images were not captured."
                                    return 1
                                fi

                                echo "Attempting rollback to previous application images..."

                                if ! IMAGE_TAG=rollback docker compose \
                                    -p leave-request-system \
                                    -f compose.deploy.yaml \
                                    up -d --no-build --force-recreate backend frontend; then
                                    echo "Rollback deployment command failed."
                                    return 1
                                fi

                                if wait_for_health 30; then
                                    echo "Rollback completed successfully."
                                    return 0
                                fi

                                echo "Rollback health checks failed. Manual investigation is required."
                                return 1
                            }

                            # Deploy the new backend and frontend images.
                            if ! docker compose \
                                -p leave-request-system \
                                -f compose.deploy.yaml \
                                up -d --no-build; then

                                echo "Deployment command failed."
                                rollback || true
                                exit 1
                            fi

                            echo "Waiting for application health checks..."

                            # Allow up to two minutes for application startup.
                            if wait_for_health 60; then
                                echo "SUCCESS: Backend API, frontend, and frontend API proxy are responding."
                                exit 0
                            fi

                            echo "Deployment health checks failed. Collecting diagnostics..."

                            echo "=== Backend logs ==="
                            docker logs leave-request-backend --tail 80 || true

                            echo "=== Frontend logs ==="
                            docker logs leave-request-frontend --tail 80 || true

                            echo "=== Backend API diagnostic ==="
                            curl -v --max-time 5 \
                                http://host.docker.internal:8080/api/leave-requests || true

                            echo "=== Frontend diagnostic ==="
                            curl -v --max-time 5 \
                                http://host.docker.internal:4200/ || true

                            echo "=== Frontend API proxy diagnostic ==="
                            curl -v --max-time 5 \
                                http://host.docker.internal:4200/api/leave-requests || true

                            echo "Attempting rollback..."
                            rollback || true

                            # Keep the build failed, even when rollback succeeds.
                            exit 1
                        '''
                    }
                }
            }
        }
    }

    post {
        success {
            echo 'SUCCESS: Backend tests, Angular build, Docker image builds, deployment, and health checks completed.'
        }

        failure {
            echo 'FAILURE: Check the failed stage and diagnostic output. Verify application health before retrying.'
        }
    }
}