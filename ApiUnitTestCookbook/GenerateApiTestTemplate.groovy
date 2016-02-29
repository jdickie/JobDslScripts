/**
 * Creates template for WWW Api tests. Expects the following environment
 * params:
 *
 * NUM_BUILDS_TO_KEEP - Number of builds to keep stored in Jenkins. Default is 10
 */

NUM_BUILDS_TO_KEEP = NUM_BUILDS_TO_KEEP ?:10

freeStyleJob('ApiTestTemplate') {
    displayName('WWW Api Test Template')
    description('Template for WWW Api functional tests. ' +
            'These tests are run on the environment they are ' +
            'deployed to through invoking PHPUnit via a remote SSH call.')
    logRotator {
        numToKeep(NUM_BUILDS_TO_KEEP)
    }
    steps {
        remoteShell('cms@stage4.npr.org:22') {
            command ('cd /www/netsite-docs/qa/unittest/api')
            command ('phpunit -c phpunit.xml --testsuite=all')
        }
    }
}