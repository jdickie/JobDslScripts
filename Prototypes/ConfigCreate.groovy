import groovy.json.JsonOutput
import javaposse.jobdsl.dsl.views.jobfilter.MatchType
import javaposse.jobdsl.dsl.views.jobfilter.Status

def startingRoot = ROOT_QA_PATH
def folderName = FOLDER_NAME


def fullPath = new File(WORKSPACE + "/" + startingRoot, '.')
def jobs = []
def pathToJsonFile = new File(WORKSPACE + "/TestConfig.json")
def shortEnvName = folderName.replaceAll("/[a-z]+/", "")

PHPUNIT = "PHP"
JUNIT = "JAVA"
PHPUNIT_TEMPLATE = "ApiTestCookbook"
PHPUNIT_REMOTECOMMAND = "cd _TEST_BASE_DIR_\nphpunit -c phpunit.xml _TEST_DIR_/_TEST_PHP_FILE_"
PHPUNIT_TABNAME = "${shortEnvName}_API"

JUNIT_TEMPLATE = "SeamusTestCookbook"
JUNIT_REMOTECOMMAND = "~/testrunner.sh http://_TEST_SERVER_/new_cms/servlet/runTests?tests=_TESTSUITE_"
JUNIT_TABNAME = "${folderName}_Seamus"
BASE_TEST_NAME = "${folderName}_"
def lists = [
        [name: PHPUNIT_TABNAME, regex: "${BASE_TEST_NAME}Api.*"],
        [name: JUNIT_TABNAME, regex: "${BASE_TEST_NAME}Seamus.*"],
        [name: "${BASE_TEST_NAME}Carbon", regex: "${BASE_TEST_NAME}Carbon.*"]
]

def traverseWorkspaceDir(File path, jobs) {
    phpUnitMaster = new MultiJob()
    phpUnitMaster.name = "${BASE_TEST_NAME}Api_Master"
    phpUnitMaster.listView = "${BASE_TEST_NAME}Master"

    path.traverse { file ->
        if (!file.isDirectory() && !(file.name.matches(/^\.[A-z]*/))) {
            addFileToJobsList(file, jobs)
            phpUnitMaster.jobs << BASE_TEST_NAME + "Api_" + file.name.replaceAll(/\.[a-z]*$/, "")
        }
    }

    jobs << phpUnitMaster
}

def JUnitTestSuites = [
        "all",
        "podchan",
        "podepi",
        "robots",
        "genre",
        "rights",
        "show",
        "program",
        "musicquery",
        "musicbuttons",
        "resource",
        "audioresource",
        "sites",
        "artist",
        "hub",
        "topicstatic",
        "topicinstance",
        "perf",
        "defaultloc",
        "defaultcon",
        "ptconcurrency",
        "expiration",
        "encoding",
        "publish",
        "user",
        "crowd",
        "html",
        "modelpub",
        "pagestatic",
        "pageinstance",
        "text",
        "listtext",
        "charscrub",
        "image",
        "books",
        "newsflex",
        "newsflexstatic",
        "lightweightPageThing",
        "updatablePageThing",
        "pageThingFactory",
        "lightweightResources",
        "promoLocations",
        "resourceAssociation",
        "moveResource",
        "codes",
        "singleprop",
        "transcript",
        "nprone",
        "tagsuggest",
        "homepageprocessor",
        "associatedstories",
        "firewall",
        "pluckedPodcastsApi",
        "rssChannelGetter"
]

def traverseJunitSuites(junitSuites, jobs) {
    def jUnitMasterJob = new MultiJob()
    jUnitMasterJob.name = "${BASE_TEST_NAME}Seamus_Master"
    jUnitMasterJob.listView = "${FOLDER_NAME}_Master"

    junitSuites.each { String suite ->
        curJob = new Job()
        curJob.name = BASE_TEST_NAME + "Seamus_" + suite.replaceAll(/\.[a-z]*$/, "")
        curJob.template = JUNIT_TEMPLATE
        curJob.listView = JUNIT_TABNAME
        curJob.testName = suite.toUpperCase()
        curJob.remoteCommand = "~/testrunner.sh http://${SERVER_NAME}/new_cms/servlet/runTests?tests=${suite}"
        jUnitMasterJob.jobs << suite

        jobs << curJob
    }
    jobs << jUnitMasterJob
}

def String getFileExtension(String name) {
    int i = name.lastIndexOf('.')
    return name.substring(i + 1)
}

def addFileToJobsList(File file, jobs) {
    curJob = new Job()
    switch (getFileExtension(file.name).toUpperCase()) {
        case PHPUNIT:
            curJob.name = BASE_TEST_NAME + "Api_" + file.name.replaceAll(/\.[a-z]*$/, "")
            curJob.template = PHPUNIT_TEMPLATE

            curJob.listView = PHPUNIT_TABNAME
            curJob.testPath = file.canonicalPath.replaceAll(/[\/A-z]*\/unittest/, ".")
            curJob.testName = file.name
            curJob.remoteCommand = "cd /www/netsite-docs/\nphpunit -c phpunit.xml ${curJob.testPath}/${curJob.testName}"
            break;
        case JUNIT:
            curJob.name = BASE_TEST_NAME + "Seamus_" + file.name.replaceAll(/\.[a-z]*$/, "")
            curJob.template = JUNIT_TEMPLATE
            curJob.listView = JUNIT_TABNAME
            curJob.testName = file.name
            curJob.remoteCommand = JUNIT_REMOTECOMMAND
            break;
    }

    jobs << curJob
}

def writeJsonToFile(String json, String fileName) {
    new File(WORKSPACE + "/" + CONFIG_DIR).mkdir()
    new File(WORKSPACE + "/" + CONFIG_PATH).withWriter('utf-8') { writer ->
        writer.write(json)
    }
}

/*
Maps to a nested view that will contain pre-conditioned
tabs inside (e.g. Active, All, Quarantine...)
 */
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
    def listView
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

def globals = new Globals()
globals.folderName = folderName
globals.serverName = SERVER_NAME

traverseWorkspaceDir(fullPath, jobs)
traverseJunitSuites(JUnitTestSuites, jobs)
def testConfig = new Config()
testConfig.globals = globals
testConfig.jobs = jobs
lists.each { list ->
    curList = new List()
    curList.displayName = list.name
    curList.regex = list.regex
    testConfig.lists << curList
}
writeJsonToFile(JsonOutput.toJson(testConfig), "TestConfig.json")