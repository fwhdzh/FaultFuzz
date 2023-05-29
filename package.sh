OWN_DIR=$( cd -- "$( dirname -- "${BASH_SOURCE[0]}" )" &> /dev/null && pwd )
# PACKEGE_CONF_FILE=

PACKAGE_TARGET=zk-3.6.3

PACKAGE_DIR=${OWN_DIR}/package/${PACKAGE_TARGET}

rm -r ${PACKAGE_DIR}

mkdir -p ${PACKAGE_DIR}

SCRIPT_ROOT_DIR=${OWN_DIR}/script/controller-bash/$PACKAGE_TARGET
cp -r ${SCRIPT_ROOT_DIR}/* ${PACKAGE_DIR}

CONFIG_ROOT_DIR=${OWN_DIR}/configuration/$PACKAGE_TARGET
cp -r ${CONFIG_ROOT_DIR}/* ${PACKAGE_DIR}

cp ctrl/target/FaultFuzz-ctrl-0.0.1-SNAPSHOT.jar ${PACKAGE_DIR}
cp inst/target/FaultFuzz-inst-0.0.5-SNAPSHOT.jar ${PACKAGE_DIR}

#!/bin/bash

declare WORKLOAD_ROOT_DIR

if [ "$PACKAGE_TARGET" = "zk-3.6.3" ]; then
  WORKLOAD_ROOT_DIR=/home/fengwenhan/code/faultfuzz/workload/ZKCases/target/FaultFuzz-workload-ZKCases-0.0.1-SNAPSHOT.jar
elif [ "$PACKAGE_TARGET" = "hdfs-3.3.1" ]; then
  WORKLOAD_ROOT_DIR=workload/HDFSCasesV3/target/FaultFuzz-workload-HDFSCasesV3-0.0.1-SNAPSHOT.jar
elif [ "$PACKAGE_TARGET" = "hbase-2.4.4" ]; then
  WORKLOAD_ROOT_DIR=workload/HBaseCases/target/FaultFuzz-workload-HBaseCases-0.0.1-SNAPSHOT.jar
else
  echo "FaultFuzz cannot find the corresponding workload jar file! PACKAGE_TARGET is $PACKAGE_TARGET"
fi

cp ${WORKLOAD_ROOT_DIR} ${PACKAGE_DIR}