import javaposse.jobdsl.dsl.jobs.*

class ApiTestCookbook {
    public static void main() {}
    private FreeStyleJob apiTestJob
    private String remoteServer
    private String remoteUser
    private String remotePort
    private String jobDir
    private String jobName
    private static String DEFAULT_NAME = "Api_Test_Job"

    ApiTestCookbook(Map args) {
        this.jobDir = args.get('jobDir')
        this.jobName = args.get('jobName')
        this.remotePort = args.get('remotePort')
        this.remoteServer = args.get('remoteServer')
        this.remoteUser = args.get('remoteUser')
    }

    public FreeStyleJob buildJob() {
        if (apiTestJob == null) {
            apiTestJob = new FreeStyleJob()
            apiTestJob.setName(this.jobName)
            def shellServer = this.remoteUser + '@' + this.remoteServer + ':' + this.remotePort
            apiTestJob.steps {
                remoteShell(shellServer) {
                    command("cd ${this.jobDir}")
                    command("/usr/local/bin/phpunit ${this.jobName}.php")
                }
            }
        }
        return apiTestJob
    }
}
