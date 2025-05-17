pipeline {
    agent any

    parameters {
        string(name: 'BRANCH', defaultValue: 'main', description: 'Informe a branch para checkout.')
        choice(name: 'ENVIRONMENT', choices: ['QA', 'PROD'], description: 'Selecione o ambiente para deploy.')
        choice(name: 'JAVA_VERSION', choices: ['21'], description: 'Selecione a versão do Java para build e deploy.')
    }

    environment {
        PROJECT_NAME = 'gpi-prev-batch-faturamento'
        GIT_REPOSITORY = "git@github.com:og-gpi-prev/gpi-prev-batch-faturamento.git"
        AWS_REGION = 'us-east-1'
        REGISTRY = "117890058519.dkr.ecr.us-east-1.amazonaws.com"
    }

    stages {
        stage('Set Remote Variables') {
            steps {
                script {
                    if(params.ENVIRONMENT == 'QA') {
                        env.APP_NAME_WITH_VERSION = "${env.PROJECT_NAME}:qa-${env.BUILD_NUMBER}"
                        env.SPRING_PROFILES_ACTIVE = 'qa'
                        env.HOST_USER = 'gpi'
                        env.HOST_IP = '192.168.91.160'
                    } else {
                        APP_NAME_WITH_VERSION = "${env.PROJECT_NAME}:${env.BUILD_NUMBER}"
                        SPRING_PROFILES_ACTIVE = 'prod'
                    }
                }
            }
        }

        stage('Checkout') {
            steps {
                checkout scm: [
                    $class: 'GitSCM',
                    branches: [[name: "${params.BRANCH}"]],
                    userRemoteConfigs: [[url: "${env.GIT_REPOSITORY}", credentialsId: 'github']]
                ]
            }
        }

        stage('Build') {
            agent any
            environment {
                JAVA_21_HOME = "/usr/lib/jvm/java-21-openjdk-amd64"
            }
            steps {
                script {
                    def selectedJdk = env.JAVA_21_HOME
                    env.JAVA_HOME = selectedJdk
                    env.PATH = "${selectedJdk}/bin:${env.PATH}"
                }

                sh '''
                    ./mvnw clean install -DskipTests
                    ls -la target
                '''
            }
        }

        stage('Login to AWS ECR') {
            steps {
                withCredentials([aws(credentialsId: 'aws-credentials', region: "${env.AWS_REGION}")]) {
                    sh "aws ecr get-login-password --region ${env.AWS_REGION} | docker login --username AWS --password-stdin ${env.REGISTRY}"
                }
            }
        }

        stage('Docker Build') {
            steps {
                script {
                    dockerBuild()
                }
            }
        }

        stage('Deploy') {
            steps {
                script {
                    deployApplication()
                }
            }
        }
    }

    post {
        always {
            cleanWs()
        }
    }
}

def dockerBuild() {
    env.IMAGE = "${REGISTRY}/${env.APP_NAME_WITH_VERSION}"

    sh "docker build -t ${env.APP_NAME_WITH_VERSION} ."
    sh "docker tag ${env.APP_NAME_WITH_VERSION} ${env.IMAGE}"
    sh "docker push ${env.IMAGE}"
}

def deployApplication() {
    sh """
    docker stop ${env.PROJECT_NAME} || true
    docker rm ${env.PROJECT_NAME} || true
    docker run -d --name ${env.PROJECT_NAME} \\
       -p 8001:8080 \\
       -e SPRING_PROFILES_ACTIVE=${env.SPRING_PROFILES_ACTIVE} \\
       -e DB_URL=${env.DATABASE_HOST} \\
       -e DB_USERNAME=${env.DATABASE_NAME} \\
       -e DB_PASSWORD="${env.DATABASE_PASSWORD}" \\
       ${env.APP_NAME_WITH_VERSION}
    docker system prune -af
    '
    """
}
