# Assumes data has been loaded into topic test1

if [ -f /tmp/ccloud-env.sh ] ; then
  source /tmp/ccloud-env.sh
elif [ -f ~/tmp/ccloud-env.sh ] ; then
    source  ~/tmp/ccloud-env.sh
else
  echo "ccloud-env not found"
  exit
fi

bin/sigma-loader.sh -topic sigma-rules -dir test/rules/$1


