import groovy.json.JsonSlurper
import javaposse.jobdsl.dsl.views.ListView
import javaposse.jobdsl.dsl.views.jobfilter.MatchType
import javaposse.jobdsl.dsl.views.jobfilter.RegexMatchValue
import javaposse.jobdsl.dsl.views.jobfilter.Status

/**
 * JobCreate
 *
 * Generates jobs by referencing templates and passing in parameters.
 */
def parser = new JsonSlurper()
def Config = parser.parseText(new File(WORKSPACE + "/configs/ApiTestConfig.json").text)
def namePrefix = ""
def serverName = null

if (Config.globals) {
    namePrefix = Config.globals.folderName ? Config.globals.folderName + "/" : namePrefix
    serverName = Config.globals.serverName ? Config.globals.serverName + "/" : null
    // Create folder
    folder(Config.globals.folderName) {
        displayName(Config.globals.folderName)
        description("Test environment for ${Config.globals.folderName}. Uses server ${serverName}.")
    }
}

listView(namePrefix + Config.globals.folderName + "_Master") {
    jobFilters {
        regex {
            matchType(MatchType.INCLUDE_MATCHED)
            matchValue(RegexMatchValue.NAME)
            regex(".*Master")
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

/**
 * @TODO: Investigate once we get more into PHPUnit abstraction
 */
Config.lists.each { list ->
    nestedView(namePrefix + list.displayName) {
        views {
            listView("${list.displayName}_Active") {
                jobs {
                    regex(list.regex)
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
            listView("${list.displayName}_Quarantine") {
                jobs {
                    regex(list.regex)
                }
                statusFilter(ListView.StatusFilter.DISABLED)
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
            listView("${list.displayName}_Failed") {
                jobs {
                    regex(list.regex)
                }
                jobFilters {
                    status {
                        matchType(MatchType.EXCLUDE_UNMATCHED)
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
        }
    }

}

/**
 * Go through each job and change the parameters based on the
 * given template.
 */
Config.jobs.each { _job ->
    switch (_job.type) {
        case 'job':
            job(namePrefix + _job.name) {
                if (serverName && _job.remoteCommand) {
                    // Have to manipulate XML directly here
                    configure {
                        project ->
                            project / 'builders' / 'org.jvnet.hudson.plugins.SSHBuilder' {
                                'siteName'(serverName)
                                'command'(_job.remoteCommand)
                            }
                    }
                }
                using(_job.template)
            }
            break;
        case 'master':
            multiJob(namePrefix + _job.name) {
                steps {
                    phase('A') {
                        _job.jobs.each { jobName ->
                            phaseJob(jobName)
                        }
                    }
                }
            }
            break;
    }

}