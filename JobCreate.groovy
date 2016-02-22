import groovy.json.JsonSlurper
import utilities

/**
 * JobCreate
 *
 * Generates jobs by referencing templates and passing in parameters.
 */
def parser = new JsonSlurper()
def Config = parser.parseText(CONFIG_JSON)

/**
 * Go through each job and change the parameters based on the
 * given template.
 */
Config.jobs.each {
    _job ->
        // This is JobsDSL code below
        job(_job.name) {
            parameters {
                _job.params.each {
                    _param ->
                        if (_param.type == "string") {
                            stringParam(_param.name, _param.value)
                        }
                }
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