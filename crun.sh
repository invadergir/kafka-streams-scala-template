#!/bin/bash

#set -u # no uninitialized var usage.
ARGS_ARRAY=("${@}")
NUM_ARGS=${#ARGS_ARRAY[@]}
THISDIR=$(dirname $(readlink -e ${BASH_SOURCE[0]}))
THISPROG=$(basename $(readlink -e ${BASH_SOURCE[0]}))

usageText="
SYNTAX:  $THISPROG  [options]

Run the container.

OPTIONS:

-b = run bash instead of the normal entrypoint
-r = run as root (superuser)

EXAMPLES:  

    $THISPROG some-topic
    $THISPROG -b [-r]  # for bash
"

Syntax()
{
    set +x
    echo "$usageText"
    exit 101
}

err()
{
    set +x
    set +u
    >&2 echo
    >&2 echo "ERROR - $1"
    >&2 echo
    >&2 echo "Type '$THISPROG -h' for help."
    >&2 echo
    CODE="$2"
    [ -z "$CODE" ] && CODE=1
    exit $CODE
}

# Parse the arguments.
runBash=false
useRoot=""
for (( ix = 0; ix < ${NUM_ARGS}; ix++ )); do
    arg="${ARGS_ARRAY[${ix}]}"
    if [[ $arg = "-h" || $arg = "--help" ]]; then
        Syntax
    elif [[ $arg = "-b" || $arg = "--bash" ]]; then
        runBash=true
    elif [[ $arg = "-r" || $arg = "--root" ]]; then
        useRoot="--user root"
    else # a regular param
        err "unexpected argument:  $arg"
    fi
done

set -x
set -e

NAME=kstemplate
podName=mkafkaPod
netOpt=""
#netOpt="--hostname $NAME --network=network:kafkanet"
stdOpt=" --name $NAME $useRoot --pod=$podName"

podman rm --ignore -v $NAME

if $runBash; then
    podman run -it -e KAFKA_HOST -e KAFKA_PORT $netOpt $stdOpt --entrypoint bash kstemplate:latest
else # normally:
    podman run -it -e KAFKA_HOST -e KAFKA_PORT $netOpt $stdOpt kstemplate:latest "$topicName"
fi

