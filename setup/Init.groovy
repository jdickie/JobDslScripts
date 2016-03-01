// Fetch the environment variables in a safe manner
def configuration = new HashMap()
def binding = getBinding()
configuration.putAll(binding.getVariables())


// Create dir for Jobs DSL code to be checked out into
new File('nprDSL').mkdir()

def testFolderName = configuration["TEST_FOLDER_NAME"] ?: "TestGeneration"


folder(testFolderName) {
    description('Creates the test tools for automatically creating Api, Seamus, and Carbon tests for a given environment.')
    primaryView('Master')
}

freeStyleJob("${testFolderName}/Master") {
    logRotator(1, 5)
    parameters {
        stringParam('ENVIRONMENT', 'StageX', 'Name given to the folder that houses all tests for this environment. Should reflect the' +
                'server environment name.')
        stringParam('ENVIRONMENT_SSH_HOST', 'cms@stagex.npr.org:22', 'Server to use for remote SSH-ing commands. Needs to fit the format specified' +
                'in the Jenkins configuration under SSH remote hosts.')
    }
    scm {
        git {
            remote {
                name("Jobs DSL Scripts")
                url('git@github.com:jdickie/JobDslScripts.git')
                credentials('jdickie')
            }
            clean(true)
            branch('dev')
            relativeTargetDir('nprDSL')
        }
    }
    steps {
        dsl {
            external('**/nprDSL/setup/SetupEnvironment.groovy')
        }
    }
    publishers {
        archiveArtifacts('**/*')
    }
}

// WWW Api tests
freeStyleJob("${testFolderName}/FetchWWW") {
    logRotator(1, 5)
    parameters {
        stringParam('GIT_BRANCH', 'dev', 'Git branch to pull code from')
    }
    scm {
        git {
            remote {
                name("WWW")
                url('git@github.com:nprdm/www.git')
                credentials('jdickie')
            }
            clean(true)
            branch('${GIT_BRANCH}')
        }
    }
    publishers {
        archiveArtifacts('**/*')
    }
}


freeStyleJob("${testFolderName}/ApiCreateConfig") {
    logRotator(1, 5)
    parameters {
        stringParam('ENVIRONMENT', 'StageX', 'Name given to the folder that houses all tests for this environment. Should reflect the' +
                'server environment name.')
        stringParam('ENVIRONMENT_SSH_HOST', 'cms@stagex.npr.org:22', 'Server to use for remote SSH-ing commands. Needs to fit the format specified' +
                'in the Jenkins configuration under SSH remote hosts.')
        stringParam('CONFIG_FILE_PATH', 'configs/ApiTestConfig.json')
    }
    steps {
        copyArtifacts("${testFolderName}/SetupEnvironment") {
            buildSelector {
                latestSuccessful(true)
            }
        }
        dsl {
            external('**/nprDSL/ApiUnitTestCookbook/ApiConfigCreate.groovy')
        }
    }
    publishers {
        archiveArtifacts('**/*')
    }
}

freeStyleJob("${testFolderName}/ApiJobCreate") {
    logRotator(1, 5)
    parameters {
        stringParam('ENVIRONMENT', 'StageX', 'Name given to the folder that houses all tests for this environment. Should reflect the' +
                'server environment name.')
        stringParam('ENVIRONMENT_SSH_HOST', 'cms@stagex.npr.org:22', 'Server to use for remote SSH-ing commands. Needs to fit the format specified' +
                'in the Jenkins configuration under SSH remote hosts.')
        stringParam('CONFIG_FILE_PATH', 'configs/ApiTestConfig.json')
    }
    steps {
        copyArtifacts("${testFolderName}/ApiCreateConfig") {
            buildSelector {
                latestSuccessful(true)
            }
        }
        dsl {
            external('**/nprDSL/ApiUnitTestCookbook/ApiJobCreate.groovy')
        }
    }
}

freeStyleJob("${testFolderName}/ApiTestTemplate") {
    logRotator(4, 10)
    steps {
        remoteShell('cms@stagex.npr.org:22') {
            command('cd /www/netsite-docs/qa/unittest')
            command('phpunit -c phpunit.xml --testsuite=all')
        }
    }
}

// Setting up lists to go inside of Test Folder
listView("${testFolderName}/Api") {
    jobs {
        name("ApiCreateConfig")
        name("ApiJobCreate")
    }
}

listView("${testFolderName}/Templates") {
    jobs {
        name("ApiTestTemplate")
    }
}

listView("${testFolderName}/Master") {
    jobs {
        name("SetupEnvironment")
    }
}