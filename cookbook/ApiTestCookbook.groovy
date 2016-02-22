package cookbook

class ApiTestCookbook {
    static void createTest(test) {
        freeStyleJob("${test.name}") {
            displayName(test.name)
            steps {
                remoteShell("${test.ssh_user}@${test.ssh_server}:22") {
                    command("cd ${jobDir}")
                    command("/usr/local/bin/phpunit ${test.test_file}.php")
                }
            }
        }
    }
}
