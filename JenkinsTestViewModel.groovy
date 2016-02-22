import javaposse.jobdsl.dsl.jobs.FreeStyleJob
import javaposse.jobdsl.dsl.views.ListView
import javaposse.jobdsl.dsl.views.jobfilter.MatchType
import javaposse.jobdsl.dsl.views.jobfilter.Status

String baseDir = "/www/netsite-docs/qa/unittest/api"
String folderName = BUILD_ENVIRONMENT ? BUILD_ENVIRONMENT : "StageY"
String domain = BUILD_DOMAIN ? BUILD_DOMAIN : "StageY"
String buildUser = BUILD_USER ? BUILD_USER : "cms"
String baseTestNamePrefix = "QA_${folderName}_"
String viewName = folderName.replaceAll("/[a-z]+/", "")
def allTestNames = []
def allTests = []

/**
 *
 * @param jobName
 * @param jobDir
 * @param folderName
 * @param buildUser
 * @param domain
 */
public void createJob(String jobName, String jobDir, String folderName, String buildUser, String domain) {
    freeStyleJob("${folderName}/${jobName}") {
        displayName(jobName)

        steps {
            remoteShell("${buildUser}@${domain}:22") {
                command("cd ${jobDir}")
                command("/usr/local/bin/phpunit ${jobName}.php")
            }
        }
    }
}

public FreeStyleJob createApiCookbook(Map args) {
    ApiTestCookbook apiTest = new ApiTestCookbook(args)
    return apiTest.buildJob()
}

ListView createListViewWithNames(String listViewName, ArrayList names) {
    ListView aView = new ListView()
    aView.jobs {
        name.each { jobName -> name(jobName) }
    }
    aView.columns {
        name()
        status()
        weather()
        lastDuration()
        lastSuccess()
        lastFailure()
        buildButton()
        progressBar()
    }
    return aView
}

allTests << [name: "Authorization", path: "${baseDir}/authorization", tests: ["TestAuthorizationApi"]]
allTests << [name: "Bus", path: "${baseDir}/bus", tests: ["TestStationsApi", "TestStreamsApi"]]
allTests << [name: "Genres", path: "${baseDir}/genres/functional", tests: ["TestGenresAPISmokeTests"]]
allTests << [name: "Ingest", path: "${baseDir}/ingest", tests: ["TestIngestAudio",
                                                                "TestIngestAuthentication",
                                                                "TestIngestDelete",
                                                                "TestIngestGet",
                                                                "TestIngestPost",
                                                                "TestNPRMLBioIngest",
                                                                "TestNPRMLBlogIngest",
                                                                "TestNPRMLCategoryIngest",
                                                                "TestNPRMLIngestElements",
                                                                "TestNPRMLIngestErrors",
                                                                "TestNPRMLIngestWithTopics",
                                                                "TestNPRMLShowIngest",
                                                                "TestNPRMLTagIngest",
                                                                "TestRSSIngestElements",
                                                                "TestRSSIngestWithTopics"]]
allTests << [name: "List", path: "${baseDir}/list/functional", tests: ["TestListAPISmokeTests"]]
allTests << [name: "List - JSON", path: "${baseDir}/list/JSON", tests: ["TestListApiJSON"]]
allTests << [name: "List - NPRML", path: "${baseDir}/list/NPRML", tests: ["TestItem"]]
allTests << [name: "Player - Functional", path: "${baseDir}/player/functional", tests: ["TestPlayerAPI", "TestPlayerAPISmokeTests", "TestUserPlaylist"]]
allTests << [name: "Station", path: "${baseDir}/station", tests: ["TestCoverageUrl",
                                                                  "TestIdentifierAudioUrl",
                                                                  "TestImage",
                                                                  "TestNetwork",
                                                                  "TestSignal",
                                                                  "TestStation",
                                                                  "TestStations",
                                                                  "TestTitle",
                                                                  "TestUrl"]]
allTests << [name: "Station - Functional", path: "${baseDir}/station/functional", tests: ["TestAuthentication", "TestIphoneAppAPI", "TestMobileAPI",
                                                                                          "TestRandomizeQuery", "TestSortQuery", "TestStatusExclusions"]]
allTests << [name: "Station - JSON", path: "${baseDir}/station/JSON", tests: ["TestStationApiJSON"]]
allTests << [name: "Story - ATOM", path: "${baseDir}/story/ATOM", tests: ["TestLink"]]
allTests << [name: "Story - HTML", path: "${baseDir}/story/HTML", tests: ["TestLink"]]
allTests << [name: "Story - JSON", path: "${baseDir}/story/JSON", tests: ["TestAudio",
                                                                          "TestAudio4",
                                                                          "TestExternalAsset",
                                                                          "TestImage",
                                                                          "TestMultimedia",
                                                                          "TestPlanetMoney",
                                                                          "TestStoryApiJSON"]]
