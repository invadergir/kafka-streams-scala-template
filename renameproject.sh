#!/bin/bash

ARGS_ARRAY=("${@}")
NUM_ARGS=${#ARGS_ARRAY[@]}
THISDIR=$(dirname $(readlink -e ${BASH_SOURCE[0]}))
THISPROG=$(basename $(readlink -e ${BASH_SOURCE[0]}))

PKGPREFIX="com.example"
ORIGNAME="kafka-streams-scala-template"
ORIGNAME_NODASHES="kafkastreamsscalatemplate"
shopt -s dotglob 

PrintManualTodo()
{
    set +x
    echo " * You'll have to edit README.md by hand."
    echo " * In addition, you'll probably want to delete this script."
}

Syntax()
{
    set +x
    echo ""
    echo "SYNTAX:  $THISPROG  NEW_NAME"
    echo ""
    echo "Adjusts this project to use the new name you specify.  Things it does:"
    echo ""
    echo "  * Renames packages from '$PKGPREFIX.$ORIGNAME_NODASHES' to '$PKGPREFIX.{NEWNAME}'"
    echo "    (scala files)"
    echo "  * Rename source dir names to match package names"
    echo ""
    echo "Options:"
    echo ""
    echo "-p NEW_PACKAGE_PREFIX = the 'old' package prefix is 'com.example', and this "
    echo "                        script will replace all instances of that with the NEW_PACKAGE_PREFIX,"
    echo "                        as well as change the directory structure to match.  If not specified,"
    echo "                        no directory changes will be made, and the packages will stay the same"
    echo "                        except for the project name which is always updated."
    echo ""
    echo "Things it does not do that you'll have to do afterwards:"
    echo ""
    PrintManualTodo
    echo ""
    echo "*** NOTE that this script requires perl to be installed.  ***"
    echo ""
    echo "EXAMPLE:  $THISPROG  my-super-fun-app  -p com.acme.mysuperfunapp"
    echo ""
    exit 101
}

err()
{
    set +x
    echo
    echo "ERROR - $1"
    echo
    echo "Type '$THISPROG -h' for help."
    echo
    exit 1
}


# Parse the arguments.
NEWNAME=""
NEW_PACKAGE_PREFIX=""
#echo "Number of arguments = $NUM_ARGS"
for (( ix = 0; ix < ${NUM_ARGS}; ix++ )); do
    arg="${ARGS_ARRAY[${ix}]}"
    #echo "next arg = [ $arg ]"
    [ ! -z "$arg" ] || err "Should not get here."
    if [ "$arg" = "-h" -o "$arg" = "--help" ]; then
        Syntax
    elif [ "$arg" = "-p" ]; then
        ((++ix))
        nextarg="${ARGS_ARRAY[${ix}]}"
        [ -n "$nextarg" -a "${nextarg:0:1}" != "-" ] || err "expected an argument after $arg."
        NEW_PACKAGE_PREFIX="$nextarg"
    else # a regular param
        if [ -z "$NEWNAME" ]; then
            NEWNAME=$arg
        else
            err "unexpected argument:  $arg"
        fi
    fi
done

# Required params:
[ -n "$NEWNAME" ] || err "The NEWNAME was not specified."

ORIGNAMELEAN="${ORIGNAME//-/}"
NEWNAMELEAN="${NEWNAME//-/}"
[ -n "$ORIGNAMELEAN" ] || err "problem calculating ORIGNAMELEAN (it was all dashes): '$ORIGNAMELEAN'"
[ -n "$NEWNAMELEAN" ] || err "problem calculating NEWNAMELEAN (it was all dashes): '$NEWNAMELEAN'"

# Make no change to package prefix if not specfied.
[ -z "$NEW_PACKAGE_PREFIX" ] && NEW_PACKAGE_PREFIX="$PKGPREFIX"

ORIGPKG="$PKGPREFIX.$ORIGNAMELEAN"
NEWPKG="$NEW_PACKAGE_PREFIX.$NEWNAMELEAN"

echo "About to:"
echo "* Rename all dirs from '$ORIGNAMELEAN' to '$NEWNAMELEAN'."
echo "* Rename all package names from '$ORIGPKG' to '$NEWPKG' in code (and .sbt files)"
echo "* Change the name of the project in build.sbt from '$ORIGNAMELEAN' to '$NEWNAMELEAN'."
echo "* Change the name of assembly jar file from '$ORIGNAME.jar' to '$NEWNAME.jar'."
echo "* Remove the .git directory."
echo ""
echo "This is the last chance to cancel.  Are you sure you want to do this?  "
echo "(Enter continues; Ctrl-C exits)"
read $garbage

cd $THISDIR || err "Couldn't cd to this dir:  $THISDIR"

function joinBy { local d=$1; shift; echo -n "$1"; shift; printf "%s" "${@/#/$d}"; }

# Parse the words in the PKGPREFIX and NEW_PACKAGE_PREFIX strings:
set -e
IFS='.' read -ra PPWORDS <<< "$PKGPREFIX"
IFS='.' read -ra NEWPPWORDS <<< "$NEW_PACKAGE_PREFIX"
set -x
PPSIZE=${#PPWORDS[*]}
NEWPPSIZE=${#NEWPPWORDS[*]}
[ $PPSIZE -eq 2 ] || err "The default PKGPREFIX must have only 2 elements; this algorithm depends on it: $PKGPREFIX."
[ $NEWPPSIZE -ge $PPSIZE ] || err "The NEW_PACKAGE_PREFIX cannot have fewer elements in it than the original '$PKGPREFIX':  '$NEW_PACKAGE_PREFIX'"

PPFIRST="${PPWORDS[0]}"
NEWPPFIRST="${NEWPPWORDS[0]}"

PPLAST="${PPWORDS[${PPSIZE}-1]}"
NEWPPLAST="${NEWPPWORDS[${NEWPPSIZE}-1]}"

PPDIRS=$(joinBy "/" ${PPWORDS[*]})
NEWPPDIRS=$(joinBy "/" ${NEWPPWORDS[*]})

MAINDIR="$(readlink -f ./src/main/scala)"
TESTDIR="$(readlink -f ./src/test/scala)"
set +e

# If the package prefix is changing:
if [ "$NEW_PACKAGE_PREFIX" != "$PKGPREFIX" ]; then

    set -e

    # Rename LAST dirs
    if [[ $PPLAST != $NEWPPLAST ]]; then
        mv -fv $MAINDIR/$PPDIRS $MAINDIR/$PPFIRST/$NEWPPLAST
        mv -fv $TESTDIR/$PPDIRS $TESTDIR/$PPFIRST/$NEWPPLAST
    fi

    # Rename FIRST dirs
    if [[ $PPFIRST != $NEWPPFIRST ]]; then
        mv -fv $MAINDIR/$PPFIRST $MAINDIR/$NEWPPFIRST
        mv -fv $TESTDIR/$PPFIRST $TESTDIR/$NEWPPFIRST
    fi

    # com/example is now new1/newX

    # Rename 'last' to temp dir, create middle dirs, then move last dirs to correct spot:
    BASEDIRS=($MAINDIR $TESTDIR)
    for DIR in "${BASEDIRS[@]}"; do
        pushd .
        # Rename last dir to temp dir.
        cd $DIR/$NEWPPFIRST
        TEMPLAST=$(mktemp -d -p .)
        rmdir $TEMPLAST
        mv $NEWPPLAST $TEMPLAST
        # create middle dirs
        cd ..
        mkdir -p $NEWPPDIRS
        # move last dir to correct spot
        mv $DIR/$NEWPPFIRST/$TEMPLAST/* $NEWPPDIRS/
        rmdir $DIR/$NEWPPFIRST/$TEMPLAST
        popd
    done

    set +e
fi

cd $THISDIR

# Move all dirs named ORIGNAME to NEWNAME
# Only set this true when testing.
DISABLE_DIR_RENAME=false
if ! $DISABLE_DIR_RENAME; then
    mapfile -t fileList < <(find . -name "$ORIGNAMELEAN" -type d)
    for FILE in "${fileList[@]}"; do 
        mv -vf "$FILE" "$(dirname $FILE)/$NEWNAMELEAN"
    done
fi

# In all scala files:
mapfile -t fileList < <(find . -name "*.scala" -type f)
for FILE in "${fileList[@]}"; do 
    # replace the package names:
    echo "Modifying package names in $FILE..."
    perl -pi -e 's/'$ORIGPKG'/'$NEWPKG'/g' $FILE

    # Also replace the project name
    perl -pi -e 's/'$ORIGNAMELEAN'/'$NEWNAMELEAN'/g' $FILE
done

# fix the conf file
mapfile -t fileList < <(find . -name "*.conf" -type f)
for FILE in "${fileList[@]}"; do 
    # replace the project name
    perl -pi -e 's/'$ORIGNAMELEAN'/'$NEWNAMELEAN'/g' $FILE
done

# In all SBT files, replace the package names:
mapfile -t fileList < <(find . -name "*.sbt" -type f)
for FILE in "${fileList[@]}"; do 
    echo "Modifying package names in $FILE..."
    perl -pi -e 's/'$ORIGPKG'/'$NEWPKG'/g' $FILE
done

# Replace the organization in build.sbt.
# We consider the organization os the first two segments in 
# the new package name.
NEWORG="${NEWPPWORDS[0]}.${NEWPPWORDS[1]}"
perl -pi -e 's/organization := "com.example"/organization := "'$NEWORG'"/g' build.sbt

# Change the project name in build.sbt:
echo "Modifying project & jar names in build.sbt..."
set -x
perl -pi -e 's/'$ORIGNAMELEAN'/'$NEWNAMELEAN'/g' build.sbt
perl -pi -e 's/'$ORIGNAME'/'$NEWNAME'/g' build.sbt
set +x

echo "Blowing away the .git dir..."
rm -rf .git

echo "Clearing out the LICENSE file.  You will have to make that decision on your own."
echo "TODO" > LICENSE

echo 
echo "The files & dirs have been modified.  Please note, you may still want to:"
PrintManualTodo
echo

