/**
 * Sets up the folder for the future created test jobs
 */

def folderName = ENVIRONMENT ?: "StageX"
def serverName = ENVIRONMENT_SSH_HOST ?: "cms@stagex.npr.org:22"

// Create folder
folder(folderName) {
    displayName(Config.globals.folderName)
    description("Test environment for ${Config.globals.folderName}. Uses server ${serverName}.")
}