# https://stackoverflow.com/questions/44763140/how-to-test-if-hbase-is-correctly-running

IS_RUNNING=$(sudo docker exec hbase-master bash -c 'if echo -e "list" | hbase shell 2>&1 | grep -q "ERROR:" 2>/dev/null ;then echo "NO"; fi')

if [ "$IS_RUNNING" == "NO" ]; then
    echo "HBase NOT running"
    exit 1
else
    echo "HBase is running!"
    exit 0
fi