allTests << [name: "Story - NPRML", path: "${baseDir}/story/NPRML", tests: ["ApiNprmlPodtracRuleTest",
                                                                            "TestChildStory",
                                                                            "TestLayout",
                                                                            "TestMultimedia",
                                                                            "TestStaticGraphic",
                                                                            "PodcastChannelImageTest",
                                                                            "TestCollection",
                                                                            "TestLink",
                                                                            "TestPromoArt",
                                                                            "TestTitle",
                                                                            "TestAudio",
                                                                            "TestExternalAsset",
                                                                            "TestMP3",
                                                                            "TestRelatedLink",
                                                                            "TestTrait",
                                                                            "TestAuthor",
                                                                            "TestImage",
                                                                            "TestMember",
                                                                            "TestSong"]]
allTests << [name: "Story - RSS - FIA", path: "${baseDir}/story/RSS/FIA", tests: ["ApiFIAChannelElementTest",
                                                                                  "ApiFIAFilterOutPrimaryAudioStoriesRuleTest",
                                                                                  "ApiFIAHtmlElementTest",
                                                                                  "ApiFIAHtmlStrategyTest",
                                                                                  "ApiFIAItemElementTest",
                                                                                  "ApiFIARssDocumentTest"]]
allTests << [name: "Story - RSS - news", path: "${baseDir}/story/RSS/news", tests: ["GuidTest"]]
allTests << [name: "Story - RSS - podcast", path: "${baseDir}/story/RSS/podcast", tests: ["ApiPodcastPodtracRuleTest",
                                                                                          "ApiPodcastTritonRuleTest",
                                                                                          "ApiRyopPodtracRuleTest",
                                                                                          "ApiRyopTritonRuleTest",
                                                                                          "PodcastGuidTest",
                                                                                          "PodlayerGuidTest",
                                                                                          "TestPodcast"]]
allTests << [name: "Story - Functional", path: "${baseDir}/story/functional", tests: ["TestATCPermissions", "TestAtom", "TestChildOnlyStory", "TestDistributionRights", "TestDoNotSyndicateFlag",
                                                                                      "TestDrawerAssets", "TestExitPoints",
                                                                                      "TestGetUnderwritingTransform",
                                                                                      "TestHTMLWidget",
                                                                                      "TestLegacyRSS",
                                                                                      "TestM3U.php",
                                                                                      "TestMLESmokeTests",
                                                                                      "TestMP3Download.php",
                                                                                      "TestMP3Method.php",
                                                                                      "TestMediaIdTransform",
                                                                                      "TestMusicLists",
                                                                                      "TestNPRMLError",
                                                                                      "TestNPRMLGeneric",
                                                                                      "TestNPRMLNewsletters",
                                                                                      "TestNPRMLPublicUser",
                                                                                      "TestNPRMLStationUser",
                                                                                      "TestNPRMLWebsiteUser",
                                                                                      "TestRSSGeneric",
                                                                                      "TestRSSPublicUser",
                                                                                      "TestRSSRequiredAssets",
                                                                                      "TestRenumberLayoutTransform",
                                                                                      "TestStoryFieldFilterTransform"]]
allTests << [name: "Story - RYOP", path: "${baseDir}/story/RYOP", tests: ["TestEnclosure"]]
allTests << [name: "Transcript - Functional", path: "${baseDir}/transcript/functional", tests: ["TestTranscript"]]
allTests << [name: "User - Functional", path: "${baseDir}/user/functional", tests: ["TestUserAPI", "TestUserAPISmokeTests"]]

// Go through map and generate tests
allTests.each { test ->
    test.get('tests').each { jobName ->
        // Make the official job name be a mix of the View Name + Job for uniqueness
        jobName = baseTestNamePrefix + test.get('name').trim().replaceAll("-", "_") + "_" + jobName
        // Add it to our test name manifest
        allTestNames << jobName
        createJob(jobName, test.get('path'), folderName, buildUser, domain)
    }
}

// Put all tests inside a workspace folder - this has to be generated first
// in order to be applied elsewhere
folder(folderName) {
    displayName("${folderName}")
    description("Folder containing functional tests for ${folderName}")
}

/** Generating views with following hieararchy:
 * folderName/API
 *   API folderName - Active
 *   API folderName - All
 *   API folderName - Failed
 *   API folderName - Quarantine
 */
nestedView("${folderName}/API") {
    views {
        listView("API ${viewName} - Active") {
            jobFilters {
                status {
                    matchType(MatchType.EXCLUDE_MATCHED)
                    status(Status.DISABLED)
                }
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
        listView("API ${viewName} - All") {
            jobs {
                allTestNames.each {
                    fullTestName -> name(fullTestName)
                }
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
        listView("API ${viewName} - Failed") {
            jobFilters {
                status {
                    matchType(MatchType.INCLUDE_MATCHED)
                    status(Status.FAILED)
                }
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
        listView("API ${viewName} - Quarantine") {
            jobFilters {
                status {
                    matchType(MatchType.INCLUDE_MATCHED)
                    status(Status.DISABLED)
                }
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


    }
}