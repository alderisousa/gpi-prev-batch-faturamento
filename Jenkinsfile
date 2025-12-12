pipeline {
    agent any
    tools {
        jdk 'jdk21'
    }

    environment {
        PROJECT_NAME = 'gpi-prev-batch-faturamento'
        GIT_REPOSITORY = "git@github.com:og-gpi-prev/gpi-prev-batch-faturamento.git"
        AWS_REGION = 'us-east-1'
        REGISTRY = "117890058519.dkr.ecr.us-east-1.amazonaws.com"
        DOCKER_BUILDKIT = '1'
    }

    parameters {
        gitParameter(
            name: 'BRANCH',
            type: 'PT_BRANCH',
            defaultValue: 'main',
            description: 'Selecione a branch para checkout',
            branch: '',
            branchFilter: 'origin/(.*)',
            tagFilter: '*',
            sortMode: 'DESCENDING_SMART',
            selectedValue: 'DEFAULT',
            quickFilterEnabled: true,
            useRepository: env.GIT_REPOSITORY,
            listSize: '10'
        )
        choice(name: 'ENVIRONMENT', choices: ['QA', 'PROD'], description: 'Selecione o ambiente para deploy.')
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
    def environment = params.ENVIRONMENT.toLowerCase()

    withVault(configuration: [
            timeout: 60,
            vaultCredentialId: 'vault-approle-jenkins',
            vaultUrl: 'http://192.168.91.160:8200'
        ], vaultSecrets: [
            [
                path: "secret/gprev-aon/${environment}",
                engineVersion: 2,
                secretValues: [
                    [envVar: 'DB_HOST', vaultKey: 'db_host'],
                    [envVar: 'DB_NAME', vaultKey: 'db_name'],
                    [envVar: 'DB_USER', vaultKey: 'db_user'],
                    [envVar: 'DB_PASSWORD', vaultKey: 'db_password']
                ]
            ]
        ]) {
            if(params.ENVIRONMENT == 'QA') {
                sh '''
                docker stop ${PROJECT_NAME} || true
                docker rm ${PROJECT_NAME} || true
                docker run -d --name ${PROJECT_NAME} \
                ${PROJECT_NAME}
                docker system prune -af
                '''
                sh """
                docker stop ${PROJECT_NAME} || true
                docker rm ${PROJECT_NAME} || true
                docker run -d --name ${PROJECT_NAME} --restart unless-stopped \
                -e DATABASE_HOST=\${DB_HOST} \
                -e DATABASE_NAME=\${DB_NAME} \
                -e DATABASE_USER=\${DB_USER} \
                -e DATABASE_PASSWORD=\${DB_PASSWORD} \
                ${PROJECT_NAME}
                docker system prune -af
                """
            } else {
                sshagent(['aws-key-aon']) {
                    sh """
                    ssh -o StrictHostKeyChecking=no ubuntu@15.229.180.137 '
                    aws ecr get-login-password --region ${env.AWS_REGION} | docker login --username AWS --password-stdin ${env.REGISTRY} &&
                    docker stop ${PROJECT_NAME} || true &&
                    docker rm ${PROJECT_NAME} || true &&
                    docker pull ${env.IMAGE} &&
                    docker run -d --name ${PROJECT_NAME} --restart unless-stopped \
                    -e API_VERSION=${env.BUILD_DATE} \
                    -e DATABASE_HOST=\${DB_HOST} \
                    -e DATABASE_NAME=\${DB_NAME} \
                    -e DATABASE_USER=\${DB_USER} \
                    -e DATABASE_PASSWORD=\${DB_PASSWORD} \
                    ${env.IMAGE} &&
                    docker system prune -af
                    '
                    """
                }
            }
        }
}
