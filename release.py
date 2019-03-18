
import os
import json
import argparse
import subprocess
import requests
import sys
import logging

# These two lines enable debugging at httplib level (requests->urllib3->http.client)
# You will see the REQUEST, including HEADERS and DATA, and RESPONSE with HEADERS but without DATA.
# The only thing missing will be the response.body which is not logged.
# try:
#     import http.client as http_client
# except ImportError:
#     # Python 2
#     import httplib as http_client
# http_client.HTTPConnection.debuglevel = 1

# # You must initialize logging, otherwise you'll not see debug output.
# logging.basicConfig()
# logging.getLogger().setLevel(logging.DEBUG)
# requests_log = logging.getLogger("requests.packages.urllib3")
# requests_log.setLevel(logging.DEBUG)
# requests_log.propagate = True

#########################################################

def run(cmd):
    process = subprocess.Popen(cmd, stdout=subprocess.PIPE)
    (output, _err) = process.communicate()
    if process.returncode != 0:
        return None, process.returncode
    return output.strip().decode(), None

def runNoOutput(cmd):
    process = subprocess.Popen(cmd)
    process.communicate()
    if process.returncode == 0:
        return None
    else:
        return process.returncode

def runVim(filename):
    process = subprocess.Popen(["vim", filename])
    (_output, err) = process.communicate()
    return err

# Uploading to CF requires a API token (CF_API_TOKEN)
apiToken = os.getenv("CF_API_TOKEN")
if apiToken == None or len(apiToken) < 1:
    print("No CF_API_TOKEN available!")
    sys.exit(1)

headers = {'X-Api-Token': apiToken}

parser = argparse.ArgumentParser()
parser.add_argument("-project", help="Mod project:filename:[deps]", action='append', default=[])
parser.add_argument("-tag", help="Tag to use for this release", default="")
parser.add_argument("-mcvsn", help="Game version supported by this release", default="1.12.2")
parser.add_argument("-rel", help="Release type (alpha|beta|release)", default="alpha")
parser.add_argument("-skipupload", help="Skip uploading to CF", default=False)
args = parser.parse_args()

# Pull the list of game versions
versionsReq = requests.get("https://minecraft.curseforge.com/api/game/versions", headers=headers)
versions = {}
for obj in versionsReq.json():
   versions[obj["name"]] = obj["id"]

# Translate requested versions to game version IDs
gameId = [versions[args.mcvsn]]

# If no tag was provided, use the last one on the repo
if args.tag == "":
    args.tag, err = run(["git", "describe", "--abbrev=0", "--tags"])
    if err != None:
        print("Failed to find most recent tag!")
        sys.exit(1)

print(args.tag, args.rel)

# Checkout the appropriate release from git
_, err = run(["git", "checkout", "-qf", args.tag])
if err != None:
    print("Failed to checkout tag %s" % (args.tag))
    sys.exit(1)

# Get the tag preceding the requested one
lastTag, err = run(["git", "describe", "--abbrev=0", "--tags", args.tag+"^"])
if err != None:
    print("Failed to get tag preceding %s" % (args.tag))
    sys.exit(1)

# Pull the changelog between the two tags
changelog, err = run(["git", "log", "--format=%h: %s", lastTag + ".." + args.tag])
if err != None:
    print("Failed to get changelog for %s -> %s" % (lastTag, args.tag))
    sys.exit(1)

# Dump the changelog into a temporary file
with open("/tmp/changelog.tmp", "w") as f:
    f.write(changelog)

# Open up Vim with the temporary file for editing
res = runVim("/tmp/changelog.tmp")
if res != None:
    print("VIM result: " + res)
    sys.exit(1)

# Read the changelog
with open("/tmp/changelog.tmp", "r") as f:
    changelog = "".join(f.readlines())
    print(changelog)

# Do the actual build
err = runNoOutput(["./gradlew", "-Pversion=" + args.tag, "clean", "build"])
if err != None:
    print(err)
    print("Build of %s tag failed" % (args.tag))
    sys.exit(1)

# Print changelog
print(changelog)

# For each project, extract ID, filename and dependencies and upload
for project in args.project:
    (id, name, dep) = project.split(":", 3)

    # Setup metadata field of our POST
    metadata = {"changelog": changelog, "gameVersions": gameId, "releaseType": args.rel}
    filename = "build/libs/%s-%s-%s.jar" % (name, args.mcvsn, args.tag)

    # Add any specified dependencies
    if dep != "":
        metadata["relations"] = {"projects": [{"slug": dep, "type": "requiredDependency"}]}

    if args.skipupload:
        print("Skipping upload of %s" % filename)
        continue

    # Setup files for POST
    files = [('metadata', (None, json.dumps(metadata), "application/json")),
            ('file', (open(filename, "rb")))]
    resp = requests.post("https://minecraft.curseforge.com/api/projects/%s/upload-file" % id, headers=headers, files=files)
    print(resp.text)
    resp.raise_for_status()


