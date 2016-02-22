package utilities

class PHPUnitJob {
    static void createJob(def _job) {
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
        }
    }
}
