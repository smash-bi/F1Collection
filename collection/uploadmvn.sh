#!/bin/sh 

display_usage() {
    echo "Upload the specified artifact to a S3 bucket."
    echo "\nUsage:\n$0 version\n"
}
if [ $# -ne 1 ]; then
    display_usage
    exit 1
fi

command -v mvn >/dev/null 2>&1 || { echo >&2 "mvn is required but it's not installed.  Aborting."; exit 1; }

if [[ ( $# == "--help") || $# == "-h" ]];then 
    display_usage
    exit 0
fi

if [ -f upload.env ]; then
    source upload.env
fi 

mvn deploy:deploy-file \
-DgroupId=smash.f1 \
-DartifactId=smash-f1-collection \
-Dversion=$1 \
-Dpackaging=jar \
-Dfile=target/smash-f1-collection-$1.jar \
-DrepositoryId=repo.smash.bi \
-Durl=s3://repo.smash.bi/release
