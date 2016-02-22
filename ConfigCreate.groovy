import groovy.json.JsonBuilder
import groovy.json.JsonOutput

def startingRoot = ROOT_QA_PATH

def fullPath = new File(WORKSPACE + "/" + startingRoot, '.')
def jobs = []
def pathToJsonFile = new File(WORKSPACE + "/TestConfig.json")

PHPUNIT = "PHP"
JUNIT = "JAVA"
PHPUNIT_TYPE = "PHPUnit"
PHPUNIT_TEMPLATE = "ApiTestCookbook"
PHPUNIT_REMOTECOMMAND = "cd \${TEST_BASE_DIR}\nphpunit -c phpunit.xml \${TEST_DIR}/\${TEST_PHP_FILE}"

def traverseWorkspaceDir(File path, jobs) {
    path.traverse { file ->
        if (!file.isDirectory() && !(file.name.matches(/^\.[A-z]*/))) {
            addFileToJobsList(file, jobs)
        }
    }
}

def String getFileExtension(String name) {
    int i = name.lastIndexOf('.')
    return name.substring(i+1)
}

def addFileToJobsList(File file, jobs) {
    curJob = new Job()
    curJob.name = file.name
    switch (getFileExtension(file.name).toUpperCase()) {
        case PHPUNIT:
            curJob.testType = "PHPUnit"
            curJob.template = "ApiTestCookbook"
            curJob.remoteCommand = PHPUNIT_REMOTECOMMAND
            break;
        case JUNIT:
            break;
    }

    jobs << curJob
}

class Config {
    def jobs
    def globals
}

class Job {
    def name
    def testType
    def remoteCommand
    def template

    def addParam(Param param) {
        this.params << param
    }
}

class Globals {
    def serverName
    def folderName
}

class Param {
    def name
    def type
    def value
}

def globals = new Globals()
globals.folderName = FOLDER_NAME
globals.serverName = SERVER_NAME

traverseWorkspaceDir(fullPath, jobs)
def testConfig = new Config()
testConfig.globals = globals
testConfig.jobs = jobs
println(JsonOutput.toJson(testConfig))