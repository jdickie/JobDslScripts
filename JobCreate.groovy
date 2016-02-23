import groovy.json.JsonSlurper

/**
 * JobCreate
 *
 * Generates jobs by referencing templates and passing in parameters.
 */
def parser = new JsonSlurper()
def Config = parser.parseText(CONFIG_JSON)
def namePrefix = ""

if (Config.globals) {
    // Create folder
    folder(Config.globals.folderName) {
        displayName(Config.globals.folderName)
        description("")
    }
    namePrefix = Config.globals.folderName ? Config.globals.folderName + "/" : namePrefix
}

/**
 * Go through each job and change the parameters based on the
 * given template.
 */
Config.jobs.each {
    _job ->
        // This is JobsDSL code below
        job(namePrefix + _job.testName) {
            parameters {
                stringParam("TEST_NAME", _job.name)
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