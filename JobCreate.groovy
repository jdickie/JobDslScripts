import groovy.json.JsonSlurper
import javaposse.jobdsl.dsl.views.jobfilter.MatchType
import javaposse.jobdsl.dsl.views.jobfilter.RegexMatchValue
import javaposse.jobdsl.dsl.views.jobfilter.Status

/**
 * JobCreate
 *
 * Generates jobs by referencing templates and passing in parameters.
 */
def parser = new JsonSlurper()
def Config = parser.parseText(new File(WORKSPACE + "/" + CONFIG_PATH).text)
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

Config.lists.each { list ->
    if (list.name =~ /Master/) {
        listView("${list.name}") {
            jobFilters {
                regex {
                    matchType(MatchType.INCLUDE_MATCHED)
                    matchValue(RegexMatchValue.NAME)
                    regex(list.regex)
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
    } else {
        nestedView(namePrefix + list.displayName) {
            list.views.each { view ->
                listView("${view.name}") {
                    jobFilters {
                        regex {
                            matchType(MatchType.INCLUDE_MATCHED)
                            matchValue(RegexMatchValue.NAME)
                            regex(list.regex)
                        }
                        status {
                            matchType((view.statusMatchType == "exclude") ? MatchType.EXCLUDE_MATCHED : MatchType.INCLUDE_MATCHED)
                            status(view.status == "disabled" ? Status.DISABLED : Status.FAILED)
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

