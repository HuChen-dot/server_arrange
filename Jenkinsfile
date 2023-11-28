import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

def TARGET_PATH = '/www/server/wison-punch'
def SOURCE_PATH = './punch-web/target'
def SHELL_NAME = 'wison-punch.sh'

def RELEASE_REMOTE_HOST = 'root@10.99.10.154'
def TEST_REMOTE_HOST = 'root@10.99.10.122'

def RELEASE_SKYWALKING_HOST = '10.99.10.155:11800'
def TEST_SKYWALKING_HOST = '10.99.10.123:11800'
def JVM = '-XX:+HeapDumpOnOutOfMemoryError -Xms1g -Xmx1g'
def APP_PORT = '8668'



pipeline {
    agent any

    stages {
        stage('getGitlabBranchName') {
            steps {
                echo "current branch is: ${env.gitlabBranch}"
            }
        }

        stage('test-build') {
            when {
                environment name: 'gitlabBranch', value: 'test'
            }

            steps {
                script {
                      def timestamp = LocalDateTime.now()
                      def formattedTime = timestamp.format(DateTimeFormatter.ofPattern("yyyy-MM-dd~HH.mm.ss"))

                      withEnv(['JENKINS_NODE_COOKIE=dontkillme']) {
                                        sh "mvnd clean package -U -Ptest -DskipTests"
                                        sh "ssh $TEST_REMOTE_HOST mkdir -p $TARGET_PATH/$formattedTime/logs"
                                        sh "scp $SOURCE_PATH/*.jar $TEST_REMOTE_HOST:$TARGET_PATH/$formattedTime"
                                        sh "scp ./$SHELL_NAME $TEST_REMOTE_HOST:$TARGET_PATH/$formattedTime"
                                        sh "ssh $TEST_REMOTE_HOST \"sh $TARGET_PATH/$formattedTime/$SHELL_NAME\" restart $TEST_SKYWALKING_HOST $formattedTime $APP_PORT $JVM"
                      }
                }
            }
        }

        stage('release-build') {
            when {
                environment name: 'gitlabBranch', value: 'release'
            }

            steps {
                      script {
                        def timestamp = LocalDateTime.now()
                        def formattedTime = timestamp.format(DateTimeFormatter.ofPattern("yyyy-MM-dd~HH.mm.ss"))

                        withEnv(['JENKINS_NODE_COOKIE=dontkillme']) {
                            sh "mvnd clean package -U -Pprod -DskipTests"
                            sh "ssh $RELEASE_REMOTE_HOST mkdir -p $TARGET_PATH/$formattedTime/logs"
                            sh "scp $SOURCE_PATH/*.jar $RELEASE_REMOTE_HOST:$TARGET_PATH/$formattedTime"
                            sh "scp ./$SHELL_NAME $RELEASE_REMOTE_HOST:$TARGET_PATH/$formattedTime"

                            }
                      }
            }
        }

    }
}
