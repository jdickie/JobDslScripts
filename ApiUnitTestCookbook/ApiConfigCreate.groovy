import groovy.json.JsonOutput

BASE_TEST_NAME = ENVIRONMENT + "_"
ROOT_QA_PATH = API_ROOT_QA_PATH ?: WORKSPACE + "www/qa/unittest/api"

def jobs = []
def qaDir = new File(ROOT_QA_PATH)
def folderName = ENVIRONMENT ?: "StageX"
def shortEnvName = folderName.replaceAll("/[a-z]+/", "")

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
        [name: "${shortEnvName}_API", regex: "${BASE_TEST_NAME}Api.*"]
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
    curJob.template = "TestGeneration/ApiTestTemplate"
    curJob.testPath = file.canonicalPath.replaceAll(/[\/A-z]*\/unittest/, ".")
    curJob.testName = file.name
    curJob.remoteCommand = "cd /www/netsite-docs/\nphpunit -c phpunit.xml ${curJob.testPath}/${curJob.testName}"

    jobs << curJob
}

def writeJsonToFile(String json, String fileName) {
    new File(WORKSPACE + "/configs").mkdir()
    new File(WORKSPACE + "/configs/" + fileName).withWriter('utf-8') { writer ->
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
writeJsonToFile(JsonOutput.toJson(testConfig), "ApiTestConfig.json")