import groovy.json.JsonOutput

def startingRoot = ROOT_QA_PATH
def folderName = FOLDER_NAME


def fullPath = new File(WORKSPACE + "/" + startingRoot, '.')
def jobs = []
def pathToJsonFile = new File(WORKSPACE + "/TestConfig.json")
def shortEnvName = folderName.replaceAll("/[a-z]+/", "")

PHPUNIT = "PHP"
JUNIT = "JAVA"
PHPUNIT_TEMPLATE = "ApiTestCookbook"
PHPUNIT_REMOTECOMMAND = "cd \${TEST_BASE_DIR}\nphpunit -c phpunit.xml \${TEST_DIR}/\${TEST_PHP_FILE}"
PHPUNIT_TABNAME = "API ${shortEnvName}"

JUNIT_TEMPLATE = "SeamusTestCookbook"
JUNIT_REMOTECOMMAND = "~/testrunner.sh http://\${TEST_SERVER}/new_cms/servlet/runTests?tests=\${TESTSUITE}"
JUNIT_TABNAME = "Seamus ${shortEnvName}"
BASE_TEST_NAME = "${folderName}_"
def lists = [ [name: PHPUNIT_TABNAME, regex: "${BASE_TEST_NAME}Api.*"], [name: JUNIT_TABNAME, regex: "${BASE_TEST_NAME}Seamus.*"]]


def traverseWorkspaceDir(File path, jobs) {
    path.traverse { file ->
        if (!file.isDirectory() && !(file.name.matches(/^\.[A-z]*/))) {
            addFileToJobsList(file, jobs)
        }
    }
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
    junitSuites.each { String suite ->
        curJob = new Job()
        curJob.name = suite
        curJob.template = JUNIT_TEMPLATE
        curJob.remoteCommand = JUNIT_REMOTECOMMAND
        curJob.listView = JUNIT_TABNAME
        curJob.testName = suite.toUpperCase()

        jobs << curJob
    }
}

def String getFileExtension(String name) {
    int i = name.lastIndexOf('.')
    return name.substring(i + 1)
}

def addFileToJobsList(File file, jobs) {
    curJob = new Job()
    switch (getFileExtension(file.name).toUpperCase()) {
        case PHPUNIT:
            curJob.name = BASE_TEST_NAME + "API_" + file.name.replaceAll(/\.[a-z]*$/, "")
            curJob.template = PHPUNIT_TEMPLATE
            curJob.remoteCommand = PHPUNIT_REMOTECOMMAND
            curJob.listView = PHPUNIT_TABNAME
            curJob.testPath = file.canonicalPath.replaceAll(/[\/A-z]*\/unittest/, ".")
            curJob.testName = file.name
            break;
        case JUNIT:
            curJob.name = BASE_TEST_NAME + "Seamus_" + file.name.replaceAll(/\.[a-z]*$/, "")
            curJob.template = JUNIT_TEMPLATE
            curJob.remoteCommand = JUNIT_REMOTECOMMAND
            curJob.listView = JUNIT_TABNAME
            curJob.testName = file.name
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
}

class Globals {
    def serverName
    def folderName
}

class MultiJob {
    def name
    def listView
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