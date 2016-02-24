import groovy.json.JsonSlurper
import javaposse.jobdsl.dsl.views.jobfilter.RegexMatchValue

/**
 * JobCreate
 *
 * Generates jobs by referencing templates and passing in parameters.
 */
def parser = new JsonSlurper()
def Config = parser.parseText(new File(WORKSPACE + "/" + CONFIG_PATH).text)
def namePrefix = ""


if (Config.globals) {
    // Create folder
    folder(Config.globals.folderName) {
        displayName(Config.globals.folderName)
        description("")
    }
    namePrefix = Config.globals.folderName ? Config.globals.folderName + "/" : namePrefix
}

Config.lists.each { list ->
    nestedView(namePrefix + list.displayName) {
        views {
            listView("${list.displayName} - Active") {
                jobFilters {
                    if (list.regex) {
                        regex {
                            matchType(MatchType.INCLUDE_MATCHED)
                            matchValue(RegexMatchValue.NAME)
                            regex(list.regex)
                        }
                    }
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
            listView("${list.displayName} - All") {
                jobFilters {
                    if (list.regex) {
                        regex {
                            matchType(MatchType.INCLUDE_MATCHED)
                            matchValue(RegexMatchValue.NAME)
                            regex(list.regex)
                        }
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
            listView("${list.displayName} - Failed") {
                jobFilters {
                    if (list.regex) {
                        regex {
                            matchType(MatchType.INCLUDE_MATCHED)
                            matchValue(RegexMatchValue.NAME)
                            regex(list.regex)
                        }
                    }
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
            listView("${list.displayName} - Quarantine") {
                jobFilters {
                    if (list.regex) {
                        regex {
                            matchType(MatchType.INCLUDE_MATCHED)
                            matchValue(RegexMatchValue.NAME)
                            regex(list.regex)
                        }
                    }
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

}

/**
 * Go through each job and change the parameters based on the
 * given template.
 */
Config.jobs.each {
    _job ->
        job(namePrefix + _job.name) {
            parameters {
                stringParam("TEST_NAME", _job.testName)
                stringParam("TEST_PATH", _job.testPath)
            }
            if (_job.remoteHost && _job.remoteCommand) {
                // Have to manipulate XML directly here
                configure {
                    project ->
                        project / 'builders' / 'org.jvnet.hudson.plugins.SSHBuilder' {
                            'siteName'(_job.remoteHost)
                            'command'(_job.remoteCommand)
                        }
                }
            }
            using(_job.template)
        }
}

