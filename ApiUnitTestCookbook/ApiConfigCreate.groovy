import groovy.json.JsonOutput

// Fetch the environment variables in a safe manner
def configuration = new HashMap()
def binding = getBinding()
configuration.putAll(binding.getVariables())

if(!configuration["API_ROOT_QA_PATH"] || !configuration["ENVIRONMENT"]) {
    println("Job failed due to one or more of the following variables not being present: " +
            "ENVIRONMENT, API_ROOT_QA_PATH")
    System.exit(0)
}

BASE_TEST_NAME = configuration["ENVIRONMENT"] + "/" + configuration["ENVIRONMENT"] + "_"
ROOT_QA_PATH = WORKSPACE + "/" + configuration["API_ROOT_QA_PATH"]
CONFIG_FOLDER_PATH = WORKSPACE + configuration["CONFIG_FILE_PATH"]



def jobs = []
def qaDir = new File(ROOT_QA_PATH)
def folderName = configuration["ENVIRONMENT"]
def shortEnvName = folderName.replaceAll("/[a-z]+/", "")
APITESTTEMPLATE = "${folderName}/ApiTestTemplate"

class List {
    def displayName
    def regex
}

class Config {
    def jobs
    def lists = []
    def globals
}

class Job {
    def name
    def remoteCommand
    def template
    def testName
    def testPath
    def type = "job"
}

class Globals {
    def serverName
    def folderName
}

class MultiJob {
    def name
    def listView
    def jobs = []
    def type = "master"
}

def nestedLists = [
        [name: "${folderName}/${shortEnvName}_API", regex: "${BASE_TEST_NAME}Api.*"]
]

def traverseWorkspaceDir(File path, jobs) {
    path.traverse { file ->
        if (!file.isDirectory() && !(file.name.matches(/^\.[A-z]*/))) {
            addFileToJobsList(file, jobs)
            phpUnitMaster.jobs << BASE_TEST_NAME + "Api_" + file.name.replaceAll(/\.[a-z]*$/, "")
        }
    }
}

def addFileToJobsList(File file, jobs) {
    curJob = new Job()

    curJob.name = BASE_TEST_NAME + "Api_" + file.name.replaceAll(/\.[a-z]*$/, "")
    curJob.template = APITESTTEMPLATE
    curJob.testPath = file.canonicalPath.replaceAll(/[\/A-z]*\/unittest/, ".")
    curJob.testName = file.name
    curJob.remoteCommand = "cd /www/netsite-docs/\nphpunit -c phpunit.xml ${curJob.testPath}/${curJob.testName}"

    jobs << curJob
}

def writeJsonToFile(String json) {
    new File(WORKSPACE + "/nprDSL/configs").mkdir()
    new File(CONFIG_FOLDER_PATH).withWriter('utf-8') { writer ->
        writer.write(json)
    }
}

def globals = new Globals()
globals.folderName = folderName
globals.serverName = ENVIRONMENT_SSH_HOST

phpUnitMaster = new MultiJob()
phpUnitMaster.name = "${BASE_TEST_NAME}Api_Master"
phpUnitMaster.listView = "${BASE_TEST_NAME}Master"
// Add in multijob as regular job - JobCreate will figure out what to do with it
jobs << phpUnitMaster

traverseWorkspaceDir(qaDir, jobs)

def testConfig = new Config()
testConfig.globals = globals
testConfig.jobs = jobs
nestedLists.each { list ->
    curList = new List()
    curList.displayName = list.name
    curList.regex = list.regex
    testConfig.lists << curList
}
writeJsonToFile(JsonOutput.toJson(testConfig))