pipeline {
    agent any
    tools {
        jdk 'jdk21'
    }

    parameters {
        string(name: 'BRANCH', defaultValue: 'main', description: 'Informe a branch para checkout.')
        choice(name: 'ENVIRONMENT', choices: ['QA', 'PROD'], description: 'Selecione o ambiente para deploy.')
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
                    env.BUILD_DATE = new Date().format('yyyyMMdd-HHmmss', TimeZone.getTimeZone('America/Sao_Paulo'))
                    def tagSuffix = params.ENVIRONMENT == 'QA' ? "qa-${env.BUILD_NUMBER}" : "${env.BUILD_DATE}"
                    env.APP_NAME_WITH_VERSION = "${env.PROJECT_NAME}:${tagSuffix}"
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
            steps {
                script {
                    sh './mvnw clean install -DskipTests'
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
    if(params.ENVIRONMENT == 'QA') {
        sh "docker build -t ${PROJECT_NAME} ."
    } else {
        withCredentials([aws(credentialsId: 'aws-credentials', region: "${env.AWS_REGION}")]) {
            sh "aws ecr get-login-password --region ${env.AWS_REGION} | docker login --username AWS --password-stdin ${env.REGISTRY}"
        }

        env.IMAGE = "${REGISTRY}/${env.APP_NAME_WITH_VERSION}"

        sh "docker build -t ${env.APP_NAME_WITH_VERSION} ."
        sh "docker build -t ${PROJECT_NAME}:latest ."

        sh "docker tag ${env.APP_NAME_WITH_VERSION} ${env.IMAGE}"
        sh "docker tag ${PROJECT_NAME}:latest ${REGISTRY}/${PROJECT_NAME}:latest"

        sh "docker push ${env.IMAGE}"
        sh "docker push ${REGISTRY}/${PROJECT_NAME}:latest"
    }
}


def deployApplication() {
    if(params.ENVIRONMENT == 'QA') {
        sh '''
        docker stop ${PROJECT_NAME} || true
        docker rm ${PROJECT_NAME} || true
        docker run -d --name ${PROJECT_NAME} \
        ${PROJECT_NAME}
        docker system prune -af
        '''
    } else {
        sshagent(['ec2-ssh-key']) {
            sh """
            ssh -o StrictHostKeyChecking=no ubuntu@54.227.253.137 '
            aws ecr get-login-password --region ${env.AWS_REGION} | docker login --username AWS --password-stdin ${env.REGISTRY} &&
            docker stop ${PROJECT_NAME} || true &&
            docker rm ${PROJECT_NAME} || true &&
            docker pull ${env.IMAGE} &&
            docker run -d --name ${PROJECT_NAME} \
            -e API_VERSION=${env.BUILD_DATE} \
            -e DATABASE_HOST=200.201.200.43 \
            -e DATABASE_PORT=5000 \
            ${env.IMAGE} &&
            docker system prune -af
            '
            """
        }
    }
}
