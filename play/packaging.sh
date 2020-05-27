#!/bin/bash
set -eu
SCRIPT_DIR=$(cd $(dirname "${0}") && pwd) # bourne shell系依存の殺り方
REVISION=$(git show -s --format=%H)

./sbt.sh 'project message-api' universal:packageBin
./sbt.sh 'project message-api' assembly
cd ${SCRIPT_DIR}

# message-api
repository="asia.gcr.io/${ALBOT_PROJECT_ID}/albot-message-api"
revisiontag="${repository}:${REVISION}"
docker build -t ${repository} -f ./services/api/Dockerfile .
docker tag ${repository} ${revisiontag}
gcloud docker -- push ${repository}
gcloud docker -- push ${revisiontag}
docker rmi ${repository} ${revisiontag}

# message-broker
repository="asia.gcr.io/${ALBOT_PROJECT_ID}/albot-message-broker"
revisiontag="${repository}:${REVISION}"
docker build -t ${repository} -f ./services/broker/Dockerfile .
docker tag ${repository} ${revisiontag}
gcloud docker -- push ${repository}
gcloud docker -- push ${revisiontag}
docker rmi ${repository} ${revisiontag}
