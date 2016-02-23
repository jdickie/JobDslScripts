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

def lists = [PHPUNIT_TABNAME, JUNIT_TABNAME]

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
    curJob.name = file.name
    switch (getFileExtension(file.name).toUpperCase()) {
        case PHPUNIT:
            curJob.template = PHPUNIT_TEMPLATE
            curJob.remoteCommand = PHPUNIT_REMOTECOMMAND
            curJob.listView = PHPUNIT_TABNAME
            curJob.testPath = file.canonicalPath.replaceAll(/[\/A-z]*\/unittest/, ".")
            curJob.testName = file.name.replaceAll(/\.[a-z]*$/, "")
            break;
        case JUNIT:
            curJob.template = JUNIT_TEMPLATE
            curJob.remoteCommand = JUNIT_REMOTECOMMAND
            curJob.listView = JUNIT_TABNAME
            curJob.testName = file.name.replaceAll(/\.[a-z]*/, '')
            break;
    }

    jobs << curJob
}

def writeJsonToFile(String json, String fileName) {
    new File(WORKSPACE + '/configJson').mkdir()
    new File(WORKSPACE + '/configJson', fileName).withWriter('utf-8') { writer ->
        writer.write(json)
    }
}

class Config {
    def jobs
    def lists
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

def globals = new Globals()
globals.folderName = folderName
globals.serverName = SERVER_NAME

traverseWorkspaceDir(fullPath, jobs)
traverseJunitSuites(JUnitTestSuites, jobs)
def testConfig = new Config()
testConfig.globals = globals
testConfig.jobs = jobs
testConfig.lists = lists
writeJsonToFile(JsonOutput.toJson(testConfig), "TestConfig.json")