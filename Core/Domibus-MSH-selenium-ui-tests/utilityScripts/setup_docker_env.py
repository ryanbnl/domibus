import os
import shutil
import xml.etree.ElementTree as ET
import yaml
from yaml import dump

file_name = "../../../../docker/development/corners/docker-compose-tc-mysql-mt.yml"
pom_filename = "../../../pom.xml"
my_webserver_key = "tomcat"

# GET YML FILE CONTENT
file = open(file_name)
content = file.read()
file.close()

# GET DOMIBUS VERSION FROM POM FILE
version = ""
root = ET.parse(pom_filename).getroot()
for child in root:
    if "version" in child.tag:
        version = child.text.strip()

# Load in yaml obj and put correct ports for wildfly
data = yaml.unsafe_load(content)

# set correct version for all services
services = data['services']
for service in services:
    image_text = data["services"][service]["image"]
    version_start_index = image_text.rfind(":$") + 1
    new_image_text = 'edelivery-docker.devops.tech.ec.europa.eu/' + image_text[:version_start_index] + version

    del data['services'][service]['image']
    data["services"][service]['image'] = new_image_text

# set ports and container name (necessary for selfsending)
obj = data['services'][my_webserver_key]
if "ports" in obj:
    del obj["ports"]

obj["ports"] = ["9088:8080"]
obj["container_name"] = "docker_local_wildfly"
obj["environment"].append("domibus.ui.session.timeout=300")
output = dump(data)

# create docker folder and pour yaml content in it
folder_path = "../docker"
yml_path = folder_path + "/docker-compose.yml"

if os.path.exists(folder_path):
    shutil.rmtree(folder_path)

os.makedirs(folder_path)

download_path = "../downloadFiles"
if os.path.exists(download_path):
    shutil.rmtree(download_path)

os.makedirs(download_path)

with open(yml_path, 'w') as f:
    print(output, file=f)
