import React, { useState, useEffect } from "react";
import {
  Grid,
} from "@material-ui/core";
import { useTheme } from "@material-ui/styles";
import {
  ResponsiveContainer,
  AreaChart,
  Area,
} from "recharts";

// styles
import useStyles from "./styles";

// components
import Widget from "../../components/Widget";
import PageTitle from "../../components/PageTitle";
import { Typography } from "../../components/Wrappers";

var lastRecordCount = 0;
var recordArray = [];
var lastDetectionCount = 0;
var detectionArray = new Array(10).fill(0);

var recordCounts = [
  { value: 0 },
  { value: 0 },
  { value: 0 },
  { value: 0 },
  { value: 0 },
  { value: 0 },
  { value: 0 },
  { value: 0 },
  { value: 0 },
  { value: 0 },
];

var detectionCounts = [
  { value: 0 },
  { value: 0 },
  { value: 0 },
  { value: 0 },
  { value: 0 },
  { value: 0 },
  { value: 0 },
  { value: 0 },
  { value: 0 },
  { value: 0 },
];

export default function Dashboard(props) {
  var classes = useStyles();
  var theme = useTheme();

  // local
  const [ruleStats, setRuleStats] = useState([{}])
  const [processorStats, setProcessorStats] = useState([{}]);
  const [recordTable, setRecordTable] = useState([{}]);
  const [detectionTable, setDetectionTable] = useState([{}]);

  const SERVER_ENDPOINT = process.env.REACT_APP_SERVER_ENDPOINT;

  const refreshTable = async () => {
     updateRules();
     updateProcessors();
  }

  const updateRules = async () => {
    try {
      const rules = await (await fetch(SERVER_ENDPOINT + "sigmaRules")).json()

      const products = new Map();
      const services = new Map();
      const authors = new Map();
      rules.forEach(rule => {
        var ruleJson = JSON.parse(JSON.stringify(rule));
        if (ruleJson.logsource.product != null) {
          products.set(ruleJson.logsource.product, 1);
        }
        if (ruleJson.logsource.service != null) {
          services.set(ruleJson.logsource.service, 1);
        }
        if (ruleJson.author != null) {
          authors.set(ruleJson.author, 1);
        }

      });

      var updatedStats = {
        totalCount: rules.length,
        totalProducts: products.size,
        totalServices: services.size,
        totalAuthors: authors.size
      }

      setRuleStats(updatedStats);
    } catch (err) {
      console.log(err.message)
    }
  }

    const updateProcessors = async () => {
    try {
      const processors = await (await fetch(SERVER_ENDPOINT + "processorStates")).json()

      var records = 0;
      var detections = 0;
      var newRecords = 0;
      var newDetections = 0;

      processors.forEach(processor => {
        var processorJson = JSON.parse(JSON.stringify(processor));
        records += processorJson.recordsProcessed;
        detections += processorJson.numMatches;
      });
      if (lastRecordCount == 0) {
        lastRecordCount = records;
      }
      newRecords = records - lastRecordCount;
      const newRecordData = [];
      for (let i = 1; i < 10; i++) {
        newRecordData.push(recordCounts.at(i));
      };
      newRecordData.push({ value: newRecords });
      recordCounts = newRecordData;
      setRecordTable(newRecordData);

      if (lastDetectionCount == 0) {
        lastDetectionCount = detections;
      }
      newDetections = detections - lastDetectionCount;
      const newDetectionData = [];
      for (let i = 1; i < 10; i++) {
        newDetectionData.push(detectionCounts.at(i));
      };
      newDetectionData.push({ value: newDetections });
      detectionCounts = newDetectionData;
      setDetectionTable(newDetectionData);

      var updatedStats = {
        totalCount: processors.length,
        totalRecords: records,
        totalDetections: detections
      }
      setProcessorStats(updatedStats);

      lastRecordCount = records;
      lastDetectionCount = detections;
    } catch (err) {
      console.log(err.message)
    }
  }


  useEffect(() => {
    const interval = setInterval(() => {
      refreshTable();
    }, 1000);

    return () => clearInterval(interval);
  }, []);

  useEffect(() => {
    refreshTable();
  }, []);

  return (
    <>
      <PageTitle title="Dashboard"/>
      <Grid container spacing={4}>
        <Grid item md={4} sm={6} xs={12}>
          <Widget
            title="Sigma Rules"
            upperTitle
            bodyClass={classes.fullHeightBody}
            className={classes.card}
          >
            <div className={classes.visitsNumberContainer}>
              <Grid container item alignItems={"center"}>
                <Grid item xs={6}>
                  <Typography color="text" colorBrightness="secondary" noWrap>
                    Total Count
                  </Typography>
                  <Typography size="xl" weight="medium" noWrap>
                    {ruleStats.totalCount}
                  </Typography>
                </Grid>
              </Grid>
            </div>
            <Grid
              container
              direction="row"
              justify="space-between"
              alignItems="center"
            >
              <Grid item xs={4}>
                <Typography color="text" colorBrightness="secondary" noWrap>
                  Products
                </Typography>
                <Typography size="md">{ruleStats.totalProducts}</Typography>
              </Grid>
              <Grid item xs={4}>
                <Typography color="text" colorBrightness="secondary" noWrap>
                  Services
                </Typography>
                <Typography size="md">{ruleStats.totalServices}</Typography>
              </Grid>
              <Grid item xs={4}>
                <Typography color="text" colorBrightness="secondary" noWrap>
                  Authors
                </Typography>
                <Typography size="md">{ruleStats.totalAuthors}</Typography>
              </Grid>
            </Grid>
          </Widget>
        </Grid>
        <Grid item sm={6} xs={12}>
          <Widget
            title="Processor Overview"
            upperTitle
            className={classes.card}
            bodyClass={classes.fullHeightBody}
          >
            <Grid item xs={6}>
              <Typography color="text" colorBrightness="secondary" noWrap>
                Total Count
              </Typography>
              <Typography size="xl" weight="medium" noWrap>
                {processorStats.totalCount}
              </Typography>
            </Grid>
            <div className={classes.serverOverviewElement}>
              <Grid item xs={6}>
                <Typography color="text" colorBrightness="secondary" noWrap>
                  Records Processed
                </Typography>
                <Typography
                  color="text"
                  colorBrightness="secondary"
                  className={classes.serverOverviewElementText}
                  noWrap
                >
                  {processorStats.totalRecords}
                </Typography>
              </Grid>
              <div className={classes.serverOverviewElementChartWrapper}>
                <ResponsiveContainer height={50} width="99%">
                  <AreaChart data={recordTable}>
                    <Area
                      type="natural"
                      dataKey="value"
                      stroke="#808080"
                      fill="#808080"
                      strokeWidth={2}
                      fillOpacity="0.2"
                      isAnimationActive="false"
                    />
                  </AreaChart>
                </ResponsiveContainer>
              </div>
            </div>
            <div className={classes.serverOverviewElement}>
              <Grid item xs={6}>
                <Typography color="text" colorBrightness="secondary" noWrap>
                  Detections
                </Typography>
                <Typography
                  color="text"
                  colorBrightness="secondary"
                  className={classes.serverOverviewElementText}
                  noWrap
                >
                  {processorStats.totalDetections}
                </Typography>
              </Grid>
              <div className={classes.serverOverviewElementChartWrapper}>
                <ResponsiveContainer height={50} width="99%">
                  <AreaChart data={detectionTable}>
                    <Area
                      type="natural"
                      dataKey="value"
                      stroke="#ff0000"
                      fill="#ff0000"
                      strokeWidth={2}
                      fillOpacity="0.2"
                      isAnimationActive="false"
                    />
                  </AreaChart>
                </ResponsiveContainer>
              </div>
            </div>
          </Widget>
        </Grid>
      </Grid>
    </>
  );
}

// #######################################################################
function getRandomData(length, min, max, multiplier = 10, maxDiff = 10) {
  var array = new Array(length).fill();
  let lastValue;

  console.log("array: " + array);

  return array.map((item, index) => {
    let randomValue = Math.floor(Math.random() * multiplier + 1);

    while (
      randomValue <= min ||
      randomValue >= max ||
      (lastValue && randomValue - lastValue > maxDiff)
    ) {
      randomValue = Math.floor(Math.random() * multiplier + 1);
    }

    //console.log("index: " + index);
    lastValue = randomValue;

 
    return { value: randomValue };
  });
}