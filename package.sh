if [ -z "$1" ]; then
    echo "Error: package target is required."
    echo "Package target is one of basic-target zk-3.6.3, hdfs-3.3.1, hbase-2.4.8, zk-3.8.1. hdfs-3.3.5, hbase-2.4.11"
    exit 1
fi

OWN_DIR=$( cd -- "$( dirname -- "${BASH_SOURCE[0]}" )" &> /dev/null && pwd )
# PACKEGE_CONF_FILE=

PACKAGE_TARGET=$1

PACKAGE_DIR=${OWN_DIR}/package/${PACKAGE_TARGET}

rm -r ${PACKAGE_DIR}

mkdir -p ${PACKAGE_DIR}

declare SCRIPT_ROOT_DIR

if [ "$PACKAGE_TARGET" = "hdfs-3.3.1" ]; then
  SCRIPT_ROOT_DIR=${OWN_DIR}/script/controller-bash/hdfs-3.3.1-60
elif [ "$PACKAGE_TARGET" = "hdfs-3.3.5" ]; then
  SCRIPT_ROOT_DIR=${OWN_DIR}/script/controller-bash/hdfs-3.3.5-60
else
  SCRIPT_ROOT_DIR=${OWN_DIR}/script/controller-bash/$PACKAGE_TARGET
fi
cp -r ${SCRIPT_ROOT_DIR}/* ${PACKAGE_DIR}

PACKAGE_CONF_DIR=${PACKAGE_DIR}/configuration
mkdir -p ${PACKAGE_CONF_DIR}
CONFIG_ROOT_DIR=${OWN_DIR}/configuration/$PACKAGE_TARGET
cp -r ${CONFIG_ROOT_DIR}/* ${PACKAGE_CONF_DIR}

cp ${OWN_DIR}/ctrl/target/FaultFuzz-ctrl-0.0.1-SNAPSHOT.jar ${PACKAGE_DIR}
cp ${OWN_DIR}/inst/target/FaultFuzz-inst-0.0.5-SNAPSHOT.jar ${PACKAGE_DIR}

#!/bin/bash

declare WORKLOAD_ROOT_DIR

if [ "$PACKAGE_TARGET" = "zk-3.6.3" ]; then
  WORKLOAD_ROOT_DIR=${OWN_DIR}/workload/ZKCases/target/FaultFuzz-workload-ZKCases-0.0.1-SNAPSHOT.jar
elif [ "$PACKAGE_TARGET" = "hdfs-3.3.1" ]; then
  WORKLOAD_ROOT_DIR=${OWN_DIR}/workload/HDFSCasesV3/target/FaultFuzz-workload-HDFSCasesV3-0.0.1-SNAPSHOT.jar
elif [ "$PACKAGE_TARGET" = "hbase-2.4.8" ]; then
  WORKLOAD_ROOT_DIR=${OWN_DIR}/workload/HBaseCases/target/FaultFuzz-workload-HBaseCases-0.0.1-SNAPSHOT.jar
elif [ "$PACKAGE_TARGET" = "zk-3.8.1" ]; then
  WORKLOAD_ROOT_DIR=${OWN_DIR}/workload/ZKCases/target/FaultFuzz-workload-ZKCases-0.0.1-SNAPSHOT.jar
elif [ "$PACKAGE_TARGET" = "hdfs-3.3.5" ]; then
  WORKLOAD_ROOT_DIR=${OWN_DIR}/workload/HDFSCasesV3/target/FaultFuzz-workload-HDFSCasesV3-0.0.1-SNAPSHOT.jar
elif [ "$PACKAGE_TARGET" = "hbase-2.4.11" ]; then
  WORKLOAD_ROOT_DIR=${OWN_DIR}/workload/HBaseCases/target/FaultFuzz-workload-HBaseCases-0.0.1-SNAPSHOT.jar
elif [ "$PACKAGE_TARGET" = "basic-target" ]; then
  WORKLOAD_ROOT_DIR=${OWN_DIR}/workload/BasicInstTarget/target/FaultFuzz-workload-BasicInstTarget-1.0-SNAPSHOT.jar
else
  echo "FaultFuzz cannot find the corresponding workload jar file! PACKAGE_TARGET is $PACKAGE_TARGET"
fi

cp ${WORKLOAD_ROOT_DIR} ${PACKAGE_DIR}/backend-configuration