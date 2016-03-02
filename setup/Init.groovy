import groovy.json.JsonSlurper

// Fetch the environment variables in a safe manner
def configuration = new HashMap()
def binding = getBinding()
configuration.putAll(binding.getVariables())
if (!configuration["GITHUB_CI_ID"]) {
    println("Need to provide UUID that maps to a valid keypair in Jenkins - " +
            "add a String parameter named GITHUB_CI_ID and put in " +
            "the UUID of the Git credentials used for NPRDM.")
    System.exit(0)
}

// Create dir for Jobs DSL code to be checked out into
new File('nprDSL').mkdir()

def testFolderName = configuration["TEST_FOLDER_NAME"] ?: "TestGeneration"
// Setting up a global-level logrotator for these seed jobs. Not sure why we would need more
// fine-tuning on this front but if we need to have separate values per job in this config then
// this code has to be changed.
def seedJobDaysToKeepBuilds = configuration["DAYS_TO_KEEP_BUILDS"] ?: 1
def seedJobBuildsToKeep = configuration["NUM_BUILDS_TO_KEEP"] ?: 10

def apiTestJobDaysToKeepBuild = configuration["API_TEMPLATE_DAYS_TO_KEEP_BUILD"] ?: 4
def apiTestJobNumBuildsToKeep = configuration["API_TEMPLATE_NUM_BUILDS_TO_KEEP"] ?: 10

// API BUILD NAMES
def ApiCreateConfig = "${testFolderName}/ApiCreateConfig"
def ApiJobCreate = "${testFolderName}/ApiJobCreate"
def ApiMaster = "${testFolderName}/ApiMaster"
def ApiTestTemplate = "${testFolderName}/ApiTestTemplate"
def FetchWWW = "FetchWWW"

folder(testFolderName) {
    description('Creates the test tools for automatically creating Api, Seamus, and Carbon tests for a given environment.')
    primaryView('Master')
}

freeStyleJob(ApiMaster) {
    logRotator(seedJobDaysToKeepBuilds, seedJobBuildsToKeep)
    parameters {
        stringParam('ENVIRONMENT', 'StageX', 'Name given to the folder that houses all tests for this environment. Should reflect the' +
                'server environment name.')
        stringParam('ENVIRONMENT_SSH_HOST', 'cms@stagex.npr.org:22', 'Server to use for remote SSH-ing ' +
                'commands. Needs to fit the format specified' +
                'in the Jenkins configuration under SSH remote hosts.')
        stringParam('API_ROOT_QA_PATH', null, 'Relative path pointing to where in the WWW codebase the PHPUnit tests are located.' +
                'This script will search recursively through the directory to find all *.php files and generate one test job per ' +
                'php file.'
        )
    }
    scm {
        git {
            remote {
                name("Jobs DSL Scripts")
                url('git@github.com:jdickie/JobDslScripts.git')
                credentials(configuration["GITHUB_CI_ID"])
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
        downstreamParameterized {
            trigger(ApiCreateConfig) {
                condition('SUCCESS')
                parameters {
                    currentBuild()
                }
            }
        }
    }
}

// WWW Api tests
freeStyleJob(FetchWWW) {
    logRotator(seedJobDaysToKeepBuilds, seedJobBuildsToKeep)
    parameters {
        stringParam('GIT_BRANCH', 'dev', 'Git branch to pull code from')
    }
    scm {
        git {
            remote {
                name("WWW")
                url('git@github.com:nprdm/www.git')
                credentials(configuration["GITHUB_CI_ID"])
            }
            clean(true)
            branch('${GIT_BRANCH}')
        }
    }
    publishers {
        archiveArtifacts('**/*')
    }
}


freeStyleJob(ApiCreateConfig) {
    logRotator(seedJobDaysToKeepBuilds, seedJobBuildsToKeep)
    parameters {
        stringParam('ENVIRONMENT', 'StageX', 'Name given to the folder that houses all tests for this environment. Should reflect the' +
                'server environment name.')
        stringParam('ENVIRONMENT_SSH_HOST', 'cms@stagex.npr.org:22', 'Server to use for remote SSH-ing commands. Needs to fit the format specified' +
                'in the Jenkins configuration under SSH remote hosts.')
        stringParam('CONFIG_FILE_PATH', 'configs/ApiTestConfig.json')
        stringParam('API_ROOT_QA_PATH', null, 'Relative path pointing to where in the WWW codebase the PHPUnit tests are located.' +
                'This script will search recursively through the directory to find all *.php files and generate one test job per ' +
                'php file.'
        )
    }
    steps {
        copyArtifacts(FetchWWW) {
            buildSelector {
                latestSuccessful(true)
            }
        }
        copyArtifacts(ApiMaster) {
            buildSelector {
                latestSuccessful(true)
            }
        }
        dsl {
            external('**/nprDSL/ApiUnitTestCookbook/ApiConfigCreate.groovy')
            removeAction('DELETE')
        }
    }
    publishers {
        archiveArtifacts('**/*')
        downstreamParameterized {
            trigger(ApiJobCreate) {
                condition('SUCCESS')
                parameters {
                    currentBuild()
                }
            }
        }
    }
}

freeStyleJob(ApiJobCreate) {
    logRotator(seedJobDaysToKeepBuilds, seedJobBuildsToKeep)
    parameters {
        stringParam('ENVIRONMENT', 'StageX', 'Name given to the folder that houses all tests for this environment. Should reflect the' +
                'server environment name.')
        stringParam('ENVIRONMENT_SSH_HOST', 'cms@stagex.npr.org:22', 'Server to use for remote SSH-ing commands. Needs to fit the format specified' +
                'in the Jenkins configuration under SSH remote hosts.')
        stringParam('CONFIG_FILE_PATH', 'configs/ApiTestConfig.json')
    }
    steps {
        copyArtifacts(ApiCreateConfig) {
            buildSelector {
                latestSuccessful(true)
            }
        }
        dsl {
            external('**/nprDSL/ApiUnitTestCookbook/ApiJobCreate.groovy')
        }
    }
}

freeStyleJob(ApiTestTemplate) {
    logRotator(apiTestJobDaysToKeepBuild, apiTestJobNumBuildsToKeep)
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
    columns {
        status()
        weather()
        name()
        lastSuccess()
        lastFailure()
        lastDuration()
        buildButton()
        lastBuildConsole()
    }
}

listView("${testFolderName}/Templates") {
    jobs {
        name("ApiTestTemplate")
    }
    columns {
        status()
        weather()
        name()
        lastSuccess()
        lastFailure()
        lastDuration()
        buildButton()
        lastBuildConsole()
    }
}

listView("${testFolderName}/Master") {
    jobs {
        name("ApiMaster")
    }
    columns {
        status()
        weather()
        name()
        lastSuccess()
        lastFailure()
        lastDuration()
        buildButton()
        lastBuildConsole()
    }
}